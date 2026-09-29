---
sidebar_position: 5
---

# Сервер MCP

В Hindsight есть встроенный сервер [Model Context Protocol (MCP)](https://modelcontextprotocol.io/), через который ИИ-помощники могут прямо сохранять и находить воспоминания.

## Доступ

Сервер MCP **включён по умолчанию** и доступен по `/mcp` на сервере API. У каждого банка памяти свой адрес MCP:

```
http://localhost:8888/mcp/{bank_id}/
```

Например, для банка `alice`:
```
http://localhost:8888/mcp/alice/
```

Чтобы выключить сервер MCP, задайте переменную среды:

```bash
export HINDSIGHT_API_MCP_ENABLED=false
```

## Проверка доступа

По умолчанию адрес MCP **открыт**: проверка доступа не нужна.

Чтобы включить её, настройте расширение для арендаторов с ключом API:

```bash
export HINDSIGHT_API_TENANT_EXTENSION=hindsight_api.extensions.builtin.tenant:ApiKeyTenantExtension
export HINDSIGHT_API_TENANT_API_KEY=your-secret-key
```

После этого передавайте ключ API в заголовке `Authorization`:

### Claude Code

```bash
claude mcp add --transport http hindsight http://localhost:8888/mcp \
  --header "Authorization: Bearer your-secret-key" \
  --header "X-Bank-Id: my-bank"
```

### Claude Desktop

Add to `~/.claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "hindsight": {
      "url": "http://localhost:8888/mcp",
      "headers": {
        "Authorization": "Bearer your-secret-key",
        "X-Bank-Id": "my-bank"
      }
    }
  }
}
```

### Прямой HTTP-запрос

```bash
curl -X POST http://localhost:8888/mcp \
  -H "Authorization: Bearer your-secret-key" \
  -H "X-Bank-Id: my-bank" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc": "2.0", "method": "tools/list", "id": 1}'
```

Если ключа нет или он неверен, запрос получит ответ `401 Unauthorized`.

## Выбор банка

Банк выбирается по такому приоритету:

1. **Путь URL** (высший приоритет): `http://localhost:8888/mcp/my-bank/`.
2. **Заголовок X-Bank-Id**: `--header "X-Bank-Id: my-bank"`.
3. **По умолчанию**: переменная среды `HINDSIGHT_MCP_BANK_ID` (её значение по умолчанию — `"default"`).

## Отдельный адрес для каждого банка

В отличие от MCP-серверов, где инструменту нужно явно передать ID, Hindsight даёт **отдельный адрес каждому банку**. `bank_id` входит в путь URL, поэтому инструменту не нужно указывать банк: он известен из подключения.

Такой подход:
- **Упрощает вызовы** — не нужно передавать `bank_id` каждый раз.
- **Разделяет данные** — каждое подключение MCP относится к одному банку.
- **Помогает работать с несколькими арендаторами** — подключайте пользователей к разным адресам.

## Два режима

Режим сервера MCP зависит от URL:

| Режим | URL | Инструменты | bank_id |
|------|-----|-------|---------|
| **Один банк** | `/mcp/{bank_id}/` | 27 инструментов: память, ментальные модели, указания, документы, операции, теги, управление банком | Известен из URL |
| **Несколько банков** | `/mcp/` | Все 30 инструментов, включая `list_banks`, `create_bank`, `get_bank_stats` | Явный параметр `bank_id` у каждого инструмента |

**Режим одного банка** (советуем) ограничивает все операции банком из URL. У инструментов нет параметра `bank_id`.

**Режим нескольких банков** открывает все инструменты с необязательным `bank_id` и добавляет управление банками (`list_banks`, `create_bank`, `get_bank_stats`).

---

## Доступные инструменты

### retain

Сохраняет сведения в долгой памяти.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `content` | string | Да | Факт или воспоминание для записи |
| `context` | string | Нет | Вид воспоминания (по умолчанию `general`) |
| `timestamp` | string | Нет | Время события в формате ISO 8601 |
| `tags` | list[string] | Нет | Теги для группировки и отбора воспоминания |
| `metadata` | object | Нет | Метаданные в виде ключей и значений, например `{"source": "slack"}` |
| `document_id` | string | Нет | Связать воспоминание с существующим документом |

**Пример:**
```json
{
  "name": "retain",
  "arguments": {
    "content": "User prefers Python over JavaScript for backend development",
    "context": "programming_preferences",
    "tags": ["user:alice", "preferences"]
  }
}
```

**Когда брать:**
- Пользователь сообщает факты о себе, предпочтения или интересы.
- Упоминаются важные события или вехи.
- Названы решения, мнения или цели.
- Обсуждаются работа или детали проекта.

---

### sync_retain

Сохраняет данные в долгой памяти и ждёт завершения. В отличие от асинхронного [`retain`](#retain), `sync_retain` не вернётся, пока память не будет записана и готова к recall. Это нужно, если сразу после записи идёт поиск.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `content` | string | Да | Факт или воспоминание для записи |
| `context` | string | Нет | Вид воспоминания (по умолчанию `general`) |
| `timestamp` | string | Нет | Время события в формате ISO 8601 |
| `tags` | list[string] | Нет | Теги для группировки и отбора воспоминания |
| `metadata` | object | Нет | Метаданные в виде ключей и значений, например `{"source": "slack"}` |
| `document_id` | string | Нет | Связать воспоминание с существующим документом |

**Пример:**
```json
{
  "name": "sync_retain",
  "arguments": {
    "content": "User prefers Python over JavaScript for backend development",
    "context": "programming_preferences",
    "tags": ["user:alice", "preferences"]
  }
}
```

**Когда брать:**
- Записанное воспоминание должно сразу находиться поиском.
- Следующий шаг работы зависит от готовности этой памяти.
- В остальных случаях берите асинхронный `retain`, чтобы не ждать записи.

---

### recall

Ищет воспоминания для ответа с учётом пользователя.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `query` | string | Да | Поисковый запрос обычным языком |
| `max_tokens` | integer | Нет | Предел токенов в ответе (по умолчанию 4096) |
| `budget` | string | Нет | Глубина поиска: `low`, `mid` или `high` (по умолчанию `high`) |
| `types` | list[string] | Нет | Отбор по виду факта: `world`, `experience`, `observation`. По умолчанию все |
| `tags` | list[string] | Нет | Отбор воспоминаний по тегам |
| `tags_match` | string | Нет | Режим сравнения тегов: `any` (по умолчанию) или `all` |
| `query_timestamp` | string | Нет | Время в формате ISO 8601: recall ищет так, будто вопрос задан тогда; от него считаются относительные даты и свежесть |
| `min_scores` | object | Нет | Нижние границы оценок по шагам, например `{"reranker": 0.5}`. Ключи: `semantic`/`keyword` (до сортировки), `reranker`/`final` (после неё). Условия включают границу и соединяются через «И». Без поля фильтра нет. Оценки сортировщика несопоставимы между запросами — настройте порог перед применением |

**Пример:**
```json
{
  "name": "recall",
  "arguments": {
    "query": "What are the user's programming language preferences?",
    "tags": ["preferences"],
    "budget": "high"
  }
}
```

**Когда брать:**
- В начале беседы, чтобы вспомнить нужный контекст.
- До совета пользователю.
- Когда пользователь спрашивает о том, что мог упоминать раньше.
- Чтобы беседы оставались связными.

---

### reflect

Создаёт обдуманный вывод на основе сохранённой памяти с учётом характера банка.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `query` | string | Да | Вопрос или тема для рассуждения |
| `context` | string | Нет | Зачем нужен этот вывод; контекст по желанию |
| `budget` | string | Нет | Бюджет поиска: `low`, `mid` или `high` (по умолчанию `low`) |
| `max_tokens` | integer | Нет | Предел токенов ответа (по умолчанию 4096) |
| `response_schema` | object | Нет | Схема JSON для структурного ответа. При её наличии ответ содержит `structured_output` |
| `tags` | list[string] | Нет | Отбор памяти по тегам до рассуждения |
| `tags_match` | string | Нет | Режим сравнения тегов: `any` (по умолчанию) или `all` |
| `include_trace` | boolean | Нет | Добавить `tool_trace` и `llm_trace` для поиска ошибок. По умолчанию `false`, чтобы ответ был меньше |

**Пример:**
```json
{
  "name": "reflect",
  "arguments": {
    "query": "Based on my past decisions, what architectural style do I prefer?",
    "budget": "mid",
    "tags": ["architecture"]
  }
}
```

**Когда брать:**
- Нужен вывод, а не лишь найденные факты.
- Вопрос вроде «Что мне делать?», а не «Что я сказал?».
- Нужно обобщить закономерности из нескольких воспоминаний.

---

### create_mental_model

Создаёт ментальную модель — документ, который обновляется вместе с памятью. Модели хранят заранее созданные выводы reflect и могут сами обновляться при новых воспоминаниях.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `name` | string | Да | Понятное человеку имя модели |
| `source_query` | string | Да | Запрос для создания и обновления модели |
| `mental_model_id` | string | Нет | Свой ID: строчные буквы, цифры и дефисы. Если не задан, создаётся сам |
| `tags` | list[string] | Нет | Теги для группировки и отбора моделей |
| `max_tokens` | integer | Нет | Предел токенов в модели (по умолчанию 2048) |
| `trigger_refresh_after_consolidation` | boolean | Нет | Обновлять модель после обобщения памяти (по умолчанию `false`) |

**Пример:**
```json
{
  "name": "create_mental_model",
  "arguments": {
    "name": "Team Directory",
    "source_query": "Who works here and what do they do?",
    "tags": ["team", "people"]
  }
}
```

Текст создаётся асинхронно. Ответ содержит `operation_id` для слежения за ходом.

---

### list_mental_models

Возвращает все ментальные модели банка; по желанию отбирает по тегам.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `tags` | list[string] | Нет | Отбор моделей по тегам |

---

### get_mental_model

Возвращает модель по ID вместе с полным текстом.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `mental_model_id` | string | Да | ID нужной модели |

---

### update_mental_model

Обновляет метаданные или настройки модели.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `mental_model_id` | string | Да | ID модели для правки |
| `name` | string | Нет | Новое имя |
| `source_query` | string | Нет | Новый исходный запрос |
| `tags` | list[string] | Нет | Новые теги |
| `max_tokens` | integer | Нет | Новый предел токенов |
| `trigger_refresh_after_consolidation` | boolean | Нет | Автообновление после обобщения. Задавайте лишь при смене этого параметра |

---

### delete_mental_model

Безвозвратно удалить ментальную модель.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `mental_model_id` | string | Да | ID удаляемой ментальной модели |

---

### refresh_mental_model

Заново сформировать содержимое ментальной модели на основе последних воспоминаний. Операция выполняется асинхронно.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `mental_model_id` | string | Да | ID обновляемой ментальной модели |

---

### clear_mental_model

Очистить содержимое ментальной модели, сохранив её определение. Затем вызовите `refresh_mental_model`, чтобы восстановить содержимое из последних воспоминаний.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `mental_model_id` | string | Да | ID очищаемой ментальной модели |

---

### list_banks (только в режиме нескольких банков)

Вывести все доступные банки памяти.

---

### create_bank (только в режиме нескольких банков)

Создать новый банк памяти или получить существующий.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `bank_id` | string | Да | ID нового банка |
| `name` | string | Нет | Понятное человеку имя банка |
| `mission` | string | Нет | Назначение, описывающее роль агента и его цель |

---

### list_directives

Вывести все директивы банка. Директивы задают правила обработки запросов и ответов системы памяти.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `tags` | list[string] | Нет | Фильтр директив по тегам |
| `active_only` | boolean | Нет | Возвращать только активные директивы (по умолчанию `true`) |

---

### create_directive

Создать директиву в банке.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `name` | string | Да | Понятное человеку имя директивы |
| `content` | string | Да | Содержание или инструкция директивы |
| `priority` | integer | Нет | Приоритет: чем выше значение, тем важнее директива |
| `is_active` | boolean | Нет | Активна ли директива (по умолчанию `true`) |
| `tags` | list[string] | Нет | Теги для организации директив |

---

### delete_directive

Удалить директиву по ID.

| Параметр | Тип | Обязательно | Описание |
|-----------|------|----------|-------------|
| `directive_id` | string | Да | ID удаляемой директивы |

---

### list_memories

Browse stored memories with optional filtering and pagination.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `type` | string | No | Filter by fact type: `world`, `experience`, or `observation` |
| `q` | string | No | Search query to filter memories |
| `limit` | integer | No | Maximum number of results (default: 100) |
| `offset` | integer | No | Number of results to skip for pagination (default: 0) |

---

### get_memory

Retrieve a specific memory by ID.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `memory_id` | string | Yes | The ID of the memory to retrieve |

---

### list_documents

List documents that have been ingested into the memory bank.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | No | Search query to filter documents |
| `limit` | integer | No | Maximum number of results (default: 100) |

---

### get_document

Retrieve a specific document by ID, including its metadata.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `document_id` | string | Yes | The ID of the document to retrieve |

---

### delete_document

Delete a document and all memories linked to it.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `document_id` | string | Yes | The ID of the document to delete |

---

### list_operations

List async operations (retain processing, mental model refresh, etc.) with optional status filtering.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Filter by status: `pending`, `running`, `completed`, `failed`, `cancelled` |
| `limit` | integer | No | Maximum number of results (default: 100) |

---

### get_operation

Get the status and details of an async operation.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `operation_id` | string | Yes | The ID of the operation to check |

---

### cancel_operation

Cancel a pending or running async operation.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `operation_id` | string | Yes | The ID of the operation to cancel |

---

### list_tags

List all unique tags used in a bank, optionally filtered by pattern.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | No | Glob pattern to filter tags (e.g., `project:*`) |
| `limit` | integer | No | Maximum number of results (default: 100) |

---

### get_bank

Get information about a memory bank, including its name, mission, and disposition.

---

### get_bank_stats (multi-bank mode only)

Get statistics for a memory bank (node/link counts).

---

### update_bank

Update a memory bank's configuration. Updates the bank's name and/or any bank-level configuration fields — only provided fields are updated; omitted fields remain unchanged.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `name` | string | No | Human-friendly display name for the bank |
| `mission` | string | No | **Deprecated** — alias for `config_updates.reflect_mission` |
| `config_updates` | object | No | Dictionary of configuration fields to update. Supports all bank-configurable fields (see below). Non-configurable or credential fields are rejected |

The `config_updates` object accepts any bank-configurable field by its Python field name, including:

- `reflect_mission` — mission/context for Reflect operations
- `retain_mission` — steers what gets extracted during `retain()`
- `retain_extraction_mode` — `concise` (default), `verbose`, or `custom`
- `retain_custom_instructions` — custom extraction prompt (active when mode is `custom`)
- `retain_chunk_size` — target maximum characters for each content chunk
- `retain_structured_chunk_size` — maximum characters for a single JSONL line or conversation turn to keep whole
- `retain_chunk_batch_size` — number of chunks to process in parallel
- `enable_observations` — toggle observation consolidation after `retain()`
- `observations_mission` — controls observation synthesis rules
- `disposition_skepticism` — critical evaluation level (1–5)
- `disposition_literalism` — literal vs. abstract interpretation (1–5)
- `disposition_empathy` — emotional context consideration (1–5)
- `entity_labels` — controlled vocabulary for entity classification
- `entities_allow_free_form` — allow labels outside `entity_labels`
- `recall_include_chunks` — include raw chunks in recall results
- `recall_max_tokens` — max tokens for recall results
- `mcp_enabled_tools` — tool allowlist for this bank

---

### delete_bank

Permanently delete a memory bank and all its data (memories, documents, entities, mental models).

---

### clear_memories

Clear all memories from a bank without deleting the bank itself. Optionally filter by fact type to only clear specific kinds of memories.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `type` | string | No | Fact type to clear: `world`, `experience`, or `observation`. If not specified, clears all |

---

## Integration with AI Assistants

The MCP server can be used with any MCP-compatible AI assistant. See the [Authentication](#authentication) section above for Claude Code and Claude Desktop configuration examples.

Each user can have their own configuration pointing to their personal memory bank using either:
- A bank-specific URL path like `/mcp/alice/` (recommended)
- The `X-Bank-Id` header
