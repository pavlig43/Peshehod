---
sidebar_position: 2
---

# Программный API (Python)

Пакет Python `hindsight-all` позволяет коду запускать местную службу Hindsight и управлять ею без отдельной серверной среды. Он содержит сервер API Hindsight, встроенный PostgreSQL и клиент Python. Достаточно `pip install hindsight-all`, чтобы затем запустить полноценный Hindsight несколькими строками Python.

Служба работает как **отдельный процесс ОС** на `127.0.0.1`, а не в памяти процесса Python. Код общается с ней по HTTP через входящий в пакет `HindsightClient`.

Если сервер Hindsight уже работает и нужен только клиент, возьмите [клиент Python (`hindsight-client`)](./python.md).

## Как это работает

`hindsight-all` даёт два основных API:

- **`HindsightServer`** — явное управление жизненным циклом. Применяйте как контекстный менеджер, если запуск и остановка должны происходить в точный момент, например в тестах.
- **`HindsightEmbedded`** — сам управляет службой. Запускает её при первом вызове, применяет снова при следующих и останавливает после срока бездействия. Подходит коду приложения, где не хотят вручную управлять службой.

Оба варианта общаются с одной и той же службой через один HTTP-интерфейс `HindsightClient`. Отличается лишь управление процессом сервера.

## Установка

```bash
pip install hindsight-all
```

Пакет `hindsight-all` включает зависимости `hindsight-api-slim`, `hindsight-client` и `hindsight-embed`, поэтому одна команда `pip install` ставит всё нужное.

На Mac с процессором Intel (x86_64) ставьте `hindsight-all-slim`: для местных моделей ML из полного пакета нет собранных пакетов под Intel Mac. См. [«Поддерживаемые платформы»](../developer/installation#supported-platforms).

## `HindsightServer` — явное управление

Применяйте `HindsightServer` как контекстный менеджер, если сервер нужно запустить сразу, держать в течение блока и затем штатно остановить. Это удобно для тестов и коротких скриптов.

```python
import os
from hindsight import HindsightServer, HindsightClient

with HindsightServer(
    llm_provider="openai",
    llm_model="gpt-4o-mini",
    llm_api_key=os.environ["OPENAI_API_KEY"],
) as server:
    client = HindsightClient(base_url=server.url)

    client.retain(bank_id="my-bank", content="Alice works at Google")
    results = client.recall(bank_id="my-bank", query="What does Alice do?")
    for r in results:
        print(r.text)

    answer = client.reflect(bank_id="my-bank", query="Tell me about Alice")
    print(answer.text)
# Server is stopped here
```

## `HindsightEmbedded` — автоматическое управление

`HindsightEmbedded` — самый простой способ применять Hindsight в Python. Он сам управляет фоновой службой: запускает её при первом вызове, держит между вызовами и останавливает после срока бездействия.

```python
from hindsight import HindsightEmbedded
import os

# Server starts automatically on first call
client = HindsightEmbedded(
    profile="myapp",                        # Profile for data isolation
    llm_provider="openai",
    llm_model="gpt-4o-mini",
    llm_api_key=os.environ["OPENAI_API_KEY"],
)

# Use immediately - no manual server management needed
client.retain(bank_id="my-bank", content="Alice works at Google")
results = client.recall(bank_id="my-bank", query="What does Alice do?")

# Server continues running (auto-stops after idle timeout)
# Or explicitly stop it:
client.close(stop_daemon=True)
```

### Что такое профиль?

Профиль — отдельная среда Hindsight. У каждого профиля своя встроенная база PostgreSQL (лежит в `~/.pg0/instances/hindsight-embed-{profile}/`) и свой сервер API. Разные профили помогают разделить среды (разработка/рабочая), приложения или пользователей.

### Какой вариант выбрать

| Задача | Выбор |
|---|---|
| Тесты, короткие скрипты, точный момент запуска и остановки | `HindsightServer` (контекстный менеджер) |
| Долгая работа приложения, запуск при первом вызове, без ручного управления службой | `HindsightEmbedded` |
| Сервер Hindsight уже работает в другом месте | Напрямую [`hindsight-client`](./python.md) |

## Группы API

В `HindsightEmbedded` и `HindsightClient` есть отдельные группы API для банков, ментальных моделей, указаний и воспоминаний:

```python
from hindsight import HindsightEmbedded
import os

embedded = HindsightEmbedded(
    profile="myapp",
    llm_provider="openai",
    llm_api_key=os.environ["OPENAI_API_KEY"],
)

# Core operations
embedded.retain(bank_id="test", content="Hello")
results = embedded.recall(bank_id="test", query="Hello")

# Bank management
embedded.banks.create(bank_id="test", name="Test Bank", mission="Help users")
embedded.banks.set_mission(bank_id="test", mission="Updated mission")
embedded.banks.delete(bank_id="test")

# Mental models
embedded.mental_models.create(
    bank_id="test",
    name="User Preferences",
    content="User prefers dark mode"
)
models = embedded.mental_models.list(bank_id="test")

# Directives
embedded.directives.create(
    bank_id="test",
    name="Response Style",
    content="Be concise and friendly"
)
directives = embedded.directives.list(bank_id="test")

# List memories
memories = embedded.memories.list(bank_id="test", type="world", limit=50)
```

Группы API проверяют работу службы перед каждым вызовом и поэтому помогают пережить её сбой:

```python
# ✅ GOOD - Uses API namespace (daemon restarts handled)
embedded.banks.create(bank_id="test", name="Test")

# ❌ BAD - Direct client access (daemon crashes NOT handled)
client = embedded.client
client.create_bank(bank_id="test", name="Test")  # Fails if daemon crashed
```

Полное описание методов retain/recall/reflect и их параметров одинаково для всех способов получения клиента. См. страницу [«Клиент Python»](./python.md).
