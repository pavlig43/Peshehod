---
hide_table_of_contents: true
---

import PageHero from '@site/src/components/PageHero';

<PageHero title="Claude Code — история изменений" subtitle="hindsight-memory — плагин памяти Hindsight для Claude Code." />

← Claude Code — интеграция

## [0.7.2](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.7.2)

**Исправления ошибок**

- Исправлено сопоставление каталогов с банками памяти при работе с символьными ссылками.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/365diascollaboration-prog" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@365diascollaboration-prog</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/0672fba27" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>0672fba27</a>
- В Windows сопоставление каталогов с банками памяти теперь не зависит от регистра букв.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/eskild-boop" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@eskild-boop</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/c3bb64764" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>c3bb64764</a>
- Исправлен поиск виртуального окружения в Windows: средство запуска MCP находит Python в каталоге Scripts/.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/eldar702" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@eldar702</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/12851bc7e" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>12851bc7e</a>
- Время ожидания проверки фоновой службы по умолчанию увеличено с 2 до 10 секунд, чтобы реже перезапускать её без нужды.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/21Felix04" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@21Felix04</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/c5a61db2b" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>c5a61db2b</a>

## [0.7.1](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.7.1)

**Исправления ошибок**

- Инструменты знаний теперь включены по умолчанию; сервер MCP продолжает работу, даже если их выключить.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/655435ea" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>655435ea</a>

## [0.7.0](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.7.0)

**Улучшения**

- Интеграция Claude Code по умолчанию извлекает наблюдения, чтобы поиск в памяти лучше работал сразу после установки.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/4b19a0fb" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>4b19a0fb</a>

**Исправления ошибок**

- В контексте памяти текущее время теперь помечено как UTC, чтобы не путать часовые пояса.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/valda" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@valda</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/3d6c2ba8" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>3d6c2ba8</a>
- Начальная настройка MCP в Windows теперь безопасна при повторном запуске скрипта.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/ottopichlhoefer" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@ottopichlhoefer</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/80046797" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>80046797</a>

## [0.6.5](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.6.5)

**Новые возможности**

- В интеграции Claude Code добавлено настраиваемое время ожидания запросов MCP.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/rsaulo" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@rsaulo</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/55ef7067" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>55ef7067</a>

## [0.6.4](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.6.4)

**Исправления ошибок**

- Исправлено имя параметра извлечения памяти в Claude Code: ограничение числа результатов больше не вызывает сбой.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/offendingcommit" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@offendingcommit</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/909a4fd4" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>909a4fd4</a>
- При чтении страниц теперь возвращается их полный текст и обрабатываются слишком большие результаты инструментов.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/cdbartholomew" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@cdbartholomew</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b2a693ab" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b2a693ab</a>
- Инструмент списка страниц знаний агента теперь запрашивает лишь метаданные, чтобы не получать неверные или слишком большие ответы.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/cdbartholomew" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@cdbartholomew</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/6c6ee73c" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>6c6ee73c</a>

## [0.6.3](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.6.3)

**Новые возможности**

- В интеграцию Claude Code добавлено явное сопоставление каталогов с банками памяти и улучшена поддержка рабочих деревьев Git.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/691c65ac" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>691c65ac</a>

## [0.6.2](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.6.2)

**Исправления ошибок**

- При настройке интеграции Claude Code нужные зависимости Python теперь ставятся в отдельное виртуальное окружение в каталоге данных плагина.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/012c100e" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>012c100e</a>

## [0.6.1](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.6.1)

**Новые возможности**

- Навык create-agent в Claude Code теперь учитывает структуру проектов SDA при создании агентов.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/0231094d" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>0231094d</a>

## [0.6.0](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.6.0)

**Новые возможности**

- В Claude Code добавлены инструменты знаний на базе сервера MCP на Python: они позволяют напрямую читать, писать и применять знания Hindsight.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/6c55dbde" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>6c55dbde</a>
- Добавлены подагенты и навык create-agent для создания и настройки агентов и их процессов в Claude Code.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/1b9d6d91" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>1b9d6d91</a>

## [0.4.0](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.4.0)

**Новые возможности**

- Память теперь можно извлекать не лишь из основного, но и из других банков для более полного контекста.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/starbit-biostar" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@starbit-biostar</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/cba2b0d8" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>cba2b0d8</a>

**Улучшения**

- В шаблон retainTags добавлена переменная \{user_id\} для сортировки памяти по пользователям.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/soichisumi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@soichisumi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/9181c9a2" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>9181c9a2</a>

**Исправления ошибок**

- Исправлен разбор записей сеансов, где результаты инструментов содержат списки: извлечение и сохранение памяти больше не срываются.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/b67b6886" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>b67b6886</a>
- При создании ID банков памяти сохраняются исходные байты UTF-8; ID с символами вне ASCII больше не ведут к выбору неверного банка.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/Desko77" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@Desko77</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/08a75b5b" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>08a75b5b</a>
- Сжатие памяти больше не перезаписывает воспоминания, которые нужно было сохранить.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/9c9a5a29" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>9c9a5a29</a>
- При SessionEnd сохраняется память о последних шагах сеанса, даже если retainEveryNTurns больше 1.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/starbit-biostar" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@starbit-biostar</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/aefc1ebc" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>aefc1ebc</a>
- Записи сеансов читаются в UTF-8, чтобы текст с символами вне ASCII не портился и не вызывал ошибок.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/tordf" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@tordf</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/eb9be903" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>eb9be903</a>

## [0.3.1](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.3.1)

**Исправления ошибок**

- Все HTTP-запросы интеграции Claude Code теперь содержат User-Agent для лучшей совместимости и отслеживания. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

## [0.3.0](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.3.0)

**Новые возможности**

- Интеграция Claude Code теперь сохраняет вызовы инструментов как структурированный JSON для более точного хранения и поиска. ([`8cb8b912`](https://github.com/vectorize-io/hindsight/commit/8cb8b912))

## [0.2.0](https://github.com/vectorize-io/hindsight/tree/integrations/claude-code/v0.2.0)

**Новые возможности**

- Добавлен плагин интеграции Claude Code для записи и применения памяти Hindsight. ([`f4390bdc`](https://github.com/vectorize-io/hindsight/commit/f4390bdc))
- Интеграция Claude Code может сохранять целые сеансы с обновлением документа и настраиваемыми тегами. ([`2d31b67d`](https://github.com/vectorize-io/hindsight/commit/2d31b67d))

**Улучшения**

- Упрощены установка и настройка плагина Claude Code. ([`35b2cbb6`](https://github.com/vectorize-io/hindsight/commit/35b2cbb6))
- Интеграции больше не используют жёстко заданные модели по умолчанию: выбор модели можно полностью настроить. ([`58e68f3e`](https://github.com/vectorize-io/hindsight/commit/58e68f3e))
- Claude Code теперь автоматически запускает фоновую службу Hindsight в начале сеанса. ([`26944e25`](https://github.com/vectorize-io/hindsight/commit/26944e25))

**Исправления ошибок**

- Добавлена штатная команда настройки для надёжной регистрации хуков. ([`22ca6a8d`](https://github.com/vectorize-io/hindsight/commit/22ca6a8d))
- Исправлена работа интеграции Claude Code в Windows. ([`a94a90ea`](https://github.com/vectorize-io/hindsight/commit/a94a90ea))
