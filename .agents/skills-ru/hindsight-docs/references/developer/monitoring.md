# Слежение за работой

Hindsight даёт метрики Prometheus, распределённые трассы OpenTelemetry и готовые панели Grafana.

## Местная разработка

Для наблюдения на своём компьютере возьмите набор Grafana LGTM (Loki, Grafana, Tempo, Mimir):

```bash
./scripts/dev/start-monitoring.sh
```

Команда запускает один контейнер Docker с такими службами:
- **Интерфейс Grafana**: http://localhost:3000 (доступ администратора без входа).
- **Трассы (Tempo)**: адрес OTLP http://localhost:4318 (HTTP) и http://localhost:4317 (gRPC).
- **Метрики (Prometheus/Mimir)**: сами собираются с http://localhost:8888/metrics.
- **Журналы (Loki)**: доступны для общего сбора.
- **Готовые панели**: операции Hindsight, метрики LLM, служба API.

**Включите трассы в API:**
```bash
export HINDSIGHT_API_OTEL_TRACES_ENABLED=true
export HINDSIGHT_API_OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
```

:::note Рабочая среда
Местный набор слежения нужен только для разработки. В рабочей среде разверните Grafana LGTM отдельно либо возьмите платную службу (Grafana Cloud, DataDog, New Relic и др.).
:::

## Панели Grafana

Готовые панели лежат в [`monitoring/grafana/dashboards/`](https://github.com/anthropics/hindsight/tree/main/monitoring/grafana/dashboards). Загрузите эти JSON-файлы в свой Grafana:

| Панель | Что показывает |
|-----------|-------------|
| **Hindsight Operations** | Частоту операций, доли задержек, метрики по банкам |
| **Hindsight LLM Metrics** | Вызовы LLM, число токенов, задержки по задачам и поставщикам |
| **Hindsight API Service** | HTTP-запросы, ошибки, пул базы, метрики процесса |

При запуске через скрипт слежения панели добавляются сами.

## Адрес метрик

Метрики Prometheus доступны по `/metrics`:

```bash
curl http://localhost:8888/metrics
```

## Доступные метрики

### Метрики операций

| Метрика | Вид | Метки | Описание |
|--------|------|--------|-------------|
| `hindsight.operation.duration` | Гистограмма | operation, bank_id, source, budget, max_tokens, success | Длительность операций в секундах |
| `hindsight.operation.total` | Счётчик | operation, bank_id, source, budget, max_tokens, success | Общее число выполненных операций |

**Метки:**
- `operation`: вид операции (`retain`, `recall`, `reflect` и виды фоновых задач, например `consolidation`).
- `bank_id`: ID банка памяти.
- `source`: откуда вызвана операция (`api`, `reflect`, `internal`, `worker`).
- `budget`: бюджет, если задан (`low`, `mid`, `high`).
- `max_tokens`: предел токенов, если задан.
- `success`: удалась ли операция (`true`, `false`).

Метка `source` позволяет различить:
- `api`: прямые вызовы API от клиентов.
- `reflect`: внутренние вызовы recall при reflect.
- `internal`: другие внутренние операции.
- `worker`: завершения фоновых задач, записанные, когда взятая задача дошла до итогового состояния.

При `source="worker"` метка `success` показывает успех завершения задачи:
`false` значит, что задача после всех повторов вернула ошибку обработчику очереди или возник неожиданный сбой. Ошибки, пойманные внутри исполнителя с обычным возвратом, всё равно дадут здесь `success="true"`. Для точного статуса ошибки фоновой операции смотрите `hindsight_async_operations{status="failed"}`.

### Метрики LLM

| Метрика | Вид | Метки | Описание |
|--------|------|--------|-------------|
| `hindsight.llm.duration` | Гистограмма | provider, model, scope, success | Длительность вызовов API LLM в секундах |
| `hindsight.llm.calls.total` | Счётчик | provider, model, scope, success | Общее число вызовов API LLM |
| `hindsight.llm.tokens.input` | Счётчик | provider, model, scope, success, token_bucket | Входные токены вызовов LLM |
| `hindsight.llm.tokens.output` | Счётчик | provider, model, scope, success, token_bucket | Выходные токены вызовов LLM |

**Метки:**
- `provider`: поставщик LLM (`openai`, `anthropic`, `gemini`, `groq`, `ollama`, `lmstudio`, `bedrock`, `litellm`).
- `model`: имя модели (например, `gpt-4`, `claude-3-sonnet`).
- `scope`: задача вызова LLM (`memory`, `reflect`, `consolidation`, `answer`).
- `success`: успешность вызова (`true`, `false`).
- `token_bucket`: группа числа токенов, ограничивающая число разных меток (`0-100`, `100-500`, `500-1k`, `1k-5k`, `5k-10k`, `10k-50k`, `50k+`).

### Метрики HTTP-запросов

| Метрика | Вид | Метки | Описание |
|--------|------|--------|-------------|
| `hindsight.http.duration` | Гистограмма | method, endpoint, status_code, status_class | Длительность HTTP-запросов в секундах |
| `hindsight.http.requests.total` | Счётчик | method, endpoint, status_code, status_class | Общее число HTTP-запросов |
| `hindsight.http.requests.in_progress` | UpDownCounter | method, endpoint | Число HTTP-запросов в работе |

**Метки:**
- `method`: метод HTTP (`GET`, `POST`, `PUT`, `DELETE`).
- `endpoint`: путь запроса; UUID заменяются на `{id}`, чтобы меток не стало слишком много.
- `status_code`: код HTTP (`200`, `400`, `500` и др.).
- `status_class`: группа кода (`2xx`, `4xx`, `5xx`).

### Метрики пула базы данных

| Метрика | Вид | Метки | Описание |
|--------|------|--------|-------------|
| `hindsight.db.pool.size` | Gauge | — | Текущее число соединений в пуле |
| `hindsight.db.pool.idle` | Gauge | — | Число свободных соединений в пуле |
| `hindsight.db.pool.min` | Gauge | — | Минимальный размер пула |
| `hindsight.db.pool.max` | Gauge | — | Максимальный размер пула |

### Метрики процесса

| Метрика | Вид | Метки | Описание |
|--------|------|--------|-------------|
| `hindsight.process.cpu.seconds` | Gauge | type | Время работы CPU в секундах |
| `hindsight.process.memory.bytes` | Gauge | type | Объём памяти процесса в байтах |
| `hindsight.process.open_fds` | Gauge | — | Число открытых файловых дескрипторов |
| `hindsight.process.threads` | Gauge | — | Число активных потоков |

**Метки:**
- `type` (CPU): `user` или `system`.
- `type` (память): `rss_max` (самый большой объём занятой физической памяти).

### Группы гистограмм

Для более точных долей заданы свои границы групп:

**Длительность операций (секунды):**
```
0.1, 0.25, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0, 7.5, 10.0, 15.0, 20.0, 30.0, 60.0, 120.0
```

**Длительность LLM (секунды):**
```
0.1, 0.25, 0.5, 1.0, 2.0, 3.0, 5.0, 10.0, 15.0, 30.0, 60.0, 120.0
```

**Длительность HTTP (секунды):**
```
0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0, 30.0
```

## Настройка Prometheus

```yaml
scrape_configs:
  - job_name: 'hindsight'
    static_configs:
      - targets: ['localhost:8888']
```

## Примеры запросов

### Средняя задержка операций по виду
```promql
rate(hindsight_operation_duration_sum[5m]) / rate(hindsight_operation_duration_count[5m])
```

### Вызовы LLM в минуту по поставщикам
```promql
rate(hindsight_llm_calls_total[1m]) * 60
```

### 95-й процентиль задержки LLM
```promql
histogram_quantile(0.95, rate(hindsight_llm_duration_bucket[5m]))
```

### Общее число токенов по моделям
```promql
sum by (model) (hindsight_llm_tokens_input_total + hindsight_llm_tokens_output_total)
```

### Внутренние и прямые вызовы Recall
```promql
sum by (source) (rate(hindsight_operation_total{operation="recall"}[5m]))
```

### HTTP-запросы в секунду по адресам
```promql
sum by (endpoint) (rate(hindsight_http_requests_total[1m]))
```

### Доля ошибок HTTP (5xx)
```promql
sum(rate(hindsight_http_requests_total{status_class="5xx"}[5m])) / sum(rate(hindsight_http_requests_total[5m]))
```

### 95-й процентиль задержки HTTP
```promql
histogram_quantile(0.95, sum by (le) (rate(hindsight_http_duration_seconds_bucket[5m])))
```

### Доля занятых мест в пуле базы
```promql
hindsight_db_pool_size / hindsight_db_pool_max
```

### Активные соединения с базой
```promql
hindsight_db_pool_size - hindsight_db_pool_idle
```

### Доля времени CPU
```promql
rate(hindsight_process_cpu_seconds{type="user"}[1m])
```

---

## Распределённые трассы

Hindsight поддерживает трассы OpenTelemetry для операций памяти и вызовов LLM по правилам GenAI v1.37+.

### Настройка

Переменные среды описаны в разделе [«Настройка — трассы OpenTelemetry»](./configuration#opentelemetry-tracing).

**Быстрый старт:**
```bash
# Enable tracing
export HINDSIGHT_API_OTEL_TRACES_ENABLED=true
export HINDSIGHT_API_OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318

# View traces with Grafana LGTM (local dev)
./scripts/dev/start-monitoring.sh
# Open http://localhost:3000 → Explore → Tempo
```

Подойдёт любая служба с OTLP: Grafana LGTM, Langfuse, OpenLIT, DataDog, New Relic, Honeycomb, [Pydantic Logfire](https://logfire.pydantic.dev) и др.

### Иерархия участков трассы

**Главные участки (операции):**
- `hindsight.retain` — загрузка воспоминаний.
- `hindsight.recall` — поиск памяти.
  - `hindsight.recall_embedding` — вектор запроса.
  - `hindsight.recall_retrieval` — параллельный поиск по смыслу, BM25, графу и времени.
  - `hindsight.recall_fusion` — слияние рангов RRF.
  - `hindsight.recall_rerank` — повторная сортировка кросс-энкодером.
- `hindsight.reflect` — рассуждение агента.
  - `hindsight.reflect_tool_call` — вызов инструмента (recall, lookup и др.).
- `hindsight.consolidation` — обобщение наблюдений.
- `hindsight.mental_model_refresh` — обновление ментальной модели.

**Вложенные участки (вызовы LLM):**
- Именуются по задаче, например `hindsight.memory`, `hindsight.reflect`.
- Содержат полные запросы и ответы как события.
- Следуют правилам имён свойств GenAI.

### Свойства участков

**Операции:**
- `hindsight.operation` — вид операции.
- `hindsight.bank_id` — ID банка.
- `hindsight.query` — текст запроса (не более 100 символов).
- `hindsight.fact_types` — виды фактов для recall.
- `hindsight.thinking_budget` — выделенный бюджет.
- `hindsight.max_tokens` — предел токенов.

**Вызовы LLM (правила GenAI):**
- `gen_ai.operation.name` — всегда `"chat"`.
- `gen_ai.provider.name` — поставщик (`openai`, `anthropic`, `google` и др.).
- `gen_ai.request.model` — имя модели.
- `gen_ai.usage.input_tokens` — входные токены.
- `gen_ai.usage.output_tokens` — выходные токены.
- `hindsight.scope` — задача вызова LLM (`memory`, `reflect`, `consolidation` и др.).

**События:**
- `gen_ai.client.inference.operation.details` — полные запросы и ответы.
