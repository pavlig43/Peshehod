
# Воспоминания

**Единица памяти** — отдельный факт, который Hindsight извлекает и хранит. Здесь описаны адреса для работы с такими единицами: чтение и список, история изменений наблюдения, а также **ручная правка** (исправление, вывод из работы и возврат). Загрузка и поиск описаны отдельно в [Retain](./retain.mdx) и [Recall](./recall.mdx).

{/* Import raw source files */}

## Адреса API

| Метод | Адрес | Для чего |
|---|---|---|
| `GET` | `/v1/default/banks/{bank}/memories/list` | Получить и отобрать единицы памяти банка |
| `GET` | `/v1/default/banks/{bank}/memories/{id}` | Получить одну единицу памяти |
| `GET` | `/v1/default/banks/{bank}/memories/{id}/history` | Получить историю обновлений наблюдения |
| `PATCH` | `/v1/default/banks/{bank}/memories/{id}` | Исправить, вывести из работы или вернуть |
| `DELETE` | `/v1/default/banks/{bank}/memories/{id}/observations` | Сбросить наблюдения, выведенные из воспоминания |

## Список единиц памяти

Запрос возвращает единицы памяти банка. У каждой есть `fact_type` (`world` | `experience` | `observation`), `state` (`valid` | `invalidated`), сущности, даты событий, а у исправленных человеком фактов — время `edited_at`. Выведенные из работы записи **входят в ответ по умолчанию**, чтобы можно было проверить историю правок. Отбирайте их через `state=`.

### Python

```python
# Section 'list-memories' not found in api/memories.py
```

### Node.js

```javascript
// List memory units in a bank. Invalidated rows are included by default.
const memories = await client.listMemories(BANK_ID);
for (const unit of memories.items) {
    console.log(`- [${unit.fact_type}] ${unit.text}`);
}

// Filter to only the invalidated facts (e.g. to review duplicates).
const invalidated = await client.listMemories(BANK_ID, { state: 'invalidated' });
console.log(`${invalidated.items.length} invalidated fact(s)`);
```

### CLI

```bash
# List memory units in a bank (invalidated rows are included by default)
hindsight memory list "$BANK_ID"

# Filter to only the invalidated facts (e.g. to review duplicates)
curl -s "$HINDSIGHT_URL/v1/default/banks/$BANK_ID/memories/list?state=invalidated"
```

### Go

```go
# Section 'list-memories' not found in api/memories.go
```

## Получить одну единицу памяти

### Python

```python
# Section 'get-memory' not found in api/memories.py
```

### Node.js

```javascript
// Fetch a single memory unit (entities, dates, state).
const memory = await (
    await fetch(`${HINDSIGHT_URL}/v1/default/banks/${BANK_ID}/memories/${memoryId}`)
).json();
console.log(`Text: ${memory.text}`);
console.log(`Type: ${memory.type}  Entities: ${memory.entities}`);
```

### CLI

```bash
# Section 'get-memory' not found in api/memories.sh
```

### Go

```go
# Section 'get-memory' not found in api/memories.go
```

Для **выведенного наблюдения** адрес истории показывает, как оно менялось по мере прихода новых исходных фактов:

### Python

```python
# Section 'observation-history' not found in api/memories.py
```

### Node.js

```javascript
# Section 'observation-history' not found in api/memories.mjs
```

### CLI

```bash
# Section 'observation-history' not found in api/memories.sh
```

### Go

```go
# Section 'observation-history' not found in api/memories.go
```

## Ручная правка: исправление, вывод из работы и очистка

Память задумана как журнал с добавлением записей. Но факт может быть **ошибочным**, **устаревшим** или **повтором**. Ручная правка позволяет исправить или вывести отдельное воспоминание из работы, не теряя историю. Выведенные факты убираются из активного набора и не попадают в recall, но их можно вернуть.

### Что выбрать

Для разных причин ошибки нужны разные действия:

| Что случилось с воспоминанием | Действие | Почему |
|---|---|---|
| **Банк в целом извлекает факты неверно** (например, всё время путает лицо) | Исправьте `retain_mission` / `observations_mission`, затем **обработайте документ заново** | Общую ошибку лучше исправить у источника и повторить обработку. См. [Retain](./retain.mdx) и [«Наблюдения»](../observations.mdx). |
| **Разовая ошибка** (один неверно извлечённый факт) | **Исправьте** воспоминание | Факт и всё выведенное из него будут созданы заново. |
| **Факт уже неверен, а замены нет** (сервер выключили, инструмент исправили, роль сменилась) | **Выведите** воспоминание из работы | Система сама не узнает о перемене, поэтому вы сообщаете о ней явно. |
| **Повтор или факт, заменённый другим** | **Выведите** воспоминание из работы | Убирает лишнее из recall, но хранит историю. |
| **Факт заменён новым, который вы и так запишете** (например, «любит BMW» → «любит Toyota») | Просто сохраните новый факт | Обобщение само согласует противоречия при поступлении новых данных. |

Общее правило: **если Hindsight может узнать о перемене из новых фактов, доверьте её обобщению. Если знаете лишь вы — исправьте запись сами.**

Вручную можно править лишь исходные факты **world** и **experience**. Наблюдения *выведены* из них и создаются заново из источников, поэтому исправлять нужно факты. Вызов `PATCH` для наблюдения вернёт `400`.

### Исправить воспоминание

Исправьте ошибку LLM при извлечении. Можно сменить **текст**, **контекст**, **даты событий**, **вид факта** и **сущности** — всё, в чём могла ошибиться модель. Hindsight создаст новый вектор факта, уберёт наблюдения и связи старой версии, затем вновь обобщит данные. У исправленного факта появится время `edited_at` (в панели управления видна отметка **Edited**).

Ничего вручную перестраивать не нужно: после правки **граф знаний и связи пересчитываются сами** в фоне. Связи факта с сущностями находятся заново по новому тексту и именам; связи по времени и смыслу тоже строятся заново, затем вновь идёт обобщение. `PATCH` возвращается сразу после записи правки, а граф и наблюдения обновляются асинхронно.

### Python

```python
# Section 'edit-memory' not found in api/memories.py
```

### Node.js

```javascript
// Correct the fact's text. Re-embeds, drops derived observations/links,
// re-consolidates, and recomputes the graph automatically.
await patchMemory(memoryId, { text: 'The user visited Paris in 2023.', reason: 'wrong subject' });
```

### CLI

```bash
# Section 'edit-memory' not found in api/memories.sh
```

### Go

```go
# Section 'edit-memory' not found in api/memories.go
```

Так же можно исправить даты, вид факта и сущности. Для `context`, `occurred_start` и `occurred_end` пустая строка `""` очищает поле, а отсутствие поля оставляет его прежним. Для `entities` список **заменяет** все сущности факта (имена ищутся или создаются так же, как при retain), а `[]` убирает их все. Если поле пропустить, сущности не меняются.

### Python

```python
# Section 'edit-memory-fields' not found in api/memories.py
```

### Node.js

```javascript
// Correct dates, fact type, and entities in one call. "" clears a field;
// entities replaces the set ([] detaches all); omit to leave unchanged.
await patchMemory(memoryId, {
    occurred_start: '2023-06-01',
    fact_type: 'experience',
    entities: ['Alice', 'Paris'],
});
```

### CLI

```bash
# Section 'edit-memory-fields' not found in api/memories.sh
```

### Go

```go
# Section 'edit-memory-fields' not found in api/memories.go
```

### Вывести воспоминание из работы (можно вернуть)

Факт можно мягко вывести из работы. Такое воспоминание:

- **Исчезает из recall**, обобщения и графа знаний.
- **Теряет связи**; выведенные из него **наблюдения пересчитываются** без этого факта.
- **Остаётся в банке** для аудита и видно среди воспоминаний и документов.
- Может быть **возвращено** в любой момент.

### Python

```python
# Section 'invalidate-memory' not found in api/memories.py
```

### Node.js

```javascript
// Soft-retire a fact: removed from recall/consolidation/graph, links pruned,
// derived observations recomputed without it — but kept for audit.
await patchMemory(memoryId, { state: 'invalidated', reason: 'server decommissioned 2026-06-01' });
```

### CLI

```bash
# Section 'invalidate-memory' not found in api/memories.sh
```

### Go

```go
# Section 'invalidate-memory' not found in api/memories.go
```

Возврат снова делает факт активным и запускает обобщение:

### Python

```python
# Section 'restore-memory' not found in api/memories.py
```

### Node.js

```javascript
// Restore a previously invalidated fact.
await patchMemory(memoryId, { state: 'valid' });
```

### CLI

```bash
# Section 'restore-memory' not found in api/memories.sh
```

### Go

```go
# Section 'restore-memory' not found in api/memories.go
```

При выводе из работы запись **переносится** из активной таблицы `memory_units` в отдельный архив. Поэтому recall и обобщению не нужен фильтр «пропустить выведенные»: таких строк в активной таблице просто нет.

> **📝 Документы — первоисточник**
> 
Воспоминание извлечено из документа. Правка или вывод воспоминания из работы **не меняет исходный документ**: он остаётся точной записью прошлого. Поэтому **повторная обработка документа отменит ручные правки** его фактов: извлечение заново пойдёт из исходного текста. Общие ошибки исправляйте в задаче банка и запускайте обработку снова; разовые — через правку или вывод факта.
### Как очистить повторы

Чтобы убрать повторы, сгруппируйте их из `memories/list` и **выведите** лишние из работы. Recall сразу перестанет их находить, а история останется.
