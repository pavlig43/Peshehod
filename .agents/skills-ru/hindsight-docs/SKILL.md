---
name: hindsight-docs
description: Полная документация Hindsight для ИИ-агентов. Помогает изучить архитектуру, API, настройку и советы по работе с Hindsight.
---

# Документация Hindsight

Полная техническая документация Hindsight — системы памяти для ИИ-агентов, устроенной по образцу биологической памяти.

## Когда применять этот навык

Он поможет, если нужно:
- Разобраться в архитектуре Hindsight и ключевых понятиях
- Изучить операции retain/recall/reflect
- Настроить банки памяти и черты поведения
- Запустить сервер API Hindsight через Docker, Kubernetes или pip
- Подключить SDK для Python, Node.js или Rust
- Разобраться в способах поиска: семантическом, BM25, по графу и по времени
- Найти причину сбоя или ускорить работу
- Проверить адреса и параметры API
- Найти готовые примеры и рецепты

## Структура документации

Вся документация лежит в `references/` и разбита по темам:

```
references/
├── best-practices.md # START HERE — missions, tags, formats, anti-patterns
├── faq.md            # Common questions and decisions
├── changelog/        # Release history and version changes (index.md + integrations/)
├── openapi.json      # Full OpenAPI spec — endpoint schemas, request/response models
├── developer/
│   ├── api/          # Core operations: retain, recall, reflect, memory banks
│   └── *.md          # Architecture, configuration, deployment, performance
├── sdks/
│   ├── *.md          # Python, Node.js, CLI, embedded
│   └── integrations/ # LiteLLM, AI SDK, OpenClaw, MCP, skills
└── cookbook/
    ├── recipes/      # Usage patterns and examples
    └── applications/ # Full application demos
```

## Как найти нужный раздел

### 1. Ищите файлы по шаблону (инструмент Glob)

```bash
# Core API operations
references/developer/api/*.md

# SDK documentation
references/sdks/*.md
references/sdks/integrations/*.md

# Cookbook examples
references/cookbook/recipes/*.md
references/cookbook/applications/*.md

# Find specific topics
references/**/configuration.md
references/**/*python*.md
references/**/*deployment*.md
```

### 2. Ищите текст (инструмент Grep)

```bash
# Search for concepts
pattern: "disposition"        # Memory bank configuration
pattern: "graph retrieval"    # Graph-based search
pattern: "helm install"       # Kubernetes deployment
pattern: "document_id"        # Document management
pattern: "HINDSIGHT_API_"     # Environment variables

# Search in specific areas
path: references/developer/api/
pattern: "POST /v1"           # Find API endpoints

path: references/cookbook/
pattern: "def |async def "    # Find Python examples
```

### 3. Читайте нужный файл целиком (инструмент Read)

```
references/developer/api/retain.md
references/sdks/python.md
references/cookbook/recipes/per-user-memory.md
```

## С чего начать: советы по работе

Перед описанием API прочитайте руководство по работе с системой. В нём есть правила для задач, тегов, формата текста, областей наблюдений и разбор частых ошибок. Это самый быстрый путь к верному подключению.

```
references/best-practices.md
```

## Ключевые понятия

- **Банки памяти (Memory Banks)**: изолированные хранилища памяти (по одному на пользователя или агента)
- **Retain**: сохраняет воспоминания, сам извлекает факты, сущности и связи
- **Recall**: находит воспоминания четырьмя параллельными способами: семантический поиск, BM25, поиск по графу и по времени
- **Reflect**: рассуждает на основе воспоминаний с учётом черт поведения
- **document_id**: объединяет сообщения беседы; повторная запись с тем же ID обновляет документ
- **Черты поведения (Dispositions)**: скептицизм, буквальность и эмпатия по шкале 1–5; влияют на reflect
- **Ментальные модели (Mental Models)**: обобщённые знания, собранные из фактов

## Примечания

- Примеры кода взяты из рабочих примеров
- Для настройки служат переменные среды `HINDSIGHT_API_*`
- При запуске система сама выполняет миграции базы данных
- Запросы к нескольким банкам нужно собирать на стороне клиента
- Для развития беседы задавайте `document_id` (тот же ID обновит запись)

---

**Создано автоматически** из `hindsight-docs/docs/`. Для обновления запустите `./scripts/generate-docs-skill.sh`.
