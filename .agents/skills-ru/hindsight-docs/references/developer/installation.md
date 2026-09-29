# Установка

Hindsight можно запустить разными способами в зависимости от вашей среды и задач.

:::tip Не хотите сами управлять серверами?
**[Hindsight Cloud](https://ui.hindsight.vectorize.io/signup)** — готовая служба, которая берёт на себя серверы, рост нагрузки и поддержку. [Зарегистрируйтесь здесь](https://ui.hindsight.vectorize.io/signup).
:::

## Поддерживаемые платформы

Hindsight работает на **Linux**, **macOS** и **Windows**:

| Платформа | Docker | Свой сервер (pip) | Встроенная БД (pg0) | Примечания |
|----------|--------|------------------|--------------------|-------|
| **Linux** (x86_64, ARM64) | ✅ | ✅ | ✅ | Полная поддержка; советуют для рабочей среды |
| **macOS** (Apple Silicon / arm64) | ✅ | ✅ | ✅ | Полная поддержка |
| **macOS** (Intel / x86_64) | ✅ | ⚠️ только slim | ✅ | Берите `hindsight-all-slim` / `hindsight-api-slim`. Для местных моделей ML полного пакета (PyTorch, MLX) нет сборок под Intel Mac, поэтому `pip install hindsight-all` незаметно откатывается к выпуску многомесячной давности. Добавьте к slim внешнюю службу векторов и повторной сортировки либо местный движок ONNX (`hindsight-api-slim[local-onnx]`). |
| **Windows** (x86_64) | ✅ | ✅ | ✅ | Полная поддержка; отдельная база PostgreSQL описана в разделе [«Windows»](#windows) |

На всех платформах для разработки доступна встроенная база pg0. В Windows можно также поставить отдельный PostgreSQL; шаги описаны в разделе [«Windows»](#windows).

---

## Что понадобится

### PostgreSQL

Hindsight нужен PostgreSQL 14 или новее с расширением для поиска близких векторов. Поддерживаются:

- **pgvector** (по умолчанию)
- **pgvectorscale**
- **vchord**
- **scann** (AlloyDB)

Выбор задаётся через `HINDSIGHT_API_VECTOR_EXTENSION`. Подробности — в разделе [«Настройка»](./configuration).

**По умолчанию** Hindsight берёт **pg0** — встроенный PostgreSQL на вашем компьютере. Это удобно для разработки, но **не советуют для рабочей среды**.

**В рабочей среде** берите отдельный PostgreSQL с поддерживаемым расширением:
- **Supabase** — управляемый PostgreSQL со встроенным pgvector.
- **Neon** — бессерверный PostgreSQL с pgvector.
- **Azure Database for PostgreSQL** — с pgvector и pgvectorscale.
- **Google AlloyDB** / **AlloyDB Omni** — с pgvector и ScaNN.
- **AWS RDS** / **Cloud SQL** — с включённым pgvector.
- **Свой сервер** — PostgreSQL 14+ с выбранным расширением.

### Поставщик LLM

Для извлечения фактов, сопоставления сущностей и создания ответа нужен ключ API LLM. Поддерживаемые службы, модели и настройки описаны в разделе [«Модели»](./models).

### Оборудование

Hindsight работает на обычных серверах. Потребление ресурсов прежде всего зависит от образа: **full** содержит местные модели векторов и сортировки, а **slim** передаёт эти задачи внешним службам.

| Часть | Минимум ОЗУ | Советуемый объём ОЗУ | Примечания |
|-----------|-------------|-----------------|-------|
| **API — образ Full** | 1,5 ГБ | 2 ГБ | Загружает местную модель BGE (~130 МБ), кросс-энкодер MiniLM (~90 МБ) и среды PyTorch/ONNX. В простое RSS — около 0,8–1,0 ГБ, при нагрузке — 1,2–1,5 ГБ. |
| **API — образ Slim** | 512 МБ | 1 ГБ | Местных моделей нет. Память в основном занимают Python и соединения с базой. Нужны [внешние службы векторов и сортировки](./configuration#embeddings), например TEI, OpenAI, Cohere. |
| **Панель управления** | 128 МБ | 256 МБ | Лёгкий процесс Next.js. |
| **Обработчик** (если отдельно) | Как для выбранного образа API | Как для выбранного образа API | Загружает те же модели, что и сервер API. |
| **PostgreSQL** | 512 МБ | От 1 ГБ | Расход растёт с числом воспоминаний и индексов. |

:::tip Как снизить расход памяти
Большую часть ОЗУ полного образа занимают модели векторов и сортировки вместе с PyTorch/ONNX. Чтобы уложиться в несколько сотен МБ, берите образ **slim** и настройте [внешние службы векторов и сортировки](./configuration#embeddings).
:::

CPU и GPU: двух виртуальных ядер CPU хватит для разработки и малой нагрузки. В рабочей среде главная задержка обычно у местной модели сортировки (кросс-энкодера). GPU поможет держать recall быстрым. Другой путь — передать сортировку [внешней службе](./configuration#embeddings), например TEI или Cohere, на отдельном GPU.

---

## Docker

**Подходит для**: быстрого старта, разработки и небольшой среды.

Запустите всё в одном контейнере со встроенным PostgreSQL:

```bash
export OPENAI_API_KEY=sk-xxx

docker run -it --pull always --name hindsight --restart unless-stopped -p 8888:8888 -p 9999:9999 \
  -e HINDSIGHT_API_LLM_API_KEY=$OPENAI_API_KEY \
  -v hindsight-data:/home/hindsight/.pg0 \
  ghcr.io/vectorize-io/hindsight:latest
```

- **Сервер API**: http://localhost:8888
- **Панель управления** (веб-интерфейс): http://localhost:9999

:::note Хранение данных: именованный том или папка узла
Контейнер работает от пользователя без прав root (UID 1000). Выше указан **именованный том** `hindsight-data`: Docker создаёт его с нужным владельцем, и новая настройка не требуется.

Если подключаете **папку узла** (`-v $HOME/.hindsight-docker:/home/hindsight/.pg0`), пользователь UID 1000 должен иметь право на запись. Иначе встроенная база не запустится с ошибкой `Permission denied`. Смените владельца папки через `chown` на UID 1000 либо запустите контейнер от пользователя узла: `--user $(id -u):$(id -g) -e HOME=/home/hindsight` (предварительно назначив папке ваш UID).
:::

Все опубликованные образы [подписаны через Cosign](#verifying-image-signatures). Проверять подпись можно по желанию.

:::tip Задайте постоянный `HINDSIGHT_API_WORKER_ID` в рабочей среде
Обработчик берёт имя узла контейнера как свой ID. Docker по умолчанию ставит сюда ID контейнера, который меняется при каждом перезапуске. Задача, шедшая в момент остановки, остаётся за старым ID, а новый контейнер не узнаёт её как свою.

Задайте постоянный `HINDSIGHT_API_WORKER_ID`, например `-e HINDSIGHT_API_WORKER_ID=hindsight-prod`, чтобы ID не менялся. Это полезно и при одном контейнере. Команды поиска и восстановления — в разделе [«Команды администратора — зависшие операции»](./admin-cli#recovering-stuck-or-zombie-operations).
:::

### Варианты образа Docker

| Вариант | Размер (AMD64) | Размер (ARM64) | Когда брать |
|---------|--------------|--------------|-------------|
| **Full** (`latest`) | ~9 ГБ | ~3,7 ГБ | По умолчанию. Работает сразу, из внешних служб нужна лишь LLM. |
| **Slim** (`slim`) | ~500 МБ | ~500 МБ | Если векторы и повторную сортировку уже дают внешние службы (OpenAI, Cohere, TEI). Образ гораздо меньше и быстрее запускается. Нужны [внешние поставщики](./configuration#embeddings). |

Образу slim отвечает пакет pip [`hindsight-api-slim`](#bare-metal-pip). Настройки внешних служб — в разделе [«Настройка»](./configuration#embeddings).

### Свои модели в своём образе

:::tip Рабочая среда с другими местными моделями
Если вы сменили местную модель векторов или сортировки, добавьте её в свой образ при сборке вместо включения Helm PVC `modelCache`. Рабочий пример есть в [`docker/docker-compose/custom-models/`](https://github.com/vectorize-io/hindsight/tree/main/docker/docker-compose/custom-models).
:::

### Доступные теги

```bash
# Standalone (API + Control Plane)
ghcr.io/vectorize-io/hindsight:latest        # Full, latest release
ghcr.io/vectorize-io/hindsight:latest-slim          # Slim, latest release
ghcr.io/vectorize-io/hindsight:0.4.9         # Full, specific version
ghcr.io/vectorize-io/hindsight:0.4.9-slim    # Slim, specific version

# API only
ghcr.io/vectorize-io/hindsight-api:latest
ghcr.io/vectorize-io/hindsight-api:latest-slim

# Control Plane only
ghcr.io/vectorize-io/hindsight-control-plane:latest
```

### Проверка подписей образов

Образы подписаны через [Cosign](https://docs.sigstore.dev/cosign/signing/overview/) с OIDC без ключа. Чтобы проверить любой тег:

```bash
cosign verify ghcr.io/vectorize-io/hindsight:<tag> \
  --certificate-identity-regexp '^https://github\.com/vectorize-io/hindsight/\.github/workflows/(sign-images|release)\.yml@.*' \
  --certificate-oidc-issuer https://token.actions.githubusercontent.com
```

---

## Helm / Kubernetes

**Подходит для**: рабочей среды, автоматического роста числа экземпляров и облака.

```bash
# Install with built-in PostgreSQL
helm install hindsight oci://ghcr.io/vectorize-io/charts/hindsight \
  --set api.llm.provider=groq \
  --set api.llm.apiKey=gsk_xxxxxxxxxxxx \
  --set postgresql.enabled=true

# Or use external PostgreSQL
helm install hindsight oci://ghcr.io/vectorize-io/charts/hindsight \
  --set api.llm.provider=groq \
  --set api.llm.apiKey=gsk_xxxxxxxxxxxx \
  --set postgresql.enabled=false \
  --set api.database.url=postgresql://user:pass@postgres.example.com:5432/hindsight

# Install a specific version
helm install hindsight oci://ghcr.io/vectorize-io/charts/hindsight --version 0.1.3

# Upgrade to latest
helm upgrade hindsight oci://ghcr.io/vectorize-io/charts/hindsight
```

**Требования**:
- Кластер Kubernetes (GKE, EKS, AKS или свой).
- Helm 3.8 или новее.

### Распределённые обработчики

При высокой нагрузке включите отдельные поды обработчиков, чтобы наращивать их число независимо от API:

```bash
helm install hindsight oci://ghcr.io/vectorize-io/charts/hindsight \
  --set worker.enabled=true \
  --set worker.replicaCount=3
```

Схема запускает обработчики как StatefulSet: у каждого пода постоянное имя, например `hindsight-worker-0`, которое берётся как `HINDSIGHT_API_WORKER_ID`. После перезапуска под узнает свои задачи. Если вместо схемы взять обычный Deployment, задайте `HINDSIGHT_API_WORKER_ID` отдельно для каждого экземпляра. Иначе имена узлов будут случайными, а взятые ранее задачи останутся без исполнителя. Поиск причины описан в разделе [«Команды администратора — зависшие операции»](./admin-cli#recovering-stuck-or-zombie-operations).

Настройки и устройство описаны в разделе [«Службы — обработчик задач»](./services#worker-service).

Все параметры схемы есть в [Helm-файле values.yaml](https://github.com/vectorize-io/hindsight/tree/main/helm/hindsight/values.yaml).

---

## Свой сервер (pip)

**Подходит для**: отдельной службы Hindsight на своём узле.

### Установка

```bash
pip install hindsight-api        # Full — works out of the box
pip install hindsight-api-slim   # Slim — requires external services for embeddings, reranking, and the database
```

Для `hindsight-api-slim` все операции моделей должны идти через внешние службы. Подробности — в разделе [«Настройка»](./configuration#embeddings).

### Запуск со встроенной базой

Для разработки и тестов Hindsight может работать со встроенным PostgreSQL (pg0):

```bash
export HINDSIGHT_API_LLM_PROVIDER=groq
export HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx

hindsight-api
```

База будет создана в `~/.hindsight/data/`, а API запустится на http://localhost:8888.

### Запуск с отдельным PostgreSQL

В рабочей среде подключитесь к своему PostgreSQL:

```bash
export HINDSIGHT_API_DATABASE_URL=postgresql://user:pass@localhost:5432/hindsight
export HINDSIGHT_API_LLM_PROVIDER=groq
export HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx

hindsight-api
```

**Примечание:** база должна уже существовать, а pgvector — быть включён (`CREATE EXTENSION vector;`).

### Параметры командной строки

```bash
hindsight-api --port 9000          # Custom port (default: 8888)
hindsight-api --host 127.0.0.1     # Bind to localhost only
hindsight-api --workers 4          # Multiple worker processes
hindsight-api --log-level debug    # Verbose logging
```

### Панель управления

Панель управления (веб-интерфейс) можно запустить отдельно через npx:

```bash
npx @vectorize-io/hindsight-control-plane --api-url http://localhost:8888
```

Она подключится к работающему серверу API и даст интерфейс для банков памяти, сущностей и проверки запросов.

#### Параметры

| Параметр | Переменная среды | По умолчанию | Описание |
|--------|---------------------|---------|-------------|
| `-p, --port` | `PORT` | 9999 | Порт для входящих запросов |
| `-H, --hostname` | `HOSTNAME` | 0.0.0.0 | Имя узла для привязки |
| `-a, --api-url` | `HINDSIGHT_CP_DATAPLANE_API_URL` | http://localhost:8888 | Адрес API Hindsight |
| | `HINDSIGHT_CP_ACCESS_KEY` | *(нет)* | Ключ для защиты панели. Если задан, пользователь должен ввести его при входе. |

#### Примеры

```bash
# Run on custom port
npx @vectorize-io/hindsight-control-plane --port 9999 --api-url http://localhost:8888

# Using environment variables
export HINDSIGHT_CP_DATAPLANE_API_URL=http://api.example.com
npx @vectorize-io/hindsight-control-plane

# Production deployment
PORT=80 HINDSIGHT_CP_DATAPLANE_API_URL=https://api.hindsight.io npx @vectorize-io/hindsight-control-plane
```

---

## Windows

**Подходит для**: запуска Hindsight прямо в Windows без Docker.

В Windows Hindsight сразу работает со встроенной базой pg0. Установите пакет и запустите:

```powershell
pip install hindsight-api

set HINDSIGHT_API_LLM_PROVIDER=openai
set HINDSIGHT_API_LLM_API_KEY=sk-xxx
set HINDSIGHT_API_LLM_MODEL=gpt-4o-mini

hindsight-api
```

### Отдельный PostgreSQL (по желанию)

Если хотите взять свой PostgreSQL вместо встроенной базы:

```powershell
# Install PostgreSQL
winget install PostgreSQL.PostgreSQL.17

# Build pgvector (requires Visual Studio Build Tools)
git clone https://github.com/pgvector/pgvector.git
cd pgvector

# Open "x64 Native Tools Command Prompt for VS" and run:
set PGROOT=C:\Program Files\PostgreSQL\17
nmake /F Makefile.win
nmake /F Makefile.win install

# Create the database and enable the vector extension
psql -U postgres -c "CREATE DATABASE hindsight;"
psql -U postgres -d hindsight -c "CREATE EXTENSION vector;"
```

Затем запустите Hindsight с адресом своей базы:

```powershell
pip install hindsight-api

set HINDSIGHT_API_DATABASE_URL=postgresql://postgres@localhost:5432/hindsight
set HINDSIGHT_API_LLM_PROVIDER=openai
set HINDSIGHT_API_LLM_API_KEY=sk-xxx
set HINDSIGHT_API_LLM_MODEL=gpt-4o-mini

hindsight-api
```

- **Сервер API**: http://localhost:8888

:::tip
Можно взять пакет slim (`pip install hindsight-api-slim`), если настроить внешние службы векторов и повторной сортировки. См. [«Настройка»](./configuration#embeddings).
:::

### Windows при сетевых ограничениях в Китае

Если вы работаете в Windows при сетевых ограничениях в Китае:

1. DeepSeek подходит для `HINDSIGHT_API_LLM_PROVIDER`, но не даёт адреса для создания векторов.
2. Берите местную модель векторов: это лучше для личных данных и надёжнее при сетевых ограничениях.
3. До запуска Hindsight задайте `HF_ENDPOINT=https://hf-mirror.com`, чтобы модели Hugging Face качались через доступное из Китая зеркало.

```powershell
set HF_ENDPOINT=https://hf-mirror.com

set HINDSIGHT_API_LLM_PROVIDER=deepseek
set HINDSIGHT_API_LLM_API_KEY=sk-your-deepseek-key
set HINDSIGHT_API_LLM_MODEL=deepseek-v4-flash
set HINDSIGHT_API_LLM_BASE_URL=https://api.deepseek.com

set HINDSIGHT_API_EMBEDDINGS_PROVIDER=local
set HINDSIGHT_API_EMBEDDINGS_LOCAL_MODEL=BAAI/bge-small-en-v1.5

set HINDSIGHT_API_RERANKER_PROVIDER=flashrank

hindsight-api
```

Переменную `HF_ENDPOINT` читает библиотека Hugging Face (`huggingface_hub`), а не сам Hindsight.

---

## Внутри приложения Python

**Подходит для**: вызова Hindsight из Python без отдельного процесса сервера.

```bash
pip install hindsight-all        # Full — works out of the box (Linux, Windows, Apple Silicon Macs)
pip install hindsight-all-slim   # Slim — requires external services for embeddings, reranking, and the database
```

На Mac с Intel (x86_64) ставьте `hindsight-all-slim`; см. [«Поддерживаемые платформы»](#supported-platforms).

У `hindsight-all` есть два способа встроить службу:

**В одном процессе** (`HindsightServer`): сервер работает в фоновом потоке вашего приложения. Удобно для тесной связи с приложением, если вы уже сами управляете жизненным циклом процесса.

```python
from hindsight import HindsightServer, HindsightClient

with HindsightServer(llm_provider="openai", llm_api_key="sk-xxx") as server:
    client = HindsightClient(base_url=server.url)
    client.retain(bank_id="alice", content="Alice prefers concise answers.")
    results = client.recall(bank_id="alice", query="How should I respond to Alice?")
```

**Управляемый дочерний процесс** (`HindsightEmbedded`): сервер работает как фоновая служба для нескольких процессов Python или сеансов. Запускается при первом вызове и сам останавливается после срока простоя.

```python
from hindsight import HindsightEmbedded

client = HindsightEmbedded(llm_provider="openai", llm_api_key="sk-xxx")
client.retain(bank_id="alice", content="Alice prefers concise answers.")
results = client.recall(bank_id="alice", query="How should I respond to Alice?")
```

Полное описание API — в [SDK для Python](../sdks/python.md).

---

## Что дальше

- [Настройка](./configuration.md) — переменные среды и параметры
- [Модели](./models.mdx) — модели ML и поставщики
- [Слежение за работой](./monitoring.md) — метрики и трассы
