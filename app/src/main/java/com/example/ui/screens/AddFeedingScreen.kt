package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.util.ImageUtils
import com.example.ui.components.LargeOutdoorChipGroup
import com.example.ui.components.PhotoCaptureSection
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import com.example.ui.viewmodel.BeeViewModel
import kotlinx.coroutines.launch

val FEEDING_TYPES = listOf("Şurup 1:1", "Şurup 2:1", "Arı Keki", "Protein Keki", "Fondan Şeker", "Su Beslemesi", "Diğer")
val FEEDING_PRESETS = listOf(
  Triple("0.5 Litre", 0.5, "Litre"),
  Triple("1.0 Litre", 1.0, "Litre"),
  Triple("1.5 Litre", 1.5, "Litre"),
  Triple("2.0 Litre", 2.0, "Litre"),
  Triple("1.0 kg Kek", 1.0, "kg"),
  Triple("2.0 kg Kek", 2.0, "kg")
)

@Composable
fun AddFeedingScreen(
  viewModel: BeeViewModel,
  hiveId: String,
  onNavigateBack: () -> Unit,
  onFeedingSaved: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val allHives by viewModel.allHives.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
  val hive = allHives.find { it.id == hiveId }

  var feedingType by remember { mutableStateOf("Şurup 1:1") }
  var amountStr by remember { mutableStateOf("1.0") }
  var unit by remember { mutableStateOf("Litre") }
  var notes by remember { mutableStateOf("") }
  var amountError by remember { mutableStateOf(false) }

  var attachedPhotoPath by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("add_feeding_screen"),
    contentPadding = PaddingValues(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("feeding_back_btn")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Kovan ${hive?.hiveNumber ?: "?"} Besleme Ekle",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = HoneyGoldDark
          )
          Text(
            text = "Arı Takip — Hızlı Besleme Kaydı",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // 1. Feeding Type
    item {
      LargeOutdoorChipGroup(
        title = "🍯 Besleme Türü",
        options = FEEDING_TYPES,
        selectedOption = feedingType,
        onOptionSelected = {
          feedingType = it
          if (it.contains("Kek") || it.contains("Şeker")) {
            unit = "kg"
          } else {
            unit = "Litre"
          }
        },
        tagPrefix = "feeding_type"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 2. Quick Presets Row
    item {
      Text(
        text = "⚡ Hızlı Miktar Seçimi",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(FEEDING_PRESETS) { preset ->
          val isSelected = amountStr == preset.second.toString() && unit == preset.third
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable {
                amountStr = preset.second.toString()
                unit = preset.third
                amountError = false
              }
              .testTag("preset_${preset.first.replace(" ", "_")}")
          ) {
            Text(
              text = preset.first,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 13.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 3. Custom Amount & Unit Input
    item {
      Text(
        text = "⚖️ Miktar ve Birim",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = amountStr,
          onValueChange = {
            amountStr = it
            amountError = it.toDoubleOrNull() == null
          },
          label = { Text("Miktar") },
          isError = amountError,
          supportingText = { if (amountError) Text("Geçerli bir sayı girin") },
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoneyGoldPrimary),
          modifier = Modifier
            .weight(1f)
            .testTag("feeding_amount_input")
        )

        // Unit selector
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Litre", "kg", "gr").forEach { u ->
            val isSelected = unit == u
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) HoneyGoldDark else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { unit = u }
            ) {
              Text(
                text = u,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
              )
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. Notes
    item {
      Text(
        text = "📝 Not / Açıklama",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        placeholder = { Text("Örn: Kek beslemesi yapıldı, şerbet verildi.") },
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoneyGoldPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("feeding_notes_input")
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 5. Photo Attachment with Camera & Gallery (V1.3.2)
    item {
      PhotoCaptureSection(
        title = "📷 Besleme Fotoğrafı",
        photoPaths = if (attachedPhotoPath != null) listOf(attachedPhotoPath!!) else emptyList(),
        onPhotosUpdated = { list ->
          attachedPhotoPath = list.firstOrNull()
        },
        allowMultiple = false
      )
      Spacer(modifier = Modifier.height(24.dp))
    }

    // Submit Button with Double-Click Prevention (V1.1 Section 16)
    item {
      Button(
        onClick = {
          if (hive == null || isSaving) return@Button
          val amount = amountStr.toDoubleOrNull() ?: return@Button

          viewModel.saveFeeding(
            hiveId = hive.id,
            apiaryId = hive.apiaryId,
            feedingType = feedingType,
            amount = amount,
            unit = unit,
            notes = notes,
            photoUri = attachedPhotoPath,
            onSuccess = onFeedingSaved
          )
        },
        enabled = !isSaving && !amountError && amountStr.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("submit_feeding_btn")
      ) {
        if (isSaving) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Kaydediliyor...", fontWeight = FontWeight.Black, fontSize = 16.sp)
        } else {
          Icon(Icons.Filled.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Beslemeyi Kaydet",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
          )
        }
      }
    }
  }
}
