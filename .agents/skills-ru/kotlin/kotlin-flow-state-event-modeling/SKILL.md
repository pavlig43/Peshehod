---
name: kotlin-flow-state-event-modeling
description: Применяйте при создании или проверке API состояния и событий Kotlin Flow: StateFlow, MutableStateFlow.update, SharedFlow, Channel, stateIn, SharingStarted, .value, receiveAsFlow, одноразовых событий и фиктивных начальных значений.
---

# Kotlin Flow: состояние и события

## Главное правило

**Выбирайте средство по нужным повторам, числу получателей и чтению значения без ожидания.** `StateFlow`, `SharedFlow`, поток из `Channel` и холодный `Flow` различаются буфером, тем, кто видит каждое событие, и наличием `.value`. Неверный выбор теряет события, оставляет лишние корутины общего потока или заставляет подмешивать в состояние фиктивные значения предметной области.

## Когда нужен этот навык

Вы пишете или проверяете код Kotlin, где есть:

- `MutableStateFlow<T>(SomeSentinel)`, например `NoUser`, `Empty` или `Loading`, поскольку настоящее значение приходит позже;
- `.stateIn(...)` внутри функции, а не в свойстве;
- `SharingStarted.WhileSubscribed(...)` у потока, чей `.value` читают сразу и ждут свежие данные;
- `MutableSharedFlow` для навигации, сообщений на экране или других одноразовых событий, потеря которых была бы ошибкой;
- `.map { }` над `StateFlow`, когда получателям по-прежнему нужен `.value`;
- `MutableStateFlow.value = _state.value.copy(...)` либо дорогая сборка объекта внутри `update { ... }`.

## `SharedFlow` для событий одному получателю

По умолчанию у `SharedFlow` нет буфера повтора. Если в момент отправки никто не собирает поток, событие исчезает. Для **одного получателя в интерфейсе**, который должен обработать каждый переход или сообщение ровно один раз, часто лучше подходит `Channel` с буфером, открытый как `Flow`:

```kotlin
// ❌ НЕВЕРНО
private val _navEvents = MutableSharedFlow<NavigationEvent>()
val navEvents: SharedFlow<NavigationEvent> = _navEvents.asSharedFlow()

// ✅ ВЕРНО
private val _navEvents = Channel<NavigationEvent>(Channel.BUFFERED)
val navEvents: Flow<NavigationEvent> = _navEvents.receiveAsFlow()
```

`Channel.receiveAsFlow()` **раздаёт события, а не вещает всем**: при нескольких сборщиках каждое событие достанется **одному**. Размер `Channel.BUFFERED` ограничен: отправка может приостановиться, а `trySend` — не сработать. Если каждое событие должны видеть все наблюдатели, берите явное состояние, долговечное хранение или осознанно настроенный `SharedFlow`.

## Фиктивное начальное значение в `StateFlow`

`StateFlow` требует начальное значение. Если настоящее значение загружается позже, иногда вводят поддельное значение предметной области — `NoUser`, `EmptyUser` или пустой ID. Тогда каждый получатель вынужден обращаться с ним как с настоящими данными.

```kotlin
// ❌ НЕВЕРНО: фиктивное значение попало в тип.
class UserSession(private val db: Db) {
    private val _user = MutableStateFlow<User>(NoUser)
    val user: StateFlow<User> = _user.asStateFlow()
    init { scope.launch { _user.value = db.load() } }
}
```

Одно решение — **разделить работу на этапы**: не открывать `StateFlow`, пока нет настоящего значения.

```kotlin
// ✅ ВЕРНО: запуск приостанавливается; наблюдатели видят лишь настоящих пользователей.
class UserSession(private val db: Db) {
    private var _user: MutableStateFlow<User>? = null
    val user: StateFlow<User>
        get() = checkNotNull(_user) { "Сначала вызовите login()" }

    suspend fun login() {
        _user = MutableStateFlow(db.load())
    }
}
```

Если отсутствие, загрузка или ошибка — настоящие состояния, задайте их явно через `User?`, `sealed interface UserUiState`, `Result` и т. п. Ошибка здесь — не любое начальное значение, а поддельный объект предметной области, выдаваемый за настоящий.

## Меняйте `MutableStateFlow` через `update { ... }`

Предпочитайте `MutableStateFlow.update { current -> ... }` чтению `.value` с последующей записью. `update` применяет изменение атомарно к самому свежему состоянию, не теряя параллельные правки нескольких корутин.

```kotlin
// НЕВЕРНО: чтение, правка и запись могут потерять параллельное изменение.
_state.value = _state.value.copy(
    selectedId = id,
    details = details,
)

// ВЕРНО: изменение опирается на самое свежее состояние.
_state.update { current ->
    current.copy(
        selectedId = id,
        details = details,
    )
}
```

Создавайте объекты до блока `update`, если для этого не требуется текущее состояние. Лямбда `update` может быть вызвана снова, поэтому дорогая работа или побочные действия внутри неё могут повториться:

```kotlin
// ВЕРНО: details не зависит от текущего состояния, создаём один раз.
val details = Details.from(response)
_state.update { current ->
    current.copy(details = details)
}

// ВЕРНО: производное значение зависит от текущего состояния, считаем внутри.
_state.update { current ->
    val nextItems = current.items.replaceById(updatedItem)
    current.copy(items = nextItems)
}
```

Блок должен быть чистым и быстрым преобразованием состояния: без вызовов сети, записи в базу, побочных записей в журнал, создания случайных ID и чтения времени. Если значения нужны, получите их до блока.

## `stateIn()` внутри функции

```kotlin
// ❌ НЕВЕРНО: каждый вызов запускает новую корутину общего потока.
fun getPreferences(): StateFlow<Prefs> {
    return repo.prefsFlow.stateIn(scope, SharingStarted.Eagerly, Prefs.Default)
}
```

Каждый вызов `getPreferences()` запускает в `scope` новую корутину, которая не завершается. При частом чтении работа быстро замедляется.

```kotlin
// ✅ ВЕРНО: один общий экземпляр, созданный один раз.
val preferences: StateFlow<Prefs> =
    repo.prefsFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Prefs.Default)
```

## `WhileSubscribed` и мгновенное чтение `.value`

`SharingStarted.WhileSubscribed(timeout)` отключает источник без активных сборщиков. Пока он отключён, `.value` отдаёт последнее сохранённое значение: оно может устареть или всё ещё быть начальным.

**Правило:** если `.value` должно быть свежим или готовым даже без сборщика, берите `SharingStarted.Eagerly` либо явную инициализацию. `WhileSubscribed` подходит, когда старое значение допустимо, а получатели в основном собирают поток с ожиданием.

## `.map` над `StateFlow` теряет `.value`

```kotlin
// ❌ НЕВЕРНО: `name.value` не соберётся; теперь это простой Flow.
val name: Flow<String> = userState.map { it.name }
```

Если нужен `.value` без ожидания, завершите цепочку через `.stateIn(...)`:

```kotlin
// ✅ ВЕРНО
val name: StateFlow<String> = userState
    .map { it.name }
    .stateIn(viewModelScope, SharingStarted.Eagerly, userState.value.name)
```

Сторонние утилиты «производного потока состояния» могут запускать преобразование при каждом чтении `.value`; это годится лишь для быстрых преобразований без побочных действий. По умолчанию берите `.stateIn(...)`.

## Какой тип Flow нужен?

| Потребность | Средство |
|---|---|
| Состояние со значением всегда, читаемое и сборщиками потока, **и** сразу другим кодом | `StateFlow`; часто с `SharingStarted.Eagerly`, когда важен `.value` |
| Горячий поток для нескольких подписчиков, без нужды в `.value` | `SharedFlow` |
| Отдельные события для **одного** получателя, каждое надо передать один раз | Рассмотрите `Channel(BUFFERED).receiveAsFlow()` |
| Холодный поток, отдельный получатель на каждую сборку | Простой `Flow` |

Прежде чем взять `SharedFlow`, спросите: будет ли потеря события ошибкой и сколько получателей должны его увидеть? Для одного получателя с однократной обработкой может подойти `Channel`. Если событие должны видеть все, задайте долговечное состояние или осознанно настройте вещание.

## Краткая памятка

| Признак | Проблема | Что сделать |
|---|---|---|
| `MutableStateFlow<X>(FakeDomainValue)` | Неверное фиктивное значение | Явно задать отсутствие либо поэтапный запуск |
| `MutableSharedFlow<Event>` для навигации или сообщений одному получателю | Событие может теряться | Рассмотреть `Channel(BUFFERED).receiveAsFlow()` |
| Функция создаёт `flow.stateIn(...)` при каждом вызове | Отдельная корутина общего потока на каждый вызов | Создать `val` или общий экземпляр |
| `WhileSubscribed`, а `.value` должен быть свежим или готовым | Старые либо начальные данные | `SharingStarted.Eagerly` или явная инициализация |
| Результат `stateFlow.map { ... }` используют как состояние | Потерян `.value` | Завершить `.stateIn(...)` |
| `_state.value = _state.value.copy(...)` | Чтение, правка и запись не атомарны | `_state.update { it.copy(...) }` |
| Дорогой объект создаётся внутри `update`, хотя текущее состояние ему не нужно | Работа может повториться при новом вызове лямбды | Создать до `update`; внутри оставить лишь зависимые от состояния преобразования |

## Неверные доводы

| Довод | Что на деле |
|---|---|
| «Нужен `SharedFlow`, ведь подписчиков несколько» | Число получателей меняет смысл. `Channel.receiveAsFlow()` не вещает всем; выбирайте модель события осознанно. |
| «`WhileSubscribed` сэкономит ресурсы» | Только если старый или начальный `.value` допустим; сперва проверьте. |
| «Пока идёт загрузка, поставлю фиктивное значение» | Получатели примут его за данные; лучше явно задать состояние интерфейса или этапы запуска. |
| «Новый объект удобно создать внутри `update`» | Лямбда может запускаться снова. Создайте его снаружи, если ему не нужно текущее состояние. |

## См. также

- [`kotlin-control-flow`](../kotlin-control-flow/SKILL.md) — `when`, охранники, полнота, умные приведения и ранний выход при работе с состоянием и событиями.
- [`kotlin-coroutines-structured-concurrency`](../kotlin-coroutines-structured-concurrency/SKILL.md) — владелец области, запуск из `init`, фоновые действия, отмена и `runBlocking`.
- [`compose-side-effects`](../../compose-multiplatform/compose-side-effects/SKILL.md) — сбор потоков событий и побочные действия в Compose.
- [`compose-state-holder-ui-split`](../../compose-multiplatform/compose-state-holder-ui-split/SKILL.md) — где держатель состояния открывает потоки интерфейсу.
