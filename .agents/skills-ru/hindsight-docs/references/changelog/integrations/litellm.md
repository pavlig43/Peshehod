---
hide_table_of_contents: true
---

import PageHero from '@site/src/components/PageHero';

<PageHero title="LiteLLM — история изменений" subtitle="hindsight-litellm — общая интеграция памяти для языковых моделей через LiteLLM." />

← LiteLLM — интеграция

## [0.5.4](https://github.com/vectorize-io/hindsight/tree/integrations/litellm/v0.5.4)

**Исправления ошибок**

- Исправлен режим внедрения в LiteLLM, восстановление состояния менеджера контекста и согласованность проверок и ошибок. Интеграция стала надёжнее.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/dfe74b1d" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>dfe74b1d</a>

## [0.5.3](https://github.com/vectorize-io/hindsight/tree/integrations/litellm/v0.5.3)

*В этом выпуске лишь внутренние правки и изменения инфраструктуры.*

## [0.5.2](https://github.com/vectorize-io/hindsight/tree/integrations/litellm/v0.5.2)

**Исправления ошибок**

- Исправлено сохранение бесед при потоковых ответах LiteLLM.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/ac5181f5" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>ac5181f5</a>

## [0.5.1](https://github.com/vectorize-io/hindsight/tree/integrations/litellm/v0.5.1)

**Улучшения**

- В пакет добавлены сведения о типах для более точной проверки в типизированных проектах Python. ([`d054b884`](https://github.com/vectorize-io/hindsight/commit/d054b884))
- Зависимость LiteLLM обновлена и ограничена по версиям, включая исключение скомпрометированной версии, ради безопасности и устойчивости. ([`8a2388a4`](https://github.com/vectorize-io/hindsight/commit/8a2388a4))

**Исправления ошибок**

- Во все HTTP-запросы добавлен заголовок User-Agent для лучшей совместимости с поставщиками и прокси. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.5.0](https://github.com/vectorize-io/hindsight/tree/integrations/litellm/v0.5.0)

**Новые возможности**

- Добавлена поддержка потоковой передачи в интеграции через обёртку LiteLLM. ([`665877bb`](https://github.com/vectorize-io/hindsight/commit/665877bb))
- Добавлена асинхронная поддержка retain и reflect; API интеграции LiteLLM упрощён. ([`1d4879a2`](https://github.com/vectorize-io/hindsight/commit/1d4879a2))
- Первый выпуск интеграции Hindsight с LiteLLM. ([`dfccbf29`](https://github.com/vectorize-io/hindsight/commit/dfccbf29))

**Улучшения**

- Через интеграцию LiteLLM теперь можно передавать теги и метаданные задачи, чтобы лучше упорядочивать и искать воспоминания. ([`f3c5a9c1`](https://github.com/vectorize-io/hindsight/commit/f3c5a9c1))

**Исправления ошибок**

- Если запрос к Hindsight не задан, интеграция берёт последнее сообщение пользователя. Это исключает пустой или пропущенный поиск в памяти. ([`5e8952c5`](https://github.com/vectorize-io/hindsight/commit/5e8952c5))
- Исправлена передача заданного api_key клиенту Hindsight в интеграции LiteLLM. ([`c0ca9b02`](https://github.com/vectorize-io/hindsight/commit/c0ca9b02))
