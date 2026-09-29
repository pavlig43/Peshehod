---
hide_table_of_contents: true
---

import PageHero from '@site/src/components/PageHero';

<PageHero title="LangGraph — история изменений" subtitle="hindsight-langgraph — интеграция памяти с LangGraph и LangChain." />

← LangGraph — интеграция

## [0.2.0](https://github.com/vectorize-io/hindsight/tree/integrations/langgraph/v0.2.0)

**Несовместимые изменения**

- Из API интеграции LangGraph убрано старое хранилище на базе BaseStore; узлы и инструменты переведены на новый поток инструкций для памяти.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b67e813a" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b67e813a</a>

**Улучшения**

- Исправлены узлы графа и расширены сквозные тесты и тесты потоков для более надёжной работы LangGraph.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b67e813a" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b67e813a</a>

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/langgraph/v0.1.2)

**Улучшения**

- В пакет интеграции Hindsight с LangGraph добавлен маркер py.typed по PEP 561, чтобы средства проверки типов Python работали верно. ([`d054b884`](https://github.com/vectorize-io/hindsight/commit/d054b884))
- Обновлены зависимости интеграции Hindsight с LangGraph для устранения критических и серьёзных уязвимостей. ([`ee4510a7`](https://github.com/vectorize-io/hindsight/commit/ee4510a7))

**Исправления ошибок**

- Все HTTP-запросы интеграции Hindsight с LangGraph теперь содержат единый заголовок User-Agent. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/langgraph/v0.1.1)

**Новые возможности**

- Добавлена интеграция LangGraph с Hindsight. ([`b4320254`](https://github.com/vectorize-io/hindsight/commit/b4320254))
