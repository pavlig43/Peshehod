

<PageHero title="Советы по работе" subtitle="Правила для агентов и разработчиков, которые подключают память Hindsight к рабочим системам." />

**Содержание**
- [Основные понятия](#core-concepts) — банки, виды данных и памяти
- [Настройка банка](#bank-configuration) — задачи, черты поведения, метки сущностей
- [Запись данных](#retaining-data) — формат текста, контекст, document_id, [теги](#tags-naming-conventions), области наблюдений
- [Поиск воспоминаний](#recalling-memories) — бюджет, фильтры по тегам и меткам сущностей, параметры include, query_timestamp
- [Рассуждение](#reflecting) — recall и reflect, response_schema, аудит
- [Ментальные модели](#mental-models) — когда создавать, как выбирать теги и обновлять
- [Частые ошибки](#anti-patterns)

---

<a id="core-concepts"></a>
## Основные понятия

### Банки памяти

**Банк памяти** — отдельное хранилище, которое разделяет пользователей, агентов или контексты. Все операции (retain, recall, reflect) обращаются к одному банку. Банки не делятся данными.

- В приложениях с несколькими пользователями чаще всего заводят по банку на каждого пользователя.
- Для долгой памяти отдельного агента часто заводят по банку на агента.
- Для анализа данных разных пользователей может подойти общий банк с тегами (см. [«Теги»](#tags-naming-conventions)).

При первом обращении банк создаётся сам. Настройте его до загрузки данных, чтобы задать нужное поведение.

---

### Виды операций и данных

| Операция | Что делает | Когда вызывать |
|-----------|-------------|-----------------|
| **Retain** | Принимает исходный текст (беседы, документы, заметки). LLM извлекает факты, сущности и связи; исходный текст не хранится дословно. | После каждого хода беседы или в конце сеанса |
| **Recall** | Находит нужные воспоминания четырьмя способами: по смыслу, BM25, графу и времени. Возвращает список фактов по убыванию полезности. | Перед ответом, которому поможет прошлый контекст |
| **Reflect** | Самостоятельно ищет память, обобщает её и сразу возвращает ответ. Сначала берёт ментальные модели и наблюдения. | Когда Hindsight должен ответить на вопрос, а не лишь найти факты |
| **Наблюдения** | Знания из нескольких фактов, очищенные от повторов и связанные с источниками. Для каждого хранятся точные цитаты исходных воспоминаний и число подтверждений. Новые факты уточняют наблюдение, а не стирают его. | Создаются сами после retain, но не внутри вызова retain |
| **Ментальные модели** | Заранее созданные ответы reflect для частых запросов. Возвращаются сразу и одинаково. | Для частых запросов с высокой нагрузкой или профилей пользователя, которые меняются редко |

---

### Виды памяти

Факты, извлечённые через retain, делятся на три вида:

| Вид | Описание | Пример |
|------|-------------|---------|
| `world` | Общие знания и внешние факты | «Эйфелева башня находится в Париже» |
| `experience` | Личные события и факты о пользователе | «Пользователь переехал в Берлин в 2024 году» |
| `observation` | Обобщённый вывод из нескольких фактов, очищенный от повторов и обновляемый с учётом источников | «Пользователь постоянно предпочитает асинхронное общение (пять подтверждений)» |

Параметр `types` в recall помогает искать только нужные виды памяти.

---

<a id="bank-configuration"></a>
## Настройка банка

Настройте банк до первого вызова, чтобы память работала под вашу задачу. Ошибки в задаче банка — главная причина слабых воспоминаний.

### Как писать задачи

Все три задачи задаются обычным языком. Пишите конкретно: расплывчатая задача даёт расплывчатый результат.

#### `retain_mission`

Добавляется к запросу на извлечение фактов. Говорит LLM, что извлекать и что пропускать.

| Оценка | Пример |
|---------|---------|
| **Хорошо** | `Always extract technical decisions, API design choices, architectural trade-offs, blockers, and error messages. Ignore greetings, small talk, and scheduling logistics.` |
| **Хорошо** | `Extract personal preferences, ongoing commitments, deadlines, health info, and relationship details. Ignore filler phrases and pleasantries.` |
| **Плохо** | `Extract all information` — слишком расплывчато; попадёт лишнее |
| **Плохо** | `Be helpful` — не указывает, какие факты извлекать |

**Советы:**
- Перечислите нужные *виды* фактов: предпочтения, решения, ошибки, обязательства.
- Назовите, что *пропускать*: это столь же важно, как и список нужного.
- Подстройте задачу под ваши данные: беседы, документы или обращения.

#### `observations_mission`

Указывает, какие закономерности искать при обобщении после retain.

```
Identify evolving preferences, recurring patterns, behavioral shifts, and contradictions
with prior knowledge. Focus on durable patterns — not transient states. Highlight when
user behavior contradicts previous observations.
```

**Советы:**
- Просите искать устойчивые закономерности, чтобы не плодить наблюдения о кратких состояниях.
- Если нужна история изменений, прямо попросите выявлять противоречия.
- Учитывайте, как часто ожидаемые закономерности могут меняться.

#### `reflect_mission`

Задаёт роль агента и ход рассуждения в операции `reflect`.

| Сфера | Задача |
|----------|---------|
| Помощник для кода | `You are a senior developer helping optimize the user's workflow. Always factor in past technical decisions, current project context, and stated preferences. Be direct and opinionated.` |
| Поддержка клиентов | `You are a support agent with full context of this customer's history. Reference past tickets and resolutions where relevant. Be concise and solution-focused.` |
| Личный помощник | `You are a personal assistant who remembers everything important to the user. Personalize every response using what you know about their preferences, schedule, and ongoing projects.` |
| Помощник по здоровью | `You are a health assistant. Reference the user's history accurately. Always recommend consulting a professional for medical decisions. Do not speculate.` |

---

### Черты поведения

Черты влияют лишь на `reflect`, но не на `recall`. Шкала — от 1 до 5.

| Черта | 1 | 5 |
|-------|---|---|
| `skepticism` | Принимает все воспоминания без сомнений | Проверяет противоречия, отмечает неточные сведения |
| `literalism` | Вольно трактует слова, угадывает намерение | Читает строго по смыслу слов, не делает догадок |
| `empathy` | Сухой, нейтральный тон | Тёплый, личный тон с учётом чувств |

**Примеры настроек:**

| Вид агента | Скептицизм | Буквальность | Эмпатия |
|------------|------------|------------|---------|
| Проверка кода | 4 | 5 | 1 |
| Поддержка клиентов | 2 | 3 | 4 |
| Личный помощник | 2 | 2 | 4 |
| Помощник по здоровью | 5 | 4 | 3 |
| Помощник для исследований | 4 | 4 | 2 |

---

### Метки сущностей

Задайте строгий набор значений для группировки. LLM извлечёт значения и приведёт их к этому набору.

```json
{
  "entity_labels": [
    {
      "key": "tech_stack",
      "type": "multi-values",
      "values": [
        {"value": "python", "description": "Python programming language"},
        {"value": "typescript", "description": "TypeScript / Node.js"},
        {"value": "react", "description": "React frontend framework"}
      ]
    },
    {
      "key": "priority",
      "type": "value",
      "tag": true,
      "values": [
        {"value": "high", "description": "Urgent or blocking"},
        {"value": "low", "description": "Nice to have"}
      ]
    }
  ]
}
```

- **`type: "value"`** — одно значение у каждой сущности; новая запись заменяет старую.
- **`type: "multi-values"`** — накапливает несколько значений.
- **`tag: true`** — найденные значения меток также добавляются как теги, чтобы по ним можно было искать.

Применяйте метки сущностей, когда важна единая группировка: термины вашей сферы, статусы, уровни важности, виды участия.

---

<a id="retaining-data"></a>
## Запись данных

### Формат текста

Передавайте самый полный доступный вариант. Не делайте сводку заранее.

| Формат | Совет |
|--------|---------------|
| Массив беседы в JSON | **Лучший вариант** для бесед: сохраняет структуру, роли и связи |
| Обычный текст с префиксами | Подходит: по строке вида `[ISO-timestamp] role: text` |
| Markdown / HTML / обычный текст | Подходит для документов и заметок |
| Заранее сделанная сводка | **Избегайте**: теряет связи сущностей, указания на время и структуру |

**Беседа в JSON (лучший вариант):**

```json
[
  {"role": "user",      "content": "I'm using React for the frontend.", "timestamp": "2025-06-01T10:30:00Z"},
  {"role": "assistant", "content": "Got it. What state management are you using?"},
  {"role": "user",      "content": "Zustand. We moved away from Redux last quarter."}
]
```

**Почему не стоит заранее делать сводку:** LLM извлекает факты, сущности и связи из структуры. Сводка вроде «пользователь применяет React и Zustand» теряет указание на время («в прошлом квартале»), связи сущностей (React ↔ интерфейс, Redux ↔ переход) и причину изменения (ушли от Redux).

---

### Поле `context`

Сильно влияет на качество извлечения. Задавайте его всегда. Оно описывает *вид и источник* текста.

```python
# Good — specific, descriptive
context="Customer support ticket #12345 from user Alice about a billing discrepancy"
context="Developer's architecture review session for the payments service"
context="User's onboarding form: stated goals, current tools, and team size"
context="Weekly standup notes: blockers, progress, and upcoming tasks"

# Bad — generic, adds no signal
context="some data"
context="conversation"
# Omitted entirely — extraction uses no context
```

---

### Поле `document_id`

Нужно для обновления документа: тот же `document_id` удаляет прошлую версию и запускает обработку заново.

**Правила:**
- Берите постоянные, понятные ID: сеанса, обращения или UUID документа.
- Для растущей беседы всегда берите один ID и на каждом шаге заново передавайте беседу целиком.
- **Не** создавайте новый случайный UUID на каждый вызов retain: это даст повторы.

```python
# Good — stable session ID
client.retain(bank_id="user-alice", items=[{
    "content": full_conversation,
    "document_id": f"session-{session_id}",
}])

# Bad — new random ID every call = duplicates
client.retain(bank_id="user-alice", items=[{
    "content": full_conversation,
    "document_id": str(uuid.uuid4()),  # ❌ creates a new document each time
}])
```

---

### Поле `timestamp`

Задавайте, если известен момент события. Так Hindsight сможет искать по времени.

- Формат ISO 8601: `"2025-06-01T10:32:00Z"`.
- Для беседы укажите время её *начала*.
- Если поле пропустить, поиск по времени вовсе не сможет учитывать дату.

---

<a id="tags-naming-conventions"></a>
### Теги: как давать им имена

Теги задают область видимости. Воспоминание с тегом `user:alice` возвращается для recall/reflect лишь при наличии `user:alice` в фильтре `tags` и строгом сравнении.

**Принятые схемы имён:**

| Шаблон | Пример | Для чего |
|---------|---------|---------|
| `user:<id>` | `user:alice`, `user:u_123` | Разделение по пользователям |
| `session:<id>` | `session:s_abc` | Память в пределах сеанса |
| `team:<name>` | `team:engineering` | Общие знания команды |
| `topic:<name>` | `topic:billing`, `topic:technical` | Отбор по теме |
| `scope:<name>` | `scope:private`, `scope:public` | Уровни доступа |

**Минимум для нескольких пользователей:** каждый retain с данными пользователя должен иметь хотя бы тег `user:<id>`. Без него воспоминание будет видно всем.

```python
# Multi-tenant retain — always tag with user ID
items=[{
    "content": conversation,
    "tags": ["user:alice", "session:s_abc", "topic:billing"],
    "document_id": f"session-{session_id}",
}]
```

---

### Схема метаданных

Нужна для связи с источниками. **По метаданным нельзя фильтровать**; для этого берите теги.

```python
# Source tracking
metadata={"source": "slack", "channel": "#engineering", "thread_id": "T123456"}

# Ticket linking
metadata={"ticket_id": "JIRA-123", "priority": "high", "reporter": "alice"}

# Document provenance
metadata={"url": "https://...", "section": "pricing-faq", "version": "2025-Q1"}
```

Метаданные возвращаются с каждым найденным воспоминанием. По ним можно открыть источник в интерфейсе, сделать прямую ссылку или сохранить историю для аудита.

---

### Области наблюдений

Задают, для каких наборов тегов нужен отдельный проход при создании наблюдений.

| Значение | Поведение | Когда брать |
|-------|----------|------------|
| `"combined"` | Один проход со всеми тегами вместе | По умолчанию; банки одного пользователя и общие задачи |
| `"per_tag"` | Отдельный проход для каждого тега | Наблюдения о поведении разных пользователей должны быть разделены |
| `"all_combinations"` | Все возможные подмножества тегов | Сложный многомерный анализ (дорого) |
| Свой список | Явно заданные области | Точное разделение нескольких пользователей |

**Пример своих областей (советуем для нескольких пользователей):**

```python
# Observations scoped to: user-level, team-level, and combined
observation_scopes=[
    ["user:alice"],
    ["team:engineering"],
    ["user:alice", "team:engineering"],
]
```

---

### Синхронный и асинхронный режимы

| Режим | Когда брать |
|------|-------------|
| `async_=False` (по умолчанию) | Когда перед следующим шагом нужно получить подтверждение |
| `async_=True` | Запись в конце хода или сеанса; действия пользователя, где важна скорость ответа |

Не вызывайте retain и recall в одном ходе: retain пишет данные, а извлечённые факты не появятся в поиске сразу.

---

<a id="recalling-memories"></a>
## Поиск воспоминаний

### Выбор бюджета

| Бюджет | Задержка | Когда брать |
|--------|---------|----------|
| `low` | 50–100 мс | Поиск простого факта, вопрос с одной связью |
| `mid` | 100–300 мс | Вывод через несколько связей, вопросы о связях *(по умолчанию)* |
| `high` | 300–500 мс | Глубокий поиск, сложные связи между разными темами |

Начните с `mid`. В частых циклах агента берите `low`. Оставьте `high` для явного запроса пользователя на глубокий поиск.

---

### Режимы отбора по тегам

| Режим | Включает записи без тегов? | Условие |
|------|-------------------|-----------|
| `any` *(по умолчанию)* | Да | Совпал хотя бы один тег **или** тегов нет |
| `all` | Да | Есть все указанные теги **или** тегов нет |
| `any_strict` | Нет | Совпал хотя бы один тег |
| `all_strict` | Нет | Есть все указанные теги |

**Как выбрать:**
- Общие знания вместе с личными: `tags=["user:alice"], tags_match="any"` — вернёт воспоминания Алисы и общие записи без тегов.
- Полное разделение без утечек: `tags=["user:alice"], tags_match="any_strict"` — только воспоминания Алисы.
- Несколько условий «И»: `tags=["user:alice", "topic:billing"], tags_match="all_strict"` — лишь записи с обоими тегами.

**`tag_groups` для сложных фильтров:**

Группы тегов устроены как дерево: составные узлы `and`/`or`/`not` и конечные узлы `{"tags": [...], "match": "..."}`.

```python
# Alice's billing memories OR shared billing memories (no user tag)
recall(
    query="...",
    tag_groups=[
        {"or": [
            {"tags": ["user:alice", "topic:billing"], "match": "all_strict"},
            {"and": [
                {"tags": ["topic:billing"], "match": "any_strict"},
                {"not": {"tags": ["user:alice"], "match": "any_strict"}},
            ]},
        ]}
    ]
)
```

---

### Параметры `include`

| Параметр | По умолчанию | Когда включать |
|--------|---------|-------------|
| `include.entities` | Включён | Оставьте: даёт сведения о сущностях для поиска по графу |
| `include.chunks` | Выключен | Агенту нужна точная формулировка или цитата из источника |
| `include.source_facts` | Выключен | Для аудита нужно найти источники наблюдения |

---

### Фильтр `types`

| Значение | Что возвращает |
|-------|---------|
| *(не задано)* | Все виды |
| `["observation"]` | Лишь обобщённые закономерности; быстрее для общих вопросов |
| `["world", "experience"]` | Лишь исходные факты; для проверки фактов и запросов, где нужны цитаты |

---

### Отбор по форме воспоминания через метки сущностей

Если в одном банке есть похожие по смыслу воспоминания с разной целью (например, короткие правила и подробные шаги поиска сбоев), одной сортировки по сходству мало. Две записи о «точках входа» могут получить близкий балл, хотя одна — правило в строку, а другая — подробное руководство.

Через [метки сущностей](developer/api/memory-banks.md#entity-labels) с `tag: true` помечайте факты при retain и строго отбирайте их при recall.

**1. Задайте группу меток в банке:**

```json
{
  "entity_labels": [
    {
      "key": "memory_type",
      "description": "The type of knowledge: 'rule' for concise operating rules and canonical guidance, 'procedure' for step-by-step technical instructions and troubleshooting notes",
      "type": "value",
      "optional": false,
      "tag": true,
      "values": [
        { "value": "rule",      "description": "Concise operating rule or canonical guidance" },
        { "value": "procedure", "description": "Step-by-step technical instruction or troubleshooting note" }
      ]
    }
  ]
}
```

**2. Вызовите retain как обычно** — LLM сама отнесёт каждый факт к группе и добавит тег `memory_type:rule` или `memory_type:procedure`.

**3. Отберите при recall:**

```python
# Only rules — procedures are excluded at the database level, not post-filtered
result = client.recall(
    bank_id="my-bank",
    query="which entrypoint should I use?",
    tags=["memory_type:rule"],
    tags_match="any_strict"
)
```

Это строгое условие SQL WHERE для всех четырёх видов поиска. Лишние воспоминания даже не попадут в этап сортировки.

---

### `query_timestamp`

Задавайте для вопросов, где важно время. Так относительные слова о времени и оценка свежести будут отсчитываться от нужной даты.

```python
# "What was the team working on in January?"
recall(query="team priorities", query_timestamp="2025-01-31T23:59:59Z")

# Current context (most common)
recall(query="user preferences", query_timestamp=datetime.utcnow().isoformat() + "Z")
```

---

<a id="reflecting"></a>
## Рассуждение

### Recall и Reflect

| Берите `recall`, когда | Берите `reflect`, когда |
|-------------------|--------------------|
| Агент сам будет рассуждать над фактами | Hindsight должен рассудить и вернуть готовый ответ |
| Нужны прямые ссылки на факты | Нужен обобщённый ответ |
| Вы строите цепочку RAG | Нужен самостоятельный поиск в несколько шагов |
| Задержка важнее всего | Качество ответа важнее задержки |
| Нужно точное число фактов | Нужен ответ с учётом контекста и тонкостей |

---

### `response_schema`

Берите, когда нужен структурный результат для дальнейшей обработки в коде.

```python
reflect(
    query="What are the user's top 3 technical preferences?",
    response_schema={
        "type": "object",
        "properties": {
            "preferences": {
                "type": "array",
                "items": {"type": "string"},
                "maxItems": 3
            },
            "confidence": {"type": "number", "minimum": 0, "maximum": 1}
        },
        "required": ["preferences", "confidence"]
    }
)
# Returns: result.structured_output["preferences"], result.structured_output["confidence"]
```

---

### Аудит и поиск ошибок

| Параметр | Для чего |
|--------|---------|
| `include.facts=True` | Показывает, какие воспоминания и ментальные модели взяты для ответа; помогает проверить вывод |
| `include.tool_calls=True` | Даёт полную историю вызовов внутреннего поиска для поиска ошибок |

В рабочей среде включайте `include.facts` для аудита. `include.tool_calls` включайте лишь при разработке.

---

<a id="mental-models"></a>
## Ментальные модели

Ментальные модели — заранее созданные ответы `reflect` на частые вопросы. Они возвращаются сразу и без расхождений.

### Когда создавать

- Частые вопросы, для которых нужен один и тот же ответ.
- Агенты с большой нагрузкой, которым нужен ответ быстрее 100 мс.
- Профили пользователя или роли, читаемые при каждом запросе.
- Сводки знаний, которые человек проверил или одобрил.
- Данные между сеансами, которые меняются медленно: предпочтения, навыки, биография.

### Как брать теги

Теги ментальной модели определяют **и** какие воспоминания служат её основой, **и** какие вызовы recall/reflect могут её видеть.

```python
# Per-user mental model — uses Alice's memories (all_strict applied automatically during refresh)
create_mental_model(
    bank_id="shared-bank",
    name="Alice's Technical Profile",
    source_query="Summarize Alice's technical background, preferred stack, and current projects",
    tags=["user:alice"],
)

# Global mental model — uses all memories, visible to everyone
create_mental_model(
    bank_id="shared-bank",
    name="Team Engineering Standards",
    source_query="What are the team's agreed engineering standards and conventions?",
    # No tags — reads all memories, visible to all
)
```

### Как обновлять

| Условие | Когда брать |
|---------|------------|
| Вручную через API | После крупных обновлений данных или очередной проверки |
| `trigger={"refresh_after_consolidation": True}` | Когда наблюдения часто меняются и модель должна быть свежей |

Создавайте узкие модели — по одной на вид знаний. Модель «Всё о пользователе» почти бесполезна.

**Примеры деления моделей для личного помощника:**
- «Профиль пользователя» — общие сведения, предпочтения, цели.
- «Текущие проекты» — активные дела, сроки, помехи.
- «Технический набор» — языки, инструменты, библиотеки.
- «Стиль общения» — желаемая строгость тона и длина ответа.

---

<a id="anti-patterns"></a>
## Частые ошибки

| Ошибка | Чем мешает | Что делать |
|-------------|---------|-----|
| Сводка до retain | Теряются связи сущностей, указания на время и структура | Передавайте исходный текст; Hindsight сам извлечёт факты |
| Случайный UUID в `document_id` | При каждом retain появляется новый документ | Берите постоянный ID сеанса, обращения или документа |
| Нет поля `context` | Качество извлечения фактов сильно падает | Всегда пишите, что это за данные |
| Отбор по `metadata` | По метаданным нельзя фильтровать | Для отбора берите `tags` |
| Расплывчатые общие задачи | Извлекаются лишние факты, память теряет пользу | Уточните сферу, вид данных и что нужно пропускать |
| `tags_match="any"` в банке нескольких пользователей | Чужие воспоминания могут попасть в ответ | Для данных отдельных пользователей берите `any_strict` или `all_strict` |
| Retain и recall в одном запросе | Новые воспоминания ещё не попали в индекс | Пишите в конце хода, ищите в начале следующего |
| Одна ментальная модель для всего | Малая точность, долгое обновление, трудно задать область | Создайте по модели для каждого вида знаний |
| Бюджет `high` для каждого recall | Дорого, медленно и чаще всего лишнее | Для простого поиска берите `low`, обычно — `mid` |
| Нет `timestamp` при retain | Поиск по времени не работает | Всегда задавайте время из самого текста |
