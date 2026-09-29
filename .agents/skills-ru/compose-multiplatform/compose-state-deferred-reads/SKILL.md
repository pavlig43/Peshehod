---
name: compose-state-deferred-reads
description: Используй, когда Jetpack Compose читает состояние прокрутки, анимации, жестов или другие значения, меняющиеся на каждом кадре, в композиции; передаёт такие значения между composable; применяет формы layout/draw-модификаторов со значением; либо записывает наблюдаемое состояние из поздней фазы в уже завершённую.
---

# Отложенное чтение состояния в Compose

## Главный принцип

Чтение состояния делает недействительной ту фазу, где оно происходит. Если `State<T>` прочитан в теле composable, его смена запускает композицию. Если чтение происходит в layout или draw, можно повторить лишь layout или draw. Состояние, меняющееся на каждом кадре, — смещение прокрутки, анимация, положение перетаскивания — обычно следует читать в layout/draw.

**Обратная запись** — симметричная проблема: запись наблюдаемого состояния из фазы, которая делает недействительной более раннюю фазу. Compose выполняет композицию → layout → draw. Запись в snapshot-состояние из layout или draw, если оно читалось в композиции, запускает новую композицию. То же происходит при записи в композиции состояния, прочитанного ранее в том же проходе. Оба случая создают лишнюю работу, иногда для всех соседних элементов lazy-списка.

Правка должна менять структуру: передавай `State<T>` или лямбду, выдающую значение, и читай внутри callback layout/draw. Измерения сохраняй в callback и применяй в фазе measure, а не читай их в телах соседних composable.

## Когда применять

- `val x by animate*AsState(...)` передаётся в `Modifier.offset(x = ...)`, `Modifier.size(...)`, `Modifier.graphicsLayer(...)` или другой модификатор со значением.
- `LazyListState.firstVisibleItemScrollOffset`, `ScrollState.value`, `Animatable.value` или состояние жеста читается в теле composable.
- Composable принимает `scrollOffset: Int`, `progress: Float`, `dragOffset: Offset` или похожее значение, меняющееся на каждом кадре.
- Счётчики рекомпозиции растут при прокрутке, анимации или жестах, хотя данные не меняются.
- Тело composable при каждом проходе вызывает `stateMap[key] = …`, `list.addAll(…)` или похожую обратную запись.
- Один элемент lazy-списка сохраняет размер через `onSizeChanged` / `onGloballyPositioned`, а сосед читает высоту в композиции (`Modifier.height(state.dp)`) — запись layout → composition.

## 0. Обратная запись

**Обратная запись** — изменение наблюдаемого состояния в фазе, которое делает недействительной более раннюю или текущую фазу. Compose идёт по порядку composition → layout → draw, поэтому опасны:

- запись snapshot-состояния в ходе композиции, если оно читается в ней же;
- запись в layout (из `Modifier.layout`, `onSizeChanged`, `onGloballyPositioned`), если состояние читается в композиции;
- запись в draw, если состояние читается в композиции или layout.

Во всех случаях запись назначает лишние проходы, часто задевая соседние элементы lazy-списка.

Не меняй `mutableStateOf`, `mutableStateListOf`, `mutableStateMapOf` или другое snapshot-состояние в теле composable на каждом проходе:

```kotlin
// ❌ ПЛОХО — меняет наблюдаемую map во время композиции
@Composable
fun MergeOverlay(parent: Map<Key, ViewState>, overlay: Map<Key, ViewState>): Map<Key, ViewState> {
    val merged = remember { mutableStateMapOf<Key, ViewState>() }
    merged.clear()
    merged.putAll(parent)
    merged.putAll(overlay)   // обратная запись composition → composition
    return merged
}

// ✅ ХОРОШО — слияние только для чтения, без записи во время композиции
@Composable
fun MergeOverlay(parent: Map<Key, ViewState>, overlay: Map<Key, ViewState>): Map<Key, ViewState> =
    remember(parent, overlay) {
        if (overlay.isEmpty()) parent else parent + overlay
    }
```

Для вычисленного снимка, который только читается, предпочитай `remember(keys) { … }`. Записывай в `mutableState*` из обработчиков событий (`onClick`) или эффектов, а не для пересоздания данных при каждой композиции.

Callback вроде `onSizeChanged` пишет **в фазе layout**. Это безопасно лишь тогда, когда полученное состояние не читается в более ранней фазе. См. случай соседних строк ниже.

### Измерение соседних строк (обратная запись layout → composition)

Если строка B должна иметь высоту строки A, не читай сохранённый размер A в теле composable строки B. `onSizeChanged` пишет в layout; если B читает результат в композиции, layout только что сделал обратную запись:

```kotlin
var anchorHeightPx by remember { mutableIntStateOf(0) }

// ❌ ПЛОХО — B читает размер в композиции; вставка/фокус могут дважды пересобрать B
RowA(Modifier.onSizeChanged { anchorHeightPx = it.height })
RowB(Modifier.height(with(LocalDensity.current) { anchorHeightPx.toDp() }))

// ✅ ХОРОШО — A сохраняет размер, B применяет его только при measure
RowA(Modifier.onSizeChanged { if (it.height != anchorHeightPx) anchorHeightPx = it.height })
RowB(
    Modifier.decorateMeasureConstraints { incoming ->
        if (anchorHeightPx > 0) incoming.copy(minHeight = anchorHeightPx, maxHeight = anchorHeightPx)
        else incoming
    },
)
```

`decorateMeasureConstraints` — небольшой помощник layout; см. [`compose-modifier-and-layout-style`](../compose-modifier-and-layout-style/SKILL.md). Пока высота неизвестна, соседи используют фиксированный запасной размер в композиции; после измерения меняется только layout, без цепи новых композиций.

## 1. Предпочитай блочные формы модификаторов

У ряда модификаторов есть форма со значением и форма с блоком. Первая принимает значение, уже прочитанное в композиции; вторая может читать его в layout или draw.

```kotlin
// До: делегат `by` читает значение анимации в композиции
@Composable
fun SelectionPill(selectedIndex: Int) {
    val offsetX by animateDpAsState(120.dp * selectedIndex)
    Box(Modifier.offset(x = offsetX))
}

// После: храним State, читаем значение внутри блока offset фазы layout
@Composable
fun SelectionPill(selectedIndex: Int) {
    val offsetX = animateDpAsState(120.dp * selectedIndex)
    Box(
        Modifier.offset {
            IntOffset(offsetX.value.roundToPx(), 0)
        },
    )
}
```

Частые замены:

| Чтение в композиции | Отложенное чтение |
|---|---|
| `Modifier.offset(x = animatedX)` | `Modifier.offset { IntOffset(animatedX.value.roundToPx(), 0) }` |
| `Modifier.graphicsLayer(translationY = y)` | `Modifier.graphicsLayer { translationY = yProvider() }` |
| `val radius by animateFloatAsState(...); drawBehind { drawCircle(radius = radius) }` | `val radius = animateFloatAsState(...); drawBehind { drawCircle(radius = radius.value) }` |

Блок `drawBehind` уже относится к фазе draw. Важно, чтобы чтение `State.value` происходило **внутри** него.

## 2. Передавай поставщика значения через границу composable

Если быстро меняющееся значение пересекает границу composable, передай лямбду, выдающую его, а не готовый снимок:

```kotlin
// До: HomeScreen читает смещение в композиции и передаёт число ниже
@Composable
fun HomeScreen() {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        item { HeroImage(scrollOffset = listState.firstVisibleItemScrollOffset) }
    }
}

@Composable
fun HeroImage(scrollOffset: Int, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "...",
        modifier = modifier.graphicsLayer(translationY = -scrollOffset / 2f),
    )
}

// После: чтение происходит только внутри graphicsLayer
@Composable
fun HomeScreen() {
    val listState = rememberLazyListState()
    LazyColumn(state = listState) {
        item {
            HeroImage(
                scrollOffsetProvider = {
                    if (listState.firstVisibleItemIndex == 0) {
                        listState.firstVisibleItemScrollOffset
                    } else {
                        0
                    }
                },
            )
        }
    }
}

@Composable
fun HeroImage(scrollOffsetProvider: () -> Int, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "...",
        modifier = modifier.graphicsLayer {
            translationY = -scrollOffsetProvider() / 2f
        },
    )
}
```

Если суффикс `Provider` помогает показать контракт отложенного чтения, добавляй его к имени параметра.

## 3. Другие места чтения в layout/draw

Чтение состояния можно отложить внутрь:

- `Modifier.layout { measurable, constraints -> ... }`;
- своего `Alignment.align(...)`;
- `drawWithContent`, `drawBehind` и других модификаторов draw;
- блочных модификаторов layout/layer, таких как `graphicsLayer { ... }` и `offset { ... }`.

Используй их, когда состояние меняет **место** или **вид** элемента. Если состояние решает, **какие composable существуют**, читать его нужно в композиции.

## Краткая памятка

| Симптом | Причина | Исправление |
|---|---|---|
| `val x by animateFloatAsState(...)`, затем `Modifier.offset(...)` | `by` читает значение в композиции | Храни `State<Float>` и читай `.value` в `offset {}` |
| `Modifier.graphicsLayer(translationY = animatedY)` | Форма с аргументом использует значение композиции | Применяй `graphicsLayer { translationY = ... }` |
| `Child(scrollOffset = listState.firstVisibleItemScrollOffset)` | Быстрое значение пересекает границу composable | `Child(scrollOffsetProvider = { ... })` |
| Блок draw всё равно вызывает рекомпозицию каждый кадр | Значение прочитано до блока | Перенеси чтение `State.value` внутрь draw |
| Состояние выбирает ветку UI | Это решение композиции | Оставь чтение в композиции |
| `mergedMap.putAll(overlay)` в теле composable | Обратная запись composition → composition | `remember(parent, overlay) { parent + overlay }` |
| `Modifier.height(measuredPx.toDp())` у соседней строки | Обратная запись layout → composition | Меняй ограничения в фазе measure |
| Кеш тождества для слияния только для чтения | Риск устаревшего overlay | `remember(keys)` с неизменяемым результатом |

## Когда не применять

- Состояние выбирает, какие composable создать.
- Анимация разовая, дешёвая, и ясность кода важнее.
- В тестах проще напрямую проверять значения.
- Измерения показывают, что рекомпозиция не замедляет работу.

## Связанные навыки

- [`compose-state-authoring`](../compose-state-authoring/SKILL.md) — когда менять `mutableState*` в композиции, а когда из callback.
- [`compose-state-holder-ui-split`](../compose-state-holder-ui-split/SKILL.md) — граница state holder и простого UI при передаче лямбд.
- [`compose-stability-diagnostics`](../compose-stability-diagnostics/SKILL.md) — стабильность параметров и отчёты компилятора.
- [`compose-modifier-and-layout-style`](../compose-modifier-and-layout-style/SKILL.md) — помощник для правки ограничений в фазе measure.
