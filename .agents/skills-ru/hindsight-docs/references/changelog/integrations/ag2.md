---
hide_table_of_contents: true
---

# ag2 — история изменений интеграции

История изменений [`hindsight-ag2`](https://pypi.org/project/hindsight-ag2/).

Исходный код: [`hindsight-integrations/ag2`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/ag2).

← [К общей истории изменений](../index.md)

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/ag2/v0.1.2)

**Улучшения**

- Улучшена проверка типов в интеграции AG2: пакет теперь содержит сведения о типах по PEP 561. ([`d054b884`](https://github.com/vectorize-io/hindsight/commit/d054b884))

**Исправления ошибок**

- Все HTTP-запросы интеграции AG2 теперь содержат заголовок User-Agent для лучшей совместимости и отслеживания. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))
- Устранены критические и серьёзные уязвимости в зависимостях. ([`ee4510a7`](https://github.com/vectorize-io/hindsight/commit/ee4510a7))

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/ag2/v0.1.1)

**Новые возможности**

- Добавлена интеграция Hindsight с фреймворком AG2. ([`73123870`](https://github.com/vectorize-io/hindsight/commit/73123870))
