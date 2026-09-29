Примеры правил сохранения по видам кода. Для каждого указано, что искать в исходниках и какое узкое правило рассмотреть.

### 1. Классы, загружаемые по имени

**Случай:** библиотека или приложение загружает класс по строковому имени.

**Ищите:** `Class.forName("...")`, `getDeclaredConstructor().newInstance()` и интерфейсы для загрузки во время работы.

**Пример:**

```kotlin
val taskClass = Class.forName(className)
val task = taskClass.getDeclaredConstructor().newInstance() as StartupTask
```

**Возможное правило:**

```proguard
-keep class * implements com.example.library.StartupTask {
    <init>();
}
```

### 2. Передача класса через `::class.java`

**Случай:** приложение передаёт ссылку на класс прямо в функцию библиотеки.

**Ищите:** `::class.java` в Kotlin или `.class` в Java в аргументах вызова.

**Пример:**

```kotlin
fun <T> register(clazz: Class<T>) { }
register(MyService::class.java)
```

**Возможное правило:**

```proguard
# Сохраняем сам класс. R8 обычно обрабатывает это сам, но явное правило делает поведение устойчивым.
-keep class com.example.app.MyService {
    <init>();
}
```

### 3. Отражение по аннотациям

**Случай:** свои аннотации помечают методы или классы для вызова через отражение.

**Ищите:** свои определения `@interface` и результат `getDeclaredMethods()`, отобранный по аннотации.

**Пример:**

```kotlin
annotation class ReflectiveExecutor
// Код находит методы с @ReflectiveExecutor и вызывает их.
```

**Возможное правило:**

```proguard
# Сохраняем саму аннотацию.
-keep @interface com.example.library.ReflectiveExecutor

# Сохраняем члены классов с этой аннотацией.
-keepclassmembers class * {
    @com.example.library.ReflectiveExecutor *;
}
```

### 4. Необязательные зависимости

**Случай:** основная библиотека проверяет, есть ли необязательный модуль в списке классов.

**Ищите:** `try-catch` вокруг `Class.forName()`, который включает или выключает функцию.

**Пример:**

```kotlin
private const val VIDEO_TRACKER_CLASS = "com.example.analytics.video.VideoEventTracker"

try {
    Class.forName(VIDEO_TRACKER_CLASS).getDeclaredConstructor().newInstance()
} catch (e: ClassNotFoundException) { /* Пропустить функцию. */ }
```

**Возможное правило:**

```proguard
# Сохраняем необязательный класс, чтобы удаление кода не сломало проверку.
-keep class com.example.analytics.video.VideoEventTracker {
    <init>();
}
```

### 5. Доступ к закрытым членам

**Случай:** отражение читает закрытые поля или методы, не открытые обычным API.

**Ищите:** `getDeclaredField("...")` или `getDeclaredMethod("...")` с последующим `isAccessible = true`.

**Пример:**

```kotlin
val secretField = instance::class.java.getDeclaredField("secretMessage")
secretField.isAccessible = true
```

**Возможное правило:**

```proguard
# Сохраняем лишь нужное закрытое поле или метод по имени и типу.
-keepclassmembers class com.example.LibraryClass {
    private java.lang.String secretMessage;
}
```

### 6. Ручная реализация `Parcelable`

**Случай:** `Parcelable` написан без аннотации `@Parcelize`.

**Ищите:** реализацию `Parcelable` и статическое поле `CREATOR`.

**Пример:**

```kotlin
class MyData : Parcelable {
    // Ручная реализация с полем CREATOR.
}
```

**Возможное правило:** если есть `import kotlinx.parcelize.Parcelize`, R8/ProGuard создаёт правило сам. Для ручной реализации:

```proguard
-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}
```

### 7. Перечисления и обфускация

**Случай:** код косвенно вызывает `Enum.valueOf("STRING_NAME")`, например при чтении JSON, а имена элементов перечисления меняются.

**Ищите:** лишние общие правила для `Enum` в файлах ProGuard.

**Пример лишнего правила:**

```proguard
-keepclassmembers enum * { *; }
```

**Что делать:** стандартный `proguard-android-optimize.txt` уже содержит подходящие правила для перечислений и сохраняет `values()` и `valueOf(String)`. Дополнительное ручное правило не нужно.
