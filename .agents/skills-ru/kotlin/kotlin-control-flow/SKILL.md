---
name: kotlin-control-flow
description: "Применяйте при создании или проверке ветвления в Kotlin: выражений when, условий-охранников, полного разбора sealed-типов, умных приведений, веток для null, раннего выхода и замены сложных цепочек if/else."
---

# Ветвление в Kotlin

## Назначение

Используйте навык для создания или проверки веток кода Kotlin. Это порядок переделки кода, а не вкусовое правило оформления.

Цель: сразу видно, какое значение разбирается; дополнительные условия стоят у своей ветки; умные приведения остаются доступны; компилятор доказывает полноту разбора закрытого набора вариантов.

## Порядок работы

Проверяйте по очереди.

### 1. Найдите разбираемое значение

Найдите значение, которое код делит на варианты. Если каждая ветка проверяет его, сделайте это значение аргументом `when`.

```kotlin
// Вместо повторных проверок state используйте when с аргументом.
val action = when (state) {
    State.SignedOut -> Action.ShowSignIn
    is State.SignedIn -> Action.ShowHome(state.user)
}
```

Если одного общего значения нет, оставьте `when` без аргумента или цепочку `if`.

### 2. Выберите вид ветвления

До правки сверьтесь с таблицей:

| В коде есть... | Возьмите... |
|---|---|
| Одно разбираемое значение | `when (subject)` |
| Не связанные друг с другом логические условия | `when` без аргумента или `if`/`else` |
| Основное совпадение и ещё одно условие лишь для этой ветки | Условие-охранник |
| Неверный ввод до основного пути | Ранний `return`, `require` или `check` |
| Закрытый `enum`, `Boolean`, `sealed` или закрытый тип с `null`, из которого получают значение | Полное выражение `when` |
| Открытый внешний ввод или настоящий запасной путь | Явный `else` |

### 3. Перенесите местные условия в охранники

Если ветка сперва совпадает по типу или значению, а затем проверяет добавочное условие, примените охранник:

```kotlin
return when (event) {
    is Event.Message if event.isUnread -> Row.Highlighted(event.message)
    is Event.Message -> Row.Normal(event.message)
    Event.Empty -> Row.Empty
}
```

Охранник уместен, лишь когда выполнено всё:

- У `when` есть аргумент.
- У ветки есть основное условие: `is Type`, элемент `enum`, объект, значение, диапазон и т. п.
- Добавочное условие касается лишь этой ветки.
- Более поздняя ветка всё ещё обрабатывает то же основное условие либо полнота выражения обеспечена иначе.

Ветки с охранником ставьте до ветки без него для того же основного условия.

### 4. Сохраните полный разбор

Для выражения `when` над закрытым набором явно обработайте каждый вариант. Не добавляйте `else` лишь ради тишины компилятора.

```kotlin
val action = when (state) {
    SessionState.SignedOut -> Action.ShowSignIn
    is SessionState.SignedIn -> Action.ShowHome(state.user)
    is SessionState.Expired if state.canRefresh -> Action.Refresh
    is SessionState.Expired -> Action.ShowSignIn
}
```

Берите `else` для открытого набора: строк сервера, числовых кодов состояния, неизвестных значений платформы или сознательного запасного пути с записью в журнал.

### 5. Разделите ветки, где охранник не поддержан

Охранники нельзя ставить на ветку с условиями через запятую. Если добавочная проверка нужна лишь одному случаю, разделите ветку:

```kotlin
when (status) {
    Status.Pending if canRetry -> retry()
    Status.Pending -> showPending()
    Status.Queued -> showQueued()
}
```

### 6. Уберите неверные условия из основного пути

Берите ранний выход, если он убирает `null` или неверное состояние из основного пути:

```kotlin
fun render(user: User?): UiModel {
    user ?: return UiModel.SignedOut

    return UiModel.SignedIn(
        name = user.name,
        avatar = user.avatar,
    )
}
```

Не убирайте вложенность, если она показывает очистку ресурсов, границу транзакции или обработку ошибки.

### 7. Проверьте умные приведения

После правки убедитесь, что в каждой ветке нужный узкий тип всё ещё доступен. Если новая схема требует `as`, `!!`, временных изменяемых переменных или повторных приведений, оставьте прежнюю либо сделайте меньшую правку.

## Примеры замены

### Ветка внутри `when`

Когда вложенная ветка лишь уточняет один основной случай, превратите её в ветки с охранником:

```kotlin
// Было
return when (event) {
    is Event.Message -> {
        if (event.isUnread) Row.Highlighted(event.message) else Row.Normal(event.message)
    }
    Event.Empty -> Row.Empty
}

// Стало
return when (event) {
    is Event.Message if event.isUnread -> Row.Highlighted(event.message)
    is Event.Message -> Row.Normal(event.message)
    Event.Empty -> Row.Empty
}
```

### Повторные проверки одного значения

Если каждое условие разбирает одно значение, сделайте его аргументом:

```kotlin
// Было
return when {
    result is Result.Success -> Ui.Success(result.value)
    result is Result.Failure && result.canRetry -> Ui.Retry(result.error)
    result is Result.Failure -> Ui.Error(result.error)
    else -> Ui.Loading
}

// Стало
return when (result) {
    is Result.Success -> Ui.Success(result.value)
    is Result.Failure if result.canRetry -> Ui.Retry(result.error)
    is Result.Failure -> Ui.Error(result.error)
    Result.Loading -> Ui.Loading
}
```

### `null` как один из вариантов

Если `null` — лишь одна ветка большого разбора, берите `when (value)`:

```kotlin
return when (val selected = selection) {
    null -> SelectionUi.None
    is Selection.Single if selected.item.isArchived -> SelectionUi.Archived(selected.item)
    is Selection.Single -> SelectionUi.Active(selected.item)
    is Selection.Multiple -> SelectionUi.Count(selected.items.size)
}
```

## Проверка перед завершением

- У кода есть один явный аргумент для разбора либо осознанно нет общего аргумента.
- Ветка с охранником стоит до совпадающей ветки без него.
- У ветки с несколькими условиями через запятую нет охранника.
- Выражение `when` над закрытым набором полно без лишнего `else`.
- Для открытого набора запасной путь задан явно.
- Умные приведения работают без `as`, `!!` и повторных приведений.
- Новую схему проще читать, чем старую.

## Когда не применять

- Не вводите охранники, если версия Kotlin в проекте их не поддерживает.
- Не превращайте несвязанные логические проверки в неудобный `when` с аргументом.
- Не убирайте нужный `else` для открытого внешнего ввода.
- Не выпрямляйте код, если так хуже видны очистка, границы транзакции или обработка ошибки.

## См. также

- [`kotlin-flow-state-event-modeling`](../kotlin-flow-state-event-modeling/SKILL.md) — выбор средств для состояния и событий Flow.
- [`kotlin-multiplatform-expect-actual`](../kotlin-multiplatform-expect-actual/SKILL.md) — разделение общих правил и тонкого платформенного кода.
