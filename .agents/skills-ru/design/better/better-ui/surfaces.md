# Поверхности

Скругления, зрительное выравнивание, тени и обводки изображений.

## Соосные скругления

Если один скруглённый элемент лежит внутри другого, внешний радиус равен внутреннему радиусу плюс отступ между ними:

```
outerRadius = innerRadius + padding
```

Правило полезнее всего для близких вложенных поверхностей. Если отступ больше `24px`, считайте слои отдельными и выбирайте радиус каждого независимо, без строгого расчёта.

### Пример

```css
/* Good: concentric radii */
.card {
  border-radius: 20px; /* 12 + 8 */
  padding: 8px;
}
.card-inner {
  border-radius: 12px;
}

/* Bad: same radius on both */
.card {
  border-radius: 12px;
  padding: 8px;
}
.card-inner {
  border-radius: 12px;
}
```

### Пример с Tailwind

```tsx
// Good: outer radius accounts for padding
<div className="rounded-2xl p-2">       {/* 16px radius, 8px padding */}
  <div className="rounded-lg">          {/* 8px radius = 16 - 8 ✓ */}
    ...
  </div>
</div>

// Bad: same radius on both
<div className="rounded-xl p-2">
  <div className="rounded-xl">          {/* same radius, looks off */}
    ...
  </div>
</div>
```

Разные радиусы у тесно вложенных блоков часто выглядят неуютно. Считайте радиусы по общему центру, если между слоями виден ровный отступ. Сохраняйте переменную компонента, если слои независимы или отступ намеренно неодинаковый.

## Зрительное выравнивание

Если точное выравнивание по геометрии выглядит криво, выровняйте на глаз.

### Кнопки с текстом и значком

Если значок нарушает видимый баланс равных отступов, чуть уменьшите отступ с его стороны. Начальная формула:
`icon-side padding = text-side padding - 2px`.

```css
/* Good: less padding on icon side */
.button-with-icon {
  padding-inline-start: 16px;
  padding-inline-end: 14px; /* trailing icon side = text side - 2px */
}

/* Bad: equal padding looks like icon is pushed too far right */
.button-with-icon {
  padding-inline: 16px;
}
```

```tsx
// Tailwind
<button className="ps-4 pe-3.5 flex items-center gap-2">
  <span>Continue</span>
  <ArrowRightIcon />
</button>
```

### Треугольник кнопки воспроизведения

Значок воспроизведения треугольный: его геометрический центр не совпадает со зрительным. Сдвиньте его чуть вправо:

```css
/* Good: optically centered */
.play-button svg {
  transform: translateX(2px); /* physical correction to the glyph itself */
}

/* Bad: geometrically centered but looks off */
.play-button svg {
  /* no adjustment */
}
```

### Несимметричные значки: звёзды, стрелки, указатели

У некоторых значков зрительный вес распределён неравномерно. Лучше поправить сам SVG, тогда компоненту не нужны лишние поля.

```tsx
// Best: fix in the SVG itself
// Adjust the viewBox or path to visually center the icon

// Fallback: adjust with margin
<span className="translate-x-px">
  <StarIcon />
</span>
```

## Тени вместо рамок

Если рамка **кнопки, карточки или блока** должна показать высоту или глубину, лучше заменить её мягкой `box-shadow`. Тень с прозрачностью подходит к любому фону; сплошная рамка — нет. Это полезно, когда фоном служат изображения или разные цвета.

**К разделителям правило не относится**: `border-b`, `border-t`, боковые линии и рамки, которые отделяют части макета, должны оставаться рамками.

### Тень как рамка в светлой теме

Тень состоит из трёх слоёв. Первый создаёт кольцо шириной 1px, второй слегка приподнимает элемент, третий даёт глубину:

```css
:root {
  --shadow-border:
    0px 0px 0px 1px oklch(0 0 0 / 0.06),
    0px 1px 2px -1px oklch(0 0 0 / 0.06),
    0px 2px 4px 0px oklch(0 0 0 / 0.04);
  --shadow-border-hover:
    0px 0px 0px 1px oklch(0 0 0 / 0.08),
    0px 1px 2px -1px oklch(0 0 0 / 0.08),
    0px 2px 4px 0px oklch(0 0 0 / 0.06);
}
```

### Тень как рамка в тёмной теме

На тёмном фоне глубину нескольких слоёв не видно, поэтому хватит одного белого кольца:

```css
/* Dark mode: adapt to whatever setup the project uses
   (prefers-color-scheme, class, data attribute, etc.) */
--shadow-border: 0 0 0 1px oklch(1 0 0 / 0.08);
--shadow-border-hover: 0 0 0 1px oklch(1 0 0 / 0.13);
```

### Переход при наведении

Примените переменную и добавьте `transition-[box-shadow]` для плавной смены тени:

```css
.card {
  box-shadow: var(--shadow-border);
  transition-property: box-shadow;
  transition-duration: 150ms;
  transition-timing-function: ease-out;
}

.card:hover {
  box-shadow: var(--shadow-border-hover);
}
```

### Где тень, а где рамка

| Берите тень | Берите рамку |
| --- | --- |
| Карточки и блоки с глубиной | Разделители строк списка |
| Кнопки с видом рамки | Границы ячеек таблицы |
| Приподнятые меню и модальные окна | Обводка полей формы для доступности |
| Элементы на разных фонах | Тонкие разделители в плотном интерфейсе |
| Подъём при наведении или фокусе | |

## Обводка изображений

Добавьте изображению тонкую обводку `1px` с малой непрозрачностью. Так глубина выглядит одинаково, особенно если другие элементы системы оформлены рамками или тенями.

### Строгие правила цвета

- **Светлая тема**: чистый чёрный `oklch(0 0 0 / 0.1)`.
- **Тёмная тема**: чистый белый `oklch(1 0 0 / 0.1)`.
- Не берите почти чёрный или почти белый цвет из палитры проекта: slate-900, zinc-900, `#0a0a0a`, `#111827`, `#f5f5f7`. Цветная обводка подхватывает оттенок фона и выглядит как грязь по краю изображения.
- Не подбирайте обводку под акцент или цвет текста проекта. Она нейтрально отделяет изображение, а не задаёт тему.

### Светлая тема

```css
img {
  outline: 1px solid oklch(0 0 0 / 0.1);
  outline-offset: -1px; /* draw the ring just inside the image edge */
}
```

### Тёмная тема

```css
img {
  outline: 1px solid oklch(1 0 0 / 0.1);
  outline-offset: -1px;
}
```

### Tailwind с тёмной темой

```tsx
<img
  className="outline outline-1 -outline-offset-1 outline-black/10 dark:outline-white/10"
  src={src}
  alt={alt}
/>
```

Берите именно `outline-black/10` и `outline-white/10`, а не `outline-slate-*`, `outline-zinc-*`, `outline-neutral-*` или другую цветную шкалу.

**Почему обводка, а не рамка?** `outline` не меняет размеры макета. `outline-offset: -1px` рисует кольцо внутри края изображения, прижимая его к скруглению, а не снаружи.
