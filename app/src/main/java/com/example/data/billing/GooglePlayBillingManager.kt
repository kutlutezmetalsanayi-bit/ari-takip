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

    // Alternative aliases for Play Console naming flexibility
    private val MONTHLY_ALIASES = listOf("aylik", "monthly", "aylik_plan", "aylik-plan")
    private val YEARLY_ALIASES = listOf("yillik", "yearly", "yillik_plan", "yillik-plan")

    @Volatile
    private var INSTANCE: GooglePlayBillingManager? = null

    fun getInstance(context: Context): GooglePlayBillingManager {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: GooglePlayBillingManager(context.applicationContext).also { INSTANCE = it }
      }
    }
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private var billingClient: BillingClient = BillingClient.newBuilder(appContext)
    .setListener(this)
    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
    .build()

  // PRO Entitlement state derived strictly from Google Play purchases
  private val _isPro = MutableStateFlow(false)
  val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

  // Connection state
  private val _isConnected = MutableStateFlow(false)
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  // Dynamic Pricing states fetched from Google Play Console ProductDetails
  private val _monthlyPrice = MutableStateFlow("49,99 TL / Ay")
  val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

  private val _yearlyPrice = MutableStateFlow("499,99 TL / Yıl")
  val yearlyPrice: StateFlow<String> = _yearlyPrice.asStateFlow()

  // User notifications & feedback
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
      queryProductDetails()
      queryActivePurchases()
      onConnected?.invoke()
      return
    }

    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
          Log.d(TAG, "BillingClient connected successfully.")
          _isConnected.value = true
          reconnectAttempts = 0
          queryProductDetails()
          queryActivePurchases()
          onConnected?.invoke()
        } else {
          // Response code 3 (BILLING_UNAVAILABLE) happens in emulator/development environments without Play Store
          if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            Log.i(TAG, "Google Play Billing service is not available on this device/emulator. Continuing in offline/development mode.")
          } else {
            Log.w(TAG, "Billing setup finished with code: ${billingResult.responseCode}, ${billingResult.debugMessage}")
          }
          _isConnected.value = false
        }
      }

      override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected.")
        _isConnected.value = false
        if (reconnectAttempts < maxReconnectAttempts) {
          reconnectAttempts++
          Log.d(TAG, "Attempting reconnect $reconnectAttempts of $maxReconnectAttempts...")
          startBillingConnection()
        }
      }
    })
  }

  /**
   * Queries Google Play Console for real ProductDetails (ari_takip_pro)
   * Extracts real localized prices and offerTokens for base plans (aylik / yillik).
   */
  fun queryProductDetails() {
    if (!billingClient.isReady) {
      Log.w(TAG, "BillingClient not ready for queryProductDetails.")
      return
    }

    val productList = listOf(
      QueryProductDetailsParams.Product.newBuilder()
        .setProductId(PRODUCT_ID_PRO)
        .setProductType(BillingClient.ProductType.SUBS)
        .build()
    )

    val params = QueryProductDetailsParams.newBuilder()
      .setProductList(productList)
      .build()

    billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
        val details = productDetailsList.firstOrNull { it.productId == PRODUCT_ID_PRO }
        if (details != null) {
          proProductDetails = details
          Log.d(TAG, "Found ProductDetails: ${details.title}, Sub Offers: ${details.subscriptionOfferDetails?.size}")

          details.subscriptionOfferDetails?.forEach { offer ->
            val basePlanId = offer.basePlanId.lowercase()
            val pricingPhase = offer.pricingPhases.pricingPhaseList.firstOrNull()
            val formattedPrice = pricingPhase?.formattedPrice

            val isMonthly = MONTHLY_ALIASES.any { basePlanId.contains(it) }
            val isYearly = YEARLY_ALIASES.any { basePlanId.contains(it) }

            if (isMonthly) {
              monthlyOfferToken = offer.offerToken
              if (formattedPrice != null) {
                _monthlyPrice.value = "$formattedPrice / Ay"
              }
              Log.d(TAG, "Monthly plan token resolved: $basePlanId, price: $formattedPrice")
            } else if (isYearly) {
              yearlyOfferToken = offer.offerToken
              if (formattedPrice != null) {
                _yearlyPrice.value = "$formattedPrice / Yıl"
              }
              Log.d(TAG, "Yearly plan token resolved: $basePlanId, price: $formattedPrice")
            } else {
              // Fallback: If only 1 or 2 offers exist without exact name match
              if (monthlyOfferToken == null) {
                monthlyOfferToken = offer.offerToken
                if (formattedPrice != null) _monthlyPrice.value = "$formattedPrice / Ay"
              } else if (yearlyOfferToken == null) {
                yearlyOfferToken = offer.offerToken
                if (formattedPrice != null) _yearlyPrice.value = "$formattedPrice / Yıl"
              }
            }
          }
        }
      } else {
        Log.w(TAG, "Failed to query ProductDetails: ${billingResult.responseCode}, ${billingResult.debugMessage}")
      }
    }
  }

  /**
   * Queries active subscription purchases from Google Play.
   * Runs on app launch and whenever purchase state needs verification.
   */
  fun queryActivePurchases(onComplete: ((Boolean) -> Unit)? = null) {
    if (!billingClient.isReady) {
      startBillingConnection {
        queryActivePurchases(onComplete)
      }
      return
    }

    val params = QueryPurchasesParams.newBuilder()
      .setProductType(BillingClient.ProductType.SUBS)
      .build()

    billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
        val hasActivePro = processPurchases(purchasesList)
        onComplete?.invoke(hasActivePro)
      } else {
        Log.e(TAG, "Error querying active purchases: ${billingResult.responseCode}, ${billingResult.debugMessage}")
        onComplete?.invoke(false)
      }
    }
  }

  /**
   * Launches real Google Play Billing Flow for Monthly or Yearly plan.
   */
  fun launchPurchase(activity: Activity, planType: String) {
    if (!billingClient.isReady) {
      _statusMessage.value = "Google Play bağlantısı kuruluyor, lütfen tekrar deneyin."
      startBillingConnection {
        launchPurchase(activity, planType)
      }
      return
    }

    val details = proProductDetails
    if (details == null) {
      // Re-query product details if not loaded yet
      queryProductDetails()
      _statusMessage.value = "Abonelik ürün bilgisi yükleniyor, lütfen birkaç saniye sonra tekrar deneyin."
      return
    }

    val isYearly = planType.equals(BASE_PLAN_YEARLY, ignoreCase = true) ||
      YEARLY_ALIASES.any { planType.contains(it, ignoreCase = true) }

    val offerToken = if (isYearly) {
      yearlyOfferToken ?: details.subscriptionOfferDetails?.getOrNull(1)?.offerToken
      ?: details.subscriptionOfferDetails?.firstOrNull()?.offerToken
    } else {
      monthlyOfferToken ?: details.subscriptionOfferDetails?.firstOrNull()?.offerToken
    }

    if (offerToken == null) {
      _statusMessage.value = "Abonelik planı bulunamadı. Lütfen internet bağlantınızı kontrol edin."
      return
    }

    val productDetailsParamsList = listOf(
      BillingFlowParams.ProductDetailsParams.newBuilder()
        .setProductDetails(details)
        .setOfferToken(offerToken)
        .build()
    )

    val billingFlowParams = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(productDetailsParamsList)
      .build()

    val result = billingClient.launchBillingFlow(activity, billingFlowParams)
    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
      Log.e(TAG, "launchBillingFlow failed: ${result.responseCode}, ${result.debugMessage}")
      _statusMessage.value = "Satın alma başlatılamadı: ${result.debugMessage}"
    }
  }

  /**
   * Listener triggered by Google Play when a purchase flow completes.
   */
  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
    when (billingResult.responseCode) {
      BillingClient.BillingResponseCode.OK -> {
        if (!purchases.isNullOrEmpty()) {
          val active = processPurchases(purchases)
          if (active) {
            _statusMessage.value = "Arı Takip PRO aktif."
          }
        }
      }
      BillingClient.BillingResponseCode.USER_CANCELED -> {
        Log.i(TAG, "User canceled purchase flow.")
        _statusMessage.value = "Satın alma işlemi tamamlanmadı."
      }
      BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
        Log.i(TAG, "Item already owned.")
        queryActivePurchases {
          _statusMessage.value = "Aboneliğiniz zaten aktif."
        }
      }
      else -> {
        Log.e(TAG, "onPurchasesUpdated error: ${billingResult.responseCode}, ${billingResult.debugMessage}")
        _statusMessage.value = "Satın alma tamamlanamadı (${billingResult.responseCode})."
      }
    }
  }

  /**
   * Validates and acknowledges active purchases, updates PRO entitlement.
   */
  private fun processPurchases(purchases: List<Purchase>): Boolean {
    var hasActivePro = false

    for (purchase in purchases) {
      if (purchase.products.contains(PRODUCT_ID_PRO) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
        hasActivePro = true

        // Acknowledge purchase if not already acknowledged
        if (!purchase.isAcknowledged) {
          val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

          billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
              Log.d(TAG, "Purchase successfully acknowledged: ${purchase.purchaseToken}")
            } else {
              Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.responseCode}")
            }
          }
        }
      }
    }

    _isPro.value = hasActivePro
    PlanManager.setTestSubscriptionTier(
      if (hasActivePro) SubscriptionTier.PRO else SubscriptionTier.FREE
    )
    return hasActivePro
  }

  /**
   * Restores purchases by re-querying Google Play Store.
   */
  fun restorePurchases(onComplete: (isPro: Boolean, message: String) -> Unit) {
    if (!billingClient.isReady) {
      startBillingConnection {
        restorePurchases(onComplete)
      }
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
