# Схема гибкого интерфейса Compose Multiplatform

## Содержание

1. Цель.
2. Модель от размера окна.
3. Границы размеров.
4. Общее состояние разметки.
5. Подстройка всего приложения и вложенных элементов.
6. Сохранение состояния.
7. Неверные схемы.

## 1. Цель

Сделайте одну общую схему интерфейса, которая подстраивается под окно приложения, а не под вид устройства. Применяйте её для Android, компьютера и iOS, а на Android добавляйте лишь нужные сведения о складке, нескольких окнах и внешнем экране.

## 2. Сначала размер окна

Не выбирайте разметку по проверке «телефон, планшет или складное устройство». Смотрите доступное место во время работы.

Предлагаемые уровни:

- `WindowInfoProvider` на платформе возвращает ширину и высоту окна в dp и, при нужде, положение складки.
- Общий `AdaptivePolicy` переводит эти данные в классы ширины и высоты и правила разметки.
- Общий `AppAdaptiveState` — неизменяемое состояние для composable-функций.

## 3. Границы размеров

Классы ширины:

- Узкий: `< 600dp`.
- Средний: `600dp <= width < 840dp`.
- Широкий: `840dp <= width < 1200dp`.
- Большой: `1200dp <= width < 1600dp`.
- Очень большой: `>= 1600dp`.

Классы высоты:

- Малый: `< 480dp`.
- Средний: `480dp <= height < 900dp`.
- Большой: `>= 900dp`.

Ширина определяет большинство решений; малая высота защищает от неудобных двух панелей в низком окне, например на повернутом телефоне или раскладушке.

## 4. Общее состояние разметки

Пример общей модели:

```kotlin
enum class WidthClass { Compact, Medium, Expanded, Large, ExtraLarge }
enum class HeightClass { Compact, Medium, Expanded }

enum class NavChrome { BottomBar, Rail, Drawer }
enum class ContentLayout { SinglePane, ListDetail, SupportingPane, FeedGrid }

data class AppAdaptiveState(
    val widthClass: WidthClass,
    val heightClass: HeightClass,
    val navChrome: NavChrome,
    val contentLayout: ContentLayout,
    val preferTwoPane: Boolean,
    val maxContentWidthDp: Int,
)
```

Пример правил:

```kotlin
fun buildAdaptiveState(widthDp: Int, heightDp: Int): AppAdaptiveState {
    val widthClass = when {
        widthDp >= 1600 -> WidthClass.ExtraLarge
        widthDp >= 1200 -> WidthClass.Large
        widthDp >= 840 -> WidthClass.Expanded
        widthDp >= 600 -> WidthClass.Medium
        else -> WidthClass.Compact
    }
    val heightClass = when {
        heightDp >= 900 -> HeightClass.Expanded
        heightDp >= 480 -> HeightClass.Medium
        else -> HeightClass.Compact
    }

    val navChrome = when (widthClass) {
        WidthClass.Compact -> NavChrome.BottomBar
        WidthClass.Medium, WidthClass.Expanded -> NavChrome.Rail
        WidthClass.Large, WidthClass.ExtraLarge -> NavChrome.Drawer
    }

    val preferTwoPane = widthClass >= WidthClass.Medium && heightClass != HeightClass.Compact

    return AppAdaptiveState(
        widthClass = widthClass,
        heightClass = heightClass,
        navChrome = navChrome,
        contentLayout = if (preferTwoPane) ContentLayout.ListDetail else ContentLayout.SinglePane,
        preferTwoPane = preferTwoPane,
        maxContentWidthDp = if (widthClass >= WidthClass.Expanded) 1200 else Int.MAX_VALUE,
    )
}
```

## 5. Где подстраивать разметку

Для устройства всего приложения или экрана берите классы размера окна: вид навигации, число панелей и набор содержимого маршрута.

Для вложенных элементов повторного применения берите местные ограничения:

- `BoxWithConstraints` — когда показываете разные виды содержимого;
- свой `Layout` или модификаторы — для гибкого размещения;
- не читайте общий размер окна глубоко во вложенных простых элементах.

## 6. Как сохранить состояние

Держите достаточно состояния и данных для любого вида разметки.

Нужно:

- поднимать вверх выбранный элемент списка, состояние выдвижной панели, место прокрутки и ввод пользователя;
- хранить выбранный элемент деталей и при скрытой панели в узком окне;
- сохранять воспроизведение и долгую работу при смене размера и настроек.

Не нужно:

- загружать дополнительные данные лишь после перехода к широкой разметке;
- связывать сетевые побочные действия со сменой класса ширины.

## 7. Неверные схемы

- Списки разрешённых устройств, например «большой экран лишь на известных планшетах».
- Выбор разметки по физическому экрану или устаревшим API Display.
- `fillMaxWidth` повсюду без предела ширины, хотя строки становятся слишком длинными.
- Фиксированный поворот или соотношение сторон для окна камеры и медиасодержимого.
- Длинные формы без прокрутки в окнах малой высоты.
