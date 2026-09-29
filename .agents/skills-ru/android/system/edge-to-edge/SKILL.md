---
name: edge-to-edge
description: Применяйте при переходе приложения Jetpack Compose на гибкий интерфейс от края до края и поиске частых ошибок. Навык помогает исправить кнопки и списки, которые перекрыты строкой состояния или панелью навигации, поля ввода за клавиатурой и плохо видимые системные значки.
license: Полный текст условий в LICENSE.txt
metadata:
  author: Google LLC
  keywords:
  - android
  - compose
  - system bars
  - edge-to-edge
  - status bar
  - navigation bar
---

## Условия

- Проект **обязательно** должен использовать Android Jetpack Compose.
- Целевая версия SDK — **35 или выше**. Если она ниже, поднимите её до 35.

## Шаг 1. Составьте план

1. Найдите все `Activity` и выясните, где уже есть интерфейс от края до края. Для остальных запланируйте его добавление.
2. В каждой `Activity` изучите списки и плавающие кнопки FAB. Запланируйте поддержку краёв для тех, где её нет.
3. Найдите `TextField`, `OutlinedTextField` и `BasicTextField`. Если они есть, **обязательно** проверьте по разделу IME ниже, не закрывает ли клавиатура поле ввода.

## Шаг 2. Включите интерфейс от края до края

1. В `onCreate` каждой нужной `Activity` вызовите `enableEdgeToEdge` перед `setContent`, если вызова ещё нет.
2. Для каждой `Activity` с экранной клавиатурой задайте `android:windowSoftInputMode="adjustResize"` в `AndroidManifest.xml`.

## Шаг 3. Учтите системные отступы

Приложение **обязательно** должно учитывать системные области или выравнивать содержимое по направляющим, чтобы важные элементы оставались доступными для нажатия. Выберите **один** способ, иначе отступ удвоится:

1. **Лучше всего:** где возможно, берите `Scaffold` и передавайте `PaddingValues` внутрь содержимого.

```kotlin
Scaffold { innerPadding ->
    // innerPadding учитывает системные панели и элементы Scaffold.
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(innerPadding),
        contentPadding = innerPadding
    ) { /* Содержимое */ }
}
```

<br />

2. **Тоже предпочтительно:** берите встроенную работу с системными областями или модификаторы отступов у компонентов Material, где они есть.
   - Компоненты Material 3 сами учитывают безопасные области: `TopAppBar`, `SmallTopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, `LargeTopAppBar`, `BottomAppBar`, `ModalDrawerSheet`, `DismissibleDrawerSheet`, `PermanentDrawerSheet`, `ModalBottomSheet`, `NavigationBar`, `NavigationRail`.
   - В Material 2 задавайте отступы панелям `BottomAppBar`, `TopAppBar` и `BottomNavigation` через параметр `windowInsets`. **Не** добавляйте отступ родителю: фон панели тогда не дойдёт до системной области. Для `TopAppBar` выберите один вариант:
     1. **Предпочтительно:** `TopAppBar(windowInsets = AppBarDefaults.topAppBarWindowInsets)`.
     2. `TopAppBar(windowInsets = WindowInsets.systemBars.exclude(WindowInsets.navigationBars))`.
     3. `TopAppBar(windowInsets = WindowInsets.systemBars.add(WindowInsets.captionBar))`.
3. Для элементов вне `Scaffold` берите модификаторы вроде `Modifier.safeDrawingPadding()` или `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`.

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
) {
    Button(
        onClick = {},
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Text("Войти")
    }
}
```

<br />

4. При глубокой вложенности и избыточных отступах берите `WindowInsetsRulers`, например `Modifier.fitInside(WindowInsetsRulers.SafeDrawing.current)`. Пример есть в разделе IME.
5. Если элемент должен быть высотой с системную панель, например свой заголовок или затемнение, берите модификатор размера, например `Modifier.windowInsetsTopHeight(WindowInsets.systemBars)`. Пример есть в разделе списков.

## Гибкие `Scaffold`

`NavigationSuiteScaffold` сам учитывает безопасные области у своих `NavigationRail` и `NavigationBar`. Но гибкие контейнеры, например `NavigationSuiteScaffold` и `ListDetailPaneScaffold`, не передают `PaddingValues` своему внутреннему содержимому. **Обязательно** применяйте системные отступы к **отдельным** экранам и элементам: к `contentPadding` списка или отступам FAB, как в шаге 3. **Не** ставьте `safeDrawingPadding` и похожие модификаторы на родителя `NavigationSuiteScaffold`: это обрежет экран и не даст рисовать от края до края.

## IME — экранная клавиатура

В каждой `Activity` с клавиатурой проверьте `android:windowSoftInputMode="adjustResize"` в `AndroidManifest.xml`. Не берите устаревший `SOFT_INPUT_ADJUST_RESIZE`. Затем следите, чтобы поле ввода оставалось доступным. Выберите одно:

1. **Предпочтительно:** поставьте `Modifier.fitInside(WindowInsetsRulers.Ime.current)` на контейнер содержимого. Это обычно лучше `imePadding()`: меньше рывков и лишних отступов, которые возникают, если выше по дереву забыли отметить уже учтённые системные области.
2. Поставьте `imePadding` на контейнер содержимого. Он **обязательно** должен стоять перед `Modifier.verticalScroll()`. Не ставьте `Modifier.imePadding()`, если родитель уже учитывает клавиатуру через `contentWindowInsets`, например `contentWindowInsets = WindowInsets.safeDrawing`: отступ станет двойным.

### IME со `Scaffold`

#### ВЕРНО

`contentWindowInsets` содержит область IME и передаёт её в `innerPadding`:

```kotlin
// ВЕРНО
Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .verticalScroll(rememberScrollState())
    ) { /* Содержимое */ }
}
```

<br />

`fitInside` подгоняет содержимое под IME независимо от `contentWindowInsets`:

```kotlin
// ВЕРНО
Scaffold() { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .fitInside(WindowInsetsRulers.Ime.current)
            .verticalScroll(rememberScrollState())
    ) { /* Содержимое */ }
}
```

<br />

Стандартный `contentWindowInsets` не содержит область IME, поэтому её добавляет `imePadding()`:

```kotlin
// ВЕРНО
Scaffold() { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) { /* Содержимое */ }
}
```

<br />

#### НЕВЕРНО

При открытии клавиатуры отступ станет слишком большим: область IME учтена и в `innerPadding` из переданного `contentWindowInsets`, и в `imePadding()`:

```kotlin
// НЕВЕРНО
Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) { /* Содержимое */ }
}
```

<br />

Стандартный `contentWindowInsets` у `Scaffold` не содержит IME, поэтому клавиатура закроет содержимое:

```kotlin
// НЕВЕРНО
Scaffold() { innerPadding ->
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
    ) { /* Содержимое */ }
}
```

<br />

### IME без `Scaffold`

#### ВЕРНО

Следующие примеры не создают лишних отступов:

```kotlin
// ВЕРНО
Box(
    // Системные области уже учтены.
    modifier = Modifier.safeDrawingPadding() // Или imePadding(), safeContentPadding(), safeGesturesPadding().
) {
    Column(
        modifier = Modifier.imePadding()
    ) { /* Содержимое */ }
}
```

<br />

```kotlin
// ВЕРНО
Box(
    // Системные области уже учтены.
    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing) // Или WindowInsets.ime, WindowInsets.safeContent, WindowInsets.safeGestures.
) {
    Column(
        modifier = Modifier.imePadding()
    ) { /* Содержимое */ }
}
```

<br />

```kotlin
// ВЕРНО
Box(
    // Отступы ещё не отмечены учтёнными, но это не мешает fitInside.
    modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()) // Или WindowInsets.ime.asPaddingValues(), WindowInsets.safeContent.asPaddingValues(), WindowInsets.safeGestures.asPaddingValues().
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fitInside(WindowInsetsRulers.Ime.current)
    ) { /* Содержимое */ }
}
```

<br />

#### НЕВЕРНО

Здесь область IME учитывается дважды, поэтому отступ станет слишком большим:

```kotlin
// НЕВЕРНО
Box(
    // Системные области ещё не отмечены учтёнными.
    modifier = Modifier.padding(WindowInsets.safeDrawing.asPaddingValues()) // Или WindowInsets.ime.asPaddingValues(), WindowInsets.safeContent.asPaddingValues(), WindowInsets.safeGestures.asPaddingValues().
) {
    Column(
        modifier = Modifier.imePadding()
    ) { /* Содержимое */ }
}
```

<br />

## Контраст панели навигации и системных значков

- Если `Activity` вызывает `enableEdgeToEdge` через `WindowCompat`, **обязательно** выставьте `isAppearanceLightNavigationBars` и `isAppearanceLightStatusBars` обратно значению тёмной темы для приложения со светлой и тёмной темой: тогда системные значки будут читаемы. Лучше делать это в теме. Если `Activity` вызывает `enableEdgeToEdge` из `ComponentActivity`, **не** делайте этого: она сама выбирает цвет значков.

```kotlin
// Только для enableEdgeToEdge из WindowCompat.
// Разместите в файле темы.
@Composable
fun MyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)

            // Тёмные значки в светлой теме, светлые — в тёмной.
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(content = content)
}
```

<br />

- Если экран использует `Scaffold` или `NavigationSuiteScaffold` с нижней панелью, например `BottomAppBar` или `NavigationBar`, для SDK 29+ поставьте `window.isNavigationBarContrastEnforced = false` в нужной `Activity`. Тогда система не добавит полупрозрачный фон панели навигации; проверьте, что цвет нижней панели доходит до края экрана.

## Списки

- Передавайте системные отступы, например `innerPadding` от `Scaffold`, в `contentPadding` прокручиваемого элемента (`LazyColumn`, `LazyRow`). **Не** ставьте их через `Modifier.padding()` на родителя списка: содержимое обрежется и не сможет уходить за системные панели.
- Сделайте полупрозрачный composable-элемент поверх системной панели, чтобы значки оставались видны.

```kotlin
class SystemBarProtectionSnippets : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // enableEdgeToEdge ставит window.isNavigationBarContrastEnforced = true:
        // для навигации с тремя кнопками появляется полупрозрачный фон.
        enableEdgeToEdge()

        setContent {
            MyTheme {
                // Главное содержимое.
                MyContent()
                // Затем защита строки состояния.
                StatusBarProtection()
            }
        }
    }
}

@Composable
private fun StatusBarProtection(
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                with(LocalDensity.current) {
                    (WindowInsets.statusBars.getTop(this) * 1.2f).toDp()
                }
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 1f),
                        color.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    )
}
```

<br />

## Диалоги

Диалог занимает весь экран и тоже должен рисоваться от края до края, если выполнены оба условия:

1. В `DialogProperties` задано `usePlatformDefaultWidth = false`.
2. Диалог вызывает `Modifier.fillMaxSize()`.

Для такого диалога поставьте `decorFitsSystemWindows = false` в `DialogProperties`:

```kotlin
Dialog(
    onDismissRequest = { /* Обработайте закрытие. */ },
    properties = DialogProperties(
        // Даёт диалогу занять всю ширину.
        usePlatformDefaultWidth = false,
        // Даёт рисовать за строкой состояния и панелью навигации.
        decorFitsSystemWindows = false
    )
) { /* Содержимое */ }
```

<br />

## Список проверки

- [ ] Каждая `Activity` вызывает `enableEdgeToEdge()`?
- [ ] В `AndroidManifest.xml` задан `adjustResize`?
- [ ] У каждого `TextField`, `OutlinedTextField` и `BasicTextField` есть родитель с `imePadding()`, `fitInside`, `Modifier.safeDrawingPadding()`, `Modifier.safeContentPadding()`, `Modifier.safeGesturesPadding()` либо `contentWindowInsets`, равным `WindowInsets.safeDrawing` или `WindowInsets.ime`?
- [ ] Первый и последний элементы списка не заходят под системные панели благодаря отступам в `contentPadding`?
- [ ] FAB находится выше панели навигации благодаря `Scaffold` или `Modifier.safeDrawingPadding()`?
- [ ] Проект собирается? Для проверки запустите `./gradlew build`.
