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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.components.LargeOutdoorMultiSelectGroup
import com.example.ui.components.PhotoCaptureSection
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import com.example.ui.viewmodel.BeeViewModel
import kotlinx.coroutines.launch

val HONEY_STATUS_OPTIONS = listOf("Az", "Orta", "İyi", "Çok İyi")
val POLLEN_STATUS_OPTIONS = listOf("Az", "Yeterli", "Çok")
val BEHAVIOR_OPTIONS = listOf("Sakin", "Normal", "Hırçın")
val FRAME_CHANGE_OPTIONS = listOf("Kat eklendi", "Kat çıkarıldı", "Çerçeve eklendi", "Çerçeve çıkarıldı")
val PROBLEM_OPTIONS = listOf("Varroa belirtisi", "Hastalık şüphesi", "Ana arı problemi", "Oğul eğilimi", "Diğer")

data class InspectionAttachedPhoto(
  val localPath: String,
  val caption: String = "Kontrol Fotoğrafı"
)

@Composable
fun AddInspectionScreen(
  viewModel: BeeViewModel,
  hiveId: String,
  onNavigateBack: () -> Unit,
  onInspectionSaved: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val allHives by viewModel.allHives.collectAsStateWithLifecycle()
  val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
  val weatherMap by viewModel.weatherMap.collectAsStateWithLifecycle()
  val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

  val hive = allHives.find { it.id == hiveId }
  val apiary = apiaries.find { it.id == hive?.apiaryId }
  val weather = apiary?.let { weatherMap[it.id] }

  var queenSeen by remember { mutableStateOf("Görüldü") }
  var selectedBrood by remember { mutableStateOf(setOf("Yumurta", "Larva", "Kapalı Yavru")) }
  var colonyStrength by remember { mutableStateOf(hive?.colonyStrength ?: "Güçlü") }
  var honeyStatus by remember { mutableStateOf("İyi") }
  var pollenStatus by remember { mutableStateOf("Yeterli") }
  var behavior by remember { mutableStateOf("Sakin") }
  var selectedFrameChanges by remember { mutableStateOf(setOf<String>()) }
  var selectedProblems by remember { mutableStateOf(setOf<String>()) }
  var notes by remember { mutableStateOf("") }
  var photoUrisList by remember { mutableStateOf(listOf<String>()) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("add_inspection_screen"),
    contentPadding = PaddingValues(16.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("inspection_back_btn")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "Kovan ${hive?.hiveNumber ?: "?"} Kontrolü",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = HoneyGoldDark
          )
          Text(
            text = "Arı Takip — Hızlı Kontrol Formu",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Weather Snapshot Card
    if (weather != null) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = HoneyAmberContainer),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = weather.conditionIcon, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
              Text(
                text = "🌤️ Otomatik Hava Durumu Kaydı",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HoneyOnAmberContainer
              )
              Text(
                text = "${weather.temperature.toInt()}°C • ${weather.conditionDescription} • %${weather.humidity} Nem",
                fontSize = 13.sp,
                color = HoneyOnAmberContainer
              )
            }
          }
        }
      }
    }

    // 1. Queen Seen / Not Seen
    item {
      LargeOutdoorChipGroup(
        title = "👑 Ana Arı Durumu",
        options = listOf("Görüldü", "Görülmedi"),
        selectedOption = queenSeen,
        onOptionSelected = { queenSeen = it },
        tagPrefix = "queen"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 2. Brood Multi-Select
    item {
      LargeOutdoorMultiSelectGroup(
        title = "🥚 Yavru Durumu",
        options = listOf("Yumurta", "Larva", "Kapalı Yavru"),
        selectedOptions = selectedBrood,
        onOptionToggled = { opt ->
          selectedBrood = if (selectedBrood.contains(opt)) selectedBrood - opt else selectedBrood + opt
        },
        tagPrefix = "brood"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 3. Colony Strength
    item {
      LargeOutdoorChipGroup(
        title = "💪 Koloni Gücü",
        options = listOf("Zayıf", "Orta", "Güçlü", "Çok Güçlü"),
        selectedOption = colonyStrength,
        onOptionSelected = { colonyStrength = it },
        tagPrefix = "colony"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 4. Honey Status
    item {
      LargeOutdoorChipGroup(
        title = "🍯 Bal Durumu",
        options = HONEY_STATUS_OPTIONS,
        selectedOption = honeyStatus,
        onOptionSelected = { honeyStatus = it },
        tagPrefix = "honey"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 5. Pollen Status
    item {
      LargeOutdoorChipGroup(
        title = "🌻 Polen Seviyesi",
        options = POLLEN_STATUS_OPTIONS,
        selectedOption = pollenStatus,
        onOptionSelected = { pollenStatus = it },
        tagPrefix = "pollen"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 6. Behavior
    item {
      LargeOutdoorChipGroup(
        title = "🐝 Koloni Davranışı",
        options = BEHAVIOR_OPTIONS,
        selectedOption = behavior,
        onOptionSelected = { behavior = it },
        tagPrefix = "behavior"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 7. Hive Frame Changes
    item {
      LargeOutdoorMultiSelectGroup(
        title = "🪵 Kovan & Çerçeve İşlemleri",
        options = FRAME_CHANGE_OPTIONS,
        selectedOptions = selectedFrameChanges,
        onOptionToggled = { opt ->
          selectedFrameChanges = if (selectedFrameChanges.contains(opt)) selectedFrameChanges - opt else selectedFrameChanges + opt
        },
        tagPrefix = "frame"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 8. Problems
    item {
      LargeOutdoorMultiSelectGroup(
        title = "⚠️ Sorunlar & Riskler",
        options = PROBLEM_OPTIONS,
        selectedOptions = selectedProblems,
        onOptionToggled = { opt ->
          selectedProblems = if (selectedProblems.contains(opt)) selectedProblems - opt else selectedProblems + opt
        },
        tagPrefix = "problem"
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 9. Free Notes
    item {
      Text(
        text = "📝 Kontrol Notu",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        placeholder = { Text("Örn: 6 çerçeve yavru basılı, yeni petek kabartıldı") },
        minLines = 3,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoneyGoldPrimary),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("inspection_notes_input")
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // 10. Photo Attachments with Camera & Gallery (V1.3.1)
    item {
      PhotoCaptureSection(
        title = "📷 Kontrol Fotoğrafları (${photoUrisList.size})",
        photoPaths = photoUrisList,
        onPhotosUpdated = { updated -> photoUrisList = updated },
        allowMultiple = true
      )
      Spacer(modifier = Modifier.height(24.dp))
    }

    // Submit Button with Double-Click Prevention (V1.1 Section 16)
    item {
      Button(
        onClick = {
          if (hive == null || isSaving) return@Button

          viewModel.saveInspection(
            hiveId = hive.id,
            apiaryId = hive.apiaryId,
            queenSeen = (queenSeen == "Görüldü"),
            broodEgg = selectedBrood.contains("Yumurta"),
            broodLarva = selectedBrood.contains("Larva"),
            broodCapped = selectedBrood.contains("Kapalı Yavru"),
            colonyStrength = colonyStrength,
            honeyStatus = honeyStatus,
            pollenStatus = pollenStatus,
            behavior = behavior,
            frameChanges = selectedFrameChanges.joinToString(", "),
            problems = selectedProblems.joinToString(", "),
            notes = notes,
            photoUris = photoUrisList,
            onSuccess = onInspectionSaved
          )
        },
        enabled = !isSaving,
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenSecondary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("submit_inspection_btn")
      ) {
        if (isSaving) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Kaydediliyor...", fontWeight = FontWeight.Black, fontSize = 16.sp)
        } else {
          Icon(Icons.Filled.Check, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Kontrolü Kaydet",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
          )
        }
      }
    }
  }
}
