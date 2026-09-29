# План проекта Peshehod

## Цель первого этапа

Собрать основу приложения для пеших маршрутов:

- Android-клиент с общим Compose UI;
- iOS-цели для будущей сборки на Mac;
- сервер Spring Boot;
- PostgreSQL и сервер приложения на одном хосте через Docker Compose;
- отдельные постоянные тома для базы и фото.

На первом этапе нужны тема, корневая навигация, карта, связь клиента с сервером и рабочая схема хранения. Аккаунты, публикация маршрутов и фоновая запись GPS пойдут после основы.

## Схема запуска

```text
Android / iOS
      |
      | HTTP API
      v
server (Spring Boot, контейнер)
      |                    |
      v                    v
PostgreSQL             том с фото
peshehod-postgres-data peshehod-photo-data
```

В разработке всё работает на одном ПК. После выпуска та же схема переносится на один Linux-сервер без смены кода.

## Структура проекта

```text
Peshehod/
├─ androidApp/       # Android-точка входа
├─ shared/           # общий код и Compose UI
├─ iosApp/           # Xcode-проект
├─ build-logic/      # общие настройки Gradle
├─ server/           # Spring Boot и Dockerfile
├─ gradle/
├─ compose.yaml      # сервер, PostgreSQL и тома
└─ README.md
```

## Версии сборки

- JDK 21;
- Kotlin 2.4.10;
- Compose Multiplatform 1.11.1;
- AGP 9.3.1;
- Gradle 9.7.1;
- Android SDK 37.0;
- `targetSdk = 37`, `minSdk = 26`.

Для SDK 37.0 применяется штатный `minorApiLevel = 0`. Строку `android.suppressUnsupportedCompileSdk` не добавляем.

## Клиент

Оставить модули `androidApp`, `shared` и `iosApp`. Общий код пока делить пакетами, а не мелкими Gradle-модулями:

```text
core/model
core/network
core/database
core/location
core/map
core/designsystem
feature/routes
feature/navigation
feature/recording
feature/admin
feature/auth
```

Основные библиотеки:

- Compose Multiplatform и Material 3;
- Navigation Compose;
- Koin;
- Ktor Client;
- Room с bundled SQLite;
- MapLibre Compose;
- kotlinx.serialization, coroutines и datetime;
- Kermit.

MapLibre остаётся за интерфейсами `MapView`, `OfflineMapStore` и `MapStyleProvider`. Адрес debug-сервера для Android Emulator — `http://10.0.2.2:8080`.

## Сервер

Сервер — Spring Boot на Java 21 с Web, Validation, Data JPA, Actuator, Flyway и Testcontainers.

Нужно добавить:

- Dockerfile для сервера;
- сервис `server` в `compose.yaml`;
- внутреннюю сеть между сервером и PostgreSQL;
- ожидание готовности PostgreSQL;
- `GET /actuator/health`;
- базовый путь `/api/v1`;
- Flyway-миграции;
- тесты с PostgreSQL через Testcontainers.

В режиме Docker сервер подключается к `postgres:5432`. Порт PostgreSQL не открываем наружу при выпуске. Порт сервера `8080` доступен клиенту.

## Хранение фото

S3, MinIO и LocalStack сейчас не нужны. На одном сервере берём простую схему:

- байты фото лежат в томе `peshehod-photo-data`;
- сервер монтирует том в `/data/photos`;
- PostgreSQL хранит ключ файла, размер, тип, автора и связь с маршрутом;
- `PhotoStorage` скрывает работу с файловой системой;
- текущий `LocalPhotoStorage` переименовывается в `FileSystemPhotoStorage`;
- путь задаётся через `PESHEHOD_PHOTO_DIR=/data/photos`.

Фото нельзя хранить:

- внутри контейнера без тома — они исчезнут при замене контейнера;
- в Git или папке с исходным кодом;
- как большие бинарные поля PostgreSQL.

До загрузки файла сервер проверяет размер, MIME-тип и имя. Наружу не отдаём путь на диске: API работает с ID фото.

## Тома и код для копий

Docker Compose создаёт два тома:

```text
peshehod-postgres-data  # данные PostgreSQL
peshehod-photo-data     # фото
```

`docker compose stop` и `docker compose down` не удаляют данные. Команду `docker compose down -v` без прямого согласия владельца проекта не запускать.

Сейчас резервные копии не создаём и не запускаем по расписанию. Готовим только два скрипта на будущее:

- `scripts/backup.ps1` — сохраняет дамп PostgreSQL и архив фото;
- `scripts/restore.ps1` — восстанавливает базу и фото из выбранной копии.

Скрипты не запускаются сами. Место для копий и расписание выберем позже.

## Секреты

Пароли, ключи и `.env` не входят в Git. В репозитории остаётся только `.env.example` без настоящих значений. Debug-пароли допустимы лишь для локального запуска.

## Порядок работ

1. Завершить Gradle Sync на AGP 9.3.1 и Gradle 9.7.1.
2. Собрать Android APK и запустить его на `Pixel_9` с API 37.1.
3. Добавить Dockerfile сервера.
4. Расширить `compose.yaml`: `server`, `postgres`, два тома и проверки готовности.
5. Перевести фото на `/data/photos` в отдельном томе.
6. Запустить PostgreSQL и сервер через Docker Compose.
7. Проверить `/actuator/health` и запрос клиента через `10.0.2.2:8080`.
8. Запустить тесты клиента и сервера.
9. Проверить остановку и повторный запуск без потери базы и фото.
10. Добавить неактивные скрипты `backup.ps1` и `restore.ps1`.
11. Создать первый коммит только после всех проверок.

## Границы первого этапа

- iOS-код создаём, но собираем только на Mac с Xcode;
- CI, подпись и публикацию добавим перед тестовым выпуском;
- S3 понадобится лишь при переходе на несколько серверов или облачное хранение;
- резервные копии и их расписание пока не запускаем;
- аккаунты, JWT, маршруты и GPS-таблицы идут после готовой основы.

## Правило загрузок

Codex не скачивает Gradle, AGP, Docker-образы и другие крупные файлы сам. Перед нужной загрузкой он сообщает точное имя и ждёт, пока владелец проекта скачает файл или даст прямое разрешение.
