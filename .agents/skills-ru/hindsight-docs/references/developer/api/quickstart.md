
# Быстрый старт

Запустите Hindsight за 60 секунд.

{/* Import raw source files */}

## Клиенты

<ClientsGrid />

## Запуск сервера API

### pip (только API)

```bash
pip install hindsight-api
export OPENAI_API_KEY=sk-xxx
export HINDSIGHT_API_LLM_API_KEY=$OPENAI_API_KEY

hindsight-api
```

API доступен по адресу [http://localhost:8888](http://localhost:8888/docs).

### Docker (все возможности)

```bash

export OPENAI_API_KEY=sk-xxx

docker run -it --pull always --name hindsight --restart unless-stopped -p 8888:8888 -p 9999:9999 \
  -e HINDSIGHT_API_LLM_API_KEY=$OPENAI_API_KEY \
  -v $HOME/.hindsight-docker:/home/hindsight/.pg0 \
  ghcr.io/vectorize-io/hindsight:latest
```

- **API**: http://localhost:8888
- **Панель управления** (веб-интерфейс): http://localhost:9999

> **💡 Задайте постоянный `HINDSIGHT_API_WORKER_ID` в рабочей среде**
> 
По умолчанию обработчик берёт имя узла контейнера как свой ID. Docker задаёт в этом качестве ID контейнера, который меняется при каждом перезапуске. Задача, которая выполнялась в момент остановки, остаётся привязана к старому ID: новый контейнер не узнает её как свою.

Задайте постоянное значение `HINDSIGHT_API_WORKER_ID` (например, `-e HINDSIGHT_API_WORKER_ID=hindsight-prod`), чтобы ID обработчика сохранялся после перезапуска. Это полезно и при работе с одним контейнером. Команды для поиска и исправления таких случаев приведены в разделе [«Команды администратора — восстановление зависших операций»](../admin-cli.md#recovering-stuck-or-zombie-operations).
> **💡 Поставщик LLM**
> 
Hindsight нужна LLM, которая умеет возвращать структурный результат. Для быстрого и недорогого вывода советуем **Groq** с `gpt-oss-20b`.
Подробнее — в разделе [«Поставщики LLM»](../models.md#llm).
---

## Работа с клиентом

### Python

```bash
pip install hindsight-client
```

```python
from hindsight_client import Hindsight

client = Hindsight(base_url="http://localhost:8888")

# Retain: Store information
client.retain(bank_id="my-bank", content="Alice works at Google as a software engineer")

# Recall: Search memories
client.recall(bank_id="my-bank", query="What does Alice do?")

# Reflect: Generate disposition-aware response
client.reflect(bank_id="my-bank", query="Tell me about Alice")
```

### Node.js

```bash
npm install @vectorize-io/hindsight-client
```

```javascript
import { HindsightClient } from '@vectorize-io/hindsight-client';

const client = new HindsightClient({ baseUrl: 'http://localhost:8888' });

// Retain: Store information
await client.retain('my-bank', 'Alice works at Google as a software engineer');

// Recall: Search memories
await client.recall('my-bank', 'What does Alice do?');

// Reflect: Generate response
await client.reflect('my-bank', 'Tell me about Alice');
```

### CLI

```bash
curl -fsSL https://hindsight.vectorize.io/get-cli | bash
```

```bash
# Retain: Store information
hindsight memory retain my-bank "Alice works at Google as a software engineer"

# Recall: Search memories
hindsight memory recall my-bank "What does Alice do?"

# Reflect: Generate response
hindsight memory reflect my-bank "Tell me about Alice"
```

### Go

```bash
go get github.com/vectorize-io/hindsight/hindsight-clients/go
```

```go
# Section 'quickstart-full' not found in api/quickstart.go
```

---

## Что происходит

| Операция | Действие |
|-----------|--------------|
| **Retain** | Обрабатывает текст, извлекает факты, распознаёт сущности и связывает их в графе знаний |
| **Recall** | Параллельно ищет нужные воспоминания четырьмя способами: по смыслу, ключевым словам, графу и времени |
| **Reflect** | Создаёт ответ на основе найденных воспоминаний с учётом черт поведения |

---

## Подключения

Все поддерживаемые подключения перечислены в разделе Integrations Hub.

## Что дальше

- [**Retain**](./retain) — тонкая настройка сохранения воспоминаний
- [**Recall**](./recall) — способы поиска
- [**Reflect**](./reflect) — рассуждение с учётом черт поведения
- [**Банки памяти**](./memory-banks) — настройка черт поведения и задачи
- [**Запуск сервера**](../installation.md) — Docker Compose, Helm и рабочая среда
