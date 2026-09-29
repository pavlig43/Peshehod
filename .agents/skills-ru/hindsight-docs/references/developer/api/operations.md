
# Операции

Часть задач по обслуживанию и загрузке Hindsight выполняет асинхронно, чтобы вызов API не ждал их завершения. У них общая очередь (`async_operations`) и общий набор обработчиков. Одни и те же адреса REST дают список, статус, отмену и повтор для всех видов задач.

Здесь описаны виды операций, условия их запуска, просмотр и управление.

{/* Import raw source files */}

> **💡 Что нужно заранее**
> 
Пройдите [«Быстрый старт»](./quickstart) и изучите [работу retain](./retain).
## Как работают операции

Если вызову API нужна фоновая работа, обработчик запроса пишет строку со `status=pending` в таблицу `async_operations` и сразу возвращает ответ. Обработчик задач (по умолчанию внутри процесса API либо как отдельная служба — см. [«Службы — обработчик задач»](../services#worker-service)) проверяет таблицу, берёт ожидающие строки, выполняет работу и ставит `completed` или `failed`.

По умолчанию всё работает внутри одного процесса: внешняя очередь и новые процессы не нужны. При росте нагрузки тот же код можно запустить в отдельных обработчиках.

### Жизненный цикл

| Статус | Значение |
|--------|---------|
| `pending` | Задача в очереди: её ещё не взяли либо расширение отложило её до будущего `next_retry_at`, например для снижения нагрузки. |
| `processing` | Обработчик взял задачу и выполняет её. |
| `completed` | Задача завершилась без ошибки. |
| `failed` | Возникла ошибка; причина лежит в `error_message`. Задачу можно вернуть в очередь через `POST /…/retry`. |
| `cancelled` | Операция отменена через `DELETE /…/operations/{id}` до взятия обработчиком. Отменить `processing` нельзя. |

Обработчик повторяет неудачную операцию до `HINDSIGHT_API_WORKER_MAX_RETRIES` раз, затем ставит `failed`. Ошибки с постоянной причиной (например, неверный размер вектора или нарушение целостности) не повторяются: повторный запуск не поможет.

## Виды операций

У каждой операции есть `operation_type` в базе и `task_type` в данных задачи. Обычно значения совпадают.

### `retain`

Создаётся вызовом `POST /v1/default/banks/{bank_id}/memories` с `async=true` либо пакетным `retain_batch`. Обработчик выполняет те же шаги, что и синхронный retain: извлечение фактов LLM, создание векторов, сопоставление сущностей и построение связей по времени и смыслу.

Асинхронный retain полезен при загрузке тысяч элементов, когда HTTP-вызов не должен ждать несколько минут. По `operation_id` из ответа можно проверить завершение.

#### Главная операция: `retain_batch`

Для больших запросов Hindsight сам делит данные на части и создаёт одну главную операцию `retain_batch`, которая следит за дочерними. Её статус обобщает их работу: `pending` до запуска хотя бы одной части, `processing` во время выполнения, `completed` после всех частей, `failed`, если хоть одна дала ошибку. Каждая часть — отдельная операция `retain`, связанная с главной. Так можно посмотреть ошибки по частям.

В списке по умолчанию видны и главная операция, и дочерние. Передайте `exclude_parents=true`, чтобы скрыть общие строки и оставить лишь отдельные задачи `retain`.

### `file_convert_retain`

Создаётся адресами загрузки файлов. Обработчик преобразует файл по его MIME-типу (PDF → текст, DOCX → текст и т. д.) и передаёт результат в retain. Такие ошибки по умолчанию **не повторяются**: повреждённый PDF или отсутствие OCR не исправятся при повторном запуске, поэтому операция сразу получает `failed`.

Парсер (`markitdown`, `iris` или `llama_parse`) выбирается для среды через `HINDSIGHT_API_FILE_PARSER`. Клиент может задать другой на один запрос. См. [«Настройка → обработка файлов»](../configuration#file-processing).

### `consolidation`

Создаёт **наблюдения** из новых воспоминаний world/experience. Что это и как они строятся, см. в разделе [«Наблюдения»](../observations).

Запускается сама:

- После каждого retain с новыми фактами world/experience, если в банке включены `enable_auto_consolidation` и `enable_observations`.
- После удаления, из-за которого прежние наблюдения стали неверными: источник исчез → выведенные наблюдения устарели → обобщение повторяется с оставшимися источниками.
- Вручную через `POST /v1/default/banks/{bank_id}/consolidate`. Параметр `observation_scopes` оставляет лишь воспоминания с нужным набором тегов.

**Без повторов внутри банка**: пока одна задача `consolidation` ожидает запуска, новый запрос вернёт её `operation_id`, а не добавит ещё одну. Когда она перейдёт в работу, следующий запрос займёт новое место в очереди.

### `refresh_mental_model`

У ментальной модели есть `source_query`, который задаёт круг обобщаемых воспоминаний. Обработчик снова выполняет запрос, собирает новую сводку и обновляет текст модели.

Запускается вручную через `POST /v1/default/banks/{bank_id}/mental-models/{id}/refresh` либо сама по расписанию, если для модели включено автообновление.

### `graph_maintenance`

Исправляет выведенные данные, которые устарели после удаления. Каждый вызов делает три прохода:

1. **Восстановление связей.** Разбирает `graph_maintenance_queue`: единицы памяти, чьи исходящие связи по времени или смыслу потеряли соседей. Если связей стало меньше предела (20 по времени, 50 по смыслу), Hindsight повторяет поиск, который делает retain, и добавляет недостающие. Без этого предел top-K при записи оставлял бы часть выживших единиц с нехваткой связей после каждого удаления, ухудшая recall с обходом графа.
2. **Удаление одиноких сущностей.** Убирает сущности банка, на которые больше нет ссылок `unit_entities`. Внешний ключ `ON DELETE CASCADE` у `entity_cooccurrences` затем удаляет строки совместных упоминаний с такой сущностью.
3. **Удаление устаревших совместных упоминаний.** Чистит строки `entity_cooccurrences`, у которых обе сущности ещё есть, но ни одна текущая `memory_unit` больше не связана с обеими. Когда-то упоминание было верным, но все единицы, где оно встречалось, уже удалены.

Повторные запросы для одного банка при постановке в очередь сливаются в один проход.

**Когда запускается:** при любом удалении `memory_units`: `DELETE /documents/{id}`, `DELETE /memories/{id}` и повторной записи прежнего `document_id` (путь обновления). После полной очистки банка (`delete_bank`) делать нечего: в банке не осталось данных.

### `webhook_delivery`

После некоторых операций, например после обобщения в банке с вебхуком, Hindsight ставит в очередь задачу `webhook_delivery`. Обработчик отправляет данные POST-запросом на заданный адрес и повторяет попытку при временной ошибке.

## Адреса API

Все пути ниже относятся к одному `bank_id`.

### Список операций

```bash
GET /v1/default/banks/{bank_id}/operations
```

Параметры запроса:

| Параметр | Описание |
|-------|-------------|
| `status` | Отбор по `pending`, `processing`, `completed`, `failed`, `cancelled`. |
| `type` | Отбор по `retain`, `file_convert_retain`, `consolidation`, `refresh_mental_model`, `graph_maintenance`, `webhook_delivery`. |
| `limit` | От 1 до 100, по умолчанию 20. |
| `offset` | Сдвиг при чтении страницами. |
| `exclude_parents` | Убрать из ответа главные операции пакетов (крупный `retain_batch` создаёт одну главную и N дочерних). |

### Python

```python
# Section 'operations-list' not found in api/operations.py
```

### Node.js

```javascript
// List recent operations for a bank (default: 20 most recent).
const { data: recent } = await sdk.listOperations({
    client: apiClient,
    path: { bank_id: 'my-bank' },
});
for (const op of recent.operations) {
    console.log(op.id, op.task_type, op.status);
}

// Filter by status and type.
const { data: pendingRecompute } = await sdk.listOperations({
    client: apiClient,
    path: { bank_id: 'my-bank' },
    query: { status: 'pending', type: 'graph_maintenance' },
});

// Hide retain_batch parent rows (show only individual child retain jobs).
const { data: flat } = await sdk.listOperations({
    client: apiClient,
    path: { bank_id: 'my-bank' },
    query: { exclude_parents: true },
});
```

### CLI

```bash
hindsight operation list my-bank
```

### Go

```go
# Section 'operations-list' not found in api/operations.go
```

`items_count` зависит от вида операции и больше нуля лишь для задач retain: это число элементов текста в запросе.

### Получить статус операции

### Python

```python
# Section 'operations-get' not found in api/operations.py
```

### Node.js

```javascript
const { data: status } = await sdk.getOperationStatus({
    client: apiClient,
    path: { bank_id: 'my-bank', operation_id: '550e8400-e29b-41d4-a716-446655440000' },
});
console.log(status.status, status.error_message);

// Include the submission payload (can be large for retain batches).
const { data: detailed } = await sdk.getOperationStatus({
    client: apiClient,
    path: { bank_id: 'my-bank', operation_id: '550e8400-e29b-41d4-a716-446655440000' },
    query: { include_payload: true },
});
```

### CLI

```bash
hindsight operation get my-bank "$OPERATION_ID"
```

### Go

```go
# Section 'operations-get' not found in api/operations.go
```

Параметры запроса:

| Параметр | Описание |
|-------|-------------|
| `include_payload` | Добавить к ответу исходные параметры задачи как `task_payload`. По умолчанию `false`; объём может быть большим. |

Поля ответа, на которые стоит обратить внимание:

| Поле | Описание |
|-------|-------------|
| `updated_at` | Время последней правки строки операции: взятие в работу, отметка о ходе или завершение. |
| `progress` | Последние данные о ходе долгой операции; `null`, если их нет (операция завершилась сразу или создана до появления функции). |
| `task_payload` | Исходные параметры задачи; есть лишь при `include_payload=true`. |

`progress` записывается при смене крупных этапов или пакетов (обобщение, пакетный retain). По нему видно, работает ли долгая задача: если `processed` растёт между проверками, она жива; если число и время `at` не меняются, она зависла. Структура:

| Поле | Описание |
|-------|-------------|
| `stage` | Последний крупный этап, например `processing_batch`. |
| `at` | Время записи в формате ISO 8601. |
| `processed` | Сколько частей работы уже сделано (подпакетов, воспоминаний), если известно. |
| `total` | Общее число частей работы, если известно. |
| `detail` | Счётчики по виду операции, например `observations_created`, `round`, `items_in_sub_batch`. |

### Отменить ожидающую операцию

Вернёт `409`, если операция уже имеет статус `processing`, `completed` или `failed`.

### Python

```python
# Section 'operations-cancel' not found in api/operations.py
```

### Node.js

```javascript
// Cancel a pending operation before a worker claims it.
// Returns 409 if the operation is already processing/completed/failed.
await sdk.cancelOperation({
    client: apiClient,
    path: { bank_id: 'my-bank', operation_id: '550e8400-e29b-41d4-a716-446655440000' },
});
```

### CLI

```bash
hindsight operation cancel my-bank "$OPERATION_ID"
```

### Go

```go
# Section 'operations-cancel' not found in api/operations.go
```

### Повторить неудачную операцию

Статус строки сбрасывается на `pending`, и обработчик берёт её снова. Если статус не `failed` и не `cancelled`, вернётся `409`.

### Python

```python
# Section 'operations-retry' not found in api/operations.py
```

### Node.js

```javascript
// Re-queue a failed (or cancelled) operation.
// Returns 409 if the operation isn't in failed/cancelled state.
await sdk.retryOperation({
    client: apiClient,
    path: { bank_id: 'my-bank', operation_id: '550e8400-e29b-41d4-a716-446655440000' },
});
```

### CLI

```bash
hindsight operation retry my-bank "$OPERATION_ID"
```

### Go

```go
# Section 'operations-retry' not found in api/operations.go
```

## Пример асинхронного retain

Отправьте пакет асинхронно и проверяйте статус до завершения:

### Python

```python
# Section 'operations-async-retain' not found in api/operations.py
```

### Node.js

```javascript
// Submit a large batch asynchronously — the call returns immediately with an
// operation_id you can poll.
const submission = await client.retainBatch('my-bank', [
    { content: 'Alice joined Google in 2023' },
    { content: 'Bob prefers Python over JavaScript' },
], { async: true });
const operationId = submission.operation_id;

while (true) {
    const { data: s } = await sdk.getOperationStatus({
        client: apiClient,
        path: { bank_id: 'my-bank', operation_id: operationId },
    });
    if (['completed', 'failed', 'cancelled'].includes(s.status)) {
        console.log(`finished: ${s.status}`);
        break;
    }
    await new Promise((r) => setTimeout(r, 2000));
}
```

### CLI

```bash
# Submit an async retain and capture the operation_id from the JSON response.
OPERATION_ID=$(
  hindsight memory retain my-bank "Alice joined Google in 2023" --async -o json \
    | jq -r '.operation_id'
)

# Poll until the worker finishes — completed/failed/cancelled are all terminal.
while true; do
  STATUS=$(hindsight operation get my-bank "$OPERATION_ID" -o json | jq -r '.status')
  if [ "$STATUS" = "completed" ] || [ "$STATUS" = "failed" ] || [ "$STATUS" = "cancelled" ]; then
    echo "finished: $STATUS"
    break
  fi
  sleep 2
done
```

### Go

```go
# Section 'operations-async-retain' not found in api/operations.go
```

## Настройка обработчиков

У каждого обработчика общий предел одновременных задач (`HINDSIGHT_API_WORKER_MAX_SLOTS`, по умолчанию 10) для всех видов операций. Настройки `HINDSIGHT_API_WORKER_<TYPE>_MAX_SLOTS` резервируют часть мест для отдельных видов; оставшиеся места общие. Полная таблица — в разделе [«Настройка → распределённые обработчики»](../configuration#distributed-workers).

Обычно настройки по умолчанию подходят. Резервируйте места, если один вид задач вытесняет другой, например долгий `file_convert_retain` мешает `graph_maintenance` при частых удалениях.

## Что дальше

- [**Документы**](./documents) — работа с источниками документов
- [**Банки памяти**](./memory-banks) — настройка банков
