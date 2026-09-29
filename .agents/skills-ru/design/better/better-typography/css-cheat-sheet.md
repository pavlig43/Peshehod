# Краткий справочник CSS

По одной строке для каждого свойства CSS, связанного со шрифтами в этом навыке, и его пары в Tailwind 4. Если готового класса нет, показана запись с произвольным значением. Выберите столбец по проекту: свойства CSS для чистого CSS, CSS Modules, styled-components или StyleX; классы — для Tailwind.

## Шрифт

| Свойство | Что делает | Tailwind |
| --- | --- | --- |
| `font-family: sans-serif` | Шрифт без засечек | `font-sans` |
| `font-family: serif` | Шрифт с засечками | `font-serif` |
| `font-family: monospace` | Моноширинный шрифт | `font-mono` |
| `font-size` | Размер из шкалы шрифтов | `text-*` |
| `font-weight` | Вес от 1 до 1000 | `font-*` |
| `font-style: italic` | Курсив | `italic` |
| `-webkit-font-smoothing` + `-moz-osx-font-smoothing` | Сглаживание шрифта на macOS; задайте один раз на корне | `antialiased` |
| `font-synthesis: none` | Запрет искусственных начертаний после проверки запасных шрифтов и выделений | `[font-synthesis:none]` |
| `font-feature-settings` | Включает и выключает функции OpenType | `[font-feature-settings:"ss01"]` |
| `font-variation-settings` | Меняет оси переменного шрифта | `[font-variation-settings:"GRAD"_80]` |
| `font-optical-sizing` | Подстраивает детали под размер | `[font-optical-sizing:auto]` |
| `font-variant-caps` | Настоящие капители | `[font-variant-caps:small-caps]` |
| `font-variant-position` | Настоящие верхние и нижние индексы | `[font-variant-position:super]` |
| `font-variant-numeric: tabular-nums` | Цифры равной ширины | `tabular-nums` |
| `font-variant-numeric: slashed-zero` | Помогает отличить 0 от O | `slashed-zero` |

## Интервалы и компоновка

| Свойство | Что делает | Tailwind |
| --- | --- | --- |
| `letter-spacing` | Расстояние между буквами | `tracking-*` |
| `line-height` | Расстояние между строками | `leading-*` |
| `font-kerning` | Включает и выключает кернинг | `[font-kerning:none]` |
| `text-box: trim-both` | Убирает лишнее место сверху и снизу текста | `[text-box:trim-both_cap_alphabetic]` |
| `max-width` для столбцов текста | Ограничивает строку примерно 60–75 знаками | `max-w-xl` / `max-w-2xl` / `max-w-[65ch]` |
| `text-align` | Откуда начинаются и где кончаются строки | `text-start` / `text-center` |

## Перенос и выход за края

| Свойство | Что делает | Tailwind |
| --- | --- | --- |
| `text-wrap: balance` | Равномерно делит заголовок на строки | `text-balance` |
| `text-wrap: pretty` | Не оставляет одно слово в конце | `text-pretty` |
| `text-overflow: ellipsis` | Ставит многоточие у обрезанного текста | `truncate` |
| `line-clamp` | Обрезает текст после N строк | `line-clamp-*` |
| `overflow-wrap: break-word` | Переносит длинные строки | `break-words` |
| `white-space: nowrap` | Отключает перенос | `whitespace-nowrap` |
| `text-transform` | Меняет вид регистра | `uppercase` / `capitalize` |

## Украшения и работа с текстом

| Свойство | Что делает | Tailwind |
| --- | --- | --- |
| `text-decoration-line: underline` | Подчёркивает текст | `underline` |
| `text-decoration-color` | Цвет подчёркивания | `decoration-*` |
| `text-decoration-thickness` | Толщина подчёркивания | `decoration-1` / `decoration-2` |
| `text-underline-offset` | Сдвигает линию вниз | `underline-offset-*` |
| `text-underline-position: from-font` | Положение линии по данным шрифта | `[text-underline-position:from-font]` |
| `text-decoration-style` | Точки, штрихи или волна | `decoration-dotted` / `decoration-wavy` |
| `text-decoration-thickness: from-font` | Толщина линии по данным шрифта | `decoration-from-font` |
| `text-decoration-skip-ink` | Разрывы линии у нижних выносных частей букв | `[text-decoration-skip-ink:auto]` |
| `caret-color` | Цвет текстового курсора | `caret-*` |
| `user-select: none` | Запрещает выделение лишь при проверенном конфликте с перетаскиванием или жестом | `select-none` |
| `text-shadow` | Тень букв | `text-shadow-*` |
| `-webkit-text-stroke` | Обводка букв | `[-webkit-text-stroke:1px_black]` |
| `background-clip: text` | Обрезает фон по контурам букв | `bg-clip-text` |
| `initial-letter` | Размер буквицы | `[initial-letter:3]` |
