# Расширения

Расширения позволяют менять и дополнять работу Hindsight без правки основного кода. С их помощью можно добавить несколько арендаторов, свой вход в систему, новые адреса HTTP и обработчики событий операций.

---

## Доступные расширения

### TenantExtension

Отвечает за работу с несколькими арендаторами и вход по ключу API. Проверяет входящие запросы и выбирает схему PostgreSQL для операций с базой. Это разделяет данные арендаторов на уровне базы.

**Встроено: ApiKeyTenantExtension**

Простая реализация: сверяет ключ API с переменной среды и берёт схему `public` для всех запросов, прошедших проверку.

```bash
HINDSIGHT_API_TENANT_EXTENSION=hindsight_api.extensions.builtin.tenant:ApiKeyTenantExtension
HINDSIGHT_API_TENANT_API_KEY=your-secret-key
```

**Встроено: SupabaseTenantExtension**

Проверяет JWT из [Supabase](https://supabase.com) и разделяет память арендаторов. Каждый вошедший пользователь получает свою схему PostgreSQL (`{prefix}_{user_id}`), поэтому данные полностью отделены. JWT проверяется на месте через JWKS: сетевой вызов на каждый запрос не нужен.

```bash
HINDSIGHT_API_TENANT_EXTENSION=hindsight_api.extensions.builtin.supabase_tenant:SupabaseTenantExtension
HINDSIGHT_API_TENANT_SUPABASE_URL=https://your-project.supabase.co
# Optional - only needed for legacy HS256 projects or health check
HINDSIGHT_API_TENANT_SUPABASE_SERVICE_KEY=your-service-role-key
```

Полные настройки и детали работы есть в [исходном коде](https://github.com/vectorize-io/hindsight/blob/main/hindsight-api-slim/hindsight_api/extensions/builtin/supabase_tenant.py).

Если каждому арендатору нужна своя схема, но вход устроен иначе (например, свой JWT), напишите собственный `TenantExtension`.

---

### HttpExtension

Добавляет свои адреса HTTP с префиксом `/ext/`. Подходит для API вашей сферы, которым нужен движок памяти Hindsight.

Даёт два метода маршрутизации:
- `get_router(memory)` — возвращает маршрутизатор FastAPI для `/ext/`.
- `get_root_router(memory)` — возвращает маршрутизатор FastAPI для корня приложения (если адрес должен быть строго на заданном пути). По умолчанию возвращает `None`.

**Встроенной реализации нет**. Чтобы добавить адреса, напишите свою.

```bash
HINDSIGHT_API_HTTP_EXTENSION=mypackage.ext:MyHttpExtension
```

---

### OperationValidatorExtension

Подключается к retain/recall/reflect для проверки и слежения. Подходит для:
- Лимитов частоты и квот.
- Проверки прав и фильтра текста.
- Журнала аудита и учёта использования.
- Сбора своих метрик.

**Встроенной реализации нет**. Напишите свою под нужную задачу.

```bash
HINDSIGHT_API_OPERATION_VALIDATOR_EXTENSION=mypackage.validators:MyValidator
```

---

### MCPExtension

Регистрирует новые инструменты MCP (Model Context Protocol) на сервере MCP Hindsight. Сторонний пакет может добавить свои инструменты без правки основного кода.

**Встроенной реализации нет**. Для своих инструментов MCP напишите расширение.

```bash
HINDSIGHT_API_MCP_EXTENSION=mypackage.mcp:MyMCPExtension
```

---

## Как написать своё расширение

### Основы

Расширения — классы Python, которые загружаются по переменным среды:

```bash
HINDSIGHT_API_<TYPE>_EXTENSION=mypackage.module:MyExtensionClass
```

Настройки передаются через переменные среды с нужным префиксом:

```bash
HINDSIGHT_API_<TYPE>_SOME_CONFIG=value
# Extension receives: {"some_config": "value"}
```

Все расширения поддерживают события жизненного цикла:
- `on_startup()` — вызов при запуске приложения.
- `on_shutdown()` — вызов при остановке приложения.

Расширениям доступен `ExtensionContext` с методами:
- `run_migration(schema)` — выполнить миграции базы для схемы.
- `get_memory_engine()` — получить интерфейс MemoryEngine.

### Пример: свой TenantExtension с JWT

```python
import jwt
from hindsight_api.extensions import TenantExtension, TenantContext, AuthenticationError

class JwtTenantExtension(TenantExtension):
    def __init__(self, config: dict[str, str]):
        super().__init__(config)
        self.jwt_secret = config.get("jwt_secret")
        if not self.jwt_secret:
            raise ValueError("HINDSIGHT_API_TENANT_JWT_SECRET is required")

    async def authenticate(self, context: RequestContext) -> TenantContext:
        token = context.api_key
        if not token:
            # Optional headers dict is forwarded in HTTP/MCP error responses
            raise AuthenticationError("Bearer token required")

        try:
            payload = jwt.decode(token, self.jwt_secret, algorithms=["HS256"])
            tenant_id = payload.get("tenant_id")
            if not tenant_id:
                raise AuthenticationError("Missing tenant_id in token")
            return TenantContext(schema_name=f"tenant_{tenant_id}")
        except jwt.InvalidTokenError as e:
            raise AuthenticationError(str(e))
```

`AuthenticationError` принимает необязательный словарь `headers`, который попадёт в ответы об ошибке HTTP и MCP. Так можно вернуть свой заголовок, например `WWW-Authenticate`:

```python
raise AuthenticationError(
    "Authorization required",
    headers={"WWW-Authenticate": 'Bearer realm="example"'},
)
```

### Пример: свой HttpExtension

```python
from fastapi import APIRouter
from hindsight_api.extensions import HttpExtension

class MyHttpExtension(HttpExtension):
    def get_router(self, memory: MemoryEngine) -> APIRouter:
        router = APIRouter()

        @router.get("/hello")
        async def hello():
            return {"message": "Hello from extension!"}

        @router.post("/custom/{bank_id}/action")
        async def custom_action(bank_id: str):
            # Access memory engine for database operations
            pool = await memory._get_pool()
            # ... custom logic
            return {"status": "ok"}

        return router

    def get_root_router(self, memory: MemoryEngine) -> APIRouter | None:
        """Optional: mount routes at the application root (not under /ext/)."""
        router = APIRouter()

        @router.get("/.well-known/my-metadata")
        async def metadata():
            return {"version": "1.0"}

        return router
```

Маршруты из `get_router` доступны по `/ext/hello`, `/ext/custom/{bank_id}/action` и т. д.
Маршруты из `get_root_router` находятся в корне приложения, например `/.well-known/my-metadata`.

### Пример: свой OperationValidatorExtension

```python
from hindsight_api.extensions import (
    OperationValidatorExtension,
    ValidationResult,
    PrecheckContext,
    RetainContext,
    RecallContext,
    ReflectContext,
    RetainResult,
)

class MyValidator(OperationValidatorExtension):
    # Pre-body validation (optional)
    async def precheck(self, ctx: PrecheckContext) -> ValidationResult:
        if ctx.content_length is not None and ctx.content_length > 10_000_000:
            return ValidationResult.reject("Payload is too large")
        return ValidationResult.accept()

    # Pre-operation validation (required)
    async def validate_retain(self, ctx: RetainContext) -> ValidationResult:
        # Implement your validation logic
        return ValidationResult.accept()
        # Or reject: return ValidationResult.reject("Reason")

    async def validate_recall(self, ctx: RecallContext) -> ValidationResult:
        return ValidationResult.accept()

    async def validate_reflect(self, ctx: ReflectContext) -> ValidationResult:
        return ValidationResult.accept()

    # Post-operation hooks (optional)
    async def on_retain_complete(self, result: RetainResult) -> None:
        # Log usage, update metrics, send notifications, etc.
        pass
```

`precheck` работает до чтения и разбора тела запроса. Поле
`PrecheckContext.content_length` содержит целое число из заголовка
`Content-Length` либо `None`, если заголовка нет или его нельзя разобрать
(например, при передаче частями). Это дешёвый способ проверить размер,
квоту или расходы. После разбора всё равно запускаются полные обработчики
`validate_*`: они должны точно соблюдать пределы для каждой операции.

#### Отложить операцию

Кроме `accept` и `reject`, обработчик `validate_*` может попросить
**вернуть операцию в очередь на поздний срок**, вызвав `DeferOperation`.
Это помогает при перегрузке: лимит частоты у внешней службы, ещё не
открывшаяся квота, запуск зависимости. В отличие от повтора, такое
действие не увеличивает `retry_count` и не пишет `error_message`.
Обработчик ставит `next_retry_at` равным вашему `exec_date`. До этого
срока задача не видна запросам на взятие работы.

```python
from datetime import datetime, timedelta, timezone

from hindsight_api.extensions import (
    DeferOperation,
    OperationValidatorExtension,
    RetainContext,
    ValidationResult,
)


class QuotaAwareValidator(OperationValidatorExtension):
    async def validate_retain(self, ctx: RetainContext) -> ValidationResult:
        if not await self._quota_available(ctx.bank_id):
            raise DeferOperation(
                exec_date=datetime.now(timezone.utc) + timedelta(minutes=5),
                reason="bank quota window exhausted",
            )
        return ValidationResult.accept()
```

`DeferOperation` работает **лишь в обработчике задач**. Не вызывайте его из
`validate_recall` или `validate_reflect` на пути синхронного HTTP-запроса:
там нет очереди для переноса, и клиент получит 500.

### Пример: свой MCPExtension

```python
from mcp.server.fastmcp import FastMCP
from hindsight_api.extensions import MCPExtension
from hindsight_api.engine import MemoryEngine

class MyMCPExtension(MCPExtension):
    async def register_tools(self, mcp: FastMCP, memory: MemoryEngine) -> None:
        @mcp.tool()
        async def custom_search(query: str) -> str:
            """Custom MCP tool for specialized search."""
            # Access memory engine for operations
            pool = await memory._get_pool()
            # ... custom logic
            return f"Results for: {query}"
```

---

## Запуск своих расширений

### Через Docker

Подключите пакет расширения как том и задайте переменную среды:

```yaml
# docker-compose.yml
services:
  hindsight-api:
    image: vectorize/hindsight-api:latest
    volumes:
      - ./my_extensions:/app/my_extensions
    environment:
      - HINDSIGHT_API_TENANT_EXTENSION=my_extensions.auth:JwtTenantExtension
      - HINDSIGHT_API_TENANT_JWT_SECRET=${JWT_SECRET}
      - PYTHONPATH=/app
```

Либо соберите свой образ с расширениями:

```dockerfile
FROM vectorize/hindsight-api:latest
COPY my_extensions /app/my_extensions
ENV PYTHONPATH=/app
```

### На собственном сервере

Поставьте пакет расширения в ту же среду Python, что и Hindsight:

```bash
# Install Hindsight
pip install hindsight-api

# Install your extension package
pip install ./my-extensions
# or
pip install my-extensions-package

# Configure
export HINDSIGHT_API_TENANT_EXTENSION=my_extensions.auth:JwtTenantExtension
export HINDSIGHT_API_TENANT_JWT_SECRET=your-secret

# Run
hindsight-api
```

---

## Как поделиться расширением

Авторы Hindsight приветствуют расширения для частых задач. Если вы создали расширение для:

- Систем входа (OAuth, SAML, шлюзы API).
- Лимитов частоты или квот.
- Подключения журнала аудита.
- Выгрузки метрик (Datadog, New Relic и др.).
- Своих адресов HTTP для отдельных платформ.

Предложите добавить его в пакет `hindsight_api.extensions.builtin`. Чтобы обсудить расширение, создайте обращение или запрос на слияние на [GitHub](https://github.com/vectorize-io/hindsight).
