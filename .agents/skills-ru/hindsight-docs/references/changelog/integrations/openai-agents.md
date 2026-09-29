---
hide_table_of_contents: true
---

# openai-agents — история изменений интеграции

История изменений [`hindsight-openai-agents`](https://pypi.org/project/hindsight-openai-agents/).

Исходный код: [`hindsight-integrations/openai-agents`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/openai-agents).

← [К общей истории изменений](../index.md)

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/openai-agents/v0.1.2)

**Улучшения**

- Интеграция OpenAI Agents по умолчанию использует облачный сервер; добавлены сквозные тесты с условием запуска и отдельной группой для реальных языковых моделей.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/c01fc12f" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>c01fc12f</a>

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/openai-agents/v0.1.1)

**Улучшения**

- Уточнено требование к версии openai-agents SDK; функция `memory_instructions()` описана в README и справке по API. В README добавлен раздел о работе в продакшене (ошибки, жизненный цикл банка, процессы с несколькими агентами), а в `test_config.py` — тесты значений по умолчанию, настройки, резервного чтения переменных среды и сброса.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/52c148c2" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>52c148c2</a>

## [0.1.0](https://github.com/vectorize-io/hindsight/tree/integrations/openai-agents/v0.1.0)

**Новые возможности**

- Добавлена интеграция с OpenAI Agents SDK для работы ИИ-памяти Hindsight.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b8da88c8" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b8da88c8</a>
