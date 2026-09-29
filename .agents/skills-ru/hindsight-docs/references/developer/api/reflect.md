
# Reflect

Создавайте обоснованный ответ с учётом черт поведения через цикл рассуждения агента.

При вызове **reflect** Hindsight запускает цикл агента. Он сам ищет в банке разными способами, учитывает черты поведения банка и создаёт ответ на основе найденных данных. В отличие от recall, который возвращает исходные факты, reflect даёт готовый ответ LLM.

{/* Import raw source files */}

> **ℹ️ Как работает Reflect**
> 
О рассуждении с учётом черт поведения читайте в разделе [«Устройство Reflect»](../reflect.md).
> **💡 Что нужно заранее**
> 
Пройдите [«Быстрый старт»](./quickstart), чтобы установить клиент и запустить сервер.
## Основной вызов

### Python

```python
client.reflect(bank_id="my-bank", query="What should I know about Alice?")
```

### Node.js

```javascript
await client.reflect('my-bank', 'What should I know about Alice?');
```

### CLI

```bash
hindsight memory reflect my-bank "What do you know about Alice?"
```

### Go

```go
# Section 'reflect-basic' not found in api/reflect.go
```

---

## Параметры

### query

Вопрос или запрос для рассуждения. Это единственное обязательное поле. Если на ответ должен влиять контекст ситуации, добавьте его прямо в запрос, а не в отдельное поле.

### budget

Задаёт, насколько глубоко агент изучает банк до ответа. Возможны `low` (по умолчанию), `mid` и `high`. При `low` он ищет неглубоко и быстро. При `mid` по нужде проверяет несколько источников. При `high` проходит все уровни знаний и может по-разному сформулировать запрос, чтобы найти косвенные связи. Берите `high` для сложных вопросов, где нужно объединить много источников.

### Python

```python
response = client.reflect(
    bank_id="my-bank",
    query="We're considering a hybrid work policy. What do you think about remote work?",
    budget="mid",
)
```

### Node.js

```javascript
const response = await client.reflect('my-bank', 'What do you think about remote work?', {
    budget: 'mid',
    context: "We're considering a hybrid work policy"
});
```

### CLI

```bash
hindsight memory reflect my-bank "Summarize my week" --budget high --max-tokens 8192
```

### Go

```go
# Section 'reflect-with-params' not found in api/reflect.go
```

### max_tokens

Ограничивает длину итогового ответа. По умолчанию `4096`. Это не влияет на объём данных, который агент может найти в ходе работы, а лишь на длину ответа.

### response_schema

Необязательная схема JSON. Если она задана, LLM создаёт ответ по схеме, а в поле `structured_output` возвращается разобранный результат. Поле `text` будет пустым, так как выполняется один структурный вызов LLM. Берите это, если ответ пойдёт на обработку в код, а не для чтения человеком.

### Python

```python
from pydantic import BaseModel

# Define your response structure with Pydantic
class HiringRecommendation(BaseModel):
    recommendation: str
    confidence: str  # "low", "medium", "high"
    key_factors: list[str]
    risks: list[str] = []

response = client.reflect(
    bank_id="hiring-team",
    query="Should we hire Alice for the ML team lead position?",
    response_schema=HiringRecommendation.model_json_schema(),
)

# Parse structured output into Pydantic model
result = HiringRecommendation.model_validate(response.structured_output)
print(f"Recommendation: {result.recommendation}")
print(f"Confidence: {result.confidence}")
print(f"Key factors: {result.key_factors}")
```

### Node.js

```javascript
// Define JSON schema directly
const responseSchema = {
    type: 'object',
    properties: {
        recommendation: { type: 'string' },
        confidence: { type: 'string', enum: ['low', 'medium', 'high'] },
        key_factors: { type: 'array', items: { type: 'string' } },
        risks: { type: 'array', items: { type: 'string' } },
    },
    required: ['recommendation', 'confidence', 'key_factors'],
};

const structuredResponse = await client.reflect('my-bank', 'What do you know about Alice and her career?', {
    responseSchema: responseSchema,
});

// Structured output (if returned)
if (structuredResponse.structuredOutput) {
    console.log('Recommendation:', structuredResponse.structuredOutput.recommendation || 'N/A');
    console.log('Key factors:', structuredResponse.structuredOutput.key_factors || []);
}
```

### CLI

```bash
# First, create a JSON schema file schema.json:
cat > schema.json << 'EOF'
{
  "type": "object",
  "properties": {
    "recommendation": {"type": "string"},
    "confidence": {"type": "string", "enum": ["low", "medium", "high"]},
    "key_factors": {"type": "array", "items": {"type": "string"}}
  },
  "required": ["recommendation", "confidence", "key_factors"]
}
EOF

# Then use the --schema flag:
hindsight memory reflect hiring-team \
  "Should we hire Alice for the ML team lead position?" \
  --schema schema.json

# Cleanup the temporary schema file
rm -f schema.json
```

### Go

```go
# Section 'reflect-structured-output' not found in api/reflect.go
```

### tags

Ограничивает воспоминания, доступные агенту при рассуждении. Работает так же, как [теги recall](./recall#tags): учитываются лишь записи с подходящими тегами. Параметр `tags_match` задаёт способ сравнения (`any`, `all`, `any_strict`, `all_strict`, `exact`) с тем же смыслом, что и в recall.

### Python

```python
# Filter reflection to only consider memories for a specific user
response = client.reflect(
    bank_id="my-bank",
    query="What does this user think about our product?",
    tags=["user:alice"],
    tags_match="any_strict"  # Only use memories tagged for this user
)
```

### Node.js

```javascript
// Filter reflect to only use memories tagged for a specific user
await client.reflect('my-bank', 'What feedback did the user give?', {
    tags: ['user:alice'],
    tagsMatch: 'any_strict'
});
```

### CLI

```bash
hindsight memory reflect my-bank "What feedback did the user give?" \
  --tags "user:alice" --tags-match any_strict
```

### Go

```go
# Section 'reflect-with-tags' not found in api/reflect.go
```

### include

Задаёт лишние данные, которые вернутся вместе с главным ответом.

#### include.facts

Если включён, ответ содержит объект `based_on` со списком воспоминаний, ментальных моделей и указаний, которые агент взял для вывода. Попасть туда могут только источники, найденные в ходе работы: ссылки проверяются, чтобы не допустить выдуманных оснований. Это помогает проверить ответ.

### Python

```python
# include_facts=True enables the based_on field in the response
response = client.reflect(
    bank_id="my-bank",
    query="Tell me about Alice",
    include_facts=True,
)

print("Response:", response.text)
print("\nBased on:")
for fact in (response.based_on.memories if response.based_on else []):
    print(f"  - [{fact.type}] {fact.text}")
```

### Node.js

```javascript
const sourcesResponse = await client.reflect('my-bank', 'Tell me about Alice', {
    includeFacts: true
});

console.log('Response:', sourcesResponse.text);
console.log('\nBased on:');
for (const fact of (sourcesResponse.based_on?.memories || [])) {
    console.log(`  - [${fact.type}] ${fact.text}`);
}
```

### CLI

```bash
hindsight memory reflect my-bank "Tell me about Alice" --include-facts
```

### Go

```go
# Section 'reflect-sources' not found in api/reflect.go
```

#### include.tool_calls

Если включён, ответ содержит объект `trace` с полной историей вызовов инструментов и LLM: входом, выходом и временем. Задайте `output: false`, чтобы оставить лишь входы инструментов и уменьшить ответ. Это помогает выяснить, почему агент пришёл к выводу.

---

## Ответ

### text

Готовый ответ как строка Markdown. Это главный результат reflect. Если задано `response_schema`, поле пустое: читайте `structured_output`.

### structured_output

Ответ LLM, разобранный по схеме `response_schema` из запроса. Есть лишь при заданном `response_schema`; иначе `null`.

### based_on

Источники, взятые агентом для ответа. Поле есть лишь при включённом `include.facts`. В нём три части:

- `memories` — найденные и процитированные факты (world, experience, observation). У каждого есть `id`, `text`, `type`, `context`, `occurred_start` и `occurred_end`.
- `mental_models` — взятые ментальные модели. У каждой есть `id`, `text` и `context`.
- `directives` — указания, соблюдённые при выводе. У каждого есть `id`, `name` и `content`.

### usage

Число токенов во всех вызовах LLM за цикл агента: `input_tokens`, `output_tokens` и `total_tokens`. Помогает считать расходы.

### trace

Полная история работы агента. Есть лишь при включённом `include.tool_calls`. Содержит:

- `tool_calls` — каждый вызов инструмента с именем `tool` (`lookup`, `recall`, `learn`, `expand`), полями `input`, `output` (если `output: true`), `duration_ms` и номером шага `iteration`.
- `llm_calls` — каждый вызов LLM с полями `scope` (например, `"agent_1"`, `"final"`) и `duration_ms`.
