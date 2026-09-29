---
sidebar_position: 5
---

# Команды локальной службы (`hindsight-embed`)

Местная память с автоматическим управлением службой и почти без настройки. Подходит для разработки, проверки идей и приложений с одним пользователем.

## Обзор

`hindsight-embed` — SDK, который объединяет API Hindsight и базу PostgreSQL в одну местную службу с автоматическим управлением. Он создан для разработки, проверки идей и приложений с одним пользователем, которым нужна память без лишних служб.

**Как это работает:**

1. **Первая команда запускает службу**: любая команда `hindsight-embed` сначала проверяет, работает ли местная служба.
2. **Автоматическое управление**: если службы нет, команда сама запускает `hindsight-api --daemon` в фоновом режиме.
3. **Встроенная база**: служба берёт `pg0` (встроенный PostgreSQL), поэтому отдельно ставить базу не нужно.
4. **Передача команды**: команда отправляется местной службе по HTTP (`localhost:8888`).
5. **Автоматическая остановка**: после пяти минут бездействия (срок можно менять) служба штатно останавливается и освобождает ресурсы.

**Главные свойства:**

- **Почти без настройки** — хватит одной команды `configure`.
- **Автоматический жизненный цикл** — служба запускается по нужде и останавливается при простое.
- **Раздельное хранение** — у каждого банка своя встроенная база PostgreSQL.
- **Только местный доступ** — привязка к `127.0.0.1:8888`, из сети служба недоступна.
- **Полный движок** — тот же движок памяти, что и в службе API.

Это похоже на SQLite для долгой памяти: возможности Hindsight без ручного управления серверами.

## Установка

Запустите через `uvx` (советуем этот вариант: всегда свежая версия):

```bash
# Run directly without installation
uvx hindsight-embed@latest configure

# Or use pipx for persistent installation
pipx install hindsight-embed
```

## Быстрый старт

### 1. Настройте

```bash
# Interactive configuration
hindsight-embed configure

# Or non-interactive via environment variables
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gpt-4o-mini
hindsight-embed configure
```

Настройки сохраняются в `~/.hindsight/embed`:

```bash
HINDSIGHT_API_LLM_PROVIDER=openai
HINDSIGHT_API_LLM_MODEL=gpt-4o-mini
HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx

# Daemon settings (macOS: force CPU to avoid MPS/XPC issues)
HINDSIGHT_API_EMBEDDINGS_LOCAL_FORCE_CPU=1
HINDSIGHT_API_RERANKER_LOCAL_FORCE_CPU=1
```

### 2. Работайте с памятью

```bash
# Store a memory
hindsight-embed memory retain default "User prefers dark mode"

# Query memories
hindsight-embed memory recall default "user preferences"

# Reasoning with memory
hindsight-embed memory reflect default "What color scheme should I use?"
```

При первом вызове служба запустится сама.

### 3. Откройте центр управления (по желанию)

Местный центр управления нужен, если вы хотите настраивать службу в браузере и следить за её работой:

```bash
# Launch the control center and open the browser
hindsight-embed control start

# Or use the browser wizard instead of terminal prompts during setup
hindsight-embed configure --ui
```

Центр доступен лишь на этом компьютере (по умолчанию `http://localhost:7878`) и выводит адрес с токеном. Местный токен доступа лежит в `~/.hindsight/control.token`, а журнал — в `~/.hindsight/control.log`.

```bash
# Pick a different control-center port for this launch
hindsight-embed control start --port 7879

# Start without opening a browser automatically
hindsight-embed control start --no-open

# Check, inspect, or stop the control center
hindsight-embed control status
hindsight-embed control logs -f
hindsight-embed control stop
```

Центр управления работает в отдельном от службы памяти процессе. Его остановка или перезапуск не останавливает работающую службу.

## Переменные среды

| Переменная | Описание | По умолчанию |
|----------|-------------|---------|
| `HINDSIGHT_API_LLM_API_KEY` | **Обязательна**. Ключ API поставщика LLM | — |
| `HINDSIGHT_API_LLM_PROVIDER` | Поставщик LLM: `openai`, `anthropic`, `gemini`, `groq`, `minimax`, `ollama` | `openai` |
| `HINDSIGHT_API_LLM_MODEL` | Имя модели | `gpt-4o-mini` |
| `HINDSIGHT_EMBED_DAEMON_IDLE_TIMEOUT` | Через сколько секунд простоя служба сама завершится (0 — никогда) | `0` |
| `HINDSIGHT_EMBED_CONTROL_PORT` | Порт для `hindsight-embed control start` | `7878` |

**Примеры для разных поставщиков:**

```bash
# OpenAI
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gpt-4o

# Groq (fast inference)
export HINDSIGHT_API_LLM_PROVIDER=groq
export HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=llama-3.3-70b-versatile

# Anthropic
export HINDSIGHT_API_LLM_PROVIDER=anthropic
export HINDSIGHT_API_LLM_API_KEY=sk-ant-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=claude-sonnet-4-20250514
```

## Управление службой

### Срок простоя

Задайте, сколько служба будет работать без вызовов:

```bash
# Never timeout (daemon runs until manually stopped)
export HINDSIGHT_EMBED_DAEMON_IDLE_TIMEOUT=0

# Shorter timeout: 1 minute
export HINDSIGHT_EMBED_DAEMON_IDLE_TIMEOUT=60

# Longer timeout: 30 minutes
export HINDSIGHT_EMBED_DAEMON_IDLE_TIMEOUT=1800
```

### Команды службы

```bash
# Check daemon status
hindsight-embed daemon status

# View daemon logs in real-time
hindsight-embed daemon logs -f

# Stop daemon manually
hindsight-embed daemon stop
```

### Команды центра управления

```bash
# Start or reuse the local browser control center
hindsight-embed control start

# Check whether it is running
hindsight-embed control status

# View control-center logs
hindsight-embed control logs -f

# Stop the control-center process
hindsight-embed control stop
```

## Команды

У всех операций с памятью тот же интерфейс, что и в CLI:

### Retain (сохранить воспоминание)

```bash
hindsight-embed memory retain <bank_id> "content"

# With context
hindsight-embed memory retain <bank_id> "content" --context "source information"

# Background processing
hindsight-embed memory retain <bank_id> "content" --async
```

### Recall (поиск)

```bash
hindsight-embed memory recall <bank_id> "query"

# With budget control
hindsight-embed memory recall <bank_id> "query" --budget high

# Show trace
hindsight-embed memory recall <bank_id> "query" --trace
```

### Reflect (создать ответ)

```bash
hindsight-embed memory reflect <bank_id> "prompt"

# With additional context
hindsight-embed memory reflect <bank_id> "prompt" --context "additional info"
```

### Управление банками

```bash
# List all banks
hindsight-embed bank list

# View bank stats
hindsight-embed bank stats <bank_id>

# Set bank name
hindsight-embed bank name <bank_id> "My Assistant"

# Set bank mission
hindsight-embed bank mission <bank_id> "I am a helpful AI assistant"
```

## Поиск ошибок

### Служба не запускается

Проверьте журнал:

```bash
hindsight-embed daemon logs
# Or watch in real-time
hindsight-embed daemon logs -f
```

Частые причины:
- **Нет ключа API**: задайте `HINDSIGHT_API_LLM_API_KEY`.
- **Порт занят**: другая служба использует порт 8888.
- **Нет прав доступа**: проверьте права для `~/.hindsight/`.

### Служба сразу останавливается

Проверьте, не слишком ли короткий задан срок простоя:

```bash
# Disable idle timeout for debugging
export HINDSIGHT_EMBED_DAEMON_IDLE_TIMEOUT=0
hindsight-embed daemon status
```

### Сброс настроек

```bash
# Remove config file and reconfigure
rm ~/.hindsight/embed
hindsight-embed configure
```

## Когда применять

**Подходит для:**
- Разработки и проверки идей
- Приложений с одним пользователем
- Инструментов, где данные хранятся прежде всего на устройстве
- Быстрых опытов с Hindsight

**Не подходит для:**
- Рабочих служб с несколькими пользователями
- Служб с доступом по сети
- Задач с высокими требованиями к доступности
- Приложений с несколькими арендаторами

Для рабочей среды возьмите [службу API](../developer/services.md) с отдельным PostgreSQL.
