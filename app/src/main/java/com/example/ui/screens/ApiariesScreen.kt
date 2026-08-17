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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.TurkeyLocationData
import com.example.ui.components.AppBrandHeader
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.components.TurkeyLocationSelector
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import com.example.ui.viewmodel.BeeViewModel

data class RegionPreset(
  val name: String,
  val province: String,
  val lat: Double,
  val lon: Double
)

val TURKISH_BEEKEEPING_PRESETS = listOf(
  RegionPreset("Marmaris Çam Balı", "Muğla", 36.8550, 28.2740),
  RegionPreset("Kaz Dağları Kestane", "Balıkesir", 39.7042, 26.8505),
  RegionPreset("Anzer Yaylası", "Rize", 40.6122, 40.5401),
  RegionPreset("Ünye Kır Çiçeği", "Ordu", 41.1278, 37.2858),
  RegionPreset("Macahel Biyosfer", "Artvin", 41.4428, 41.9794),
  RegionPreset("Çamlıdere Orman", "Ankara", 40.4897, 32.4744),
  RegionPreset("Urla Lavanta & Çam", "İzmir", 38.3229, 26.7640),
  RegionPreset("Kaş Sedir Arılığı", "Antalya", 36.2000, 29.6378)
)

@Composable
fun ApiariesScreen(
  viewModel: BeeViewModel,
  onNavigateToHivesForApiary: (String) -> Unit
) {
  val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
  val allHives by viewModel.allHives.collectAsStateWithLifecycle()
  val weatherMap by viewModel.weatherMap.collectAsStateWithLifecycle()

  var showAddEditDialog by remember { mutableStateOf(false) }
  var editingApiary by remember { mutableStateOf<ApiaryEntity?>(null) }
  var apiaryToDelete by remember { mutableStateOf<ApiaryEntity?>(null) }
  var showProDialog by remember { mutableStateOf(false) }

  val onAddApiaryClick: () -> Unit = {
    if (!PlanManager.canAddApiary(apiaries.size)) {
      showProDialog = true
    } else {
      editingApiary = null
      showAddEditDialog = true
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("apiaries_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      item {
        AppBrandHeader(
          pageSubtitle = "Arı Takip — Arılıklar",
          actions = {
            Button(
              onClick = onAddApiaryClick,
              colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("add_apiary_top_btn")
            ) {
              Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Arılık Ekle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        )
      }

      if (apiaries.isEmpty()) {
        item {
          EmptyStateCard(
            title = "Henüz arılık eklenmemiş",
            description = "Arılıklar kovanlarınızı konumlarına göre gruplar ve o bölgeye özel hava durumunu takip etmenizi sağlar.",
            buttonText = "+ İlk Arılığını Ekle",
            iconEmoji = "🏡",
            onButtonClick = onAddApiaryClick,
            testTag = "apiaries_empty_card"
          )
        }
      } else {
        items(apiaries) { apiary ->
          val hivesInApiary = allHives.filter { it.apiaryId == apiary.id }
          val activeCount = hivesInApiary.count { it.status == "active" }
          val archivedCount = hivesInApiary.count { it.status == "archived" }
          val weather = weatherMap[apiary.id]

          var showMenu by remember { mutableStateOf(false) }

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
              .clickable {
                viewModel.setSelectedApiary(apiary.id)
                onNavigateToHivesForApiary(apiary.id)
              }
              .testTag("apiary_card_${apiary.id}")
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
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(HoneyAmberContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(text = "🏡", fontSize = 22.sp)
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = apiary.name,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(2.dp))
                      Text(
                        text = apiary.displayLocation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }

                Box {
                  IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("apiary_menu_btn_${apiary.id}")
                  ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Seçenekler")
                  }
                  DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                  ) {
                    DropdownMenuItem(
                      text = { Text("Düzenle") },
                      leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                      onClick = {
                        showMenu = false
                        editingApiary = apiary
                        showAddEditDialog = true
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Hava Durumunu Yenile") },
                      leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                      onClick = {
                        showMenu = false
                        viewModel.fetchWeatherForApiary(apiary)
                      }
                    )
                    DropdownMenuItem(
                      text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                      leadingIcon = {
                        Icon(
                          Icons.Filled.Delete,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.error
                        )
                      },
                      onClick = {
                        showMenu = false
                        apiaryToDelete = apiary
                      }
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Hive count badge & Weather preview
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = ForestGreenContainer
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = "🐝 ", fontSize = 12.sp)
                    Text(
                      text = "$activeCount Aktif Kovan" + if (archivedCount > 0) " ($archivedCount Arşiv)" else "",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = ForestOnGreenContainer
                    )
                  }
                }

                if (weather != null) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HoneyAmberContainer
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = "${weather.conditionIcon} ", fontSize = 12.sp)
                      Text(
                        text = "${weather.temperature.toInt()}°C ${weather.conditionDescription}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HoneyOnAmberContainer
                      )
                    }
                  }
                }
              }

              if (apiary.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "📝 ${apiary.notes}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2
                )
              }
            }
          }
        }
      }
    }

    FloatingActionButton(
      onClick = {
        editingApiary = null
        showAddEditDialog = true
      },
      containerColor = HoneyGoldPrimary,
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 80.dp)
        .testTag("add_apiary_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = "Yeni Arılık Ekle", modifier = Modifier.size(28.dp))
    }
  }

  // Add / Edit Apiary Dialog
  if (showAddEditDialog) {
    AddEditApiaryDialog(
      apiary = editingApiary,
      onDismiss = { showAddEditDialog = false },
      onSave = { name, country, city, district, address, lat, lon, notes ->
        viewModel.saveApiary(
          name = name,
          country = country,
          city = city,
          district = district,
          address = address,
          latitude = lat,
          longitude = lon,
          notes = notes,
          id = editingApiary?.id,
          onSuccess = { showAddEditDialog = false }
        )
      }
    )
  }

  // Delete Confirmation Dialog
  if (apiaryToDelete != null) {
    ConfirmationDialog(
      title = "Arılığı Sil",
      message = "\"${apiaryToDelete?.name}\" adlı arılığı silmek istediğinize emin misiniz?",
      confirmButtonText = "Evet, Sil",
      onConfirm = {
        apiaryToDelete?.id?.let { viewModel.deleteApiary(it) }
        apiaryToDelete = null
      },
      onDismiss = { apiaryToDelete = null }
    )
  }

  if (showProDialog) {
    ProUpgradeDialog(
      reason = "Ücretsiz kullanım sınırına ulaştınız.\nÜcretsiz planda en fazla 1 arılık kullanabilirsiniz.\n2. arılığı eklemek için Arı Takip PRO'ya yükseltin.",
      onDismiss = { showProDialog = false }
    )
  }
}

@Composable
fun AddEditApiaryDialog(
  apiary: ApiaryEntity?,
  onDismiss: () -> Unit,
  onSave: (name: String, country: String, city: String, district: String, address: String, lat: Double, lon: Double, notes: String) -> Unit
) {
  var name by remember { mutableStateOf(apiary?.name ?: "") }
  var city by remember {
    mutableStateOf(
      if (apiary != null && apiary.city.isNotBlank()) apiary.city
      else if (apiary != null && apiary.address.contains("/")) apiary.address.split("/").getOrNull(0)?.trim() ?: ""
      else "İstanbul"
    )
  }
  var district by remember {
    mutableStateOf(
      if (apiary != null && apiary.district.isNotBlank()) apiary.district
      else if (apiary != null && apiary.address.contains("/")) apiary.address.split("/").getOrNull(1)?.trim() ?: ""
      else "Şile"
    )
  }
  var lat by remember { mutableStateOf(apiary?.latitude ?: 41.1764) }
  var lon by remember { mutableStateOf(apiary?.longitude ?: 29.6133) }
  var notes by remember { mutableStateOf(apiary?.notes ?: "") }
  var nameError by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (apiary == null) "🏡 Yeni Arılık Ekle" else "🏡 Arılığı Düzenle",
        fontWeight = FontWeight.Bold,
        color = HoneyGoldDark
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = {
              name = it
              nameError = it.isBlank()
            },
            label = { Text("Arılık Adı *") },
            placeholder = { Text("Örn: Şile Arılığı / Köy Arılığı") },
            isError = nameError,
            supportingText = if (nameError) { { Text("Arılık adı zorunludur") } } else null,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = HoneyGoldPrimary,
              focusedLabelColor = HoneyGoldDark
            ),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("apiary_name_input")
          )
        }

        item {
          TurkeyLocationSelector(
            selectedCity = city,
            selectedDistrict = district,
            onLocationSelected = { newCity, newDistrict, newLat, newLon ->
              city = newCity
              district = newDistrict
              lat = newLat
              lon = newLon
            }
          )
        }

        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Arılık Notları (Opsiyonel)") },
            placeholder = { Text("Örn: Su kaynağına 100m, rüzgar korunaklı") },
            minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = HoneyGoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("apiary_notes_input")
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            nameError = true
            return@Button
          }
          val resolvedAddress = if (city.isNotBlank() && district.isNotBlank()) "$city / $district" else city
          onSave(name, "Türkiye", city, district, resolvedAddress, lat, lon, notes)
        },
        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
        modifier = Modifier.testTag("save_apiary_btn")
      ) {
        Text("Kaydet", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("İptal")
      }
    }
  )
}
