---
name: decompose-component
description: Применяйте при создании или правке компонента Decompose, при упоминании ComponentContext, retainedInstance, stateKeeper, instanceKeeper, InstanceKeeper, MutableValue, Value<, жизненного цикла, сохранения состояния и экземпляра, обработки «Назад» внутри компонента, а также при создании класса, который реализует интерфейс компонента Decompose. Подходит и для вопросов об устройстве компонента, сохранении состояния при смене настроек устройства, аналоге ViewModel в Decompose и обработке кнопки «Назад».
version: 1.0.0
---

Вы помогаете писать компонент Decompose. Точно следуйте этим схемам.

## Устройство компонента

Всегда берите `interface + DefaultXxxComponent`. Не наследуйте базовый класс библиотеки.

```kotlin
interface CounterComponent {
    val model: Value<Model>
    fun onIncrementClicked()

    data class Model(val count: Int = 0)
}

class DefaultCounterComponent(
    componentContext: ComponentContext,
    private val onFinished: () -> Unit,          // Обратные вызовы родителю передаются через конструктор.
) : CounterComponent, ComponentContext by componentContext {   // Делегирование, а не наследование.

    private val _model = MutableValue(CounterComponent.Model())
    override val model: Value<CounterComponent.Model> = _model

    override fun onIncrementClicked() {
        _model.update { it.copy(count = it.count + 1) }
    }
}
```

**Правила:**
- Всегда делегируйте через `ComponentContext by componentContext`, не наследуйте.
- Первый параметр конструктора — `componentContext: ComponentContext`.
- Обратные вызовы родителю для переходов и результатов передавайте лямбдами конструктора.
- Наружу открывайте неизменяемый `Value<T>`, а `MutableValue<T>` держите закрытым.
- Для смены состояния берите `MutableValue.update { }` и вызывайте его лишь в главном потоке.

## `Value` и другие средства состояния

`Value<T>` — наблюдаемое средство Decompose для разных платформ. Предпочитайте его в компонентах, общих для платформ.

```kotlin
// Подходит всем платформам; состояние наблюдают Compose, SwiftUI и React.
val state: Value<State> = _state

// Допустимо, если вы работаете лишь с корутинами на Android/JVM:
val state: StateFlow<State>
```

`Value` — не корутина. У него нет `collect`; в Compose берите `subscribe` или `subscribeAsState()`.

## Жизненный цикл

Компонент получает жизненный цикл сам. Подписывайтесь лишь в `init` или его обратных вызовах.

```kotlin
class DefaultSomeComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    init {
        lifecycle.doOnStart { /* Начать опрос. */ }
        lifecycle.doOnStop { /* Остановить опрос. */ }
        lifecycle.doOnDestroy { /* Освободить ресурсы. */ }
    }
}
```

Состояния: `INITIALIZED → CREATED → STARTED → RESUMED → STOPPED → DESTROYED`.
- Активный компонент находится в `RESUMED`.
- Компоненты стека возврата остаются живыми в `CREATED`, но остановлены.

## Сохранение состояния после остановки процесса и смены настроек

```kotlin
@Serializable
private data class State(val query: String = "", val selectedId: Long? = null)

class DefaultSearchComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private var state: State by saveable(serializer = State.serializer(), init = ::State)
}
```

Ручной вариант:

```kotlin
private var state = stateKeeper.consume("STATE", State.serializer()) ?: State()
init {
    stateKeeper.register("STATE", State.serializer()) { state }
}
```

**Правила:** нужна `@Serializable`, на Android состояние должно быть небольшим — менее 500 КБ; `consume()` вызывайте лишь один раз.

## Сохранение экземпляра при смене настроек, как `ViewModel`

```kotlin
class DefaultTimerComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val timer = retainedInstance { Timer() }

    private class Timer : InstanceKeeper.Instance {
        override fun onDestroy() {}
    }
}
```

**Правила:** класс не должен быть `inner`, не держите ссылки на `Activity`, `Context` и `View`, реализуйте `onDestroy()`.

## Сочетание сохранения состояния и экземпляра

```kotlin
class DefaultComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val logic by saveable(serializer = Logic.State.serializer(), state = { it.state }) { savedState ->
        retainedInstance { Logic(savedState) }
    }

    private class Logic(savedState: Logic.State?) : InstanceKeeper.Instance {
        var state = savedState ?: Logic.State()
            private set
        @Serializable data class State(val items: List<String> = emptyList())
        override fun onDestroy() {}
    }
}
```

## Кнопка «Назад»

```kotlin
class DefaultEditorComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val backCallback = BackCallback(isEnabled = false) {
        showDiscardDialog()
    }

    init { backHandler.register(backCallback) }

    fun onFormChanged() { backCallback.isEnabled = true }
}
```

Приоритет у обратного вызова, который зарегистрирован последним. Чтобы всегда перехватить первым, задайте `priority = Int.MAX_VALUE`.

## Подмены для просмотра и тестов

```kotlin
class PreviewCounterComponent : CounterComponent {
    override val model: Value<Model> = MutableValue(Model(count = 42))
    override fun onIncrementClicked() {}
}

internal val PreviewContext: ComponentContext = DefaultComponentContext(LifecycleRegistry())
```

## Важные ограничения

- **Корневой компонент создавайте в главном потоке интерфейса, никогда внутри `@Composable`.**
- **`defaultComponentContext()` вызывайте лишь один раз за жизнь `Activity` или `Fragment`.**
- **Для `retainedComponent()` действует то же правило:** один вызов в `onCreate`.
- **Переходы делайте только в главном потоке:** фоновый переход может вызвать сбой.
- **Подписку и правку `MutableValue` делайте только в главном потоке:** иначе возможны гонки.
- **Не делайте сохраняемый класс `inner`:** он удержит внешний компонент и вызовет утечку памяти.

## Краткая памятка

| Задача | API |
|---|---|
| Делегирование контекста | `class Foo(ctx: ComponentContext) : ComponentContext by ctx` |
| Наблюдаемое состояние | `MutableValue<T>` / `Value<T>` |
| Смена состояния | `_state.update { it.copy(...) }` |
| Пережить смену настроек | `retainedInstance { }` |
| Пережить остановку процесса | `saveable(serializer = ...)` или `stateKeeper` |
| События жизненного цикла | `lifecycle.doOnStart/Stop/Destroy { }` |
| Своя обработка «Назад» | `backHandler.register(BackCallback { })` |
| Автообработка «Назад» в навигации | `handleBackButton = true` в `childStack()` или `childSlot()` |
