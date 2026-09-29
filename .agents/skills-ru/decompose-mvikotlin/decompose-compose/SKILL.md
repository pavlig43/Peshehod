---
name: decompose-compose
description: Применяйте при связи компонентов Decompose с Jetpack Compose или Compose Multiplatform, при упоминании subscribeAsState, Children, stackAnimation, predictiveBackAnimation, ChildPages, ChildPanels, показа ChildSlot, вкладок с нижней панелью, LifecycleController для компьютера, образцов Compose для компонентов Decompose или обработки «Назад» через BackHandler Decompose.
version: 1.0.0
---

Вы связываете компоненты Decompose с интерфейсом Compose. Точно следуйте этим схемам.

## Наблюдение за состоянием

Преобразуйте `Value<T>` в `State<T>` Compose через `subscribeAsState()`.

```kotlin
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@Composable
fun CounterContent(component: CounterComponent, modifier: Modifier = Modifier) {
    val model by component.model.subscribeAsState()  // Подписка и отписка выполняются сами.

    Column(modifier = modifier) {
        Text(text = model.count.toString())
        Button(onClick = component::onIncrementClicked) { Text("Увеличить") }
    }
}
```

**Правила:** всегда берите делегирование через `by`. Не подписывайтесь вручную. Открывайте `Model` как `data class`, а не сырое изменяемое состояние.

## Показ стека детей

```kotlin
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade() + scale()),
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.ListChild -> ListContent(child.component)
            is RootComponent.Child.DetailsChild -> DetailsContent(child.component)
        }
    }
}
```

Движение можно сочетать через `+`: `fade()`, `scale()`, `slide()` или своё `stackAnimation { ... }`.

## Жест возврата с предпросмотром на Android 13+

```kotlin
@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = predictiveBackAnimation(
            backHandler = component.backHandler,
            fallbackAnimation = stackAnimation(fade() + scale()),
            onBack = component::onBackClicked,
        ),
    ) { /* Содержимое */ }
}
```

Интерфейс должен расширять `BackHandlerOwner`: `interface RootComponent : BackHandlerOwner { ... }`.

## Показ дочернего окна или диалога

```kotlin
val dialogSlot by component.dialogSlot.subscribeAsState()
dialogSlot.child?.instance?.also { dialog ->
    AlertDialog(
        onDismissRequest = dialog::onDismissClicked,
        text = { Text(dialog.message) },
        confirmButton = { Button(onClick = dialog::onDismissClicked) { Text("Хорошо") } },
    )
}
```

## Показ дочерних страниц

```kotlin
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation

ChildPages(
    pages = component.pages,
    onPageSelected = component::selectPage,
    modifier = modifier,
    scrollAnimation = PagesScrollAnimation.Default,
) { _, page ->
    ImageContent(component = page, modifier = Modifier.fillMaxSize())
}
```

## Показ нескольких панелей

```kotlin
BoxWithConstraints(modifier = modifier) {
    val mode = when {
        maxWidth >= 1200.dp -> ChildPanelsMode.TRIPLE
        maxWidth >= 800.dp -> ChildPanelsMode.DUAL
        else -> ChildPanelsMode.SINGLE
    }
    LaunchedEffect(mode) {
        component.setMode(mode)
    }
    ChildPanels(
        panels = panels,
        mainChild = { ArticleListContent(it.instance) },
        detailsChild = { ArticleDetailsContent(it.instance) },
        layout = HorizontalChildPanelsLayout(dualWeights = Pair(0.4f, 0.6f), tripleWeights = Triple(0.3f, 0.4f, 0.3f)),
    )
}
```

## Вкладки с нижней панелью

```kotlin
@Composable
fun TabsContent(component: TabsComponent, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Children(stack = component.stack, modifier = Modifier.weight(1f)) {
            when (val child = it.instance) {
                is TabsComponent.Child.HomeChild -> HomeContent(child.component)
                is TabsComponent.Child.ProfileChild -> ProfileContent(child.component)
            }
        }
        val stack by component.stack.subscribeAsState()
        val active = stack.active.instance
        NavigationBar {
            NavigationBarItem(selected = active is TabsComponent.Child.HomeChild, onClick = component::onHomeTabClicked, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Главная") })
            NavigationBarItem(selected = active is TabsComponent.Child.ProfileChild, onClick = component::onProfileTabClicked, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Профиль") })
        }
    }
}
```

Для смены вкладки компонент берёт `bringToFront`, а не `push`:

```kotlin
fun onHomeTabClicked() {
    nav.bringToFront(Config.Home)
}
```

## Компьютер: `LifecycleController`

```kotlin
application {
    val windowState = rememberWindowState()
    LifecycleController(lifecycle, windowState)   // Обязателен на компьютере.

    Window(onCloseRequest = ::exitApplication, state = windowState) { RootContent(root) }
}
```

## Образцы Compose

```kotlin
class PreviewCounterComponent : CounterComponent {
    override val model: Value<CounterComponent.Model> = MutableValue(CounterComponent.Model(count = 42))
    override val dialogSlot: Value<ChildSlot<*, DialogComponent>> = MutableValue(ChildSlot())
    override fun onIncrementClicked() {}
    override fun onInfoClicked() {}
}

@Preview @Composable
fun CounterContentPreview() {
    CounterContent(PreviewCounterComponent())
}
```

Для `ComponentContext` в образцах: `val PreviewContext = DefaultComponentContext(LifecycleRegistry())`.

## Обработка «Назад» в Compose

```kotlin
DisposableEffect(component.backHandler, hasChanges) {
    val callback = BackCallback(isEnabled = hasChanges) { component.onBackClicked() }
    component.backHandler.register(callback)
    onDispose { component.backHandler.unregister(callback) }
}
```

Не берите composable-функцию `BackHandler {}` из Jetpack: она обходит дерево компонентов.

## Важные ограничения

- **Никогда не создавайте корневой компонент внутри `@Composable`:** код может запуститься в фоновом потоке.
- **`subscribeAsState()` сам управляет подпиской:** не вызывайте `subscribe()` и `unsubscribe()` вручную.
- **`Children()` управляет `SaveableStateHolder`:** не добавляйте свой `rememberSaveableStateHolder`.
- **На компьютере нужен `LifecycleController`:** без него компоненты не получат события жизненного цикла.
- **Для вкладок берите `bringToFront`, а не `push`.**

## Зависимости

```kotlin
commonMain.dependencies {
    implementation("com.arkivanov.decompose:decompose:$decomposeVersion")
    implementation("com.arkivanov.decompose:extensions-compose:$decomposeVersion")
}
```
