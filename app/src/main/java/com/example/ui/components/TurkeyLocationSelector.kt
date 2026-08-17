package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DistrictInfo
import com.example.data.model.ProvinceInfo
import com.example.data.model.TurkeyLocationData
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer

@Composable
fun TurkeyLocationSelector(
  selectedCity: String,
  selectedDistrict: String,
  onLocationSelected: (city: String, district: String, lat: Double, lon: Double) -> Unit,
  modifier: Modifier = Modifier
) {
  var showCityDialog by remember { mutableStateOf(false) }
  var showDistrictDialog by remember { mutableStateOf(false) }

  val currentProvince = remember(selectedCity) {
    TurkeyLocationData.getProvinceByName(selectedCity)
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // 1. Ülke (Sabit Türkiye)
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("location_country_field")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "🇹🇷", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Ülke",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Türkiye",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = HoneyAmberContainer,
          modifier = Modifier.padding(start = 4.dp)
        ) {
          Text(
            text = "81 İl",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = HoneyOnAmberContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
    }

    // 2. Şehir (İl) Seçimi
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surface,
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (selectedCity.isNotBlank()) HoneyGoldPrimary else MaterialTheme.colorScheme.outline
      ),
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showCityDialog = true }
        .testTag("location_city_selector")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.LocationCity,
          contentDescription = null,
          tint = if (selectedCity.isNotBlank()) HoneyGoldDark else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Şehir (İl)",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (selectedCity.isNotBlank()) {
              currentProvince?.let { "${it.name} (${String.format("%02d", it.plateCode)})" } ?: selectedCity
            } else {
              "Şehir Seçiniz (81 İl)"
            },
            fontSize = 14.sp,
            fontWeight = if (selectedCity.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
            color = if (selectedCity.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "Şehir Seç",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 3. İlçe Seçimi (Şehir seçilmeden aktif olmaz)
    val isDistrictEnabled = selectedCity.isNotBlank() && currentProvince != null
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = if (isDistrictEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        when {
          !isDistrictEnabled -> MaterialTheme.colorScheme.outlineVariant
          selectedDistrict.isNotBlank() -> HoneyGoldPrimary
          else -> MaterialTheme.colorScheme.outline
        }
      ),
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = isDistrictEnabled) { showDistrictDialog = true }
        .testTag("location_district_selector")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Place,
          contentDescription = null,
          tint = when {
            !isDistrictEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            selectedDistrict.isNotBlank() -> HoneyGoldDark
            else -> MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "İlçe",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = when {
              !isDistrictEnabled -> "Önce şehir seçiniz"
              selectedDistrict.isNotBlank() -> selectedDistrict
              else -> "İlçe Seçiniz (${currentProvince?.districts?.size ?: 0} İlçe)"
            },
            fontSize = 14.sp,
            fontWeight = if (selectedDistrict.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
            color = when {
              !isDistrictEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
              selectedDistrict.isNotBlank() -> MaterialTheme.colorScheme.onSurface
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
          )
        }
        Icon(
          imageVector = Icons.Default.ArrowDropDown,
          contentDescription = "İlçe Seç",
          tint = if (isDistrictEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
      }
    }

    // Seçilen konum bilgi özeti
    if (selectedCity.isNotBlank()) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = HoneyAmberContainer.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "📍 ", fontSize = 12.sp)
          Text(
            text = "Hava Durumu Konumu: ${if (selectedDistrict.isNotBlank()) "$selectedDistrict / $selectedCity" else selectedCity}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = HoneyOnAmberContainer
          )
        }
      }
    }
  }

  // --- ŞEHİR SEÇİM DİALOGU ---
  if (showCityDialog) {
    SearchableProvinceDialog(
      onDismiss = { showCityDialog = false },
      onSelectProvince = { province ->
        showCityDialog = false
        // Check if previous district exists in new province
        val matchingDistrict = province.districts.find { it.name.equals(selectedDistrict, ignoreCase = true) }
        val newDistrict = matchingDistrict?.name ?: (province.districts.firstOrNull()?.name ?: "")
        val targetLat = matchingDistrict?.lat ?: province.lat
        val targetLon = matchingDistrict?.lon ?: province.lon
        onLocationSelected(province.name, newDistrict, targetLat, targetLon)
      }
    )
  }

  // --- İLÇE SEÇİM DİALOGU ---
  if (showDistrictDialog && currentProvince != null) {
    SearchableDistrictDialog(
      province = currentProvince,
      onDismiss = { showDistrictDialog = false },
      onSelectDistrict = { district ->
        showDistrictDialog = false
        onLocationSelected(currentProvince.name, district.name, district.lat, district.lon)
      }
    )
  }
}

@Composable
fun SearchableProvinceDialog(
  onDismiss: () -> Unit,
  onSelectProvince: (ProvinceInfo) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filteredProvinces = remember(searchQuery) {
    TurkeyLocationData.searchProvinces(searchQuery)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .testTag("province_selection_dialog")
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "🇹🇷 Şehir (İl) Seçin",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Türkiye'nin 81 İli",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Kapat")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Arama Kutusu (Search TextField with Turkish support)
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("Şehir Ara (örn: İstanbul, İzmir, İst)") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Temizle")
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("city_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "${filteredProvinces.size} il bulundu",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Provinces List
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredProvinces, key = { it.plateCode }) { province ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectProvince(province) }
                .testTag("city_item_${province.plateCode}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Plaka Kodu Rozeti
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = HoneyAmberContainer,
                  modifier = Modifier.size(width = 36.dp, height = 28.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = String.format("%02d", province.plateCode),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = HoneyOnAmberContainer
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = province.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${province.districts.size} İlçe",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Icon(
                  imageVector = Icons.Default.LocationOn,
                  contentDescription = null,
                  tint = HoneyGoldDark.copy(alpha = 0.6f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun SearchableDistrictDialog(
  province: ProvinceInfo,
  onDismiss: () -> Unit,
  onSelectDistrict: (DistrictInfo) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val filteredDistricts = remember(searchQuery, province) {
    TurkeyLocationData.searchDistricts(province.name, searchQuery)
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .testTag("district_selection_dialog")
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "📍 ${province.name} İlçeleri",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "İlçe seçimi hava durumunu hassaslaştırır",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Kapat")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Arama Kutusu
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("İlçe Ara (örn: ${province.districts.firstOrNull()?.name ?: "Merkez"})") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Temizle")
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("district_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "${filteredDistricts.size} ilçe listeleniyor",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Districts List
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(filteredDistricts, key = { it.name }) { district ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectDistrict(district) }
                .testTag("district_item_${district.name}")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Place,
                  contentDescription = null,
                  tint = HoneyGoldDark,
                  modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                  text = district.name,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }
    }
  }
}
