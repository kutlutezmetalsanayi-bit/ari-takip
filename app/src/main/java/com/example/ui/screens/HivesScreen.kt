package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.local.entity.HiveEntity
import com.example.data.local.entity.InspectionEntity
import com.example.ui.components.AppBrandHeader
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProUpgradeDialog
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

fun getQueenColorForYear(year: Int): Color {
  return when (year % 5) {
    1 -> Color(0xFFF1F5F9) // White (2021, 2026)
    2 -> Color(0xFFFACC15) // Yellow (2022, 2027)
    3 -> Color(0xFFEF4444) // Red (2023, 2028)
    4 -> Color(0xFF22C55E) // Green (2024, 2029)
    0 -> Color(0xFF3B82F6) // Blue (2025, 2030)
    else -> Color(0xFFFACC15)
  }
}

@Composable
fun HivesScreen(
  viewModel: BeeViewModel,
  onNavigateToHiveDetail: (String) -> Unit,
  onNavigateToAddHive: (String?) -> Unit
) {
  val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
  val filteredHives by viewModel.filteredHives.collectAsStateWithLifecycle()
  val selectedApiaryId by viewModel.selectedApiaryId.collectAsStateWithLifecycle()
  val hiveSearchQuery by viewModel.hiveSearchQuery.collectAsStateWithLifecycle()
  val showArchivedHives by viewModel.showArchivedHives.collectAsStateWithLifecycle()
  val strengthFilter by viewModel.strengthFilter.collectAsStateWithLifecycle()
  val inspections by viewModel.inspections.collectAsStateWithLifecycle()
  val allHives by viewModel.allHives.collectAsStateWithLifecycle()

  var isSearchExpanded by remember { mutableStateOf(false) }
  var showProDialog by remember { mutableStateOf(false) }

  val activeHivesCount = remember(allHives) { allHives.count { it.status == "active" } }
  val onAddHiveClick: () -> Unit = {
    if (!PlanManager.canAddActiveHive(activeHivesCount)) {
      showProDialog = true
    } else {
      onNavigateToAddHive(selectedApiaryId)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("hives_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      item {
        AppBrandHeader(
          pageSubtitle = "Arı Takip — Kovanlar",
          actions = {
            IconButton(
              onClick = { isSearchExpanded = !isSearchExpanded },
              modifier = Modifier.testTag("toggle_search_btn")
            ) {
              Icon(Icons.Filled.Search, contentDescription = "Ara", tint = HoneyGoldDark)
            }
          }
        )
      }

      // Fast in-memory search input
      if (isSearchExpanded || hiveSearchQuery.isNotBlank()) {
        item {
          OutlinedTextField(
            value = hiveSearchQuery,
            onValueChange = { viewModel.setHiveSearchQuery(it) },
            placeholder = { Text("Kovan no (örn: 17), ırk, tip veya not ara...") },
            trailingIcon = {
              if (hiveSearchQuery.isNotBlank()) {
                IconButton(onClick = { viewModel.setHiveSearchQuery("") }) {
                  Icon(Icons.Filled.Clear, contentDescription = "Temizle")
                }
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = HoneyGoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .testTag("hive_search_input")
          )
        }
      }

      // Apiary selector filter row
      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            HivesFilterChip(
              label = "🐝 Tüm Arılıklar",
              isSelected = selectedApiaryId == null,
              onClick = { viewModel.setSelectedApiary(null) },
              testTag = "hive_filter_all"
            )
          }
          items(apiaries) { apiary ->
            HivesFilterChip(
              label = apiary.name,
              isSelected = selectedApiaryId == apiary.id,
              onClick = { viewModel.setSelectedApiary(apiary.id) },
              testTag = "hive_filter_${apiary.id}"
            )
          }
        }
      }

      // Colony strength filters row
      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          item {
            HivesSmallFilterChip(
              label = "Tüm Güçler",
              isSelected = strengthFilter == null,
              onClick = { viewModel.setStrengthFilter(null) }
            )
          }
          listOf("Çok Güçlü", "Güçlü", "Orta", "Zayıf").forEach { strength ->
            item {
              HivesSmallFilterChip(
                label = strength,
                isSelected = strengthFilter == strength,
                onClick = {
                  if (strengthFilter == strength) viewModel.setStrengthFilter(null)
                  else viewModel.setStrengthFilter(strength)
                }
              )
            }
          }
        }
      }

      // Archive toggle row
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "${filteredHives.size} Kovan Listeleniyor",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Arşivdekileri Göster",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Switch(
              checked = showArchivedHives,
              onCheckedChange = { viewModel.setShowArchivedHives(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HoneyGoldPrimary,
                checkedTrackColor = HoneyAmberContainer
              ),
              modifier = Modifier.testTag("show_archived_switch")
            )
          }
        }
      }

      if (filteredHives.isEmpty()) {
        item {
          EmptyStateCard(
            title = if (hiveSearchQuery.isNotBlank() || strengthFilter != null) "Arama kriterine uygun kovan bulunamadı" else "Henüz kovan eklenmemiş",
            description = if (hiveSearchQuery.isNotBlank() || strengthFilter != null) "Filtreleri sıfırlayarak tüm kovanları görüntüleyebilirsiniz." else "Seçili arılığa yeni kovan ekleyerek takibe başlayın.",
            buttonText = "+ Yeni Kovan Ekle",
            iconEmoji = "🐝",
            onButtonClick = onAddHiveClick,
            testTag = "hives_empty_card"
          )
        }
      } else {
        items(
          items = filteredHives,
          key = { it.id }
        ) { hive ->
          val apiary = apiaries.find { it.id == hive.apiaryId }
          val hiveInspections = inspections.filter { it.hiveId == hive.id }
          val latestInspection = hiveInspections.maxByOrNull { it.inspectionDate }

          HiveCardItem(
            hive = hive,
            apiaryName = apiary?.name ?: "Arılık",
            latestInspection = latestInspection,
            onClick = { onNavigateToHiveDetail(hive.id) }
          )
        }
      }
    }

    FloatingActionButton(
      onClick = onAddHiveClick,
      containerColor = HoneyGoldPrimary,
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 80.dp)
        .testTag("add_hive_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = "Yeni Kovan Ekle", modifier = Modifier.size(28.dp))
    }

    if (showProDialog) {
      ProUpgradeDialog(
        reason = "Ücretsiz kullanım sınırına ulaştınız.\nÜcretsiz planda bir arılıkta en fazla 10 aktif kovan kullanabilirsiniz.\n11. kovanı eklemek için Arı Takip PRO'ya yükseltin.",
        onDismiss = { showProDialog = false }
      )
    }
  }
}

@Composable
fun HiveCardItem(
  hive: HiveEntity,
  apiaryName: String,
  latestInspection: InspectionEntity?,
  onClick: () -> Unit
) {
  val isArchived = hive.status == "archived"
  val queenColor = getQueenColorForYear(hive.queenYear)
  val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale("tr", "TR")) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isArchived) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(if (isArchived) 0.dp else 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clickable { onClick() }
      .testTag("hive_card_${hive.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(if (isArchived) EarthWoodContainer else HoneyAmberContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "🐝", fontSize = 24.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Kovan ${hive.hiveNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isArchived) EarthWoodContainer else ForestGreenContainer
              ) {
                Text(
                  text = if (isArchived) "📦 Arşiv" else "🟢 Aktif",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant else ForestOnGreenContainer,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "🏡 $apiaryName",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Colony Strength badge
        StrengthBadge(strength = hive.colonyStrength)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Metadata details chips (with Queen Color dot)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Queen color marking circle
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(queenColor)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "👑 Ana: ${hive.queenYear} ${hive.queenBreed}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "🪵 ${hive.hiveType}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      // Last inspection summary snapshot (V1.1 Section 8)
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          if (latestInspection != null) {
            val dateStr = dateFormat.format(Date(latestInspection.inspectionDate))
            val queenStr = if (latestInspection.queenSeen) "👑 Ana Görüldü" else "⚪ Ana Görülmedi"
            Text(
              text = "🔍 Son Kontrol: $dateStr • $queenStr • 🍯 Bal: ${latestInspection.honeyStatus}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            Text(
              text = "🔍 Henüz kontrol kaydı yok",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }
      }
    }
  }
}

@Composable
fun StrengthBadge(strength: String) {
  val (bgColor, textColor) = when (strength) {
    "Çok Güçlü" -> ForestGreenContainer to ForestOnGreenContainer
    "Güçlü" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
    "Orta" -> HoneyAmberContainer to HoneyOnAmberContainer
    else -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor
  ) {
    Text(
      text = "💪 $strength",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun HivesFilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Text(
      text = label,
      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      fontSize = 13.sp,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
  }
}

@Composable
fun HivesSmallFilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = if (isSelected) ForestGreenSecondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable { onClick() }
  ) {
    Text(
      text = label,
      color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      fontSize = 11.sp,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}
