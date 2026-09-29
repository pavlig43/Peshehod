# Команды администратора

`hindsight-admin` даёт команды для управления Hindsight: миграции базы, резервное копирование и восстановление.

## Установка

Команды администратора входят в пакет `hindsight-api`. После его установки `hindsight-admin` появится в `PATH`:

```bash
pip install hindsight-api
# or
uv add hindsight-api
```

## Запуск команд

`hindsight-admin` подключается **прямо к PostgreSQL**, не вызывая HTTP API. Он читает **те же настройки, что и служба API**: переменные среды и файл `.env` в текущей папке. Команды работают с базой, на которую указывает `HINDSIGHT_API_DATABASE_URL`:

- **По умолчанию**: `pg0`, встроенная база для разработки. Команду нужно запускать на узле, где лежат данные pg0.
- **В рабочей среде**: задайте `HINDSIGHT_API_DATABASE_URL=postgresql://user:pass@host:5432/hindsight`.

Поскольку команды обращаются к базе напрямую (через двоичный `COPY`, `TRUNCATE` и др.), они работают **только с PostgreSQL**, а не с Oracle. Запускайте их на том же узле или в том же контейнере, что и API: так они получат верные настройки и доступ к базе по сети.

```bash
# Bare metal / virtualenv (with the API's env or a .env in the working dir)
hindsight-admin worker-status

# Docker — exec into the API container
docker exec -it hindsight-api hindsight-admin backup /data/backup.zip

# Kubernetes — exec into an API pod
kubectl exec deploy/hindsight-api -- hindsight-admin run-db-migration
```

Параметр `--schema` выбирает схему отдельного арендатора. По умолчанию берётся настроенная основная схема. См. ниже [«Переменные среды»](#environment-variables).

## Команды

### run-db-migration

Обновляет схему базы до последней версии. По умолчанию берёт основную схему и все схемы арендаторов, найденные расширением. Через `--schema` можно обновить одну схему. Это удобно, когда миграции должны идти отдельно от запуска API, например в CI/CD или перед выпуском новой версии.

```bash
hindsight-admin run-db-migration [OPTIONS]
```

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--schema`, `-s` | Схема для миграции. Без параметра обновляются основная и все найденные схемы арендаторов. | Все схемы |
| `--embedding-dimension` | Ожидаемый размер вектора, который нужно закрепить после миграции. Если не задан, размер не сверяется. | Сверка пропущена |
| `--skip-extension-reconcile` | Не сверять индексы векторов и поиска по тексту после миграции. Сверка нужна, только если `HINDSIGHT_API_VECTOR_EXTENSION` или `HINDSIGHT_API_TEXT_SEARCH_EXTENSION` отличается от текущих индексов схемы. Пропуск ускоряет повторный запуск миграций по многим схемам без иных правок; берите лишь при прежнем движке. | Сверка идёт |

**Примеры:**

```bash
# Run migrations on the base schema plus all discovered tenant schemas
hindsight-admin run-db-migration

# Run migrations on a specific tenant schema
hindsight-admin run-db-migration --schema tenant_acme
```

:::tip Выключить автоматические миграции
Чтобы миграции не шли при запуске API, задайте `HINDSIGHT_API_RUN_MIGRATIONS_ON_STARTUP=false`. Тогда их можно выполнять отдельным шагом при выпуске.
:::

---

### backup

Создать копию всех данных Hindsight в ZIP-файле.

```bash
hindsight-admin backup OUTPUT [OPTIONS]
```

**Аргументы:**

| Аргумент | Описание |
|----------|-------------|
| `OUTPUT` | Путь к файлу копии. Если нет расширения `.zip`, оно будет добавлено. |

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--schema`, `-s` | Схема базы для копирования | `public` |

**Примеры:**

```bash
# Backup to a file
hindsight-admin backup /backups/hindsight-2024-01-15.zip

# Backup a specific tenant schema
hindsight-admin backup /backups/tenant-acme.zip --schema tenant_acme
```

В копию входят:
- Банки памяти и их настройки.
- Документы и фрагменты.
- Сущности и связи.
- Единицы памяти: факты, опыт, наблюдения.
- Совместные упоминания сущностей и связи воспоминаний.
- Ментальные модели и указания.
- Вебхуки и хранилище файлов.
- Внутренние таблицы работы: фоновые операции, журнал аудита, очередь обслуживания графа и похожие данные, чтобы восстановить всю базу точно.

:::note Целостность
Копия создаётся внутри транзакции базы с уровнем изоляции `REPEATABLE READ`, поэтому все таблицы отражают один согласованный момент.
:::

---

### restore

Восстановить данные из копии. **Внимание: все текущие данные выбранной схемы будут удалены.**

```bash
hindsight-admin restore INPUT [OPTIONS]
```

**Аргументы:**

| Аргумент | Описание |
|----------|-------------|
| `INPUT` | Файл копии (.zip) |

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--schema`, `-s` | Схема базы для восстановления | `public` |
| `--yes`, `-y` | Пропустить запрос на подтверждение | `false` |

**Примеры:**

```bash
# Restore with confirmation prompt
hindsight-admin restore /backups/hindsight-2024-01-15.zip

# Restore without confirmation (for scripts)
hindsight-admin restore /backups/hindsight-2024-01-15.zip --yes

# Restore to a specific tenant schema
hindsight-admin restore /backups/tenant-acme.zip --schema tenant_acme --yes
```

:::warning Потеря данных
Перед импортом копии восстановление **удалит все текущие данные** в выбранной схеме. До запуска проверьте, что у вас есть свежая копия.
:::

---

### decommission-worker

Освободить все задачи обработчика: сменить их статус с `processing` на `pending`, чтобы их взяли другие обработчики.

```bash
hindsight-admin decommission-worker WORKER_ID [OPTIONS]
```

**Аргументы:**

| Аргумент | Описание |
|----------|-------------|
| `WORKER_ID` | ID обработчика, который нужно вывести из работы |

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--schema`, `-s` | Схема базы | `public` |
| `--yes`, `-y` | Пропустить запрос на подтверждение | `false` |

**Примеры:**

```bash
# Before scaling down - release tasks from workers being removed
hindsight-admin decommission-worker hindsight-worker-4
hindsight-admin decommission-worker hindsight-worker-3

# Release tasks from a crashed worker
hindsight-admin decommission-worker worker-2

# For a specific tenant schema
hindsight-admin decommission-worker worker-1 --schema tenant_acme
```

**Когда применять:**

- **Снижение числа обработчиков**: до удаления экземпляров в Kubernetes.
- **Штатное отключение**: перед остановкой обработчика для работ.
- **Восстановление после сбоя**: обработчик упал во время задачи.
- **Зависший обработчик**: не отвечает.

:::tip Как найти ID обработчика
По умолчанию ID — имя узла. В Kubernetes StatefulSet это имя пода, например `hindsight-worker-0`. Свой ID можно задать через `HINDSIGHT_API_WORKER_ID` или `--worker-id`.
:::


### decommission-workers

Освободить все задачи в работе у всех обработчиков: сменить `processing` на `pending`. Команда нужна, когда один или несколько обработчиков упали либо были удалены без штатной остановки, а их ID неизвестны.

```bash
hindsight-admin decommission-workers [OPTIONS]
```

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--schema`, `-s` | Схема базы | `public` |
| `--yes`, `-y` | Пропустить запрос на подтверждение | `false` |

**Примеры:**

```bash
# Release all processing tasks across all workers (with confirmation)
hindsight-admin decommission-workers

# Skip the confirmation prompt (useful in scripts)
hindsight-admin decommission-workers --yes

# Release tasks in a specific tenant schema
hindsight-admin decommission-workers --schema tenant_acme
```

**Когда применять:**

- **Неизвестны ID упавших обработчиков**: упало несколько процессов.
- **Общее восстановление**: после сбоя среды, затронувшего много обработчиков.
- **Быстрый сброс всей очереди**: когда нет смысла разбирать каждого обработчика отдельно.

:::warning Прерывает работающие задачи
Команда освобождает **все** задачи со статусом `processing`, в том числе у исправных обработчиков. Если вы знаете ID нужных обработчиков, берите `decommission-worker <WORKER_ID>`.
:::

---

### worker-status

Показывает все задачи в работе по обработчикам: вид операции, банк, сколько она уже длится и когда обновлялась. Помогает найти брошенные задачи до освобождения.

```bash
hindsight-admin worker-status [OPTIONS]
```

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--schema`, `-s` | Схема базы | `public` |

**Примеры:**

```bash
# Show all processing tasks across all workers
hindsight-admin worker-status

# Show processing tasks for a specific tenant schema
hindsight-admin worker-status --schema tenant_acme
```

**Когда применять:**

- **Перед выводом обработчика из работы**: увидеть, где есть старые задачи и сколько они висят.
- **Поиск причин медленной очереди**: понять, почему задачи не уходят (не застряли ли в `processing`).
- **Проверка обработчиков**: найти те, у которых `last_update_ago` растёт без остановки, что указывает на сбой или отсутствие ответа.

---

### export-bank

Выгружает весь банк в переносимый архив ZIP: документы, факты, наблюдения, настройки банка, ментальные модели, указания и вебхуки. Векторы **никогда не входят** в архив: при импорте они создаются заново. Это первый шаг переноса между экземплярами, например при смене модели векторов, расширения векторного поиска или движка поиска по тексту. Работает только с PostgreSQL.

```bash
hindsight-admin export-bank --bank <BANK_ID> --output <FILE.zip> [OPTIONS]
```

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--bank`, `-b` | ID банка для выгрузки. | Обязательно |
| `--output`, `-o` | Путь к архиву `.zip`. | Обязательно |
| `--schema`, `-s` | Схема, где лежит банк. | Основная схема |
| `--include-history` | Также выгрузить историю работы (`audit_log`, `llm_requests`). | `false` |

**Примеры:**

```bash
hindsight-admin export-bank --bank my-bank --output my-bank.zip

# include operational history
hindsight-admin export-bank --bank my-bank --output my-bank.zip --include-history
```

Команда лишь читает данные: её можно запускать на работающем экземпляре.

---

### import-bank

Восстанавливает полный архив банка, созданный `export-bank`, в **этом** экземпляре. Векторы фактов пересчитываются через настроенную здесь модель, связи и индексы строятся заново; настройки банка, модели, указания и вебхуки возвращаются точно. Извлечение фактов LLM не запускается. Поскольку перенос восстанавливает прежнее состояние, он **не** вызывает вебхуки и не запускает новое обобщение. Только PostgreSQL.

```bash
hindsight-admin import-bank --archive <FILE.zip> [OPTIONS]
```

**Параметры:**

| Параметр | Описание | По умолчанию |
|--------|-------------|---------|
| `--archive`, `-a` | Путь к `.zip` от `export-bank`. | Обязательно |
| `--schema`, `-s` | Целевая схема. | Основная схема |
| `--target-bank` | Задать другой ID банка (иначе берётся ID из архива). | Исходный банк |
| `--include-history` | Также восстановить историю, если она есть в архиве. | `false` |

**Примеры:**

```bash
hindsight-admin import-bank --archive my-bank.zip
```

Запускайте команду в экземпляре с **нужными новыми** моделью векторов, расширением векторного поиска и движком поиска по тексту: они будут взяты при пересчёте.

:::warning Целевого банка ещё не должно быть
Импорт восстанавливает **весь банк** (настройки, факты, модели и др.), а **не сливает** его с прежним. Если банк с целевым ID уже есть, команда завершится ошибкой. Сначала удалите такой банк либо через `--target-bank` задайте новый ID.
:::

---

## Перенос банка в новый экземпляр

У заполненного банка нельзя на месте сменить **модель векторов** (например, 384 измерения → 1024), **расширение векторного поиска** (pgvector / vchord / pgvectorscale) или **движок поиска по тексту**: сохранённые векторы и индексы связаны с этими настройками. Каждый вектор и индекс можно заново построить из текста на диске. Поэтому поддерживаемый путь — **перенести банк в новый экземпляр с нужными настройками и пересчитать всё там без нового извлечения фактов LLM**.

`export-bank` и `import-bank` переносят документы, факты, наблюдения, настройки банка, ментальные модели, указания и вебхуки, но не векторы. Целевой экземпляр создаёт векторы своей моделью.

**Порядок переноса с двумя экземплярами:**

1. Запустите **новый экземпляр** на новой базе с нужной моделью векторов, расширением и движком поиска по тексту.
2. Остановите запись в исходный банк на время работ и на всякий случай выполните `hindsight-admin backup`.
3. Выгрузите банк из исходного экземпляра и импортируйте в новый:
   ```bash
   # on the source instance:
   hindsight-admin export-bank --bank my-bank --output my-bank.zip
   # on the target instance (configured with the new settings):
   hindsight-admin import-bank --archive my-bank.zip
   ```
4. Проверьте новый экземпляр: выполните несколько типовых запросов recall и сравните ответы.
5. Переведите запросы на новый экземпляр. Старый пока сохраните для быстрого отката.

:::note Почему нужен новый экземпляр
Модель векторов задаётся на уровне сервера, а столбец `memory_units.embedding` у банка имеет один размер для всей схемы. Банку с другим размером векторов или другим движком нужен свой экземпляр и база. Старые векторы не меняются, поэтому откат прост.
:::

---

## Восстановление зависших и «зомби» операций

«Зомби» операция навсегда остаётся в `processing`, потому что взявший её обработчик исчез. Частая причина — непостоянный `HINDSIGHT_API_WORKER_ID`. По умолчанию это имя узла контейнера. После перезапуска Docker даёт новый ID контейнера; новый обработчик не считает задачи старого своими, и они остаются без исполнителя.

**Как найти:**

```bash
# List processing tasks grouped by worker — workers with a growing last_update_ago are dead
hindsight-admin worker-status

# Bank-level counters; pending_consolidation that never decreases is the usual symptom
curl -s http://localhost:8888/v1/default/banks/<bank_id>/stats
```

**Как восстановить:**

```bash
# You know which worker is dead (e.g. from worker-status):
hindsight-admin decommission-worker <old-worker-id>

# You don't know — release every processing task across the fleet:
hindsight-admin decommission-workers
```

Обе команды меняют `processing` на `pending`, чтобы работающий обработчик взял задачи при следующей проверке очереди.

**Как предотвратить:**

Задайте постоянный `HINDSIGHT_API_WORKER_ID`, чтобы ID обработчика не менялся при перезапуске:

- **Docker**: передайте `-e HINDSIGHT_API_WORKER_ID=hindsight-prod` (для нескольких контейнеров задайте каждому своё имя).
- **Kubernetes (Helm)**: StatefulSet в схеме сам берёт имя пода, новых настроек не нужно.
- **Свой сервер / pip**: передайте `--worker-id <name>` или задайте переменную среды для каждого процесса.

См. [«Установка — Docker»](./installation#docker) и [«Настройка — распределённые обработчики»](./configuration#distributed-workers).

---

## Переменные среды

Команды администратора берут те же переменные среды, что и служба API. Самая важная:

| Переменная | Описание | По умолчанию |
|----------|-------------|---------|
| `HINDSIGHT_API_DATABASE_URL` | Строка подключения к PostgreSQL | `pg0` (встроена) |

**Пример:**

```bash
# Use a specific database
export HINDSIGHT_API_DATABASE_URL=postgresql://user:pass@localhost:5432/hindsight
hindsight-admin backup /backups/mybackup.zip
```
