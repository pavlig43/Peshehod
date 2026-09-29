---
sidebar_position: 4
---

# Справочник команд

Команды Hindsight дают доступ к операциям памяти и управлению банками из терминала. Все они следуют [схеме OpenAPI](../openapi.json). Чтобы увидеть доступные параметры команды, вызовите её с `--help`.

## Установка

```bash
curl -fsSL https://hindsight.vectorize.io/get-cli | bash
```

## Настройка

Задайте адрес API:

```bash
# Interactive configuration
hindsight configure

# Or set directly
hindsight configure --api-url http://localhost:8888

# With API key for authentication
hindsight configure --api-url http://localhost:8888 --api-key your-api-key

# Or use environment variables (highest priority)
export HINDSIGHT_API_URL=http://localhost:8888
export HINDSIGHT_API_KEY=your-api-key
```

### Именованные профили

Если нужно переключаться между несколькими средами Hindsight (местной,
тестовой, рабочей), не переписывая каждый раз `~/.hindsight/config`,
возьмите именованные профили. Каждый профиль — файл TOML по пути
`~/.hindsight/cli-profiles/<name>.toml`. Выбирайте его для отдельной
команды через `-p/--profile` либо задайте `$HINDSIGHT_PROFILE`.

```bash
# Create (or overwrite) a profile
hindsight profile create prod \
  --api-url https://api.hindsight.vectorize.io \
  --api-key hsk_...

# List and inspect profiles
hindsight profile list
hindsight profile show prod

# Use a profile for a single command
hindsight -p prod bank list

# Or make it sticky for the current shell
export HINDSIGHT_PROFILE=prod
hindsight bank list

# Remove a profile
hindsight profile delete prod -y
```

В Unix файлы профилей получают права `0600`, чтобы ключ API мог читать
лишь владелец.

**Приоритет настроек** (от высокого к низкому):

1. Переменные среды (`HINDSIGHT_API_URL`, `HINDSIGHT_API_KEY`).
2. Именованный профиль: явный `-p <name>` либо `$HINDSIGHT_PROFILE`.
3. Общий файл настроек (`~/.hindsight/config`, создаётся через `hindsight configure`).
4. Значение по умолчанию (`http://localhost:8888`).

`HINDSIGHT_API_URL` / `HINDSIGHT_API_KEY` всегда перекрывают значения профиля.
Поэтому в скриптах можно указывать `-p`, а в CI передавать учётные данные
через переменные среды.

## Основные команды

### Retain (сохранить память)

Сохранить одно воспоминание:

```bash
hindsight memory retain <bank_id> "Alice works at Google as a software engineer"

# With context
hindsight memory retain <bank_id> "Bob loves hiking" --context "hobby discussion"

# Queue for background processing
hindsight memory retain <bank_id> "Meeting notes" --async

# With an event date (ISO 8601 datetime or date)
hindsight memory retain <bank_id> "Project launched" --timestamp 2024-01-15

# Store without a timestamp (overrides the default of "now")
hindsight memory retain <bank_id> "Background fact" --timestamp unset
```

### Сохранить файлы

Загрузить сразу несколько файлов:

```bash
# Single file
hindsight memory retain-files <bank_id> notes.txt

# Directory (recursive by default)
hindsight memory retain-files <bank_id> ./documents/

# With context
hindsight memory retain-files <bank_id> meeting-notes.txt --context "team meeting"

# With a named retain strategy (see retain_strategies in bank config)
hindsight memory retain-files <bank_id> ./documents/ --strategy conversations

# Background processing
hindsight memory retain-files <bank_id> ./data/ --async
```

### Recall (поиск)

Искать воспоминания по смысловому сходству:

```bash
hindsight memory recall <bank_id> "What does Alice do?"

# With options
hindsight memory recall <bank_id> "hiking recommendations" \
  --budget high \
  --max-tokens 8192

# Filter by fact type
hindsight memory recall <bank_id> "query" --fact-type world,observation

# Filter by tags
hindsight memory recall <bank_id> "query" --tags work,project \
  --tags-match all

# Pin results to a specific time
hindsight memory recall <bank_id> "query" --query-timestamp "2026-01-15T00:00:00Z"

# Show trace information
hindsight memory recall <bank_id> "query" --trace
```

### Reflect (создать ответ)

Создать ответ на основе памяти с учётом черт банка:

```bash
hindsight memory reflect <bank_id> "What do you know about Alice?"

# With additional context
hindsight memory reflect <bank_id> "Should I learn Python?" --context "career advice"

# Higher budget for complex questions
hindsight memory reflect <bank_id> "Summarize my week" --budget high

# Filter by fact type
hindsight memory reflect <bank_id> "query" \
  --fact-types world,experience \
  --exclude-mental-models
```

### История воспоминания

Посмотреть историю наблюдения для одной единицы памяти:

```bash
hindsight memory history <bank_id> <memory_id>
```

### Сброс наблюдений

Удалить все наблюдения для единицы памяти, сохранив исходный факт:

```bash
hindsight memory clear-observations <bank_id> <memory_id>

# Skip confirmation prompt
hindsight memory clear-observations <bank_id> <memory_id> -y
```

## Управление банками

### Список банков

```bash
hindsight bank list
```

### Просмотр черт поведения

```bash
hindsight bank disposition <bank_id>
```

### Задать черты поведения

```bash
hindsight bank set-disposition <bank_id> --skepticism 3 --literalism 4 --empathy 5
```

### Просмотр статистики

```bash
hindsight bank stats <bank_id>
```

### Задать имя банка

```bash
hindsight bank name <bank_id> "My Assistant"
```

### Задать задачу

```bash
hindsight bank mission <bank_id> "I am a helpful AI assistant interested in technology"
```

### Сброс наблюдений всего банка

Удалить все наблюдения банка:

```bash
hindsight bank clear-observations <bank_id>

# Skip confirmation prompt
hindsight bank clear-observations <bank_id> -y
```

### Восстановить обобщение

Возобновить обобщение после сбоя или зависания:

```bash
hindsight bank consolidation-recover <bank_id>
```

## Управление документами

```bash
# List documents
hindsight document list <bank_id>

# Get document details
hindsight document get <bank_id> <document_id>

# Update document metadata
hindsight document update <bank_id> <document_id> --context "updated context"

# Delete document and its memories
hindsight document delete <bank_id> <document_id>
```

## Управление сущностями

```bash
# List entities
hindsight entity list <bank_id>

# Get entity details
hindsight entity get <bank_id> <entity_id>
```

## Управление операциями

Следить за асинхронными операциями (загрузка файлов, обобщение и др.) и управлять ими:

```bash
# List operations
hindsight operation list <bank_id>

# Get operation status
hindsight operation get <bank_id> <operation_id>

# Cancel a pending operation
hindsight operation cancel <bank_id> <operation_id>

# Retry a failed operation
hindsight operation retry <bank_id> <operation_id>
```

## Управление вебхуками

Настроить отправку событий банка:

```bash
# List webhooks
hindsight webhook list <bank_id>

# Create a webhook (defaults to consolidation.completed events)
hindsight webhook create <bank_id> https://example.com/hook

# Create with specific events and signing secret
hindsight webhook create <bank_id> https://example.com/hook \
  --event-types retain.completed,consolidation.completed \
  --secret my-hmac-secret

# Update a webhook
hindsight webhook update <bank_id> <webhook_id> --url https://new-url.com

# Delete a webhook
hindsight webhook delete <bank_id> <webhook_id>

# View delivery history
hindsight webhook deliveries <bank_id> <webhook_id>
```

## Журнал аудита

Посмотреть историю действий с банком:

```bash
# List audit entries
hindsight audit list <bank_id>

# Filter by action and transport
hindsight audit list <bank_id> --action recall --transport mcp

# Filter by date range
hindsight audit list <bank_id> \
  --start-date "2026-04-01T00:00:00Z" \
  --end-date "2026-04-10T00:00:00Z"

# Pagination
hindsight audit list <bank_id> --limit 50 --offset 100
```

## Форматы вывода

```bash
# Pretty (default)
hindsight memory recall <bank_id> "query"

# JSON
hindsight memory recall <bank_id> "query" -o json

# YAML
hindsight memory recall <bank_id> "query" -o yaml
```

## Общие параметры

| Флаг | Описание |
|------|-------------|
| `-v, --verbose` | Подробный вывод, включая запрос и ответ |
| `-o, --output <format>` | Формат вывода: pretty, json, yaml |
| `--help` | Показать справку |
| `--version` | Показать версию |

## Панель управления

Запустите веб-панель прямо из командной строки:

```bash
hindsight ui
```

Панель запустится на вашем компьютере на порту 9999 и возьмёт адрес API из настроек. В ней можно:

- **Управлять банками памяти** — смотреть и менять все банки.
- **Изучать сущности** — просматривать граф знаний.
- **Проверять запросы** — в диалоге запускать recall и reflect.
- **Смотреть историю операций** — читать журналы загрузки и обработки.

:::tip
Для команды UI нужен Node.js. Пакет `@vectorize-io/hindsight-control-plane` сам загрузится и запустится через npx.
:::

## Обозреватель в терминале

Запустите текстовый интерфейс для просмотра банков памяти:

```bash
hindsight explore
```

Через него можно:

- **Смотреть банки памяти** — все банки и их статистику.
- **Искать воспоминания** — сразу видеть результаты recall.
- **Изучать сущности** — граф знаний и связи.
- **Смотреть факты** — факты world, experience и observation.
- **Читать документы** — источники и извлечённые из них воспоминания.

### Клавиши

| Клавиша | Действие |
|-----|--------|
| `↑/↓` | Перейти по списку |
| `Enter` | Выбрать или раскрыть |
| `Tab` | Сменить панель |
| `/` | Искать |
| `q` | Выйти |

<!-- Место для снимка экрана команды explore -->

## Пример работы

```bash
# Configure API URL
hindsight configure --api-url http://localhost:8888

# Store some memories
hindsight memory retain demo "Alice works at Google"
hindsight memory retain demo "Bob is a data scientist"
hindsight memory retain demo "Alice and Bob are colleagues"

# Search memories
hindsight memory recall demo "Who works with Alice?"

# Generate a response
hindsight memory reflect demo "What do you know about the team?"

# Check bank disposition
hindsight bank disposition demo
```
