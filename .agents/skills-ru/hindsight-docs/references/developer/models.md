# Модели

Hindsight использует несколько моделей машинного обучения для разных задач.

## Обзор

- **LLM** — извлечение фактов, рассуждение и генерация. Поставщик выбирается отдельно и полностью настраивается.
- **Векторная модель** — создаёт векторные представления для семантического поиска. По умолчанию: `BAAI/bge-small-en-v1.5`.
- **Cross-Encoder** — повторно ранжирует результаты поиска. По умолчанию: `cross-encoder/ms-marco-MiniLM-L-6-v2`.

Векторная модель и Cross-Encoder автоматически загружаются с Hugging Face при первом запуске.

---

## LLM

Используется для извлечения фактов, сопоставления сущностей, объединения ментальных моделей и синтеза ответов.

**Поддерживаемые поставщики:** OpenAI, Anthropic, Google Gemini, Vertex AI, Groq, Ollama, Ollama Cloud, LM Studio, llama.cpp, MiniMax, DeepSeek, z.ai, opencode-go, Atlas Cloud, Volcano Engine, OpenRouter, OpenAI Codex, Claude Code, AWS Bedrock, Fireworks AI, Nous Portal, OpenAI Compatible и LiteLLM (более 100 поставщиков).

Поддерживается **любой API, совместимый с OpenAI** (например, Azure OpenAI, Together AI, Fireworks), а также **более 100 поставщиков через LiteLLM** (например, AWS Bedrock, Azure OpenAI, Together AI).

> **💡 Поставщики, совместимые с OpenAI**
>
> Hindsight работает с любым поставщиком, у которого есть API, совместимый с OpenAI, например Azure OpenAI. Задайте `HINDSIGHT_API_LLM_PROVIDER=openai`, а переменную `HINDSIGHT_API_LLM_BASE_URL` направьте на конечную точку поставщика.
>
> Примеры настройки приведены в разделе [Конфигурация](./configuration#llm-provider).

> **💡 AWS Bedrock**
>
> Задайте `HINDSIGHT_API_LLM_PROVIDER=bedrock`, чтобы напрямую использовать модели AWS Bedrock. В именах моделей используются идентификаторы Bedrock, например `us.amazon.nova-2-lite-v1:0`. API-ключ не нужен: аутентификация выполняется через учётные данные AWS (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION_NAME`) или роли IAM. Чтобы сократить стоимость обработки на 50%, задайте `HINDSIGHT_API_LLM_BEDROCK_SERVICE_TIER=flex`. См. [настройки поставщика](./configuration#llm-provider).

> **💡 Встроенный llama.cpp — локально, без API-ключа**
>
> Задайте `HINDSIGHT_API_LLM_PROVIDER=llamacpp`, чтобы запустить встроенный сервер llama.cpp без внешних зависимостей. При первом запуске автоматически загружается модель Gemma 4 E2B в формате GGUF (около 3,5 ГБ). Нужен дополнительный пакет `local-llm`: `pip install 'hindsight-api-slim[local-llm]'`.
>
> В опубликованный образ Docker не входит `llama-cpp-python`, чтобы сохранить небольшой размер. Пример запуска Docker с этим пакетом: [`docker/docker-compose/local-llm/`](https://github.com/vectorize-io/hindsight/tree/main/docker/docker-compose/local-llm). Все параметры — в разделе [встроенного llama.cpp](./configuration#built-in-llamacpp).

> **💡 Поставщик LiteLLM (Azure, Together AI и другие)**
>
> Задайте `HINDSIGHT_API_LLM_PROVIDER=litellm`, чтобы использовать любую модель из [списка LiteLLM](https://docs.litellm.ai/docs/providers), включая **Azure OpenAI**, **Together AI**, **Fireworks AI** и другие. Имена указываются с префиксом поставщика LiteLLM, например `azure/gpt-4o`. Примеры — в разделе [Конфигурация](./configuration#llm-provider).

> **💡 Маршрутизатор LiteLLM: резервные модели, балансировка и лимиты развёртываний**
>
> Задайте `HINDSIGHT_API_LLM_PROVIDER=litellmrouter`, чтобы запускать LLM через [Router в LiteLLM](https://docs.litellm.ai/docs/routing). Доступны упорядоченные резервные модели, балансировка запросов одного уровня, выбор с весами, отдельные лимиты `rpm` / `tpm` для развёртываний и периоды охлаждения. Настройки передаются в JSON-конфигурации [Router](https://docs.litellm.ai/docs/routing#fallbacks) без изменений. Подробнее: [настройки маршрутизатора](./configuration#llm-router-litellm-router).

### Возможности поставщиков

Помимо базовой генерации некоторые поставщики поддерживают дополнительные функции, сокращающие стоимость или задержку. Hindsight автоматически использует функцию, если настроенный поставщик её поддерживает.

| Поставщик | Пакетный API | Явное кэширование запросов |
|-----------|:------------:|:--------------------------:|
| OpenAI (`openai`) | ✅ | — |
| Anthropic (`anthropic`) | — | — |
| Google Gemini (`gemini`) | ✅ | ✅ |
| Vertex AI (`vertexai`) | — | ✅ |
| Groq (`groq`) | ✅ | — |
| Ollama (`ollama`) | — | — |
| Ollama Cloud (`ollama-cloud`) | — | — |
| LM Studio (`lmstudio`) | — | — |
| llama.cpp (`llamacpp`) | — | — |
| MiniMax (`minimax`) | — | — |
| DeepSeek (`deepseek`) | — | — |
| z.ai (`zai`) | — | — |
| opencode-go (`opencode-go`) | — | — |
| Atlas Cloud (`atlas`) | — | — |
| Volcano Engine (`volcano`) | — | — |
| OpenRouter (`openrouter`) | — | — |
| OpenAI Codex (`openai-codex`) | — | — |
| Claude Code (`claude-code`) | — | — |
| AWS Bedrock (`bedrock`) | — | — |
| Fireworks AI (`fireworks`) | ✅ | — |
| Nous Portal (`nous`) | — | — |
| LiteLLM (более 100) (`litellm`) | — | — |

- **Пакетный API** — отправляет набор данных для извлечения через асинхронную конечную точку поставщика, обычно примерно за половину цены. Если доступен, используется автоматически; иначе запросы выполняются синхронно.
- **Явное кэширование запросов** — повторно использует большой неизменный системный префикс, который при каждом вызове отправляется для retain (извлечение фактов), консолидации и цикла инструментов reflect, оплачивая его по тарифу кэшированного ввода поставщика. Для Gemini и Vertex используется API `CachedContent`. Функция **включена по умолчанию**; отключите её через `HINDSIGHT_API_LLM_PROMPT_CACHE_ENABLED=false`. Запросы составлены так, чтобы кэшированный префикс **не зависел от банка** и общий кэш использовался разными банками, а не создавался для каждого банка или миссии. При ошибке кэширования запрос выполняется без кэша — работа не прерывается.

> **📝 Примечание**
>
> Пустая ячейка в столбце «Явное кэширование запросов» не означает, что у поставщика нет кэширования. Например, OpenAI автоматически кэширует стабильный начальный префикс; это работает без настройки. Anthropic поддерживает кэширование через точки `cache_control`, которые можно подключить тем же способом. В таблице отмечен только явный вызов Hindsight `get_or_create_cached_prefix`, доступный сейчас для Gemini и Vertex.

### Сравнение моделей

Не знаете, какую модель выбрать? В [рейтинге моделей](https://benchmarks.hindsight.vectorize.io/) сравниваются точность, скорость, стоимость и надёжность для retain, reflect и консолидации наблюдений. Используйте его, чтобы выбрать оптимальный баланс для своей задачи.

### Проверенные модели

Следующие модели проверены на совместимость с Hindsight:

| Поставщик | Модель |
|-----------|--------|
| OpenAI | `gpt-5.2`, `gpt-5`, `gpt-5-mini`, `gpt-5-nano`, `gpt-4.1-mini`, `gpt-4.1-nano`, `gpt-4o-mini` |
| Anthropic | `claude-sonnet-4-20250514`, `claude-3-5-sonnet-20241022` |
| Gemini | `gemini-3.5-flash`, `gemini-3.1-pro-preview`, `gemini-3.1-flash-lite` |
| Groq | `openai/gpt-oss-120b`, `openai/gpt-oss-20b` |

### Модели по умолчанию для поставщиков

Если `HINDSIGHT_API_LLM_MODEL` не задана, используется рекомендованная модель поставщика. Это упрощает настройку: достаточно выбрать поставщика, чтобы получить подходящее значение по умолчанию.

| Поставщик | Модель по умолчанию |
|-----------|---------------------|
| `openai` | `gpt-4o-mini` |
| `anthropic` | `claude-haiku-4-5` |
| `gemini` | `gemini-3.5-flash` |
| `vertexai` | `google/gemini-3.1-flash-lite` |
| `groq` | `openai/gpt-oss-120b` |
| `ollama` | `gemma3:12b` |
| `ollama-cloud` | `gemma3:12b` |
| `lmstudio` | `local-model` |
| `llamacpp` | `gemma-4-e2b-it` (загружается автоматически в GGUF) |
| `minimax` | `MiniMax-M3` |
| `deepseek` | `deepseek-v4-flash` |
| `zai` | `glm-4.5-flash` |
| `opencode-go` | `deepseek-v4-flash` |
| `atlas` | `deepseek-ai/deepseek-v4-pro` |
| `volcano` | `doubao-pro-32k` |
| `openrouter` | `qwen/qwen3.5-9b` |
| `openai-codex` | `gpt-5.4-mini` |
| `claude-code` | `claude-sonnet-4-5-20250929` |
| `bedrock` | `us.amazon.nova-2-lite-v1:0` |
| `fireworks` | `accounts/fireworks/models/llama-v3p1-8b-instruct` |
| `nous` | `deepseek/deepseek-v4-flash` |
| `litellm` | `gpt-4o-mini` |

**Пример:** если указать только поставщика, будет выбрана его модель по умолчанию:

```bash
# Автоматически использует claude-haiku-4-5
export HINDSIGHT_API_LLM_PROVIDER=anthropic
export HINDSIGHT_API_LLM_API_KEY=sk-ant-xxxxxxxxxxxx
```

Модель по умолчанию можно переопределить через `HINDSIGHT_API_LLM_MODEL`:

```bash
# Вместо неё использует Sonnet
export HINDSIGHT_API_LLM_PROVIDER=anthropic
export HINDSIGHT_API_LLM_API_KEY=sk-ant-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=claude-sonnet-4-5-20250929
```

Это работает и для переопределения модели отдельной операции:

```bash
# Общая модель: OpenAI gpt-4o-mini (по умолчанию)
export HINDSIGHT_API_LLM_PROVIDER=openai

# Для retain: Anthropic claude-haiku-4-5 (по умолчанию)
export HINDSIGHT_API_RETAIN_LLM_PROVIDER=anthropic
```

### Другие модели

Другие модели LLM тоже могут работать с Hindsight, но для надёжного извлечения фактов они должны поддерживать **не менее 65 000 выходных токенов**. Чтобы запросить поддержку конкретной модели, которая не соответствует этому требованию, [создайте issue](https://github.com/hindsight-ai/hindsight/issues).

> **💡 Модели с ограничением выходных токенов**
>
> Если модель поддерживает не более 32 тысяч выходных токенов (например, некоторые старые модели), уменьшите лимит токенов ответа для retain:

```bash
# Для моделей с лимитом в 32 тысячи выходных токенов
export HINDSIGHT_API_RETAIN_MAX_COMPLETION_TOKENS=32000

# Для моделей с лимитом в 16 тысяч выходных токенов
export HINDSIGHT_API_RETAIN_MAX_COMPLETION_TOKENS=16000
```

**Важно:** `HINDSIGHT_API_RETAIN_MAX_COMPLETION_TOKENS` должен быть больше `HINDSIGHT_API_RETAIN_CHUNK_SIZE` (по умолчанию 3000). При запуске система проверит настройки и сообщит об ошибке, если условие не выполнено.

> **⚠️ Бесплатный тариф Groq не подходит для Hindsight**
>
> Бесплатный тариф Groq ограничен 8 000 токенами в минуту — гораздо меньше, чем нужно для одного вызова retain (около 64 тысяч). Бесплатные модели Groq использовать с Hindsight нельзя; выберите платный тариф Groq или другого поставщика.

### Общая конфигурация LLM

```bash
# Groq (рекомендуется)
export HINDSIGHT_API_LLM_PROVIDER=groq
export HINDSIGHT_API_LLM_API_KEY=gsk_xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=openai/gpt-oss-20b

# OpenAI
export HINDSIGHT_API_LLM_PROVIDER=openai
export HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gpt-4o

# Gemini
export HINDSIGHT_API_LLM_PROVIDER=gemini
export HINDSIGHT_API_LLM_API_KEY=xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=gemini-3.5-flash

# Anthropic
export HINDSIGHT_API_LLM_PROVIDER=anthropic
export HINDSIGHT_API_LLM_API_KEY=sk-ant-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=claude-sonnet-4-20250514

# Ollama (локально)
export HINDSIGHT_API_LLM_PROVIDER=ollama
export HINDSIGHT_API_LLM_BASE_URL=http://localhost:11434/v1
export HINDSIGHT_API_LLM_MODEL=llama3

# Ollama Cloud (размещённая служба, нужен API-ключ)
export HINDSIGHT_API_LLM_PROVIDER=ollama-cloud
export HINDSIGHT_API_LLM_API_KEY=your-ollama-cloud-api-key
export HINDSIGHT_API_LLM_MODEL=gemma3:12b

# LM Studio (локально)
export HINDSIGHT_API_LLM_PROVIDER=lmstudio
export HINDSIGHT_API_LLM_BASE_URL=http://localhost:1234/v1
export HINDSIGHT_API_LLM_MODEL=your-local-model

# MiniMax (контекстное окно 1 млн токенов)
export HINDSIGHT_API_LLM_PROVIDER=minimax
export HINDSIGHT_API_LLM_API_KEY=your-minimax-api-key
export HINDSIGHT_API_LLM_MODEL=MiniMax-M3  # или MiniMax-M2.7 предыдущего поколения

# DeepSeek (https://api.deepseek.com)
export HINDSIGHT_API_LLM_PROVIDER=deepseek
export HINDSIGHT_API_LLM_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_LLM_MODEL=deepseek-v4-flash  # или deepseek-v4-pro / deepseek-chat / deepseek-reasoner

# z.ai (серия Zhipu GLM, совместимая с OpenAI, https://z.ai)
export HINDSIGHT_API_LLM_PROVIDER=zai
export HINDSIGHT_API_LLM_API_KEY=your-zai-api-key
export HINDSIGHT_API_LLM_MODEL=glm-4.5-flash  # или glm-4.5-air для платного тарифа

# opencode-go (совместим с OpenAI)
export HINDSIGHT_API_LLM_PROVIDER=opencode-go
export HINDSIGHT_API_LLM_API_KEY=your-opencode-go-api-key
export HINDSIGHT_API_LLM_MODEL=deepseek-v4-flash

# Atlas Cloud (совместим с OpenAI, https://www.atlascloud.ai)
export HINDSIGHT_API_LLM_PROVIDER=atlas
export HINDSIGHT_API_LLM_API_KEY=your-atlascloud-api-key  # базовый URL по умолчанию: https://api.atlascloud.ai/v1
export HINDSIGHT_API_LLM_MODEL=deepseek-ai/deepseek-v4-pro  # модель рассуждений; также доступны Qwen / GLM / Kimi / MiniMax и др.

# Nous Portal (совместим с OpenAI; API-ключ не нужен, используется вход через `hermes portal`)
export HINDSIGHT_API_LLM_PROVIDER=nous
export HINDSIGHT_API_LLM_MODEL=deepseek/deepseek-v4-flash  # любая модель Nous
# API-ключ не нужен: автоматически читает обновляемый JWT из ~/.hermes/auth.json

# Vertex AI (Google Cloud)
export HINDSIGHT_API_LLM_PROVIDER=vertexai
export HINDSIGHT_API_LLM_MODEL=gemini-3.1-flash-lite
export HINDSIGHT_API_LLM_VERTEXAI_PROJECT_ID=your-gcp-project-id
# Необязательно: регион (по умолчанию us-central1)
# export HINDSIGHT_API_LLM_VERTEXAI_REGION=us-central1
# Необязательно: ключ сервисной учётной записи (иначе используется ADC)
# export HINDSIGHT_API_LLM_VERTEXAI_SERVICE_ACCOUNT_KEY=/path/to/key.json
```

Основное ограничение производительности операций retain — скорость LLM. Рекомендации по оптимизации приведены в разделе [Производительность](./performance).

### Настройка OpenAI Codex (ChatGPT Plus / Pro)

Используйте подписку ChatGPT Plus или Pro для Hindsight без отдельных расходов на API OpenAI Platform.

**Требования:** активная подписка ChatGPT Plus или Pro и установленный Node.js / npm (для Codex CLI).

**Настройка:**

1. Установите Codex CLI: `npm install -g @openai/codex`.
2. Войдите с учётными данными ChatGPT: `codex auth login`. Откроется браузер; OAuth-токены сохраняются в `~/.codex/auth.json`.
3. Убедитесь, что файл существует: `ls ~/.codex/auth.json`.
4. Задайте поставщика и запустите Hindsight:

```bash
export HINDSIGHT_API_LLM_PROVIDER=openai-codex
# export HINDSIGHT_API_LLM_MODEL=gpt-5.3-codex  # по умолчанию gpt-5.4-mini
# API-ключ не нужен — данные автоматически читаются из ~/.codex/auth.json
hindsight-api
```

Доступна любая модель, поддерживаемая Codex CLI.

**Примечания:** OAuth-токены хранятся в `~/.codex/auth.json` и обновляются автоматически. Использование оплачивается подпиской ChatGPT, а не отдельными запросами API. Только для личной разработки; см. условия использования ChatGPT.

### Настройка Nous Portal (Hermes)

Используйте подписку [Nous Portal](https://portal.nousresearch.com) для Hindsight через вход в Hermes CLI; постоянный API-ключ не требуется.

**Требования:** учётная запись Nous Portal и установленный CLI [Hermes](https://hermes-agent.nousresearch.com).

1. Выполните `hermes portal`, войдите через открывшийся браузер. OAuth-данные сохраняются в `~/.hermes/auth.json`.
2. Проверьте вход: `hermes portal status` (должно показать `Auth: ✓ logged in`).
3. Задайте поставщика и запустите Hindsight:

```bash
export HINDSIGHT_API_LLM_PROVIDER=nous
# export HINDSIGHT_API_LLM_MODEL=deepseek/deepseek-v4-flash  # модель по умолчанию
# API-ключ не нужен — данные автоматически читаются из ~/.hermes/auth.json
hindsight-api
```

Можно использовать любую модель из API инференса Nous Portal. Учётные данные берутся из общего с Hermes файла `~/.hermes/auth.json`; короткоживущий JWT обновляется автоматически до истечения срока и после ошибки 401. Обновления согласуются с запущенным агентом Hermes, поэтому сессии не мешают друг другу. URL по умолчанию: `https://inference-api.nousresearch.com/v1`; его можно переопределить через `HINDSIGHT_API_LLM_BASE_URL`.

### Настройка Claude Code (Claude Pro / Max)

Используйте подписку Claude Pro или Max для Hindsight без отдельной оплаты API Anthropic.

> **⚠️ Об условиях использования**
>
> Интеграция использует Claude Agent SDK и учётные данные личной подписки Claude Pro / Max. Перед использованием войдите в Claude Code на своём компьютере.

Учитывайте следующее:

- В [документации Agent SDK Anthropic](https://docs.claude.com/en/api/agent-sdk/overview) говорится, что сторонним разработчикам не следует предлагать вход в claude.ai или его лимиты использования в составе своих продуктов. Hindsight не выполняет вход за пользователя, а использует учётные данные, уже авторизованные командой `claude auth login`.
- В январе 2026 года Anthropic [ввела ограничения](https://paddo.dev/blog/anthropic-walled-garden-crackdown/) против сторонних инструментов, использующих OAuth-токены подписки Claude. Ограничения касались инструментов, **подделывавших идентичность клиента Claude Code**; Hindsight вместо этого использует официальный Claude Agent SDK.
- Поставщик предназначен **только для личной разработки на локальном компьютере**. Не используйте его в производственной среде или общем окружении.
- Условия Anthropic могут измениться. Для гарантированного соответствия используйте поставщика `anthropic` с API-ключом.
- Использование расходует лимит подписки Claude Pro / Max.

Для production и командного использования рекомендуется `HINDSIGHT_API_LLM_PROVIDER=anthropic` с ключом из [Anthropic Console](https://console.anthropic.com/).

**Требования:** активная подписка Claude Pro или Max и установленный CLI Claude Code.

1. Установите CLI: `npm install -g @anthropics/claude-code` или `brew install anthropics/claude-code/claude-code`.
2. Выполните `claude auth login` и войдите через браузер. Аутентификацией автоматически управляет Claude Agent SDK.
3. Проверьте установку командой `claude --version`.
4. Задайте поставщика и запустите Hindsight:

```bash
export HINDSIGHT_API_LLM_PROVIDER=claude-code
# API-ключ не нужен — используются данные входа claude auth login
hindsight-api
```

Поддерживается любая модель из CLI Claude Code. SDK управляет аутентификацией и безопасным хранением учётных данных. Использование оплачивается подпиской Claude, а не отдельными запросами API. Только для личной разработки; см. условия Claude.

### Настройка Vertex AI (Google Cloud)

Vertex AI в Google Cloud предоставляет доступ к моделям Gemini через нативный Google GenAI SDK.

**Требования:** проект GCP с включённым Vertex AI API и роль IAM `roles/aiplatform.user` для используемых учётных данных.

| Переменная | Описание | Обязательная |
|------------|----------|--------------|
| `HINDSIGHT_API_LLM_VERTEXAI_PROJECT_ID` | Идентификатор проекта GCP | Да |
| `HINDSIGHT_API_LLM_VERTEXAI_REGION` | Регион GCP, например `us-central1` | Нет, по умолчанию `us-central1` |
| `HINDSIGHT_API_LLM_VERTEXAI_SERVICE_ACCOUNT_KEY` | Путь к JSON-ключу сервисной учётной записи | Нет, если не указан, применяется ADC |

**Способы аутентификации:**

1. **Application Default Credentials (ADC)** — рекомендуется для разработки. Выполните `gcloud auth application-default login`, затем задайте `HINDSIGHT_API_LLM_PROVIDER=vertexai`, `HINDSIGHT_API_LLM_MODEL=gemini-3.1-flash-lite` и `HINDSIGHT_API_LLM_VERTEXAI_PROJECT_ID=your-project-id`.
2. **Ключ сервисной учётной записи** — рекомендуется для production. Создайте учётную запись `gcloud iam service-accounts create hindsight-api`, назначьте ей роль `roles/aiplatform.user` через `gcloud projects add-iam-policy-binding`, создайте ключ командой `gcloud iam service-accounts keys create key.json`, затем задайте поставщика, модель, ID проекта и `HINDSIGHT_API_LLM_VERTEXAI_SERVICE_ACCOUNT_KEY=/path/to/key.json`.

К имени модели можно добавлять префикс `google/`, например `google/gemini-3.1-flash-lite`; он удаляется автоматически. Нативный SDK сам обновляет токены. Если задан ключ сервисной учётной записи, он используется вместо ADC.

---

## Векторная модель

Преобразует текст в плотные векторы для семантического поиска по сходству.

**По умолчанию:** `BAAI/bge-small-en-v1.5` (384 измерения, около 130 МБ).

### Поддерживаемые поставщики

| Поставщик | Описание | Лучше всего подходит для |
|-----------|----------|--------------------------|
| `local` | SentenceTransformers (по умолчанию) | Разработки и низкой задержки |
| `onnx` | Векторизация в ONNX Runtime в процессе, без Ollama / TEI / отдельного API | Локального процессора, малого размера и многоязычия |
| `openai` | API векторизации OpenAI | Production и высокого качества |
| `openai-codex` | Векторизация OpenAI через OAuth Codex (ChatGPT Plus / Pro, без API-ключа) | Подписчиков ChatGPT / Codex |
| `openrouter` | Векторизация OpenRouter (шлюз, совместимый с OpenAI) | Настроек с несколькими поставщиками |
| `cohere` | API векторизации Cohere | Production и многоязычия |
| `google` | Векторизация Google через Gemini API или Vertex AI | Production, многоязычия и высокого качества |
| `tei` | Hugging Face Text Embeddings Inference | Собственного production-развёртывания |
| `zeroentropy` | ZeroEntropy zembed-1 | Качественного поиска |
| `litellm` | Прокси LiteLLM — единый шлюз | Настроек с несколькими поставщиками |
| `litellm-sdk` | SDK LiteLLM — прямой API без прокси | Простого подключения разных поставщиков |

### Локальные модели

| Модель | Измерения | Сценарий |
|--------|-----------|----------|
| `BAAI/bge-small-en-v1.5` | 384 | По умолчанию; быстрая, хорошее качество |
| `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` | 384 | Многоязычная (более 50 языков) |

### Модели OpenAI

| Модель | Измерения | Сценарий |
|--------|-----------|----------|
| `text-embedding-3-small` | 1536 | Модель OpenAI по умолчанию, доступная по цене |
| `text-embedding-3-large` | 3072 | Выше качество, дороже |
| `text-embedding-ada-002` | 1536 | Устаревшая модель |

### Модели Google

| Модель | Измерения | Сценарий |
|--------|-----------|----------|
| `gemini-embedding-001` | 768 (настраивается) | Модель Google по умолчанию, общего назначения |
| `gemini-embedding-2-preview` | 768 (настраивается) | Мультимодальная модель семейства Gemini Embedding 2; один вектор на вход |

У `gemini-embedding-001` можно настроить число измерений выходного вектора посредством усечения. Google рекомендует 768, 1536 или 3072. Параметр: `HINDSIGHT_API_EMBEDDINGS_GEMINI_OUTPUT_DIMENSIONALITY`; значение по умолчанию — 768.

Семейство `gemini-embedding-2`, включая `gemini-embedding-2-preview`, поддерживается и в Gemini API, и в Vertex AI. Эти модели объединяют несколько входов, поэтому для соответствия вектора каждому факту Hindsight автоматически отправляет по одному входу за вызов.

### Модели Cohere

| Модель | Измерения | Сценарий |
|--------|-----------|----------|
| `embed-english-v3.0` | 1024 | Текст на английском |
| `embed-multilingual-v3.0` | 1024 | Более 100 языков |

### Модели ZeroEntropy

| Модель | Измерения | Сценарий |
|--------|-----------|----------|
| `zembed-1` | По умолчанию 1280; можно задать 2560 / 1280 / 640 / 320 / 160 / 80 / 40 | Качественный асимметричный поиск |

Hindsight отправляет сохраняемые воспоминания в ZeroEntropy как входы типа `document`, а текст поиска или Recall — как входы типа `query`. API ZeroEntropy по умолчанию возвращает 2560 измерений; Hindsight использует 1280, чтобы HNSW в pgvector работал без замены векторного расширения.

> **⚠️ Размерность векторов**
>
> Hindsight определяет размерность вектора при запуске и подстраивает схему базы данных. После сохранения воспоминаний изменить размерность без потери данных нельзя.

Примеры конфигурации разных поставщиков приведены ниже; названия переменных и значений нужно сохранить без изменений:

```bash
# Локальный поставщик (по умолчанию)
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=local
export HINDSIGHT_API_EMBEDDINGS_LOCAL_MODEL=BAAI/bge-small-en-v1.5

# ONNX: локальный процессор, без Ollama / TEI / отдельного API
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=onnx
export HINDSIGHT_API_EMBEDDINGS_ONNX_MODEL_ID=intfloat/multilingual-e5-small
export HINDSIGHT_API_EMBEDDINGS_ONNX_DIMENSIONS=384

# OpenAI
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=openai
export HINDSIGHT_API_EMBEDDINGS_OPENAI_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_EMBEDDINGS_OPENAI_MODEL=text-embedding-3-small

# Cohere
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=cohere
export HINDSIGHT_API_COHERE_API_KEY=your-api-key
export HINDSIGHT_API_EMBEDDINGS_COHERE_MODEL=embed-english-v3.0

# Google, аутентификация через API-ключ
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=google
export HINDSIGHT_API_EMBEDDINGS_GEMINI_API_KEY=xxxxxxxxxxxx
export HINDSIGHT_API_EMBEDDINGS_GEMINI_MODEL=gemini-embedding-001

# Google, аутентификация Vertex AI определяется автоматически по ID проекта
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=google
export HINDSIGHT_API_EMBEDDINGS_GEMINI_MODEL=gemini-embedding-001
export HINDSIGHT_API_EMBEDDINGS_VERTEXAI_PROJECT_ID=your-gcp-project-id

# TEI (собственный сервер)
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=tei
export HINDSIGHT_API_EMBEDDINGS_TEI_URL=http://localhost:8080

# ZeroEntropy
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=zeroentropy
export HINDSIGHT_API_EMBEDDINGS_ZEROENTROPY_API_KEY=your-api-key
export HINDSIGHT_API_EMBEDDINGS_ZEROENTROPY_MODEL=zembed-1
export HINDSIGHT_API_EMBEDDINGS_ZEROENTROPY_DIMENSIONS=1280

# Прокси LiteLLM
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=litellm
export HINDSIGHT_API_LITELLM_API_BASE=http://localhost:4000
export HINDSIGHT_API_EMBEDDINGS_LITELLM_MODEL=text-embedding-3-small

# SDK LiteLLM — прямое подключение без прокси
export HINDSIGHT_API_EMBEDDINGS_PROVIDER=litellm-sdk
export HINDSIGHT_API_EMBEDDINGS_LITELLM_SDK_API_KEY=sk-xxxxxxxxxxxx
export HINDSIGHT_API_EMBEDDINGS_LITELLM_SDK_MODEL=openai/text-embedding-3-small
```

Все параметры, включая Azure OpenAI и пользовательские конечные точки, описаны в разделе [Конфигурация векторизации](./configuration#embeddings).

---

## Cross-Encoder (повторное ранжирование)

Повторно ранжирует исходные результаты поиска, чтобы повысить точность.

**По умолчанию:** `cross-encoder/ms-marco-MiniLM-L-6-v2` (около 85 МБ).

### Поддерживаемые поставщики

| Поставщик | Описание | Лучше всего подходит для |
|-----------|----------|--------------------------|
| `local` | SentenceTransformers CrossEncoder (по умолчанию) | Разработки и низкой задержки |
| `cohere` | API ранжирования Cohere | Production и высокого качества |
| `openrouter` | API ранжирования OpenRouter (шлюз, совместимый с Cohere) | Конфигураций с несколькими поставщиками |
| `zeroentropy` | ZeroEntropy rerank API (zerank-2) | Production и высокой точности |
| `siliconflow` | API SiliconFlow, совместимая с Cohere конечная точка `/rerank` | Пользователей SiliconFlow и клиентов из Китая |
| `alibaba` | API Alibaba Cloud DashScope (qwen3-rerank) | Пользователей Alibaba Cloud / DashScope |
| `google` | API ранжирования Google Discovery Engine (REST и вход Google) | Production и интеграции с GCP |
| `tei` | Hugging Face Text Embeddings Inference | Собственного production-развёртывания |
| `flashrank` | FlashRank — быстрая облегчённая модель | Сред с ограниченными ресурсами |
| `litellm` | Прокси LiteLLM — единый шлюз | Конфигураций с несколькими поставщиками |
| `litellm-sdk` | SDK LiteLLM — прямой API без прокси | Простого подключения разных поставщиков |
| `jina-mlx` | Jina rerank v3 через Apple Silicon MLX, локально без API-ключа | Локальной обработки на Apple Silicon (M1 и новее) |
| `rrf` | Только RRF, без нейронного ранжирования | Тестирования и минимальных ресурсов |

### Локальные модели

| Модель | Сценарий |
|--------|----------|
| `cross-encoder/ms-marco-MiniLM-L-6-v2` | По умолчанию, быстрая |
| `cross-encoder/ms-marco-MiniLM-L-12-v2` | Выше точность |
| `cross-encoder/mmarco-mMiniLMv2-L12-H384-v1` | Многоязычная |

### Модели Cohere

| Модель | Сценарий |
|--------|----------|
| `rerank-english-v3.0` | Английский текст |
| `rerank-multilingual-v3.0` | Более 100 языков |

### Модели ZeroEntropy

| Модель | Сценарий |
|--------|----------|
| `zerank-2` | Основная многоязычная модель, по умолчанию |
| `zerank-2-small` | Более быстрая и лёгкая версия |

### Модели SiliconFlow

SiliconFlow предоставляет ряд моделей с открытыми весами через конечную точку `/rerank`, совместимую с Cohere:

| Модель | Сценарий |
|--------|----------|
| `BAAI/bge-reranker-v2-m3` | Многоязычная, хороший вариант по умолчанию |
| `Qwen/Qwen3-Reranker-8B` | Крупнее, выше точность |

### Модели Alibaba Cloud

Alibaba Cloud DashScope предоставляет `qwen3-rerank` через конечную точку `/reranks`, совместимую с Cohere:

| Модель | Сценарий |
|--------|----------|
| `qwen3-rerank` | Более 100 языков, модель по умолчанию |

### Поддерживаемые поставщики LiteLLM

LiteLLM поддерживает несколько поставщиков повторного ранжирования через конечную точку `/rerank`:

| Поставщик | Пример модели |
|-----------|---------------|
| Cohere | `cohere/rerank-english-v3.0` |
| Together AI | `together_ai/...` |
| Voyage AI | `voyage/rerank-2` |
| Jina AI | `jina_ai/...` |
| AWS Bedrock | `bedrock/...` |

### Примеры настройки Cross-Encoder

```bash
# Локальный поставщик (по умолчанию)
export HINDSIGHT_API_RERANKER_PROVIDER=local
export HINDSIGHT_API_RERANKER_LOCAL_MODEL=cross-encoder/ms-marco-MiniLM-L-6-v2

# Cohere
export HINDSIGHT_API_RERANKER_PROVIDER=cohere
export HINDSIGHT_API_COHERE_API_KEY=your-api-key
export HINDSIGHT_API_RERANKER_COHERE_MODEL=rerank-english-v3.0

# Конечная точка, совместимая с Cohere (Azure AI Foundry, Jina, Voyage, собственная BGE и др.)
# COHERE_BASE_URL переключает поставщика со SDK Cohere на обычный HTTP-клиент
# со стандартным форматом запросов rerank.
export HINDSIGHT_API_RERANKER_PROVIDER=cohere
export HINDSIGHT_API_RERANKER_COHERE_API_KEY=your-api-key
export HINDSIGHT_API_RERANKER_COHERE_MODEL=rerank-v3.5
export HINDSIGHT_API_RERANKER_COHERE_BASE_URL=https://your-endpoint.example/rerank

# ZeroEntropy (высокая точность)
export HINDSIGHT_API_RERANKER_PROVIDER=zeroentropy
export HINDSIGHT_API_RERANKER_ZEROENTROPY_API_KEY=your-api-key
export HINDSIGHT_API_RERANKER_ZEROENTROPY_MODEL=zerank-2

# SiliconFlow (конечная точка /rerank, совместимая с Cohere)
export HINDSIGHT_API_RERANKER_PROVIDER=siliconflow
export HINDSIGHT_API_RERANKER_SILICONFLOW_API_KEY=your-api-key
export HINDSIGHT_API_RERANKER_SILICONFLOW_MODEL=BAAI/bge-reranker-v2-m3

# Alibaba Cloud DashScope (qwen3-rerank)
export HINDSIGHT_API_RERANKER_PROVIDER=alibaba
export HINDSIGHT_API_RERANKER_ALIBABA_API_KEY=your-dashscope-api-key
export HINDSIGHT_API_RERANKER_ALIBABA_MODEL=qwen3-rerank

# TEI (собственный сервер)
export HINDSIGHT_API_RERANKER_PROVIDER=tei
export HINDSIGHT_API_RERANKER_TEI_URL=http://localhost:8081

# FlashRank (облегчённый)
export HINDSIGHT_API_RERANKER_PROVIDER=flashrank

# Прокси LiteLLM
export HINDSIGHT_API_RERANKER_PROVIDER=litellm
export HINDSIGHT_API_LITELLM_API_BASE=http://localhost:4000
export HINDSIGHT_API_RERANKER_LITELLM_MODEL=cohere/rerank-english-v3.0

# Только RRF, без нейронного ранжирования
export HINDSIGHT_API_RERANKER_PROVIDER=rrf
```

Все параметры, включая конечные точки Azure и пакетную обработку, приведены в разделе [Конфигурация ранжировщика](./configuration#reranker).
