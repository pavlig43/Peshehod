---
hide_table_of_contents: true
---

# OpenCode — история изменений интеграции

История изменений [`@vectorize-io/opencode-hindsight`](https://www.npmjs.com/package/@vectorize-io/opencode-hindsight).

Исходный код: [`hindsight-integrations/opencode`](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/opencode).

← [К общей истории изменений](../index.md)

## [0.2.6](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.6)

**Новые возможности**

- Для области наблюдений добавлено значение "shared", чтобы объединять наблюдения.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/0a046f97" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>0a046f97</a>

**Исправления ошибок**

- Из точки входа плагина OpenCode убраны служебные экспорты для совместимости со старыми загрузчиками плагинов.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/mdbenito" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@mdbenito</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/9500a6bf" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>9500a6bf</a>

## [0.2.5](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.5)

**Исправления ошибок**

- Инструкции по извлечению памяти теперь входят в начальный системный запрос, а не добавляются отдельным разделом.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/421cde6de" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>421cde6de</a>

## [0.2.4](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.4)

**Исправления ошибок**

- Исправлен вывод журналов интеграции OpenCode.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/102416c42" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>102416c42</a>

## [0.2.3](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.3)

**Исправления ошибок**

- Журналы интеграции OpenCode теперь показывают при отладке лишь сведения о настройках, записывают итоговый адрес сервера и яснее выводят ошибки.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/796a9eff9" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>796a9eff9</a>

## [0.2.2](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.2)

**Исправления ошибок**

- Исправлен экспорт плагина OpenCode: точка входа больше не выдаёт значение, которое не является функцией.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/r266-tech" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@r266-tech</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/e68d32583" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>e68d32583</a>

## [0.2.1](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.1)

**Исправления ошибок**

- Интеграция OpenCode по умолчанию использует Hindsight Cloud; для сквозных тестов с реальным сервисом требуется отдельное условие запуска.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/06f36b8b" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>06f36b8b</a>

## [0.2.0](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.2.0)

**Новые возможности**

- Несколько рабочих деревьев Git одного репозитория могут делить общий банк памяти.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/isac322" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@isac322</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/78e48e59" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>78e48e59</a>

**Улучшения**

- Изменена частота сохранения памяти по умолчанию (retainEveryNTurns: 3 вместо 10).<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/DK09876" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@DK09876</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/902704df" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>902704df</a>

**Исправления ошибок**

- При динамическом создании ID банка сохраняются исходные байты UTF-8, поэтому ID не портится.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/Desko77" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@Desko77</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/08a75b5b" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>08a75b5b</a>

## [0.1.4](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.1.4)

**Улучшения**

- Ошибки интеграции OpenCode теперь пишутся в журнал лишь при отладке, чтобы убрать лишний шум. ([`33442f19`](https://github.com/vectorize-io/hindsight/commit/33442f19))

## [0.1.3](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.1.3)

**Исправления ошибок**

- Интеграция OpenCode теперь верно разбирает сообщения, не путает общее состояние и сохраняет текст после сжатия. ([`6076354a`](https://github.com/vectorize-io/hindsight/commit/6076354a))

## [0.1.2](https://github.com/vectorize-io/hindsight/tree/integrations/opencode/v0.1.2)

**Новые возможности**

- Добавлены настройки фильтрации извлечённой памяти по тегам (recallTags) и правил их совпадения (recallTagsMatch). ([`b57e337f`](https://github.com/vectorize-io/hindsight/commit/b57e337f))

**Исправления ошибок**

- API сообщений сеанса теперь возвращает данные в формате, нужном плагину OpenCode. ([`fd87de9c`](https://github.com/vectorize-io/hindsight/commit/fd87de9c))
