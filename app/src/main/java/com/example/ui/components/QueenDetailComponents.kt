package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.QueenEntity
import com.example.ui.theme.EarthWoodContainer
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Queen marking color mapping based on international standard
fun getQueenColorHex(markingColor: String): Color {
  return when (markingColor.lowercase(Locale.ROOT)) {
    "beyaz" -> Color(0xFFEEEEEE)
    "sarı" -> Color(0xFFFFD54F)
    "kırmızı" -> Color(0xFFE53935)
    "yeşil" -> Color(0xFF43A047)
    "mavi" -> Color(0xFF1E88E5)
    else -> Color(0xFF9E9E9E)
  }
}

fun getInternationalColorNameForYear(year: Int): String {
  return when (year % 10) {
    1, 6 -> "Beyaz"
    2, 7 -> "Sarı"
    3, 8 -> "Kırmızı"
    4, 9 -> "Yeşil"
    0, 5 -> "Mavi"
    else -> "İşaretsiz"
  }
}

@Composable
fun ActiveQueenCard(
  queen: QueenEntity?,
  defaultBreed: String,
  defaultYear: Int,
  onEditQueenClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentYear = Calendar.getInstance().get(Calendar.YEAR)
  val queenYear = queen?.year ?: defaultYear
  val breed = queen?.breed ?: defaultBreed
  val status = queen?.status ?: "Var"
  val markingColor = queen?.markingColor ?: getInternationalColorNameForYear(queenYear)
  val source = queen?.source ?: "Kendi Üretimimiz"
  val notes = queen?.notes ?: ""
  val colorHex = getQueenColorHex(markingColor)
  val ageYears = currentYear - queenYear

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
    modifier = modifier.fillMaxWidth()
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
            Text("👑", fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "AKTİF ANA ARI",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$breed ırkı • $queenYear doğumlu",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (status == "Var") ForestGreenContainer else HoneyAmberContainer
        ) {
          Text(
            text = if (status == "Var") "🟢 Ana Arı Var" else "⚠️ $status",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (status == "Var") ForestOnGreenContainer else HoneyOnAmberContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Attributes Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Year & Color Marking
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(colorHex)
                  .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "$markingColor ($queenYear)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (ageYears <= 0) "Genç Ana (0 yaş)" else "$ageYears Yaşında",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Source
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "📌 Köken / Kaynak",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = source,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
          }
        }
      }

      if (notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "📝 $notes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = onEditQueenClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = HoneyGoldDark),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_edit_queen")
      ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Ana Arı Bilgilerini Güncelle / Yeni Ana Tak", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun QueenHistoryItemCard(
  queen: QueenEntity,
  onDeleteClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")) }
  val dateStr = if (queen.installedDate > 0) dateFormat.format(Date(queen.installedDate)) else "Bilinmiyor"
  val colorHex = getQueenColorHex(queen.markingColor)

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(EarthWoodContainer),
          contentAlignment = Alignment.Center
        ) {
          Text("👑", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${queen.year} ${queen.breed}",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colorHex)
                .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
            )
          }
          Text(
            text = "Kaynak: ${queen.source} • Giriş: $dateStr",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (queen.notes.isNotBlank()) {
            Text(
              text = "Not: ${queen.notes}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }
        }
      }

      if (onDeleteClick != null) {
        IconButton(onClick = onDeleteClick) {
          Icon(
            Icons.Default.Delete,
            contentDescription = "Sil",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueenEditDialog(
  currentQueen: QueenEntity?,
  defaultBreed: String,
  defaultYear: Int,
  onDismiss: () -> Unit,
  onSave: (
    status: String,
    year: Int,
    breed: String,
    markingColor: String,
    source: String,
    notes: String,
    isNewActiveQueen: Boolean
  ) -> Unit
) {
  val currentCalendarYear = Calendar.getInstance().get(Calendar.YEAR)
  val breeds = listOf(
    "Kafkas",
    "Karniyol (Carnica)",
    "Anadolu",
    "Belfast (Buckfast)",
    "İtalyan (Ligustica)",
    "Muğla Yerli",
    "Trakya",
    "Karakovan Melezi",
    "Diğer / Hibrit"
  )
  val statuses = listOf("Var", "Yok (Ana Arısız)", "Yalancı Ana", "Memede / Yüksük Var", "Çiftleşmede")
  val sources = listOf(
    "Kendi Üretimimiz",
    "Satın Alındı (Sertifikalı)",
    "Bölme Koloniden",
    "Doğal Oğuldan",
    "Takas / Hediye"
  )
  val years = ((currentCalendarYear - 4)..currentCalendarYear).toList().reversed()

  var selectedStatus by remember { mutableStateOf(currentQueen?.status ?: "Var") }
  var selectedYear by remember { mutableIntStateOf(currentQueen?.year ?: defaultYear) }
  var selectedBreed by remember { mutableStateOf(currentQueen?.breed ?: defaultBreed) }
  var selectedSource by remember { mutableStateOf(currentQueen?.source ?: "Kendi Üretimimiz") }
  var notes by remember { mutableStateOf(currentQueen?.notes ?: "") }
  var isNewQueenReplacement by remember { mutableStateOf(currentQueen == null) }

  val autoColor = getInternationalColorNameForYear(selectedYear)
  var markingColor by remember(selectedYear) { mutableStateOf(autoColor) }

  var breedExpanded by remember { mutableStateOf(false) }
  var statusExpanded by remember { mutableStateOf(false) }
  var sourceExpanded by remember { mutableStateOf(false) }
  var yearExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("👑", fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Ana Arı Bilgileri", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Status Dropdown
        ExposedDropdownMenuBox(
          expanded = statusExpanded,
          onExpandedChange = { statusExpanded = !statusExpanded }
        ) {
          OutlinedTextField(
            value = selectedStatus,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ana Arı Durumu") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = statusExpanded,
            onDismissRequest = { statusExpanded = false }
          ) {
            statuses.forEach { item ->
              DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  selectedStatus = item
                  statusExpanded = false
                }
              )
            }
          }
        }

        // Year & Marking Color Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Year Dropdown
          ExposedDropdownMenuBox(
            expanded = yearExpanded,
            onExpandedChange = { yearExpanded = !yearExpanded },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = selectedYear.toString(),
              onValueChange = {},
              readOnly = true,
              label = { Text("Doğum Yılı") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
              modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = yearExpanded,
              onDismissRequest = { yearExpanded = false }
            ) {
              years.forEach { y ->
                DropdownMenuItem(
                  text = { Text(y.toString()) },
                  onClick = {
                    selectedYear = y
                    markingColor = getInternationalColorNameForYear(y)
                    yearExpanded = false
                  }
                )
              }
            }
          }

          // Marking Color Display
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .height(56.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(getQueenColorHex(markingColor))
                  .border(1.dp, Color.Gray.copy(alpha = 0.6f), CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text("İşaret Rengi", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(markingColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Breed Dropdown
        ExposedDropdownMenuBox(
          expanded = breedExpanded,
          onExpandedChange = { breedExpanded = !breedExpanded }
        ) {
          OutlinedTextField(
            value = selectedBreed,
            onValueChange = { selectedBreed = it },
            label = { Text("Ana Arı Irkı") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = breedExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = breedExpanded,
            onDismissRequest = { breedExpanded = false }
          ) {
            breeds.forEach { item ->
              DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  selectedBreed = item
                  breedExpanded = false
                }
              )
            }
          }
        }

        // Source Dropdown
        ExposedDropdownMenuBox(
          expanded = sourceExpanded,
          onExpandedChange = { sourceExpanded = !sourceExpanded }
        ) {
          OutlinedTextField(
            value = selectedSource,
            onValueChange = { selectedSource = it },
            label = { Text("Köken / Kaynak") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = sourceExpanded,
            onDismissRequest = { sourceExpanded = false }
          ) {
            sources.forEach { item ->
              DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  selectedSource = item
                  sourceExpanded = false
                }
              )
            }
          }
        }

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Ana Arı Notları") },
          placeholder = { Text("Yumurtlama hızı, mizaç, petek düzeni...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(selectedStatus, selectedYear, selectedBreed, markingColor, selectedSource, notes, true)
        },
        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark)
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
