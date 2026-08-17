package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GooglePlayBillingManager private constructor(private val appContext: Context) : PurchasesUpdatedListener {

  companion object {
    private const val TAG = "GooglePlayBilling"
    const val PRODUCT_ID_PRO = "ari_takip_pro"
    const val BASE_PLAN_MONTHLY = "aylik"
    const val BASE_PLAN_YEARLY = "yillik"

    private val MONTHLY_ALIASES = listOf("aylik", "monthly", "aylik_plan", "aylik-plan")
    private val YEARLY_ALIASES = listOf("yillik", "yearly", "yillik_plan", "yillik-plan")

    @Volatile private var INSTANCE: GooglePlayBillingManager? = null

    fun getInstance(context: Context): GooglePlayBillingManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: GooglePlayBillingManager(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
    .setListener(this)
    .enablePendingPurchases(
      PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()
    )
    .build()

  private val _isPro = MutableStateFlow(false)
  val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

  private val _isConnected = MutableStateFlow(false)
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  private val _monthlyPrice = MutableStateFlow("49,99 TL / Ay")
  val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

  private val _yearlyPrice = MutableStateFlow("499,99 TL / Yıl")
  val yearlyPrice: StateFlow<String> = _yearlyPrice.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private var proProductDetails: ProductDetails? = null
  private var monthlyOfferToken: String? = null
  private var yearlyOfferToken: String? = null
  private var reconnectAttempts = 0
  private val maxReconnectAttempts = 3

  init {
    startBillingConnection()
  }

  fun startBillingConnection(onConnected: (() -> Unit)? = null) {
    if (billingClient.isReady) {
      _isConnected.value = true
      refreshStoreState(onConnected)
      return
    }

    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          _isConnected.value = true
          reconnectAttempts = 0
          refreshStoreState(onConnected)
        } else {
          _isConnected.value = false
          _statusMessage.value = when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
              "Google Play Billing bu cihazda kullanılamıyor."
            else ->
              "Google Play bağlantısı kurulamadı (${billingResult.responseCode})."
          }
          Log.w(TAG, "Billing setup failed: ${billingResult.responseCode}, ${billingResult.debugMessage}")
        }
      }

      override fun onBillingServiceDisconnected() {
        _isConnected.value = false
        if (reconnectAttempts < maxReconnectAttempts) {
          reconnectAttempts++
          startBillingConnection()
        }
      }
    })
  }

  private fun refreshStoreState(onConnected: (() -> Unit)? = null) {
    queryProductDetails {
      queryActivePurchases()
      onConnected?.invoke()
    }
  }

  /**
   * Reads the subscription and its actual offer tokens from Google Play.
   * The app never invents an offer token; it always uses the token returned by Play.
   */
  fun queryProductDetails(onReady: (() -> Unit)? = null) {
    if (!billingClient.isReady) {
      onReady?.invoke()
      return
    }

    val product = QueryProductDetailsParams.Product.newBuilder()
      .setProductId(PRODUCT_ID_PRO)
      .setProductType(BillingClient.ProductType.SUBS)
      .build()

    val params = QueryProductDetailsParams.newBuilder()
      .setProductList(listOf(product))
      .build()

    billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
      if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
        proProductDetails = null
        monthlyOfferToken = null
        yearlyOfferToken = null
        _statusMessage.value = "Google Play PRO ürünü okunamadı. Kod: ${billingResult.responseCode}"
        Log.w(TAG, "queryProductDetails failed: ${billingResult.responseCode}, ${billingResult.debugMessage}")
        onReady?.invoke()
        return@queryProductDetailsAsync
      }

      val details = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PRO }
      if (details == null) {
        proProductDetails = null
        monthlyOfferToken = null
        yearlyOfferToken = null
        _statusMessage.value = "Google Play'de '${PRODUCT_ID_PRO}' aboneliği bulunamadı."
        Log.w(TAG, "Product not found: $PRODUCT_ID_PRO")
        onReady?.invoke()
        return@queryProductDetailsAsync
      }

      proProductDetails = details
      monthlyOfferToken = null
      yearlyOfferToken = null

      details.subscriptionOfferDetails.orEmpty().forEach { offer ->
        val basePlanId = offer.basePlanId.lowercase(Locale.ROOT)
        val phase = offer.pricingPhases.pricingPhaseList.firstOrNull()
        val price = phase?.formattedPrice

        when {
          MONTHLY_ALIASES.any { basePlanId == it || basePlanId.contains(it) } -> {
            if (monthlyOfferToken == null) monthlyOfferToken = offer.offerToken
            if (price != null) _monthlyPrice.value = "$price / Ay"
          }
          YEARLY_ALIASES.any { basePlanId == it || basePlanId.contains(it) } -> {
            if (yearlyOfferToken == null) yearlyOfferToken = offer.offerToken
            if (price != null) _yearlyPrice.value = "$price / Yıl"
          }
        }
      }

      // If Play returns different base-plan names, use the first available offer as a safe fallback.
      val offers = details.subscriptionOfferDetails.orEmpty()
      if (monthlyOfferToken == null && offers.isNotEmpty()) {
        monthlyOfferToken = offers.first().offerToken
        offers.first().pricingPhases.pricingPhaseList.firstOrNull()?.formattedPrice?.let {
          _monthlyPrice.value = "$it / Ay"
        }
      }

      Log.d(
        TAG,
        "PRO ready: product=${details.productId}, monthly=${monthlyOfferToken != null}, yearly=${yearlyOfferToken != null}"
      )
      onReady?.invoke()
    }
  }

  fun queryActivePurchases(onComplete: ((Boolean) -> Unit)? = null) {
    if (!billingClient.isReady) {
      startBillingConnection { queryActivePurchases(onComplete) }
      return
    }

    val params = QueryPurchasesParams.newBuilder()
      .setProductType(BillingClient.ProductType.SUBS)
      .build()

    billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        onComplete?.invoke(processPurchases(purchasesList))
      } else {
        _statusMessage.value = "Aktif abonelikler kontrol edilemedi."
        onComplete?.invoke(false)
      }
    }
  }

  /**
   * Starts the real Google Play purchase screen.
   * It waits for ProductDetails and an actual offer token before calling launchBillingFlow.
   */
  fun launchPurchase(activity: Activity, planType: String) {
    if (!billingClient.isReady) {
      _statusMessage.value = "Google Play bağlantısı kuruluyor..."
      startBillingConnection { launchPurchase(activity, planType) }
      return
    }

    val details = proProductDetails
    val wantsYearly = planType.equals(BASE_PLAN_YEARLY, ignoreCase = true) ||
      YEARLY_ALIASES.any { planType.contains(it, ignoreCase = true) }

    val token = if (wantsYearly) yearlyOfferToken else monthlyOfferToken

    if (details == null || token == null) {
      _statusMessage.value = "Google Play abonelik bilgileri alınıyor..."
      queryProductDetails {
        activity.runOnUiThread {
          launchPurchase(activity, planType)
        }
      }
      return
    }

    val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
      .setProductDetails(details)
      .setOfferToken(token)
      .build()

    val billingFlowParams = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(listOf(productDetailsParams))
      .build()

    val result = billingClient.launchBillingFlow(activity, billingFlowParams)
    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
      Log.e(TAG, "launchBillingFlow failed: ${result.responseCode}, ${result.debugMessage}")
      _statusMessage.value = "Satın alma ekranı açılamadı: ${result.debugMessage}"
    } else {
      _statusMessage.value = null
    }
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
    when (billingResult.responseCode) {
      BillingClient.BillingResponseCode.OK -> {
        if (!purchases.isNullOrEmpty() && processPurchases(purchases)) {
          _statusMessage.value = "Arı Takip PRO aktif."
        }
      }
      BillingClient.BillingResponseCode.USER_CANCELED -> {
        _statusMessage.value = "Satın alma işlemi iptal edildi."
      }
      BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
        queryActivePurchases { active ->
          _statusMessage.value = if (active) "Aboneliğiniz zaten aktif." else "Abonelik durumu kontrol edilemedi."
        }
      }
      else -> {
        _statusMessage.value = "Satın alma tamamlanamadı (${billingResult.responseCode}): ${billingResult.debugMessage}"
      }
    }
  }

  private fun processPurchases(purchases: List<Purchase>): Boolean {
    var hasActivePro = false

    for (purchase in purchases) {
      if (purchase.products.contains(PRODUCT_ID_PRO) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        hasActivePro = true

        if (!purchase.isAcknowledged) {
          val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
          billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
              Log.d(TAG, "Purchase acknowledged")
            } else {
              Log.w(TAG, "Purchase acknowledgement failed: ${result.responseCode}")
            }
          }
        }
      }
    }

    _isPro.value = hasActivePro
    PlanManager.setTestSubscriptionTier(if (hasActivePro) SubscriptionTier.PRO else SubscriptionTier.FREE)
    return hasActivePro
  }

  fun restorePurchases(onComplete: (isPro: Boolean, message: String) -> Unit) {
    if (!billingClient.isReady) {
      startBillingConnection { restorePurchases(onComplete) }
      return
    }

    queryActivePurchases { isPro ->
      val msg = if (isPro) {
        "Aktif Arı Takip PRO aboneliğiniz başarıyla geri yüklendi!"
      } else {
        "Bu Google Play hesabına ait aktif bir PRO aboneliği bulunamadı."
      }
      _statusMessage.value = msg
      onComplete(isPro, msg)
    }
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }
}
