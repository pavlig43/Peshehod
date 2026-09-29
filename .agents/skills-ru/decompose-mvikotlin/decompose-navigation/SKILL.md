---
name: decompose-navigation
description: Применяйте при создании навигации Decompose, при упоминании ChildStack, ChildSlot, ChildPages, ChildPanels, ChildItems, StackNavigation, SlotNavigation, PagesNavigation, PanelsNavigation, childStack, childSlot, childPages, childPanels, при выборе модели навигации, переходов push/pop, диалогов, страниц, вкладок, вида «список и детали», глубоких ссылок, классов конфигурации и bringToFront.
version: 1.0.0
---

Вы помогаете создавать навигацию через Decompose. Выберите подходящую модель и точно следуйте схемам.

## Выбор модели навигации

| Случай | Модель |
|---|---|
| Экраны кладут в стек: список → детали, вход → главная | **ChildStack** |
| В один момент есть не более одного необязательного окна: диалог, модальное окно, нижняя панель | **ChildSlot** |
| Горизонтальные страницы и вкладки с пролистыванием: галерея, знакомство | **ChildPages** |
| Гибкий экран «список и детали» рядом | **ChildPanels** |
| Ленивый список, где каждый элемент — действующий компонент | **ChildItems** |
| Другой случай: карусель или своя машина состояний | **Generic Navigation** |

В одном компоненте могут быть несколько моделей, если их `key` различаются.

## Требования к конфигурации

```kotlin
@Serializable  // Нужен плагин kotlinx-serialization.
private sealed interface Config {
    @Serializable data object List : Config
    @Serializable data class Details(val itemId: Long) : Config
}
```

**Правила:** нужна `@Serializable`; неизменяемые `data class` и `data object`; `equals`/`hashCode` у `data class`; небольшой размер — на Android менее 500 КБ; конфигурации внутри `ChildStack` уникальны.

## ChildStack — стек экранов

```kotlin
interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    fun onBackClicked(toIndex: Int)

    sealed class Child {
        class ListChild(val component: ListComponent) : Child()
        class DetailsChild(val component: DetailsComponent) : Child()
    }
}

class DefaultRootComponent(componentContext: ComponentContext) : RootComponent, ComponentContext by componentContext {
    private val nav = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = nav,
            serializer = Config.serializer(),
            initialConfiguration = Config.List,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(config: Config, ctx: ComponentContext): RootComponent.Child {
        return when (config) {
            is Config.List -> ListChild(DefaultListComponent(ctx, onItemSelected = { nav.pushNew(Config.Details(it)) }))
            is Config.Details -> DetailsChild(DefaultDetailsComponent(ctx, config.itemId, onFinished = nav::pop))
        }
    }

    override fun onBackClicked(toIndex: Int) {
        nav.popTo(index = toIndex)
    }

    @Serializable private sealed interface Config {
        @Serializable data object List : Config
        @Serializable data class Details(val itemId: Long) : Config
    }
}
```

**Основные действия:** `push`, `pushNew` для защиты от двойного нажатия, `pushToFront`, `pop`, `popTo(index)`, `popWhile { }`, `replaceCurrent`, `replaceAll`, `bringToFront` только для вкладок.

**Передача результата:**

```kotlin
nav.pop {  // onComplete вызывается после перехода.
    (stack.active.instance as? ListChild)?.component?.onItemDeleted(itemId)
}
```

## ChildSlot — одно необязательное окно

```kotlin
class DefaultCounterComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val dialogNav = SlotNavigation<DialogConfig>()

    val dialogSlot: Value<ChildSlot<*, DialogComponent>> =
        childSlot(
            source = dialogNav,
            serializer = null,
            handleBackButton = true,
            childFactory = { config, _ -> DefaultDialogComponent(config.value, dialogNav::dismiss) },
        )

    fun onInfoClicked() {
        dialogNav.activate(DialogConfig(value = 42))
    }

    @Serializable private data class DialogConfig(val value: Int)
}
```

**Действия:** `activate(config)`, `dismiss()`.
**В Compose:** `slot.child?.instance?.also { dialog -> ... }`.

## ChildPages — страницы

```kotlin
class DefaultGalleryComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val nav = PagesNavigation<ImageId>()

    val pages: Value<ChildPages<*, ImageComponent>> =
        childPages(
            source = nav,
            serializer = ImageId.serializer(),
            initialPages = { Pages(items = ImageId.entries, selectedIndex = 0) },
            childFactory = { id, ctx -> DefaultImageComponent(ctx, id) },
        )

    fun selectPage(index: Int) {
        nav.select(index = index)
    }
}
```

**Действия:** `select(index)`, `selectNext()`, `selectPrev()`, `selectFirst()`, `selectLast()`, `setItems(items, selectedIndex)`.

## ChildPanels — несколько панелей

```kotlin
class DefaultMultiPaneComponent(componentContext: ComponentContext) : ComponentContext by componentContext {
    private val nav = PanelsNavigation<Unit, DetailsConfig, ExtraConfig>()

    val panels = childPanels(
        source = nav,
        initialPanels = { Panels(main = Unit) },
        serializers = Triple(null, DetailsConfig.serializer(), ExtraConfig.serializer()),
        handleBackButton = true,
        mainFactory = { _, ctx -> MainChild(DefaultListComponent(ctx, onItemSelected = { id -> nav.navigate { it.copy(details = DetailsConfig(id)) } })) },
        detailsFactory = { config, ctx -> DetailsChild(DefaultDetailsComponent(ctx, config.itemId, onFinished = { nav.navigate { it.copy(details = null) } })) },
        extraFactory = { config, ctx -> ExtraChild(DefaultExtraComponent(ctx, config)) },
    )

    fun setMode(mode: ChildPanelsMode) {
        nav.navigate { it.copy(mode = mode) }
    }
}
```

**Режимы:** `SINGLE` для телефона, `DUAL` для планшета, `TRIPLE` для большого планшета и компьютера. Управляйте из Compose через `BoxWithConstraints` и `LaunchedEffect`.

## Несколько моделей

```kotlin
private val mainStack = childStack(source = mainNav, key = "MainStack", ...)
private val sideStack = childStack(source = sideNav, key = "SideStack", ...)
```

## Глубокие ссылки

```kotlin
override val stack = childStack(
    source = nav,
    serializer = Config.serializer(),
    initialStack = { buildInitialStack(deepLinkUrl) },
    childFactory = ::createChild,
)
```

## Важные ограничения

- **Переходите только в главном потоке:** фоновая навигация даёт непредсказуемый результат.
- **Конфигурации `ChildStack` должны быть уникальны:** иначе по умолчанию будет ошибка; для разрешения дублей есть `DecomposeSettings.duplicateConfigurationsEnabled = true`.
- **Для вкладок берите `bringToFront`, а не `push`:** `push` создаёт дубли.
- **Для защиты от двойного нажатия берите `pushNew`, а не `push`.**
- **Помните предел размера Android Bundle:** конфигурации должны быть небольшими.
- **Ставьте `serializer = null`,** если намеренно не хотите хранить состояние, например для краткого диалога.
- **У нескольких стеков должны различаться `key`:** стандартный `"default"` совпадёт.
