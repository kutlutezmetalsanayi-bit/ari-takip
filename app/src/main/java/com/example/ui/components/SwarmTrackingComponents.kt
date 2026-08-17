package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.local.entity.HiveEntity
import com.example.data.local.entity.SwarmEntity
import com.example.ui.theme.EarthWoodContainer
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SwarmOverviewCard(
  events: List<SwarmEntity>,
  onAddEventClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val latestEvent = events.firstOrNull()
  val tendency = latestEvent?.tendencyLevel ?: "Yok"
  val queenCells = latestEvent?.queenCellsStatus ?: "Yok"

  val tendencyBg = when (tendency) {
    "Yüksek", "Acil" -> MaterialTheme.colorScheme.errorContainer
    "Orta" -> HoneyAmberContainer
    else -> ForestGreenContainer
  }
  val tendencyTextColor = when (tendency) {
    "Yüksek", "Acil" -> MaterialTheme.colorScheme.onErrorContainer
    "Orta" -> HoneyOnAmberContainer
    else -> ForestOnGreenContainer
  }

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
            Text("🐝", fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "OĞUL VE BÖLME DURUMU",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Oğul eğilimi ve kovan bölme kayıtları",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = tendencyBg
        ) {
          Text(
            text = "Eğilim: $tendency",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = tendencyTextColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "👑 Ana Arı Memesi", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = queenCells, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.weight(1f)
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "📊 Toplam Olay", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "${events.size} Kayıt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = onAddEventClick,
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenSecondary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_add_swarm_event")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Oğul / Bölme Olayı Ekle", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun SwarmEventItemCard(
  event: SwarmEntity,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")) }
  val dateStr = dateFormat.format(Date(event.eventDate))

  val (icon, bg) = when {
    event.eventType.contains("Bölme") -> "🌿" to ForestGreenContainer
    event.eventType.contains("Çıktı") -> "⚠️" to MaterialTheme.colorScheme.errorContainer
    event.eventType.contains("Yakalandı") -> "🎯" to HoneyAmberContainer
    else -> "🐝" to EarthWoodContainer
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(1.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(bg),
            contentAlignment = Alignment.Center
          ) {
            Text(icon, fontSize = 18.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = event.eventType,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = dateStr,
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        IconButton(onClick = onDeleteClick) {
          Icon(
            Icons.Default.Delete,
            contentDescription = "Sil",
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (event.tendencyLevel != "Yok") {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = "Eğilim: ${event.tendencyLevel}",
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        if (event.queenCellsStatus != "Yok") {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = HoneyAmberContainer
          ) {
            Text(
              text = "Meme: ${event.queenCellsStatus}",
              fontSize = 11.sp,
              color = HoneyOnAmberContainer,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }

        if (event.relatedHiveNumber != null) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = ForestGreenContainer
          ) {
            Text(
              text = "İlişkili: Kovan ${event.relatedHiveNumber}",
              fontSize = 11.sp,
              color = ForestOnGreenContainer,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }
        }
      }

      if (event.actionTaken.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "🛠️ Yapılan Müdahale: ${event.actionTaken}",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (event.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "📝 ${event.notes}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSwarmEventDialog(
  availableHives: List<HiveEntity>,
  currentHiveId: String,
  onDismiss: () -> Unit,
  onSave: (
    eventType: String,
    tendencyLevel: String,
    queenCellsStatus: String,
    relatedHiveId: String?,
    relatedHiveNumber: Int?,
    actionTaken: String,
    notes: String
  ) -> Unit
) {
  val eventTypes = listOf(
    "Oğul Eğilimi / Meme Gözlemi",
    "Oğul Çıktı (Kovan Terk)",
    "Oğul Yakalandı & Kovana Alındı",
    "Bölme Yapıldı (Kaynak Kovan)",
    "Bölme Oluşturuldu (Yeni Kovan)",
    "Yapay Oğul / Koloni Çoğaltma"
  )
  val tendencies = listOf("Yok", "Düşük", "Orta", "Yüksek", "Acil / Çıkmak Üzere")
  val cellStatuses = listOf("Yok", "Yüksük Başlangıcı", "Açık Meme (Besleniyor)", "Kapalı Meme", "Meme Bozuldu / Temizlendi")

  var selectedType by remember { mutableStateOf(eventTypes.first()) }
  var selectedTendency by remember { mutableStateOf(tendencies.first()) }
  var selectedCellStatus by remember { mutableStateOf(cellStatuses.first()) }
  var selectedRelatedHive by remember { mutableStateOf<HiveEntity?>(null) }
  var actionTaken by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  var typeExpanded by remember { mutableStateOf(false) }
  var tendencyExpanded by remember { mutableStateOf(false) }
  var cellExpanded by remember { mutableStateOf(false) }
  var relatedExpanded by remember { mutableStateOf(false) }

  val otherHives = availableHives.filter { it.id != currentHiveId }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🐝", fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Oğul / Bölme Kaydı", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Event Type Dropdown
        ExposedDropdownMenuBox(
          expanded = typeExpanded,
          onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
          OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Olay / İşlem Türü") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = typeExpanded,
            onDismissRequest = { typeExpanded = false }
          ) {
            eventTypes.forEach { item ->
              DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  selectedType = item
                  typeExpanded = false
                }
              )
            }
          }
        }

        // Tendency Dropdown
        ExposedDropdownMenuBox(
          expanded = tendencyExpanded,
          onExpandedChange = { tendencyExpanded = !tendencyExpanded }
        ) {
          OutlinedTextField(
            value = selectedTendency,
            onValueChange = {},
            readOnly = true,
            label = { Text("Oğul Eğilimi") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tendencyExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = tendencyExpanded,
            onDismissRequest = { tendencyExpanded = false }
          ) {
            tendencies.forEach { item ->
              DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  selectedTendency = item
                  tendencyExpanded = false
                }
              )
            }
          }
        }

        // Queen Cell Status Dropdown
        ExposedDropdownMenuBox(
          expanded = cellExpanded,
          onExpandedChange = { cellExpanded = !cellExpanded }
        ) {
          OutlinedTextField(
            value = selectedCellStatus,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ana Arı Memesi Durumu") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cellExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = cellExpanded,
            onDismissRequest = { cellExpanded = false }
          ) {
            cellStatuses.forEach { item ->
              DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  selectedCellStatus = item
                  cellExpanded = false
                }
              )
            }
          }
        }

        // Related Hive (Optional, for splits)
        if (otherHives.isNotEmpty()) {
          ExposedDropdownMenuBox(
            expanded = relatedExpanded,
            onExpandedChange = { relatedExpanded = !relatedExpanded }
          ) {
            OutlinedTextField(
              value = selectedRelatedHive?.let { "Kovan ${it.hiveNumber} (${it.queenBreed})" } ?: "Yok / İlişkilendirme",
              onValueChange = {},
              readOnly = true,
              label = { Text("İlişkili Kovan (Bölme / Ana Kovan)") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = relatedExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = relatedExpanded,
              onDismissRequest = { relatedExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Yok (İlişkilendirme)") },
                onClick = {
                  selectedRelatedHive = null
                  relatedExpanded = false
                }
              )
              otherHives.forEach { h ->
                DropdownMenuItem(
                  text = { Text("Kovan ${h.hiveNumber} (${h.queenBreed})") },
                  onClick = {
                    selectedRelatedHive = h
                    relatedExpanded = false
                  }
                )
              }
            }
          }
        }

        // Action Taken
        OutlinedTextField(
          value = actionTaken,
          onValueChange = { actionTaken = it },
          label = { Text("Yapılan Müdahale") },
          placeholder = { Text("Örn: Kat atıldı, memeler temizlendi, 3 çıta bölündü...") },
          modifier = Modifier.fillMaxWidth()
        )

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notlar") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            selectedType,
            selectedTendency,
            selectedCellStatus,
            selectedRelatedHive?.id,
            selectedRelatedHive?.hiveNumber,
            actionTaken,
            notes
          )
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
