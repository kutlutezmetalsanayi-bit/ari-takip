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

  // Standard product IDs prepared for Google Play Billing integration
  val PRODUCT_ID_PRO = "ari_takip_pro"
  val BASE_PLAN_MONTHLY = "monthly"
  val BASE_PLAN_YEARLY = "yearly"

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
      description = "Yıllık plan ile daha avantajlı! 2 ay hediye",
      priceFormatted = "499,99 TL / Yıl",
      period = "Yıllık"
    )
  )

  // Central subscription state (Defaults to FREE)
  private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
  val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()

  val limits: PlanLimits
    get() = if (_currentTier.value == SubscriptionTier.PRO) PRO_LIMITS else FREE_LIMITS

  fun isPro(): Boolean = _currentTier.value == SubscriptionTier.PRO

  /**
   * Centralized check for whether an apiary can be created.
   * Free plan allows 1 apiary. Pro allows unlimited.
   */
  fun canAddApiary(currentActiveApiaryCount: Int): Boolean {
    if (isPro()) return true
    return currentActiveApiaryCount < FREE_LIMITS.maxApiaries
  }

  /**
   * Centralized check for whether a hive can be created.
   * Free plan allows max 10 hives in an apiary (and total). Pro allows unlimited.
   */
  /**
   * Centralized check for whether an active hive can be created.
   * Free plan allows max 10 active hives in total. Pro allows unlimited.
   * Archived hives do not count towards the 10 limit.
   */
  fun canAddActiveHive(totalActiveHiveCount: Int): Boolean {
    if (isPro()) return true
    return totalActiveHiveCount < FREE_LIMITS.maxHivesTotal
  }

  fun canAddHive(currentHiveCountInApiary: Int, totalHiveCount: Int): Boolean {
    if (isPro()) return true
    return currentHiveCountInApiary < FREE_LIMITS.maxHivesPerApiary && totalHiveCount < FREE_LIMITS.maxHivesTotal
  }

  /**
   * Test mode entitlement toggle for development and review purposes.
   * Note: Real Google Play Billing purchases will activate this via Play Store verification.
   */
  fun setTestSubscriptionTier(tier: SubscriptionTier) {
    _currentTier.value = tier
  }
}
