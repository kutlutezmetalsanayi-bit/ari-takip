package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.billing.PlanManager
import com.example.data.local.entity.ApiaryEntity
import com.example.data.local.entity.FeedingEntity
import com.example.data.local.entity.HiveEntity
import com.example.data.local.entity.InspectionEntity
import com.example.ui.components.AppBrandHeader
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.components.WeatherCard
import com.example.ui.theme.EarthWoodContainer
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import com.example.ui.viewmodel.BeeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: BeeViewModel,
  onNavigateToApiaries: () -> Unit,
  onNavigateToHives: () -> Unit,
  onNavigateToAddHive: (String?) -> Unit,
  onNavigateToHiveDetail: (String) -> Unit,
  onNavigateToInspection: (String) -> Unit,
  onNavigateToFeeding: (String) -> Unit
) {
  val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
  val allHives by viewModel.allHives.collectAsStateWithLifecycle()
  val inspections by viewModel.inspections.collectAsStateWithLifecycle()
  val feedings by viewModel.feedings.collectAsStateWithLifecycle()
  val selectedApiaryId by viewModel.selectedApiaryId.collectAsStateWithLifecycle()
  val weatherMap by viewModel.weatherMap.collectAsStateWithLifecycle()
  val isWeatherLoading by viewModel.isWeatherLoading.collectAsStateWithLifecycle()

  val activeHives = allHives.filter { it.status == "active" }
  val archivedHives = allHives.filter { it.status == "archived" }

  var showProDialog by remember { mutableStateOf(false) }

  val onAddHiveClick: () -> Unit = {
    if (!PlanManager.canAddActiveHive(activeHives.size)) {
      showProDialog = true
    } else {
      onNavigateToAddHive(selectedApiaryId)
    }
  }

  val activeSelectedApiary = apiaries.find { it.id == selectedApiaryId } ?: apiaries.firstOrNull()
  val currentWeather = activeSelectedApiary?.let { weatherMap[it.id] }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    item {
      AppBrandHeader(
        pageSubtitle = "Arı Takip — Ana Sayfa",
        actions = {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = HoneyAmberContainer,
            modifier = Modifier
              .clickable { onNavigateToApiaries() }
              .testTag("dashboard_apiary_count_badge")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "🏡 ", fontSize = 12.sp)
              Text(
                text = "${apiaries.size} Arılık",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HoneyOnAmberContainer
              )
            }
          }
        }
      )
    }

    // Apiary Filter Bar
    if (apiaries.isNotEmpty()) {
      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            val isAll = selectedApiaryId == null
            FilterChipItem(
              label = "🐝 Tüm Arılıklar (${activeHives.size} Kovan)",
              isSelected = isAll,
              onClick = { viewModel.setSelectedApiary(null) },
              testTag = "filter_all_apiaries"
            )
          }
          items(apiaries) { apiary ->
            val isSelected = selectedApiaryId == apiary.id
            val apiaryHiveCount = activeHives.count { it.apiaryId == apiary.id }
            FilterChipItem(
              label = "${apiary.name} ($apiaryHiveCount)",
              isSelected = isSelected,
              onClick = { viewModel.setSelectedApiary(apiary.id) },
              testTag = "filter_apiary_${apiary.id}"
            )
          }
        }
      }
    }

    // Weather widget
    if (activeSelectedApiary != null) {
      item {
        val locationTitle = if (activeSelectedApiary.district.isNotBlank() && activeSelectedApiary.city.isNotBlank()) {
          "${activeSelectedApiary.name} (${activeSelectedApiary.district}, ${activeSelectedApiary.city})"
        } else if (activeSelectedApiary.city.isNotBlank()) {
          "${activeSelectedApiary.name} (${activeSelectedApiary.city})"
        } else {
          activeSelectedApiary.name
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          WeatherCard(
            weather = currentWeather,
            apiaryName = locationTitle,
            isLoading = isWeatherLoading,
            onRefresh = { viewModel.refreshWeather(activeSelectedApiary) }
          )
        }
      }
    }

    // Metric summary stats cards (Section 5)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatCard(
          title = "Arılık",
          count = apiaries.size.toString(),
          subtitle = "Kayıtlı",
          iconEmoji = "🏡",
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToApiaries,
          testTag = "stat_apiaries"
        )
        StatCard(
          title = "Kovan",
          count = allHives.size.toString(),
          subtitle = "Toplam",
          iconEmoji = "🐝",
          containerColor = HoneyAmberContainer,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToHives,
          testTag = "stat_total_hives"
        )
        StatCard(
          title = "Aktif",
          count = activeHives.size.toString(),
          subtitle = "Üretimde",
          iconEmoji = "🟢",
          containerColor = ForestGreenContainer,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToHives,
          testTag = "stat_active_hives"
        )
        StatCard(
          title = "Arşiv",
          count = archivedHives.size.toString(),
          subtitle = "Geçmiş",
          iconEmoji = "📦",
          containerColor = EarthWoodContainer,
          modifier = Modifier.weight(1f),
          onClick = onNavigateToHives,
          testTag = "stat_archived_hives"
        )
      }
    }

    // Quick Action Buttons (Section 5: Outdoor friendly big buttons)
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
          text = "⚡ Hızlı İşlemler",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionButton(
            label = "+ KOVAN EKLE",
            icon = Icons.Filled.Add,
            backgroundColor = HoneyGoldPrimary,
            contentColor = Color.White,
            modifier = Modifier.weight(1f),
            onClick = onAddHiveClick,
            testTag = "quick_add_hive_btn"
          )
          QuickActionButton(
            label = "🔍 KONTROL",
            icon = Icons.Filled.Search,
            backgroundColor = ForestGreenSecondary,
            contentColor = Color.White,
            modifier = Modifier.weight(1f),
            onClick = {
              val firstHive = activeHives.firstOrNull()
              if (firstHive != null) {
                onNavigateToInspection(firstHive.id)
              } else {
                onNavigateToHives()
              }
            },
            testTag = "quick_inspection_btn"
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionButton(
            label = "🍯 BESLEME",
            icon = Icons.Filled.Fastfood,
            backgroundColor = HoneyGoldDark,
            contentColor = Color.White,
            modifier = Modifier.weight(1f),
            onClick = {
              val firstHive = activeHives.firstOrNull()
              if (firstHive != null) {
                onNavigateToFeeding(firstHive.id)
              } else {
                onNavigateToHives()
              }
            },
            testTag = "quick_feeding_btn"
          )
          QuickActionButton(
            label = "📷 FOTOĞRAF",
            icon = Icons.Filled.CameraAlt,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            onClick = onNavigateToHives,
            testTag = "quick_photo_btn"
          )
        }
      }
    }

    // Empty state if no apiaries or hives
    if (apiaries.isEmpty()) {
      item {
        EmptyStateCard(
          title = "Henüz arılık eklenmemiş",
          description = "Arılıklarınızı ve kovanlarınızı yönetmeye başlamak için ilk arılığınızı oluşturun veya hazır demo verisini yükleyin.",
          buttonText = "+ İlk Arılığını Ekle",
          iconEmoji = "🏡",
          onButtonClick = onNavigateToApiaries,
          testTag = "dashboard_empty_apiary_card"
        )
      }
    } else if (activeHives.isEmpty()) {
      item {
        EmptyStateCard(
          title = "Henüz kovan eklenmemiş",
          description = "${activeSelectedApiary?.name ?: "Arılığınıza"} ilk kovanınızı ekleyin. Sistem kovan numarasını otomatik olarak Kovan 1 şeklinde verecektir.",
          buttonText = "+ İlk Kovanını Ekle",
          iconEmoji = "🐝",
          onButtonClick = onAddHiveClick,
          testTag = "dashboard_empty_hive_card"
        )
      }
    }

    // Attention Needed Section (Section 5: Dikkat Gerekenler)
    val weakHives = activeHives.filter { it.colonyStrength == "Zayıf" }
    if (weakHives.isNotEmpty()) {
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Warning,
              contentDescription = "Dikkat",
              tint = Color(0xFFE11D48),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "⚠️ Dikkat Gereken Kovanlar",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          weakHives.forEach { hive ->
            val apiary = apiaries.find { it.id == hive.apiaryId }
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onNavigateToHiveDetail(hive.id) }
                .testTag("attention_hive_${hive.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "🐝 Kovan ${hive.hiveNumber} (${apiary?.name ?: "Arılık"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF9F1239)
                  )
                  Text(
                    text = "Koloni Gücü: Zayıf • Teşvik beslemesi veya ana arı kontrolü önerilir.",
                    fontSize = 12.sp,
                    color = Color(0xFF881337)
                  )
                }
                Text(
                  text = "İncele →",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFFBE123C)
                )
              }
            }
          }
        }
      }
    }

    // Recent Activities (Section 5: Son İşlemler)
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "📋 Son Yapılan İşlemler",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Tümü",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = HoneyGoldDark,
            modifier = Modifier.clickable { onNavigateToHives() }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    val recentActivities = buildList {
      inspections.take(5).forEach { insp ->
        add(Triple("KONTROL", insp.inspectionDate, insp))
      }
      feedings.take(5).forEach { feed ->
        add(Triple("BESLEME", feed.feedingDate, feed))
      }
    }.sortedByDescending { it.second }.take(6)

    if (recentActivities.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Text(
            text = "Henüz kontrol veya besleme kaydı girilmedi.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(recentActivities) { item ->
        val (type, timestamp, obj) = item
        val timeStr = formatActivityTimestamp(timestamp)

        if (type == "KONTROL" && obj is InspectionEntity) {
          val hive = allHives.find { it.id == obj.hiveId }
          RecentActivityRow(
            emoji = "🔍",
            title = "Kovan ${hive?.hiveNumber ?: "?"} — Kontrol",
            subtitle = if (obj.queenSeen) "Ana arı görüldü • ${obj.colonyStrength}" else "Ana arı görülmedi • ${obj.colonyStrength}",
            timeText = timeStr,
            tagColor = ForestGreenContainer,
            tagTextColor = ForestOnGreenContainer,
            onClick = { hive?.let { onNavigateToHiveDetail(it.id) } }
          )
        } else if (type == "BESLEME" && obj is FeedingEntity) {
          val hive = allHives.find { it.id == obj.hiveId }
          RecentActivityRow(
            emoji = "🍯",
            title = "Kovan ${hive?.hiveNumber ?: "?"} — Besleme",
            subtitle = "${obj.amount} ${obj.unit} ${obj.feedingType}",
            timeText = timeStr,
            tagColor = HoneyAmberContainer,
            tagTextColor = HoneyOnAmberContainer,
            onClick = { hive?.let { onNavigateToHiveDetail(it.id) } }
          )
        }
      }
    }
  }

  if (showProDialog) {
    ProUpgradeDialog(
      reason = "Ücretsiz kullanım sınırına ulaştınız.\nÜcretsiz planda bir arılıkta en fazla 10 aktif kovan kullanabilirsiniz.\n11. kovanı eklemek için Arı Takip PRO'ya yükseltin.",
      onDismiss = { showProDialog = false }
    )
  }
}

@Composable
fun FilterChipItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(20.dp))
      .background(if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.surface)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
      fontSize = 13.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
    )
  }
}

@Composable
fun StatCard(
  title: String,
  count: String,
  subtitle: String,
  iconEmoji: String,
  containerColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    elevation = CardDefaults.cardElevation(1.dp),
    modifier = modifier
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = iconEmoji, fontSize = 20.sp)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = count,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun QuickActionButton(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  backgroundColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = backgroundColor,
    shadowElevation = 2.dp,
    modifier = modifier
      .height(56.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = contentColor,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        color = contentColor,
        fontWeight = FontWeight.Black,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun RecentActivityRow(
  emoji: String,
  title: String,
  subtitle: String,
  timeText: String,
  tagColor: Color,
  tagTextColor: Color,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(tagColor),
          contentAlignment = Alignment.Center
        ) {
          Text(text = emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
      Text(
        text = timeText,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun formatActivityTimestamp(timestamp: Long): String {
  val now = System.currentTimeMillis()
  val diff = now - timestamp
  val dayMillis = 86400000L
  val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

  return when {
    diff < dayMillis && Date(now).date == Date(timestamp).date -> "Bugün ${timeFormat.format(Date(timestamp))}"
    diff < 2 * dayMillis -> "Dün ${timeFormat.format(Date(timestamp))}"
    else -> SimpleDateFormat("d MMM", Locale("tr", "TR")).format(Date(timestamp))
  }
}
