# Быстродействие

Точный выбор свойств для переходов и подсказки браузеру о слоях GPU.

## Меняйте плавно лишь нужные свойства

Не применяйте `transition: all` и класс Tailwind `transition-all`. Всегда указывайте ровно те свойства, которые меняются. Обычный класс Tailwind `transition` охватывает заранее заданный набор цветов, прозрачность, тень и преобразования, а не все свойства; всё же лучше назвать нужные свойства явно.

### Почему

- `transition: all` заставляет браузер следить за переменами всех свойств.
- Из-за него могут неожиданно оживать цвет, поля и тени, которые вы не хотели анимировать.
- Он мешает браузеру ускорять отрисовку.

### Пример CSS

```css
/* Good: only transition what changes */
.button {
  transition-property: scale, background-color;
  transition-duration: 150ms;
  transition-timing-function: ease-out;
}

/* Bad: transition everything */
.button {
  transition: all 150ms ease-out;
}
```

### Tailwind

```tsx
// Good: explicit properties
<button className="transition-[scale,background-color] duration-150 ease-out">

// Bad: transition all
<button className="transition-all duration-150 ease-out">
```

### О классе Tailwind `transition-transform`

В Tailwind он задаёт `transition-property: transform, translate, scale, rotate`. Значит, он охватывает все свойства преобразования, а не только `transform`. Берите его, когда анимируете лишь преобразования. Если нужны ещё и другие свойства, пишите их в скобках: `transition-[scale,opacity,filter]`.

## Применяйте `will-change` редко

`will-change` заранее подсказывает браузеру вынести элемент в отдельный слой GPU. Без этой подсказки слой может появиться лишь при старте анимации, а первый кадр — слегка дёрнуться.

Это особенно полезно для элементов, которые меняют `scale` или `rotation` либо движутся через `transform`. Для других свойств пользы мало: браузер всё равно не сможет сложить их на GPU.

### Правила

```css
/* Good: specific property that benefits from GPU compositing */
.animated-card {
  will-change: transform;
}

/* Good: multiple compositor-friendly properties */
.animated-card {
  will-change: transform, opacity;
}

/* Bad: never use will-change: all */
.animated-card {
  will-change: all;
}

/* Bad: properties that can't be GPU-composited anyway */
.animated-card {
  will-change: background-color, padding;
}
```

### Полезные свойства

| Свойство | Можно собрать на GPU | Есть смысл в `will-change` |
| --- | --- | --- |
| `transform` | Да | Да |
| `opacity` | Да | Да |
| `filter` (размытие, яркость) | Да | Да |
| `clip-path` | Лишь в новых версиях Chromium | Редко: работает не во всех браузерах |
| `top`, `left`, `width`, `height` | Нет | Нет |
| `background`, `border`, `color` | Нет | Нет |

### Когда не применять

Новые браузеры и сами хорошо ускоряют анимацию. Добавляйте `will-change`, лишь если заметили рывок на первом кадре; особенно это может помочь Safari. Не ставьте его на каждый анимируемый элемент заранее: каждый новый слой тратит память.
