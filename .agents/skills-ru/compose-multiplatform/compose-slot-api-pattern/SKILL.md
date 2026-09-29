---
name: compose-slot-api-pattern
description: Используй при проектировании или проверке переиспользуемого компонента Jetpack Compose, когда визуальные области меняются от вызова к вызову или накапливаются примитивные параметры контента и boolean-флаги формы.
---

# Compose: шаблон slot API

## Главный принцип

Переиспользуемый компонент Compose описывает структуру макета. Вызывающая сторона передаёт меняющийся визуальный контент через слоты.

## Проверка API

1. Убедись, что компонент переиспользуемый. Для действительно одноразового composable не добавляй лишнюю обвязку слотов.
2. Отметь области, которые меняются: заголовок, поясняющий текст, начало, конец, действия, основное тело.
3. Замени примитивный контент и флаги формы, которыми управляет вызывающая сторона, слотами.
4. Добавляй receiver scope только если слот создаётся внутри layout, чьи API области должны быть доступны вызывающему коду.
5. Отсутствующие необязательные области делай nullable (`null`), чтобы компонент мог убрать контейнер и отступы.
6. Повторяющийся контент и токены по умолчанию выноси в `XxxDefaults`.
7. Соблюдай правила параметра `modifier` из `compose-modifier-and-layout-style`.

## 1. Замени примитивный контент слотами `@Composable`

Для контента, которым управляет вызывающий код, предпочитай слот `@Composable () -> Unit`. Обязательный слот оставляй non-nullable и без значения по умолчанию. Необязательный делай nullable со значением `null`.

```kotlin
// ❌ ПЛОХО — примитивные параметры; слотом является только конец, всё остальное зафиксировано
@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
) { … }
```

```kotlin
// ✅ ХОРОШО — каждая визуальная область является слотом, строка описывает структуру, а не контент
@Composable
fun SettingsRow(
    headlineContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) { … }
```

Для обычного однострочного контента вызовы остаются короткими:

```kotlin
SettingsRow(
    headlineContent = { Text("Account") },
    leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
    trailingContent = { SettingsRowDefaults.Chevron() },
    onClick = { … },
)
```

Необычные случаи больше не требуют новых параметров:

```kotlin
SettingsRow(
    headlineContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Inbox")
            Spacer(Modifier.width(8.dp))
            Badge { Text("3") }
        }
    },
    onClick = { … },
)
```

### Имена слотов

- Для свободных слотов `@Composable () -> Unit` используй `xxxContent`: `headlineContent`, `supportingContent`, `trailingContent` — это соответствует Material 3.
- Используй существительное в единственном числе, если слот ограничен по смыслу и компонент уже задаёт контекст: `Scaffold(topBar = { … }, bottomBar = { … }, floatingActionButton = { … })`.
- Не смешивай `content` и другие `xxxContent` в одном компоненте — выбери один стиль.

## 2. Добавляй receiver scope, если слот находится внутри layout

Если слот выводится внутри `Row`, `Column` или `Box`, а вызывающему коду нужны их возможности (`Modifier.weight`, `BoxScope.matchParentSize`, выравнивание), объяви receiver-лямбду: `@Composable RowScope.() -> Unit`.

```kotlin
// ❌ ПЛОХО — actions рисуются в Row, но вызывающий код не может использовать RowScope.weight()
@Composable
fun MyTopBar(
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit = {},
)
```

```kotlin
// ✅ ХОРОШО — вызывающий код получает RowScope
@Composable
fun MyTopBar(
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
)
```

Именно поэтому `TopAppBar(actions = { IconButton(…); IconButton(…) })` работает внутри `RowScope`.

Не добавляй receiver ко всем слотам автоматически. Он должен соответствовать реальному родительскому layout: для `Box` используй `BoxScope`, для `Column` — `ColumnScope`, а если полезных scope API нет — не добавляй receiver.

## 3. Необязательные слоты — nullable и `null` по умолчанию

Для отсутствующей области предпочитай `(@Composable () -> Unit)? = null`, а не пустую лямбду:

```kotlin
// ❌ ПЛОХО — пустая лямбда означает «контента нет»
leadingContent: @Composable () -> Unit = {}

// ✅ ХОРОШО — null позволяет убрать место, отступы и контейнер
leadingContent: (@Composable () -> Unit)? = null
```

Компонент может проверить `leadingContent != null` и полностью пропустить контейнер, расстояние и padding. При пустом значении layout часто всё равно занимает место.

## 4. Значения по умолчанию храни в `XxxDefaults`

Если приходится писать, что в trailing-слоте обычно должна быть стрелка или что фон по умолчанию должен использовать `MaterialTheme.colorScheme.surface`, помести помощники рядом с компонентом в объект `XxxDefaults`:

```kotlin
object SettingsRowDefaults {
    @Composable
    fun Chevron() = Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
    )

    @Composable
    fun TrailingValue(text: String) = Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
```

Обычные вызовы остаются декларативными, а слот по-прежнему открыт для особых случаев:

```kotlin
SettingsRow(
    headlineContent = { Text("Notifications") },
    trailingContent = { SettingsRowDefaults.Chevron() },
    onClick = { … },
)
```

Так устроены `ButtonDefaults`, `TopAppBarDefaults` и другие компоненты Material 3: composable-значения по умолчанию должны находиться там, а не превращаться в новые параметры.

## Краткая памятка

| Симптом | Причина | Исправление |
|---------|---------|-------------|
| `title: String, subtitle: String?, leadingIcon: ImageVector?` в переиспользуемом компоненте | Примитивные параметры контента | Преобразовать в слоты `xxxContent` |
| Несколько boolean-флагов (`showChevron`, `showSwitch`) | Перечисление форм | Один nullable `trailingContent` |
| Параметр `mode: Mode.Sealed` с вариантами | Та же проблема с флагами | Использовать слот |
| `actions: @Composable () -> Unit = {}` внутри `Row` | Нет receiver scope | `actions: @Composable RowScope.() -> Unit = {}` |
| `slot: @Composable () -> Unit = {}` для необязательной области | Пустая лямбда | `slot: (@Composable () -> Unit)? = null` и ветвление |
| `defaultColor: Color = MaterialTheme.colorScheme.surface` | Значение встроено в параметр | Перенести в `XxxDefaults.color` |
| Часто повторяющийся trailing-контент | Нет помощника по умолчанию | Добавить `XxxDefaults.Chevron()` |

## Когда не применять

- **Одноразовые компоненты.** Если composable используется в одном месте и не планируется переиспользование, слоты усложнят чтение. Когда появится второй вызов, вынеси контент в слот.
- **Примитивы дизайн-системы с одинаковым видом.** `Heading2(text: String)` нужен именно для единого оформления. Если позже понадобится значок рядом, тогда добавь слот.
- **Семантические параметры, которыми владеет компонент.** Если компонент задаёт типографику, иконки, текст для доступности или единообразие продукта, примитив может быть нужным ограничением.
- **Параметры с действительно ограниченным типом.** `Switch(checked: Boolean, onCheckedChange: ...)` не нуждается в слоте индикатора.
- **Критичные по производительности горячие пути.** Слот создаёт лямбду. В глубоком слое элемента `LazyList` примитив иногда лучше.

## Тревожные признаки при проверке

| Мысль | Что происходит на самом деле |
|--------|------------------------------|
| «Заголовок всегда строка, слот — избыточен» | Сегодняшнее «всегда» ломается завтра: кому-то понадобится `Text + Badge`. Переход на слот позже изменит все вызовы. |
| «Лямбды тяжелее строк» | В обычном Compose UI это незаметно; `Button`, `ListItem`, `TopAppBar` и `Scaffold` сами используют слоты. |
| «Добавлю слот, если попросят» | Поздняя смена формы параметров затронет каждый вызов. |
| «Варианты лучше описать sealed-классом Trailing» | Перечень ограничен, слот не ограничен. Новый вариант снова потребует менять компонент. |
| «Начало всегда иконка, меняется только конец» | Позже понадобится аватар, флаг или цветная форма. Слот нужно предусмотреть и для начала. |
| «Сегодня только один вызов» | Возможно, это ещё не переиспользуемый компонент. Для настоящего одноразового компонента примитивы подходят. |

## Связанные навыки

- [`compose-modifier-and-layout-style`](../compose-modifier-and-layout-style/SKILL.md) — правило параметра modifier сопровождает slot API: переиспользуемый компонент принимает `modifier` и даёт вызывающему выбрать контент.
