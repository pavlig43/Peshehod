---
name: compose-state-hoisting
description: "Используй, когда нужно решить, где хранить состояние или логику элемента Jetpack Compose: локально через remember, в параметрах composable, в обычном классе-хранилище состояния или в экранном ViewModel/component."
---

# Подъём состояния в Compose

## Главный принцип

Поднимай состояние только настолько высоко, насколько этого требует логика. Простое состояние элемента оставляй локальным, общее состояние поднимай к ближайшему общему владельцу, UI-поведение, ставшее отдельным понятием, выноси в обычный state holder, а бизнес-логику и данные приложения храни в экранном состоянии.

## Как выбрать владельца

| Ситуация | Владелец |
|----------|----------|
| Один composable читает и меняет простое состояние | Оставь локально через `remember` / `rememberSaveable` |
| Соседним или родительским composable нужно читать или менять состояние | Подними состояние и события к их ближайшему общему предку |
| Связанное состояние элементов и UI-логика мешают читать, просматривать или тестировать composable | Вынеси в обычный state holder и запоминай его в композиции |
| Есть вызовы репозитория, сохранение, бизнес-правила или подготовка состояния экрана | Используй экранный state holder: например, `ViewModel` или component |

Состояние элемента UI включает раскрытие, видимость листа, позицию прокрутки, фокус, редактирование текста, выбор и состояние анимации или взаимодействия. Состояние экрана — это данные приложения, подготовленные для отображения.

Если состояние элемента UI служит входом для бизнес-логики, его тоже может понадобиться хранить на уровне экрана. Например, текст для запроса подсказок из репозитория должен находиться в том же state holder, который создаёт эти подсказки.

## Когда нужен обычный state holder

Выноси состояние в обычный класс, если выполняются несколько условий:

- несколько связанных значений `remember` координируются одними callback-функциями;
- для прокрутки, фокуса, текста, выбора или листа нужны именованные операции вроде `clear`, `submit`, `jumpToTop` или `openFilters`;
- вычисляемые флаги UI разбросаны по composable;
- дочерние composable получают механику, которой они не владеют по смыслу;
- для проверки одного поведения в preview или тесте нужно воспроизводить длинную последовательность деталей UI;
- вспомогательным функциям нужно много параметров состояния только ради читаемости composable.

Не выноси один boolean, одно текстовое поле или простое скрытие и показ. Дополнительная обвязка сама по себе не разделяет ответственность.

## Шаблон

Используй обычный класс для состояния и логики элемента UI, а функцию `remember...State` — для объектов, которыми владеет композиция:

```kotlin
@Stable
class ProductSearchState(
    query: String,
    private val listState: LazyListState,
    private val focusRequester: FocusRequester,
) {
    var query by mutableStateOf(query)
        private set

    var filtersOpen by mutableStateOf(false)
        private set

    val canClear: Boolean
        get() = query.isNotEmpty()

    fun updateQuery(value: String) {
        query = value
    }

    fun clear() {
        query = ""
        focusRequester.requestFocus()
    }

    suspend fun jumpToTop() {
        listState.animateScrollToItem(0)
    }
}

@Composable
fun rememberProductSearchState(
    initialQuery: String = "",
    listState: LazyListState = rememberLazyListState(),
    focusRequester: FocusRequester = remember { FocusRequester() },
): ProductSearchState {
    return remember(listState, focusRequester) {
        ProductSearchState(initialQuery, listState, focusRequester)
    }
}
```

Composable рисует UI из state holder и вызывает методы, описывающие намерение. Если родителю нужно координировать то же поведение, принимай state holder параметром с безопасным значением по умолчанию:

```kotlin
@Composable
fun ProductSearchPanel(
    state: ProductSearchState = rememberProductSearchState(),
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    SearchField(
        query = state.query,
        onQueryChange = state::updateQuery,
        onClear = state::clear,
    )

    JumpToTopButton(onClick = {
        scope.launch { state.jumpToTop() }
    })
}
```

## Владение композицией

Обычные state holder, созданные через `remember`, следуют жизненному циклу composable. Поэтому в них удобно хранить объекты Compose UI: `LazyListState`, `FocusRequester`, `PagerState`, `DrawerState` и `TextFieldState`.

Оставляй приостанавливаемые операции UI, которым нужны кадры, например анимацию прокрутки или шторки, в coroutine, принадлежащей композиции (`rememberCoroutineScope`, `LaunchedEffect` или другой scope композиции). Не переноси такие вызовы в `viewModelScope`.

## Сохранение состояния

Используй `rememberSaveable` или собственный `Saver` только для значений, которые должны пережить пересоздание Activity или процесса: строки запроса, идентификаторы выбранных фильтров или ключ текущей вкладки.

Не пытайся напрямую сохранять объекты времени выполнения: `LazyListState`, `FocusRequester`, coroutine scope или callback-функции. Сохраняй минимальные сериализуемые значения, из которых можно восстановить поведение.

## Частые ошибки

| Ошибка | Исправление |
|--------|-------------|
| Поднимать каждое локальное состояние «на всякий случай» | Поднимай к самому нижнему владельцу, который действительно читает или меняет его |
| Выносить один boolean в state holder | Оставляй простое приватное состояние локально |
| Помещать вызовы репозитория или правила продукта в Compose state holder | Переноси их в экранный state holder, например `ViewModel` или component |
| Оставлять локальными текст или выбор, если они формируют состояние экрана из репозитория | Переноси этот ввод в экранный state holder вместе с бизнес-логикой |
| Передавать state holder глубоко в несвязанные дочерние компоненты | Передавай обычные значения и callback-функции, если ребёнок не координирует поведение holder |
| Превращать holder в свалку всего экрана | Разделяй по связанному поведению: поиск, координация листа или управление списком |
| Вызывать suspend-анимации из `viewModelScope` | Используй coroutine композиции |

## Связанные навыки

- [`compose-state-authoring`](../compose-state-authoring/SKILL.md) — правильное локальное использование `remember` и изменяемого состояния;
- [`compose-state-holder-ui-split`](../compose-state-holder-ui-split/SKILL.md) — разделение связи экранного holder с UI и обычного UI на основе состояния;
- [`compose-side-effects`](../compose-side-effects/SKILL.md) — выбор API эффектов и границ coroutine композиции;
- [`compose-focus-navigation`](../compose-focus-navigation/SKILL.md) — состояние фокуса, requesters и управление с клавиатуры/D-pad.
