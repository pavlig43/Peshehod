---
name: compose-modifier-and-layout-style
description: Используй при написании или проверке layout API Jetpack Compose, параметров modifier, цепочек модификаторов, жёстко заданных решений о корневом layout и обёрток вокруг одного условия.
---

# Стиль modifier и layout в Compose

## Главный принцип

Composable, создающий layout, — элемент, который размещает *родитель*. Родитель решает, где он стоит, каковы его размер, выравнивание и отступы. Задача самого composable — структура внутри, а не место на экране. Отсюда три правила:

- **Объяви параметр `modifier` и примени его к корню**, чтобы родитель мог управлять размещением. Жёсткий `.fillMaxWidth()` у корня отнимает выбор у всех будущих вызывающих сторон.
- **Строй цепочку modifier одним выражением**, а не последовательными присваиваниями. Результат компиляции одинаков, но цепочка показывает замысел сразу.
- **Условный вывод размещай там, где действует условие.** Если layout содержит только один `if`, вынеси `if` наружу.

Эти правила часто встречаются вместе: объявляешь параметры компонента (1), вызывающий код строит цепочку для его места (2), внутри есть условие, которое хочется обернуть layout (3).

## Когда применять

- Пишешь `@Composable fun`, вызывающую `Box`, `Column`, `Row`, `LazyColumn`, `Text`, `Image`, `Surface`, `Card`, `Layout { … }` или другой layout, но в сигнатуре нет `modifier`; либо он не применён к корню; либо у корня жёстко задан `.fillMaxWidth()` / `.padding(...)`.
- Видишь `var m = Modifier`, затем `m = m.padding(…)`, `m = m.background(…)` и т. п.
- Аргумент `modifier = …` содержит три или более вызова цепочки в одной строке.
- Тело composable имеет вид `Layout { if (cond) Content() }` — одно условие и больше ничего.

## 1. Объяви параметр `modifier`

Для composable, создающего layout, ставь `modifier` после обязательных параметров и до лямбд контента, со значением `Modifier` по умолчанию. Имя должно быть ровно `modifier`, а не `mod`, `m` или `wrapperModifier`.

```kotlin
// ❌ ПЛОХО — нет modifier: вызывающий код не задаст место и размер
@Composable
fun HomeScreenHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}
```

```kotlin
// ✅ ХОРОШО — родитель задаёт ширину и отступы, composable описывает структуру
@Composable
fun HomeScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}
```

Теперь вызывающий код один раз пишет `HomeScreenHeader(title, subtitle, Modifier.fillMaxWidth().padding(horizontal = 16.dp))` на домашнем экране — там, где известны нужные размеры.

## 2. Применяй modifier вызывающей стороны к корню и первым

Если корневой layout уже имеет аргументы выравнивания или внутренние отступы, переданный `modifier` всё равно идёт в параметр `modifier` корня. Локальная цепочка компонента добавляется после него.

```kotlin
// ❌ ПЛОХО — параметр принят, но не применён
@Composable
fun Avatar(url: String, modifier: Modifier = Modifier) {
    Image(painter = rememberAsyncImagePainter(url), contentDescription = null)
}

// ❌ ПЛОХО — применён к дочернему Image, а не корню
@Composable
fun Avatar(url: String, modifier: Modifier = Modifier) {
    Box {
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = null,
            modifier = modifier,
        )
    }
}

// ❌ ПЛОХО — modifier вызывающего кода последним, собственный размер побеждает
@Composable
fun Avatar(url: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        modifier = Modifier
            .clip(CircleShape)
            .size(48.dp)
            .then(modifier),
    )
}
```

```kotlin
// ✅ ХОРОШО — сначала modifier вызывающего кода, затем внутренняя цепочка
@Composable
fun Avatar(url: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        modifier = modifier
            .clip(CircleShape)
            .size(48.dp),
    )
}
```

Порядок важен: ранняя часть цепочки образует внешнюю обёртку. Modifier вызывающего кода должен быть снаружи, чтобы его `.size(...)` и `.padding(...)` могли менять параметры по умолчанию, а не быть перекрыты ими.

## 3. Не закрепляй решения о layout на корне

Если у корня жёстко задан `.fillMaxWidth()`, `.padding(horizontal = 16.dp)`, `.height(56.dp)` и т. п., вызывающий код не может от них отказаться. Это выбор родителя.

```kotlin
// ❌ ПЛОХО — все вызовы занимают всю ширину
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) { Text(text) }
}

// ✅ ХОРОШО — вызывающий код добавит .fillMaxWidth(), если нужно
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) { Text(text) }
}
```

Исключение — модификаторы, составляющие **суть** компонента: аватар остаётся аватаром благодаря `.clip(CircleShape)` и размеру по умолчанию `.size(48.dp)`. Спроси себя: нужен ли кому-либо такой компонент *без* модификатора? Если да, перенеси его наружу. Если нет, оставь после modifier вызывающего кода в цепочке (см. §2).

## 4. Строй цепочку одним выражением

При рекомпозиции тело composable исполняется снова, в том числе выражения modifier. Пошаговое присваивание `var modifier =` ломает ход чтения, подталкивает к дальнейшим изменениям и не даёт ничего сверх цепочки.

```kotlin
// ❌ ПЛОХО — цепочка разбита на присваивания
@Composable
fun Demo() {
    var m = Modifier
    m = m.padding(16.dp)
    m = m.fillMaxSize()
    Box(m) { }
}

// ❌ ТОЖЕ ПЛОХО — тот же вид с .then()
@Composable
fun Demo() {
    var m = Modifier
    m = m.padding(16.dp)
    m = m.then(Modifier.fillMaxSize())
    Box(m) { }
}
```

```kotlin
// ✅ ХОРОШО
@Composable
fun Demo() {
    val m = Modifier
        .padding(16.dp)
        .fillMaxSize()
    Box(m) { }
}
```

Бери `val`, а не `var`: готовую цепочку не нужно перепривязывать. `var` кажется нужным лишь из-за формы с присваиваниями.

### Короткую цепочку можно оставить в месте вызова

Для одного или двух вызовов строй modifier прямо в аргументе. Выносить в `val` стоит длинную или повторяющуюся цепочку.

```kotlin
// ✅ ХОРОШО — короткие цепочки в месте вызова
Box(modifier = Modifier.fillMaxWidth()) { … }
Box(modifier = Modifier.padding(8.dp).background(Color.Red)) { … }
```

### Условная часть остаётся в цепочке

Условие часто кажется поводом взять `var`. Вместо этого вставь условие в выражение:

```kotlin
// ✅ ХОРОШО — условная часть внутри единой цепочки
Box(
    modifier = Modifier
        .fillMaxWidth()
        .then(if (selected) Modifier.background(Color.Red) else Modifier),
)
```

Пустой `Modifier` — нейтральный элемент `.then`: одна ветка может ничего не добавлять, а цепочка останется целой.

## 5. Переносы строк в месте вызова

Если в аргументе `modifier` цепочка содержит **три или более** вызова, пиши каждый с новой строки, выровняв точки под значением.

```kotlin
// ❌ ПЛОХО — три вызова в одной строке
Box(
    modifier = modifier.fillMaxSize().padding(16.dp).weight(1f),
)

// ✅ ХОРОШО
Box(
    modifier = modifier
        .fillMaxSize()
        .padding(16.dp)
        .weight(1f),
)
```

Один или два вызова оставляй в строке. Порог — число вызовов, не длина строки. Если у одного вызова длинные аргументы, это другая задача: вынеси `val` или сократи их.

Правило относится **только** к параметру с именем `modifier`, не к любому цепочечному аргументу.

## 6. Выноси одно условие наружу layout

Если единственное содержимое layout — один `if`, контейнер существует лишь ради условия. Вынеси `if` наружу: layout будет создан только тогда, когда есть что показать.

```kotlin
// ❌ ПЛОХО — Column существует всегда, контент внутри условный
@Composable
fun A() {
    Column {
        if (showHeader) {
            Text("Title")
            Text("Subtitle")
        }
    }
}

// ✅ ХОРОШО — Column создаётся только вместе с контентом
@Composable
fun A() {
    if (showHeader) {
        Column {
            Text("Title")
            Text("Subtitle")
        }
    }
}
```

Польза здесь в ясности, не в скорости: среда справится с обоими случаями. Во второй форме сразу видно условную секцию заголовка; в первой — постоянно существующий Column, который может оказаться пустым.

### Исключения и причины

- **У layout есть собственный видимый смысл.** Аргументы `modifier`, `contentAlignment`, `horizontalArrangement` или `verticalAlignment` описывают контейнер. Вынос `if` уберёт его при пустом содержимом или заставит повторять аргументы в ветках. Оставь как есть.

  ```kotlin
  // ✅ ОСТАВИТЬ — modifier контейнера выполняет видимую работу
  @Composable
  fun A(modifier: Modifier = Modifier) {
      Box(modifier = modifier) {
          if (something) {
              Text("Bleh1")
              Text("Bleh2")
          }
      }
  }
  ```

- **Рядом с `if` есть другие элементы.** У layout есть иной контент; вынос изменит структуру.
- **Обе ветки `if … else …` создают composable.** Контейнер нужен обеим веткам.

  ```kotlin
  // ✅ ОСТАВИТЬ — обе ветки наполняют layout
  Box {
      if (something) Text("Hint") else innerTextField()
  }
  ```

## 7. Правка ограничений в фазе measure

Если composable A сохраняет размер, а B должен ему соответствовать, **не читай размер в теле B** (`Modifier.height(state.dp)`). Иначе смена измеренного размера запустит композицию B.

Сохраняй размер из layout callback у A, а у B применяй в `Modifier.layout`: тогда меняется лишь layout.

```kotlin
fun Modifier.decorateMeasureConstraints(
    decorate: (Constraints) -> Constraints,
): Modifier = layout { measurable, incoming ->
    val constraints = decorate(incoming).constrain(incoming)
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(0, 0)
    }
}
```

```kotlin
// Поднято к общему родителю строк:
//   var anchorHeightPx by remember { mutableIntStateOf(0) }

// Строка для измерения — запись только из onSizeChanged
RowAnchor(Modifier.onSizeChanged { size -> if (size.height != anchorHeightPx) anchorHeightPx = size.height })

// Соседи читают anchorHeightPx только внутри layout
RowSibling(
    Modifier.decorateMeasureConstraints { incoming ->
        if (anchorHeightPx > 0) {
            // Ограничь входными границами, чтобы не превысить максимум родителя.
            incoming.copy(minHeight = anchorHeightPx, maxHeight = anchorHeightPx)
        } else {
            incoming
        }
    },
)
```

Запасную фиксированную высоту в композиции применяй лишь пока `anchorHeightPx` равен `0`. Полный шаблон для соседних строк — в [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md).

## Краткая памятка

| Симптом | Причина | Исправление |
|---|---|---|
| `@Composable fun Foo(text: String)` с `Column`/`Box`/`Text` в теле | Нет `modifier` | Добавь `modifier: Modifier = Modifier`, передай корню |
| `modifier` объявлен, но не используется | Параметр игнорируется | Примени к корневому layout |
| `modifier` передан ребёнку, а не корню | Неверная цель | Перенеси к самому внешнему layout |
| `modifier = Modifier.x().y().then(modifier)` | Modifier вызывающего кода последний | Начинай с него: `modifier = modifier.x().y()` |
| `modifier.fillMaxWidth().padding(...)` у общего компонента | Layout задан жёстко | Убери вызовы и дай родителю добавить их |
| Соседние composable тоже без `modifier` | Ошибка распространяется | Исправь этот, по случаю — соседние |
| `mod` или `wrapperModifier` | Неверное имя | Назови ровно `modifier` |
| `var m = Modifier`, затем `m = m.xxx()` | Пошаговая сборка | Одна цепочка на `val` или в месте вызова |
| `var m = Modifier; m = m.then(Modifier.xxx())` | Та же форма через `.then` | Сведи `.then(Modifier.x())` к `.x()` |
| В цепочке есть условие | Возникает соблазн взять `var` | `.then(if (c) Modifier.x() else Modifier)` |
| `modifier.a().b().c()` в строке | Длинная цепочка без переносов | По вызову на строку с отступом |
| `Layout { if (cond) X() }` без другого контента | Лишняя обёртка | Перенеси `if` наружу |
| `Box(modifier = …) { if (cond) X() }` | У layout есть свой смысл | Оставь |
| `Box { if (cond) X() else Y() }` | Обе ветки дают контент | Оставь |
| Сосед lazy-строки читает `height(state)` из её измерения | Связь размера с композицией | Сохрани размер у первой, примени через `decorateMeasureConstraints` у соседей |

## Когда не применять

- **Composable без layout.** `@Composable fun computeColor(): Color` или функция доступа `@ReadOnlyComposable` не создаёт узлов. Параметр `modifier` не нужен; см. `compose-state-authoring`.
- **Функции `@Preview`.** У них нет вызывающего кода, который бы применял `modifier`.
- **Тестовые composable** в исходниках `*Test`, вызываемые лишь через `composeTestRule.setContent { … }`.
- **Внутренние примитивы layout, где `modifier` — первый обязательный параметр.** Это редкий случай кода уровня framework. Правило касается первого *необязательного* параметра.
- **Императивная сборка modifier из состояния анимации.** Промежуточные переменные могут улучшить читаемость. Цепочка не самоцель.
- **Slot API, хранящие modifier в data class или builder.** Правило цепочки касается построения в месте вызова.
- **Тестовые composable** с намеренно заданным ходом рекомпозиции. Не правь их лишь ради стиля.

Правила объявления (§1–§3) действуют и для внутреннего или пока одноразового composable. Отговорки «все вызовы известны» и «лишний параметр» как раз ведут к тому, что при втором вызове компонент оказывается негибким.

## Тревожные признаки при проверке

| Мысль | Что на деле |
|---|---|
| «Компонент только внутренний, modifier избыточен» | Параметр с умолчанием — стандарт Compose API. Отказ от него вводит особое правило. |
| «Вызывается в одном месте, значит размеры известны» | Сегодня место одно; затраты на параметр однократны, а поздняя правка затронет каждый вызов. |
| «У соседних composable тоже нет modifier, соблюду стиль» | Не распространяй ошибку. Исправь этот и по случаю соседние. |
| «Родителю здесь всегда нужна вся ширина» | Пусть родитель передаст `.fillMaxWidth()`. |
| «Добавлю, когда понадобится» | Следующий вызывающий код скорее обойдёт отсутствие параметра, чем исправит API. |
| «Компонент маленький, параметр — шум» | В вызовах без нужды он не появляется. |
| «Добавил modifier, но оставил `.fillMaxWidth()` у корня ради домашнего экрана» | Другой экран не сможет его убрать. Перенеси в вызов. |
| «Из-за условия нужен var» | Вставь условную часть через `.then(if (c) Modifier.x() else Modifier)`. |
| «Три вызова — слишком мало для нескольких строк» | Порог — именно три вызова. |
| «Column пустой, но оставлю ради симметрии» | Перенеси условие наружу, а Column оставь в нужной ветке. |
| «Поставлю if внутри, ведь layout уже существует» | Само постоянное существование пустого layout и есть проблема. |

## Связанные навыки

- [`compose-slot-api-pattern`](../compose-slot-api-pattern/SKILL.md) — вторая половина публичного API: слоты `@Composable () -> Unit` для меняющегося контента. Переиспользуемый компонент принимает и `modifier`, и слоты: вызывающий код задаёт место и содержимое.
- [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md) — обратная запись между фазами и отложенное чтение измерений.
