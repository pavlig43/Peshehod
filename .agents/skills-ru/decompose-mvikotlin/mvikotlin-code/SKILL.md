---
name: mvikotlin-code
description: Пишите, проверяйте и меняйте код MVIKotlin в этом репозитории. Применяйте при упоминании MVIKotlin, StoreFactory, Store<Intent, State, Label>, CoroutineExecutor, Reducer, Bootstrapper, Label, MviView, LoggingStoreFactory, TimeTravelStoreFactory либо при создании хранилища функции, связанного с компонентом Decompose.
---

# Код MVIKotlin

Применяйте навык при работе с MVIKotlin в этом репозитории.

Здесь используются:

- MVIKotlin `4.3.0`;
- `mvikotlin-extensions-coroutines`;
- компоненты Decompose как граница интерфейса;
- Metro DI и общий `StoreFactory`, передаваемый как зависимость.

По умолчанию следуйте схеме репозитория, а не самым общим возможностям MVIKotlin.

## Что прочитать сначала

Пути ниже даны от корня репозитория. Открывайте лишь то, что относится к задаче.

- Справка хранилища: `libs/MVIKotlin/docs/store.md`.
- Справка представления: `libs/MVIKotlin/docs/view.md`.
- Справка связи и жизненного цикла: `libs/MVIKotlin/docs/binding_and_lifecycle.md`.
- Справка сохранения состояния: `libs/MVIKotlin/docs/state_preservation.md`.
- Справка журнала: `libs/MVIKotlin/docs/logging.md`.
- Справка возврата во времени: `libs/MVIKotlin/docs/time_travel.md`.
- Простое хранилище с запуском: `feature/home/src/commonMain/kotlin/com/yet/tetris/feature/home/store/HomeStore.kt`.
- Его реализация: `feature/home/src/commonMain/kotlin/com/yet/tetris/feature/home/store/HomeStoreFactory.kt`.
- Мгновенная правка с записью позже: `feature/settings/src/commonMain/kotlin/com/yet/tetris/feature/settings/store/SettingsStoreFactory.kt`.
- Сложный долгоживущий исполнитель: `feature/game/src/commonMain/kotlin/com/yet/tetris/feature/game/store/GameStoreFactory.kt`.
- Преобразование состояния хранилища в компонент: `feature/game/src/commonMain/kotlin/com/yet/tetris/feature/game/integration/Mappers.kt`.
- Компонент с сохраняемым хранилищем: `feature/game/src/commonMain/kotlin/com/yet/tetris/feature/game/DefaultGameComponent.kt`.
- Общий производственный `StoreFactory`: `core/common/src/commonMain/kotlin/com/app/common/di/CommonBindings.kt`.
- Преобразование хранилища в `Value` Decompose: `core/common/src/commonMain/kotlin/com/app/common/decompose/asValue.kt`.

## Правила репозитория

1. Передавайте `StoreFactory` каждой фабрике хранилища как зависимость. Не создавайте `DefaultStoreFactory()` в производственном коде отдельной функции приложения.
2. Пишите `internal interface XxxStore : Store<XxxStore.Intent, XxxStore.State, XxxStore.Label>`.
3. Бизнес-логику функции держите в `XxxStoreFactory`, а не в Compose или компоненте Decompose.
4. По умолчанию берите `CoroutineExecutor`. Для хранилищ функций здесь не применяют Reaktive.
5. `Reducer` должен быть чистым и синхронным: без ввода-вывода, корутин, навигации и побочной записи в журнал.
6. `Label` берите лишь для однократных действий: переходов, диалогов, ошибок и внешних команд.
7. Долгую и начальную работу помещайте в `Action` с `Bootstrapper`, а не в `init` компонента.
8. В компонентах Decompose сохраняйте хранилище через `instanceKeeper.getStore { factory.create() }`.
9. Открывайте состояние интерфейса как `Value<Model>` через `store.asValue().map(stateToModel)`.
10. Преобразование `State -> Model` держите в `integration/Mappers.kt`.
11. Зависимости функции приложения связывайте через Metro в её пакете `di/`.
12. Не вводите `MviView` или `Binder` по умолчанию: они нужны лишь для особой задачи с платформенным представлением вне Compose и Decompose.

## Стандартное размещение файлов

Для новой функции предпочитайте:

```text
feature/foo/src/commonMain/kotlin/com/yet/tetris/feature/foo/
  FooComponent.kt
  DefaultFooComponent.kt
  PreviewFooComponent.kt
  integration/Mappers.kt
  store/FooStore.kt
  store/FooStoreFactory.kt
  di/FooBindings.kt
```

## Схема хранилища

По умолчанию создавайте отдельный интерфейс хранилища и его фабрику.

```kotlin
internal interface FooStore : Store<FooStore.Intent, FooStore.State, FooStore.Label> {
    data class State(
        val isLoading: Boolean = false,
        val items: List<Item> = emptyList(),
    )

    sealed class Intent {
        data object Refresh : Intent()
        data class Delete(val id: String) : Intent()
    }

    sealed class Action {
        data object LoadStarted : Action()
    }

    sealed class Msg {
        data class LoadingChanged(val isLoading: Boolean) : Msg()
        data class Loaded(val items: List<Item>) : Msg()
        data class Deleted(val id: String) : Msg()
    }

    sealed class Label {
        data class ShowError(val message: String) : Label()
        data object NavigateBack : Label()
    }
}
```

```kotlin
internal class FooStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: FooRepository,
) {
    fun create(): FooStore {
        return object : FooStore,
            Store<FooStore.Intent, FooStore.State, FooStore.Label> by storeFactory.create(
                name = "FooStore",
                initialState = FooStore.State(),
                bootstrapper = SimpleBootstrapper(FooStore.Action.LoadStarted),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}
    }

    private object ReducerImpl : Reducer<FooStore.State, FooStore.Msg> {
        override fun FooStore.State.reduce(msg: FooStore.Msg): FooStore.State {
            return when (msg) {
                is FooStore.Msg.LoadingChanged -> copy(isLoading = msg.isLoading)
                is FooStore.Msg.Loaded -> copy(isLoading = false, items = msg.items)
                is FooStore.Msg.Deleted -> copy(items = items.filterNot { it.id == msg.id })
            }
        }
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<FooStore.Intent, FooStore.Action, FooStore.State, FooStore.Msg, FooStore.Label>() {

        override fun executeAction(action: FooStore.Action) {
            when (action) {
                FooStore.Action.LoadStarted -> load()
            }
        }

        override fun executeIntent(intent: FooStore.Intent) {
            when (intent) {
                FooStore.Intent.Refresh -> load()
                is FooStore.Intent.Delete -> delete(intent.id)
            }
        }

        private fun load() {
            scope.launch {
                try {
                    dispatch(FooStore.Msg.LoadingChanged(true))
                    dispatch(FooStore.Msg.Loaded(repository.getAll()))
                } catch (e: Exception) {
                    dispatch(FooStore.Msg.LoadingChanged(false))
                    publish(FooStore.Label.ShowError(e.message ?: "Не удалось загрузить"))
                }
            }
        }

        private fun delete(id: String) {
            scope.launch {
                try {
                    repository.delete(id)
                    dispatch(FooStore.Msg.Deleted(id))
                } catch (e: Exception) {
                    publish(FooStore.Label.ShowError(e.message ?: "Не удалось удалить"))
                }
            }
        }
    }
}
```

## `Intent`, `Action`, `Msg`, `Label`

У этих видов постоянные роли:

- `Intent` — ввод из интерфейса или API компонента.
- `Action` — запуск или внутренняя передача действия, особенно работа при старте.
- `Msg` — данные о смене состояния для редьюсера.
- `Label` — однократное действие, которое не должно жить в состоянии.

Предпочитайте `Intent` для нажатий, правок поля, жестов и повтора; `Action` — для `FeatureLoadStarted`, новой подписки и переданной работы; `Msg` — для каждой смены состояния; `Label` — для навигации, сообщений, диалогов, внешних вызовов и сигналов аналитики, если для них нет лучшего места.

Не меняйте состояние прямо из компонента, не кладите одноразовую навигацию в `State`, не вызывайте репозиторий или сценарий работы из редьюсера и не обходите `Msg` для произвольной правки состояния из исполнителя.

## Правила исполнителя

MVIKotlin гарантирует:

- `accept(Intent)`, `init()` и `dispose()` вызываются в главном потоке;
- состояния и метки `Label` выдаются в главном потоке;
- `Executor` и `Bootstrapper` хранят состояние и не должны быть одиночными объектами.

Применяйте эти правила:

- `executorFactory = ::ExecutorImpl` каждый раз создаёт новый экземпляр;
- `ReducerImpl` допустим как `object` лишь потому, что он чистый и не хранит состояние;
- для работы с ожиданием используйте `scope.launch {}`;
- на фоновые диспетчеры переходите лишь для настоящего ввода-вывода или тяжёлых вычислений;
- `dispatch`, `publish` и `forward` вызывайте из контекста исполнителя в главном потоке.

Если действию нужен согласованный снимок состояния, один раз сохраните `val state = state()` и передайте дальше. Если позже понадобится самое свежее значение, снова вызовите `state()`, а не берите устаревшую копию.

## Правила начального запуска

В этом репозитории:

- для обычной загрузки при создании берите `SimpleBootstrapper(Action.LoadStarted)`;
- свой `CoroutineBootstrapper` нужен, лишь если сама начальная работа асинхронна и заметно сложнее;
- никогда не делайте `Bootstrapper` объектом `object`.

`StoreFactory.create(...)` по умолчанию сам запускает хранилище. Не вызывайте `store.init()` вручную, если только вы намеренно не отключили `autoInit`.

## Связь с Decompose

Стандартная схема компонента:

```kotlin
internal class DefaultFooComponent(
    componentContext: ComponentContext,
    private val fooStoreFactory: FooStoreFactory,
    private val navigateBack: () -> Unit,
) : ComponentContext by componentContext, FooComponent {

    private val store = instanceKeeper.getStore { fooStoreFactory.create() }

    init {
        coroutineScope().launch {
            store.labels.collect { label ->
                when (label) {
                    FooStore.Label.NavigateBack -> navigateBack()
                    is FooStore.Label.ShowError -> Unit
                }
            }
        }
    }

    override val model: Value<FooComponent.Model> =
        store.asValue().map(stateToModel)
}
```

Правила:

- сохраняйте хранилище через `instanceKeeper.getStore`;
- собирайте метки в области корутин, связанной с жизненным циклом;
- передавайте события интерфейса через `store.accept(...)`;
- методы компонента держите короткими, а логику — в хранилище;
- не дублируйте бизнес-правила в `init` компонента.

Если компонент владеет дочерней навигацией, метки могут управлять переходами. Обрабатывайте переходы в компоненте, а не в хранилище.

## `View` и `Binder`

Официальный MVIKotlin поддерживает `MviView`, `BaseMviView` и `Binder`, но обычно они не нужны здесь: Compose и Decompose могут наблюдать состояние хранилища напрямую.

По умолчанию:

- для состояния Decompose берите `store.asValue()` с `map(stateToModel)`;
- сохраняйте отдельный тип `Model` интерфейса, когда уже есть контракт компонента;
- берите `Binder`, лишь если связываете платформенное представление вне Compose, где уже принята связь MVIKotlin между видом и событиями.

Не вводите `MviView` в функцию приложения с контрактом компонента Decompose без прямой нужды.

## Сохранение состояния и экземпляра

Выбирайте средство по нужному сроку жизни:

- `instanceKeeper.getStore { ... }` сохраняет всё хранилище при пересоздании области Decompose и смене настроек устройства;
- `StateKeeper` хранит записываемое состояние для восстановления после остановки процесса.

Сейчас в репозитории хранилища сохраняются через `instanceKeeper.getStore`, но их состояние после остановки процесса через `StateKeeper` ещё не восстанавливается.

Если задаче нужно такое восстановление:

1. Передайте `StateKeeper` из контекста компонента при создании хранилища.
2. Возьмите `initialState` из `stateKeeper.consume(...)`.
3. Зарегистрируйте очищенное состояние через `stateKeeper.register(...)`.
4. Перед записью сбросьте временные поля, например признак загрузки.

Не сохраняйте тяжёлые зависимости. Храните лишь данные, которые можно записать и прочитать.

## Журнал и возврат во времени

Эти средства MVIKotlin оборачивают общий `StoreFactory`. Меняйте привязку уровня приложения, а не каждую функцию:

```kotlin
@Provides
fun provideStoreFactory(): StoreFactory {
    return DefaultStoreFactory()
}
```

Если нужны отладочные обёртки, замените единственную общую привязку в `core/common/.../CommonBindings.kt`:

```kotlin
LoggingStoreFactory(DefaultStoreFactory())
LoggingStoreFactory(TimeTravelStoreFactory())
```

Правила:

- фабрики хранилищ функций по-прежнему принимают простой `StoreFactory`;
- не встраивайте журнал или возврат во времени в отдельный модуль функции;
- оба средства — для отладки, а не настройка выпуска;
- если включён возврат во времени, помните: настройка сервера зависит от платформы.

Не добавляйте `JvmSerializable` повсюду по умолчанию. Дополнительные пометки для записи нужны лишь конкретному случаю отладки или выгрузки.

## Правила внедрения зависимостей

Связывайте хранилища через Metro в модуле функции:

```kotlin
@Provides
internal fun provideFooStoreFactory(
    storeFactory: StoreFactory,
    repository: FooRepository,
): FooStoreFactory {
    return FooStoreFactory(
        storeFactory = storeFactory,
        repository = repository,
    )
}
```

Передавайте доменные репозитории и сценарии работы в фабрику хранилища, фабрики — в фабрики компонентов. Не ставьте аннотации внедрения зависимостей на саму реализацию хранилища.

## Тесты

Следуйте принятому здесь виду тестов хранилища:

- создавайте хранилище через `DefaultStoreFactory()`;
- по нужде отключайте проверку главного потока MVIKotlin в тесте;
- ставьте `Dispatchers.Main` в `StandardTestDispatcher`;
- берите `runTest` и `advanceUntilIdle()`;
- проверяйте `store.state` напрямую для результата редьюсера;
- собирайте `store.labels` для однократных действий.

Для новых тестов предпочитайте готовые помощники пакета тестов модуля. Тест хранилища сосредоточьте на переходах состояния и метках, а тест компонента — на навигации и связи с детьми.

## Как выбрать схему

Берите отдельный интерфейс `XxxStore`, если у функции уже есть контракт компонента, хранилище не совсем простое или нужны метки, начальный запуск и заметная асинхронная логика.

Рассмотрите DSL MVIKotlin, лишь если хранилище очень мало, пользователь прямо попросил DSL либо вы пишете отдельный пример, а не расширяете сложившееся устройство репозитория. И тогда без сильной причины не отступайте от схемы с отдельным интерфейсом и фабрикой.

## Ошибочные схемы

По умолчанию отклоняйте:

1. Создание `DefaultStoreFactory()` внутри кода функции приложения.
2. Вызовы репозитория из редьюсера.
3. Отдельную правку состояния компонента вне состояния хранилища.
4. Хранение навигации как постоянного состояния.
5. Исполнители `object` и свои загрузчики без нужды.
6. `MviView`, когда граница функции уже задаётся компонентом Decompose.
7. Перенос доменной логики в Compose или SwiftUI.
8. Журнал или возврат во времени лишь для одной функции при ином общем `StoreFactory` у остальных.

## Что должно быть в результате

При работе с MVIKotlin здесь:

1. Следуйте именам и размещению файлов уже имеющихся функций.
2. Бизнес-логику держите в фабрике хранилища и редьюсере.
3. Преобразование в интерфейс держите в `integration/Mappers.kt`.
4. Компоненты оставляйте короткими и учитывающими жизненный цикл.
5. Добавляйте узкие тесты состояния и меток либо обновляйте готовые.
