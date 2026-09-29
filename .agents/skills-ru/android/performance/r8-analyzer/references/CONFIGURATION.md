Чтобы R8 работал на полную, настройте проект с учётом языка скриптов сборки: Kotlin DSL или Groovy DSL.

## 1. Модули приложения (`com.android.application`)

В `build.gradle` или `build.gradle.kts` приложения для типа сборки `release` либо своего типа для выпуска и проверки скорости должны быть включены уменьшение и обфускация кода, а также удаление лишних ресурсов. Обязательно берите оптимизированный стандартный файл `proguard-android-optimize.txt`.

**Kotlin DSL (`build.gradle.kts`):**

```kotlin
buildTypes {
   getByName("release") {
       isMinifyEnabled = true
       isShrinkResources = true
       proguardFiles(
           getDefaultProguardFile("proguard-android-optimize.txt"),
           "proguard-rules.pro"
       )
   }
}
```

**Groovy DSL (`build.gradle`):**

```groovy
buildTypes {
   release {
       minifyEnabled = true
       shrinkResources = true
       proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
   }
}
```

## 2. Параметры `gradle.properties`

**Полный режим:** открывает весь набор улучшений R8.

- **AGP 8.0+:** включён по умолчанию. Проверьте, что строки `android.enableR8.fullMode=false` **нет**.
- **До AGP 8.0:** включите явно через `android.enableR8.fullMode=true`.

**Улучшенное удаление ресурсов:** если версия AGP проекта выше 8.6, но ниже 9.0, явно включите новый способ удаления лишних ресурсов:

```properties
android.r8.optimizedResourceShrinking=true
```
