package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.billing.GooglePlayBillingManager
import com.example.data.billing.PlanManager
import com.example.data.billing.SubscriptionTier
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer

private fun Context.findActivity(): Activity? {
  var currentContext = this
  while (currentContext is ContextWrapper) {
    if (currentContext is Activity) return currentContext
    currentContext = currentContext.baseContext
  }
  return null
}

@Composable
fun ProUpgradeDialog(
  reason: String? = null,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val activity = remember(context) { context.findActivity() }
  val billingManager = remember { GooglePlayBillingManager.getInstance(context) }

  val currentTier by PlanManager.currentTier.collectAsStateWithLifecycle()
  val isPro = currentTier == SubscriptionTier.PRO

  val monthlyPriceFormatted by billingManager.monthlyPrice.collectAsStateWithLifecycle()
  val yearlyPriceFormatted by billingManager.yearlyPrice.collectAsStateWithLifecycle()
  val statusMessage by billingManager.statusMessage.collectAsStateWithLifecycle()

  var selectedProductIndex by remember { mutableIntStateOf(1) } // Default to annual
  var isRestoring by remember { mutableStateOf(false) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(8.dp),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .clip(RoundedCornerShape(24.dp))
        .testTag("pro_upgrade_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Top Bar with Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isPro) ForestGreenContainer else HoneyAmberContainer
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (isPro) ForestGreenSecondary else HoneyGoldDark,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isPro) "PRO AKTİF" else "PRO'YA YÜKSELT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isPro) ForestOnGreenContainer else HoneyOnAmberContainer
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_pro_dialog_btn")
          ) {
            Icon(Icons.Filled.Close, contentDescription = "Kapat")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hero Header Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
              Brush.horizontalGradient(
                if (isPro) listOf(ForestGreenSecondary, Color(0xFF15803D))
                else listOf(HoneyGoldDark, HoneyGoldPrimary)
              )
            )
            .padding(18.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = if (isPro) "👑 ARI TAKİP PRO AKTİF" else "🐝 ARI TAKİP PRO",
              fontSize = 22.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isPro) "Sınırsız arılık ve kovan yönetiminiz aktif."
              else "Arılıklarınızı ve kovanlarınızı daha özgür yönetin.",
              fontSize = 13.sp,
              color = Color.White.copy(alpha = 0.95f),
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center
            )
          }
        }

        if (!reason.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(14.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFFFBEB),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "⚠️ $reason",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF92400E),
              modifier = Modifier.padding(12.dp)
            )
          }
        }

        if (statusMessage != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isPro) ForestGreenContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = statusMessage ?: "",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPro) ForestOnGreenContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = { billingManager.clearStatusMessage() },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Filled.Close, contentDescription = "Kapat", modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Comparison Matrix
        Text(
          text = "📋 Plan Özellikleri",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ComparisonRow(
            feature = "🏡 Arılık Yönetimi",
            freeValue = "1 Arılık",
            proValue = "Birden Fazla Arılık"
          )
          ComparisonRow(
            feature = "🐝 Aktif Kovan",
            freeValue = "10 Aktif Kovan",
            proValue = "Daha Fazla Aktif Kovan"
          )
          ComparisonRow(
            feature = "📊 Raporlar & Analizler",
            freeValue = "Temel Takip",
            proValue = "Gelecekteki PRO Özellikleri"
          )
          ComparisonRow(
            feature = "☁️ Çevrimdışı & Bulut",
            freeValue = "Tam Destek",
            proValue = "Tam Destek & Öncelikli"
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subscription Pricing Options
        Text(
          text = "💳 Abonelik Paketleri",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Monthly Card
          val isMonthlySelected = selectedProductIndex == 0
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isMonthlySelected) HoneyAmberContainer else MaterialTheme.colorScheme.surface
            ),
            border = if (isMonthlySelected) androidx.compose.foundation.BorderStroke(2.dp, HoneyGoldPrimary)
            else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
              .weight(1f)
              .clickable { selectedProductIndex = 0 }
              .testTag("monthly_plan_card")
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "Aylık Plan",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = monthlyPriceFormatted,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = HoneyGoldDark
              )
            }
          }

          // Yearly Card (Default)
          val isYearlySelected = selectedProductIndex == 1
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isYearlySelected) HoneyAmberContainer else MaterialTheme.colorScheme.surface
            ),
            border = if (isYearlySelected) androidx.compose.foundation.BorderStroke(2.dp, HoneyGoldPrimary)
            else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
              .weight(1f)
              .clickable { selectedProductIndex = 1 }
              .testTag("yearly_plan_card")
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = HoneyGoldDark
              ) {
                Text(
                  text = "AVANTAJLI YILLIK",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Yıllık Plan",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = yearlyPriceFormatted,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = HoneyGoldDark
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main CTA Button (Real Google Play launchBillingFlow)
        Button(
          onClick = {
            if (activity != null) {
              val planType = if (selectedProductIndex == 0) GooglePlayBillingManager.BASE_PLAN_MONTHLY
              else GooglePlayBillingManager.BASE_PLAN_YEARLY
              billingManager.launchPurchase(activity, planType)
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isPro) ForestGreenSecondary else HoneyGoldDark
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("upgrade_to_pro_btn")
        ) {
          Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isPro) "⭐ GOOGLE PLAY'DE YÖNET" else "⭐ PRO'YA YÜKSELT",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Restore Purchases Button
        OutlinedButton(
          onClick = {
            isRestoring = true
            billingManager.restorePurchases { _, _ ->
              isRestoring = false
            }
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("restore_purchases_btn")
        ) {
          if (isRestoring) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
          } else {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
          }
          Text(
            text = "Satın Alımları Geri Yükle / PRO Kontrol Et",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        TextButton(
          onClick = onDismiss,
          modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
          Text("Kapat", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
  }
}

@Composable
private fun ComparisonRow(
  feature: String,
  freeValue: String,
  proValue: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = feature,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1.2f)
    )
    Text(
      text = freeValue,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.9f),
      textAlign = TextAlign.Center
    )
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = ForestGreenContainer
    ) {
      Text(
        text = proValue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = ForestOnGreenContainer,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}
