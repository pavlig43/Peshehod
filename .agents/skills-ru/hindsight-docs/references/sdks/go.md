---
sidebar_position: 3
---

# Клиент для Go

Официальный клиент Go для API Hindsight. Он создан по схеме OpenAPI 3.1 с помощью [OpenAPI Generator](https://github.com/OpenAPITools/openapi-generator).

import CodeSnippet from '@site/src/components/CodeSnippet';
import quickstartGo from '!!raw-loader!@site/examples/api/quickstart.go';

## Установка

```bash
go get github.com/vectorize-io/hindsight/hindsight-clients/go
```

Нужен Go 1.23 или новее.

## Быстрый старт

<CodeSnippet code={quickstartGo} section="quickstart-full" language="go" />

## Устройство API

Клиент Go даёт доступ ко всем операциям API Hindsight через отдельные группы:

- **`client.MemoryAPI`** — операции retain, recall, reflect
- **`client.BanksAPI`** — управление банками
- **`client.DirectivesAPI`** — управление указаниями
- **`client.MentalModelsAPI`** — управление ментальными моделями
- **`client.DocumentsAPI`** — операции с документами
- **`client.EntitiesAPI`** — операции с сущностями
- **`client.OperationsAPI`** — слежение за асинхронными операциями

## Поля, которые могут быть пустыми

Для необязательных полей клиент Go применяет типы `NullableString`, `NullableTime` и подобные:

<CodeSnippet code={quickstartGo} section="nullable-fields" language="go" />

## Обработка ошибок

<CodeSnippet code={quickstartGo} section="error-handling" language="go" />

## Другие примеры

Подробные примеры всех операций:
- [Документация SDK для Python](./python.md) — те же понятия API
- [Документация SDK для Node.js](./nodejs.md) — те же понятия API
- [Схема OpenAPI](https://hindsight.dev/openapi.json) — полное описание API
