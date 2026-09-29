Здесь перечислены частые вредные или лишние правила сохранения в Android и известных библиотеках. Новые средства сборки и библиотеки кладут свои правила для приложений прямо в AAR/JAR, поэтому многие ручные настройки больше не нужны и даже мешают улучшать код.

---

## Общие правила, отключающие работу R8

**Частая ошибка:**

```proguard
-dontshrink
-dontobfuscate
-dontoptimize
```

**Что сделать:** эти правила полностью выключают главные улучшения R8 для всего проекта. Удалите их.

---

## Компоненты Android

Правила для `Activity`, `Fragment`, `ViewModel`, `View`, `Service` и приёмников вещания лишние. AAPT2 и R8 сами сохраняют компоненты из `AndroidManifest.xml` и разметки XML.

**Частая ошибка:**

```proguard
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.view.View
-keepclassmembers class * extends android.app.Fragment { public void *(android.view.View); }
```

**Что сделать:** удалите эти ручные правила: AAPT2 уже учитывает нужные компоненты.

---

## Официальные библиотеки Android и Kotlin

Правила для пакетов AndroidX, Kotlin и Kotlinx лишние: нужные правила уже входят в библиотеки. Ручные правила часто шире, чем нужно.

**Частая ошибка:**

```proguard
-keep class androidx.** { *; }
-keep class kotlinx.** { *; }
-keep class kotlin.** { *; }
```

**Что сделать:** удалите ручные правила и опирайтесь на правила, поставляемые вместе с этими зависимостями.

---

## Gson

### Слишком широкие правила для моделей

Частая ошибка — сохранять целые пакеты моделей данных (POJO/DTO). Для чтения данных вовсе не нужно сохранять все модели целиком:

```proguard
-keep class com.example.app.models.** { *; }
-keep class com.example.app.package.models.* { *; }
```

### Лишние правила для интерфейсов и адаптеров

Эти правила для `TypeAdapter` не нужны: библиотека уже обрабатывает нужные случаи. Они не дают R8 удалить или улучшить свои адаптеры, которые нигде не используются:

```proguard
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
```

### Лишние правила `TypeToken`

Не нужно вручную обходить стирание обобщённых типов: правила Gson уже сохраняют нужный `TypeToken`:

```proguard
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
```

### Внутренние пакеты и примеры

Сохранение внутренней логики библиотеки мешает удалить её неиспользуемый код:

```proguard
-keep class com.google.gson.internal.** { *; }
-keep class com.google.gson.internal.reflect.** { *; }
-keep class com.google.gson.internal.UnsafeAllocator { *; }
-keep class com.google.gson.stream.** { *; }
```

- **Остаётся неиспользуемый код:** R8 не может удалить модели, которые приложение не применяет.
- **Не удаляются лишние методы:** сохраняются все геттеры, сеттеры, `toString()`, `equals()` и `hashCode()`, даже если их не вызывают.
- **Не меняются имена:** обфускация классов блокируется, хотя для Gson с `@SerializedName` это не требуется.

**Что сделать:**

1. Пометьте каждое поле модели данных, которое читает Gson, аннотацией `@SerializedName`, чтобы оно сохранилось после работы R8.
2. Gson **v2.11.0+** содержит свои [правила ProGuard](https://github.com/google/gson/blob/main/gson/src/main/resources/META-INF/proguard/gson.pro). Они сохраняют поля с `@SerializedName`. Если версия старее, перейдите как минимум на 2.11 и удалите ручные правила для классов, которые Gson записывает и читает.

---

## Retrofit

Начиная с 2.9.0 Retrofit поставляет свои правила для приложений. Ручные правила для самой библиотеки и зависящих от неё классов мешают улучшению кода.

### Сохранение всей библиотеки

Это самое вредное правило для Retrofit: оно запрещает удалять код всей библиотеки.

```proguard
-keep class retrofit2.** { *; }
-keep class retrofit2.api.** { *; }
-keep class com.package.example.retrofit.api.** { *; }
```

### Ручные правила для аннотаций

Правила Retrofit сами сохраняют интерфейсы с `@GET`, `@POST`, `@DELETE`, `@PUT`, `@HEAD`, `@OPTIONS` и `@PATCH`. Ручное правило устарело:

```proguard
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }
```

### Лишние правила для ответа сети и адаптеров

Из осторожности разработчики часто сохраняют больше ответов и обёрток сторонних адаптеров, например RxJava, чем нужно:

```proguard
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep class retrofit2.adapter.rxjava2.Result { *; }
```

**Что сделать:** проверьте, что Retrofit не старее 2.9.0. С этой версии он поставляет [свои правила ProGuard](https://github.com/square/retrofit/blob/master/retrofit/src/main/resources/META-INF/proguard/retrofit2.pro), которые находят его HTTP-аннотации и сохраняют нужные сигнатуры методов.

---

## Корутины Kotlin

`kotlinx-coroutines-core` содержит свои правила R8 и уже рассчитан на уменьшение кода.

### Правила для всей библиотеки корутин

Сохранение всего `kotlinx.coroutines` сильно увеличивает размер приложения: внутри много неиспользуемых API.

```proguard
-keepclassmembers class kotlinx.coroutines.** { *; }
```

### Лишнее сохранение внутренних продолжений

Правила библиотеки уже защищают эти низкоуровневые элементы. Ручные правила не дают R8 убрать ненужные продолжения и встроить вызовы:

```proguard
-keepclassmembers class kotlin.coroutines.SafeContinuation { *; }
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
```

### Диспетчеры и обработчики ошибок

При сбоях из-за отсутствующих классов на старых версиях Android иногда добавляют такие правила. В свежей версии корутин нужные случаи уже обработаны либо такой проблемы нет:

```proguard
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
```

**Что сделать:** уберите широкие правила для `kotlinx`. Корутины **v1.7.0+** содержат [нужные правила ProGuard](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro).

---

## Parcelable

**Частая ошибка:** в старых проектах встречается `-keep class * implements android.os.Parcelable { public static final android.os.Parcelable$Creator *; }`.

**Что сделать:**

1. Подключите плагин `kotlin-parcelize`.
2. Замените ручной `writeToParcel` аннотацией `@Parcelize`.
3. Удалите ручные правила для Parcelable: плагин создаёт нужные сам.
4. Стандартный `proguard-android-optimize.txt` уже содержит правила сохранения для классов Parcelable.
5. Лучшее ручное правило — никакое.

---

## База Room

**Частая ошибка:** вручную сохранять интерфейсы DAO или созданные классы `_Impl`.

```proguard
-keep class * extends androidx.room.RoomDatabase
-keep class *_*Impl { *; }
```

**Что сделать:** Room сам создаёт правила ProGuard для своего кода. Ручные правила лишние и мешают R8 улучшать слой доступа к базе. Удалите все ручные правила Room и DAO.

---

## Итог

Если библиотеки обновлены до версий, названных выше, в `proguard-rules.pro` не должно быть ручных правил для этих библиотек.
