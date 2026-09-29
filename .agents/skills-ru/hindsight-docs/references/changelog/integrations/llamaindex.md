---
hide_table_of_contents: true
---

# LlamaIndex — история изменений интеграции

История изменений [`hindsight-llamaindex`](https://pypi.org/project/hindsight-llamaindex/).

Исходный код: [`hindsight-integrations/llamaindex`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/llamaindex).

← [К общей истории изменений](../index.md)

## [0.1.5](https://github.com/vectorize-io/hindsight/tree/integrations/llamaindex/v0.1.5)

**Улучшения**

- Устаревший ручной тест заменён набором сквозных тестов с отдельным условием запуска. Интеграцию можно проверять без реальной языковой модели при каждом прогоне.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/ed34756c" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>ed34756c</a>

**Исправления ошибок**

- Интеграция LlamaIndex по умолчанию работает с Hindsight Cloud, что упрощает подключение и снижает число ошибок при настройке.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/ed34756c" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>ed34756c</a>

## [0.1.4](https://github.com/vectorize-io/hindsight/tree/integrations/llamaindex/v0.1.4)

**Улучшения**

- Все HTTP-запросы теперь содержат единый заголовок User-Agent. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))
- В пакет добавлен маркер PEP 561 для более точной проверки типов Python. ([`d054b884`](https://github.com/vectorize-io/hindsight/commit/d054b884))
- Обновлены зависимости для устранения критических и серьёзных уязвимостей. ([`ee4510a7`](https://github.com/vectorize-io/hindsight/commit/ee4510a7))

## [0.1.3](https://github.com/vectorize-io/hindsight/tree/integrations/llamaindex/v0.1.3)

**Исправления ошибок**

- В интеграции LlamaIndex исправлена работа с идентификаторами документов, API памяти и трассировками ReAct. ([`d93dfea8`](https://github.com/vectorize-io/hindsight/commit/d93dfea8))

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/llamaindex/v0.1.2)

**Новые возможности**

- Добавлена интеграция LlamaIndex с Hindsight. ([`2d787c4f`](https://github.com/vectorize-io/hindsight/commit/2d787c4f))
