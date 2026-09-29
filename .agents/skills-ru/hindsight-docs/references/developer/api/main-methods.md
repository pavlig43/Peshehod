
# Основные методы

В Hindsight есть три главные операции: **retain**, **recall** и **reflect**.

{/* Import raw source files */}

> **💡 Что нужно заранее**
> 
Убедитесь, что [установили Hindsight](../installation) и прошли [«Быстрый старт»](./quickstart).
## Retain: сохранение сведений

Сохраняйте беседы, документы и факты в банке памяти.

### Python

```python
# Store a single fact
client.retain(
    bank_id="my-bank",
    content="Alice joined Google in March 2024 as a Senior ML Engineer"
)

# Store a conversation
conversation = """
User: What did you work on today?
Assistant: I reviewed the new ML pipeline architecture.
User: How did it look?
Assistant: Promising, but needs better error handling.
"""

client.retain(
    bank_id="my-bank",
    content=conversation,
    context="Daily standup conversation"
)

# Batch retain multiple items
client.retain_batch(
    bank_id="my-bank",
    items=[
        {"content": "Bob prefers Python for data science"},
        {"content": "Alice recommends using pytest for testing"},
        {"content": "The team uses GitHub for code reviews"}
    ]
)
```

### Node.js

```javascript
// Store a single fact
await client.retain('my-bank', 'Alice joined Google in March 2024 as a Senior ML Engineer');

// Store a conversation
const conversation = `
User: What did you work on today?
Assistant: I reviewed the new ML pipeline architecture.
User: How did it look?
Assistant: Promising, but needs better error handling.
`;

await client.retain('my-bank', conversation, {
    context: 'Daily standup conversation'
});

// Batch retain multiple items
await client.retainBatch('my-bank', [
    { content: 'Bob prefers Python for data science' },
    { content: 'Alice recommends using pytest for testing' },
    { content: 'The team uses GitHub for code reviews' }
]);
```

### CLI

```bash
# Store a single fact
hindsight memory retain my-bank "Alice joined Google in March 2024 as a Senior ML Engineer"

# Store from a file
hindsight memory retain-files my-bank conversation.txt --context "Daily standup"

# Store multiple files
hindsight memory retain-files my-bank docs/
```

### Go

```go
# Section 'main-retain' not found in api/main-methods.go
```

**Что происходит:** LLM обрабатывает текст, извлекает полные факты, находит сущности и строит связи в графе знаний.

**См.:** [«Подробности Retain»](./retain) — другие параметры и тонкая настройка.

---

## Recall: поиск воспоминаний

Ищите нужные воспоминания несколькими способами.

### Python

```python
# Basic search
results = client.recall(
    bank_id="my-bank",
    query="What does Alice do at Google?"
)

for result in results.results:
    print(f"- {result.text}")

# Search with options
results = client.recall(
    bank_id="my-bank",
    query="What happened last spring?",
    budget="high",  # More thorough graph traversal
    max_tokens=8192,  # Return more context
    types=["world"]  # Only world facts
)

# Include source chunks for more context
results = client.recall(
    bank_id="my-bank",
    query="Tell me about Alice",
    include_chunks=True,
    max_chunk_tokens=500
)

# Check chunk details (chunks are on response level, keyed by memory ID)
for result in results.results:
    print(f"Memory: {result.text}")
    if results.chunks and result.id in results.chunks:
        chunk = results.chunks[result.id]
        print(f"  Source: {chunk.text[:100]}...")
```

### Node.js

```javascript
// Basic search
const results = await client.recall('my-bank', 'What does Alice do at Google?');

for (const result of results.results) {
    console.log(`- ${result.text}`);
}

// Search with options
const filteredResults = await client.recall('my-bank', 'What happened last spring?', {
    budget: 'high',
    maxTokens: 8192,
    types: ['world']
});

// Include entity information
const entityResults = await client.recall('my-bank', 'Tell me about Alice', {
    includeEntities: true,
    maxEntityTokens: 500
});

// Check entity details
for (const [entityId, entity] of Object.entries(entityResults.entities || {})) {
    console.log(`Entity: ${entity.canonical_name}`);
    console.log(`Observations: ${entity.observations}`);
}
```

### CLI

```bash
# Basic search
hindsight memory recall my-bank "What does Alice do at Google?"

# Search with options
hindsight memory recall my-bank "What happened last spring?" \
    --budget high \
    --max-tokens 8192 \
    --fact-type world,experience

# Verbose output
hindsight memory recall my-bank "Tell me about Alice" -v
```

### Go

```go
# Section 'main-recall' not found in api/main-methods.go
```

**Что происходит:** четыре вида поиска (по смыслу, словам, графу и времени) идут параллельно, затем результаты объединяются и сортируются заново.

**См.:** [«Подробности Recall»](./recall) — как найти баланс качества и скорости.

---

## Reflect: рассуждение с учётом черт поведения

Создавайте ответы на основе воспоминаний и наблюдений с учётом черт банка.

### Python

```python
# Basic reflect
response = client.reflect(
    bank_id="my-bank",
    query="Should we adopt TypeScript for our backend?",
    include_facts=True,
)

print(response.text)
print("\nBased on:", len(response.based_on.memories if response.based_on else []), "facts")

# Reflect with options
response = client.reflect(
    bank_id="my-bank",
    query="What are Alice's strengths for the team lead role?",
    budget="high",  # More thorough reasoning
    include_facts=True,
)

# See which facts influenced the response
for fact in (response.based_on.memories if response.based_on else []):
    print(f"- {fact.text}")
```

### Node.js

```javascript
// Basic reflect
const response = await client.reflect('my-bank', 'Should we adopt TypeScript for our backend?');

console.log(response.text);
console.log('\nBased on:', (response.based_on || []).length, 'facts');

// Reflect with options
const detailedResponse = await client.reflect('my-bank', "What are Alice's strengths for the team lead role?", {
    budget: 'high'
});

// See which facts influenced the response
for (const fact of detailedResponse.based_on || []) {
    console.log(`- ${fact.text}`);
}
```

### CLI

```bash
# Basic reflect
hindsight memory reflect my-bank "Should we adopt TypeScript for our backend?"

# With higher reasoning budget
hindsight memory reflect my-bank "Analyze our tech stack" --budget high
```

### Go

```go
# Section 'main-reflect' not found in api/main-methods.go
```

**Что происходит:** Hindsight находит воспоминания и наблюдения, учитывает черты банка, а LLM строит ответ на основе этих данных.

**См.:** [«Подробности Reflect»](./reflect) — настройка черт поведения.

---

## Сравнение

| Свойство | Retain | Recall | Reflect |
|---------|--------|--------|---------|
| **Цель** | Сохранить сведения | Найти сведения | Осмыслить сведения |
| **На входе** | Исходный текст и документы | Поисковый запрос | Вопрос или запрос |
| **На выходе** | ID воспоминаний | Отсортированные факты и наблюдения | Обоснованный ответ |
| **Нужна LLM** | Да (извлечение) | Нет | Да (создание ответа) |
| **Берёт наблюдения** | Нет | Да | Да |
| **Учитывает черты поведения** | Нет | Нет | Да |

---

## Что дальше

- [**Retain**](./retain) — тонкая настройка сохранения памяти
- [**Recall**](./recall) — настройка качества и скорости поиска
- [**Reflect**](./reflect) — настройка черт поведения
- [**Банки памяти**](./memory-banks) — управление чертами банка
