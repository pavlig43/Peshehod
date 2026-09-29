# Банки памяти

Банки памяти — изолированные контейнеры, хранящие данные памяти для конкретного контекста или задачи.

{/* Импорт исходных файлов */}

## Что такое банк памяти?

Банк памяти — это отдельное хранилище, которое содержит:

- **Воспоминания** — факты и сведения, сохранённые из бесед.
- **Документы** — файлы и содержимое, проиндексированные для поиска.
- **Сущности** — люди, места и понятия, извлечённые из воспоминаний.
- **Связи** — отношения между сущностями в графе знаний.
- **Директивы** — строгие правила, которым агент следует при выполнении операций reflect.

Банки полностью изолированы: воспоминания из одного банка недоступны в другом.

Создавать банк заранее необязательно. При первом обращении Hindsight создаст его автоматически с настройками по умолчанию.

> **💡 Требования**
>
> Выполните инструкции [быстрого старта](./quickstart), чтобы установить клиент и запустить сервер.

## Создание банка памяти

### Python

```python
client.create_bank(bank_id="my-bank")
```

### Node.js

```javascript
await client.createBank('my-bank');
```

### CLI

```bash
hindsight bank create my-bank
```

### Go

```go
# Section 'create-bank' not found in api/memory-banks.go
```

## Настройка банка

Каждый банк можно настраивать независимо для разных операций. Настройки задаются через [API конфигурации банка](#обновление-конфигурации), интерфейс Control Plane или [переменные среды сервера](../configuration.md).

### retain_mission {#retain-configuration}

Описание обычным языком того, на что банк должен обращать внимание при извлечении данных. Миссия добавляется к запросу на извлечение вместе со встроенными правилами: она задаёт фокус, но не заменяет логику извлечения.

```
Например: Всегда учитывай технические решения, варианты проектирования API и компромиссы в архитектуре.
           Игнорируй организацию встреч, приветствия и светские беседы.
```

Работает при любом режиме извлечения. Оставьте поле пустым для извлечения общего назначения.

### retain_extraction_mode

Определяет, насколько подробно извлекать факты:

| Режим | Описание |
|-------|----------|
| `concise` *(по умолчанию)* | Выборочный режим — только факты, важные для долгосрочного хранения |
| `verbose` | Больше подробностей для каждого факта; медленнее и требует больше токенов |
| `custom` | Собственные правила извлечения через `retain_custom_instructions` |

### retain_custom_instructions

Действует только при `retain_extraction_mode: custom`. Полностью заменяет встроенные правила извлечения вашими инструкциями.

### retain_chunk_size

Максимальное количество символов в одном фрагменте, на который делится содержимое перед извлечением фактов. Большие фрагменты уменьшают число запросов к LLM, но могут ухудшить качество извлечения из длинных материалов. Небольшие фрагменты точнее, но требуют больше запросов.

Значение по умолчанию: `3000`.

### retain_structured_chunk_size

Максимальное количество символов в одной строке JSONL или ходе разговора, который следует сохранить целиком, если он превышает `retain_chunk_size`. Если значение не задано, используется `retain_chunk_size`. Установите большее значение для структурированных журналов и стенограмм чатов, где разделение одной записи приведёт к потере контекста.

По умолчанию не задано — используется `retain_chunk_size`.

См. [настройки retain](../configuration.md#retain), где приведены имена переменных среды и значения по умолчанию.

### entity_labels {#entity-labels}

Задаёт контролируемый словарь классификационных меток `key:value`, которые извлекаются во время retain и сохраняются в виде сущностей. Метки становятся сущностями и автоматически связывают воспоминания в графе знаний (например, два воспоминания с `pedagogy:scaffolding` связываются), улучшают семантический поиск и поиск BM25. Если у группы задано `tag: true`, метки также можно использовать для фильтрации стандартным API `tags` / `tags_match`.

Каждая запись `entity_labels` — это **группа меток**, то есть одно измерение классификации:

```json
{
  "entity_labels": [
    {
      "key": "engagement",
      "description": "Student engagement level during the session",
      "type": "value",
      "optional": true,
      "values": [
        { "value": "active",  "description": "Student is actively participating" },
        { "value": "passive", "description": "Student is listening but not participating" }
      ]
    },
    {
      "key": "pedagogy",
      "description": "Teaching strategies used",
      "type": "multi-values",
      "values": [
        { "value": "scaffolding",          "description": "Breaking complex tasks into smaller steps" },
        { "value": "direct_instruction",   "description": "Explicit explanation by the teacher" },
        { "value": "socratic_questioning", "description": "Guiding through questions rather than answers" }
      ]
    }
  ]
}
```

| Поле | По умолчанию | Описание |
|------|--------------|----------|
| `key` | — | Идентификатор группы меток. Становится префиксом сущностей `key:value` или `key:field:value` для типа `"map"`. |
| `description` | `""` | Передаётся LLM как инструкция для назначения меток. |
| `type` | `"value"` | `"value"` — выбрать одно значение; `"multi-values"` — несколько; `"text"` — произвольная строка; `"map"` — структурированная группа с именованными полями. |
| `values` | `[]` | Допустимые значения для типов `"value"` и `"multi-values"`. Не используется с типами `"text"` и `"map"`. |
| `fields` | `{}` | Определения полей для типа `"map"`. Каждое поле имеет тип `"text"`, `"value"`, `"multi-values"` или вложенный `"map"`. Не используется для других типов. |
| `optional` | `true` | Если `true`, LLM может пропустить неприменимую метку. Если `false`, нужно всегда назначать значение. Не влияет на группы `"multi-values"`: они всегда необязательны. |
| `tag` | `false` | Если `true`, извлечённые метки `key:value` также записываются в теги единицы памяти и становятся доступны для фильтрации через `tags` / `tags_match` в recall и reflect. |

**Группы с перечислением** (`type: "value"` или `type: "multi-values"`): LLM выбирает значение из заранее заданного списка `values`; значения вне списка молча отбрасываются. Словарь остаётся стабильным, а связи графа — точными. Используйте `"multi-values"`, если факт может относиться сразу к нескольким значениям.

**Группы со свободным текстом** (`type: "text"`): LLM записывает произвольную строку. Добавьте примеры и инструкции в поле `description`. Кластеризация графа менее надёжна, чем для перечислений, потому что модель может описать одно понятие по-разному в разных беседах.

```json
{
  "key": "topic",
  "description": "Specific subject being discussed. Examples: algebra, quadratic equations, geometry.",
  "type": "text",
  "optional": true,
  "values": []
}
```

**Группы map** (`type: "map"`): задают структурированный тип сущности с именованными полями. Каждое поле имеет тип `"text"`, `"value"`, `"multi-values"` или вложенный `"map"`. Например, так можно описать человека с именем, должностью и организацией. Извлечённые поля хранятся как плоские строки сущностей `key:field:value` (например, `person:name:Alice`). Существующее хранилище сущностей используется без изменения схемы, поэтому поля map участвуют в графе знаний и поиске так же, как метки с одним значением.

```json
{
  "key": "person",
  "description": "A person mentioned in the text",
  "type": "map",
  "fields": {
    "name":         { "type": "text", "description": "Full name of the person" },
    "role":         { "type": "text", "description": "Job title or role" },
    "organization": { "type": "text", "description": "Company or organization" }
  }
}
```

### entities_allow_free_form

По умолчанию метки сущностей извлекаются **вместе** с обычными именованными сущностями: людьми, местами и понятиями. Задайте `false`, чтобы отключить свободное извлечение и сохранять только сущности-метки:

```json
{
  "entity_labels": [...],
  "entities_allow_free_form": false
}
```

### enable_observations {#observations-configuration}

Включает или отключает консолидацию наблюдений. При значении `false` консолидация в этом банке не выполняется ни автоматически, ни вручную. По умолчанию включено, если на сервере активирована функция наблюдений.

### enable_auto_consolidation

Определяет, выполняется ли консолидация автоматически после операций retain, delete и update. При значении `false` консолидация запускается только явным запросом к [конечной точке consolidate](../observations.md#trigger-consolidation). По умолчанию — `true`.

Полезно, если нужно полностью управлять временем консолидации, например сохранить много данных и объединить их позже или запустить [точечную консолидацию](../observations.md#targeted-consolidation) только для нужных областей.

### observations_mission

Определяет, во что банк должен сводить долговременные наблюдения. Полностью заменяет встроенные правила консолидации. Оставьте поле пустым, чтобы использовать настройки сервера.

```
Например: Наблюдения — устойчивые факты о людях и проектах.
           Всегда включай предпочтения, навыки и повторяющиеся закономерности.
           Игнорируй разовые события и временное состояние.
```

### consolidation_llm_batch_size

Количество фактов, отправляемых LLM за один вызов консолидации. Большие значения уменьшают количество обращений к LLM и повышают пропускную способность, но увеличивают размер запросов. Значение `1` отключает пакетную обработку. Если параметр не задан, используется значение сервера (`8`).

### consolidation_source_facts_max_tokens

Общий лимит токенов для исходных фактов, передаваемых вместе с наблюдениями в запросе на консолидацию. Эти факты помогают LLM сравнивать новые сведения с существующими наблюдениями. `-1` означает отсутствие ограничений. Если значение не задано, применяется параметр сервера (`-1`).

### consolidation_source_facts_max_tokens_per_observation

Лимит токенов исходных фактов для каждого наблюдения при консолидации. Каждое наблюдение получает не больше указанного количества токенов, чтобы одно наблюдение с множеством исходных фактов не израсходовало весь лимит. `-1` означает отсутствие ограничений. Если значение не задано, применяется параметр сервера (`256`).

См. [настройки наблюдений](../configuration.md#observations), где приведены переменные среды и значения по умолчанию.

### reflect_mission

Описание от первого лица, задающее идентичность и контекст для операции `reflect`. Агент использует его как опору для рассуждений и сохраняет единую точку зрения.

```
Например: Ты — помощник старшего инженера.
           Всегда основывай ответы на задокументированных решениях и их обоснованиях.
           Не спекулируй. Пиши прямо и точно.
```

### disposition_skepticism

Уровень скептицизма или доверчивости банка при оценке утверждений во время `reflect`. Шкала от 1 до 5.

### Python

```python
client.create_bank(bank_id="architect-bank")
client.update_bank_config(
    "architect-bank",
    reflect_mission="You're a senior software architect - keep track of system designs, "
            "technology decisions, and architectural patterns. Prefer simplicity over cutting-edge.",
    disposition_skepticism=4,   # Questions new technologies
    disposition_literalism=4,   # Focuses on concrete specs
    disposition_empathy=2,      # Prioritizes technical facts
)
```

### Node.js

```javascript
await client.createBank('architect-bank');
await client.updateBankConfig('architect-bank', {
    reflectMission: "You're a senior software architect - keep track of system designs, technology decisions, and architectural patterns.",
    dispositionSkepticism: 4,   // Questions new technologies
    dispositionLiteralism: 4,   // Focuses on concrete specs
    dispositionEmpathy: 2,      // Prioritizes technical facts
});
```

### CLI

```bash
hindsight bank create architect-bank \
  --mission "You're a senior software architect - keep track of system designs, technology decisions, and architectural patterns. Prefer simplicity over cutting-edge." \
  --skepticism 4 \
  --literalism 4 \
  --empathy 2
```

### Go

```go
# Section 'bank-with-disposition' not found in api/memory-banks.go
```

| Значение | Поведение |
|----------|-----------|
| `1` | Доверчивый — принимает сведения как есть |
| `3` *(по умолчанию)* | Сбалансированный |
| `5` | Скептичный — проверяет утверждения и сомневается в них |

### disposition_literalism

Определяет, насколько буквально интерпретировать сведения во время `reflect`. Шкала от 1 до 5.

| Значение | Поведение |
|----------|-----------|
| `1` | Гибкий — учитывает контекст и подтекст |
| `3` *(по умолчанию)* | Сбалансированный |
| `5` | Буквальный — воспринимает слова точно по смыслу |

### disposition_empathy

Определяет, насколько учитывать эмоциональный контекст при рассуждении во время `reflect`. Шкала от 1 до 5.

| Значение | Поведение |
|----------|-----------|
| `1` | Отстранённый — сосредоточен на фактах и логике |
| `3` *(по умолчанию)* | Сбалансированный |
| `5` | Эмпатичный — учитывает эмоциональный контекст |

> **ℹ️ Сведения**
>
> Черты disposition и `reflect_mission` влияют только на операцию `reflect`. `retain_mission` и `observations_mission` — отдельные настройки для других операций.

### mcp_enabled_tools

Список разрешённых имён инструментов MCP для этого банка. Если значение задано, вызывать можно только перечисленные инструменты; вызов любого другого вернёт ошибку. Для совместимости с протоколом все инструменты всё равно остаются в списке MCP. Укажите `null` или не задавайте параметр, чтобы разрешить все инструменты.

```json
["recall", "reflect"]
```

Доступные инструменты: `retain`, `recall`, `reflect`, `list_banks`, `create_bank`, `list_mental_models`, `get_mental_model`, `create_mental_model`, `update_mental_model`, `delete_mental_model`, `refresh_mental_model`, `list_directives`, `create_directive`, `delete_directive`, `list_memories`, `get_memory`, `list_documents`, `get_document`, `delete_document`, `list_operations`, `get_operation`, `cancel_operation`, `list_tags`, `get_bank`, `get_bank_stats`, `update_bank`, `delete_bank`, `clear_memories`.

### llm_gemini_safety_settings

Задаёт пороги фильтрации содержимого для поставщиков Gemini и Vertex AI. Принимает список объектов настроек безопасности в [формате Google AI](https://ai.google.dev/api/generate-content#v1beta.SafetySetting). Если значение равно `null` (по умолчанию), используются встроенные параметры безопасности Gemini.

```json
[
  {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_NONE"},
  {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_NONE"}
]
```

Действует, только если `HINDSIGHT_API_LLM_PROVIDER` имеет значение `gemini` или `vertexai`.

### recall_budget_function {#recall-budget-configuration}

Определяет, как параметр `budget` запроса [`recall`](./recall) (`low`, `mid` или `high`) преобразуется во внутреннее целое число `thinking_budget`, используемое всеми методами поиска: семантическим, BM25, графовым и временным. Доступны два способа:

| Способ | Поведение |
|--------|-----------|
| `fixed` *(по умолчанию)* | `thinking_budget = recall_budget_fixed_<level>` — не зависит от `max_tokens`. Сохраняет прежнее поведение. |
| `adaptive` | `thinking_budget = round(max_tokens * recall_budget_adaptive_<level>)`, значение ограничено диапазоном `[recall_budget_min, recall_budget_max]`. Ширина поиска растёт вместе с размером запрошенного ответа. |

```json
{
  "recall_budget_function": "adaptive",
  "recall_budget_adaptive_low": 0.05,
  "recall_budget_adaptive_mid": 0.1,
  "recall_budget_adaptive_high": 0.3,
  "recall_budget_min": 30,
  "recall_budget_max": 1500
}
```

### recall_budget_fixed_low / recall_budget_fixed_mid / recall_budget_fixed_high

Если `recall_budget_function` имеет значение `fixed` (по умолчанию), эти положительные целые числа напрямую задают лимит поиска для каждого метода на каждом уровне `budget`. Значения по умолчанию: `100` / `300` / `1000` — они точно повторяют прежнее соответствие.

### recall_budget_adaptive_low / recall_budget_adaptive_mid / recall_budget_adaptive_high

Если `recall_budget_function` имеет значение `adaptive`, эти положительные коэффициенты умножаются на `max_tokens` запроса и задают лимит поиска для каждого метода. Значения по умолчанию: `0.025` / `0.075` / `0.25`; они примерно соответствуют фиксированным значениям при `max_tokens = 4096`.

### recall_budget_min / recall_budget_max

Нижняя и верхняя границы результата адаптивной функции после умножения на коэффициент. Оба значения должны быть положительными целыми числами, `min ≤ max`. По умолчанию: `20` / `2000`.

См. [соответствие лимита Recall](../configuration.md#recall-budget-mapping), где приведены переменные среды и значения по умолчанию.

### memory_defense {#memory_defense}

Политика Memory Defense для конкретного банка. По умолчанию отсутствует, то есть Memory Defense для этого банка отключена.

| Поле | Тип | По умолчанию | Описание |
|------|-----|--------------|----------|
| `enabled` | bool | `false` | Главный переключатель. |
| `default_action` | `allow`\|`redact`\|`quarantine`\|`block` | `allow` | Действие, если ни одно правило не подошло. |
| `protected_tag_namespaces` | `list[str]` | `[]` | При записи тегов из этих пространств имён (`ns:*`) применяется детектор `protected_key`. |
| `immutable_tag_namespaces` | `list[str]` | `[]` | Запись в эти пространства имён блокируется. |
| `rules` | `list[Rule]` | `[]` | Соответствие детекторов действиям (см. ниже). |
| `detector_overrides` | `dict` | `{}` | Настройка отдельных детекторов, например `size_anomaly.max_size`. |

Структура `Rule`:

| Поле | Обязательное | Описание |
|------|--------------|----------|
| `on` | да | Имя детектора (`prompt_injection`, `sensitive_data`, `protected_key`, `immutable_key`, `size_anomaly`) или `*` для любого детектора. |
| `action` | да | Одно из значений `allow`, `redact`, `quarantine`, `block`. |
| `min_severity` | нет | Минимальная серьёзность (`low`, `medium`, `high`, `critical`) для срабатывания правила. По умолчанию `low`. |

Недопустимая политика при запросе PATCH отклоняется с HTTP 422.

Примеры использования см. в [руководстве Memory Defense](../memory-defense/index.md).

---

## Обновление конфигурации

Настройки банка (миссия retain, режим извлечения, миссия наблюдений и прочее) меняются через **отдельный API конфигурации**, а не через вызов `create_bank`. Это позволяет менять рабочие параметры отдельно от идентификатора и disposition банка.

### Установка переопределений конфигурации

### Python

```python
client.update_bank_config(
    "my-bank",
    retain_mission="Always include technical decisions, API design choices, and architectural trade-offs. Ignore meeting logistics and social exchanges.",
    retain_extraction_mode="verbose",
    observations_mission="Observations are stable facts about people and projects. Always include preferences, skills, and recurring patterns. Ignore one-off events.",
    disposition_skepticism=4,
    disposition_literalism=4,
    disposition_empathy=2,
)
```

### Node.js

```javascript
await client.updateBankConfig('my-bank', {
    retainMission: 'Always include technical decisions, API design choices, and architectural trade-offs. Ignore meeting logistics and social exchanges.',
    retainExtractionMode: 'verbose',
    observationsMission: 'Observations are stable facts about people and projects. Always include preferences, skills, and recurring patterns. Ignore one-off events.',
    dispositionSkepticism: 4,
    dispositionLiteralism: 4,
    dispositionEmpathy: 2,
});
```

### CLI

```bash
hindsight bank set-config my-bank \
  --retain-mission "Always include technical decisions, API design choices, and architectural trade-offs. Ignore meeting logistics and social exchanges." \
  --retain-extraction-mode verbose \
  --observations-mission "Observations are stable facts about people and projects. Always include preferences, skills, and recurring patterns. Ignore one-off events." \
  --disposition-skepticism 4 \
  --disposition-literalism 4 \
  --disposition-empathy 2
```

### Go

```go
# Section 'update-bank-config' not found in api/memory-banks.go
```

Можно обновить любую часть настроек: изменятся только переданные ключи.

### Чтение текущей конфигурации

### Python

```python
# Получает итоговую конфигурацию (параметры сервера с настройками банка) и необработанные переопределения
data = client.get_bank_config("my-bank")
# data["config"]     — полная итоговая конфигурация
# data["overrides"]  — только поля, переопределённые на уровне банка
```

### Node.js

```javascript
// Получает итоговую конфигурацию (параметры сервера с настройками банка) и необработанные переопределения
const { config, overrides } = await client.getBankConfig('my-bank');
// config    — полная итоговая конфигурация
// overrides — только поля, переопределённые на уровне банка
```

### CLI

```bash
# Получает итоговую конфигурацию (параметры сервера с настройками банка)
hindsight bank config my-bank

# Показывает только переопределения для этого банка
hindsight bank config my-bank --overrides-only
```

### Go

```go
# Section 'get-bank-config' not found in api/memory-banks.go
```

В ответе отдельно указаны:

- **`config`** — итоговая конфигурация с учётом настроек сервера и переопределений банка.
- **`overrides`** — только поля, явно переопределённые для этого банка.

### Сброс к настройкам по умолчанию

### Python

```python
# Удаляет все переопределения на уровне банка и возвращает настройки сервера
client.reset_bank_config("my-bank")
```

### Node.js

```javascript
// Удаляет все переопределения на уровне банка и возвращает настройки сервера
await client.resetBankConfig('my-bank');
```

### CLI

```bash
# Удаляет все переопределения на уровне банка и возвращает настройки сервера
hindsight bank reset-config my-bank -y
```

### Go

```go
# Section 'reset-bank-config' not found in api/memory-banks.go
```

Удаляются все переопределения банка. Применяются настройки сервера, заданные переменными среды.

Конфигурацию можно менять и в интерфейсе Control Plane: откройте банк и перейдите на вкладку **Configuration**.

---

## Директивы

Директивы — строгие правила, которым агент обязан следовать при выполнении операций [reflect](./reflect). В отличие от disposition, влияющего на **стиль** рассуждений, директивы — это конкретные инструкции, соблюдение которых обязательно.

> **ℹ️ Сведения**
>
> Директивы влияют только на операцию `reflect`. Они добавляются в запросы, и агент обязан соблюдать их во всех ответах.

### Когда использовать директивы

Используйте директивы для правил, которые нельзя нарушать:

- **Язык и стиль:** «Всегда отвечай на формальном английском».
- **Конфиденциальность:** «Никогда не передавай персональные данные третьим лицам».
- **Ограничения предметной области:** «Отдавай предпочтение консервативным инвестиционным рекомендациям».
- **Правила поведения:** «Всегда указывай источники, когда приводишь утверждения».

### Создание директив

### Python

```python
# Создаёт директиву — обязательное правило для reflect
directive = client.create_directive(
    bank_id=BANK_ID,
    name="Formal Language",
    content="Always respond in formal English, avoiding slang and colloquialisms."
)

print(f"Created directive: {directive.id}")
```

### Node.js

```javascript
// Создаёт директиву — обязательное правило для reflect
const directive = await client.createDirective(
    BANK_ID,
    'Formal Language',
    'Always respond in formal English, avoiding slang and colloquialisms.'
);

console.log(`Created directive: ${directive.id}`);
```

### CLI

```bash
# Создаёт директиву — обязательное правило для reflect
hindsight directive create "$BANK_ID" \
  "Formal Language" \
  "Always respond in formal English, avoiding slang and colloquialisms."
```

### Go

```go
# Section 'create-directive' not found in api/directives.go
```

### Просмотр директив

### Python

```python
# Выводит все директивы банка
directives = client.list_directives(bank_id=BANK_ID)

for d in directives.items:
    print(f"- {d.name}: {d.content[:50]}...")
```

### Node.js

```javascript
// Выводит все директивы банка
const directives = await client.listDirectives(BANK_ID);

for (const d of directives.items) {
    console.log(`- ${d.name}: ${d.content.slice(0, 50)}...`);
}
```

### CLI

```bash
# Выводит все директивы банка
hindsight directive list "$BANK_ID"
```

### Go

```go
# Section 'list-directives' not found in api/directives.go
```

### Обновление директив

### Python

```python
# Обновляет директиву, например отключает её без удаления
updated = client.update_directive(
    bank_id=BANK_ID,
    directive_id=directive_id,
    is_active=False
)

print(f"Directive active: {updated.is_active}")
```

### Node.js

```javascript
// Обновляет директиву, например отключает её без удаления
const updated = await client.updateDirective(BANK_ID, directiveId, {
    isActive: false
});

console.log(`Directive active: ${updated.is_active}`);
```

### CLI

```bash
# Section 'update-directive' not found in api/directives.sh
```

### Go

```go
# Section 'update-directive' not found in api/directives.go
```

### Удаление директив

### Python

```python
# Удаляет директиву
client.delete_directive(
    bank_id=BANK_ID,
    directive_id=directive_id
)
```

### Node.js

```javascript
// Удаляет директиву
await client.deleteDirective(BANK_ID, directiveId);
```

### CLI

```bash
# Section 'delete-directive' not found in api/directives.sh
```

### Go

```go
# Section 'delete-directive' not found in api/directives.go
```

### Директивы и disposition

| Характеристика | Директивы | Disposition |
|----------------|-----------|-------------|
| **Суть** | Обязательные правила | Мягкое влияние на стиль рассуждения |
| **Применение** | Строгое: ответ отклоняется при нарушении | Гибкое: влияет на трактовку |
| **Назначение** | Соответствие требованиям, защитные правила, ограничения | Личность, характер и тон |
| **Пример** | «Никогда не рекомендуй конкретные акции» | Высокий скептицизм: проверять утверждения |

---

## Экспорт и импорт документов

Переносите документы **вместе с уже извлечёнными фактами** между банками, не запуская LLM повторно. Это полезно для проверки другой модели векторных представлений или переноса данных между банками и экземплярами без повторной оплаты извлечения. Архив содержит документы, исходные фрагменты и извлечённые факты (сущности по каноническим именам и причинно-следственные связи), но **не содержит векторные представления и идентификаторы базы данных**. При импорте факты заново векторизуются моделью целевого банка, а сущности и связи пересчитываются в его контексте. Импортированные документы связываются с уже имеющимися в целевом банке данными.

### Экспорт документов

`GET /v1/default/banks/{bank_id}/document-transfer` — синхронный запрос; передаёт ZIP-архив потоком.

```bash
# Весь банк
curl -H "Authorization: Bearer $API_KEY" \
  "$HINDSIGHT_URL/v1/default/banks/my-bank/document-transfer" -o my-bank.zip

# Выбранные документы, включая консолидированные наблюдения
curl -H "Authorization: Bearer $API_KEY" \
  "$HINDSIGHT_URL/v1/default/banks/my-bank/document-transfer?document_id=doc-1&include_observations=true" -o subset.zip
```

| Параметр запроса | Описание |
|------------------|----------|
| `document_id` | Можно указать несколько раз. Экспортирует только перечисленные документы; без параметра экспортируется весь банк. |
| `include_observations` | Дополнительно экспортирует консолидированные наблюдения (по умолчанию `false`). Допустимо только при экспорте **всего банка**: вместе с `document_id` вернёт `400`. |

### Импорт документов

`POST /v1/default/banks/{bank_id}/document-transfer` — загрузка ZIP в multipart-запросе (поле `file`). Импорт выполняется как **фоновая операция**: векторизация и сопоставление сущностей могут занять время. Запрос возвращает `202` и `operation_id`. Проверяйте состояние через конечную точку операций банка; итоговые счётчики будут в `result_metadata`.

```bash
curl -H "Authorization: Bearer $API_KEY" -F "file=@my-bank.zip" \
  "$HINDSIGHT_URL/v1/default/banks/other-bank/document-transfer?on_conflict=replace"
# -> {"operation_id": "…", "status": "pending"}

curl -H "Authorization: Bearer $API_KEY" \
  "$HINDSIGHT_URL/v1/default/banks/other-bank/operations/$OPERATION_ID"
# -> {"status":"completed","result_metadata":{"documents_imported":3,"facts_imported":42,"observations_imported":5,...}}
```

Параметр `on_conflict` определяет, что делать, если идентификатор документа уже есть в целевом банке:

| Режим | Поведение |
|-------|-----------|
| `skip` (по умолчанию) | Оставить существующий документ без изменений. |
| `replace` | Удалить данные существующего документа и импортировать его заново. |
| `new-id` | Импортировать копию с новым сгенерированным идентификатором. |

### Наблюдения

По умолчанию консолидированные наблюдения не экспортируются: целевой банк создаёт их заново из импортированных фактов при консолидации. Чтобы перенести их, укажите `include_observations=true`. Они восстанавливаются без обращения к LLM, ссылки на исходные факты переназначаются импортированным фактам, которые отмечаются как консолидированные, чтобы целевой банк не объединил их повторно.

Поскольку одно наблюдение может быть основано на фактах из нескольких документов, `include_observations` разрешён только при экспорте **всего банка** (не указывайте `document_id`). Сочетание с выборочным экспортом вернёт `400`.

> **⚠️ Импортированные наблюдения добавляются как есть — без объединения**
>
> Они не объединяются и не удаляются как дубликаты относительно наблюдений, уже сохранённых в целевом банке. При консолидации похожие наблюдения объединяются, при импорте — нет. Лучше импортировать наблюдения в новый или пустой банк либо не указывать `include_observations` и дать целевому банку консолидировать импортированные факты самостоятельно.

### Включение и отключение

Обе конечные точки управляются флагами на уровне сервера (по умолчанию `true`). Если конечная точка отключена, она возвращает `404`. В `/version` состояние указано в `features.document_export_api` и `features.document_import_api`; интерфейс Control Plane соответственно скрывает кнопки.

| Переменная | Управляет |
|------------|----------|
| `HINDSIGHT_API_ENABLE_DOCUMENT_EXPORT_API` | `GET …/document-transfer` |
| `HINDSIGHT_API_ENABLE_DOCUMENT_IMPORT_API` | `POST …/document-transfer` |

## Перенос банка на новый экземпляр

Чтобы перенести банк на экземпляр с другой **моделью векторизации**, **векторным расширением** или **механизмом полнотекстового поиска** — изменить их для заполненного банка на месте нельзя, — экспортируйте весь банк и импортируйте его в новый экземпляр. Там все векторные представления и индексы будут построены заново по сохранённому тексту, **без повторного извлечения LLM**. Переносятся документы, факты, наблюдения, настройки банка, ментальные модели, директивы и вебхуки; векторные представления не переносятся.

Используйте команды `hindsight-admin export-bank` и `import-bank`, а затем выполните инструкцию Blue-Green: **[Admin CLI → перенос банка на новый экземпляр](../admin-cli.md#migrating-a-bank-to-a-new-instance)**.
