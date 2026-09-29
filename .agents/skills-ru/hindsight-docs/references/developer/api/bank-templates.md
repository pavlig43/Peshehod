
# Шаблоны банков

Манифесты JSON для создания настроенных банков памяти одним вызовом API.

{/* Import raw source files */}

## Обзор

Шаблон банка — манифест JSON с полной настройкой банка: отличиями от общих настроек, ментальными моделями, указаниями и другими данными. Вместо многих вызовов API вы отправляете один манифест, и API создаёт всё нужное.

Шаблоны полезны для:
- **Копирования** — создавать одинаково настроенные банки для разных пользователей или агентов.
- **Первого запуска** — дать новым пользователям проверенные настройки без ручной работы с нуля.
- **Обмена** — передавать готовые настройки в файлах JSON.
- **Подключения библиотек** — поставлять подходящий шаблон вместе с вашим подключением.

Готовые шаблоны есть в разделе Bank Templates Hub.

## Схема манифеста

```json
{
  "version": "1",
  "bank": {
    "reflect_mission": "...",
    "retain_mission": "...",
    "retain_extraction_mode": "concise | verbose | custom | chunks",
    "retain_custom_instructions": "...",
    "retain_chunk_size": 2048,
    "retain_structured_chunk_size": 8192,
    "disposition_skepticism": 3,
    "disposition_literalism": 3,
    "disposition_empathy": 3,
    "enable_observations": true,
    "observations_mission": "...",
    "entity_labels": [{ "key": "sentiment", "type": "value", "values": [{ "value": "positive" }, { "value": "negative" }] }],
    "entities_allow_free_form": true
  },
  "mental_models": [
    {
      "id": "unique-lowercase-id",
      "name": "Human-Readable Name",
      "source_query": "The query that generates this mental model's content",
      "tags": ["optional", "tags"],
      "max_tokens": 2048,
      "trigger": {
        "refresh_after_consolidation": false,
        "fact_types": ["world", "experience", "observation"],
        "exclude_mental_models": false,
        "exclude_mental_model_ids": []
      }
    }
  ],
  "directives": [
    {
      "name": "directive-name",
      "content": "The directive instruction text",
      "priority": 0,
      "is_active": true,
      "tags": ["optional", "tags"]
    }
  ]
}
```

### Поля

| Поле | Обязательно | Описание |
|-------|----------|-------------|
| `version` | Да | Версия схемы. Сейчас `"1"`. |
| `bank` | Нет | Настройки банка поверх общих. Не указывайте, если их не нужно менять. |
| `mental_models` | Нет | Ментальные модели для создания или обновления. Не указывайте, если их не нужно менять. |
| `directives` | Нет | Указания для создания или обновления. Не указывайте, если их не нужно менять. |

Все три раздела — `bank`, `mental_models` и `directives` — необязательны. Если раздел пропустить, эта часть банка не изменится.

### Поля настроек банка

Все поля `bank` необязательны. Только указанные поля будут отличаться от общих настроек. Остальные берутся из значений сервера или арендатора по умолчанию.

| Поле | Тип | Описание |
|-------|------|-------------|
| `reflect_mission` | string | Задача и контекст для reflect |
| `retain_mission` | string | Направляет извлечение фактов при retain |
| `retain_extraction_mode` | string | `concise`, `verbose`, `custom` или `chunks` |
| `retain_custom_instructions` | string | Свой запрос на извлечение (нужен `mode=custom`) |
| `retain_chunk_size` | integer | Целевой предел символов в части текста |
| `retain_structured_chunk_size` | integer | Предел символов для строки JSONL или одного хода беседы, которые нужно сохранить целиком. Если не задан, берётся `retain_chunk_size` |
| `disposition_skepticism` | integer (1-5) | Степень скептицизма |
| `disposition_literalism` | integer (1-5) | Степень буквальности |
| `disposition_empathy` | integer (1-5) | Степень эмпатии |
| `enable_observations` | boolean | Включение обобщения наблюдений |
| `observations_mission` | string | Управляет тем, что попадает в наблюдения |
| `entity_labels` | object[] | Строгий набор меток по группам; см. [«Банки памяти → entity_labels»](./memory-banks#entity-labels) |
| `entities_allow_free_form` | boolean | Разрешает сущности вне заданного набора меток |

### Поля ментальной модели

| Поле | Обязательно | Описание |
|-------|----------|-------------|
| `id` | Да | Уникальный ID: строчные буквы, цифры и дефисы. Нужен для сопоставления при повторном импорте. |
| `name` | Да | Понятное человеку имя |
| `source_query` | Да | Запрос, по которому reflect создаёт текст модели |
| `tags` | Нет | Теги области видимости. По умолчанию `[]` |
| `max_tokens` | Нет | Предел токенов созданного текста (256–8192). По умолчанию `2048` |
| `trigger` | Нет | Условия автообновления |

### Поля указаний

| Поле | Обязательно | Описание |
|-------|----------|-------------|
| `name` | Да | Имя указания. По нему ищут совпадение при повторном импорте. |
| `content` | Да | Текст указания. |
| `priority` | Нет | Приоритет: чем выше, тем важнее. По умолчанию `0` |
| `is_active` | Нет | Действует ли указание. По умолчанию `true` |
| `tags` | Нет | Теги для группировки. По умолчанию `[]` |

## Импорт

Импортируйте манифест в банк. Если банка ещё нет, он будет создан сам.

### Python

```python
template = {
    "version": "1",
    "bank": {
        "retain_mission": "Extract customer issues, resolutions, and sentiment.",
        "enable_observations": True,
        "observations_mission": "Track recurring customer pain points.",
    },
    "mental_models": [
        {
            "id": "sentiment-overview",
            "name": "Customer Sentiment Overview",
            "source_query": "What is the overall sentiment trend?",
            "trigger": {"refresh_after_consolidation": True},
        }
    ],
    "directives": [
        {
            "name": "Acknowledge frustration",
            "content": "Always acknowledge frustration before offering solutions.",
            "priority": 10,
        }
    ],
}

response = requests.post(
    f"{HINDSIGHT_URL}/v1/default/banks/my-bank/import",
    json=template,
)
result = response.json()
print(f"Config applied: {result['config_applied']}")
print(f"Mental models created: {result['mental_models_created']}")
print(f"Directives created: {result['directives_created']}")
```

### Node.js

```javascript
const template = {
  version: '1',
  bank: {
    retain_mission: 'Extract customer issues, resolutions, and sentiment.',
    enable_observations: true,
    observations_mission: 'Track recurring customer pain points.',
  },
  mental_models: [
    {
      id: 'sentiment-overview',
      name: 'Customer Sentiment Overview',
      source_query: 'What is the overall sentiment trend?',
      trigger: { refresh_after_consolidation: true },
    },
  ],
  directives: [
    {
      name: 'Acknowledge frustration',
      content: 'Always acknowledge frustration before offering solutions.',
      priority: 10,
    },
  ],
};

const importResponse = await fetch(
  `${HINDSIGHT_URL}/v1/default/banks/my-bank/import`,
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(template),
  },
);
const result = await importResponse.json();
console.log('Config applied:', result.config_applied);
console.log('Mental models created:', result.mental_models_created);
console.log('Directives created:', result.directives_created);
```

### CLI

```bash
curl -X POST "$HINDSIGHT_URL/v1/default/banks/my-bank/import" \
  -H "Content-Type: application/json" \
  -d '{
    "version": "1",
    "bank": {
      "retain_mission": "Extract customer issues, resolutions, and sentiment.",
      "enable_observations": true,
      "observations_mission": "Track recurring customer pain points."
    },
    "mental_models": [
      {
        "id": "sentiment-overview",
        "name": "Customer Sentiment Overview",
        "source_query": "What is the overall sentiment trend?",
        "trigger": { "refresh_after_consolidation": true }
      }
    ],
    "directives": [
      {
        "name": "Acknowledge frustration",
        "content": "Always acknowledge frustration before offering solutions.",
        "priority": 10
      }
    ]
  }'
```

### Go

```go
# Section 'import-template' not found in api/bank-templates.go
```

### Что произойдёт

- **Настройки**: все поля `bank` применяются поверх общих настроек для этого банка.
- **Ментальные модели**: сопоставляются по `id`; прежние обновляются, новые создаются.
- **Указания**: сопоставляются по `name`; прежние обновляются, новые создаются.
- **Асинхронная работа**: текст ментальных моделей создаётся в фоне. Ответ содержит `operation_ids` для слежения за ходом.

### Проверка без записи

Проверьте манифест, не меняя банк:

### Python

```python
response = requests.post(
    f"{HINDSIGHT_URL}/v1/default/banks/my-bank/import",
    params={"dry_run": "true"},
    json=template,
)
result = response.json()
print(f"Dry run: {result['dry_run']}")
print(f"Would apply config: {result['config_applied']}")
```

### Node.js

```javascript
const dryRunResponse = await fetch(
  `${HINDSIGHT_URL}/v1/default/banks/my-bank/import?dry_run=true`,
  {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(template),
  },
);
const dryRunResult = await dryRunResponse.json();
console.log('Dry run:', dryRunResult.dry_run);
console.log('Would apply config:', dryRunResult.config_applied);
```

### CLI

```bash
curl -X POST "$HINDSIGHT_URL/v1/default/banks/my-bank/import?dry_run=true" \
  -H "Content-Type: application/json" \
  -d '{"version": "1", "bank": {"retain_mission": "Dry run test."}}'
```

### Go

```go
# Section 'import-dry-run' not found in api/bank-templates.go
```

Ответ покажет, *что произошло бы*: какие настройки применились бы и какие модели создались бы. Ничего не меняется. Если манифест неверен, вернётся HTTP 400 с подробной ошибкой.

## Экспорт

Выгрузите настройки банка поверх общих, ментальные модели и указания в манифест:

### Python

```python
response = requests.get(
    f"{HINDSIGHT_URL}/v1/default/banks/my-bank/export"
)
exported = response.json()
print(json.dumps(exported, indent=2))
```

### Node.js

```javascript
const exportResponse = await fetch(
  `${HINDSIGHT_URL}/v1/default/banks/my-bank/export`,
);
const exported = await exportResponse.json();
console.log(JSON.stringify(exported, null, 2));
```

### CLI

```bash
curl "$HINDSIGHT_URL/v1/default/banks/my-bank/export"
```

### Go

```go
# Section 'export-template' not found in api/bank-templates.go
```

В выгрузке будут только поля, явно заданные для банка, а не все итоговые настройки с общими значениями сервера и арендатора. Поэтому манифест можно перенести: при импорте в другой банк он изменит лишь те поля, которые вы сами настроили.

### Перенос между банками

Выгрузите настройки одного банка и импортируйте их в другой:

### Python

```python
# Export from source bank
response = requests.get(
    f"{HINDSIGHT_URL}/v1/default/banks/source-bank/export"
)
exported = response.json()

# Import into a new bank
response = requests.post(
    f"{HINDSIGHT_URL}/v1/default/banks/new-bank/import",
    json=exported,
)
```

### Node.js

```javascript
// Export from source bank
const srcResponse = await fetch(
  `${HINDSIGHT_URL}/v1/default/banks/source-bank/export`,
);
const srcExported = await srcResponse.json();

// Import into a new bank
await fetch(`${HINDSIGHT_URL}/v1/default/banks/new-bank/import`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(srcExported),
});
```

### CLI

```bash
# Export from source bank
curl "$HINDSIGHT_URL/v1/default/banks/source-bank/export" > template.json

# Import into a new bank
curl -X POST "$HINDSIGHT_URL/v1/default/banks/new-bank/import" \
  -H "Content-Type: application/json" \
  -d @template.json
```

### Go

```go
# Section 'export-reimport' not found in api/bank-templates.go
```

## Схема JSON

Формат манифеста задаёт схема JSON. Получите текущую схему со своего сервера:

### Python

```python
response = requests.get(
    f"{HINDSIGHT_URL}/v1/bank-template-schema"
)
schema = response.json()
print(json.dumps(schema, indent=2))
```

### Node.js

```javascript
const schemaResponse = await fetch(
  `${HINDSIGHT_URL}/v1/bank-template-schema`,
);
const schema = await schemaResponse.json();
console.log(JSON.stringify(schema, null, 2));
```

### CLI

```bash
curl "$HINDSIGHT_URL/v1/bank-template-schema"
```

### Go

```go
# Section 'get-schema' not found in api/bank-templates.go
```

Статичная схема есть и в `bank-template-schema.json`.

## Панель управления

При создании банка в панели управления можно включить «Import from template». Тогда вы сможете вставить манифест JSON и заранее настроить банк.

Шаблон любого банка можно выгрузить на странице его настроек через **Actions → Export Template**: манифест JSON скопируется в буфер обмена.

## Версии

Поле `version` позволяет обновлять схему с сохранением совместимости. Сейчас версия — `"1"`.

Когда выйдут новые версии:
- Старые манифесты при импорте сами обновятся до текущей схемы.
- При экспорте всегда будет последняя версия.
- API отклонит манифест с версией новее той, что знает сервер, и предложит обновить сервер.

Старые шаблоны продолжат работать; вручную обновлять их не нужно.
