---
hide_table_of_contents: true
---

# Paperclip — история изменений интеграции

История изменений [`@vectorize-io/hindsight-paperclip`](https://www.npmjs.com/package/@vectorize-io/hindsight-paperclip).

Исходный код: [`hindsight-integrations/paperclip`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/paperclip).

← [К общей истории изменений](../index.md)

## [0.2.3](https://github.com/vectorize-io/hindsight/tree/integrations/paperclip/v0.2.3)

**Новые возможности**

- В Paperclip добавлено разделение памяти по пользователям за счёт настраиваемого уровня детализации банка.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/benfrank241" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@benfrank241</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/beca4b42" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>beca4b42</a>

## [0.2.2](https://github.com/vectorize-io/hindsight/tree/integrations/paperclip/v0.2.2)

**Улучшения**

- Обновлены зависимости npm и pip для устранения известных уязвимостей.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/dcbouius" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@dcbouius</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/26c5028c" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>26c5028c</a>

**Исправления ошибок**

- Интеграция Paperclip теперь верно обрабатывает данные реальных событий Paperclip.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/amirhmoradi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@amirhmoradi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/be908d5b" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>be908d5b</a>

## [0.2.1](https://github.com/vectorize-io/hindsight/tree/integrations/paperclip/v0.2.1)

**Несовместимые изменения**

- Старая интеграция заменена новым плагином Paperclip (v0.2.0); изменились упаковка и способ применения.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/benfrank241" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@benfrank241</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/c571fac7" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>c571fac7</a>

## [0.2.0](https://github.com/vectorize-io/hindsight/tree/integrations/paperclip/v0.2.0)

**Несовместимые изменения**

- Интеграция переписана как плагин Paperclip (установка через `pnpm paperclipai plugin install`). Менять код не нужно: хуки памяти запускаются системой событий.
- Работает со всеми типами адаптеров (Claude, Codex, Cursor, HTTP, Process). Раньше требовались ручные вызовы `recall()` и `retain()`, а из адаптеров поддерживался лишь HTTP.

**Новые возможности**

- Хук `agent.run.started`: автоматически извлекает контекст по заголовку и описанию задачи
- Хук `agent.run.finished`: автоматически сохраняет результат агента, используя `runId` как ID документа
- Инструменты агента `hindsight_recall` и `hindsight_retain` дают доступ к памяти во время работы
- `onValidateConfig`: проверка соединения при сохранении настроек оператором
- Настраиваемое разделение банков памяти (компания и агент, лишь компания, лишь агент)

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/paperclip/v0.1.2)

**Улучшения**

- Интеграция Paperclip теперь добавляет User-Agent во все HTTP-запросы для отслеживания и совместимости. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/paperclip/v0.1.1)

**Новые возможности**

- Добавлена интеграция Hindsight с Paperclip на TypeScript. ([`81441ee9`](https://github.com/vectorize-io/hindsight/commit/81441ee9))

**Исправления ошибок**

- Исправлены ошибки в интеграции Paperclip по итогам проверки. ([`7863ffeb`](https://github.com/vectorize-io/hindsight/commit/7863ffeb))
