---
sidebar_position: 2
---

# Клиент TypeScript / JavaScript

Официальный клиент TypeScript/JavaScript для API Hindsight. Работает в **Node.js** и **Deno**.

## Установка

### Node.js

```bash
npm install @vectorize-io/hindsight-client
```

### Deno

Установка не нужна: импортируйте пакет через префикс `npm:`:

```typescript
import { HindsightClient } from "npm:@vectorize-io/hindsight-client";
```

## Быстрый старт

```typescript
import { HindsightClient } from '@vectorize-io/hindsight-client';

const client = new HindsightClient({ baseUrl: 'http://localhost:8888' });

// Retain a memory
await client.retain('my-bank', 'Alice works at Google');

// Recall memories
const response = await client.recall('my-bank', 'What does Alice do?');
for (const r of response.results) {
    console.log(r.text);
}

// Reflect - generate response with disposition
const answer = await client.reflect('my-bank', 'Tell me about Alice');
console.log(answer.text);
```

## Создание клиента

```typescript
import { HindsightClient } from '@vectorize-io/hindsight-client';

const client = new HindsightClient({
    baseUrl: 'http://localhost:8888',
});
```

## Основные операции

### Retain (сохранить воспоминание)

```typescript
// Simple
await client.retain('my-bank', 'Alice works at Google');

// With options
await client.retain('my-bank', 'Alice got promoted', {
    timestamp: new Date('2024-01-15'),
    context: 'career update',
    metadata: { source: 'slack' },
    async: false,  // Set true for background processing
});
```

### Retain: пакетная запись

```typescript
await client.retainBatch('my-bank', [
    { content: 'Alice works at Google', context: 'career' },
    { content: 'Bob is a data scientist', context: 'career' },
], {
    async: false,
});
```

### Recall (поиск)

```typescript
// Simple - returns RecallResponse
const response = await client.recall('my-bank', 'What does Alice do?');

for (const r of response.results) {
    console.log(`${r.text} (type: ${r.type})`);
}

// With options
const response = await client.recall('my-bank', 'What does Alice do?', {
    types: ['world', 'observation'],  // Filter by fact type
    maxTokens: 4096,
    budget: 'high',  // 'low', 'mid', or 'high'
});
```

### Reflect (создать ответ)

```typescript
const answer = await client.reflect('my-bank', 'What should I know about Alice?', {
    budget: 'low',  // 'low', 'mid', or 'high'
    context: 'preparing for a meeting',
});

console.log(answer.text);       // Generated response
```

## Управление банками

### Создать банк

```typescript
await client.createBank('my-bank', {
    name: 'Assistant',
    mission: "You're a helpful AI assistant - keep track of user preferences and conversation history.",
    disposition: {
        skepticism: 3,   // 1-5: trusting to skeptical
        literalism: 3,   // 1-5: flexible to literal
        empathy: 3,      // 1-5: detached to empathetic
    },
});
```

### Список воспоминаний

```typescript
const response = await client.listMemories('my-bank', {
    type: 'world',  // Optional filter
    q: 'Alice',     // Optional text search
    limit: 100,
    offset: 0,
});
console.log(response)
```
## Работа с документами

### Получить документ

```typescript
const doc = await client.getDocument('my-bank', 'conversation_001');
if (doc) {
    console.log(doc);  // null when document not found
}
```

### Список документов

```typescript
const response = await client.listDocuments('my-bank', {
    limit: 50,
    offset: 0,
});
console.log(response);
```

### Обновить документ

```typescript
await client.updateDocument('my-bank', 'conversation_001', {
    tags: ['important', 'meeting-notes'],
});
```

### Удалить документ

```typescript
await client.deleteDocument('my-bank', 'conversation_001');
```
