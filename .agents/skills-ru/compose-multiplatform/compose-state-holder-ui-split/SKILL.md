---
name: compose-state-holder-ui-split
description: Используй, когда экранный composable Jetpack Compose принимает ViewModel/component/controller, собирает состояние или эффекты, обрабатывает навигацию и snackbar либо связывает callback-функции и одновременно рисует layout.
---

# Compose: разделение state holder и UI

## Главный принцип

Отделяй подключение state holder от отрисовки UI. Composable, владеющий состоянием, общается с ViewModel, component, Flow, навигацией и побочными эффектами. UI-composable принимает обычное неизменяемое состояние и callback-функции и описывает layout.

Так экраны проще просматривать в preview, тестировать и переиспользовать на Android, Desktop, TV и KMP/CMP.

## Когда применять

Используй этот подход, если экран Compose:

- напрямую принимает ViewModel, component, controller, navigator, repository или service;
- собирает состояние приложения/бизнес-логику в той же функции, где раскладывает большую часть UI;
- передаёт дочерним composable целый state holder вместо явных значений и callback-функций;
- плохо открывается в preview из-за DI, навигации, lifecycle или fake-сервисов;
- требует поднимать полный стек приложения в UI-тесте ради простой ветки layout.

## Шаблон

Используй небольшой публичный composable для state holder:

```kotlin
@Composable
fun ProfileScreen(component: ProfileComponent, modifier: Modifier = Modifier) {
    val state by component.state.collectAsStateWithLifecycle()

    ProfileScreen(
        state = state,
        onNameChange = component::onNameChange,
        onSaveClick = component::save,
        onBackClick = component::back,
        modifier = modifier,
    )
}
```

Затем вынеси UI в обычный composable, который ничего не знает о state holder:

```kotlin
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProfileContent(
        name = state.name,
        isSaving = state.isSaving,
        canSave = state.canSave,
        onNameChange = onNameChange,
        onSaveClick = onSaveClick,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}
```

Приватные функции контента могут разделять layout:

```kotlin
@Composable
private fun ProfileContent(
    name: String,
    isSaving: Boolean,
    canSave: Boolean,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Только layout.
}
```

## Практические правила

| Ответственность | Composable state holder | UI-composable |
|-----------------|-------------------------|---------------|
| Собирает состояние ViewModel/component | Да | Нет |
| Собирает одноразовые эффекты | Да или маленький соседний обработчик | Обычно нет |
| Хранит объекты из dependency injection | Да | Нет |
| Принимает неизменяемое UI-состояние | Обычно передаёт дальше | Да |
| Принимает callback-функции событий | Подключает | Вызывает |
| Владеет layout, modifier, semantics и test tags | Нет или минимум | Да |
| Владеет локальным UI-состоянием: прокруткой, фокусом, текстом, анимацией, взаимодействием | Иногда создаёт начальное значение | Да |
| Удобен для preview и скриншотов | Не обязательно | Да |

Правило «не собирать состояние в UI-composable» относится к потокам состояния приложения и бизнес-логики. Обычный UI-composable может владеть локальным состоянием framework: `rememberScrollState`, `rememberLazyListState`, `FocusRequester`, состоянием фокуса и анимации, `TextFieldState`, `MutableInteractionSource.collectIsPressedAsState()` и похожим поведением виджета.

Если локальное состояние вырастает в согласованное поведение с несколькими полями и операциями, используй [`compose-state-hoisting`](../compose-state-hoisting/SKILL.md), чтобы решить, нужен ли обычный state holder, запоминаемый в композиции.

## Что передавать

Передавай самый маленький полезный контракт:

- предпочитай отдельный объект `UiState`/`State` множеству несвязанных примитивов;
- используй явные callback-функции (`onRetryClick`, `onItemSelected`), а не передавай целый component;
- не передавай domain-модели в UI-composable, если из-за них бизнес-правила проникают в UI; преобразуй их в UI-модели;
- передавай навигацию callback-функциями. UI сообщает «пользователь нажал назад», а не «перейди на маршрут X»;
- для значений, меняющихся на каждом кадре и не требующих пересборки всего дерева, используй provider-лямбды и отложенное чтение по [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md).

## Побочные эффекты

[`compose-side-effects`](../compose-side-effects/SKILL.md) описывает API эффектов (`LaunchedEffect`, `DisposableEffect`, `SideEffect`), ключи, очистку и `rememberUpdatedState`.

Обрабатывай эффекты рядом со state holder, где доступны и источник эффекта, и императивная цель:

```kotlin
@Composable
fun ProfileScreen(component: ProfileComponent, snackbarHostState: SnackbarHostState) {
    val state by component.state.collectAsStateWithLifecycle()

    LaunchedEffect(component) {
        component.effects.collect { effect ->
            when (effect) {
                ProfileEffect.Saved -> snackbarHostState.showSnackbar("Saved")
            }
        }
    }

    ProfileScreen(state = state, onSaveClick = component::save)
}
```

Если обработчик эффектов растёт, вынеси `ProfileEffects(component, snackbarHostState)`, а не передавай component в UI-composable.

## Частые ошибки

| Ошибка | Почему плохо | Исправление |
|--------|--------------|-------------|
| `fun Screen(viewModel: MyViewModel)` содержит весь layout | Preview и тесты требуют lifecycle Android и DI | Добавь перегрузку UI с `state` и callback-функциями |
| Дочерние composable принимают `component` | Зависимости протекают по дереву | Передавай только нужные ребёнку значения и callback-функции |
| UI-composable запускает навигацию | UI связывается с маршрутизацией приложения | Открой `onBackClick`, `onItemClick` и другие callback-функции |
| UI-composable собирает бизнес-потоки | Жизненный цикл сбора скрыт в layout | Собирай рядом со state holder и передавай значения вниз |
| Локальное UI-состояние поднято без причины | State holder начинает владеть механикой layout | Оставляй прокрутку, фокус, анимацию и текст в UI, если это только поведение UI |
| Для каждого маленького composable сделана перегрузка state holder | Слишком много обвязки | Разделяй на уровне экрана или секции, а не каждого `Row` |

## Когда не применять

- Маленькие одноразовые composable, которые уже принимают обычные значения и callback-функции.
- Примитивы дизайн-системы вроде `Button`, `Card` или `ListItem`: им нужны слоты и modifier, а не state holder.
- Случаи, когда composable state holder лишь передаёт один примитив и не даёт изоляции.

## Связанные навыки

- [`compose-ui-testing-patterns`](../compose-ui-testing-patterns/SKILL.md) — тестирование UI-composable на обычном состоянии без полного графа приложения;
- [`compose-state-hoisting`](../compose-state-hoisting/SKILL.md) — выбор места для состояния и логики элемента UI;
- [`kotlin-multiplatform-expect-actual`](../kotlin-multiplatform-expect-actual/SKILL.md) — платформенные сервисы, native views и границы expect/interface в общем UI.
