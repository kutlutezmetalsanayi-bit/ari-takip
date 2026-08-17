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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
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
import com.example.data.local.entity.IssueEntity
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

fun getCategoryEmoji(category: String): String {
  return when {
    category.contains("Varroa", ignoreCase = true) -> "🦠"
    category.contains("Çürüklük", ignoreCase = true) -> "⚠️"
    category.contains("Kireç", ignoreCase = true) -> "❄️"
    category.contains("Nosema", ignoreCase = true) -> "🔬"
    category.contains("Güve", ignoreCase = true) -> "🐛"
    category.contains("Zayıf", ignoreCase = true) -> "📉"
    category.contains("Yağma", ignoreCase = true) -> "⚔️"
    category.contains("Ana Arı", ignoreCase = true) -> "👑"
    else -> "🩺"
  }
}

@Composable
fun IssueOverviewCard(
  issues: List<IssueEntity>,
  onAddIssueClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeIssues = issues.filter { it.status != "Çözüldü" }
  val criticalIssues = activeIssues.filter { it.severity == "Acil" || it.severity == "Kritik" }

  val statusBg = if (activeIssues.isEmpty()) ForestGreenContainer else if (criticalIssues.isNotEmpty()) MaterialTheme.colorScheme.errorContainer else HoneyAmberContainer
  val statusTextColor = if (activeIssues.isEmpty()) ForestOnGreenContainer else if (criticalIssues.isNotEmpty()) MaterialTheme.colorScheme.onErrorContainer else HoneyOnAmberContainer

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
              .background(if (activeIssues.isEmpty()) ForestGreenContainer else HoneyAmberContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(if (activeIssues.isEmpty()) "🌿" else "🩺", fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "HASTALIK VE SORUN TAKİBİ",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (activeIssues.isEmpty()) "Aktif sorun bulunmuyor • Koloni sağlıklı" else "${activeIssues.size} aktif sorun takip ediliyor",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = statusBg
        ) {
          Text(
            text = if (activeIssues.isEmpty()) "🟢 Sağlıklı" else "⚠️ ${activeIssues.size} Aktif",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = statusTextColor,
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
            Text(text = "Aktif Sorun", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${activeIssues.size}",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              color = if (activeIssues.isEmpty()) ForestGreenSecondary else MaterialTheme.colorScheme.error
            )
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
            Text(text = "Çözülen Tedaviler", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${issues.count { it.status == "Çözüldü" }}",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              color = ForestGreenSecondary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = onAddIssueClick,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (activeIssues.isEmpty()) ForestGreenSecondary else MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_add_issue")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Yeni Hastalık / Sorun Bildir", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun IssueItemCard(
  issue: IssueEntity,
  onResolveClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")) }
  val detectedDateStr = dateFormat.format(Date(issue.detectedDate))
  val isResolved = issue.status == "Çözüldü"

  val severityColor = when (issue.severity) {
    "Acil", "Kritik" -> MaterialTheme.colorScheme.error
    "Orta" -> HoneyGoldDark
    else -> ForestGreenSecondary
  }

  val statusBg = when (issue.status) {
    "Çözüldü" -> ForestGreenContainer
    "Takipte" -> HoneyAmberContainer
    else -> MaterialTheme.colorScheme.errorContainer
  }
  val statusTextColor = when (issue.status) {
    "Çözüldü" -> ForestOnGreenContainer
    "Takipte" -> HoneyOnAmberContainer
    else -> MaterialTheme.colorScheme.onErrorContainer
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isResolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(if (isResolved) 0.dp else 2.dp),
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
              .background(statusBg),
            contentAlignment = Alignment.Center
          ) {
            Text(getCategoryEmoji(issue.category), fontSize = 18.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = issue.category,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Tespit: $detectedDateStr",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = statusBg
          ) {
            Text(
              text = issue.status,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = statusTextColor,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
          IconButton(onClick = onDeleteClick) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Sil",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Severity badge
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "Şiddet: ${issue.severity}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = severityColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
        if (issue.resolvedDate != null) {
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Çözüm: ${dateFormat.format(Date(issue.resolvedDate))}",
            fontSize = 11.sp,
            color = ForestGreenSecondary
          )
        }
      }

      if (issue.treatmentNotes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "💊 Uygulanan Tedavi / Müdahale: ${issue.treatmentNotes}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(8.dp)
          )
        }
      }

      if (issue.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "📝 Not: ${issue.notes}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (!isResolved) {
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
          onClick = onResolveClick,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreenSecondary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sorun Çözüldü Olarak İşaretle", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIssueDialog(
  onDismiss: () -> Unit,
  onSave: (
    category: String,
    severity: String,
    status: String,
    treatmentNotes: String,
    notes: String
  ) -> Unit
) {
  val categories = listOf(
    "Varroa (Akarlar)",
    "Amerikan Yavru Çürüklüğü",
    "Avrupa Yavru Çürüklüğü",
    "Kireç Hastalığı (Ascosphaera apis)",
    "Nosema (Nosemiasis)",
    "Güve / Mum Kurdu",
    "Zayıf Koloni / Sönme Riski",
    "Yağmalama Olayı",
    "Ana Arısızlık / Yalancı Ana",
    "Pestisit / Zehirlenme Şüphesi",
    "Diğer / Genel Sağlık Sorunu"
  )
  val severities = listOf("Hafif", "Orta", "Acil / Kritik")
  val statuses = listOf("Açık", "Takipte", "Çözüldü")

  var selectedCategory by remember { mutableStateOf(categories.first()) }
  var selectedSeverity by remember { mutableStateOf(severities[1]) } // Orta
  var selectedStatus by remember { mutableStateOf(statuses.first()) } // Açık
  var treatmentNotes by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  var categoryExpanded by remember { mutableStateOf(false) }
  var severityExpanded by remember { mutableStateOf(false) }
  var statusExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("🩺", fontSize = 22.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sorun / Hastalık Bildir", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Category Dropdown
        ExposedDropdownMenuBox(
          expanded = categoryExpanded,
          onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
          OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = { Text("Sorun / Hastalık Türü") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = { categoryExpanded = false }
          ) {
            categories.forEach { item ->
              DropdownMenuItem(
                text = { Text("${getCategoryEmoji(item)} $item") },
                onClick = {
                  selectedCategory = item
                  categoryExpanded = false
                }
              )
            }
          }
        }

        // Severity & Status Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Severity
          ExposedDropdownMenuBox(
            expanded = severityExpanded,
            onExpandedChange = { severityExpanded = !severityExpanded },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = selectedSeverity,
              onValueChange = {},
              readOnly = true,
              label = { Text("Şiddet") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = severityExpanded) },
              modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = severityExpanded,
              onDismissRequest = { severityExpanded = false }
            ) {
              severities.forEach { item ->
                DropdownMenuItem(
                  text = { Text(item) },
                  onClick = {
                    selectedSeverity = item
                    severityExpanded = false
                  }
                )
              }
            }
          }

          // Status
          ExposedDropdownMenuBox(
            expanded = statusExpanded,
            onExpandedChange = { statusExpanded = !statusExpanded },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = selectedStatus,
              onValueChange = {},
              readOnly = true,
              label = { Text("Durum") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
              modifier = Modifier.menuAnchor()
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
        }

        // Treatment Notes
        OutlinedTextField(
          value = treatmentNotes,
          onValueChange = { treatmentNotes = it },
          label = { Text("Uygulanan Tedavi / Müdahale") },
          placeholder = { Text("Örn: Oksalik asit buharlaştırma, kek desteği...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )

        // Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Açıklama ve Belirtiler") },
          placeholder = { Text("Gözlemlenen belirtiler, çerçeve durumu...") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(selectedCategory, selectedSeverity, selectedStatus, treatmentNotes, notes)
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
