# Peshehod

Kotlin Multiplatform-приложение для пеших маршрутов. Android запускает общий Compose UI; iOS подключает тот же `Shared` framework. Сервер — отдельная Spring Boot-сборка.

Текущий порядок работ и схема хранения описаны в [PLAN.md](PLAN.md).

## Что нужно

- JDK 21;
- Android SDK 37;
- Docker для PostgreSQL;
- Mac с Xcode 26.4 для iOS.

## Клиент

```powershell
.\gradlew.bat clean check :androidApp:assembleDebug
```

Debug-клиент Android обращается к `http://10.0.2.2:8080`.

## Сервер

```powershell
docker compose up -d
Set-Location server
.\gradlew.bat test
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

Проверка после запуска: `GET http://localhost:8080/actuator/health`.

## iOS

Откройте `iosApp/iosApp.xcodeproj` на Mac. Цели `iosArm64` и `iosSimulatorArm64` требуют Xcode; на Windows они не собираются.
