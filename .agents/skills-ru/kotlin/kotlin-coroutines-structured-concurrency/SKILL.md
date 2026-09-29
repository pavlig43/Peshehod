---
name: kotlin-coroutines-structured-concurrency
description: Используй при написании или проверке Kotlin-кода, который хранит CoroutineScope, запускает coroutine из init или обычного API, вызывает runBlocking либо ловит общие исключения вокруг suspend-вызовов.
---

# Корутины Kotlin: структурная конкурентность

## Главный принцип

Хорошо устроенная coroutine — отдельная асинхронная работа с одним входом и выходом, привязанная к жизненному циклу, который известен в месте вызова.

**Scope обычно должен принадлежать вызывающему коду, а не храниться в вызываемом объекте.** Поле `CoroutineScope` требует пристальной проверки: класс должен отвечать за отмену, ошибки, перезапуск и жизненный цикл. Большинство репозиториев, менеджеров, сценариев и источников данных этого не делают; им лучше открывать `suspend` API.

Типичная правка: **сделать API `suspend` и оставить владение scope вызывающему коду.**

## Когда применять

- Класс хранит `private val scope: CoroutineScope`, полученный через конструктор.
- В `init { scope.launch { ... } }` начинается работа.
- Публичная обычная функция запускает `scope.launch { ... }`.
- В коде приложения, способном вызвать suspend-функцию, стоит `runBlocking { ... }`; либо он стоит в тесте, где нужен `runTest`.
- Вокруг suspend-вызова есть `runCatching { suspendCall() }` или `catch (Exception/Throwable)` без повторного выброса `CancellationException`.
- `catch (e: CancellationException)` перехватывает отмену и не выбрасывает её снова.

## Тихий сбой после отмены

Опасность чужого `CoroutineScope` в поле: **после отмены scope любой новый `launch` в нём сразу завершится как отменённый — без ошибки и сообщения.** Работа просто не начнётся. Это трудно найти, когда объект долго хранит ссылку на жизненный цикл, которым не владеет.

При `suspend` API такой скрытой границы нет: либо scope вызывающего кода жив и работа идёт, либо сам вызов отменён и вызывающий код об этом знает.

## Ошибочные шаблоны и правки

### 1. CoroutineScope как поле

```kotlin
// ❌ ПЛОХО
@Inject
class UserRepository(
    private val scope: CoroutineScope,
    private val api: UserApi,
) {
    fun refresh() {
        scope.launch { _state.value = api.fetchUser() }
    }
}

// ✅ ХОРОШО
@Inject
class UserRepository(
    private val api: UserApi,
) {
    suspend fun refresh(): User = api.fetchUser()
}
```

Репозиторию больше не нужно знать о scope. Вызывающий код — ViewModel или сценарий — выбирает scope, обработку ошибок и смысл отмены.

### 2. Запуск в `init`

```kotlin
// ❌ ПЛОХО: побочный эффект при создании, работа без ясной границы
class UserSession(private val scope: CoroutineScope, private val api: Api) {
    init { scope.launch { _user.value = api.load() } }
}
```

Конструктор сразу возвращает объект. Вызывающий код не может дождаться загрузки, увидеть ошибку или отменить работу. Объект уже существует, но его состояние не определено.

```kotlin
// ✅ ХОРОШО: явный запуск, вызывающий код владеет приостановкой
class UserSession(private val api: Api) {
    private var _user: User? = null
    val user: User get() = checkNotNull(_user) { "Call init() first" }

    suspend fun init() { _user = api.load() }
}
```

### 3. Запуск без ожидания из класса вне UI

Обычная публичная функция **не UI-класса** — репозитория, менеджера, сценария, источника данных — запускает работу в собственном scope. Вызывающий не получает результат и ошибку, не может отменить работу и даже не знает, началась ли она.

```kotlin
// ❌ ПЛОХО — репозиторий хранит scope и открывает fire-and-forget API
class AnalyticsClient(private val scope: CoroutineScope, private val api: Api) {
    fun track(event: Event) {
        scope.launch { api.send(event) }
    }
    fun signOut() {
        scope.launch { api.signOut() }
    }
}
```

```kotlin
// ✅ ХОРОШО
class AnalyticsClient(private val api: Api) {
    suspend fun track(event: Event) = api.send(event)
    suspend fun signOut() = api.signOut()
}
```

#### Исключение: граница UI и state holder

API UI не бывают suspend: `onClick` в Composable, `onKeyEvent` во Fragment, `onNewIntent` в Activity. State holder — ViewModel, Decompose Component или модель функции, принимающая события UI и хранящая его состояние, — как раз переводит разовые события в асинхронную работу, привязанную к жизненному циклу UI.

```kotlin
// ✅ ХОРОШО — state holder принимает обычное событие UI в свой scope
class FavouritesViewModel(private val repo: FavouritesRepository) : ViewModel() {
    fun onToggleFavourite(item: Item) {
        viewModelScope.launch { repo.toggleFavourite(item) }
    }
}

// в Compose:
ListItem(onClick = { viewModel.onToggleFavourite(item) })
```

Это **не** ошибочный запуск без ожидания, если выполнены все три условия:

1. **State holder UI:** ViewModel, Component или аналог. Не репозиторий, менеджер, сценарий или источник данных.
2. **Scope связан с жизненным циклом:** `viewModelScope`, scope Component, отменяемый при уничтожении, или `rememberCoroutineScope()`. Не `AppScope`, не внедрённый долгоживущий и не самодельный `CoroutineScope(...)`.
3. **Вызывающий действительно посылает событие UI:** callback Composable, обработчик клавиши или событие жизненного цикла. Не другой класс бизнес-логики, обращающийся через state holder.

Нижние слои — репозиторий, сценарий, источник данных — всё равно открывают `suspend` API. State holder — единственное место, где обычный вызов превращается в suspend-работу.

Одного сходства со state holder мало. Спроси: «UI привязан к этому объекту напрямую?» Если нет, исключение не действует.

### 4. Scope, созданный внутри класса

Тот же ошибочный шаблон без внедрения:

```kotlin
// ❌ ПЛОХО — scope создан в классе
class FooManager {
    private val scope = MainScope()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}
```

Такой scope не имеет владельца жизненного цикла и живёт бесконечно. Замени его на `suspend` API.

Создание внутри функции не лучше: `fun foo() { CoroutineScope(...).launch { … } }` даёт новый scope без доступной отмены при каждом вызове. `by lazy` лишь прячет ту же проблему.

### 5. DI-синглтоны и инициализаторы, которые запускают работу

Трудный для поиска случай: класс из DI (`@SingleIn(AppScope)`, `@Singleton`, `Initializer.initialize()`) запускает coroutine из конструктора, `init` или `initialize()`. У такой работы:

- **непредсказуемое время начала:** когда DI впервые создаст объект;
- **нет видимого жизненного цикла:** остальной код не знает, работает ли она и не упала ли;
- **нет пути остановки и перезапуска:** плохое состояние сверху оставит цикл висеть;
- **нет явного места запуска:** поиск по коду не отвечает, кто и когда запустил работу.

Правило §1 о scope вызывающего кода нарушено косвенно: scope может быть внедрён, но `launch` спрятан в создании объекта.

```kotlin
// ❌ ПЛОХО — singleton запускает работу при создании
@SingleIn(AppScope::class)
@Inject
class TokenRefresher(
    @ForScope(AppScope::class) private val scope: CoroutineScope,
    private val auth: AuthService,
) {
    init {
        scope.launch {
            while (isActive) {
                delay(5.minutes)
                auth.refreshIfNeeded()
            }
        }
    }
}

// ❌ ТОЖЕ ПЛОХО — initialize() запускает coroutine вместо регистрации
class TokenInvalidatorInitializer @Inject constructor(
    @ForScope(AppScope::class) private val scope: CoroutineScope,
    private val store: AuthStore,
    private val invalidator: TokenInvalidator,
) : Initializer {
    override fun initialize() {
        scope.launch { store.tokenChanges.collect { invalidator.invalidate() } }
    }
}
```

Оба выглядят как singleton уровня приложения. Но исключение в разделе «Когда не применять» **не даёт права** запускать работу из `init` или `initialize()`. Оно разрешает singleton владеть scope, когда API остаётся suspend.

#### Сначала спроси: нужен ли вообще класс с фоновым циклом?

Такие классы часто появляются потому, что никто не перенёс реакцию к месту изменения состояния. Проверь три варианта по порядку:

**Вариант 1 — действие в месте изменения.** Класс бесконечно следит за состоянием, но кто-то меняет его — выход из аккаунта, смена профиля, обновление флага. Место изменения уже находится в coroutine и может сразу выполнить нужное действие.

```kotlin
// ✅ ХОРОШО — без фонового цикла и scope: работа выполняется при изменении
class Authenticator(
    private val authStore: AuthStore,
    private val tokenInvalidator: TokenInvalidator,
) {
    suspend fun signOut() {
        authStore.clearTokens()
        tokenInvalidator.invalidate()
    }
}
```

Отдельный класс фонового цикла здесь больше не нужен. Этот вариант подходит, если владелец изменения имеет ясный жизненный цикл — сценарий, Authenticator или обработчик сервиса — и может отреагировать сразу.

**Вариант 2 — задача по расписанию.** Для действительно периодической или отложенной работы используй WorkManager / BGTaskScheduler. Постановку в очередь вызывай один раз из организатора запуска, лучше через suspend API.

**Вариант 3 — явное именованное место запуска.** Иногда потребитель — синхронный API без видимого жизненного цикла: `Sampler.shouldSample(...)` из OpenTelemetry, AIDL или мост broadcast receiver. Наблюдение должно жить в coroutine, но его `launch` обязан стоять в явном именованном месте, а не в `init` самого класса.

```kotlin
// ✅ ХОРОШО — работа названа, запуском владеет явное место вызова
@SingleIn(AppScope::class)
class OtelConfigurableSampler(...) : Sampler {
    @Volatile private var delegate: Sampler = ...

    suspend fun observeRate(featureFlags: FeatureFlags) {
        featureFlags.observe(OTEL_SAMPLING_RATE).collect { rate ->
            delegate = Sampler.traceIdRatioBased(rate.coerceIn(0.0, 1.0))
        }
    }

    override fun shouldSample(...) = delegate.shouldSample(...)
}

// явно подключено в модуле запуска OTel SDK:
applicationScope.launch { otelSampler.observeRate(featureFlags) }
```

Этот вариант нужен, если синхронный API вызывает ваш объект и жизненный цикл вызова не наблюдается. Перенести реакцию к месту изменения нельзя, но место запуска всё равно должно быть видно.

#### Как выбрать вариант

«Виден ли мне жизненный цикл потребителя?»

- **Да, и он уже в coroutine** → вариант 1: перенеси реакцию туда, убери фоновый цикл.
- **Работа периодическая или отложенная** → вариант 2: единоразовая постановка в очередь.
- **Нет, это синхронный API без видимого жизненного цикла** → вариант 3: явное место `launch`, не `init`.

Попытка ввести четвёртый ответ вроде интерфейса `Bootable`, который всё запускает автоматически, лишь прячет старую ошибку за новым слоем. Цель — сделать запуск видимым.

#### Инициализатор, который только регистрирует, допустим

`Initializer.initialize()` подходит для регистрации слушателя или обработчика. Ошибка — именно запуск coroutine.

```kotlin
// ✅ ХОРОШО — регистрирует участника, не запускает coroutine
class FavouritesContributorInitializer @Inject constructor(
    private val registry: ContributorRegistry,
    private val favouritesContributor: FavouritesContributor,
) : Initializer {
    override fun initialize() {
        registry.register(favouritesContributor)
    }
}
```

**`Initializer.initialize()` не должен вызывать `launch`.** Если вызывает, ищи вариант 1, 2 или 3.

#### Вопросы при проверке

- Где задан момент запуска? «Когда DI создаст меня» — плохо.
- Кто видит, идёт ли работа? «Никто» — плохо.
- Кто может остановить или повторить её? «Никто» — плохо.
- Можно ли найти место запуска поиском? Нет — плохо.

Если ответы указывают на потребителя, организатор или явное место вызова, граница видна.

### 6. Проглатывание `CancellationException`

`catch` вокруг suspend-вызова, который ловит `CancellationException` напрямую или через `Exception` / `Throwable` и не выбрасывает снова, обычно превращает отмену в тихий успех. Родитель думает, что дочерняя работа закончилась, а она или её побочные эффекты продолжаются. Контракт отмены сломан.

Это обратная сторона ошибки §1: там работа скрыта от жизненного цикла вызывающего, здесь отмена скрыта от самой работы.

```kotlin
// ❌ ПЛОХО — ловит CancellationException и не выбрасывает снова
suspend fun fetch() {
    try {
        api.load()
    } catch (e: Exception) {
        logger.warn("load failed", e)
    }
}

// ❌ ТОЖЕ ПЛОХО — runCatching имеет ту же проблему
suspend fun fetch() {
    runCatching { api.load() }
        .onFailure { logger.warn("load failed", it) }
}
```

Допустимые формы:

```kotlin
// ✅ Сначала отдельный catch
try { api.load() }
catch (e: CancellationException) { throw e }
catch (e: Exception) { logger.warn("load failed", e) }

// ✅ Проверка внутри широкого catch
try { api.load() }
catch (e: Exception) {
    if (e is CancellationException) throw e
    logger.warn("load failed", e)
}

// ✅ ensureActive(): когда обычные ошибки обрабатываются, а отмену надо пробросить
try { api.load() }
catch (e: Exception) {
    currentCoroutineContext().ensureActive()
    logger.warn("load failed", e)
}

// ✅ runCatching с явной проверкой
runCatching { api.load() }
    .onFailure {
        if (it is CancellationException) throw it
        logger.warn("load failed", it)
    }

// ✅ getOrThrow пропускает отмену наружу
runCatching { api.load() }.getOrThrow()
```

Сигнал — suspend-вызов **внутри `try`**, а не лишь объявление внешней функции как `suspend`. Правило действует и в `suspend fun`, и в лямбде `launch { … }`, и в `Flow.collect { … }`.

Частое исключение — свой локальный таймаут: можно поймать `TimeoutCancellationException` от собственного `withTimeout` и превратить его в доменный результат. Держи такой `catch` узким и рядом с таймаутом. Это не разрешение проглатывать любую отмену.

Ловить подтип, не связанный с отменой (`IOException` или свой тип ошибки), можно: он не наследуется от `CancellationException`.

### 7. `runBlocking`

`runBlocking` останавливает текущий поток до завершения блока. В коде приложения, способном работать через suspend или scope жизненного цикла, это неверно: асинхронный путь блокирует поток, структура coroutine ломается, отмена выше не действует. Вызываемый код принимает решение о структуре вместо вызывающего.

```kotlin
// ❌ ПЛОХО — переход к suspend через блокировку потока
fun saveUser(user: User) {
    runBlocking { repository.save(user) }
}
```

Три решения по месту:

**Код приложения, способный быть suspend:** сделай функцию `suspend`.

```kotlin
// ✅ ХОРОШО
suspend fun saveUser(user: User) = repository.save(user)
```

Если ближайший вызывающий код не может быть suspend — callback UI или обработчик `BroadcastReceiver` — используй существующий scope жизненного цикла на границе; см. исключение §3. Исправляй границу, а не `saveUser`.

Законные границы блокировки: `main` консольной программы, Java API с обязательным синхронным ответом, callback платформы без suspend-аналога и временный слой миграции. Держи `runBlocking` на внешней границе и делай его тело коротким.

**Тесты:** используй `runTest`.

```kotlin
// ❌ ПЛОХО — реальное время, медленные тесты
@Test fun loadsUser() = runBlocking {
    assertThat(repository.load().name).isEqualTo("Alice")
}

// ✅ ХОРОШО
@Test fun loadsUser() = runTest {
    assertThat(repository.load().name).isEqualTo("Alice")
}
```

`runTest` даёт виртуальное время (можно быстро пропускать `delay()`), связь с `TestDispatcher` и правильную очистку coroutine. `runBlocking` делает тесты медленнее и менее устойчивыми.

**Исключение для `ContentProvider`:** методы Android `ContentProvider` (`query`, `insert`, `update`, `delete`, `onCreate`, `call`) обязаны быть синхронными для внешнего кода. В методах экземпляра подкласса `ContentProvider` `runBlocking` может быть неизбежным мостом. Сведи тело к немедленному вызову suspend-кода:

```kotlin
// ✅ Допустимо только в методах ContentProvider
class MyProvider : ContentProvider() {
    override fun query(...): Cursor? = runBlocking { dao.query(...) }
}
```

Исключение относится только к подклассам `android.content.ContentProvider`, прямым или косвенным. Похожий класс не подходит. `runBlocking` в companion object самого ContentProvider тоже нарушает правило: этот helper не является синхронным API framework.

## Краткая памятка

| Симптом | Ошибка | Исправление |
|---|---|---|
| В классе `private val scope: CoroutineScope` | Scope хранится у вызываемого | Убери, сделай публичные API `suspend` |
| `init { scope.launch { ... } }` | Запуск при создании | Перенеси в `suspend fun init()` / `login()` |
| `fun foo() { scope.launch { ... } }` в репозитории или сценарии | Запуск без ожидания вне UI | `suspend fun foo()`, scope выбирает UI state holder |
| `fun onClick() { viewModelScope.launch { ... } }` в state holder от UI | Верная граница | Оставь, см. §3 |
| `private val scope = MainScope()` | Scope создан внутри класса | Убери, сделай API `suspend` |
| Singleton DI запускает `launch` в `init` | Скрытый запуск | Открой `suspend fun run()`, запускай из организатора старта |
| `Initializer.initialize()` запускает coroutine | Инициализатор запускает, а не регистрирует | Тот же явный запуск организатором |
| `catch (Exception/Throwable/CancellationException)` вокруг suspend без повторного выброса | Отмена проглочена | Предпочитай `catch (e: CancellationException) { throw e }` |
| `runCatching { suspendCall() }.onFailure { … }` без проверки отмены | Та же ошибка | Добавь проверку или `.getOrThrow()` |
| `runBlocking` в suspend-совместимом коде | Блокировка потока | Сделай вызов `suspend` или используй scope на границе |
| `runBlocking` в тесте | Работа в реальном времени | `runTest` |
| `runBlocking` в методе `ContentProvider` | Допустимый мост | Сведи тело к минимуму |

## Как переработать код

1. **Начни с нижнего слоя:** репозитория или источника данных, самого дальнего от UI.
2. **Преобразуй публичные функции в `suspend`** по одной. Компилятор покажет всех вызывающих.
3. **Для каждого вызова явно выбери scope:** `viewModelScope`, `lifecycleScope`, `coroutineScope { }` или отдельный job. Именно этого выбора раньше не было.
4. **Убери параметр `CoroutineScope` из конструктора**, когда он больше не нужен, и соответствующую привязку DI.

Не пытайся исправить все классы одним MR. Убирай ошибочный шаблон постепенно.

## Когда не применять

- **UI state holder, принимающий события UI.** `fun onClick(...) { viewModelScope.launch { ... } }` в ViewModel/Component — правильная граница; см. §3.
- **Владелец жизненного цикла с явными правилами отмены и ошибок.** Actor, service, инфраструктура приложения или singleton уровня приложения могут владеть scope, если имеют ясные `close`/`cancel`/restart либо прямо следуют жизни приложения. Внедряй `Application.applicationScope`, а не создавай случайный scope. **Это не разрешает запуск из `init` / `initialize()`** — см. §5.
- **Уже suspend API** не нуждается в такой правке.
- **Тесты** могут намеренно применять `TestScope` как внешний scope с виртуальным временем.

## Тревожные мысли при проверке

| Мысль | Что на деле |
|---|---|
| «Добавлю `CoroutineExceptionHandler` к scope» | Дело не только в ошибках: самого scope здесь не должно быть. |
| «Нужно запустить в `init`, чтобы данные успели загрузиться» | Проблема в том, что потребитель видит ещё не готовое состояние. Задай этапы явно. |
| «Вызывающий не хочет возиться с `suspend`» | Пусть он сам решит запустить без ожидания в своём scope. |
| «Это маленький вызов без ожидания» | После отмены scope даже маленькая работа может тихо не начаться. |
| «Мы поймали и записали ошибку» | А `CancellationException` выброшен снова? Иначе отмена проглочена. |
| «Всего один `runBlocking` в неважном месте» | Каждый `runBlocking` утверждает, что у вызывающего нет асинхронного пути. |
| «Тест с `runBlocking` проще» | Он идёт в реальном времени и теряет возможности `TestDispatcher`. Используй `runTest`. |

## Связанные навыки

- [`kotlin-flow-state-event-modeling`](../kotlin-flow-state-event-modeling/SKILL.md) — `StateFlow`, `SharedFlow`, `Channel`, `stateIn`, разовые события и их модели.
