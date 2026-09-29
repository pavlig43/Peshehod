## Play Billing Library: список проверки по версиям

По этому списку проверьте все технические требования между [текущей] и [целевой] версиями.

## PBL v1.x–v3.x

- [ ] **[v1.0] Создание клиента:** используется `BillingClient.newBuilder(context)`.
- [ ] **[v2.0] Обязательное подтверждение:** `acknowledgePurchase()` или `consumeAsync()` вызывается в течение трёх дней.
- [ ] **[v2.0] Вид ответа:** код обрабатывает `BillingResult`, а не простые числа.
- [ ] **[v3.0] Удаление старого API:** параметры `ChildDirected` и `UnderAgeOfConsent` удалены.

## PBL v4.x

- [ ] **Запрос покупок без ожидания потока выполнения:** `queryPurchases()` заменён на `queryPurchasesAsync()`.
- [ ] **Доступ к нескольким SKU:** `getSku()` заменён на `getSkus()`, который возвращает список объектов `Purchase`.
- [ ] **Смена подписки:** для неё применяется `setSubscriptionUpdateParams()`.

## PBL v5.x

- [ ] **Новая модель данных:** все `SkuDetails` заменены на `ProductDetails`.
- [ ] **Цена с учётом пользователя:** для пояснения цены в ЕС применяется `setIsOfferPersonalized()`.

## PBL v6.x

- [ ] **Режим замены:** `ProrationMode` заменён перечислением `ReplacementMode`.
- [ ] **Выбор способа оплаты:** `AlternativeBillingListener` заменён на `UserChoiceBillingListener`.

## PBL v7.x

- [ ] **Требование SDK:** `compileSdk` не ниже 34.
- [ ] **Покупки в ожидании:** вызов `enablePendingPurchases()` без параметров заменён на `enablePendingPurchases(PendingPurchaseParams)`.
- [ ] **Очистка API:** `setOldSkuPurchaseToken()` заменён на `setOldPurchaseToken()`.

## PBL v8.x

- [ ] **Требование SDK:** `compileSdk` равен 35.
- [ ] **Названия:** в интерфейсе и строках «товары внутри приложения» переименованы в «разовые товары».
- [ ] **Сигнатура обработчика:** `onProductDetailsResponse` принимает `(BillingResult, QueryProductDetailsResult)`.
- [ ] **Восстановление связи:** в построителе применяется `enableAutoServiceReconnection()`.
- [ ] **Минимальный SDK:** `minSdkVersion` не ниже 23.

## Будущие версии PBL 9.0.0+

- [ ] **Новый список:** для любой версии от 9.0.0 обязательно составьте список по каждому новому заголовку версии в [примечаниях к выпускам](https://developer.android.com/google/play/billing/release-notes).
- [ ] **Разница версий:** прочитайте разделы об несовместимых изменениях и удалённых API и составьте список терминов для поиска через `grep`.
