В этом документе собраны примечания к выпускам Google Play Billing Library.

## Google Play Billing Library 8.3.0 (2025-12-23)

Выпущены версия 8.3.0 Google Play Billing Library и расширения Kotlin.

### Изменения

- Добавлены API для [внешних платежей](https://developer.android.com/google/play/billing/externalpaymentlinks):
  - добавлены классы `BillingProgram.EXTERNAL_PAYMENTS`, `EnableBillingProgramParams`, `DeveloperBillingOptionParams`, `DeveloperProvidedBillingDetails` и `DeveloperProvidedBillingListener` для поддержки сценария внешней оплаты;
  - добавлен `enableBillingProgram(EnableBillingProgramParams)` для включения внешних платежей;
  - добавлен `BillingFlowParams.Builder.enableDeveloperBillingOption` для запуска сценария внешней оплаты.

## Google Play Billing Library 8.2.1 (2025-12-15)

Выпущена версия 8.2.1 Google Play Billing Library и расширений Kotlin.

### Исправления ошибок

- Исправлена ошибка в `isBillingProgramAvailableAsync` и `createBillingProgramReportingDetailsAsync`. Обновитесь до версии 8.2.1, чтобы использовать API, появившиеся в 8.2.0.

## Google Play Billing Library 8.2.0 (2025-12-09)

Выпущена версия 8.2.0 Google Play Billing Library и расширений Kotlin.

### Изменения

- Добавлены API для [внешних ссылок на контент](https://developer.android.com/google/play/billing/externalcontentlinks) и [внешних предложений](https://developer.android.com/google/play/billing/external):
  - `enableBillingProgram` настраивает `BillingClient` для этих программ;
  - `isBillingProgramAvailableAsync` проверяет, может ли пользователь участвовать;
  - `createBillingProgramReportingDetailsAsync` создаёт внешний токен транзакции для отчётности;
  - `launchExternalLink` открывает внешнюю ссылку на предложение цифрового контента или загрузку приложения.
- Изменена программа [внешних предложений](https://developer.android.com/google/play/billing/external):
  - обновились правила программы. См. [описание изменений](https://support.google.com/googleplay/android-developer/answer/16505463) и [руководство по интеграции](https://developer.android.com/google/play/billing/external/integration) для новых API;
  - API `BillingClient.Builder.enableExternalOffer`, `isExternalOfferAvailableAsync`, `createExternalOfferReportingDetailsAsync` и `showExternalOfferInformationDialog` объявлены устаревшими.

## Google Play Billing Library 8.1.0 (2025-11-06)

### Изменения

- Добавлена поддержка приостановленных подписок. Новый параметр `BillingClient.queryPurchasesAsync()` позволяет включать их в запрос. Приостановленная подписка по-прежнему связана с пользователем, но неактивна: например, пользователь приостановил её или платёж не прошёл.
- Для таких подписок объект `Purchase` возвращает `isSuspended() = true`. Не предоставляйте доступ к подписке; предложите пользователю открыть [центр подписок](https://play.google.com/store/account/subscriptions), чтобы изменить способ оплаты или снять приостановку.
- Обновлена обработка [подписок](https://developer.android.com/google/play/billing/subscriptions):
  - в `BillingFlowParams.ProductDetailsParams` добавлен `setSubscriptionProductReplacementParams()` для настройки замены на уровне продукта;
  - `SubscriptionProductReplacementParams` предоставляет `setOldProductId` и `setReplacementMode`. Режимы похожи на `SubscriptionUpdateParams`, но сопоставление значений изменилось. Добавлен `KEEP_EXISTING`, сохраняющий прежнее расписание платежей;
  - `SubscriptionUpdateParams.setSubscriptionReplacementMode` объявлен устаревшим. Используйте `SubscriptionProductReplacementParams.setReplacementMode`.
- Минимальная версия SDK (`minSdkVersion`) повышена до 23.
- Для разовых покупок включены [API предзаказа](https://developer.android.com/google/play/billing/one-time-product-multi-purchase-options-offers#pre-order). Теперь доступен `ProductDetails.oneTimePurchaseOfferDetails.getPreorderDetails()`.
- Google Play Billing Library теперь поддерживает [Kotlin 2.2.0](https://kotlinlang.org/docs/whatsnew22.html).

## Google Play Billing Library 8.0.0 (2025-06-30)

### Изменения

- Термин *in-app items* заменён на *one-time products* (разовые продукты).
- Для разовых продуктов можно задавать несколько вариантов покупки и предложений. Это делает способы продажи гибче и упрощает управление ими.
- Улучшен `queryProductDetailsAsync()`: ранее продукты, которые не удалось получить (например, отсутствующие или без подходящих предложений), не возвращались. Теперь они включаются в ответ с новым кодом состояния на уровне продукта. Изменилась сигнатура `ProductDetailsResponseListener.onProductDetailsResponse()`, поэтому приложение нужно обновить. См. [обработку результата](https://developer.android.com/google/play/billing/integrate#process-the-result).
- Добавлено автоматическое переподключение службы. Новый параметр сборки `BillingClient.Builder.enableAutoServiceReconnection()` включает автоматическое подключение к Play Billing Service и устраняет необходимость вручную вызывать `startConnection()` после разрыва. См. [автоматическое восстановление подключения](https://developer.android.com/google/play/billing/integrate#automatic-service-reconnection).
- В ответ `BillingResult` метода `launchBillingFlow()` добавлено поле кода подответа для более точного объяснения ошибки:
  - `PAYMENT_DECLINED_DUE_TO_INSUFFICIENT_FUNDS` — средств пользователя меньше стоимости покупки;
  - `USER_INELIGIBLE` — пользователь не соответствует условиям предложения подписки;
  - `NO_APPLICABLE_SUB_RESPONSE_CODE` — стандартное значение, если не подходит другой код.
- Удалён устаревший `queryPurchaseHistory()`. См. [альтернативы запросу истории покупок](https://developer.android.com/google/play/billing/query-purchase-history).
- Удалён устаревший `querySkuDetailsAsync()`; используйте `queryProductDetailsAsync()`.
- Удалён `BillingClient.Builder.enablePendingPurchases()` без параметров. Вместо него используйте `enablePendingPurchases(PendingPurchaseParams params)`. Старый вариант равнозначен `enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())`.
- Удалён перегруженный `queryPurchasesAsync()`, принимавший `skuType`. Используйте вариант с `QueryPurchasesParams` и `PurchasesResponseListener`.

## Google Play Billing Library 7.1.1 (2024-10-03)

### Исправления ошибок

- Исправлена ошибка в Play Billing Library 7.1.0, связанная с [проверкой кодов ответа `BillingResult`](https://developer.android.com/google/play/billing/test-response-codes).

## Google Play Billing Library 7.1.0 (2024-09-19)

### Изменения

- Улучшена потокобезопасность управления состоянием подключения.
- Добавлены первые возможности тестирования кодов ответа `BillingResult`; полностью они вышли в Play Billing Library 7.1.1. Для проверки интеграции обновитесь до 7.1.1. Ошибка затрагивает только приложения с [тестированием подмены биллинга](https://developer.android.com/google/play/billing/test-response-codes#enable-billing-overrides-testing) и не влияет на обычную работу. См. [проверку кодов `BillingResult`](https://developer.android.com/google/play/billing/test-response-codes).

## Google Play Billing Library 7.0.0 (2024-05-14)

### Изменения

- Добавлены API для рассрочки по подпискам: `ProductDetails.InstallmentPlanDetails` сообщает о доступных пользователю тарифах рассрочки и их обязательствах. См. [руководство по рассрочке](https://developer.android.com/google/play/billing/subscriptions#installments).
- Добавлены `PendingPurchasesParams` и `BillingClient.Builder.enablePendingPurchases(PendingPurchaseParams)` взамен устаревшего `enablePendingPurchases()`. Старый вызов функционально равнозначен `enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())`.
- Добавлена поддержка ожидающих транзакций для предоплаченных планов подписки. Включайте `PendingPurchasesParams.Builder.enablePrepaidPlans()` вместе с новым `enablePendingPurchases(...)` и корректно обрабатывайте жизненный цикл подписки. См. [руководство по ожидающим покупкам](https://developer.android.com/google/play/billing/subscriptions#pending).
- Добавлены `Purchase.PendingPurchaseUpdate` и `Purchase.getPendingPurchaseUpdate()` для получения ожидающего пополнения или повышения / понижения существующей подписки.
- Удалены `BillingClient.Builder.enableAlternativeBilling()`, `AlternativeBillingListener` и `AlternativeChoiceDetails`. Используйте `BillingClient.Builder.enableUserChoiceBilling()`, `UserChoiceBillingListener` и `UserChoiceDetails` в обратном вызове слушателя.
- Удалены `BillingFlowParams.ProrationMode`, `setReplaceProrationMode()` и `setReplaceSkusProrationMode()`. Используйте `BillingFlowParams.SubscriptionUpdateParams.ReplacementMode` и `setSubscriptionReplacementMode(int)`.
- Удалён `setOldSkuPurchaseToken()`. Используйте `setOldPurchaseToken(java.lang.String)`.
- `BillingClient.queryPurchaseHistoryAsync()` объявлен устаревшим и будет удалён. Для активных покупок используйте `queryPurchasesAsync()`, для использованных покупок ведите учёт на своём сервере, для отменённых — API [voided-purchases](https://developers.google.com/android-publisher/voided-purchases). Подробности — [история покупок](https://developer.android.com/google/play/billing/query-purchase-history).
- `BillingFlowParams.ProductDetailsParams.setOfferToken()` теперь выбрасывает исключение при пустом `offerToken`.
- Минимальный SDK повышен до 21, целевой — до 34.

## Google Play Billing Library 6.2.1 (2024-04-16)

### Изменения

- Исправлена ошибка в `BillingClient.showAlternativeBillingOnlyInformationDialog()`, из-за которой в некоторых случаях после закрытия диалога не вызывался `AlternativeBillingOnlyInformationDialogListener`.

## Google Play Billing Library 6.2.0 (2024-03-06)

### Изменения

- Добавлены API для [внешних предложений](https://developer.android.com/google/play/billing/external): `enableExternalOffer()` включает возможность предлагать внешние варианты оплаты; `isExternalOfferAvailableAsync()` проверяет доступность; `showExternalOfferInformationDialog()` показывает информационный диалог перед переходом за пределы приложения; `createExternalOfferReportingDetailsAsync()` создаёт данные для отчётности по таким транзакциям.

## Google Play Billing Library 6.1.0 (2023-11-14)

### Изменения

- Добавлены API для [альтернативной оплаты без выбора пользователя](https://developer.android.com/google/play/billing/alternative): `enableAlternativeBillingOnly()` включает режим, `isAlternativeBillingOnlyAvailableAsync()` проверяет доступность, `showAlternativeBillingOnlyInformationDialog()` уведомляет пользователя, `createAlternativeBillingOnlyReportingDetailsAsync()` создаёт данные для отчёта по транзакциям.
- Обновлены API выбора оплаты пользователем: `UserChoiceBillingListener`, `UserChoiceDetails` и `BillingClient.Builder.enableUserChoiceBilling()` заменяют устаревшие `AlternativeBillingListener`, `AlternativeChoiceDetails` и `enableAlternativeBilling()`.
- Добавлен `BillingClient.getBillingConfigAsync()` для получения страны Google Play.

## Google Play Billing Library 6.0.1 (2023-06-22)

### Изменения

Обновлена совместимость Play Billing Library с Android 14.

## Google Play Billing Library 6.0.0 (2023-05-10)

### Изменения

- Добавлен перечислимый тип `ReplacementMode` взамен `ProrationMode`. Последний сохранён для обратной совместимости.
- У ожидающих покупок `PENDING` больше не создаётся номер заказа. Он появляется после перехода покупки в состояние `PURCHASED`.
- Удалены устаревшие `queryPurchases` и `launchPriceConfirmationFlow`. Используйте `queryPurchasesAsync()`; альтернативы для подтверждения изменения цены описаны в разделе [изменения цен](https://developer.android.com/google/play/billing/price-changes).
- Добавлен код ошибки сети `NETWORK_ERROR`. Ранее ошибки соединения возвращались как `SERVICE_UNAVAILABLE`.
- Тайм-ауты обработки теперь возвращаются как `SERVICE_UNAVAILABLE`, а не `SERVICE_TIMEOUT`. В более ранних версиях поведение не изменилось.
- `SERVICE_TIMEOUT` больше не возвращается в версии 6.0.0; предыдущие версии по-прежнему могут его возвращать.
- Добавлены подробные журналы использования API (успехи и ошибки) и проблем соединения со службой для улучшения библиотеки и диагностики.

## Google Play Billing Library 5.2.1 (2023-06-22)

### Изменения

Обновлена совместимость Play Billing Library с Android 14.

## Google Play Billing Library 5.2.0 (2023-04-06)

### Изменения

- Для пользователей из Южной Кореи добавлены классы альтернативной оплаты на телефонах и планшетах: `AlternativeBillingListener`, `AlternativeChoiceDetails` и `AlternativeChoiceDetails.Product`.
- Добавлен `BillingFlowParams.SubscriptionUpdateParams.Builder.setOriginalExternalTransactionId()` для указания ID внешней транзакции исходной подписки.
- Добавлен `BillingClient.Builder.enableAlternativeBilling()` для выбора альтернативной оплаты пользователями из Южной Кореи.

## Google Play Billing Library 5.1.0 (2022-10-31)

### Изменения

- Добавлен `ProductDetails.SubscriptionOfferDetails.getOfferId()` для получения ID предложения.
- Добавлен `ProductDetails.SubscriptionOfferDetails.getBasePlanId()` для получения ID базового плана.
- Целевая версия SDK (`targetSdkVersion`) обновлена до 31.

## Google Play Billing Library 5.0.0 (2022-05-11)

### Изменения

- Введена новая модель подписок, позволяющая создавать несколько предложений для одного продукта подписки. См. [руководство по миграции](https://developer.android.com/google/play/billing/migrate-gpblv5).
- Добавлен `BillingClient.queryProductDetailsAsync()` взамен `querySkuDetailsAsync()`.
- Добавлен `setIsOfferPersonalized()` для уведомления о персональной цене в ЕС. См. [обозначение персональной цены](https://developer.android.com/google/play/billing/integrate#personalized-price).
- Удалён устаревший `queryPurchases()`; его заменил `queryPurchasesAsync()` в версии 4.0.0.
- `launchPriceChangeFlow` объявлен устаревшим; альтернативы описаны в разделе [подтверждения изменения цены](https://developer.android.com/google/play/billing/subscriptions#price-change-launch).
- Удалён `setVrPurchaseFlow()`, который перенаправлял пользователя на устройство Android для завершения покупки. Теперь покупка проходит стандартный поток.

## Google Play Billing Library 4.1.0 (2022-02-23)

### Изменения

- Добавлен `BillingClient.showInAppMessages()` для обработки отказов оплаты подписки. См. [сообщения в приложении об отказе платежа](https://developer.android.com/google/play/billing/subscriptions#payment-declines).

## Google Play Billing Library 4.0.0 (2021-05-18)

### Изменения

- Добавлен `BillingClient.queryPurchasesAsync()` взамен `queryPurchases()`, который будет удалён в будущем.
- Добавлен новый режим замены подписки `IMMEDIATE_AND_CHARGE_FULL_PRICE`.
- Добавлен `BillingClient.getConnectionState()` для получения состояния подключения библиотеки.
- В Javadoc и реализации указано, в каком потоке можно вызывать методы и в каком возвращаются результаты.
- Добавлен `BillingFlowParams.Builder.setSubscriptionUpdateParams()` для начала обновления подписки. Он заменяет удалённые методы `getReplaceSkusProrationMode`, `getOldSkuPurchaseToken`, `getOldSku`, `setReplaceSkusProrationMode` и `setOldSku`.
- Добавлены `Purchase.getQuantity()` и `PurchaseHistoryRecord.getQuantity()`.
- Добавлены `Purchase.getSkus()` и `PurchaseHistoryRecord.getSkus()` взамен удалённых `getSku`.
- Удалены `BillingFlowParams.getSku()`, `getSkuDetails()` и `getSkuType()`.

## Google Play Billing Library 3.0.3 (2021-03-12)

Выпущены версия 3.0.3 библиотеки, расширения Kotlin и плагина Unity.

### Исправления Java и Kotlin

- Исправлена утечка памяти при вызове `endConnection()`.
- Исправлена проблема приложений с режимом запуска single task: если приложение возвращалось из лаунчера, пока был открыт диалог оплаты, вызывался обратный вызов `onPurchasesUpdated()`.

### Исправления Unity

- Обновлена Java-библиотека до версии 3.0.3 для устранения утечки памяти и проблемы, из-за которой нельзя было купить товар после возвращения приложения из лаунчера с открытым диалогом оплаты.

## Google Play Billing Library 3.0.2 (2020-11-24)

### Исправления ошибок

- Исправлена ошибка расширения Kotlin, из-за которой coroutine завершалась с сообщением `Already resumed`.
- Исправлены неразрешённые ссылки при использовании расширения Kotlin с kotlinx.coroutines 1.4 и новее.

## Google Play Billing Library 3.0.1 (2020-09-30)

### Исправления ошибок

- Если приложение закрывалось и восстанавливалось во время покупки, `PurchasesUpdatedListener` мог не получить результат. Ошибка исправлена.

## Google Play Billing Library 3.0.0 (2020-06-08)

Выпущены библиотека, расширение Kotlin и плагин Unity версии 3.0.0.

### Изменения

- Удалена поддержка вознаграждаемых SKU.
- Удалены параметры `ChildDirected` и `UnderAgeOfConsent`.
- Удалены устаревшие методы developer payload.
- Удалены устаревшие `BillingFlowParams.setAccountId()` и `setDeveloperId()`.
- Удалены устаревшие `BillingFlowParams.setOldSkus(String oldSku)` и `addOldSku(String oldSku)`.
- Добавлены аннотации nullability.

### Исправления ошибок

- `SkuDetails.getIntroductoryPriceCycles()` теперь возвращает `int`, а не `String`.
- Исправлена ошибка, при которой поток оплаты считался содержащим дополнительные параметры, даже если параметры не задавались.

## Google Play Billing Library 2.2.1 (2020-05-20)

### Исправления ошибок

- Обновлена версия Java Play Billing Library по умолчанию, от которой зависит расширение Kotlin.

## Google Play Billing Library 2.2.0 и поддержка Unity (2020-03-23)

Версия 2.2.0 упрощает привязку покупки к правильному пользователю и устраняет необходимость создавать собственные решения на основе developer payload. В рамках этих изменений developer payload объявлен устаревшим и будет удалён в будущем. См. рекомендации и альтернативы в разделе [developer payload](https://developer.android.com/google/play/billing/developer-payload).

### Google Play Billing Library 2 для Unity

Помимо версий Java и Kotlin вышла версия библиотеки для Unity. Разработчики игр, использующие внутриигровые покупки Unity, могут перейти на неё, чтобы воспользоваться возможностями версии 2 и упростить будущие обновления. См. [использование Google Play Billing с Unity](https://developer.android.com/google/play/billing/unity).

### Изменения

- В `AcknowledgePurchaseParams` методы `setDeveloperPayload()` и `getDeveloperPayload()` объявлены устаревшими.
- В `ConsumeParams` методы `setDeveloperPayload()` и `getDeveloperPayload()` объявлены устаревшими.
- В `BillingFlowParams` метод `setAccountId()` переименован в `setObfuscatedAccountId()`. Указано ограничение в 64 символа и запрещено передавать персональные данные (PII). `setAccountId()` объявлен устаревшим.
- В `BillingFlowParams` добавлен `setObfuscatedProfileId()`, работающий аналогично `setObfuscatedAccountId()`. См. [изменения developer payload и альтернативы](https://developer.android.com/google/play/billing/developer-payload).
- В `Purchase` добавлен `getAccountIdentifiers()` для получения замаскированных идентификаторов учётной записи из `BillingFlowParams`.
- `BillingClient.loadRewardedSku()` объявлен устаревшим вместе с поддержкой rewarded SKU. См. справку [Play Console](https://support.google.com/googleplay/android-developer/answer/9155268).

## Google Play Billing Library 2.1.0 и расширение Kotlin 2.1.0 (2019-12-10)

Вышла версия 2.1.0 Java-библиотеки и новое расширение Kotlin с идиоматичными API, улучшенной обработкой null и coroutine. Примеры кода приведены в [руководстве Google Play Billing](https://developer.android.com/google/play/billing/billing_library_overview).

### Изменения

- `BillingFlowParams.setOldSku(String oldSku)` объявлен устаревшим и заменён на `setOldSku(String oldSku, String purchaseToken)`. Токен позволяет различать аккаунты, если один и тот же SKU куплен несколькими аккаунтами устройства.

## Google Play Billing Library 2.0.3 (2019-08-05)

### Исправления ошибок

- Исправлена ошибка, из-за которой `querySkuDetailsAsync()` иногда возвращал `DEVELOPER_ERROR` вместо успешного результата.

## Google Play Billing Library 2.0.2 (2019-07-08)

Обновлена справочная документация. Функции библиотеки не изменились.

## Google Play Billing Library 2.0.1 (2019-06-06)

### Исправления ошибок

- Исправлены случаи, когда отладочные сообщения возвращались как `null`.
- Устранена возможная утечка памяти.

## Google Play Billing Library 2.0 (2019-05-07)

### Покупки нужно подтверждать в течение трёх дней

> [!NOTE]
> Подтверждать нужно все покупки. Если вы вызываете `consumeAsync()` в течение трёх дней, дополнительных изменений не требуется: этот метод автоматически подтверждает покупку. Неподтверждённые покупки возвращаются пользователю.

> [!NOTE]
> Требование действует только для приложений с Google Play Billing Library версии 2.0 и новее. Оно не распространяется на старые версии библиотеки и API AIDL.

Google Play поддерживает покупки внутри приложения и за его пределами. Чтобы сохранять единый процесс покупки, после предоставления пользователю доступа как можно скорее подтвердите все покупки, полученные через Google Play Billing Library. Если не подтвердить покупку за три дня, пользователь автоматически получит возврат средств, а Google Play отменит покупку. Для [ожидающих транзакций](https://developer.android.com/google/play/billing/release-notes#2_0_pending), появившихся в версии 2.0, отсчёт трёх дней начинается при переходе покупки в `PURCHASED` и не идёт, пока она находится в `PENDING`.

Для подписок подтверждайте каждую покупку с новым токеном: первоначальную покупку, смену плана и повторную подписку. Последующие продления подтверждать не требуется. Проверить необходимость подтверждения можно по соответствующему полю покупки.

В `Purchase` добавлен `isAcknowledged()`. Google Play Developer API также возвращает логическое поле подтверждения для `Purchases.products` и `Purchases.subscriptions`. Перед подтверждением проверьте, не было ли оно выполнено ранее.

Способы подтверждения:

- для расходуемых продуктов — клиентский API `consumeAsync()`;
- для нерасходуемых продуктов — клиентский API `acknowledgePurchase()`;
- на сервере доступен новый метод `acknowledge()`.

### Удалён BillingFlowParams.setSku()

Устаревший `BillingFlowParams.setSku()` удалён. Перед показом товаров в потоке покупки вызовите `BillingClient.querySkuDetailsAsync()` и передайте полученный `SkuDetails` в `BillingFlowParams.Builder.setSkuDetails()`.

> [!NOTE]
> Не рекомендуется хранить `SkuDetails` между пользовательскими сеансами: через ограниченное время объект перестаёт быть актуальным, и данные нужно получать заново через `querySkuDetailsAsync()`.

Примеры приведены в [руководстве Google Play Billing](https://developer.android.com/google/play/billing/billing_library_overview).

### Поддержка developer payload

В версии 2.0 добавлена поддержка *developer payload* — произвольных строк, прикрепляемых к покупке. Параметр можно добавить при подтверждении или расходовании покупки. В отличие от AIDL, указывать его при запуске потока покупки нельзя. Поскольку покупку можно начать [вне приложения](https://developer.android.com/google/play/billing/release-notes#2_0_acknowledge), новый подход позволяет добавить payload в любой покупке.

Для получения значения в `Purchase` добавлен метод `getDeveloperPayload()`.

- `SkuDetails.getIntroductoryPriceAmountMicros()` теперь возвращает `long`, а не `String`.

## Google Play Billing Library 1.2.2 (2019-03-07)

### Исправления ошибок

- Исправлена проблема с потоками, появившаяся в версии 1.2.1: фоновые вызовы больше не блокируют главный поток.

### Другие изменения

- Хотя рекомендуется главный поток, библиотеку теперь можно создавать в фоновом потоке.
- Создание полностью перенесено в фоновый поток, чтобы снизить вероятность ANR.

## Google Play Billing Library 1.2.1 (2019-03-04)

### Основные изменения

- Добавлена поддержка [вознаграждаемых продуктов](https://developer.android.com/google/play/billing/billing_rewarded_products). Варианты монетизации описаны в руководстве [добавления функций вознаграждаемого продукта](https://developer.android.com/distribute/best-practices/earn/monetization-options).

### Другие изменения

- Добавлены открытые конструкторы `PurchasesResult` и `SkuDetailsResult`, чтобы упростить тестирование.
- В `SkuDetails` добавлен метод `getOriginalJson()`.
- Все вызовы службы AIDL теперь выполняются в фоновых потоках.

### Исправления ошибок

- В публичные API больше не передаются пустые callback-слушатели.

## Google Play Billing Library 1.2 (2018-10-18)

### Изменения

- Библиотека перешла на [лицензионное соглашение Android SDK](https://developer.android.com/studio/terms).
- Добавлен API `launchPriceChangeConfirmationFlow`, предлагающий пользователю подтвердить ожидающее изменение цены подписки.
- Добавлен режим пропорционального расчёта `DEFERRED` для повышения или понижения подписки.
- В `BillingFlowParams` метод `setSku()` заменён на `setSkuDetails()`.
- Исправлены небольшие ошибки и оптимизирован код.

#### Подтверждение изменения цены

Теперь в Google Play Console можно изменить цену подписки и предложить пользователям подтвердить её при следующем входе в приложение.

Создайте `PriceChangeFlowParams` по `skuDetails` продукта подписки, вызовите `launchPriceChangeConfirmationFlow()` и обработайте результат в `PriceChangeConfirmationListener`. Диалог показывает новую цену и предлагает принять её. Результат имеет тип `BillingClient.BillingResponse`.

#### Новый режим пропорционального расчёта

При повышении или понижении подписки режим `DEFERRED` применяет новую подписку при следующем продлении. См. [настройку режима пропорционального расчёта](https://developer.android.com/google/play/billing/billing_subscriptions#set-proration-mode).

#### Новый способ задавать сведения SKU

Метод `BillingFlowParams.setSku()` объявлен устаревшим для оптимизации процесса Google Play Billing. Создавая `BillingFlowParams`, используйте `setSkuDetails()` с объектом, полученным через `querySkuDetailsAsync()`. Вместо объекта `sku` передавайте соответствующие сведения о продукте.

Примеры Kotlin и Java в исходной документации демонстрируют запрос `SkuDetails` и запуск `BillingFlowParams` через `setSkuDetails()`.

## Play Billing Library 1.1 (2018-05-07)

### Изменения

- Добавлена настройка режима пропорционального расчёта в `BillingFlowParams` при повышении или понижении подписки.
- Логический флаг `replaceSkusProration` больше не поддерживается. Используйте `replaceSkusProrationMode`.
- `launchBillingFlow()` теперь вызывает callback и при неуспешном ответе.

### Изменения поведения

#### Настройка replaceSkusProrationMode

`ProrationMode` точнее описывает способ пересчёта при повышении или понижении подписки.

| Режим | Поведение |
|-------|-----------|
| `IMMEDIATE_WITH_TIME_PRORATION` | Замена действует сразу; срок окончания пересчитывается, а разница возвращается или списывается. Это поведение по умолчанию. |
| `IMMEDIATE_AND_CHARGE_PRORATED_PRICE` | Замена действует сразу, расчётный цикл не меняется; списывается цена за оставшийся срок. Доступно только при повышении подписки. |
| `IMMEDIATE_WITHOUT_PRORATION` | Замена действует сразу, новая цена списывается при следующем продлении; расчётный цикл не меняется. |

#### Флаг replaceSkusProration больше не поддерживается

Раньше для списания пропорциональной суммы при повышении подписки использовался логический флаг. Теперь вместо него нужно использовать `ProrationMode`, который задаёт больше вариантов пересчёта.

#### Callback для ошибочных ответов launchBillingFlow()

Billing Library теперь всегда асинхронно вызывает `PurhcasesUpdatedListener` и передаёт `BillingResponse`. Синхронное возвращаемое значение `BillingResponse` также сохраняется.

### Исправления ошибок

- Асинхронные методы теперь корректно завершаются, если служба отключена.
- Параметры `Builder` больше не меняют уже созданные объекты.
- Исправлена ошибка 68087141: `launchBillingFlow()` теперь вызывает callback при ошибочном ответе.

## Google Play Billing Library 1.0 (2017-09-19, [объявление](https://android-developers.googleblog.com/2017/09/google-play-billing-library-10-released.html))

### Важные изменения

- Разрешение для биллинга добавлено в манифест библиотеки. Разрешение `com.android.vending.BILLING` больше не нужно добавлять в манифест приложения.
- В `BillingClient.Builder` добавлен новый конструктор.
- Для `SkuDetailsParams`, используемого при запросе SKU, введён шаблон Builder.
- Несколько API-методов приведены к единому виду: одинаковый порядок и названия возвращаемых аргументов.

### Изменения поведения

#### BillingClient.Builder

`BillingClient.Builder` теперь создаётся через `newBuilder`.

#### Новый параметр BillingFlowParams для launchBillingFlow

Для запуска покупки или подписки в `launchBillingFlow()` передаётся объект `BillingFlowParams` со значениями для конкретного запроса.

#### Новый способ запрашивать доступные продукты

Параметры `queryPurchaseHistoryAsync()` и `querySkuDetailsAsync()` теперь создаются через Builder. Результат возвращается кодом ответа и списком объектов `SkuDetails` вместо прежнего класса-обёртки; это упрощает использование и приводит API к единому виду.

#### Изменён порядок параметров onConsumeResponse()

Порядок аргументов `onConsumeResponse()` интерфейса `ConsumeResponseListener` изменён для единообразия API.

#### Удалена обёртка PurchaseResult

`PurchaseResult` развёрнут для согласования API: методы истории покупок получают код ответа и список `Purchase` напрямую.

### Исправления ошибок

- [В Bundle PURCHASES_UPDATED отсутствовал код ответа](https://issuetracker.google.com/issues/64075043).
- [Исправлены проблемы ProxyBillingActivity и PurchasesUpdatedListener при повороте устройства](https://issuetracker.google.com/issues/63266562).

## Developer Preview 1 (2017-06-12, [объявление](https://android-developers.googleblog.com/2017/06/money-made-easily-with-new-google-play.html))

Вышла предварительная версия для разработчиков. Библиотека упрощает работу с платежами, позволяя сосредоточиться на логике Android-приложения, например архитектуре и навигации.
