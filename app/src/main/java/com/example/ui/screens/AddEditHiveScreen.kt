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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.LargeOutdoorChipGroup
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import com.example.ui.viewmodel.BeeViewModel

val HIVE_TYPES = listOf("Langstroth", "Dadant", "Karakovan", "Top Bar", "Diğer")
val QUEEN_BREEDS = listOf("Kafkas", "Karniyol", "İtalyan", "Belfast (Buckfast)", "Anadolu", "Muğla Yerli", "Belli Değil")
val COLONY_STRENGTHS = listOf("Zayıf", "Orta", "Güçlü", "Çok Güçlü")
val QUEEN_YEARS = listOf(2026, 2025, 2024, 2023, 2022)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHiveScreen(
  viewModel: BeeViewModel,
  hiveId: String?,
  preselectedApiaryId: String?,
  onNavigateBack: () -> Unit,
  onHiveSaved: (String) -> Unit
) {
  val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
  val allHives by viewModel.allHives.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

  val existingHive = remember(hiveId, allHives) {
    if (!hiveId.isNullOrEmpty()) allHives.find { it.id == hiveId } else null
  }

  var selectedApiaryId by remember {
    mutableStateOf(
      existingHive?.apiaryId ?: preselectedApiaryId ?: apiaries.firstOrNull()?.id ?: ""
    )
  }

  // Update selectedApiaryId if apiaries list loads late
  LaunchedEffect(apiaries) {
    if (selectedApiaryId.isBlank() && apiaries.isNotEmpty()) {
      selectedApiaryId = apiaries.first().id
    }
  }

  var hiveType by remember { mutableStateOf(existingHive?.hiveType ?: "Langstroth") }
  var queenBreed by remember { mutableStateOf(existingHive?.queenBreed ?: "Kafkas") }
  var queenYear by remember { mutableIntStateOf(existingHive?.queenYear ?: 2025) }
  var colonyStrength by remember { mutableStateOf(existingHive?.colonyStrength ?: "Güçlü") }
  var notes by remember { mutableStateOf(existingHive?.notes ?: "") }
  var apiaryDropdownExpanded by remember { mutableStateOf(false) }
  var showProDialog by remember { mutableStateOf(false) }

  val isEditing = existingHive != null

  // Calculate next preview hive number for the selected apiary if creating new
  val nextPreviewNumber = remember(selectedApiaryId, allHives) {
    val apiaryHives = allHives.filter { it.apiaryId == selectedApiaryId }
    val maxNum = apiaryHives.maxOfOrNull { it.hiveNumber } ?: 0
    maxNum + 1
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("add_edit_hive_screen"),
    contentPadding = PaddingValues(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("hive_back_btn")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = if (isEditing) "Kovan ${existingHive?.hiveNumber} Düzenle" else "Yeni Kovan Ekle",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = HoneyGoldDark
          )
          Text(
            text = "Arı Takip — Kovan Yönetimi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Auto Hive Number Banner (Section 8: MANDATORY RULE)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HoneyAmberContainer),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(HoneyGoldPrimary),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.Lock, contentDescription = "Kilitli", tint = Color.White)
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column {
            Text(
              text = if (isEditing) "🔒 Kovan ${existingHive?.hiveNumber}" else "🔒 Otomatik Kovan No: Kovan $nextPreviewNumber",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = HoneyOnAmberContainer
            )
            Text(
              text = "Kovan numarası sistem tarafından arılık bazında sıradan otomatik verilir ve değiştirilemez.",
              fontSize = 12.sp,
              color = HoneyOnAmberContainer.copy(alpha = 0.8f)
            )
          }
        }
      }
    }

    // Apiary Selection Dropdown
    item {
      Text(
        text = "🏡 Arılık Seçimi",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )
      ExposedDropdownMenuBox(
        expanded = apiaryDropdownExpanded,
        onExpandedChange = { apiaryDropdownExpanded = !apiaryDropdownExpanded },
        modifier = Modifier.fillMaxWidth()
      ) {
        val selectedApiaryName = apiaries.find { it.id == selectedApiaryId }?.name ?: "Arılık Seçiniz"
        OutlinedTextField(
          value = selectedApiaryName,
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apiaryDropdownExpanded) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HoneyGoldPrimary
          ),
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor()
            .testTag("apiary_select_dropdown")
        )
        ExposedDropdownMenu(
          expanded = apiaryDropdownExpanded,
          onDismissRequest = { apiaryDropdownExpanded = false }
        ) {
          apiaries.forEach { apiary ->
            DropdownMenuItem(
              text = { Text(apiary.name) },
              onClick = {
                selectedApiaryId = apiary.id
                apiaryDropdownExpanded = false
              }
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Hive Type Selection
    item {
      LargeOutdoorChipGroup(
        title = "🪵 Kovan Tipi",
        options = HIVE_TYPES,
        selectedOption = hiveType,
        onOptionSelected = { hiveType = it },
        tagPrefix = "type"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Colony Strength Selection
    item {
      LargeOutdoorChipGroup(
        title = "💪 Koloni Gücü",
        options = COLONY_STRENGTHS,
        selectedOption = colonyStrength,
        onOptionSelected = { colonyStrength = it },
        tagPrefix = "strength"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Queen Breed Selection
    item {
      LargeOutdoorChipGroup(
        title = "👑 Ana Arı Irkı",
        options = QUEEN_BREEDS,
        selectedOption = queenBreed,
        onOptionSelected = { queenBreed = it },
        tagPrefix = "breed"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Queen Year Selection
    item {
      LargeOutdoorChipGroup(
        title = "📅 Ana Arı Yılı",
        options = QUEEN_YEARS.map { it.toString() },
        selectedOption = queenYear.toString(),
        onOptionSelected = { queenYear = it.toIntOrNull() ?: 2025 },
        tagPrefix = "year"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Notes
    item {
      Text(
        text = "📝 Kovan Notları",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        placeholder = { Text("Örn: 2 Katlı üretim kovanı, ızgara takılı, oğul eğilimi yok") },
        minLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = HoneyGoldPrimary
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hive_notes_input")
      )
      Spacer(modifier = Modifier.height(24.dp))
    }

    // Save Button with Double-Click Prevention
    item {
      Button(
        onClick = {
          if (selectedApiaryId.isBlank() || isSaving) return@Button
          if (isEditing && existingHive != null) {
            viewModel.updateHive(
              hiveId = existingHive.id,
              apiaryId = selectedApiaryId,
              hiveType = hiveType,
              queenYear = queenYear,
              queenBreed = queenBreed,
              colonyStrength = colonyStrength,
              notes = notes,
              onSuccess = { onHiveSaved(existingHive.id) }
            )
          } else {
            val activeHivesCount = allHives.count { it.status == "active" }
            if (!PlanManager.canAddActiveHive(activeHivesCount)) {
              showProDialog = true
              return@Button
            }

            viewModel.createHive(
              apiaryId = selectedApiaryId,
              hiveType = hiveType,
              queenYear = queenYear,
              queenBreed = queenBreed,
              colonyStrength = colonyStrength,
              notes = notes,
              onSuccess = { newHive -> onHiveSaved(newHive.id) },
              onLimitReached = { showProDialog = true }
            )
          }
        },
        enabled = !isSaving && selectedApiaryId.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("save_hive_submit_btn")
      ) {
        if (isSaving) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Kaydediliyor...", fontWeight = FontWeight.Black, fontSize = 16.sp)
        } else {
          Icon(Icons.Filled.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isEditing) "Kovanı Güncelle" else "Kovanı Oluştur (Kovan $nextPreviewNumber)",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
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
