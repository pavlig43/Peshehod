---
hide_table_of_contents: true
---

# Strands — история изменений интеграции

История изменений [`hindsight-strands`](https://pypi.org/project/hindsight-strands/).

Исходный код: [`hindsight-integrations/strands`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/strands).

← [К общей истории изменений](../index.md)

## [0.1.3](https://github.com/vectorize-io/hindsight/tree/integrations/strands/v0.1.3)

**Исправления ошибок**

- Интеграция Strands теперь закрывает созданные ею клиенты Hindsight, чтобы не допустить утечки ресурсов и сбоев.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/benfrank241" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@benfrank241</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/2bfd7747" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>2bfd7747</a>

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/strands/v0.1.2)

**Улучшения**

- В пакет интеграции Strands добавлен маркер "py.typed" по PEP 561 для проверки типов Python. ([`d054b884`](https://github.com/vectorize-io/hindsight/commit/d054b884))

**Исправления ошибок**

- Все HTTP-запросы интеграции Strands теперь содержат единый заголовок User-Agent для лучшей совместимости и поиска ошибок. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/strands/v0.1.1)

**Новые возможности**

- Добавлена интеграция Strands Agents SDK: агенты Strands могут работать с инструментами памяти Hindsight. ([`7fe773c0`](https://github.com/vectorize-io/hindsight/commit/7fe773c0))
