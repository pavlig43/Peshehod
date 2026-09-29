---
hide_table_of_contents: true
---

# AutoGen — история изменений интеграции

История изменений [`hindsight-autogen`](https://pypi.org/project/hindsight-autogen/).

Исходный код: [`hindsight-integrations/autogen`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/autogen).

← [К общей истории изменений](../index.md)

## [0.1.3](https://github.com/vectorize-io/hindsight/tree/integrations/autogen/v0.1.3)

**Улучшения**

- Интеграция AutoGen по умолчанию использует облачный сервер; добавлены сквозные тесты с отдельным условием запуска, чтобы повысить надёжность и охват проверок.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/0eb40a7c" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>0eb40a7c</a>

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/autogen/v0.1.2)

**Исправления ошибок**

- Во все HTTP-запросы добавлен заголовок User-Agent для более надёжной работы и совместимости с сервисами. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/autogen/v0.1.1)

**Новые возможности**

- Добавлена интеграция Hindsight с агентными процессами на базе AutoGen. ([`a757765a`](https://github.com/vectorize-io/hindsight/commit/a757765a))
