---
name: metro-di
description: Применяйте для создания, проверки и переработки внедрения зависимостей Metro в Logica: графов платформ, объединения MiniApp и расширений графа сеанса.
---

# Metro DI в Logica

Используйте навык при правках аннотаций Metro, состава графа, областей действия и создаваемых фабрик графа. Сверяйтесь с установленной версией Metro и кодом репозитория, а не со старыми примерами эпохи Tetris.

## Текущая схема

- Версия Metro указана в `gradle/libs.versions.toml` (сейчас `1.4.2`).
- `composeApp/src/androidMain/.../AndroidAppGraph.kt` и `composeApp/src/iosMain/.../NativeAppGraph.kt` — конечные графы платформ.
- Общие зависимости приложения используют `AppScope`; конечные графы явно перечисляют свои контейнеры привязок.
- Модуль `:miniapp:metro` владеет `MiniAppMetroBindings`, неизменяемым реестром, наборами привязок плагинов и ожиданий, допускающими пустое значение, `MiniAppSessionScope` и обёрткой сеанса, удерживающей граф.
- Каждый MiniApp добавляет один `MiniAppPlugin` в набор `AppScope`.
- Каждый активный плагин создаёт одно расширение `@GraphExtension(MiniAppSessionScope::class)` через фабрику с уникальным именем.
- Входные данные сеанса поступают одним параметром фабрики `@Provides MiniAppSessionContext`. `MiniAppSessionContextBindings` раскрывает его как контракты `ComponentContext`, видимости, хоста, хранения и звука.
- Провайдеры, принадлежащие игре, находятся в её `@BindingContainer`, который явно включён в `@GraphExtension(bindingContainers = [...])` игры. Не добавляйте контейнеры отдельной игры в общий `MiniAppSessionScope`: соседние игры не должны делить одно пространство объединения.

## Правила

1. Не вводите Metro в `core/domain` и `:miniapp:api`.
2. Привязки нативного SDK держите в наборах исходников платформы и графах приложения.
3. Применяйте `@SingleIn(AppScope::class)` лишь к объектам на время жизни приложения, а `@SingleIn(MiniAppSessionScope::class)` — к изменяемым компонентам и редьюсерам одного сеанса.
4. У каждого объявления `@Provides` явно указывайте тип результата.
5. Давайте методам `@GraphExtension.Factory` уникальные имена на основе полного ID MiniApp, например `createGameSnakeSessionGraph`. Kotlin не может объединить фабрики соседних игр, которые различаются лишь типом результата.
6. Открывайте конкретный тип сеанса из каждого дочернего графа. Не привязывайте все игры к одному ключу `MiniAppSession` с квалификаторами.
7. Возвращайте из `MiniAppPlugin.createSession` обёртку сеанса, которая держит граф; иначе дочерний граф может быть удалён сборщиком мусора, пока сеанс ещё виден.
8. Ресурсы времени работы привязывайте через `MiniAppSessionContext`. Игра не должна напрямую запрашивать хост, Settings, платформенный звук или нативную рекламу.
9. Для простых конкретных классов предпочитайте внедрение через конструктор, а для фабрик и псевдонимов — небольшие явные контейнеры привязок. Не создавайте методы-провайдеры, которые лишь скрывают простой конструктор и не проясняют владельца зависимости.
10. Берите `@GraphPrivate` лишь когда привязку нужно скрыть от дочерних графов. Если пустой общий набор допустим, берите `@Multibinds(allowEmpty = true)`.

## Схема графа MiniApp

```kotlin
@BindingContainer
abstract class SnakeSessionBindings {
    companion object {
        @Provides
        @SingleIn(MiniAppSessionScope::class)
        fun provideSession(component: SnakeComponent): SnakeSession {
            return SnakeSession(component)
        }
    }
}

@GraphExtension(
    scope = MiniAppSessionScope::class,
    bindingContainers = [SnakeSessionBindings::class],
)
interface SnakeSessionGraph {
    val session: SnakeSession

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createGameSnakeSessionGraph(
            @Provides context: MiniAppSessionContext,
        ): SnakeSessionGraph
    }
}
```

Для очень маленького MiniApp допустимы и провайдеры прямо в графе. Выделяйте контейнер привязок, если так яснее, кому они принадлежат, либо если провайдеров несколько.

## Как проверять правку

1. Через схему кода найдите граф, фабрику, вклад в граф и тех, кто использует зависимости.
2. Прочитайте `build.gradle.kts` нужного модуля. Если меняется плагин или связь модулей, прочитайте также `settings.gradle.kts` и `gradle/libs.versions.toml`.
3. Проверьте совместимость областей действия и решите, кому принадлежат привязки: графу приложения или графу одного сеанса MiniApp.
4. Соберите конечные графы Android и iOS: ошибки Metro на границах модулей часто видны лишь при их создании.
5. Для MiniApp запустите `allTests`, `validateMiniAppDependencies`, `compileAndroidMain` и `compileKotlinIosSimulatorArm64` нужной игры. При правках объединения для выпуска запустите `:miniapp:bundle:verifyMiniAppBundle`.

Отклоняйте правки, которые помещают привязки сеанса одной игры в общий глобальный вклад, различают соседние сеансы строковыми квалификаторами, создают графы из интерфейса Compose или добавляют ещё одну область действия поверх `MiniAppSessionScope` без нового настоящего жизненного цикла.
