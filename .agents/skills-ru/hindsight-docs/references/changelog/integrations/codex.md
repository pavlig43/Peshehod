---
hide_table_of_contents: true
---

import PageHero from '@site/src/components/PageHero';

<PageHero title="OpenAI Codex CLI — история изменений" subtitle="Интеграция памяти Hindsight с OpenAI Codex CLI." />

[← Codex CLI — интеграция](https://github.com/vectorize-io/hindsight/tree/main/hindsight-integrations/codex)

## [0.3.0](https://github.com/vectorize-io/hindsight/tree/integrations/codex/v0.3.0)

**Улучшения**

- Для извлечения памяти в Codex добавлено настраиваемое время ожидания, чтобы долгие запросы не зависали.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/voarsh2" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@voarsh2</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/eb76510a" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>eb76510a</a>

**Исправления ошибок**

- Служебные начальные сообщения AGENTS теперь отфильтрованы и не засоряют память и записи сеансов.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/voarsh2" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@voarsh2</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b41e5e36" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b41e5e36</a>
- Исправлены ошибки кодировки PowerShell при работе интеграции Codex в Windows.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/jerviscui" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@jerviscui</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b837e66c" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b837e66c</a>
- Улучшена работа с UTF-8 при чтении записей сеансов и создании ID банков: идентификаторы и текст памяти с символами вне ASCII больше не портятся.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/Desko77" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@Desko77</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/08a75b5b" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>08a75b5b</a>

## [0.2.1](https://github.com/vectorize-io/hindsight/tree/integrations/codex/v0.2.1)

**Улучшения**

- Все HTTP-запросы интеграции Codex теперь содержат заголовок User-Agent для отслеживания и совместимости. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.2.0](https://github.com/vectorize-io/hindsight/tree/integrations/codex/v0.2.0)

**Новые возможности**

- Структурированные вызовы инструментов Codex из файлов выполнения теперь сохраняются в памяти Hindsight. ([`3461398b`](https://github.com/vectorize-io/hindsight/commit/3461398b))

## [0.1.1](https://github.com/vectorize-io/hindsight/tree/integrations/codex/v0.1.1)

**Новые возможности**

- Добавлена интеграция памяти Hindsight с OpenAI Codex CLI: Codex может читать и сохранять воспоминания. ([`0b17a67c`](https://github.com/vectorize-io/hindsight/commit/0b17a67c))

## [0.1.0](https://github.com/vectorize-io/hindsight/tree/integrations/codex/v0.1.0)

**Новые возможности**

- Добавлена интеграция памяти Hindsight с OpenAI Codex CLI и тремя хуками: SessionStart (прогрев фоновой службы), UserPromptSubmit (автоматическое извлечение памяти) и Stop (автоматическое сохранение). ([`0b17a67c`](https://github.com/vectorize-io/hindsight/commit/0b17a67c))
- Сохранение всего сеанса с обновлением записи по ID сеанса, который служит ID документа. ([`0b17a67c`](https://github.com/vectorize-io/hindsight/commit/0b17a67c))
- Динамические ID банков памяти для разделения памяти по проектам. ([`0b17a67c`](https://github.com/vectorize-io/hindsight/commit/0b17a67c))
- Фоновый предварительный запуск и автоматическое управление жизненным циклом службы. ([`0b17a67c`](https://github.com/vectorize-io/hindsight/commit/0b17a67c))
- 57 автотестов обработки текста и сквозной работы хуков. ([`71125cd9`](https://github.com/vectorize-io/hindsight/commit/71125cd9))
