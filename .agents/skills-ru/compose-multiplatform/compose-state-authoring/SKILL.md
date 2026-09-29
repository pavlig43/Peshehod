---
name: compose-state-authoring
description: Используй при написании или проверке кода Jetpack Compose, если видишь локальный var без State в @Composable, remember { mutableStateOf(...) }, mutableStateListOf/mutableStateMapOf или @ReadOnlyComposable.
---

# Создание состояния в Compose

Не любой `remember { … }` относится сюда. Этот навык охватывает **локальное состояние UI** (`remember { mutableStateOf(…) }`, `mutableStateListOf` / `mutableStateMapOf`) и **`@ReadOnlyComposable`**. Для других API есть отдельные навыки:

- **`rememberCoroutineScope` / `rememberUpdatedState`** → [`compose-side-effects`](../compose-side-effects/SKILL.md)
- **`rememberLazyListState` / `rememberScrollState`** при чтении на каждом кадре → [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md)
- **Переходы фокуса, состояние фокуса, владение `FocusRequester`** → [`compose-focus-navigation`](../compose-focus-navigation/SKILL.md)

## Главный принцип

Среда Compose снова вызывает `@Composable`, когда меняются его входные данные. Для локального состояния нужно ответить на два вопроса:

1. **Изменяемое локальное состояние:** переживает ли `var` рекомпозицию и запускает ли её? Иначе значение незаметно сбрасывается при каждом повторном вызове, а запись не видна UI.
2. **Что делает composable:** меняет ли композицию — создаёт узлы, слоты, вызывает `remember` — или только читает её? Если лишь читает, `@ReadOnlyComposable` помогает среде пропустить часть работы.

Ошибка в любом пункте даёт тонкие сбои: исчезающее состояние или неработающую оптимизацию.

## Когда применять

Применяй навык, если видишь:

- `var x = …` внутри `@Composable fun` или composable-лямбды (`Column { var x = … }`);
- `@Composable fun` или `@Composable get()`, где тело ничего не размещает на экране;
- `@ReadOnlyComposable` на функции, которая вызывает `Text`, `Box`, `Column`, `remember` и т. п.;
- состояние UI, которое странным образом сбрасывается после поворота, смены темы или рекомпозиции.

## 1. `var` в composable должен опираться на State

При рекомпозиции composable исполняется с начала. Локальный `var` создаётся заново, предыдущее значение теряется, а запись не сообщает среде о нужной рекомпозиции.

```kotlin
// ❌ ПЛОХО — счётчик сбрасывается при каждой рекомпозиции, нажатия не обновляют UI
@Composable
fun Counter() {
    var count = 0
    Button(onClick = { count++ }) { Text("$count") }
}

// ❌ ТОЖЕ ПЛОХО — правило действует и внутри composable-лямбды
@Composable
fun Wrapper() {
    Row {
        var count = 0         // Лямбда контента Row тоже @Composable
        // …
    }
}
```

```kotlin
// ✅ ХОРОШО — `remember` переживает рекомпозицию, `mutableStateOf` запускает её
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) { Text("$count") }
}
```

Нужны обе части:

- `remember { … }` **сохраняет значение** между рекомпозициями. Без него значение создаётся заново.
- `mutableStateOf(…)` **запускает рекомпозицию**. Без него среда не видит изменение.

Для коллекций предпочитай `mutableStateListOf` / `mutableStateMapOf` и тоже запоминай их через `remember`. Они отмечают каждое чтение и изменение Snapshot. Конструкция `remember { mutableStateOf(mutableListOf<X>()) }` с последующим `list.add(x)` не вызовет рекомпозицию: `MutableList.add` обходит setter State. Нужно присвоить новое значение (`state = state + x`).

### Запись Snapshot-состояния назад во время композиции

**Обратная запись** — изменение наблюдаемого состояния в фазе, которое делает недействительной предыдущую или текущую фазу. Если менять `mutableState*` прямо в теле composable, текущий проход композиции назначит ещё один. Не пересоздавай вычисленные данные так:

```kotlin
// ❌ ПЛОХО — clear и putAll при каждой композиции
val merged = remember { mutableStateMapOf<Key, ViewState>() }
merged.clear()
merged.putAll(parent)
merged.putAll(overlay)

// ✅ ХОРОШО — неизменяемый результат запомнен по входам
val merged = remember(parent, overlay) {
    if (overlay.isEmpty()) parent else parent + overlay
}
```

Если при этих входных данных результат доступен лишь для чтения, хватит `remember(keys) { … }`. Чтение размеров соседних строк и правки фазы измерения описаны в [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md).

### Где правило не действует

- **Внутри блока создания `remember { … }`.** Он запускается один раз на смену ключа, а не при каждой рекомпозиции. Локальный `var` там допустим: `val builder = remember { mutableListOf<X>().apply { var n = 0; … } }`.
- **В обычной, не `@Composable`, лямбде, переданной наружу.** `onClick = { var a = 0; … }` — простая `() -> Unit`, и локальные переменные работают как в Kotlin.
- **В обычных вспомогательных функциях.** Правило относится только к composable-областям.

## 2. Контракт `@ReadOnlyComposable`

`@ReadOnlyComposable` объявляет, что composable **только читает** состояние композиции: не вызывает `Text`, `Box`, `remember`, не создаёт узлы layout и позиционные слоты. Среда может не создавать группу для вызова. Это полезно для быстрых функций доступа вроде `MaterialTheme.colorScheme`, `LocalDensity.current` и токенов дизайн-системы.

Контракт работает в обе стороны:

- **Добавь `@ReadOnlyComposable`**, если каждый composable-вызов в теле тоже помечен `@ReadOnlyComposable` либо таких вызовов нет — например, функция только читает `LocalFoo.current` и возвращает значение.
- **Не добавляй**, если вызывается хоть один обычный composable. Оптимизация предполагает отсутствие участия в композиции; нарушение может вызвать неверную рекомпозицию у вызывающего кода.

```kotlin
// ✅ ХОРОШО — лишь читает CompositionLocal, без layout и remember
@Composable
@ReadOnlyComposable
fun appSpacing(): Dp = LocalDimensions.current.spacing

// ✅ ХОРОШО — getter composable-свойства с тем же правилом
val accent: Color
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary
```

```kotlin
// ❌ ПЛОХО — помечена read-only, но создаёт Box
@Composable
@ReadOnlyComposable
fun Header(): Int {
    Box {}                  // Вызов composable, который не является read-only
    return 42
}

// ❌ ПЛОХО — read-only-функция вызывает обычный composable
@Composable
@ReadOnlyComposable
fun computed(): Int = nonReadOnlyHelper()
```

### Как понять, нужна ли аннотация

Если в теле есть что-то из списка, **не ставь** `@ReadOnlyComposable`:

- вызовы layout: `Box`, `Column`, `Row`, `LazyColumn`, `Text`, всё из `androidx.compose.foundation.layout` и `androidx.compose.material*`;
- эффекты: `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `produceState`;
- `remember { … }` — позиционное запоминание является состоянием композиции;
- вызов `@Composable`-лямбды (`content()`);
- вызов composable без `@ReadOnlyComposable`.

Если тело только читает `Local*.current`, вызывает другие `@ReadOnlyComposable` или выполняет чистое вычисление, **добавь** аннотацию.

### Где правило не действует

- **Объявления `override fun`.** Аннотация входит в контракт базовой функции. Если базовая функция не `@ReadOnlyComposable`, добавить её в override нельзя. Исправь базовую функцию или прими затраты на создание группы.
- **Абстрактные объявления.** Тела для проверки нет.

## Связь с побочными эффектами

Если нужны `LaunchedEffect`, `DisposableEffect`, `SideEffect`, `rememberCoroutineScope`, `rememberUpdatedState`, `snapshotFlow`, snackbar, навигация, аналитика или сбор Flow, используй [`compose-side-effects`](../compose-side-effects/SKILL.md).

Вопросы разделены так: **переходы и состояние фокуса, владение `FocusRequester`, поведение** → [`compose-focus-navigation`](../compose-focus-navigation/SKILL.md); **когда** вызывать императивный `requestFocus` — время эффекта, жизненный цикл, ключи, выбор API — → [`compose-side-effects`](../compose-side-effects/SKILL.md).

Этот навык посвящён созданию состояния Compose. `rememberUpdatedState` хранит захваченные значения эффекта и не заменяет `remember { mutableStateOf(...) }`. У эффектов отдельные правила жизненного цикла и ключей.

## Краткая памятка

| Симптом | Причина | Исправление |
|---|---|---|
| `var x = …` в `@Composable fun` | Не переживает рекомпозицию | `var x by remember { mutableStateOf(…) }` |
| `var x = …` внутри `Column { … }` / `Row { … }` | Лямбды контента тоже `@Composable` | То же исправление |
| После `remember { mutableStateOf(list) }` вызов `.add(x)` не обновляет UI | Изменение обходит setter State | Примени `mutableStateListOf` или присвой `state = state + x` |
| `stateMap.clear(); stateMap.putAll(...)` в теле composable | Запись из композиции в ту же композицию | `remember(keys) { derivedSnapshot }` |
| `@Composable fun` без `Text`/`Box`/`remember`/эффектов | Может быть read-only | Добавь `@ReadOnlyComposable` |
| Функция с `@ReadOnlyComposable` вызывает `Box {}`, `Column {}` или обычный composable | Нарушен контракт | Убери `@ReadOnlyComposable` |

## Когда не применять

- **Тесты** с `composeTestRule.setContent { … }` следуют тем же правилам: это рабочие composable.
- **`produceState`** имеет свой блок создания, который запускается в coroutine; внутри не нужен ещё один `LaunchedEffect`.
- **`derivedStateOf`** решает свои вопросы стабильности и равенства: он помогает предотвратить рекомпозицию, а не создать состояние.
- **`override` read-only-объявлений:** аннотацию задаёт базовая функция; локально её не добавить и не убрать.

## Тревожные признаки при проверке

| Мысль | Что на деле |
|---|---|
| «Composable маленький, простой `var` допустим» | Рекомпозиция может произойти в любой момент. Сброс непредсказуем. |
| «Добавлю `@ReadOnlyComposable`, потому что функция выглядит простой» | Важна не простота, а только read-only-вызовы. |
| «Всегда беру `LaunchedEffect`, потому что знаю его» | Выбор эффекта зависит от жизненного цикла и ключей; смотри `compose-side-effects`. |
| «Просто вызову `.add()` у запомненного списка» | `mutableStateOf(List)` не видит внутреннее изменение; возьми `mutableStateListOf` или замени значение. |
| «В override нужна `@ReadOnlyComposable`, раз тело только читает» | Если её нет в базе, добавить в override нельзя. Исправь базовый контракт. |
