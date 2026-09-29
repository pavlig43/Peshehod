# Настройка

Полный справочник по настройке сервисов Hindsight с помощью переменных окружения.

У Hindsight два сервиса, у каждого свой префикс настроек:

| Сервис | Префикс | Описание |
|---------|---------|----------|
| **Сервис API** | `HINDSIGHT_API_*` | Основной движок памяти |
| **Control Plane** | `HINDSIGHT_CP_*` | Веб-интерфейс |

---

## Сервис API

Сервис API выполняет все операции с памятью: `retain`, `recall` и `reflect`.

### База данных

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_DATABASE_URL` | Строка подключения к PostgreSQL | `pg0` (встроенная БД) |
| `HINDSIGHT_API_READ_DATABASE_URL` | Необязательный адрес PostgreSQL-реплики только для чтения. Если задан, запросы `recall` (семантический поиск, BM25, граф и время) направляются в отдельный пул соединений, чтобы снизить нагрузку на основную БД. Обычно это адрес реплики, например сервис CNPG `<cluster>-ro` или reader endpoint Aurora. | Не задано (используется основная БД) |
| `HINDSIGHT_API_MIGRATION_DATABASE_URL` | Прямой адрес PostgreSQL для миграций в обход пулеров соединений (например, PgBouncer). При заданном значении блокировки и миграции Alembic используют этот адрес вместо `DATABASE_URL`. | Значение `DATABASE_URL` |
| `HINDSIGHT_API_DATABASE_SCHEMA` | Имя схемы PostgreSQL для таблиц | `public` |
| `HINDSIGHT_API_RUN_MIGRATIONS_ON_STARTUP` | Запускать миграции БД при старте API | `true` |
| `HINDSIGHT_API_MIGRATION_CONCURRENCY` | Число схем арендаторов, мигрируемых одновременно (только PostgreSQL). Каждая схема обрабатывается отдельным процессом; внутри схемы миграции всегда идут последовательно. Запуск отдельного процесса занимает около 1–2 секунд, поэтому параллельный режим полезен при большом числе схем (примерно от нескольких десятков) или медленных миграциях. Каждый процесс использует около трёх соединений с БД; оставьте запас в `max_connections` и пуле PgBouncer. Значение `1` означает последовательный режим. На 20 тысячах схем повторная проверка без изменений заняла около 11 минут при `concurrency=12` вместо примерно 60 минут (ускорение в 5 раз). | `1` |
| `HINDSIGHT_API_DATABASE_BACKEND` | Движок БД: `postgresql` или `oracle` (Oracle 23ai) | `postgresql` |

Если параметр не задан, сервер использует встроенную БД `pg0`. Это удобно для разработки, но не рекомендуется для рабочей среды.

Параметр `DATABASE_SCHEMA` позволяет использовать собственную схему PostgreSQL вместо `public`. Это подходит для:
- нескольких баз данных, когда таблицы Hindsight нужно хранить в отдельной схеме;
- хостингов (например, Supabase), где схема `public` зарезервирована или используется совместно;
- собственных правил именования схем.

```bash
# Пример: использование собственной схемы
export HINDSIGHT_API_DATABASE_URL=postgresql://user:pass@host:5432/dbname
export HINDSIGHT_API_DATABASE_SCHEMA=hindsight
```

Если схема не существует, миграции создадут её и все таблицы в ней.

### Пул соединений с БД

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_DB_POOL_MIN_SIZE` | Минимальное число соединений в основном пуле | `5` |
| `HINDSIGHT_API_DB_POOL_MAX_SIZE` | Максимальное число соединений в основном пуле | `100` |
| `HINDSIGHT_API_READ_DB_POOL_MIN_SIZE` | Минимальное число соединений в пуле реплики; используется только при заданном `READ_DATABASE_URL` | Значение `DB_POOL_MIN_SIZE` |
| `HINDSIGHT_API_READ_DB_POOL_MAX_SIZE` | Максимальное число соединений в пуле реплики; используется только при заданном `READ_DATABASE_URL` | Значение `DB_POOL_MAX_SIZE` |
| `HINDSIGHT_API_DB_COMMAND_TIMEOUT` | Тайм-аут команды PostgreSQL в секундах на стороне клиента `asyncpg` | `60` |
| `HINDSIGHT_API_DB_ACQUIRE_TIMEOUT` | Тайм-аут ожидания соединения | `30` |
| `HINDSIGHT_API_DB_STATEMENT_TIMEOUT` | Серверный `statement_timeout` для каждого соединения пула, в секундах. Защищает от зависших запросов. Не действует на миграции Alembic, которые используют отдельный движок `psycopg2`. Значение `0` отключает параметр. | `600` |

При высокой параллельной нагрузке увеличьте `DB_POOL_MAX_SIZE`. Каждая параллельная операция `recall` или `think` может занимать 2–4 соединения.

Чтобы выполнить миграции вручную (например, до запуска API), используйте административную CLI-команду:

```bash
# Мигрировать базовую схему и все найденные схемы арендаторов
hindsight-admin run-db-migration

# Или мигрировать только указанную схему:
hindsight-admin run-db-migration --schema tenant_acme
```

### Векторное расширение

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_VECTOR_EXTENSION` | Алгоритм векторного индекса: `pgvector`, `vchord`, `pgvectorscale` или `scann` | `pgvector` |

Hindsight поддерживает четыре векторных расширения PostgreSQL:

#### **pgvector** (HNSW — по умолчанию)
- Индекс в оперативной памяти на основе алгоритма Hierarchical Navigable Small World.
- Подходит для большинства моделей эмбеддингов и объёмов данных.
- Быстр для небольших и средних наборов данных (менее 10 млн векторов).
- На больших наборах требует больше памяти.
- Имеет наиболее широкую поддержку и распространение.

#### **pgvectorscale** (DiskANN — рекомендуется для больших объёмов) ⭐
- Индекс на диске на основе StreamingDiskANN.
- Заявлены в 28 раз меньшая задержка p95 и в 16 раз большая пропускная способность по сравнению с выделенными векторными БД.
- Экономия затрат при масштабировании составляет 60–75%: SSD дешевле оперативной памяти.
- Хорошо работает с фильтрами благодаря потоковой модели поиска.
- Оптимизирован для больших наборов данных (от 10 млн векторов).
- Поддерживает `pgvectorscale` (открытый код) и `pg_diskann` (Azure).
- **Установка:**
  - Открытый код или собственный сервер: `CREATE EXTENSION vector; CREATE EXTENSION vectorscale CASCADE;`
  - Azure PostgreSQL: `CREATE EXTENSION vector; CREATE EXTENSION pg_diskann CASCADE;`

#### **vchord** (vchordrq)
- Альтернативный высокопроизводительный векторный индекс.
- Оптимизирован для эмбеддингов большой размерности (от 3000 измерений).
- Включает встроенный поиск BM25 по тексту.
- Требует расширение `vchord`.

#### **scann** (AlloyDB ScaNN)
- Индекс Google ScaNN, доступный в **AlloyDB** и **AlloyDB Omni**.
- В режиме `AUTO` используется один общий векторный индекс; частичные индексы для отдельных банков не применяются.
- **Установка:** `CREATE EXTENSION vector; CREATE EXTENSION alloydb_scann CASCADE;`
- Построение индекса откладывается, пока в таблице не будет **10 000 заполненных строк с эмбеддингами**: AlloyDB не может создать ScaNN AUTO-индекс для почти пустой таблицы. До достижения порога поиск `recall` использует последовательное сканирование. Общий индекс будет создан при следующем запуске API после появления достаточного числа строк.
- Готовая конфигурация Docker Compose для локального запуска Hindsight с AlloyDB Omni: [`docker/docker-compose/alloydb/docker-compose.yaml`](https://github.com/vectorize-io/hindsight/blob/main/docker/docker-compose/alloydb/docker-compose.yaml).

**Когда выбирать pgvectorscale (DiskANN):** большие наборы данных (от 10 млн векторов), сложные фильтры, ограниченный бюджет и рабочие нагрузки с высокой пропускной способностью, если дисковый ввод-вывод не является узким местом.

**Когда выбирать pgvector (HNSW):** небольшие и средние наборы (менее 10 млн векторов), максимальная скорость при размещении данных в памяти, простой поиск ближайших соседей без фильтров или стандартная установка PostgreSQL.

**Когда выбирать vchord:** эмбеддинги размерностью от 3000, необходимость встроенного BM25 или если `vchord` уже используется для текстового поиска.

**Когда выбирать scann:** при работе в Google **AlloyDB** или **AlloyDB Omni**, если нужен управляемый ScaNN с настройкой режима `AUTO`.

**Смена расширения:**
1. Задайте `HINDSIGHT_API_VECTOR_EXTENSION` равным нужному расширению: `pgvector`, `vchord`, `pgvectorscale` или `scann`.
2. Если в БД уже есть данные, сервер сообщит об этом и покажет инструкции по миграции. Переход **на** `scann` разрешён и при наличии данных: существующий индекс будет удалён и перестроен как ScaNN, когда в таблице появится не менее 10 000 строк с эмбеддингами.
3. В пустой БД индексы будут созданы заново при запуске.

**Дополнительные материалы:** [сравнение HNSW и DiskANN](https://www.tigerdata.com/learn/hnsw-vs-diskann), [pgvectorscale на GitHub](https://github.com/timescale/pgvectorscale).

### Расширение текстового поиска

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_TEXT_SEARCH_EXTENSION` | Движок текстового поиска: `native`, `vchord`, `pg_textsearch`, `pgroonga` или `pg_search` | `native` |
| `HINDSIGHT_API_TEXT_SEARCH_EXTENSION_NATIVE_LANGUAGE` | Словарь PostgreSQL для движка `native` (например, `english`, `french`, `simple`, `zhparser`) | `english` |
| `HINDSIGHT_API_TEXT_SEARCH_EXTENSION_PG_SEARCH_TOKENIZER` | Токенизатор ParadeDB `pg_search` при создании индексов BM25. Пустое значение включает токенизатор ParadeDB по умолчанию (`unicode_words`). | Не задано |
| `HINDSIGHT_API_LLM_OUTPUT_LANGUAGE` | Если задано, все создаваемые LLM материалы (факты `retain`, наблюдения консолидации и ответы `reflect`) будут на этом языке. Можно указать любой язык, например `Spanish` или `Japanese`. | Не задано |

Hindsight поддерживает пять движков для поиска по ключевым словам BM25:
- **native** — встроенный полнотекстовый поиск PostgreSQL (`tsvector` + GIN); язык можно настроить.
- **vchord** — VectorChord BM25 с многоязычным токенизатором `llmlingua2`.
- **pg_textsearch** — расширение Timescale `pg_textsearch`, только английский язык.
- **pgroonga** — полнотекстовый поиск `pgroonga` с поддержкой многих языков и CJK.
- **pg_search** — ParadeDB `pg_search`, полноценный BM25; единственный движок с поддержкой Citus.

Чтобы сменить движок, задайте `HINDSIGHT_API_TEXT_SEARCH_EXTENSION`. Если данные уже есть, сервер выведет ошибку и инструкции миграции. В пустой БД столбцы и индексы автоматически пересоздаются при запуске.

`HINDSIGHT_API_TEXT_SEARCH_EXTENSION_PG_SEARCH_TOKENIZER` действует только при `HINDSIGHT_API_TEXT_SEARCH_EXTENSION=pg_search` и создании индексов BM25. Для уже созданной БД смена токенизатора требует перестроить индексы `pg_search` или пересоздать БД. Поддерживаемые значения: пустое или не задано, `unicode_words`, `simple`, `whitespace`, `literal`, `literal_normalized`, `chinese_compatible`, `icu`, `jieba`, `source_code`, `chinese_lindera`/`lindera(chinese)`, `japanese_lindera`/`lindera(japanese)`, `korean_lindera`/`lindera(korean)`, `ngram(min,max)` и `edge_ngram(min,max)`.

О нетекстовых банках, особенно CJK, а также о выборе языка и языка извлечения см. страницу [многоязычной поддержки](./multilingual).

### Поставщик LLM

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_LLM_PROVIDER` | Поставщик: `openai`, `openai-codex`, `claude-code`, `anthropic`, `gemini`, `groq`, `minimax`, `deepseek`, `zai`, `opencode-go`, `nous`, `fireworks`, `ollama`, `ollama-cloud`, `lmstudio`, `llamacpp`, `vertexai`, `bedrock`, `litellm`, `litellmrouter`, `volcano`, `openrouter`, `none` | `openai` |
| `HINDSIGHT_API_LLM_API_KEY` | Ключ API поставщика LLM | — |
| `HINDSIGHT_API_LLM_MODEL` | Имя модели | `gpt-5-mini` |
| `HINDSIGHT_API_LLM_BASE_URL` | Адрес собственного сервера LLM | Адрес поставщика |
| `HINDSIGHT_API_LLM_MAX_CONCURRENT` | Максимум одновременных запросов к LLM | `32` |
| `HINDSIGHT_API_LLM_MAX_RETRIES` | Максимум повторных попыток запроса к API LLM | `3` |
| `HINDSIGHT_API_LLM_INITIAL_BACKOFF` | Начальная задержка перед повтором, в секундах; используется экспоненциальное увеличение | `1.0` |
| `HINDSIGHT_API_LLM_MAX_BACKOFF` | Максимальная задержка перед повтором, в секундах | `60.0` |
| `HINDSIGHT_API_LLM_TIMEOUT` | Тайм-аут запроса к LLM, в секундах | `120` |
| `HINDSIGHT_API_LLM_REASONING_EFFORT` | Уровень рассуждений для поддерживающих его поставщиков и моделей: `low`, `medium`, `high`, `xhigh` и др. | `low` |
| `HINDSIGHT_API_LLM_SEND_BANK_AS_USER` | Добавлять к запросам LLM и эмбеддингов метку `user=<bank_id>`, чтобы шлюзы (учёт расходов OpenRouter, LiteLLM, Helicone) могли вести учёт по банкам. При включении идентификатор банка передаётся поставщику как идентификатор конечного пользователя. | `false` |
| `HINDSIGHT_API_LLM_GROQ_SERVICE_TIER` | Уровень обслуживания Groq: `on_demand`, `flex` или `auto` | `auto` |
| `HINDSIGHT_API_LLM_OPENAI_SERVICE_TIER` | Уровень обслуживания OpenAI: `flex` снижает стоимость на 50% благодаря Flex Processing | Не задано |
| `HINDSIGHT_API_LLM_BEDROCK_SERVICE_TIER` | Уровень Bedrock: `flex` — экономичный режим с переменной задержкой, `priority` — гарантированная пропускная способность, `reserved` — выделенная ёмкость | Не задано (уровень по умолчанию) |
| `HINDSIGHT_API_LLM_GEMINI_SERVICE_TIER` | Уровень Gemini: `flex` снижает стоимость на 50%, но гарантии выполнения отсутствуют | Не задано |
| `HINDSIGHT_API_LLM_EXTRA_BODY` | JSON-объект дополнительных параметров тела запроса (например, `temperature`, `top_p`, `max_tokens`), объединяемый со всеми вызовами LLM. Действует для OpenAI-совместимых API, Fireworks, Anthropic, Gemini/Vertex AI и LiteLLM (включая Bedrock и Router). Используйте имена параметров, принятые у поставщика: например, `max_tokens` для OpenAI/Anthropic и `max_output_tokens` для Gemini. Подходит также для собственных серверов моделей, например для `chat_template_kwargs` в vLLM. | `null` |
| `HINDSIGHT_API_LLM_DEFAULT_HEADERS` | JSON-объект заголовков, передаваемый SDK поставщика как `default_headers`. Нужен, например, при работе через прокси и средства трассировки запросов: Cloudflare AI Gateway, Helicone или корпоративный прокси. Сейчас подключён к поставщику Anthropic; другие поставщики могут добавить его поддержку. | `null` |
| `HINDSIGHT_API_LLM_STRICT_SCHEMA` | Строго требовать структурированный ответ по `json_schema` с `strict: true` вместо мягкой схемы в подсказке и режима `json_object`. Полезно для слабых локальных моделей, которые добавляют текст до JSON, Markdown-обрамление ` ```json ` или возвращают некорректный JSON, из-за чего не удаются `retain` и консолидация. Действует для OpenAI-совместимых серверов (OpenAI, llama.cpp, vLLM) и LiteLLM. Gemini в любом случае применяет собственную `response_schema`; поставщики без строгого режима игнорируют параметр. | `false` |
| `HINDSIGHT_API_LLM_GEMINI_SAFETY_SETTINGS` | Список JSON-объектов `{category, threshold}` для фильтрации безопасного содержимого в Gemini/Vertex AI | `null` |
| `HINDSIGHT_API_LLM_PROMPT_CACHE_ENABLED` | Кэшировать неизменяемый системный префикс средствами поставщика и оплачивать его по тарифу кэшированного ввода (Gemini/Vertex `CachedContent`). Кэш общий для всех банков; при сбое запрос выполняется без кэша. Значение `false` отключает кэш. См. [описание моделей](./models#provider-capabilities). | `true` |

**Примеры настройки поставщиков**

```bash
# Groq (рекомендуется для быстрого вывода)
export HINDSIGHT_API_LLM_PROVIDER=groq
export HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=openai/gpt-oss-20b
# Для бесплатного тарифа при ошибках service_tier задайте on_demand
# export HINDSIGHT_API_LLM_GROQ_SERVICE_TIER=on_demand

# OpenAI
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gpt-4o
# Необязательно: Flex Processing снижает стоимость на 50%, задержка может меняться
# export HINDSIGHT_API_LLM_OPENAI_SERVICE_TIER=flex

# Gemini
export HINDSIGHT_API_LLM_PROVIDER=gemini
export HINDSIGHT_API_LLM_API_KEY=xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gemini-2.0-flash
# Необязательно: экономичный режим Gemini Flex
# export HINDSIGHT_API_LLM_GEMINI_SERVICE_TIER=flex

# Anthropic
export HINDSIGHT_API_LLM_PROVIDER=anthropic
export HINDSIGHT_API_LLM_API_KEY=sk-ant-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=claude-sonnet-4-20250514

# Vertex AI (Google Cloud, используется нативный genai SDK)
export HINDSIGHT_API_LLM_PROVIDER=vertexai
export HINDSIGHT_API_LLM_MODEL=gemini-2.0-flash-001
export HINDSIGHT_API_LLM_VERTEXAI_PROJECT_ID=your-gcp-project-id
export HINDSIGHT_API_LLM_VERTEXAI_REGION=us-central1
# Необязательно: используйте ADC (gcloud auth application-default login) или ключ сервисной учётной записи
# export HINDSIGHT_API_LLM_VERTEXAI_SERVICE_ACCOUNT_KEY=/path/to/service-account-key.json

# Ollama (локально, ключ API не нужен)
export HINDSIGHT_API_LLM_PROVIDER=ollama
export HINDSIGHT_API_LLM_BASE_URL=http://localhost:11434/v1
export HINDSIGHT_API_LLM_MODEL=llama3

# Встроенный llama.cpp: внешний сервер не нужен; при первом запуске загружается Gemma 4 E2B (~3,5 ГБ GGUF)
export HINDSIGHT_API_LLM_PROVIDER=llamacpp

# OpenAI-совместимый сервер
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_BASE_URL=https://your-endpoint.com/v1
export HINDSIGHT_API_LLM_API_KEY=your-api-key
export HINDSIGHT_API_LLM_MODEL=your-model-name

# OpenAI Codex (подписка ChatGPT Plus/Pro, OAuth; ключ API не нужен)
export HINDSIGHT_API_LLM_PROVIDER=openai-codex
export HINDSIGHT_API_LLM_MODEL=gpt-5.4-mini

# Claude Code (подписка Claude Pro/Max, OAuth; ключ API не нужен)
export HINDSIGHT_API_LLM_PROVIDER=claude-code
export HINDSIGHT_API_LLM_MODEL=claude-sonnet-4-5-20250929

# Без LLM: хранение фрагментов и семантический поиск
export HINDSIGHT_API_LLM_PROVIDER=none
# Retain автоматически работает в режиме фрагментов — без извлечения фактов
# Recall продолжает работать: семантический поиск, BM25 и поиск по графу
# Reflect возвращает HTTP 400, так как требует LLM
# Консолидация и наблюдения отключены
```

:::tip Настройка OpenAI Codex, Claude Code и Vertex AI
Подробные инструкции по настройке **OpenAI Codex** (ChatGPT Plus/Pro), **Claude Code** (Claude Pro/Max) и **Vertex AI** (Google Cloud) приведены в [документации моделей](./models#openai-codex-setup-chatgpt-pluspro).
:::

### Маршрутизатор LLM (LiteLLM Router)

При `HINDSIGHT_API_LLM_PROVIDER=litellmrouter` запросы по умолчанию направляются через [`Router` в LiteLLM](https://docs.litellm.ai/docs/routing). JSON-конфигурация передаётся без изменений. Цепочки резервных моделей, балансировка нагрузки, ограничения частоты, стратегии маршрутизации и другие параметры описаны в [документации LiteLLM Router](https://docs.litellm.ai/docs/routing). Hindsight всегда отправляет запросы для `model_name: "default"`, поэтому конфигурация должна содержать как минимум одну запись с таким именем.

| Переменная | Описание |
|------------|----------|
| `HINDSIGHT_API_LLM_LITELLMROUTER_CONFIG` | JSON-объект, передаваемый в `litellm.Router(**config)`. Обязателен при выборе `litellmrouter`. |
| `HINDSIGHT_API_{RETAIN,REFLECT,CONSOLIDATION}_LLM_LITELLMROUTER_CONFIG` | Отдельные настройки для операций. Если не заданы, используется общая конфигурация. |

```bash
export HINDSIGHT_API_LLM_PROVIDER=litellmrouter
export HINDSIGHT_API_LLM_LITELLMROUTER_CONFIG='{
  "model_list": [
    {"model_name": "default",  "litellm_params": {"model": "openai/gpt-4o-mini", "api_key": "sk-..."}},
    {"model_name": "fallback", "litellm_params": {"model": "anthropic/claude-sonnet-4-5", "api_key": "sk-ant-..."}}
  ],
  "fallbacks": [{"default": ["fallback"]}]
}'
```

Конфигурация содержит учётные данные и никогда не возвращается API настроек банка. Hindsight уже повторяет неудачные запросы; задайте в конфигурации Router `"num_retries": 0`, чтобы не повторять их дважды. Пакетные API в режиме Router не поддерживаются.

### Несколько LLM: резервирование и циклическая балансировка

Дополнительные LLM настраиваются **по индексам** рядом с основной; стратегия определяет распределение запросов. Это не зависит от поставщика: в цепочке можно сочетать разные полностью настроенные модели.

Настройки `HINDSIGHT_API_LLM_*` без индекса относятся к **основной** модели. Дополнительные модели нумеруются с 1:

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_LLM_<n>_PROVIDER` | Поставщик дополнительной модели `n` (`n` = 1, 2, ...). Наличие этой переменной объявляет модель; индексы должны идти подряд от 1. | — |
| `HINDSIGHT_API_LLM_<n>_API_KEY` | Ключ API модели `n`, кроме поставщиков, которым он не требуется | — |
| `HINDSIGHT_API_LLM_<n>_MODEL` | Модель `n` | Значение поставщика |
| `HINDSIGHT_API_LLM_<n>_BASE_URL` | Адрес модели `n` | Значение поставщика |
| `HINDSIGHT_API_LLM_<n>_REASONING_EFFORT` | Уровень рассуждений модели `n` | `HINDSIGHT_API_LLM_REASONING_EFFORT` |
| `HINDSIGHT_API_LLM_<n>_EXTRA_BODY` / `_DEFAULT_HEADERS` | JSON-переопределения для модели `n` | — |
| `HINDSIGHT_API_LLM_<n>_BEDROCK_SERVICE_TIER` / `_GEMINI_SERVICE_TIER` | Уровень обслуживания модели `n` | — |
| `HINDSIGHT_API_LLM_<n>_VERTEXAI_PROJECT_ID` / `_VERTEXAI_REGION` / `_VERTEXAI_SERVICE_ACCOUNT_KEY` | Проект, регион и путь к ключу сервисной учётной записи Vertex AI для модели `vertexai`. Если не заданы, используются общие значения `HINDSIGHT_API_LLM_VERTEXAI_*`. | Общие настройки / `us-central1` / ADC |
| `HINDSIGHT_API_LLM_<n>_LITELLMROUTER_CONFIG` | JSON-конфигурация LiteLLM Router для модели `litellmrouter`; если не задана, используется `HINDSIGHT_API_LLM_LITELLMROUTER_CONFIG`. | — |
| `HINDSIGHT_API_LLM_STRATEGY` | JSON-стратегия маршрутизации запросов между моделями. Если не задана, используется только основная модель. | — |

Стратегия JSON имеет два режима:
- `{"mode": "failover"}` — модели проверяются по порядку, начиная с основной; после неудачи и всех повторов запрос передаётся следующей.
- `{"mode": "round-robin"}` — для каждого запроса меняется начальная модель, а при ошибке используются остальные. Массив положительных целых чисел `"weights": [3, 1, ...]` задаёт неравные доли запросов; порядок соответствует списку моделей, начиная с основной.

```bash
# Основная OpenAI; резервные модели — Groq и Anthropic
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_API_KEY=sk-...
export HINDSIGHT_API_LLM_1_PROVIDER=groq
export HINDSIGHT_API_LLM_1_API_KEY=gsk-...
export HINDSIGHT_API_LLM_2_PROVIDER=anthropic
export HINDSIGHT_API_LLM_2_API_KEY=sk-ant-...
export HINDSIGHT_API_LLM_STRATEGY='{"mode": "failover"}'

# Циклическое распределение: основная модель получает втрое больше запросов
export HINDSIGHT_API_LLM_STRATEGY='{"mode": "round-robin", "weights": [3, 1]}'
```

Для `retain`, `reflect` и `consolidation` можно задать собственный список моделей и стратегию с соответствующим префиксом, например `HINDSIGHT_API_RETAIN_LLM_1_PROVIDER` и `HINDSIGHT_API_RETAIN_LLM_STRATEGY`. Если для операции не задано ни одной дополнительной модели или стратегия, используется общий список. Эти переменные содержат учётные данные и доступны только на уровне сервера; API настроек банка их не возвращает, а сами банки не могут их переопределять. Пакетный `retain` использует только основную модель; резервирование и балансировка действуют для обычных вызовов `retain`, `reflect` и консолидации.

### Встроенный llama.cpp

Поставщик `llamacpp` запускает сервер llama.cpp как управляемый дочерний процесс: отдельный сервер LLM не нужен. При первом запуске автоматически загружается модель GGUF по умолчанию (около 3,5 ГБ). Требуется дополнительный пакет `local-llm`: `pip install 'hindsight-api-slim[local-llm]'`.

| Переменная | Описание | Значение по умолчанию |
|------------|----------|-----------------------|
| `HINDSIGHT_API_LLAMACPP_MODEL_PATH` | Путь к файлу модели GGUF. Если не задан, с HuggingFace загружается `gemma-4-E2B-it-Q4_K_M`. | Автозагрузка |
| `HINDSIGHT_API_LLAMACPP_GPU_LAYERS` | Число слоёв для переноса на GPU. `-1` — все слои (рекомендуется), `0` — только CPU. | `-1` |
| `HINDSIGHT_API_LLAMACPP_CONTEXT_SIZE` | Размер контекста в токенах | `8192` |
| `HINDSIGHT_API_LLAMACPP_CHAT_FORMAT` | Формат шаблона диалога. `null` автоматически определяет его по метаданным GGUF (рекомендуется). | Автоопределение |
| `HINDSIGHT_API_LLAMACPP_NO_GRAMMAR` | Отключить ограничение грамматикой JSON. Генерация ускорится, но надёжность JSON снизится. | `false` |
| `HINDSIGHT_API_LLAMACPP_EXTRA_ARGS` | Дополнительные аргументы командной строки через пробел, например `--n_threads 8 --type_k 1` | — |

```bash
# Минимальная настройка: модель загрузится автоматически и будет использовать GPU
export HINDSIGHT_API_LLM_PROVIDER=llamacpp

# Собственная модель и параметры
export HINDSIGHT_API_LLM_PROVIDER=llamacpp
export HINDSIGHT_API_LLM_MAX_CONCURRENT=2
export HINDSIGHT_API_LLAMACPP_MODEL_PATH=~/.hindsight/models/my-model.gguf
export HINDSIGHT_API_LLAMACPP_CONTEXT_SIZE=16384
export HINDSIGHT_API_LLAMACPP_NO_GRAMMAR=true  # быстрее, но JSON менее надёжен
export HINDSIGHT_API_LLAMACPP_EXTRA_ARGS="--n_threads 8"
```

:::note
Один сервер llama.cpp используется всеми операциями LLM: `retain`, `reflect` и консолидацией. Значение `HINDSIGHT_API_LLM_MAX_CONCURRENT=2` позволяет `retain` и консолидации выполняться одновременно.
:::

### Отдельные настройки LLM для операций

Требования операций различаются. Для **Retain** (извлечения фактов) полезны модели, которые хорошо формируют структурированные ответы. Для **Reflect** (рассуждений и генерации ответа) можно выбрать более быструю и дешёвую модель. Настройте отдельные модели для каждой операции, чтобы управлять стоимостью и скоростью.

Для каждой из операций `RETAIN`, `REFLECT` и `CONSOLIDATION` доступны параметры `LLM_PROVIDER`, `LLM_API_KEY`, `LLM_MODEL`, `LLM_BASE_URL`, `LLM_MAX_CONCURRENT`, `LLM_MAX_RETRIES`, `LLM_INITIAL_BACKOFF`, `LLM_MAX_BACKOFF` и `LLM_TIMEOUT` с соответствующим префиксом, например `HINDSIGHT_API_RETAIN_LLM_MODEL`. Если параметр операции не задан, используется одноимённый общий `HINDSIGHT_API_LLM_*`. Предел `*_MAX_CONCURRENT` операции дополняет общий предел: вызов `retain` учитывается одновременно в лимите retain и общем лимите. Для `reflect`, если собственного лимита нет, действует только общий.

:::tip Когда настраивать LLM отдельно
- **Retain:** выбирайте модели с надёжным структурированным выводом (например, GPT-4o или Claude) для точного извлечения фактов.
- **Reflect:** используйте быстрые и недорогие модели (например, GPT-4o-mini или Groq) для рассуждения и формирования ответа.
- **Recall:** LLM не использует, это только поиск; дополнительная настройка не нужна.
:::

```bash
# Общая модель используется как запасная
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gpt-4o

# Для retain — GPT-4o с надёжным структурированным выводом
export HINDSIGHT_API_RETAIN_LLM_MODEL=gpt-4o

# Для reflect — более быстрая и дешёвая модель
export HINDSIGHT_API_REFLECT_LLM_PROVIDER=groq
export HINDSIGHT_API_REFLECT_LLM_API_KEY=gsk_xxxxxxxxxxxx
export HINDSIGHT_API_REFLECT_LLM_MODEL=llama-3.3-70b-versatile
```

Если у поставщика строгие ограничения частоты запросов, уменьшите параллельность и число повторов либо увеличьте задержку между повторами:

```bash
# Пример для Anthropic с ограничением 10 000 выходных токенов в минуту
export HINDSIGHT_API_LLM_PROVIDER=anthropic
export HINDSIGHT_API_LLM_API_KEY=sk-ant-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=claude-sonnet-4-20250514

# Меньше параллельных запросов retain
export HINDSIGHT_API_RETAIN_LLM_MAX_CONCURRENT=3

# Меньше повторов
export HINDSIGHT_API_RETAIN_LLM_MAX_RETRIES=3

# Или увеличьте интервалы ожидания между повторами
export HINDSIGHT_API_RETAIN_LLM_INITIAL_BACKOFF=2.0  # 2 секунды вместо 1
export HINDSIGHT_API_RETAIN_LLM_MAX_BACKOFF=120.0    # максимум 2 минуты вместо 1
```

Лимиты `HINDSIGHT_API_RETAIN_LLM_MAX_CONCURRENT`, `HINDSIGHT_API_REFLECT_LLM_MAX_CONCURRENT` и `HINDSIGHT_API_CONSOLIDATION_LLM_MAX_CONCURRENT` применяются **дополнительно** к `HINDSIGHT_API_LLM_MAX_CONCURRENT`. Например, при общем лимите 4, лимите retain 1 и лимите консолидации 1 для интерактивных вызовов остаются два свободных слота.

### Эмбеддинги

Параметры поставщика, учётных данных, адреса, модели, размерности, формата, размера пакета и задержки эмбеддингов задаются на уровне сервера. Это статические настройки, их нельзя переопределить отдельно для банка.

Основные параметры:

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_EMBEDDINGS_PROVIDER` | Поставщик: `local`, `onnx`, `tei`, `openai`, `openai-codex`, `openrouter`, `cohere`, `google`, `zeroentropy`, `litellm` или `litellm-sdk` | `local` |
| `HINDSIGHT_API_EMBEDDINGS_LOCAL_MODEL` | Модель локального поставщика | `BAAI/bge-small-en-v1.5` |
| `HINDSIGHT_API_EMBEDDINGS_LOCAL_TRUST_REMOTE_CODE` | Разрешить загрузку моделей с собственным кодом (риск безопасности; по умолчанию выключено) | `false` |
| `HINDSIGHT_API_EMBEDDINGS_LOCAL_FORCE_CPU` | Принудительно использовать CPU для локальных эмбеддингов; помогает избежать проблем MPS/XPC в macOS | `false` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_ID` | Репозиторий модели Hugging Face для ONNX; используется для автозагрузки и как запасной источник токенизатора | `intfloat/multilingual-e5-small` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_PATH` | Путь к локальному графу ONNX. Если не задан, Hindsight загружает файл `HINDSIGHT_API_EMBEDDINGS_ONNX_FILE` из модели `HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_ID`. | — |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_TOKENIZER_NAME_OR_PATH` | Репозиторий Hugging Face или локальная папка токенизатора; задайте его при использовании локального `ONNX_MODEL_PATH` | По умолчанию `ONNX_MODEL_ID` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_FILE` | Путь к ONNX-файлу в репозитории модели. Если рядом есть файл внешних данных с суффиксом `_data`, он тоже загружается. | `onnx/model.onnx` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_DIMENSIONS` | Ожидаемая размерность эмбеддингов; запуск завершится ошибкой, если модель вернёт другой размер | Автоопределение |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_MAX_TOKENS` | Максимальная длина токенизации для ONNX | `512` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_POOLING` | Способ объединения токенов: `mean` или `cls`. Игнорируется, если граф возвращает готовый двумерный эмбеддинг. | `mean` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_NORMALIZE` | Нормализовать векторы ONNX по L2 перед сохранением | `true` |
| `HINDSIGHT_API_EMBEDDINGS_ONNX_QUERY_PREFIX` / `HINDSIGHT_API_EMBEDDINGS_ONNX_PASSAGE_PREFIX` | Префиксы текста запроса и сохраняемого текста. Для E5 оставьте `query: ` и `passage: `; для MiniLM и BGE задайте пустые строки. | `query: ` / `passage: ` |
| `HINDSIGHT_API_EMBEDDINGS_TEI_URL` | Адрес сервера TEI | — |
| `HINDSIGHT_API_EMBEDDINGS_OPENAI_API_KEY` / `_OPENAI_MODEL` / `_OPENAI_BASE_URL` | Ключ, модель и собственный адрес OpenAI-совместимого API, например Azure OpenAI. Ключ использует общий `HINDSIGHT_API_LLM_API_KEY`, если не задан. | `text-embedding-3-small` |
| `HINDSIGHT_API_EMBEDDINGS_OPENAI_BATCH_SIZE` | Максимум входных данных в одном вызове `embeddings.create` для поставщиков `openai` и `openrouter`; уменьшите при более строгих ограничениях сервера | `100` |
| `HINDSIGHT_API_EMBEDDINGS_OPENAI_DIMENSIONS` | Необязательная выходная размерность моделей OpenAI `text-embedding-3`, например `384` для совместимости с существующей схемой pgvector | — |
| `HINDSIGHT_API_EMBEDDINGS_OPENROUTER_API_KEY` / `_OPENROUTER_MODEL` | Ключ OpenRouter (при отсутствии берётся из `HINDSIGHT_API_OPENROUTER_API_KEY`, затем `HINDSIGHT_API_LLM_API_KEY`) и модель эмбеддингов | `perplexity/pplx-embed-v1-0.6b` |
| `HINDSIGHT_API_EMBEDDINGS_ZEROENTROPY_API_KEY` / `_MODEL` / `_BASE_URL` | Ключ, модель ZeroEntropy и собственный адрес совместимого API | `zembed-1` / `https://api.zeroentropy.dev` |
| `HINDSIGHT_API_EMBEDDINGS_ZEROENTROPY_DIMENSIONS` | Размерность `zembed-1`: `2560`, `1280`, `640`, `320`, `160`, `80` или `40` | `1280` |
| `HINDSIGHT_API_EMBEDDINGS_ZEROENTROPY_ENCODING_FORMAT` / `_LATENCY` | Формат ответа `float` или `base64`; режим задержки `fast` или `slow`. Hindsight декодирует оба формата. | `float` / маршрут поставщика |
| `HINDSIGHT_API_EMBEDDINGS_COHERE_API_KEY` / `_MODEL` / `_BASE_URL` | Ключ Cohere (по умолчанию берётся из `HINDSIGHT_API_COHERE_API_KEY`), модель и адрес совместимого API, например Azure | `embed-english-v3.0` |
| `HINDSIGHT_API_EMBEDDINGS_COHERE_OUTPUT_DIMENSIONS` | Размерность Cohere, например `256`, `512` или `1024`; переопределяет размер модели по умолчанию | — |
| `HINDSIGHT_API_EMBEDDINGS_LITELLM_API_BASE` / `_API_KEY` / `_MODEL` | Адрес прокси LiteLLM, необязательный ключ и модель эмбеддингов с префиксом поставщика | `http://localhost:4000` / `text-embedding-3-small` |
| `HINDSIGHT_API_EMBEDDINGS_LITELLM_SDK_API_KEY` / `_MODEL` / `_API_BASE` | Ключ, модель и собственный адрес прямого доступа через SDK LiteLLM. Ключ можно не задавать для поставщиков с учётными данными окружения, например AWS Bedrock IAM. | `cohere/embed-english-v3.0` |
| `HINDSIGHT_API_EMBEDDINGS_LITELLM_SDK_OUTPUT_DIMENSIONS` / `_ENCODING_FORMAT` | Необязательная выходная размерность (зависит от поставщика) и формат ответа. Чтобы не передавать формат, задайте пустую строку; это нужно для Voyage AI и Gemini. | `float` |
| `HINDSIGHT_API_EMBEDDINGS_GEMINI_API_KEY` / `_MODEL` / `_OUTPUT_DIMENSIONALITY` / `_FORCE_IPV4` | Ключ Gemini (по умолчанию общий ключ LLM), модель, выходная размерность и принудительное использование IPv4 при проблемах с IPv6 | `gemini-embedding-001` / `768` / `false` |
| `HINDSIGHT_API_EMBEDDINGS_VERTEXAI_PROJECT_ID` / `_REGION` / `_SERVICE_ACCOUNT_KEY` | Проект, регион и ключ сервисной учётной записи Vertex AI; при отсутствии используются значения соответствующих настроек LLM | Настройки LLM |

#### Локальные эмбеддинги ONNX

Поставщик ONNX запускает модели эмбеддингов в процессе API через ONNX Runtime. При собственной сборке API установите дополнительную зависимость:

```bash
pip install 'hindsight-api-slim[local-onnx]'
# или из этого репозитория:
uv sync --project hindsight-api-slim --extra local-onnx
```

Задайте `HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_ID`, чтобы Hindsight загрузил модель с Hugging Face при старте. Либо заранее скачайте граф ONNX и файлы токенизатора в корень репозитория:

```bash
cd /path/to/hindsight
mkdir -p models

MODEL_ID=intfloat/multilingual-e5-small
MODEL_DIR=models/intfloat__multilingual-e5-small

uv run --project hindsight-api-slim --extra local-onnx python - <<'PY'
import os
from huggingface_hub import snapshot_download

snapshot_download(
    repo_id=os.environ["MODEL_ID"],
    local_dir=os.environ["MODEL_DIR"],
    allow_patterns=[
        "onnx/model.onnx",
        "onnx/model.onnx_data",
        "*.json",
        "*.txt",
        "*.model",
    ],
)
PY
```

Задайте пути относительно корня репозитория и запустите Hindsight:

```bash
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=onnx
export HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_PATH=./models/intfloat__multilingual-e5-small/onnx/model.onnx
export HINDSIGHT_API_EMBEDDINGS_ONNX_TOKENIZER_NAME_OR_PATH=./models/intfloat__multilingual-e5-small
export HINDSIGHT_API_EMBEDDINGS_ONNX_DIMENSIONS=384
export HINDSIGHT_API_EMBEDDINGS_ONNX_QUERY_PREFIX="query: "
export HINDSIGHT_API_EMBEDDINGS_ONNX_PASSAGE_PREFIX="passage: "
```

Для Docker подключите ту же папку моделей и укажите пути внутри контейнера:

```yaml
services:
  hindsight:
    volumes:
      - ./models:/app/models:ro
    environment:
      HINDSIGHT_API_EMBEDDINGS_PROVIDER: onnx
      HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_PATH: /app/models/intfloat__multilingual-e5-small/onnx/model.onnx
      HINDSIGHT_API_EMBEDDINGS_ONNX_TOKENIZER_NAME_OR_PATH: /app/models/intfloat__multilingual-e5-small
      HINDSIGHT_API_EMBEDDINGS_ONNX_DIMENSIONS: "384"
      HINDSIGHT_API_EMBEDDINGS_ONNX_QUERY_PREFIX: "query: "
      HINDSIGHT_API_EMBEDDINGS_ONNX_PASSAGE_PREFIX: "passage: "
```

Варианты моделей ONNX:

| Модель | Размерность | Префиксы E5 |
|--------|-------------|-------------|
| `sentence-transformers/all-MiniLM-L6-v2` | 384 | Удалить оба префикса |
| `intfloat/multilingual-e5-small` | 384 | Сохранить `query: ` и `passage: ` |
| `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` | 384 | Удалить оба префикса |
| `BAAI/bge-m3` | 1024 | Удалить оба префикса; держать `onnx/model.onnx_data` рядом с `model.onnx` |

:::warning
Не смешивайте эмбеддинги разных моделей в одном векторном индексе. Переход с `local` на `onnx` или смена модели ONNX требует заново создать эмбеддинги для существующих воспоминаний и документов, даже если размерности совпадают. Например, `BAAI/bge-small-en-v1.5` и `intfloat/multilingual-e5-small` обе возвращают векторы размерностью 384, но их векторные пространства семантически несовместимы.
:::

:::warning
Префиксы ONNX по умолчанию (`query: ` и `passage: `) предназначены для E5. Для MiniLM и BGE задайте обе переменные префиксов пустыми, иначе Hindsight добавит формат текста, на котором эти модели не обучались.
:::

#### Частая ошибка: имена переменных эмбеддингов зависят от поставщика

Имя переменной включает поставщика: `HINDSIGHT_API_EMBEDDINGS_{PROVIDER}_{PARAMETER}`. Например, при `HINDSIGHT_API_EMBEDDINGS_PROVIDER=openai` нужно указывать `HINDSIGHT_API_EMBEDDINGS_OPENAI_BASE_URL`, `HINDSIGHT_API_EMBEDDINGS_OPENAI_MODEL` и `HINDSIGHT_API_EMBEDDINGS_OPENAI_API_KEY`. Варианты без `OPENAI` неверны. Это отличается от переменных LLM вида `HINDSIGHT_API_LLM_{PARAMETER}`, где сегмента поставщика нет.

:::warning
Если указать неправильные имена, Hindsight может перейти к настройкам OpenAI по умолчанию (например, `text-embedding-3-small`) и завершиться ошибкой авторизации при обращении не к тому серверу.
:::

#### DeepSeek и эмбеддинги

DeepSeek доступен как поставщик **LLM**, но у него нет API эмбеддингов. При использовании DeepSeek для LLM выберите отдельного поставщика эмбеддингов, например `local`, `openai`, `cohere` или `google`.

#### Размерность эмбеддингов

Hindsight автоматически определяет размерность модели при старте и приводит к ней схему БД. Модель по умолчанию `BAAI/bge-small-en-v1.5` возвращает векторы размерностью 384, модели OpenAI — размерностью 1536 или 3072. Для `litellm-sdk` параметр `HINDSIGHT_API_EMBEDDINGS_LITELLM_SDK_OUTPUT_DIMENSIONS` задаёт размер, если поставщик поддерживает параметр LiteLLM `dimensions`; иначе поведение не меняется.

Модель `zembed-1` от ZeroEntropy поддерживает размеры `2560`, `1280`, `640`, `320`, `160`, `80` и `40`. API ZeroEntropy по умолчанию выбирает 2560, Hindsight — 1280, чтобы модель работала со стандартным индексом pgvector HNSW. Размер 2560 требует расширения с поддержкой высокоразмерных индексов, например DiskANN/pgvectorscale или ScaNN.

:::warning Смена размерности
После сохранения воспоминаний нельзя изменить размерность без потери данных. Для перехода на модель с другой размерностью очистите воспоминания либо выберите модель с совместимым размером. В пустой БД схема автоматически настраивается при запуске.

Размерности OpenAI: `text-embedding-3-small` — 1536; `text-embedding-3-large` — 3072; устаревшая `text-embedding-ada-002` — 1536. `gemini-embedding-001` изначально возвращает 3072 измерения, но размер можно изменить через `HINDSIGHT_API_EMBEDDINGS_GEMINI_OUTPUT_DIMENSIONALITY` (по умолчанию `768`). Для ZeroEntropy `zembed-1` доступны размеры Matryoshka: `2560`, `1280`, `640`, `320`, `160`, `80` и `40`; Hindsight выбирает `1280`.
:::

### Переранжировщик

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_RERANKER_PROVIDER` | Поставщик: `local`, `tei`, `cohere`, `openrouter`, `zeroentropy`, `siliconflow`, `alibaba`, `google`, `flashrank`, `litellm`, `litellm-sdk`, `jina-mlx` или `rrf` | `local` |
| `HINDSIGHT_API_RERANKER_LOCAL_MODEL` | Локальная модель | `cross-encoder/ms-marco-MiniLM-L-6-v2` |
| `HINDSIGHT_API_RERANKER_LOCAL_MAX_CONCURRENT` | Максимум одновременных локальных запросов ранжирования, чтобы избежать перегрузки CPU | `4` |
| `HINDSIGHT_API_RERANKER_LOCAL_TRUST_REMOTE_CODE` | Разрешить модели с собственным кодом; это риск безопасности | `false` |
| `HINDSIGHT_API_RERANKER_LOCAL_FORCE_CPU` | Принудительно использовать CPU; помогает избежать проблем MPS/XPC в macOS | `false` |
| `HINDSIGHT_API_RERANKER_LOCAL_FP16` | Вычисления половинной точности FP16. На MPS на 27–36% быстрее при том же качестве; выключено по умолчанию, поскольку не все CPU поддерживают FP16. | `false` |
| `HINDSIGHT_API_RERANKER_LOCAL_BUCKET_BATCHING` | Группировать пары по длине токенов перед пакетной обработкой, уменьшая пустое дополнение. Ускорение 36–54% при том же качестве. | `false` |
| `HINDSIGHT_API_RERANKER_LOCAL_BATCH_SIZE` | Размер пакета `predict()`; оптимум зависит от устройства и модели, на MPS небольшой пакет может быть быстрее | `32` |
| `HINDSIGHT_API_RERANKER_TEI_URL` / `_TEI_BATCH_SIZE` / `_TEI_MAX_CONCURRENT` / `_TEI_HTTP_TIMEOUT` | Адрес TEI, размер пакета, число одновременных запросов и HTTP-тайм-аут в секундах | `128` / `8` / `30.0` |
| `HINDSIGHT_API_RERANKER_OPENROUTER_API_KEY` / `_MODEL` / `_TIMEOUT` / `_BASE_URL` | Ключ, модель, тайм-аут и адрес OpenRouter (ключ по умолчанию берётся из общих настроек OpenRouter или LLM) | `cohere/rerank-v3.5` / `60.0` / `https://openrouter.ai/api/v1/rerank` |
| `HINDSIGHT_API_RERANKER_COHERE_API_KEY` / `_MODEL` / `_BASE_URL` / `_TIMEOUT` | Ключ и модель Cohere; собственный адрес совместимого `/rerank` API позволяет вызывать его по HTTP вместо SDK Cohere (например, Azure AI Foundry, Jina, Voyage или свой BGE) | `rerank-english-v3.0` / `60.0` |
| `HINDSIGHT_API_RERANKER_LITELLM_API_BASE` / `_API_KEY` / `_MODEL` / `_TIMEOUT` | Адрес прокси LiteLLM, необязательный ключ, модель с префиксом поставщика и тайм-аут | `http://localhost:4000` / `cohere/rerank-english-v3.0` / `60.0` |
| `HINDSIGHT_API_RERANKER_LITELLM_SDK_API_KEY` / `_MODEL` / `_API_BASE` / `_TIMEOUT` | Ключ, модель, адрес API и тайм-аут SDK LiteLLM для прямого доступа без прокси | `cohere/rerank-english-v3.0` / `60.0` |
| `HINDSIGHT_API_RERANKER_LITELLM_MAX_TOKENS_PER_DOC` | Ограничить длину документов в токенах перед ранжированием через `litellm` или `litellm-sdk`; нужно моделям с небольшим контекстом | Не задано (обрезки нет) |
| `HINDSIGHT_API_RERANKER_ZEROENTROPY_API_KEY` / `_MODEL` / `_BASE_URL` / `_TIMEOUT` | Ключ, модель (`zerank-2` или `zerank-2-small`), собственный адрес и тайм-аут ZeroEntropy | `zerank-2` / `https://api.zeroentropy.dev` / `60.0` |
| `HINDSIGHT_API_RERANKER_SILICONFLOW_API_KEY` / `_MODEL` / `_BASE_URL` / `_TIMEOUT` | Ключ, модель, адрес совместимого `/rerank` API и тайм-аут SiliconFlow | `BAAI/bge-reranker-v2-m3` / `https://api.siliconflow.cn/v1` / `60.0` |
| `HINDSIGHT_API_RERANKER_ALIBABA_API_KEY` / `_MODEL` / `_TIMEOUT` | Ключ Alibaba Cloud DashScope, модель и тайм-аут | `qwen3-rerank` / `60.0` |
| `HINDSIGHT_API_RERANKER_GOOGLE_PROJECT_ID` / `_MODEL` / `_SERVICE_ACCOUNT_KEY` / `_TIMEOUT` | Проект Google Cloud, модель Google Discovery Engine, необязательный путь к ключу сервисной учётной записи (иначе используются ADC) и тайм-аут | `semantic-ranker-default-004` / `60.0` |
| `HINDSIGHT_API_RERANKER_FLASHRANK_MODEL` / `_CACHE_DIR` / `_CPU_MEM_ARENA` | Модель FlashRank, папка кэша и включение арены памяти ONNX. При включении RSS может постоянно расти, поскольку арена не уменьшается; выключенное значение ограничивает память ценой чуть более медленного выделения. | `ms-marco-MiniLM-L-12-v2` / системная папка / `false` |
| `HINDSIGHT_API_RERANKER_JINA_MLX_MODEL_PATH` | Путь к загруженной локальной модели `jina-reranker-v3-mlx`; если не задан, модель скачивается с HuggingFace | Автозагрузка |

Примеры выбора поставщика:

```bash
# Локальный поставщик на SentenceTransformers CrossEncoder
export HINDSIGHT_API_RERANKER_PROVIDER=local
export HINDSIGHT_API_RERANKER_LOCAL_MODEL=cross-encoder/ms-marco-MiniLM-L-6-v2

# TEI для высокопроизводительного вывода
export HINDSIGHT_API_RERANKER_PROVIDER=tei
export HINDSIGHT_API_RERANKER_TEI_URL=http://localhost:8081

# OpenRouter
export HINDSIGHT_API_RERANKER_PROVIDER=openrouter
export HINDSIGHT_API_RERANKER_OPENROUTER_API_KEY=your-openrouter-api-key
export HINDSIGHT_API_RERANKER_OPENROUTER_MODEL=cohere/rerank-v3.5

# Cohere или другой совместимый /rerank API
export HINDSIGHT_API_RERANKER_PROVIDER=cohere
export HINDSIGHT_API_RERANKER_COHERE_API_KEY=your-api-key
export HINDSIGHT_API_RERANKER_COHERE_MODEL=rerank-english-v3.0
export HINDSIGHT_API_RERANKER_COHERE_BASE_URL=https://your-cohere-compatible-endpoint.com

# ZeroEntropy
export HINDSIGHT_API_RERANKER_PROVIDER=zeroentropy
export HINDSIGHT_API_RERANKER_ZEROENTROPY_API_KEY=your-api-key
export HINDSIGHT_API_RERANKER_ZEROENTROPY_MODEL=zerank-2

# Google Discovery Engine
export HINDSIGHT_API_RERANKER_PROVIDER=google
export HINDSIGHT_API_RERANKER_GOOGLE_PROJECT_ID=your-gcp-project-id
export HINDSIGHT_API_RERANKER_GOOGLE_SERVICE_ACCOUNT_KEY=/path/to/service-account.json
export HINDSIGHT_API_RERANKER_GOOGLE_MODEL=semantic-ranker-default-004
```

`litellm` требует отдельный работающий сервер-прокси, зато поддерживает централизованную настройку, ограничения запросов и кэширование. `litellm-sdk` обращается к поставщику напрямую, без прокси: его проще настроить, и задержка ниже. Оба варианта поддерживают Cohere, DeepInfra, Together AI, HuggingFace, Voyage AI и Jina AI.

Поставщик `jina-mlx` выполняет переранжирование на Apple Silicon без облака и отдельной видеокарты. Модель `jina-reranker-v3-mlx` размером около 1,2 ГБ автоматически загружается с HuggingFace при первом запуске и кэшируется локально.

:::note Лицензия
`jina-reranker-v3-mlx` распространяется по лицензии CC BY-NC 4.0. Для коммерческого использования обратитесь в Jina AI.
:::

### Аутентификация

По умолчанию Hindsight работает без аутентификации. Для рабочей среды включите встроенную проверку ключа API через расширение арендатора:

```bash
# Включить встроенную аутентификацию по ключу API
export HINDSIGHT_API_TENANT_EXTENSION=hindsight_api.extensions.builtin.tenant:ApiKeyTenantExtension
export HINDSIGHT_API_TENANT_API_KEY=your-secret-api-key
```

Каждый запрос должен содержать ключ в заголовке `Authorization`:

```bash
curl -H "Authorization: Bearer your-secret-api-key" \
  http://localhost:8888/v1/default/banks
```

Без действительного ключа сервер отвечает `401 Unauthorized`.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_TENANT_EXTENSION` | Путь к расширению арендатора. Значение `hindsight_api.extensions.builtin.tenant:ApiKeyTenantExtension` требует ключ API для каждого запроса. | Не задано (проверка отключена) |
| `HINDSIGHT_API_TENANT_API_KEY` | Общий ключ встроенного расширения; клиент отправляет его как `Authorization: Bearer <key>` | Не задано |

При включении Memory Defense см. `docs/developer/memory-defense/`: там описаны схема политик, каталог детекторов и журнал аудита.

:::tip Собственная аутентификация
Для JWT, OAuth или схем с несколькими арендаторами реализуйте собственный `TenantExtension`. Подробнее — в [документации расширений](./extensions.md).
:::

### Сервер

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_HOST` | Адрес, на котором сервер принимает соединения | `0.0.0.0` |
| `HINDSIGHT_API_PORT` | Порт сервера | `8888` |
| `HINDSIGHT_API_BASE_PATH` | Базовый путь API за обратным прокси, например `/hindsight` | `""` (корень) |
| `HINDSIGHT_API_WORKERS` | Число процессов uvicorn | `1` |
| `HINDSIGHT_API_ACCESS_LOG` | Включить журнал запросов uvicorn; значения `true`, `1`, `yes`, `on` включают его | `false` |
| `HINDSIGHT_API_LOG_LEVEL` | Уровень журнала: `debug`, `info`, `warning` или `error` | `info` |
| `HINDSIGHT_API_LOG_FORMAT` | Формат журнала: `text` или `json` (структурированный формат для облачных платформ) | `text` |
| `HINDSIGHT_API_LOG_JSON_FIELDS` | Список разрешённых полей JSON-журнала через запятую: `severity`, `message`, `timestamp`, `logger`, `tenant`, `exception`. Пустое значение включает все поля. | Пусто (все поля) |
| `HINDSIGHT_API_MCP_ENABLED` | Включить MCP-сервер по адресу `/mcp/{bank_id}/` | `true` |
| `HINDSIGHT_API_MODEL_INIT_TIMEOUT` | Предельное время в секундах для инициализации моделей и соединений при запуске. При зависании эмбеддингов, cross-encoder или проверки LLM (например, из-за загрузки модели без сети или недоступного поставщика) сервер завершится с понятной ошибкой вместо бесконечного ожидания. Также применяется при ленивой инициализации переранжировщика на первом запросе. Увеличьте значение, если первая загрузка модели действительно требует больше времени. | `300` |

### Поиск воспоминаний

Настройки поиска ограничивают параллельность и объём кандидатов, поступающих из семантического поиска, BM25, графа и временных связей.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_GRAPH_RETRIEVER` | Алгоритм поиска по графу | `link_expansion` |
| `HINDSIGHT_API_LINK_EXPANSION_PER_ENTITY_LIMIT` | Максимум целевых записей на сущность при графовом поиске `link_expansion`; ограничивает сущности с большим числом связей | `200` |
| `HINDSIGHT_API_LINK_EXPANSION_TIMEOUT` | Тайм-аут запроса расширения графа для сущности, секунды | `10` |
| `HINDSIGHT_API_RECALL_MAX_CONCURRENT` | Максимум параллельных операций recall на процесс | `32` |
| `HINDSIGHT_API_RECALL_CONNECTION_BUDGET` | Максимум соединений БД на одну операцию recall | `4` |
| `HINDSIGHT_API_RECALL_MAX_QUERY_TOKENS` | Максимальная длина запроса recall в токенах; превышение возвращает HTTP 400 | `500` |
| `HINDSIGHT_API_RERANKER_MAX_CANDIDATES` | Максимум кандидатов для переранжирования; RRF отбрасывает остальные | `300` |
| `HINDSIGHT_API_SEMANTIC_MIN_SIMILARITY` | Минимальное косинусное сходство кандидата, возвращаемого семантическим поиском; допустимы значения от `0` до `1` | `0.3` |
| `HINDSIGHT_API_BM25_MIN_SCORE` | Минимальный балл BM25 для участия в объединении результатов. Отсекает нулевые и не совпавшие строки в движках (особенно `vchord`), которые ранжируют все документы, а не фильтруют по словам запроса. `0` оставляет только совпадения; большее значение требует более сильного совпадения. | `0` |
| `HINDSIGHT_API_RECALL_MAX_CANDIDATES_PER_SOURCE` | Ограничение числа кандидатов от каждого источника (семантика, BM25, граф, время) до общего лимита переранжировщика. `0` выключает ограничение. | `0` |
| `HINDSIGHT_API_RECALL_STRATEGY_BOOSTS` | Приоритет источников в виде списка `strategy:level` через запятую, например `graph:high,bm25:low`. Источники: `semantic`, `bm25`, `graph`, `temporal`; уровни: `low`, `medium`, `high`. Приоритет применяется до переранжировщика, чтобы сохранить нужные кандидаты в лимите, и после него, чтобы изменить итоговый порядок. Усиливаются только перечисленные источники; пропущенные сохраняют обычный вес. Уровень по умолчанию — `medium`; пустое значение отключает функцию. | Пусто |
| `HINDSIGHT_API_RECENCY_DECAY_FUNCTION` | Форма поправки на давность при переранжировании: `linear` постепенно снижает свежесть до минимального значения за заданное число дней; `exponential` использует период полураспада, нейтральная точка соответствует `HALFLIFE_DAYS`; `none` полностью отключает влияние возраста. | `linear` |
| `HINDSIGHT_API_RECENCY_DECAY_LINEAR_WINDOW_DAYS` | Число дней, за которое при линейном режиме свежесть снижается от максимума до минимума | `365` |
| `HINDSIGHT_API_RECENCY_DECAY_HALFLIFE_DAYS` | Возраст в днях, при котором экспоненциальная поправка нейтральна. Меньшее значение сильнее отдаёт предпочтение новым воспоминаниям. | `90` |
| `HINDSIGHT_API_MENTAL_MODEL_REFRESH_CONCURRENCY` | Максимум одновременных обновлений ментальных моделей | `8` |
| `HINDSIGHT_API_ENABLE_MENTAL_MODEL_HISTORY` | Записывать историю изменений ментальных моделей (предыдущий текст и время изменения). `false` полностью отключает запись и экономит место в таблице `mental_model_history`. | `true` |
| `HINDSIGHT_API_MENTAL_MODEL_HISTORY_MAX_ENTRIES` | Максимум записей истории на модель. При каждом обновлении добавляется предыдущая версия, а самые старые записи сверх лимита удаляются. `0` или отрицательное значение снимает ограничение и позволяет истории расти без конца. Чтобы отключить историю, используйте `HINDSIGHT_API_ENABLE_MENTAL_MODEL_HISTORY=false`. | `50` |

#### Алгоритм поиска по графу

По умолчанию `link_expansion` быстро расширяет граф от семантически найденных начальных узлов через совместное упоминание сущностей, семантический поиск k ближайших соседей и причинные связи. Целевая задержка — менее 100 мс.

#### Соответствие бюджета recall

Параметр запроса `budget` (`low`, `mid` или `high`; по умолчанию `mid`) задаёт целое значение `thinking_budget`, используемое каждым методом поиска: семантическим, BM25, графовым и временным. Настройки бюджета иерархические и могут быть переопределены для банка через [API настроек](#hierarchical-configuration).

Поддерживаются два способа расчёта:
- `fixed` (по умолчанию, сохраняет прежнее поведение): `thinking_budget = recall_budget_fixed_<level>`, независимо от `max_tokens`.
- `adaptive`: `thinking_budget = round(max_tokens * recall_budget_adaptive_<level>)` с ограничением диапазоном `recall_budget_min`–`recall_budget_max`. Подходит, когда вызывающая сторона меняет `max_tokens` и нужно соразмерно менять ширину поиска.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_RECALL_BUDGET_FUNCTION` | Способ расчёта: `fixed` или `adaptive` | `fixed` |
| `HINDSIGHT_API_RECALL_BUDGET_FIXED_LOW` / `_MID` / `_HIGH` | Число элементов на метод поиска и тип факта для бюджетов `low` / `mid` / `high` при `fixed` | `100` / `300` / `1000` |
| `HINDSIGHT_API_RECALL_BUDGET_ADAPTIVE_LOW` / `_MID` / `_HIGH` | Доля `max_tokens` запроса для бюджетов `low` / `mid` / `high` при `adaptive` | `0.025` / `0.075` / `0.25` |
| `HINDSIGHT_API_RECALL_BUDGET_MIN` / `_MAX` | Нижняя и верхняя границы адаптивного бюджета после ограничения | `20` / `2000` |

### Retain

Параметры ниже управляют обработкой и сохранением воспоминаний.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_RETAIN_MAX_COMPLETION_TOKENS` | Максимум токенов ответа LLM при извлечении фактов | `64000` |
| `HINDSIGHT_API_RETAIN_CHUNK_SIZE` | Максимум символов в фрагменте для извлечения фактов. Больший фрагмент уменьшает число вызовов LLM, но может потерять контекст. | `3000` |
| `HINDSIGHT_API_RETAIN_STRUCTURED_CHUNK_SIZE` | Максимум символов в одной строке JSONL или сообщении диалога, которое нужно сохранять целиком. Если не задан, используется `HINDSIGHT_API_RETAIN_CHUNK_SIZE`; заданное значение должно быть положительным целым. | — |
| `HINDSIGHT_API_RETAIN_EXTRACTION_MODE` | Режим извлечения фактов: `concise`, `verbose`, `verbatim`, `chunks` или `custom` | `concise` |
| `HINDSIGHT_API_RETAIN_MISSION` | На что банк должен обращать внимание при извлечении. Направляет LLM, не заменяя правила извлечения; применяется вместе с любым режимом. | — |
| `HINDSIGHT_API_RETAIN_CUSTOM_INSTRUCTIONS` | Полная замена подсказки для извлечения фактов; действует только при режиме `custom` и целиком заменяет встроенные правила извлечения. | — |
| `HINDSIGHT_API_RETAIN_EXTRACT_CAUSAL_LINKS` | Извлекать причинно-следственные связи между фактами | `true` |
| `HINDSIGHT_API_RETAIN_BATCH_ENABLED` | Использовать пакетный API LLM для извлечения фактов; экономия 50%, доступно только для асинхронных операций | `false` |
| `HINDSIGHT_API_RETAIN_MAX_CONCURRENT` | Максимум параллельных этапов retain с обращениями к БД (чтение HNSW и запись); ограничивает конкуренцию за ввод-вывод при высокой нагрузке | `4` |
| `HINDSIGHT_API_RETAIN_BATCH_TOKENS` | Максимум символов в подзадаче, на которые автоматически делится асинхронный retain | `10000` |
| `HINDSIGHT_API_RETAIN_CHUNK_BATCH_SIZE` | Максимум фрагментов в потоковом пакете для длинных документов. Один фрагмент даёт примерно 17 фактов; значение `100` соответствует примерно 1700 фактам. Уменьшите его, чтобы ограничить память и нагрузку LLM; настройка доступна для отдельного банка. | `100` |
| `HINDSIGHT_API_RETAIN_ENTITY_LOOKUP` | Поиск сущностей при retain: `full` — точное совпадение, `trigram` — нечёткий поиск по триграммам | `trigram` |
| `HINDSIGHT_API_RETAIN_ENTITY_RESOLUTION_BATCH_SIZE` | Максимум уникальных имён сущностей в одном запросе нечёткого поиска кандидатов (`trigram` в PostgreSQL, `oracle_fuzzy` в Oracle). Ограничивает размер запроса для широких пакетов на банках с большим числом сущностей. | `100` |
| `HINDSIGHT_API_RETAIN_DEFAULT_STRATEGY` | Имя стратегии retain по умолчанию; применяется к вызовам без параметра `strategy` | — |

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_RETAIN_BATCH_POLL_INTERVAL_SECONDS` | Интервал опроса пакетного API в секундах | `60` |
| `HINDSIGHT_API_STORE_DOCUMENT_TEXT` | Сохранять исходный текст рядом с извлечёнными воспоминаниями. Значение `false` отключает сохранение; это статическая настройка сервера. | `true` |

**Поставщики с пакетной обработкой.** `HINDSIGHT_API_RETAIN_BATCH_ENABLED=true` работает только с поставщиками LLM, у которых есть пакетный API: `openai`, `groq`, `gemini` и `fireworks`. Пакетный режим требует асинхронного retain (`async=true`); синхронный retain с этой настройкой завершится ошибкой. При выборе неподдерживаемого поставщика сервер сразу сообщит об ошибке при запуске. **Gemini** использует [Gemini Batch API](https://ai.google.dev/gemini-api/docs/batch-api): скидка 50% на входные и выходные токены, срок выполнения до 24 часов (обычно минуты). Дополнительные настройки не нужны, кроме включения пакетного режима и API-ключа Gemini; Vertex AI (`vertexai`) не поддерживает пакетную обработку.

#### Пакетная обработка Fireworks

Пакетный API Fireworks AI не совместим с OpenAI `/v1/batches`: он использует собственные наборы данных и задания на отдельном управляющем сервере `https://api.fireworks.ai`, отличном от сервера инференса `https://api.fireworks.ai/inference/v1`. Hindsight скрывает это различие. Кроме включения batch-режима, обязательно задайте идентификатор аккаунта Fireworks. Это не меняет обычный путь через LiteLLM `fireworks_ai/...`.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_FIREWORKS_ACCOUNT_ID` | Идентификатор аккаунта Fireworks; обязателен для пакетного retain, поскольку адрес управляющего API содержит `/v1/accounts/{account_id}/...`. Настройка уровня сервера. | — |
| `HINDSIGHT_API_FIREWORKS_BATCH_BASE_URL` | Адрес управляющего API пакетной обработки Fireworks | `https://api.fireworks.ai` |
| `HINDSIGHT_API_FIREWORKS_BATCH_MAX_WAIT_SECONDS` | Максимальное ожидание пакетного задания до сообщения об ошибке. Защищает от ситуации, когда модель без поддержки batch оставляет задание в состоянии `PENDING` навсегда. | `86400` (24 часа) |

```bash
# Пакетный retain в Fireworks: экономия 50%, только асинхронно
export HINDSIGHT_API_RETAIN_LLM_PROVIDER=fireworks
export HINDSIGHT_API_RETAIN_LLM_API_KEY=fw_xxxxxxxxxxxx
export HINDSIGHT_API_RETAIN_LLM_MODEL=accounts/fireworks/models/llama-v3p1-8b-instruct
export HINDSIGHT_API_FIREWORKS_ACCOUNT_ID=your-account-id
export HINDSIGHT_API_RETAIN_BATCH_ENABLED=true
```

Метки сущностей (`entity_labels`) и свободное извлечение сущностей (`entities_allow_free_form`) задаются отдельно для каждого банка через [API настроек банка](api/memory-banks.md#retain-configuration), а не глобальными переменными окружения. Так у каждого банка может быть собственный словарь. Подробнее см. [метки сущностей](retain.md#entity-labels).

#### Не сохранять исходный текст документов

По умолчанию Hindsight хранит исходный текст каждого объекта, чтобы его можно было просмотреть, повторно обработать или экспортировать. Если нужны только извлечённые факты, сущности и ментальные модели, отключите сохранение:

```bash
export HINDSIGHT_API_STORE_DOCUMENT_TEXT=false
```

Весь конвейер retain продолжит работать: разбиение на фрагменты, извлечение фактов, создание эмбеддингов и связывание сущностей не меняются. Качество памяти и `recall` не ухудшается, поскольку поиск читает извлечённые воспоминания, а не исходный текст. Меняется только хранение: `documents.original_text` получает значение `NULL`, текст фрагментов заменяется пустой строкой, но сохраняется контрольная сумма, поэтому повторный импорт того же документа по-прежнему распознаётся.

При отключённом хранении текста запрещён `update_mode="append"`: добавление требует прочитать ранее сохранённый текст, а без него прежнее содержимое было бы потеряно. Используйте `update_mode="replace"` (по умолчанию).

Функции, зависящие от исходного текста, будут ограничены:
- экспорт документов не будет содержать текст, поэтому импорт не сможет повторить извлечение из источника;
- просмотр исходника документа, фрагментов и содержимого фрагмента (включая MCP) вернёт пустое содержимое;
- `recall` с `include_chunks=true` вернёт пустой `chunk_text`; сами факты сохранятся, но без исходного контекста;
- инструмент `expand` в Reflect и прикрепление исходных фрагментов к шагу recall будут недоступны;
- повторная обработка документа из сохранённого текста ничего не сделает.

Эта настройка действует на уровне сервера и не переопределяется для отдельного банка.

#### Как настраивать извлечение retain

| Цель | Настройка |
|------|-----------|
| Направить внимание на нужные темы и исключить второстепенные | `HINDSIGHT_API_RETAIN_MISSION` |
| Извлекать больше подробностей для каждого факта | `HINDSIGHT_API_RETAIN_EXTRACTION_MODE=verbose` |
| Хранить исходные фрагменты, а LLM использовать для метаданных | `HINDSIGHT_API_RETAIN_EXTRACTION_MODE=verbatim` |
| Хранить исходные фрагменты без затрат на LLM | `HINDSIGHT_API_RETAIN_EXTRACTION_MODE=chunks` |
| Полностью заменить правила извлечения | `HINDSIGHT_API_RETAIN_EXTRACTION_MODE=custom` и `HINDSIGHT_API_RETAIN_CUSTOM_INSTRUCTIONS` |

**`HINDSIGHT_API_RETAIN_MISSION`** — рекомендуемый первый шаг. Обычным языком укажите, на что банку обращать внимание. Миссия добавляется к встроенной подсказке и сужает фокус, не заменяя правила. Работает с режимами `concise`, `verbose`, `verbatim` и `custom`, но игнорируется в режиме `chunks`.

```bash
export HINDSIGHT_API_RETAIN_MISSION="Focus on technical decisions, architecture choices, and team member expertise. Deprioritize social or personal information."
```

Режим `verbose` создаёт более развёрнутые факты с контекстом и связями, но работает медленнее и использует больше токенов, чем `concise`.

В режиме `verbatim` каждый фрагмент сохраняется как отдельное воспоминание без сокращения или переписывания. LLM по-прежнему извлекает сущности, время и местоположение, поэтому фрагмент индексируется и находится поиском. Подходит для RAG, импорта документов и тестов, где нужно хранить исходный текст.

```bash
export HINDSIGHT_API_RETAIN_EXTRACTION_MODE=verbatim
```

Стратегии `retain_strategies` и `retain_default_strategy` позволяют обрабатывать в одном банке разные типы содержимого с разными настройками. Это набор иерархических переопределений, доступный через API настроек банка; например, для диалогов можно выбрать `concise` с размером фрагмента 3000, а для документов — `chunks` с размером 800. Вызов `retain` принимает имя стратегии. Если оно не задано, используется `retain_default_strategy`; если и он не задан, действуют общие настройки банка.

Режим `chunks` сохраняет каждый фрагмент как есть без вызова LLM. Извлечение сущностей и временная индексация не выполняются; создаются только эмбеддинги для семантического поиска. Единственные сведения о сущностях — явно переданные через `RetainContent.entities`. Выбирайте этот режим, когда скорость и стоимость важнее структурированных метаданных.

```bash
export HINDSIGHT_API_RETAIN_EXTRACTION_MODE=chunks
```

Режим `custom` вместе с `HINDSIGHT_API_RETAIN_CUSTOM_INSTRUCTIONS` заменяет встроенные правила выбора фактов. Формат ответа, обработка времени и разрешение кореференции остаются прежними; заменяются только рекомендации по извлечению. Используйте его, когда `retain_mission` недостаточно и нужны строгие правила включения и исключения.

### Обработка файлов

Настройки конвейера загрузки и преобразования файлов для `POST /v1/default/banks/{bank_id}/files/retain`:

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_ENABLE_FILE_UPLOAD_API` | Включить загрузку файлов через API | `true` |
| `HINDSIGHT_API_ENABLE_DOCUMENT_EXPORT_API` | Включить [экспорт документов](./api/memory-banks.mdx#document-export--import), `GET /document-transfer` | `true` |
| `HINDSIGHT_API_ENABLE_DOCUMENT_IMPORT_API` | Включить [импорт документов](./api/memory-banks.mdx#document-export--import), `POST /document-transfer` | `true` |
| `HINDSIGHT_API_FILE_PARSER` | Парсер по умолчанию или цепочка резервных парсеров через запятую, например `iris,markitdown` | `markitdown` |
| `HINDSIGHT_API_FILE_PARSER_ALLOWLIST` | Список парсеров через запятую, которые клиенту разрешено запрашивать. Если не задан, разрешены все зарегистрированные парсеры. | — |
| `HINDSIGHT_API_FILE_CONVERSION_MAX_BATCH_SIZE` | Максимум файлов в одном запросе | `10` |
| `HINDSIGHT_API_FILE_CONVERSION_MAX_BATCH_SIZE_MB` | Максимальный суммарный размер загрузки в МБ | `100` |
| `HINDSIGHT_API_FILE_DELETE_AFTER_RETAIN` | Удалять загруженные файлы после извлечения воспоминаний | `true` |

#### Выбор парсера

В теле `POST /v1/default/banks/{bank_id}/files/retain` клиент может переопределить серверный парсер. Можно указать один парсер или упорядоченную цепочку резервных вариантов: следующий запускается, если предыдущий завершился ошибкой.

```bash
# Сначала iris, затем markitdown при ошибке iris
export HINDSIGHT_API_FILE_PARSER=iris,markitdown

# Необязательно: разрешить клиентам выбирать только эти парсеры
export HINDSIGHT_API_FILE_PARSER_ALLOWLIST=markitdown,iris
```

```json
// Переопределение в JSON-запросе загрузки файла
{
  "parser": "iris",
  "files_metadata": [
    { "document_id": "report" },
    { "document_id": "fallback_doc", "parser": ["iris", "markitdown"] }
  ]
}
```

Если клиент запросит парсер, которого нет в разрешённом списке, API вернёт HTTP 400.

#### Парсер markitdown (по умолчанию)

Локально преобразует файлы в Markdown с помощью [Microsoft markitdown](https://github.com/microsoft/markitdown); внешний сервис обычно не нужен. Поддерживаются PDF, DOCX, DOC, PPTX, PPT, XLSX, XLS, изображения JPG/PNG (для распознавания текста нужен дополнительный OCR), аудио MP3/WAV (с транскрибацией), HTML, TXT, MD и CSV.

Для изображений можно подключить совместимый с OpenAI API сервер OCR и компьютерного зрения. По умолчанию функция выключена. Если её включить, задайте отдельные ключ, адрес и модель для MarkItDown: общие `HINDSIGHT_API_LLM_*` не используются, потому что MarkItDown напрямую вызывает OpenAI SDK. Сервер должен поддерживать Chat Completions, а модель — принимать изображения. OCR работает с JPG/PNG и изображениями внутри конвертеров, использующих `llm_client`, но не преобразует отсканированные страницы PDF в изображения. Для сканированных PDF и сложных макетов используйте `iris`, `llama_parse` или цепочку `llama_parse,markitdown`.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_ENABLED` | Включить OCR изображений через совместимый с OpenAI сервер | `false` |
| `HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_API_KEY` | Ключ API OCR; обязателен при включённой функции | — |
| `HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_BASE_URL` | Адрес Chat Completions, совместимый с OpenAI; обязателен при включённой функции | — |
| `HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_MODEL` | Модель OCR/зрения с поддержкой изображений; обязательна при включённой функции | — |
| `HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_PROMPT` | Подсказка OCR для конвертера изображений MarkItDown | Встроенная подсказка OCR |

```bash
# Подключить отдельный совместимый с OpenAI сервер OCR/зрения
export HINDSIGHT_API_FILE_PARSER=markitdown
export HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_ENABLED=true
export HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_API_KEY=your-vision-api-key
export HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_BASE_URL=https://vision.example/v1
export HINDSIGHT_API_FILE_PARSER_MARKITDOWN_OCR_MODEL=ocr-or-vision-model
```

#### Парсер iris

Облачный сервис [Vectorize Iris](https://docs.vectorize.io/build-deploy/extract-information/understanding-iris/) извлекает содержимое сложных документов с помощью удалённого ИИ.

| Переменная | Назначение |
|------------|------------|
| `HINDSIGHT_API_FILE_PARSER_IRIS_TOKEN` | Токен API Vectorize |
| `HINDSIGHT_API_FILE_PARSER_IRIS_ORG_ID` | Идентификатор организации Vectorize |

Поддерживаются PDF, DOCX, DOC, PPTX, PPT, XLSX, XLS, изображения JPG/JPEG/PNG/GIF/BMP/TIFF/WEBP, HTML, TXT, MD и CSV.

Чтобы выбрать Iris единственным парсером, задайте `HINDSIGHT_API_FILE_PARSER=iris`, `HINDSIGHT_API_FILE_PARSER_IRIS_TOKEN` и `HINDSIGHT_API_FILE_PARSER_IRIS_ORG_ID`. Для резервного перехода на MarkItDown укажите `HINDSIGHT_API_FILE_PARSER=iris,markitdown`.

#### Парсер llama_parse

Облачный сервис [LlamaParse](https://docs.cloud.llamaindex.ai/llamaparse) от LlamaIndex хорошо обрабатывает сложную вёрстку: таблицы, диаграммы и многостолбцовые PDF. Требует ключ LlamaCloud в `HINDSIGHT_API_FILE_PARSER_LLAMA_PARSE_API_KEY` (обычно начинается с `llx-`). Поддерживаются PDF, DOCX, PPTX, XLSX, HTML, EPUB, RTF, TXT и другие форматы; полный перечень см. в [документации LlamaParse](https://docs.cloud.llamaindex.ai/llamaparse/features/supported_document_types).

Для выбора одного парсера задайте `HINDSIGHT_API_FILE_PARSER=llama_parse`; резервный вариант: `HINDSIGHT_API_FILE_PARSER=llama_parse,markitdown`.

Для больших загрузок можно увеличить лимиты, например до 20 файлов и 500 МБ, через `HINDSIGHT_API_FILE_CONVERSION_MAX_BATCH_SIZE=20` и `HINDSIGHT_API_FILE_CONVERSION_MAX_BATCH_SIZE_MB=500`. Чтобы сохранять файлы после обработки для отладки или повторного разбора, задайте `HINDSIGHT_API_FILE_DELETE_AFTER_RETAIN=false`.

### Хранение файлов

Файлы, загруженные через API retain, до преобразования сохраняются в объектном хранилище. Выберите сервер хранения, подходящий вашей инфраструктуре.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_FILE_STORAGE_TYPE` | Тип хранилища: `native`, `s3`, `gcs` или `azure` | `native` |

#### Встроенное хранилище PostgreSQL

Файлы сохраняются в виде `BYTEA` в таблице `file_storage`. Дополнительная инфраструктура не нужна; подходит для разработки и небольших установок. Это вариант по умолчанию: `HINDSIGHT_API_FILE_STORAGE_TYPE=native`.

#### S3 и совместимые сервисы

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_FILE_STORAGE_S3_BUCKET` | Имя бакета S3 | — |
| `HINDSIGHT_API_FILE_STORAGE_S3_REGION` | Регион AWS | — |
| `HINDSIGHT_API_FILE_STORAGE_S3_ENDPOINT` | Собственный адрес S3-совместимого хранилища, например MinIO, Cloudflare R2 или Tigris | Адрес AWS |
| `HINDSIGHT_API_FILE_STORAGE_S3_ACCESS_KEY_ID` / `_SECRET_ACCESS_KEY` | Идентификатор и секретный ключ доступа AWS | — |

Для S3-совместимых хранилищ без регионов AWS (MinIO, Cloudflare R2, Tigris) задайте регион `auto`. Он нужен для подписи запросов SigV4, но самим сервисом не используется.

```bash
# AWS S3
export HINDSIGHT_API_FILE_STORAGE_TYPE=s3
export HINDSIGHT_API_FILE_STORAGE_S3_BUCKET=my-hindsight-files
export HINDSIGHT_API_FILE_STORAGE_S3_REGION=us-east-1
export HINDSIGHT_API_FILE_STORAGE_S3_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
export HINDSIGHT_API_FILE_STORAGE_S3_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# S3-совместимый сервер, например MinIO или Cloudflare R2
export HINDSIGHT_API_FILE_STORAGE_TYPE=s3
export HINDSIGHT_API_FILE_STORAGE_S3_BUCKET=my-bucket
export HINDSIGHT_API_FILE_STORAGE_S3_REGION=auto
export HINDSIGHT_API_FILE_STORAGE_S3_ENDPOINT=https://your-minio.example.com
export HINDSIGHT_API_FILE_STORAGE_S3_ACCESS_KEY_ID=minioadmin
export HINDSIGHT_API_FILE_STORAGE_S3_SECRET_ACCESS_KEY=minioadmin
```

#### Google Cloud Storage

Задайте `HINDSIGHT_API_FILE_STORAGE_GCS_BUCKET` и `HINDSIGHT_API_FILE_STORAGE_TYPE=gcs`. Необязательный `HINDSIGHT_API_FILE_STORAGE_GCS_SERVICE_ACCOUNT_KEY` указывает путь к JSON-ключу; если он не задан, используется ADC.

#### Azure Blob Storage

Для `HINDSIGHT_API_FILE_STORAGE_TYPE=azure` укажите `HINDSIGHT_API_FILE_STORAGE_AZURE_CONTAINER`, `HINDSIGHT_API_FILE_STORAGE_AZURE_ACCOUNT_NAME` и `HINDSIGHT_API_FILE_STORAGE_AZURE_ACCOUNT_KEY`.

| Хранилище | Подходит для | Особенности |
|-----------|--------------|-------------|
| `native` | Разработки и небольших установок | Не требует отдельной инфраструктуры; файлы находятся в PostgreSQL |
| `s3` | Рабочих сред и AWS | Совместимо с любым S3-подобным хранилищем |
| `gcs` | Рабочих сред Google Cloud | Поддерживает вход без ключа через ADC |
| `azure` | Рабочих сред Azure | Использует ключ учётной записи |

:::tip Для рабочей среды
Используйте `s3`, `gcs` или `azure`, чтобы не хранить большие двоичные файлы в PostgreSQL. Оставьте `HINDSIGHT_API_FILE_DELETE_AFTER_RETAIN=true` (по умолчанию), чтобы удалять файлы после извлечения воспоминаний и снизить расходы на хранение.
:::

### Наблюдения (экспериментальная функция) {#observations}

Наблюдения — дедуплицированные знания, собранные по нескольким фактам и подтверждённые исходными воспоминаниями. Для каждого наблюдения учитываются подтверждающие воспоминания и число доказательств; при появлении новых данных текст уточняется, а не просто заменяется.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_ENABLE_OBSERVATIONS` | Включить консолидацию наблюдений | `true` |
| `HINDSIGHT_API_ENABLE_AUTO_CONSOLIDATION` | Запускать консолидацию после retain, удаления и обновления. Если выключить, консолидацию можно запускать только через [соответствующий endpoint](api/operations.md#consolidation). Можно задать отдельно для банка. | `true` |
| `HINDSIGHT_API_CONSOLIDATION_RECONCILE_INTERVAL_SECONDS` | Интервал фоновой проверки банков, у которых остались факты без консолидации и нет активной задачи. Позволяет повторно запланировать обработку после окончательного сбоя, например при недоступном поставщике LLM. `0` отключает проверку. | `300` |
| `HINDSIGHT_API_MENTAL_MODEL_REFRESH_TICK_SECONDS` | Как часто фоновый цикл проверяет расписание обновления ментальных моделей. Это интервал проверки; реальный график задаёт `trigger.refresh_cron`. Модель обновляется, только если после прошлого обновления появились новые воспоминания в её области. `0` отключает проверку. | `60` |
| `HINDSIGHT_API_ENABLE_OBSERVATION_HISTORY` | Записывать историю текста, тегов и дат каждого наблюдения в `observation_history`. `false` полностью отключает запись. | `true` |
| `HINDSIGHT_API_OBSERVATION_HISTORY_MAX_ENTRIES` | Максимум строк истории на наблюдение; при обновлении старые строки сверх лимита удаляются. `0` или отрицательное число снимает ограничение. Для полного отключения используйте `HINDSIGHT_API_ENABLE_OBSERVATION_HISTORY=false`. | `50` |
| `HINDSIGHT_API_CONSOLIDATION_MAX_ATTEMPTS` | Внешние повторы пакетного вызова LLM для консолидации. Каждый повтор использует внутренний лимит `HINDSIGHT_API_CONSOLIDATION_LLM_MAX_RETRIES`; максимум вызовов на пакет равен `MAX_ATTEMPTS × (LLM_MAX_RETRIES + 1)`. | `3` |
| `HINDSIGHT_API_CONSOLIDATION_BATCH_SIZE` | Число воспоминаний в пакете для внутренней оптимизации | `50` |
| `HINDSIGHT_API_CONSOLIDATION_MAX_MEMORIES_PER_ROUND` | Максимум воспоминаний за один раунд. После достижения лимита задача освобождает слот и ставится в очередь заново, давая обслужить другие банки. Ментальные модели обновляются только в последнем раунде. `0` снимает ограничение; можно задать для банка. | `100` |
| `HINDSIGHT_API_CONSOLIDATION_MAX_TOKENS` | Максимум токенов recall при поиске связанных наблюдений во время консолидации | `1024` |
| `HINDSIGHT_API_CONSOLIDATION_MAX_COMPLETION_TOKENS` | Максимум выходных токенов для одного вызова LLM. Если не задан, используется лимит поставщика. Укажите его, если у поставщика есть низкий скрытый лимит, из-за которого обрывается ответ. | Не задано |
| `HINDSIGHT_API_CONSOLIDATION_LLM_BATCH_SIZE` | Число фактов в одном вызове LLM; большее значение сокращает число запросов, но увеличивает подсказку. `1` выключает пакетирование; можно менять для банка. | `8` |
| `HINDSIGHT_API_CONSOLIDATION_DEDUP_THRESHOLD` | Порог косинусного сходства для сверки почти одинаковых наблюдений отдельным вызовом LLM «объединить или оставить». Значение `1.0` отключает функцию. Поддерживается только PostgreSQL; в Oracle сверка не выполняется. | `0.97` |
| `HINDSIGHT_API_CONSOLIDATION_LLM_PARALLELISM` | Максимум параллельных групп тегов в одной задаче. Группы с пересекающимися областями записи блокируют друг друга, поэтому реальная параллельность может быть ниже. `1` означает последовательную обработку. Увеличение значения пропорционально повышает пиковую частоту запросов LLM и нагрузку на пул БД; можно менять для банка. | `4` |
| `HINDSIGHT_API_CONSOLIDATION_RECALL_BUDGET` | Бюджет поиска внутри консолидации: `low`, `mid` или `high`. Низкое значение загружает меньше кандидатов и экономит память на крупных банках. | `low` |
| `HINDSIGHT_API_CONSOLIDATION_SOURCE_FACTS_MAX_TOKENS` | Общий лимит токенов исходных фактов в подсказке консолидации; `-1` означает отсутствие лимита. Можно задавать для банка. | `4096` |
| `HINDSIGHT_API_CONSOLIDATION_SOURCE_FACTS_MAX_TOKENS_PER_OBSERVATION` | Лимит исходных фактов для каждого отдельного наблюдения; `-1` снимает ограничение. Можно задавать для банка. | `256` |
| `HINDSIGHT_API_OBSERVATIONS_MISSION` | Что именно банк должен сводить в долговременные наблюдения. Заменяет встроенные правила; если не задано, используется серверный вариант. | — |
| `HINDSIGHT_API_MAX_OBSERVATIONS_PER_SCOPE` | Максимум наблюдений для области тегов. После достижения лимита консолидация только обновляет или удаляет существующие наблюдения. Записи без тегов не ограничиваются; `-1` снимает лимит. Можно задать для банка. | `-1` |
| `HINDSIGHT_API_OBSERVATION_SCOPE_LIMITS` | JSON-массив переопределений лимита по области: правила `{"scope": [tag-globs], "limit": int}`. Шаблоны используют [fnmatch](https://docs.python.org/3/library/fnmatch.html); теги и шаблоны должны покрывать друг друга полностью. Берётся первое подходящее правило, иначе действует `MAX_OBSERVATIONS_PER_SCOPE`. Можно задать для банка. | — |

По умолчанию наблюдения содержат конкретные долговременные факты, а не временное состояние. Миссия `HINDSIGHT_API_OBSERVATIONS_MISSION` полностью заменяет это определение: обычным языком укажите, что считать наблюдением в вашей задаче. Например, задайте сводки событий по неделям, знания о конкретных людях или повторяющиеся закономерности обращений в поддержку. Если стандартное поведение подходит, оставьте переменную незаданной.

### Reflect

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_REFLECT_MAX_ITERATIONS` | Максимум итераций вызова инструментов до принудительной генерации ответа | `10` |
| `HINDSIGHT_API_REFLECT_MAX_CONTEXT_TOKENS` | Максимум накопленного контекста до итогового синтеза; предотвращает `context_length_exceeded` в больших банках. Уменьшите для моделей с контекстом меньше 128 тыс. токенов. | `100000` |
| `HINDSIGHT_API_REFLECT_WALL_TIMEOUT` | Общий тайм-аут операции reflect в секундах; при превышении возвращается HTTP 504 | `300` |
| `HINDSIGHT_API_REFLECT_MISSION` | Общая миссия reflect: идентичность и рамки рассуждения; может переопределяться для банка через API настроек | — |
| `HINDSIGHT_API_REFLECT_SOURCE_FACTS_MAX_TOKENS` | Бюджет исходных фактов в `search_observations`: `-1` отключает их (по умолчанию), `0` включает без лимита, положительное число задаёт лимит. Можно переопределять для банка. | `-1` |

#### Внутренний recall для обновления ментальных моделей

Эти иерархические настройки управляют инструментом recall внутри `reflect_async`, например при обновлении ментальной модели. Их можно переопределить для банка, а поля модели `trigger.include_chunks`, `trigger.recall_max_tokens` и `trigger.recall_chunks_max_tokens` позволяют настроить их отдельно для каждой модели.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_RECALL_INCLUDE_CHUNKS` | Возвращать ли рядом с фактами исходный текст фрагментов; `false` экономит бюджет подсказки | `true` |
| `HINDSIGHT_API_RECALL_MAX_TOKENS` | Бюджет токенов для фактов внутреннего recall | `2048` |
| `HINDSIGHT_API_RECALL_CHUNKS_MAX_TOKENS` | Бюджет токенов для исходного текста фрагментов | `1000` |

#### Особенности рассуждения

Для каждого признака задаётся число от 1 до 5. Эти иерархические значения можно переопределить для банка через [API настроек](./configuration.md#hierarchical-configuration).

| Переменная | Что регулирует | Значение по умолчанию |
|------------|----------------|-----------------------|
| `HINDSIGHT_API_DISPOSITION_SKEPTICISM` | Склонность сомневаться или доверять: 1 — доверчивость, 5 — скептицизм | `3` |
| `HINDSIGHT_API_DISPOSITION_LITERALISM` | Буквальность трактовки: 1 — гибкая, 5 — буквальная | `3` |
| `HINDSIGHT_API_DISPOSITION_EMPATHY` | Учёт эмоционального контекста: 1 — отстранённость, 5 — эмпатия | `3` |

### Сервер MCP

Настройки конечных точек MCP:

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_MCP_ENABLED` | Включить MCP-сервер по адресу `/mcp/{bank_id}/` | `true` |
| `HINDSIGHT_API_MCP_ENABLED_TOOLS` | Разрешённый глобально список инструментов MCP через запятую; пустое значение означает все инструменты | — |
| `HINDSIGHT_API_MCP_STATELESS` | Использовать HTTP без состояния (только POST). `false` включает состояние, GET/SSE и сообщения от сервера. | `false` |
| `HINDSIGHT_API_MCP_AUTH_TOKEN` | Необязательный Bearer-токен для аутентификации MCP | — |
| `HINDSIGHT_API_MCP_LOCAL_BANK_ID` | Идентификатор банка для локального MCP | `mcp` |
| `HINDSIGHT_API_MCP_INSTRUCTIONS` | Дополнительные инструкции к описаниям инструментов retain и recall | — |

`HINDSIGHT_API_MCP_ENABLED_TOOLS` ограничивает список инструментов, регистрируемых на сервере. Это удобно для доступа только на чтение или сокращения набора доступных действий. Например, `recall` оставляет только поиск, а `recall,reflect` — поиск и формирование ответа. Доступные имена включают `retain`, `recall`, `reflect`, `list_banks`, `create_bank`, операции с ментальными моделями и директивами, чтение и удаление воспоминаний и документов, просмотр операций и тегов, обновление и удаление банка, а также `clear_memories`.

Для отдельного банка список можно переопределить через API настроек:

```bash
# Разрешить только поиск recall для конкретного банка
curl -X PATCH http://localhost:8888/v1/default/banks/my-bank/config \
  -H "Content-Type: application/json" \
  -d '{"updates": {"mcp_enabled_tools": ["recall"]}}'
```

Если список задан на уровне банка, вызов запрещённого инструмента возвращает понятную ошибку, хотя инструмент остаётся в списке MCP для совместимости протокола. По умолчанию MCP открыт. В рабочей среде задайте `HINDSIGHT_API_MCP_AUTH_TOKEN`; клиент должен передавать его в `Authorization`. Подробности — в [документации MCP](./mcp-server.md#authentication).

Дополнительные инструкции для локального MCP можно передать через `HINDSIGHT_API_MCP_INSTRUCTIONS`, например: `Also store every action you take, including tool calls and decisions made.`

### Распределённые рабочие процессы

По умолчанию API сам обрабатывает фоновые задачи. Для высокой пропускной способности запустите отдельные рабочие процессы; см. [описание сервиса Worker](./services#worker-service).

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_WORKER_ENABLED` | Запускать рабочий процесс внутри API | `true` |
| `HINDSIGHT_API_WORKER_ID` | Уникальный идентификатор рабочего процесса | Имя узла |
| `HINDSIGHT_API_WORKER_POLL_INTERVAL_MS` | Интервал опроса БД, мс | `500` |
| `HINDSIGHT_API_WORKER_MAX_RETRIES` | Число повторов задачи до признания её неудачной | `3` |
| `HINDSIGHT_API_WORKER_TASK_RETRY_BACKOFF_SECONDS` | Пауза между повторами временно неудачной задачи, секунды | `60` |
| `HINDSIGHT_API_WORKER_HTTP_PORT` | HTTP-порт метрик и проверки состояния; только CLI рабочего процесса | `8889` |
| `HINDSIGHT_API_WORKER_MAX_SLOTS` | Максимум параллельных задач рабочего процесса всех типов | `10` |
| `HINDSIGHT_API_WORKER_CONSOLIDATION_MAX_SLOTS` | Резерв слотов для консолидации внутри общего лимита; сохраняется последовательная обработка задач одного банка | `2` |
| `HINDSIGHT_API_WORKER_CONSOLIDATION_BANK_PRIORITY` | Приоритет консолидации по банкам; формат описан ниже | Не задано |
| `HINDSIGHT_API_WORKER_RETAIN_MAX_SLOTS` | Резерв слотов для retain | `0` |
| `HINDSIGHT_API_WORKER_FILE_CONVERT_RETAIN_MAX_SLOTS` | Резерв слотов для преобразования файлов и retain | `0` |
| `HINDSIGHT_API_WORKER_REFRESH_MENTAL_MODEL_MAX_SLOTS` | Резерв слотов для обновления ментальных моделей | `0` |
| `HINDSIGHT_API_WORKER_GRAPH_MAINTENANCE_MAX_SLOTS` | Резерв слотов для обслуживания графа | `0` |
| `HINDSIGHT_API_WORKER_IMPORT_DOCUMENTS_MAX_SLOTS` | Резерв слотов для импорта документов | `0` |

Резерв для каждого типа задачи — это часть `WORKER_MAX_SLOTS`, а не отдельный пул; сумма резервов не должна его превышать (иначе запуск завершится `ValueError`). Остаток составляет общий пул для всех типов задач. Тип, у которого резерв заполнен, тоже может занять слот общего пула. Например, при общем лимите 10, консолидации 2, retain 3 и обновлении ментальных моделей 2 в общем пуле остаётся 3 слота. При значениях по умолчанию два слота зарезервированы под консолидацию, восемь остаются общими. Значение `CONSOLIDATION_MAX_SLOTS=0` переводит резерв консолидации в общий пул.

`HINDSIGHT_API_WORKER_CONSOLIDATION_BANK_PRIORITY` задаёт порядок банков при освобождении слота в виде списка `шаблон:приоритет`, разделённого запятыми. Больший номер означает более высокий приоритет; `*` соответствует любому банку, а неуказанные банки по умолчанию получают приоритет 1. Например, `shadow-*:10,staging-*:5,*:1` обслужит `shadow-*` раньше остальных. Это полезно при разных размерах банков. Одновременно по-прежнему выполняется не более одной задачи консолидации на банк. Если значение не задано, используется порядок `created_at`.

### Оптимизация производительности

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_SKIP_LLM_VERIFICATION` | Пропустить проверку соединения LLM при запуске | `false` |
| `HINDSIGHT_API_LAZY_RERANKER` | Загружать модель переранжировщика при первом обращении, чтобы ускорить запуск | `false` |
| `HINDSIGHT_API_BANK_STATS_CACHE_TTL_SECONDS` | Время жизни кэша `get_bank_stats`; `0` отключает кэш и запускает запрос БД при каждом вызове | `60` |
| `HINDSIGHT_API_BANK_STATS_CACHE_MAX_ENTRIES` | Максимум записей `(schema, bank)` в кэше до вытеснения LRU; ограничивает память при множестве банков | `1024` |

`get_bank_stats` агрегирует таблицу `memory_links` с `memory_units`; на банке с миллионами строк запрос может выполняться несколько секунд. Результат приблизительный (его используют виджет интерфейса и подсказка актуальности в reflect), поэтому он кэшируется на несколько десятков секунд; одновременные промахи объединяются одним запросом.

Локальные эмбеддинги и ранжирование запускают нативные пулы потоков BLAS/ML-библиотек (OpenBLAS, OpenMP, MKL). Hindsight уже распараллеливает запросы, поэтому дополнительные пулы могут перегрузить CPU и память. По умолчанию каждый такой пул ограничен меньшим из 16 потоков и числа доступных CPU. В контейнерах учитывается лимит CPU контейнера, а не все ядра узла. Число потоков можно задавать заранее, до запуска процесса:

| Переменная | Библиотека | Значение по умолчанию |
|------------|------------|-----------------------|
| `OMP_NUM_THREADS` | OpenMP (torch, ONNX Runtime и некоторые сборки BLAS) | `min(16, доступные CPU)` |
| `OPENBLAS_NUM_THREADS` | OpenBLAS (стандартная BLAS для numpy) | `min(16, доступные CPU)` |
| `MKL_NUM_THREADS` | Intel MKL (numpy/torch с MKL) | `min(16, доступные CPU)` |
| `NUMEXPR_NUM_THREADS` | Механизм выражений numexpr | `min(16, доступные CPU)` |

Для сервера с большим числом одновременных запросов уменьшите лимиты вплоть до `1`, чтобы отдать CPU параллельной обработке запросов. Для небольшого числа запросов и крупных локальных пакетов стандартный лимит оставляет возможность параллельной обработки внутри вызова. Библиотеки читают эти переменные при загрузке, поэтому изменить их для арендатора или банка нельзя.

### Вебхуки

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_WEBHOOK_URL` | Общий адрес доставки событий; пустое значение отключает вебхук | Не задано |
| `HINDSIGHT_API_WEBHOOK_SECRET` | Секрет для подписи содержимого вебхука через HMAC | Не задан (без подписи) |
| `HINDSIGHT_API_WEBHOOK_EVENT_TYPES` | Список типов событий для отправки через запятую | `consolidation.completed` |
| `HINDSIGHT_API_WEBHOOK_DELIVERY_POLL_INTERVAL_SECONDS` | Как часто фоновая задача проверяет ожидающие доставки, в секундах | `30` |

### Журнал аудита

Журнал аудита записывает изменяющие операции (retain, recall, reflect, изменение настроек банка, действия redact/block из [Memory Defense](memory-defense/index.md) и др.) в таблицу `audit_log`, доступную через `/audit-logs`. По умолчанию аудит выключен. Пока `HINDSIGHT_API_AUDIT_LOG_ENABLED=false`, таблица остаётся пустой, а `/audit-logs` возвращает `{"total": 0, "items": []}`. Для включения задайте `true` и перезапустите API.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_AUDIT_LOG_ENABLED` | Главный переключатель; должен быть `true`, иначе события не записываются | `false` |
| `HINDSIGHT_API_AUDIT_LOG_ACTIONS` | Разрешённые типы событий через запятую; пустое значение означает все подходящие действия | Пусто |
| `HINDSIGHT_API_AUDIT_LOG_RETENTION_DAYS` | Сколько дней хранить записи; `-1` хранит их бессрочно | `-1` |

### Трассировка запросов LLM

Трассировка записывает вызовы LLM для retain, reflect и консолидации в таблицу `llm_requests`, доступную по банку через `/llm-requests`. Запись содержит входные сообщения, ответ модели, число входных, выходных, кэшированных и общих токенов из ответа поставщика, причину завершения, поставщика и модель, время выполнения и данные вызывающей стороны. Неудачные вызовы тоже сохраняются со статусом `error` и текстом ошибки. Данные записываются через OpenTelemetry GenAI — тот же `record_llm_call`, что используется при экспорте OTLP.

Трассировка по умолчанию включена, записи хранятся один день. Чтобы отключить её полностью, задайте `HINDSIGHT_API_LLM_TRACE_ENABLED=false` и перезапустите API. Подсказки и ответы могут содержать чувствительные данные памяти и занимать много места: ограничьте длину через `HINDSIGHT_API_LLM_TRACE_MAX_CHARS`, сократите срок хранения или отключите трассировку.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_LLM_TRACE_ENABLED` | Главный переключатель трассировки; должен быть `true` для записи вызовов | `true` |
| `HINDSIGHT_API_LLM_TRACE_SCOPES` | Список областей вызова через запятую, например `retain_extract_facts,reflect`; пустой список означает все области | Пусто |
| `HINDSIGHT_API_LLM_TRACE_RETENTION_DAYS` | Срок хранения записей; `-1` — бессрочно | `1` |
| `HINDSIGHT_API_LLM_TRACE_MAX_CHARS` | Обрезать вход и выход длиннее указанного числа символов, сохранив сокращённую запись | `50000` |

### Настройка из кода

API можно настроить из программы через `MemoryEngine.from_env()`:

```python
from hindsight_api import MemoryEngine

memory = MemoryEngine.from_env()
await memory.initialize()
```

---

## Наблюдаемость и трассировка

Hindsight использует OpenTelemetry для наблюдения за вызовами LLM и соответствует семантическим соглашениям GenAI.

### Трассировка OpenTelemetry

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_OTEL_TRACES_ENABLED` | Включить распределённую трассировку вызовов LLM | `false` |
| `HINDSIGHT_API_OTEL_EXPORTER_OTLP_ENDPOINT` | Адрес OTLP, например Grafana LGTM или Langfuse | — |
| `HINDSIGHT_API_OTEL_EXPORTER_OTLP_HEADERS` | Заголовки экспортера в формате `key1=value1,key2=value2` | — |
| `HINDSIGHT_API_OTEL_SERVICE_NAME` | Имя сервиса в трассировках | `hindsight-api` |
| `HINDSIGHT_API_OTEL_DEPLOYMENT_ENVIRONMENT` | Среда развёртывания, например development, staging или production | `development` |
| `HINDSIGHT_API_METRICS_INCLUDE_BANK_ID` | Добавлять `bank_id` в атрибуты метрик OTel. Включайте только при малом числе банков: высокая кардинальность может привести к неограниченному росту памяти. | `false` |
| `HINDSIGHT_API_METRICS_BACKLOG_ENABLED` | Публиковать показатели глубины очереди асинхронных задач и отставания консолидации (`hindsight_async_operations`, `hindsight_consolidation_backlog`, `hindsight_consolidation_failed`). Фоновая задача периодически выполняет запросы `COUNT` по схемам. | `false` |

Трассировки могут содержать полные подсказки и ответы, данные о количестве входных и выходных токенов, модель и поставщика, а также сведения об ошибках и причинах завершения. Формат соответствует соглашениям OpenTelemetry GenAI версии 1.37 и новее. Экспортер использует стандартный OTLP HTTP, поэтому подходят Grafana LGTM (Tempo, Loki, Mimir и интерфейс Grafana), Langfuse, OpenLIT, DataDog, New Relic и Honeycomb.

Пример включения OTLP:

```bash
# Включить трассировку
export HINDSIGHT_API_OTEL_TRACES_ENABLED=true

# Адрес и заголовки (пример для OpenLIT Cloud)
export HINDSIGHT_API_OTEL_EXPORTER_OTLP_ENDPOINT=https://otlp.openlit.io
export HINDSIGHT_API_OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer olit-xxx"

# Необязательно: имя сервиса и среда
export HINDSIGHT_API_OTEL_SERVICE_NAME=hindsight-production
export HINDSIGHT_API_OTEL_DEPLOYMENT_ENVIRONMENT=production
```

Для локальной разработки можно запустить стек Grafana LGTM командой `./scripts/dev/start-grafana.sh`; подробности — в `scripts/dev/grafana/README.md`. Другие варианты описаны в `scripts/dev/openlit/README.md` (OpenLIT) и `scripts/dev/jaeger/README.md` (Jaeger).

### Метрики

Метрики Prometheus доступны по адресу `/metrics`, например `http://localhost:8888/metrics`. Они включают длительность и расход токенов вызовов LLM, длительность операций retain/recall/reflect, HTTP-запросы и состояние пула соединений с БД. Метрики всегда включены.

---

## Control Plane

Control Plane — веб-интерфейс для управления банками памяти.

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_CP_DATAPLANE_API_URL` | Адрес сервиса API | `http://localhost:8888` |
| `HINDSIGHT_CP_DATAPLANE_API_KEY` | Bearer-токен, который Control Plane передаёт в `Authorization` при каждом запросе к защищённому API. Для открытого API не задаётся. | Не задан (заголовок не отправляется) |
| `HINDSIGHT_CP_ACCESS_KEY` | Ключ доступа к интерфейсу Control Plane; если задан, пользователи должны ввести его для входа | Не задан (проверка отключена) |
| `HINDSIGHT_CP_MAX_UPLOAD_SIZE` | Максимальный размер одного запроса загрузки файла до его обрезки: строка (`100mb`, `1gb`) или число байтов. Увеличьте для более крупных файлов и согласуйте с `HINDSIGHT_API_FILE_CONVERSION_MAX_BATCH_SIZE_MB`. | `100mb` |
| `NEXT_PUBLIC_BASE_PATH` | Базовый путь интерфейса за обратным прокси, например `/hindsight` | `""` (корень) |

```bash
# Подключить Control Plane к удалённому API
export HINDSIGHT_CP_DATAPLANE_API_URL=http://api.example.com:8888

# Авторизация в защищённом API
export HINDSIGHT_CP_DATAPLANE_API_KEY=my-dataplane-bearer-token

# Защитить сам интерфейс ключом доступа
export HINDSIGHT_CP_ACCESS_KEY=my-secret-key
```

### Иерархическая настройка

Переопределения для банка наследуются по цепочке **общие настройки (переменные окружения) → арендатор → банк**.

#### Типобезопасное чтение настроек

Чтобы код не использовал глобальное значение вместо настройки банка, Hindsight проверяет тип доступа. В приложении `get_config()` подходит для статических параметров инфраструктуры, например `host` и `port`. Обращение к настраиваемым параметрам банка, например `retain_chunk_size`, через общий конфиг вызывает `ConfigFieldAccessError`. Для конкретного банка используйте `ConfigResolver.resolve_full_config(bank_id, context)`, чтобы получить полную конфигурацию с учётом наследования. Так ошибочное чтение глобального значения вместо банковского перехватывается заранее.

#### Модель безопасности

1. **Настраиваемые поля** — безопасные поведенческие параметры, доступные для банка: режим retain и размеры фрагментов, миссия и инструкции retain, включение наблюдений и автоконсолидации, миссия наблюдений, лимит наблюдений и разрешённые инструменты MCP.
2. **Поля учётных данных** — ключи API (`*_api_key`) и базовые адреса (`*_base_url`). API никогда не возвращает их и не позволяет переопределять.
3. **Статические поля сервера** — инфраструктура (`database_url`, `port`, `host`, число рабочих процессов), выбор поставщика и модели (для такого выбора нужны профили; пока не реализовано), производительность LLM, поиск и флаги оптимизации. Их нельзя переопределить для банка.

#### Включение API настроек банка

| Переменная | Назначение | Значение по умолчанию |
|------------|------------|-----------------------|
| `HINDSIGHT_API_ENABLE_BANK_CONFIG_API` | Включить API переопределений настроек по банкам | `true` |
| `HINDSIGHT_API_ENABLE_BANK_LLM_HEALTH` | Включить проверку связи с LLM через `POST /v1/default/banks/{bank_id}/health/llm`. Выполняется реальный вызов поставщика, поэтому по умолчанию выключено. Возвращает только состояние, не поставщика, модель или адрес. | `false` |
| `HINDSIGHT_API_ENABLE_DRY_RUN_EXTRACT` | Включить предварительный просмотр извлечения через `POST /v1/default/banks/{bank_id}/memories/dry-run-extract`. Выполняется реальный вызов LLM, но данные не сохраняются. `false` убирает endpoint (ответ 404). | `true` |
| `HINDSIGHT_API_DEFAULT_BANK_TEMPLATE` | JSON-шаблон банка, автоматически применяемый к каждому новому банку | Не задано |

`HINDSIGHT_API_DEFAULT_BANK_TEMPLATE` задаёт шаблон сервера. Он применяется один раз при первом обращении к новому банку через `PUT /v1/default/banks/{bank_id}`, `/import`, `/retain` и другие операции. Значение — JSON `BankTemplateManifest`, такой же формы, как в `POST /v1/default/banks/{bank_id}/import` (секции банка, ментальных моделей и директив).

Поля шаблона становятся переопределениями банка и имеют приоритет над соответствующими `HINDSIGHT_API_*` переменными окружения. Позже пользователь может изменить отдельные значения через `PATCH /v1/default/banks/{bank_id}/config`. Шаблон повторно не применяется и не затирает ручные изменения. Если JSON некорректен, версия неизвестна или схема не проходит проверку, ошибка попадает в журнал, но создание банка продолжается с обычными значениями по умолчанию.

```bash
export HINDSIGHT_API_DEFAULT_BANK_TEMPLATE='{"version":"1","bank":{"reflect_mission":"Help support agents remember customer interactions.","retain_extraction_mode":"verbose","disposition_empathy":5},"directives":[{"name":"Be concise","content":"Always respond concisely.","priority":10}]}'
```

Настройки банка доступны через `GET /v1/default/banks/{bank_id}/config`, изменяются через `PATCH /v1/default/banks/{bank_id}/config`, а `DELETE /v1/default/banks/{bank_id}/config` сбрасывает переопределения.

Расширения арендатора могут ограничить доступные для изменения поля методом `get_allowed_config_fields()`: вернуть `None`, чтобы разрешить все настраиваемые поля; набор имён, чтобы разрешить отдельные поля; пустое множество, чтобы запретить все изменения.

Проверки безопасности: ключи и базовые адреса не возвращаются в ответах; менять можно только настраиваемые поля; ответ отфильтрован по разрешениям арендатора; попытка задать учётные данные возвращает HTTP 400.

### Размещение за обратным прокси и подкаталогом

Чтобы разместить Hindsight по пути, например `example.com/hindsight/`, задайте одинаковый путь в `HINDSIGHT_API_BASE_PATH=/hindsight` и `NEXT_PUBLIC_BASE_PATH=/hindsight`. Настройте обратный прокси так, чтобы он передавал запросы `/hindsight/*` в Hindsight, сохранял полный путь и выставлял заголовки `X-Forwarded-Proto` и `X-Forwarded-For`.

Пример Nginx:

```nginx
location /hindsight/ {
    proxy_pass http://localhost:8888/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Базовый путь должен начинаться с `/` и не должен заканчиваться `/`. API и Control Plane должны использовать одинаковый путь; после изменения перезапустите оба сервиса. Документация OpenAPI будет доступна по `<base-path>/docs`, например `/hindsight/docs`. Дополнительные конфигурации Nginx, Docker Compose и Traefik приведены в `docker/compose-examples/`.

## Пример файла .env

```bash
# Сервис API
HINDSIGHT_API_DATABASE_URL=postgresql://hindsight:hindsight_dev@localhost:5432/hindsight
# HINDSIGHT_API_DATABASE_SCHEMA=public  # необязательно; по умолчанию public
HINDSIGHT_API_LLM_PROVIDER=groq
HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx

# Аутентификация (необязательно, рекомендуется для рабочей среды)
# HINDSIGHT_API_TENANT_EXTENSION=hindsight_api.extensions.builtin.tenant:ApiKeyTenantExtension
# HINDSIGHT_API_TENANT_API_KEY=your-secret-api-key

# Хранение файлов (необязательно; по умолчанию используется PostgreSQL)
# HINDSIGHT_API_FILE_STORAGE_TYPE=s3
# HINDSIGHT_API_FILE_STORAGE_S3_BUCKET=my-hindsight-files
# HINDSIGHT_API_FILE_STORAGE_S3_REGION=us-east-1
# HINDSIGHT_API_FILE_STORAGE_S3_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
# HINDSIGHT_API_FILE_STORAGE_S3_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# Control Plane
HINDSIGHT_CP_DATAPLANE_API_URL=http://localhost:8888
```

Если здесь не описана нужная настройка, [создайте issue в GitHub](https://github.com/vectorize-io/hindsight/issues).
