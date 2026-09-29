Чтобы приложение было как можно меньше и быстрее, включите его оптимизацию. R8 убирает неиспользуемый код и ресурсы, переписывает код ради скорости работы и делает другое. Для пользователя это значит:

- более быстрый запуск;
- меньший расход памяти;
- более быстрое рисование и работа приложения;
- меньше [зависаний ANR](https://developer.android.com/topic/performance/anrs/keep-your-app-responsive).

> [!IMPORTANT]
> **Важно:** всегда включайте оптимизацию для выпуска приложения, но для тестов и библиотек она, скорее всего, не нужна. Подробнее: [проверка и поиск сбоев оптимизации](https://developer.android.com/topic/performance/app-optimization/test-and-troubleshoot-the-optimization) и [оптимизация для авторов библиотек](https://developer.android.com/topic/performance/app-optimization/library-optimization).

## Как R8 улучшает приложение

R8 проходит несколько этапов, уменьшая размер и ускоряя работу:

- **Удаление лишнего кода:** R8 находит и убирает недостижимый код приложения и библиотек. Он строит граф ссылок от точек входа, например `Activity` и `Service` из манифеста, и удаляет всё, на что не осталось ссылок.
- **Улучшение логики исполнения:** R8 переписывает код, чтобы он работал быстрее с меньшими накладными затратами. В частности:
  - **Встраивание методов:** вместо вызова метода ставит его тело. Это убирает цену вызова и открывает путь к новым улучшениям.
  - **Слияние классов:** объединяет наборы классов и интерфейсов в один класс. В приложении становится меньше классов, ниже расход памяти и быстрее запуск.
- **Обфускация и сокращение имён:** для уменьшения файла DEX R8 сокращает имена классов, полей и методов, например `com.example.MyActivity` может стать `a.b.a`.

Начиная с Android Gradle Plugin (AGP) 8.12.0, R8 улучшает и ресурсы. Подробнее — в разделе [об улучшенном удалении ресурсов](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization#optimize-resource-shrinking).

## Как включить

В файле сборки модуля приложения для [выпуска](https://developer.android.com/studio/publish/preparing#turn-off-debugging) поставьте `isMinifyEnabled = true` для кода и `isShrinkResources = true` для ресурсов. Рекомендуется включать оба параметра. Включайте улучшения лишь в конечной сборке, которую проверяете перед публикацией, обычно `release`: сборка займёт больше времени, а из-за изменений кода поиск ошибок станет сложнее.

### Kotlin

```kotlin
android {
    buildTypes {
        release {
            // Включает улучшение кода приложения.
            isMinifyEnabled = true

            // Включает удаление лишних ресурсов.
            isShrinkResources = true

            proguardFiles(
                // Стандартный файл с автоматически заданными правилами.
                getDefaultProguardFile("proguard-android-optimize.txt"),

                ...
            )
            ...
        }
    }
    ...
}
```

### Groovy

```groovy
android {
    buildTypes {
        release {
            // Включает улучшение кода приложения.
            minifyEnabled = true

            // Включает удаление лишних ресурсов.
            shrinkResources = true

            // Стандартный файл с автоматически заданными правилами.
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')

            ...
        }
    }
}
```

## Улучшенное удаление ресурсов

В AGP 8.12.0 появился новый способ удаления ресурсов: он связывает разбор ресурсов и кода, чтобы приложение стало ещё меньше и быстрее.

Раньше Android Asset Packaging Tool (AAPT2) создавал правила сохранения, которые по сути отделяли ресурсы от кода. Поэтому иногда оставались недоступный код и ресурсы, которые ссылались друг на друга.

Теперь ресурсы участвуют в общем графе ссылок как часть программы. Если на группу кода и ресурсов нет нужных ссылок и она не защищена правилом, её можно удалить.

### Как включить улучшенный способ

В AGP до 9.0.0 добавьте в `gradle.properties`:

```properties
android.r8.optimizedResourceShrinking=true
```

В AGP 9.0.0 и новее строка не нужна: улучшенный способ включается сам, когда в сборке задано `isShrinkResources = true`.

## Как проверить и настроить R8

Чтобы R8 работал [в полном режиме](https://developer.android.com/topic/performance/app-optimization/full-mode), удалите из `gradle.properties` эту строку, если она есть:

```properties
android.enableR8.fullMode=false # Удалите строку из проекта.
```

После включения улучшений стеки ошибок могут стать непонятными, особенно если R8 переименует классы и методы. Как сопоставить стек с исходниками, см. [восстановление исходного стека](https://developer.android.com/topic/performance/app-optimization/test-and-troubleshoot-the-optimization#recover-original-stack-trace).

Если R8 включён, для более быстрого запуска также [создайте Startup Profiles](https://developer.android.com/topic/performance/baselineprofiles/dex-layout-optimizations).

Если после включения возникли ошибки:

- [Добавьте узкие правила сохранения](https://developer.android.com/topic/performance/app-optimization/add-keep-rules) для кода, который нельзя менять.
- [Вводите улучшения по частям](https://developer.android.com/topic/performance/app-optimization/adopt-optimizations-incrementally).
- Обновите код и [выбирайте библиотеки, лучше подходящие для оптимизации](https://developer.android.com/topic/performance/app-optimization/choose-libraries-wisely).

> [!CAUTION]
> **Осторожно:** средства, которые заменяют или меняют результат R8, могут замедлить приложение. R8 тщательно создаёт и проверяет улучшения кода, [размещения DEX](https://developer.android.com/topic/performance/baselineprofiles/dex-layout-optimizations) и Baseline Profiles. Другие средства, создающие или меняющие DEX, могут сломать эти улучшения.

Если хотите ускорить сборку, см. [как настроить работу R8](https://developer.android.com/build/r8-execution-profiles) под вашу среду.

## Что менялось в версиях AGP и R8

| Версия AGP | Новые возможности |
|---|---|
| 9.1 | **Перемещение классов по умолчанию:** R8 переносит классы в пакет без имени верхнего уровня для дальнейшего уменьшения DEX. Параметр `-repackageclasses` больше не нужен. О работе и отключении см. [общие параметры](https://developer.android.com/topic/performance/app-optimization/global-options#global-options). |
| 9.0 | **Улучшенное удаление ресурсов:** включено по умолчанию, управляется `android.r8.optimizedResourceShrinking`. [Общий разбор кода и ресурсов](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization#optimize-resource-shrinking) удаляет ресурсы, на которые ссылается лишь неиспользуемый код. Для приложений с большим общим кодом и ресурсами под разные виды устройств встречается уменьшение размера более чем на 50 %. Меньший файл быстрее загружается и ставится; запуск и рисование ускоряются, зависаний ANR меньше. **Отбор правил библиотек:** общие параметры вроде `-dontobfuscate` в правилах библиотек больше не поддерживаются, приложение их отсеивает; см. [общие параметры](https://developer.android.com/topic/performance/app-optimization/global-options). **Проверки `null` в Kotlin:** улучшаются по умолчанию через `-processkotlinnullchecks`. Также заметно ускорена сборка. **Отдельные пакеты:** экспериментальный `packageScope` позволяет [улучшать выбранные пакеты](https://developer.android.com/topic/performance/app-optimization/optimize-specified-packages). **Оптимизация по умолчанию:** `getDefaultProguardFile("proguard-android.txt")` больше не поддерживается, так как содержит нежелательный `-dontoptimize`; берите `proguard-android-optimize.txt`. Чтобы выключить улучшения всего приложения, [вручную добавьте параметр в файл ProGuard](https://developer.android.com/topic/performance/app-optimization/global-options#global-options-2). |
| 8.12 | **Удаление ресурсов:** появилась первая поддержка; по умолчанию выключена и включается через `isShrinkResources`. Вместе с R8 находит и удаляет лишние ресурсы. **Восстановление имён в Logcat:** окно [Logcat](https://developer.android.com/studio/debug/logcat) в Android Studio умеет автоматически показывать исходные имена. |
| 8.6 | **Лучшее восстановление имён:** имена файлов и номера строк восстанавливаются по умолчанию для всех `minSdk`; раньше, в 8.2, требовался `minSdk` 26+. Обновление R8 помогает читать стеки из сборок со сменёнными именами и даёт Android Studio Logcat более точное сопоставление с исходниками. |
| 8.0 | **Полный режим по умолчанию:** [полный режим R8](https://developer.android.com/topic/performance/app-optimization/full-mode) даёт более сильные улучшения и включён сразу. Отключается через `android.enableR8.fullMode=false`. |
| 7.0 | **Появился полный режим:** включается по желанию через `android.enableR8.fullMode=true`. Он делает более строгие допущения об отражении и других возможностях, которые выбираются во время работы. Размер может стать меньше, а работа быстрее, но иногда нужны дополнительные правила сохранения нужного кода. |
