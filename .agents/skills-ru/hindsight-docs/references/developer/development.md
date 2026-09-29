---
sidebar_position: 7
---

# Руководство для разработчика

Как настроить местную среду, чтобы вносить правки в Hindsight.

## Что понадобится

- Python 3.11 или новее
- [uv](https://docs.astral.sh/uv/) — быстрый менеджер пакетов Python
- Docker и Docker Compose
- Ключ API для LLM (OpenAI, Groq или Ollama)

## Настройка местной среды

### 1. Клонируйте репозиторий

```bash
git clone https://github.com/vectorize-io/hindsight.git
cd hindsight
```

### 2. Установите зависимости

```bash
uv sync
```

### 3. Запустите PostgreSQL

Через Docker запустите лишь базу:

```bash
cd docker && docker-compose up -d postgres
```

### 4. Настройте среду

```bash
cp .env.example .env
```

Добавьте ключ API вашей LLM в `.env`:

```bash
# Database (connects to Docker postgres)
HINDSIGHT_API_DATABASE_URL=postgresql://hindsight:hindsight_dev@localhost:5432/hindsight

# LLM Provider (choose one)
HINDSIGHT_API_LLM_PROVIDER=groq
HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx
HINDSIGHT_API_LLM_MODEL=llama-3.1-70b-versatile
```

### 5. Запустите сервер API

```bash
./scripts/start-server.sh --env local
```

Сервер будет доступен по адресу http://localhost:8888.

## Запуск тестов

```bash
# Run all tests
uv run pytest

# Run specific test file
uv run pytest tests/test_retrieval.py

# Run with verbose output
uv run pytest -v
```

## Создание кода

### Обновление клиентов API

После правки схемы OpenAPI заново создайте клиенты:

```bash
./scripts/generate-clients.sh
```

Скрипт создаст:
- Клиент Python в `hindsight-clients/python/`
- Клиент TypeScript в `hindsight-clients/typescript/`

### Экспорт схемы OpenAPI

```bash
./scripts/export-openapi.sh
```

## Структура проекта

```
hindsight/
├── hindsight-api/          # Main API server
│   ├── hindsight_api/
│   │   ├── api/           # HTTP endpoints
│   │   ├── engine/        # Memory engine, retrieval, reasoning
│   │   └── web/           # Server entry point
│   └── tests/
├── hindsight-clients/      # Generated SDK clients
│   ├── python/
│   └── typescript/
├── hindsight-control-plane/ # Admin UI (Next.js)
├── docker/                 # Docker Compose setup
└── scripts/               # Development scripts
```

## Как внести свой вклад

1. Создайте ветку для правки от `main`
2. Внесите правки
3. Запустите тесты: `uv run pytest`
4. Откройте запрос на слияние

## Поиск ошибок

### Ошибки связи с базой

Проверьте, работает ли PostgreSQL:

```bash
docker-compose ps
```

Проверьте связь с базой:

```bash
psql postgresql://hindsight:hindsight_dev@localhost:5432/hindsight
```

### Загрузка моделей ML

При первом запуске Hindsight скачивает модели для векторов и повторной сортировки. Это может занять несколько минут. Копии моделей хранятся в `~/.cache/huggingface/`.

### Порт занят

Если порт 8888 уже занят:

```bash
HINDSIGHT_API_PORT=8889 ./scripts/start-server.sh --env local
```
