
# Документы

Следите за источниками документов в банке памяти и управляйте ими. Документы помогают узнать, откуда взялось воспоминание.

{/* Import raw source files */}

> **💡 Что нужно заранее**
> 
Пройдите [«Быстрый старт»](./quickstart) и разберитесь, [как работает retain](./retain).
## Что такое документы?

Документы содержат текст, переданный в retain. Они позволяют:

- **Найти источник** — узнать, из какого PDF, беседы или файла взято воспоминание.
- **Обновить текст** — повторно сохранить документ и обновить его факты.
- **Удалить сразу много записей** — убрать все воспоминания документа одним действием.
- **Упорядочить память** — сгруппировать факты по источнику.

## Фрагменты

При retain Hindsight делит текст на фрагменты до извлечения фактов. Фрагменты хранятся рядом с извлечёнными воспоминаниями и сохраняют части исходного текста.

**Чем полезны фрагменты:**
- **Сохранение контекста** — в них есть исходный текст факта, если нужна точная формулировка.
- **Более полный recall** — вместе с найденным фактом можно получить соседний текст.

> **💡 Включайте фрагменты в Recall**
> 
Задайте `include_chunks=True` при recall, чтобы вместе с фактами получить исходные фрагменты. Подробности — в разделе [«Recall»](./recall).
## Retain с ID документа

Свяжите сохраняемый текст с документом:

### Python

```python
# Retain with document ID
client.retain(
    bank_id="my-bank",
    content="Alice presented the Q4 roadmap...",
    document_id="meeting-2024-03-15"
)

# Batch retain for a document with different sections
client.retain_batch(
    bank_id="my-bank",
    items=[
        {"content": "Item 1: Product launch delayed to Q2", "document_id": "meeting-2024-03-15-section-1"},
        {"content": "Item 2: New hiring targets announced", "document_id": "meeting-2024-03-15-section-2"},
        {"content": "Item 3: Budget approved for ML team", "document_id": "meeting-2024-03-15-section-3"}
    ]
)
```

### Node.js

```javascript
// Retain with document ID
await client.retain('my-bank', 'Alice presented the Q4 roadmap...', {
    document_id: 'meeting-2024-03-15'
});

// Batch retain for a document with different sections
await client.retainBatch('my-bank', [
    { content: 'Item 1: Product launch delayed to Q2', document_id: 'meeting-2024-03-15-section-1' },
    { content: 'Item 2: New hiring targets announced', document_id: 'meeting-2024-03-15-section-2' },
    { content: 'Item 3: Budget approved for ML team', document_id: 'meeting-2024-03-15-section-3' }
]);
```

### CLI

```bash
# Retain content with document ID
hindsight memory retain my-bank "Meeting notes content..." --doc-id notes-2024-03-15

# Batch retain from files
hindsight memory retain-files my-bank docs/
```

### Go

```go
# Section 'document-retain' not found in api/documents.go
```

## Обновить документы

Повторный retain с тем же `document_id` **заменяет** прежний текст:

### Python

```python
# Original
client.retain(
    bank_id="my-bank",
    content="Project deadline: March 31",
    document_id="project-plan"
)

# Update (deletes old facts, creates new ones)
client.retain(
    bank_id="my-bank",
    content="Project deadline: April 15 (extended)",
    document_id="project-plan"
)
```

### Node.js

```javascript
// Original
await client.retain('my-bank', 'Project deadline: March 31', {
    document_id: 'project-plan'
});

// Update
await client.retain('my-bank', 'Project deadline: April 15 (extended)', {
    document_id: 'project-plan'
});
```

### CLI

```bash
# Original
hindsight memory retain my-bank "Project deadline: March 31" --doc-id project-plan

# Update
hindsight memory retain my-bank "Project deadline: April 15 (extended)" --doc-id project-plan
```

### Go

```go
# Section 'document-update' not found in api/documents.go
```

## Получить документ

Получите исходный текст и метаданные документа. Это помогает расширить контекст после recall, если найденные воспоминания ссылаются на документ.

### Python

```python
from hindsight_client_api import ApiClient, Configuration
from hindsight_client_api.api import DocumentsApi

async def get_document_example():
    config = Configuration(host="http://localhost:8888")
    api_client = ApiClient(config)
    api = DocumentsApi(api_client)

    # Get document to expand context from recall results
    doc = await api.get_document(
        bank_id="my-bank",
        document_id="meeting-2024-03-15"
    )

    print(f"Document: {doc.id}")
    print(f"Original text: {doc.original_text}")
    print(f"Memory count: {doc.memory_unit_count}")
    print(f"Created: {doc.created_at}")

asyncio.run(get_document_example())
```

### Node.js

```javascript
// Get document to expand context from recall results
const { data: doc, error } = await sdk.getDocument({
    client: apiClient,
    path: { bank_id: 'my-bank', document_id: 'meeting-2024-03-15-section-1' }
});

if (error) {
    throw new Error(`Failed to get document: ${JSON.stringify(error)}`);
}

console.log(`Document: ${doc.id}`);
console.log(`Original text: ${doc.original_text}`);
console.log(`Memory count: ${doc.memory_unit_count}`);
console.log(`Created: ${doc.created_at}`);
```

### CLI

```bash
hindsight document get my-bank meeting-2024-03-15
```

### Go

```go
# Section 'document-get' not found in api/documents.go
```

## Изменить документ

Измените доступные для правки поля документа без повторной обработки текста. Сейчас можно менять `tags`.

### Python

```python
# Original
client.retain(
    bank_id="my-bank",
    content="Project deadline: March 31",
    document_id="project-plan"
)

# Update (deletes old facts, creates new ones)
client.retain(
    bank_id="my-bank",
    content="Project deadline: April 15 (extended)",
    document_id="project-plan"
)
```

### Node.js

```javascript
// Original
await client.retain('my-bank', 'Project deadline: March 31', {
    document_id: 'project-plan'
});

// Update
await client.retain('my-bank', 'Project deadline: April 15 (extended)', {
    document_id: 'project-plan'
});
```

### CLI

```bash
# Replace tags with new values
hindsight document update-tags my-bank meeting-2024-03-15 --tags team-a --tags team-b

# Remove all tags
hindsight document update-tags my-bank meeting-2024-03-15
```

### Go

```go
# Section 'document-update' not found in api/documents.go
```

> **ℹ️ Наблюдения обобщаются заново**
> 
После смены тегов наблюдения из воспоминаний документа признаются устаревшими и попадают в очередь на новое обобщение с новыми тегами. Состояние воспоминаний из других документов, которые тоже служили их источниками, сбрасывается.
## Удалить документ

Удалите документ вместе со всеми его воспоминаниями:

### Python

```python
from hindsight_client_api import ApiClient, Configuration
from hindsight_client_api.api import DocumentsApi

async def delete_document_example():
    config = Configuration(host="http://localhost:8888")
    api_client = ApiClient(config)
    api = DocumentsApi(api_client)

    # Delete document and all its memories
    result = await api.delete_document(
        bank_id="my-bank",
        document_id="meeting-2024-03-15"
    )

    print(f"Deleted {result.memory_units_deleted} memories")

asyncio.run(delete_document_example())
```

### Node.js

```javascript
// Delete document and all its memories
const { data: deleteResult } = await sdk.deleteDocument({
    client: apiClient,
    path: { bank_id: 'my-bank', document_id: 'meeting-2024-03-15-section-1' }
});

console.log(`Deleted ${deleteResult.memory_units_deleted} memories`);
```

### CLI

```bash
hindsight document delete my-bank meeting-2024-03-15
```

### Go

```go
# Section 'document-delete' not found in api/documents.go
```

> **⚠️ Внимание**
> 
При удалении документа все извлечённые из него воспоминания пропадают навсегда. Отменить действие нельзя.
## Список документов

Получите документы банка; по желанию отберите по ID и тегам.

### Python

```python
from hindsight_client_api import ApiClient, Configuration
from hindsight_client_api.api import DocumentsApi

async def list_documents_example():
    config = Configuration(host="http://localhost:8888")
    api_client = ApiClient(config)
    api = DocumentsApi(api_client)

    # List all documents
    result = await api.list_documents(bank_id="my-bank")
    print(f"Total documents: {result.total}")

    # Filter by document ID substring
    result = await api.list_documents(bank_id="my-bank", q="report")

    # Filter by tags — only docs tagged with "team-a" (untagged excluded)
    result = await api.list_documents(
        bank_id="my-bank",
        tags=["team-a"],
        tags_match="any_strict",
    )

    # Combine ID search and tags
    result = await api.list_documents(
        bank_id="my-bank",
        q="meeting",
        tags=["team-a", "team-b"],
        tags_match="all_strict",  # must have both tags
    )

    # Paginate
    result = await api.list_documents(bank_id="my-bank", limit=20, offset=40)
    print(f"Page items: {len(result.items)}")

import asyncio
asyncio.run(list_documents_example())
```

### Node.js

```javascript
const apiClient = createClient(createConfig({ baseUrl: 'http://localhost:8888' }));

// List all documents
const { data: allDocs } = await sdk.listDocuments({
    client: apiClient,
    path: { bank_id: 'my-bank' }
});
console.log(`Total documents: ${allDocs.total}`);

// Filter by document ID substring
const { data: reportDocs } = await sdk.listDocuments({
    client: apiClient,
    path: { bank_id: 'my-bank' },
    query: { q: 'report' }
});

// Filter by tags — only docs tagged with "team-a" (untagged excluded)
const { data: taggedDocs } = await sdk.listDocuments({
    client: apiClient,
    path: { bank_id: 'my-bank' },
    query: { tags: ['team-a'], tags_match: 'any_strict' }
});

// Combine ID search and tags
const { data: filtered } = await sdk.listDocuments({
    client: apiClient,
    path: { bank_id: 'my-bank' },
    query: { q: 'meeting', tags: ['team-a', 'team-b'], tags_match: 'all_strict' }
});

// Paginate
const { data: page } = await sdk.listDocuments({
    client: apiClient,
    path: { bank_id: 'my-bank' },
    query: { limit: 20, offset: 40 }
});
console.log(`Page items: ${page.items.length}`);
```

### CLI

```bash
# List all documents
hindsight document list my-bank

# Filter by ID substring
hindsight document list my-bank --q report

# Filter by tags
hindsight document list my-bank --tags team-a --tags team-b
```

### Go

```go
# Section 'document-list' not found in api/documents.go
```

### Настройки отбора

| Параметр | Описание |
|---|---|
| `q` | Поиск части ID без учёта регистра: `report` найдёт `report-2024`, `annual-report` и т. д. |
| `tags` | Отбор по тегам документа. Можно задать несколько значений. |
| `tags_match` | Способ сравнения тегов (по умолчанию `any_strict`). См. ниже. |
| `limit` / `offset` | Чтение страницами. По умолчанию предел — 100. |

**Режимы `tags_match`:**

| Режим | Поведение |
|---|---|
| `any_strict` *(по умолчанию)* | У документа должен быть **хотя бы один** указанный тег. Документы без тегов исключены. |
| `any` | То же, что `any_strict`, но с документами без тегов. |
| `all_strict` | У документа должны быть **все** указанные теги. Документы без тегов исключены. |
| `all` | То же, что `all_strict`, но с документами без тегов. |

## Вид ответа с документом

```json
{
  "id": "meeting-2024-03-15",
  "bank_id": "my-bank",
  "original_text": "Alice presented the Q4 roadmap...",
  "content_hash": "abc123def456",
  "memory_unit_count": 12,
  "nodes_by_fact_type": {
    "world": 5,
    "experience": 4,
    "observation": 3
  },
  "created_at": "2024-03-15T14:00:00Z",
  "updated_at": "2024-03-15T14:00:00Z"
}
```

## Что дальше

- [**Операции**](./operations) — слежение за фоновыми задачами
- [**Банки памяти**](./memory-banks) — настройка банков
