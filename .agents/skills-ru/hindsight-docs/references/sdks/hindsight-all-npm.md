---
sidebar_position: 6
---

# Программный API (Node.js)

Пакет npm `@vectorize-io/hindsight-all` — аналог пакета Python [`hindsight-all`](./hindsight-all.md) для Node.js. Код Node.js может запускать местную службу Hindsight и следить за ней без отдельной серверной среды. Для операций с памятью подключите [`@vectorize-io/hindsight-client`](./nodejs.md).

Служба работает как **отдельный процесс ОС** на `127.0.0.1`, а не внутри процесса Node.js. Ваш код общается с ней по HTTP через `HindsightClient`.

Этот пакет **не содержит HTTP-клиент**: он управляет только процессом сервера. После запуска службы обращайтесь к ней через [`@vectorize-io/hindsight-client`](./nodejs.md), передав адрес из `server.getBaseUrl()`. Один пакет управляет процессом, другой — вызовами API.

## Как это работает

1. `server.start()` находит команду `hindsight-embed`: через `uvx` из PyPI либо через `uv run --directory <path>` для местной копии.
2. Выполняет `profile create <name> --merge --port <port> [--env KEY=VALUE ...]` и передаёт каждую запись `options.env` как `--env`.
3. Выполняет `daemon --profile <name> start`.
4. Проверяет `http://host:port/health`, пока не получит `200` или не истечёт срок `readyTimeoutMs`.
5. `server.stop()` выполняет `daemon --profile <name> stop`.

Обёртка оставляет прямой доступ к настройкам службы: новые переменные среды и флаги CLI не требуют новой версии пакета. Передавайте их через `env`, `extraProfileCreateArgs` или `extraDaemonStartArgs`.

## Требования

- **Node.js ≥ 22** — нужны глобальные `fetch` и `AbortSignal.timeout`.
- **`uv` / `uvx`** в `PATH` — для загрузки и запуска службы Hindsight. Установка описана на [docs.astral.sh/uv](https://docs.astral.sh/uv/).

## Установка

```bash
npm install @vectorize-io/hindsight-all @vectorize-io/hindsight-client
```

## Пример

```ts
import { HindsightServer, consoleLogger } from '@vectorize-io/hindsight-all';
import { HindsightClient } from '@vectorize-io/hindsight-client';

const server = new HindsightServer({
  profile: 'my-app',
  port: 9077,
  env: {
    HINDSIGHT_API_LLM_PROVIDER: 'anthropic',
    HINDSIGHT_API_LLM_API_KEY: process.env.ANTHROPIC_API_KEY,
    HINDSIGHT_API_LLM_MODEL: 'claude-sonnet-4-20250514',
    HINDSIGHT_EMBED_DAEMON_IDLE_TIMEOUT: '0',
  },
  logger: consoleLogger,
});

await server.start();

const client = new HindsightClient({ baseUrl: server.getBaseUrl() });
await client.retain('user-123', 'User prefers dark mode.');
const recall = await client.recall('user-123', 'what are the user preferences?');

await server.stop();
```

Для удалённого API Hindsight сервер запускать не нужно: передайте его адрес прямо в `HindsightClient`.

## `HindsightServerOptions`

| Параметр | Тип | По умолчанию | Описание |
|---|---|---|---|
| `profile` | `string` | `"default"` | Имя профиля, которое каждый раз передаётся через `--profile`. |
| `port` | `number` | `8888` | TCP-порт службы. |
| `host` | `string` | `"127.0.0.1"` | Имя узла, к которому привязана служба; нужно для проверки работы. |
| `embedVersion` | `string` | `"latest"` | Версия пакета `hindsight-embed`, запускаемого через `uvx`. |
| `embedPackagePath` | `string` | — | Путь к местной копии. Имеет приоритет над `embedVersion`; вместо `uvx` берётся `uv run --directory`. |
| `env` | `Record<string, string \| undefined>` | `{}` | Переменные среды, передаваемые процессу службы **и** записываемые в профиль через `--env KEY=VALUE`. Основной способ задать любую настройку `HINDSIGHT_API_*` / `HINDSIGHT_EMBED_*`. |
| `extraProfileCreateArgs` | `string[]` | `[]` | Лишние аргументы, без правок добавляемые в конце `profile create`. |
| `extraDaemonStartArgs` | `string[]` | `[]` | Лишние аргументы, без правок добавляемые в конце `daemon start`. |
| `platformCpuWorkaround` | `boolean` | `true` на macOS | Автоматически задаёт `HINDSIGHT_API_EMBEDDINGS_LOCAL_FORCE_CPU=1` и `HINDSIGHT_API_RERANKER_LOCAL_FORCE_CPU=1`, чтобы избежать сбоев Metal/MPS. Значения из `env` пользователя имеют приоритет. |
| `readyTimeoutMs` | `number` | `30000` | Сколько ждать ответа 200 от `/health`. |
| `readyPollIntervalMs` | `number` | `1000` | Пауза между проверками `/health`. |
| `logger` | `Logger` | без вывода | Подключаемый журнал (`debug`/`info`/`warn`/`error`). Экспортируются помощники `consoleLogger` и `silentLogger`. |

## Методы сервера

| Метод | Возвращает | Описание |
|---|---|---|
| `start()` | `Promise<void>` | Настраивает профиль, запускает службу и ждёт `/health`. Повторный вызов безопасен. |
| `stop()` | `Promise<void>` | Останавливает службу. Не выбрасывает ошибку: при сбое пишет в журнал и завершает вызов. |
| `checkHealth()` | `Promise<boolean>` | Один раз проверяет `/health` с пределом ожидания 2 с. |
| `getBaseUrl()` | `string` | `http://host:port`; передайте его в `HindsightClient`. |
| `getProfile()` | `string` | Имя профиля этого сервера. |

Для операций с памятью (retain, recall, reflect, управление банками) применяйте [`@vectorize-io/hindsight-client`](./nodejs.md).
