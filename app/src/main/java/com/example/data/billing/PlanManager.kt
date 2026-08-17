package com.example.data.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SubscriptionTier {
  FREE,
  PRO
}

data class PlanLimits(
  val maxApiaries: Int,
  val maxHivesPerApiary: Int,
  val maxHivesTotal: Int,
  val tierName: String,
  val isUnlimited: Boolean
)

data class BillingProduct(
  val productId: String,
  val title: String,
  val description: String,
  val priceFormatted: String,
  val period: String
)

object PlanManager {
  val FREE_LIMITS = PlanLimits(
    maxApiaries = 1,
    maxHivesPerApiary = 10,
    maxHivesTotal = 10,
    tierName = "Ücretsiz Plan",
    isUnlimited = false
  )

  val PRO_LIMITS = PlanLimits(
    maxApiaries = Int.MAX_VALUE,
    maxHivesPerApiary = Int.MAX_VALUE,
    maxHivesTotal = Int.MAX_VALUE,
    tierName = "Arı Takip PRO",
    isUnlimited = true
  )

  const val PRODUCT_ID_PRO = "ari_takip_pro"
  const val BASE_PLAN_MONTHLY = "aylik"
  const val BASE_PLAN_YEARLY = "yillik"

  val AVAILABLE_PRODUCTS = listOf(
    BillingProduct(
      productId = "$PRODUCT_ID_PRO:$BASE_PLAN_MONTHLY",
      title = "Arı Takip PRO (Aylık)",
      description = "Birden fazla arılık, daha fazla aktif kovan",
      priceFormatted = "49,99 TL / Ay",
      period = "Aylık"
    ),
    BillingProduct(
      productId = "$PRODUCT_ID_PRO:$BASE_PLAN_YEARLY",
      title = "Arı Takip PRO (Yıllık)",
      description = "Yıllık plan ile daha avantajlı",
      priceFormatted = "499,99 TL / Yıl",
      period = "Yıllık"
    )
  )

  private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
  val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()

  val limits: PlanLimits
    get() = if (_currentTier.value == SubscriptionTier.PRO) PRO_LIMITS else FREE_LIMITS

  fun isPro(): Boolean = _currentTier.value == SubscriptionTier.PRO

  fun canAddApiary(currentActiveApiaryCount: Int): Boolean {
    if (isPro()) return true
    return currentActiveApiaryCount < FREE_LIMITS.maxApiaries
  }

  fun canAddActiveHive(totalActiveHiveCount: Int): Boolean {
    if (isPro()) return true
    return totalActiveHiveCount < FREE_LIMITS.maxHivesTotal
  }

  fun canAddHive(currentHiveCountInApiary: Int, totalHiveCount: Int): Boolean {
    if (isPro()) return true
    return currentHiveCountInApiary < FREE_LIMITS.maxHivesPerApiary && totalHiveCount < FREE_LIMITS.maxHivesTotal
  }

  fun setTestSubscriptionTier(tier: SubscriptionTier) {
    _currentTier.value = tier
  }
}
