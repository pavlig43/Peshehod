---
name: compose-side-effects
description: Применяйте при создании или проверке Jetpack Compose с LaunchedEffect, DisposableEffect, SideEffect, rememberCoroutineScope, rememberUpdatedState, snapshotFlow, сообщениями, навигацией, запросами фокуса, аналитикой и сбором событий Flow.
---

# Compose: побочные действия

## Главное правило

Тело composable-функции описывает интерфейс. Оно может выполняться снова, быть пропущено или заброшено. Работа, меняющая внешний мир, должна находиться в API эффекта, жизненный цикл которого совпадает с этой работой.

## Выбирайте самый малый эффект

| Задача | API |
|---|---|
| Передать состояние Compose в обычный код после каждой успешной рекомпозиции | `SideEffect` |
| Зарегистрировать и снять слушатель, обратный вызов, наблюдатель или ресурс | `DisposableEffect(keys...)` |
| Выполнить приостанавливаемую, отложенную или однократную работу по ключу | `LaunchedEffect(keys...)` |
| Запустить приостанавливаемую работу из обработчика события пользователя | `rememberCoroutineScope()` |
| Превратить чтение snapshot Compose в Flow внутри корутины | `snapshotFlow { ... }` внутри `LaunchedEffect` |

## Ключи эффекта

Ключи задают, при каких данных эффект нужно начать заново. При изменении любого ключа старый эффект отменяется или освобождается, затем запускается новый.

```kotlin
// ✅ Сбор заново начинается при смене userId.
LaunchedEffect(userId) {
    repository.events(userId).collect { event -> handle(event) }
}

// ❌ Unit скрывает меняющийся вход; сбор продолжит использовать первый userId.
LaunchedEffect(Unit) {
    repository.events(userId).collect { event -> handle(event) }
}
```

Берите стабильные ключи со смыслом:

- объект, за жизненным циклом которого следует эффект: `userId`, `screenId`, `lifecycleOwner`, `focusRequester`;
- не используйте широкие объекты (`state`, `viewModel`), если важна лишь одна их часть;
- не добавляйте меняющиеся лямбды, если действительно не хотите перезапускать эффект при каждой смене лямбды.

## Не захватывайте устаревшее значение

Для долгого эффекта, который не должен перезапускаться, но должен вызывать последний обратный вызов или использовать последнее значение, применяйте `rememberUpdatedState`.

```kotlin
@Composable
fun Timeout(onTimeout: () -> Unit) {
    val latestOnTimeout by rememberUpdatedState(onTimeout)

    LaunchedEffect(Unit) {
        delay(1_000)
        latestOnTimeout()
    }
}
```

Берите его, когда жизненный цикл означает «запустить один раз», но вызываемая лямбда должна оставаться свежей. Частые случаи:

- таймер или экран-заставка не должны запускаться заново при смене `onTimeout`, но должны вызвать последний обратный вызов;
- наблюдатель жизненного цикла должен остаться у того же владельца, но вызывать последние лямбды `onStart` и `onStop`;
- долгий сборщик должен сохранить свой жизненный цикл, но вызывать последний обработчик событий.

Не используйте `rememberUpdatedState`, чтобы не выбирать правильные ключи. Если изменение значения должно перезапускать работу, сделайте это значение ключом:

```kotlin
// НЕВЕРНО: смена userId должна перезапустить сбор, а не менять захваченное значение.
val latestUserId by rememberUpdatedState(userId)
LaunchedEffect(Unit) {
    repository.events(latestUserId).collect { event -> handle(event) }
}

// ВЕРНО: жизненный цикл сбора следует за userId.
LaunchedEffect(userId) {
    repository.events(userId).collect { event -> handle(event) }
}
```

### Значение `rememberUpdatedState` устаревает внутри `remember {}`

`rememberUpdatedState` возвращает объект `State`, чей `.value` обновляется при каждой рекомпозиции. Поведение «последнее значение» работает лишь при **отложенном чтении** — внутри тела эффекта или лямбды, вызываемой позже, — а не при раннем захвате значения.

В блоке `remember {}` лямбда создаёт значение один раз. Чтение делегата там сразу сохраняет текущий `.value`, и будущие обновления состояния до него не дойдут:

```kotlin
val latestChannelId by rememberUpdatedState(channelId)

// ❌ НЕВЕРНО: channelId читается один раз при выполнении лямбды remember;
// destination навсегда хранит начальное значение.
val destination = remember {
    Destination(channelId = latestChannelId)
}

// ✅ ВЕРНО: пропустите rememberUpdatedState и сделайте channelId ключом.
val destination = remember(channelId) {
    Destination(channelId = channelId)
}

// ✅ ТОЖЕ ВЕРНО: лямбда откладывает чтение до каждого вызова.
val destination = remember {
    Destination(channelId = { latestChannelId })
}
```

Та же ошибка возможна везде, где делегат `rememberUpdatedState` **читается сразу**, а не через отложенный вызов лямбды или тела эффекта: у `data class`, созданного в `remember`, у объекта, созданного один раз в блоке настройки `DisposableEffect`, и в любом выражении, вычисленном при создании.

Если изменение должно создавать объект заново, сделайте это значение ключом `remember` и полностью уберите `rememberUpdatedState`. Оставляйте `rememberUpdatedState` для значения, которое должно оставаться свежим в долгоживущей области — корутине эффекта или обработчике события — **без** перезапуска этой области.

`rememberUpdatedState` также не делает отображаемое состояние «не вызывающим рекомпозицию». Если интерфейс должен показать меняющееся значение, читайте обычный `State` при композиции или используйте отложенное чтение из [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md) для значений, меняющихся каждый кадр.

## Сбор Flow

Используйте `LaunchedEffect` для **потоков побочных действий и событий**: сообщений, навигации, аналитики, команд фокуса и других потоков, где каждое событие запускает императивную работу.

```kotlin
LaunchedEffect(events) {
    events.collect { event ->
        snackbarHostState.showSnackbar(event.message)
    }
}
```

Не собирайте состояние интерфейса императивно лишь для изменения местного состояния. Состояние собирайте рядом с его владельцем, а в composable-интерфейс передавайте обычные значения. Разделение владельца состояния и интерфейса, `collectAsStateWithLifecycle()` / `collectAsState()` и удобная для образцов связь описаны в [`compose-state-holder-ui-split`](../compose-state-holder-ui-split/SKILL.md). Не дублируйте эту архитектуру здесь.

На Android предпочитайте сбор с учётом жизненного цикла. На платформах без такого API используйте `collectAsState()`.

Для чтения состояния Compose используйте `snapshotFlow`:

```kotlin
LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .distinctUntilChanged()
        .collect { index -> analytics.visibleIndex(index) }
}
```

`snapshotFlow { ... }.map { ... }` без завершающего `collect` ничего не делает.

## События пользователя

Берите `rememberCoroutineScope()`, когда нажатие или жест запускает приостанавливаемую работу:

```kotlin
@Composable
fun SaveButton(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                snackbarHostState.showSnackbar("Сохранено")
            }
        },
    ) {
        Text("Сохранить")
    }
}
```

Не вводите флаг события лишь для запуска `LaunchedEffect`. Нажатие уже является событием.

## Регистрация и освобождение

Для парной настройки и снятия используйте `DisposableEffect`:

```kotlin
@Composable
fun ObserveLifecycle(owner: LifecycleOwner, observer: LifecycleObserver) {
    DisposableEffect(owner, observer) {
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
        }
    }
}
```

У каждого пути регистрации должен быть соответствующий путь очистки в `onDispose`.

## Частые ошибки

| Ошибка | Причина | Исправление |
|---|---|---|
| Сетевой запрос прямо в теле composable-функции | Побочная работа идёт во время композиции | Обычно перенесите её во ViewModel или владельца состояния; `LaunchedEffect` берите лишь для работы интерфейса по ключу |
| Свойство аналитики записывается из тела composable-функции | Побочная работа во время композиции | `SideEffect`, если публикация нужна после каждой успешной рекомпозиции |
| Просмотр или событие записывается из тела composable-функции | Побочная работа во время композиции | `LaunchedEffect(key)`, если действие нужно один раз для ключа |
| `LaunchedEffect(Unit)` захватывает меняющийся `id` | Нет ключа | Сделайте `id` ключом или используйте `rememberUpdatedState`, если перезапуск не нужен |
| `rememberUpdatedState(id)` оставляет `LaunchedEffect(Unit)` после смены `id` | Скрытая ошибка жизненного цикла | Сделайте `id` ключом эффекта |
| Долгий эффект вызывает старый обратный вызов после рекомпозиции | Устаревшее захваченное значение | Оберните обратный вызов в `rememberUpdatedState` и вызывайте обёртку внутри эффекта |
| Делегат `rememberUpdatedState` сразу читается в `remember {}` | Значение сохранено один раз и не обновляется | Сделайте значение ключом: `remember(id) { Destination(id = id) }` |
| `LaunchedEffect(state) { ... }` перезапускается слишком часто | Слишком широкий ключ | Используйте конкретное свойство |
| `LaunchedEffect(...) { nonSuspendSetter() }` | Выбран неверный эффект | Обычно нужен `SideEffect`; `LaunchedEffect` оставляйте для однократной или отложенной работы по ключу |
| Слушатель добавлен в `LaunchedEffect` без очистки | Нет освобождения | Используйте `DisposableEffect` |
| Для клика выставляют `shouldShowSnackbar = true` | Анти‑шаблон флага события | Используйте `rememberCoroutineScope()` в обработчике клика |
| Побочная работа запускается из чтения фокуса в теле composable-функции | Работа выполняется во время композиции | `LaunchedEffect(focused) { ... }` или `snapshotFlow` |
| `onSizeChanged { heightState = it.height }`, а сосед читает `heightState` при композиции | Запись из размещения обратно в композицию | Сосед должен использовать высоту во время измерения, а не `Modifier.height(state.dp)` при композиции |

## Фокус и измерение

**Фокус:** чтение фокуса в теле composable-функции для побочной работы — предзагрузки, аналитики или сообщения — выполняет эту работу во время композиции. Наблюдайте фокус в эффекте:

```kotlin
// ❌ НЕВЕРНО: работа выполняется во время композиции каждый раз,
// когда focused равен true, включая временные проходы фокуса.
@Composable
fun Preloader(interactionSource: MutableInteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    if (focused) {
        preloadImages()
    }
}

// ✅ ВЕРНО: побочная работа находится в эффекте с ключом.
@Composable
fun Preloader(interactionSource: MutableInteractionSource) {
    val focused by interactionSource.collectIsFocusedAsState()
    LaunchedEffect(focused) {
        if (focused) preloadImages()
    }
}
```

Используйте `snapshotFlow { … }` внутри `LaunchedEffect`, когда нужно читать несколько snapshot-значений или сгладить частые изменения без добавления каждого производного значения в ключ. Для фокуса телевизора и крестовины см. [`compose-focus-navigation`](../compose-focus-navigation/SKILL.md).

**Измерение:** `onSizeChanged` и `onGloballyPositioned` — допустимые **обратные вызовы**, но выполняются во время размещения. Запись snapshot-состояния там безопасна лишь если ни одна более ранняя фаза его не читает. Если сосед читает это состояние при композиции, размещение записывает обратно в композицию, и сосед будет пересобираться при каждом измерении. Применяйте размеры в `Modifier.layout`; см. [`compose-modifier-and-layout-style`](../compose-modifier-and-layout-style/SKILL.md), раздел 7, и [`compose-state-deferred-reads`](../compose-state-deferred-reads/SKILL.md).

## Тревожные признаки при проверке

- «Это выполняется один раз» — о коде в теле composable-функции.
- `LaunchedEffect(Unit)` в функции с меняющимися параметрами.
- Цепочка Flow в эффекте без завершающего сбора.
- Ключи эффекта выбраны лишь для подавления предупреждения, а не по жизненному циклу.
- Долгий эффект вызывает лямбду без ключа и без `rememberUpdatedState`.
- Делегат `rememberUpdatedState` сразу читается внутри `remember {}` или конструктора: значение захватывается один раз и больше не обновляется.
