---
hide_table_of_contents: true
---

import PageHero from '@site/src/components/PageHero';

<PageHero title="OpenClaw — история изменений" subtitle="@vectorize-io/hindsight-openclaw — плагин памяти Hindsight для OpenClaw." />

← OpenClaw — интеграция

## [0.8.0](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.8.0)

**Улучшения**

- OpenClaw по умолчанию извлекает лишь наблюдения, чтобы сразу после установки память работала предсказуемее.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/4b19a0fb" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>4b19a0fb</a>
- Во вставленном контексте памяти текущее время теперь помечено как UTC для однозначного чтения.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/dc41f6a5" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>dc41f6a5</a>

**Исправления ошибок**

- При завершении сеанса несохранённые реплики записываются в память, чтобы они не пропали.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/7a4400e0" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>7a4400e0</a>
- Исправлен незаметный пропуск отправки в режиме synthetic-main со статическими банками: память снова обрабатывается как ожидается.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/09c9cecf" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>09c9cecf</a>

## [0.7.7](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.7)

**Новые возможности**

- В начале сохранённых записей сеанса теперь есть блок контекста с важными сведениями о сеансе.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/kryptt" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@kryptt</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/be696b0d" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>be696b0d</a>

**Исправления ошибок**

- Мастер настройки теперь добавляет hindsight-openclaw в список разрешённых плагинов, чтобы интеграция не блокировалась после установки.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/4088af36" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>4088af36</a>

## [0.7.6](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.6)

**Исправления ошибок**

- При повторном запуске мастер настройки сохраняет токен и URL OpenClaw.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/ef600683" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>ef600683</a>

## [0.7.5](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.5)

**Исправления ошибок**

- Мастер настройки теперь верно сохраняет параметр «Разрешить доступ к беседе» для OpenClaw.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/abf84872" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>abf84872</a>

## [0.7.4](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.4)

**Исправления ошибок**

- Исправлена настройка интеграции OpenClaw: переключатель инструментов знаний теперь работает верно.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/c95206e0" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>c95206e0</a>

## [0.7.3](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.3)

**Улучшения**

- Для интеграции OpenClaw добавлен необязательный вывод времени операций при отладке.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/d37940a9" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>d37940a9</a>

**Исправления ошибок**

- Исправлена логика обработки задач и список допустимых настроек, включая retainQueue.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/aca03832" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>aca03832</a>
- Устранена повторная регистрация OpenClaw в нескольких экземплярах API, чтобы обработчики и побочные действия не дублировались.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vernmic" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@vernmic</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/0ce9f333" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>0ce9f333</a>

## [0.7.2](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.2)

*В этом выпуске лишь внутренние правки и изменения инфраструктуры.*

## [0.7.1](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.1)

**Исправления ошибок**

- Исправлено обновление интеграции OpenClaw: взята опубликованная зависимость agent-sdk и обновлена схема настроек плагина.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/c1924e9d" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>c1924e9d</a>

## [0.7.0](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.7.0)

**Новые возможности**

- В интеграцию OpenClaw с Hindsight добавлена начальная поддержка автономных агентов.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/7f30dcc7" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>7f30dcc7</a>

## [0.6.6](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.6)

**Исправления ошибок**

- Память для сеансов агента main:main по умолчанию теперь сохраняется, а не пропускается без сообщения.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/70677457" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>70677457</a>

## [0.6.5](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.5)

**Исправления ошибок**

- При сохранении новой реплики прежние реплики того же сеанса больше не перезаписываются.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/nicoloboschi" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@nicoloboschi</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/1f897314" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>1f897314</a>
- Проверка прямого запуска теперь раскрывает всю цепочку символьных ссылок, чтобы вызов через них работал верно.<span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/D2758695161" target="_blank" rel="noopener noreferrer" style={{color: "var(--ifm-color-primary)", textDecoration: "none", display: "inline-flex", alignItems: "center", gap: "4px", verticalAlign: "middle"}}>@D2758695161</a><span style={{color: "var(--ifm-color-emphasis-500)", margin: "0 0.3em"}}>·</span><a href="https://github.com/vectorize-io/hindsight/commit/7ceaa22a" target="_blank" rel="noopener noreferrer" style={{fontFamily: "var(--ifm-font-family-monospace, monospace)", fontSize: "0.85em", color: "var(--ifm-color-emphasis-600)"}}>7ceaa22a</a>

## [0.6.4](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.4)

**Новые возможности**

- OpenClaw теперь использует ID документа на уровне сеанса и записывает время каждого сообщения в структурированном виде, сохраняя порядок и целостность сеанса. ([`33645e08`](https://github.com/vectorize-io/hindsight/commit/33645e08))

## [0.6.3](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.3)

**Новые возможности**

- Теги сохранения, указанные в тексте, теперь объединяются с тегами по умолчанию без лишней настройки. ([`b79ab2b7`](https://github.com/vectorize-io/hindsight/commit/b79ab2b7))

**Улучшения**

- Все HTTP-запросы теперь содержат User-Agent для определения источника и совместимости с более строгими конечными точками. ([`9372462e`](https://github.com/vectorize-io/hindsight/commit/9372462e))

**Исправления ошибок**

- Разделение банков по агентам теперь учитывает настройку пропуска фильтра идентичности. ([`90a22016`](https://github.com/vectorize-io/hindsight/commit/90a22016))

## [0.6.2](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.2)

**Новые возможности**

- Беседы OpenClaw теперь хранятся в JSON-формате Anthropic с блоками tool_use/tool_result для точного воспроизведения и разбора. ([`adc85129`](https://github.com/vectorize-io/hindsight/commit/adc85129))

**Исправления ошибок**

- ID сеансов стали стабильнее, а служебные реплики без участия пользователя пропускаются. Это делает сеансы цельнее и убирает шум. ([`2ff805d6`](https://github.com/vectorize-io/hindsight/commit/2ff805d6))
- Хуки агента теперь регистрируются при каждом вызове плагина, чтобы они не пропадали время от времени. ([`1be5ff33`](https://github.com/vectorize-io/hindsight/commit/1be5ff33))

## [0.6.1](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.1)

**Исправления ошибок**

- Мастер настройки OpenClaw теперь запрашивает значение токена, а не имя переменной среды. ([`9679d813`](https://github.com/vectorize-io/hindsight/commit/9679d813))

## [0.6.0](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.6.0)

**Несовместимые изменения**

- Настройки теперь берутся из плагина, а не из переменных среды; существующие установки нужно обновить. ([`e22ae05f`](https://github.com/vectorize-io/hindsight/commit/e22ae05f))

**Новые возможности**

- Добавлен интерактивный мастер настройки с режимами Cloud, API и Embedded. ([`87322396`](https://github.com/vectorize-io/hindsight/commit/87322396))
- Добавлен пакет управления жизненным циклом фоновой службы Hindsight в режиме "all". ([`576016f5`](https://github.com/vectorize-io/hindsight/commit/576016f5))
- Добавлена командная строка, которая с учётом настроек загружает старые данные в память Hindsight. ([`72fd3d59`](https://github.com/vectorize-io/hindsight/commit/72fd3d59))
- Добавлена фильтрация сеансов по шаблону: выбранные сеансы можно пропускать или считать не имеющими состояния. ([`5a61ac50`](https://github.com/vectorize-io/hindsight/commit/5a61ac50))
- Для сохранённой памяти добавлены настраиваемые теги. ([`b0e8ac0f`](https://github.com/vectorize-io/hindsight/commit/b0e8ac0f))
- Для статических банков добавлена поддержка bankId. ([`0e81d1a2`](https://github.com/vectorize-io/hindsight/commit/0e81d1a2))

**Улучшения**

- Повышена устойчивость при запуске и дополнены метаданные сохранённой памяти. ([`1f1716bd`](https://github.com/vectorize-io/hindsight/commit/1f1716bd))
- Добавлена очередь сохранения на базе JSONL на случай, когда внешний API недоступен. ([`087545cc`](https://github.com/vectorize-io/hindsight/commit/087545cc))
- Командная строка запускается быстрее: тяжёлая инициализация перенесена на запуск службы. ([`41025c3b`](https://github.com/vectorize-io/hindsight/commit/41025c3b))

**Исправления ошибок**

- Чтобы не ошибаться с маршрутом, ctx.channelId игнорируется, если в нём указано имя поставщика. ([`d4b8b354`](https://github.com/vectorize-io/hindsight/commit/d4b8b354))

## [0.5.1](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.5.1)

**Исправления ошибок**

- Исправлен формат JSON-манифеста плагина OpenClaw, из-за которого возникали ошибки чтения и загрузки. ([`704e41fa`](https://github.com/vectorize-io/hindsight/commit/704e41fa))

## [0.5.0](https://github.com/vectorize-io/hindsight/tree/integrations/openclaw/v0.5.0)

**Несовместимые изменения**

- Из интеграций убраны жёстко заданные модели по умолчанию: модель и поставщика теперь нужно указать явно. ([`58e68f3e`](https://github.com/vectorize-io/hindsight/commit/58e68f3e))

**Новые возможности**

- В интеграцию OpenClaw добавлены настраиваемые структурированные журналы. ([`d441ab81`](https://github.com/vectorize-io/hindsight/commit/d441ab81))
- Добавлен переключатель автоизвлечения памяти и возможность исключить отдельных поставщиков из извлечения и сохранения. ([`3f9eb27c`](https://github.com/vectorize-io/hindsight/commit/3f9eb27c))
- Добавлена настройка пропуска извлечения и сохранения памяти для выбранных поставщиков. ([`fb7be3ec`](https://github.com/vectorize-io/hindsight/commit/fb7be3ec))
- Добавлены динамические банки памяти по каналам для разделения их данных. ([`9a776e9f`](https://github.com/vectorize-io/hindsight/commit/9a776e9f))
- Добавлена поддержка внешнего сервера API Hindsight. ([`6b346925`](https://github.com/vectorize-io/hindsight/commit/6b346925))
- В настройках плагина можно выбрать поставщика и модель языкового ИИ. ([`8564135b`](https://github.com/vectorize-io/hindsight/commit/8564135b))

**Улучшения**

- Можно выбрать место вставки извлечённой памяти, чтобы лучше сохранять кэш запросов. ([`200bab23`](https://github.com/vectorize-io/hindsight/commit/200bab23))
- Улучшены настройки извлечения и сохранения памяти, масштабируемость и поддержка параметров безопасности Gemini. ([`d425e93c`](https://github.com/vectorize-io/hindsight/commit/d425e93c))
- Теперь последние реплики беседы сохраняются раз в 10 ходов по умолчанию, чтобы не терять нить разговора. ([`ad1660b3`](https://github.com/vectorize-io/hindsight/commit/ad1660b3))
- Улучшены параметры OpenClaw и встраивания для более надёжной настройки и работы интеграции. ([`749478d9`](https://github.com/vectorize-io/hindsight/commit/749478d9))
- Улучшены настройка и начальный запуск OpenClaw. ([`27498f99`](https://github.com/vectorize-io/hindsight/commit/27498f99))

**Исправления ошибок**

- Добавлено настраиваемое время ожидания автоизвлечения памяти, чтобы запросы не зависали. ([`cd4d449f`](https://github.com/vectorize-io/hindsight/commit/cd4d449f))
- Извлечённая память теперь вставляется как системный контекст для более надёжной работы. ([`b17f338e`](https://github.com/vectorize-io/hindsight/commit/b17f338e))
- Запросы проверки службы теперь содержат токен, чтобы не получать ошибки доступа. ([`40b02645`](https://github.com/vectorize-io/hindsight/commit/40b02645))
- Улучшены обработка командной оболочки, поддержка HTTP-режима, отложенный повторный запуск и банки памяти по пользователям. ([`c4610130`](https://github.com/vectorize-io/hindsight/commit/c4610130))
- Исправлены сбои при загрузке очень больших данных (E2BIG). ([`6bad6673`](https://github.com/vectorize-io/hindsight/commit/6bad6673))
- Устранена бесконечная рекурсия при сохранении памяти. ([`4f112101`](https://github.com/vectorize-io/hindsight/commit/4f112101))
- Память пользователя больше не стирается при каждом новом сеансе. ([`981cf605`](https://github.com/vectorize-io/hindsight/commit/981cf605))
- Улучшено экранирование аргументов оболочки, чтобы спецсимволы не ломали команды. ([`63e2964a`](https://github.com/vectorize-io/hindsight/commit/63e2964a))
- Исполняемый файл OpenClaw переименован верно, чтобы имя вызова совпадало с настройкой. ([`b364bc34`](https://github.com/vectorize-io/hindsight/commit/b364bc34))
