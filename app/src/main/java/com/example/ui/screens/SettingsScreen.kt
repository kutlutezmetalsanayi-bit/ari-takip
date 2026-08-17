package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.billing.PlanManager
import com.example.data.firebase.FirebaseSecurityRules
import com.example.data.util.BackupPayload
import com.example.data.util.ImportMode
import com.example.ui.components.AppBrandHeader
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer
import com.example.ui.viewmodel.BeeViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
  viewModel: BeeViewModel
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
  val beekeeperName by viewModel.beekeeperName.collectAsStateWithLifecycle()
  val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
  val isBackingUp by viewModel.isBackingUp.collectAsStateWithLifecycle()
  val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
  val lastCloudSyncTime by viewModel.lastCloudSyncTimestamp.collectAsStateWithLifecycle()
  val stats by viewModel.databaseStats.collectAsStateWithLifecycle()
  val allQueens by viewModel.allQueens.collectAsStateWithLifecycle()
  val allSwarmEvents by viewModel.allSwarmEvents.collectAsStateWithLifecycle()
  val allIssues by viewModel.allIssues.collectAsStateWithLifecycle()

  var showEditNameDialog by remember { mutableStateOf(false) }
  var showProDialog by remember { mutableStateOf(false) }

  // Import Dialog states
  var showImportModal by remember { mutableStateOf(false) }
  var importedJsonContent by remember { mutableStateOf<String?>(null) }
  var previewPayload by remember { mutableStateOf<BackupPayload?>(null) }
  var importErrorMessage by remember { mutableStateOf<String?>(null) }
  var selectedImportMode by remember { mutableStateOf(ImportMode.MERGE) }
  var showImportConfirmDialog by remember { mutableStateOf(false) }

  // Direct Text Backup / Export dialog
  var showExportTextDialog by remember { mutableStateOf(false) }
  var exportTextContent by remember { mutableStateOf("") }
  var exportDialogTitle by remember { mutableStateOf("") }

  // Launcher for saving JSON backup via SAF
  val createJsonLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri: Uri? ->
    uri?.let {
      viewModel.generateBackupJson { json ->
        if (json != null) {
          try {
            context.contentResolver.openOutputStream(it)?.use { os ->
              os.write(json.toByteArray())
            }
          } catch (e: Exception) {
            // Handled
          }
        }
      }
    }
  }

  // Launcher for importing JSON file
  val openJsonLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      try {
        val inputStream = context.contentResolver.openInputStream(it)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
          stringBuilder.append(line).append('\n')
        }
        reader.close()
        inputStream?.close()

        val jsonStr = stringBuilder.toString()
        importedJsonContent = jsonStr
        val preview = viewModel.parseBackupForPreview(jsonStr)
        if (preview != null) {
          previewPayload = preview
          importErrorMessage = null
        } else {
          previewPayload = null
          importErrorMessage = "Bu dosya geçerli bir Arı Takip yedeği değil."
        }
        showImportModal = true
      } catch (e: Exception) {
        importErrorMessage = "Dosya okunamadı: ${e.localizedMessage}"
        previewPayload = null
        showImportModal = true
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("settings_screen"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    item {
      AppBrandHeader(pageSubtitle = "Arı Takip — Ayarlar & Profil")
    }

    // 1. User Profile & Status Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(HoneyGoldPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "👨‍🌾", fontSize = 26.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (beekeeperName.isNotBlank()) beekeeperName else "Arıcı Adınızı Ekleyin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (beekeeperName.isNotBlank()) "Profesyonel Arıcı Profili" else "Adınızı belirlemek için düzenleyin",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            IconButton(
              onClick = { showEditNameDialog = true },
              modifier = Modifier.testTag("edit_beekeeper_name_btn")
            ) {
              Icon(
                Icons.Filled.Edit,
                contentDescription = "Arıcı Adını Düzenle",
                tint = HoneyGoldDark
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Responsive Status Badges Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isOnline) ForestGreenContainer else Color(0xFFFFE4E6),
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text(
                  text = if (isOnline) "🟢 Çevrimiçi" else "🔴 Çevrimdışı",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isOnline) ForestOnGreenContainer else Color(0xFF9F1239)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = ForestGreenContainer,
              modifier = Modifier.weight(1f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "✓ Offline-Ready",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = ForestOnGreenContainer
                )
              }
            }
          }
        }
      }
    }

    // 2. PRO Subscription Status Card
    item {
      val isPro = PlanManager.isPro()
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isPro) ForestGreenContainer else HoneyAmberContainer
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = if (isPro) ForestGreenSecondary else HoneyGoldDark
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isPro) "👑 Arı Takip PRO" else "⭐ Ücretsiz Plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPro) ForestOnGreenContainer else HoneyOnAmberContainer
              )
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isPro) ForestGreenSecondary else HoneyGoldDark
            ) {
              Text(
                text = if (isPro) "AKTİF" else "STANDART",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = if (isPro) {
              "Sınırsız arılık ve kovan yönetimi aktif. Tüm PRO özelliklerinden faydalanıyorsunuz."
            } else {
              "Mevcut limit: 1 Arılık ve 10 Aktif Kovan. Daha fazla arılık ve kovan eklemek için PRO sürüme geçebilirsiniz."
            },
            fontSize = 12.sp,
            color = if (isPro) ForestOnGreenContainer.copy(alpha = 0.9f) else HoneyOnAmberContainer.copy(alpha = 0.9f)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { showProDialog = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isPro) ForestGreenSecondary else HoneyGoldDark
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_pro_upgrade_btn")
          ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isPro) "Plan Detaylarını Görüntüle" else "⭐ PRO'YA YÜKSELT",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedButton(
            onClick = {
              viewModel.restoreGooglePlayPurchases { _, _ -> }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_restore_purchases_btn")
          ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Satın Alımları Geri Yükle",
              fontWeight = FontWeight.SemiBold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // 2. V1.2 YEDEKLEME VE VERİ BÖLÜMÜ (MANDATORY V1.2)
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
          text = "📦 Yedekleme ve Veri Güvenliği",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(2.dp),
          modifier = Modifier.fillMaxWidth().testTag("backup_and_data_section")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {

            // ☁️ 2.1 Son Bulut Senkronizasyonu
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "☁️", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                Column {
                  Text(
                    text = "Son Bulut Senkronizasyonu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  val syncFormatted = if (lastCloudSyncTime > 0) {
                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
                    sdf.format(Date(lastCloudSyncTime))
                  } else {
                    "Henüz senkronize edilmedi"
                  }
                  Text(
                    text = syncFormatted,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (pendingSyncCount > 0) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = HoneyAmberContainer
                ) {
                  Text(
                    text = "$pendingSyncCount bekliyor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HoneyOnAmberContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 💾 2.2 Manuel Yedekleme ("Şimdi Yedekle")
            Button(
              onClick = { viewModel.performManualCloudBackup() },
              enabled = !isBackingUp,
              colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("manual_backup_btn")
            ) {
              if (isBackingUp) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = Color.White,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verileriniz yedekleniyor...", fontWeight = FontWeight.Bold)
              } else {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("💾 Şimdi Buluta Yedekle", fontWeight = FontWeight.Bold)
              }
            }

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 16.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // 📤 2.3 Verileri Dışa Aktar (JSON & CSV)
            Text(
              text = "📤 Verileri Dışa Aktar",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Full JSON Export
              OutlinedButton(
                onClick = {
                  val filename = "AriTakip_Yedek_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.json"
                  createJsonLauncher.launch(filename)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("export_json_btn")
              ) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("JSON Yedeği", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }

              // Share / View JSON text
              OutlinedButton(
                onClick = {
                  viewModel.generateBackupJson { json ->
                    if (json != null) {
                      exportTextContent = json
                      exportDialogTitle = "JSON Yedek Metni"
                      showExportTextDialog = true
                    }
                  }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("share_json_btn")
              ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yedek Paylaş", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CSV Export Buttons (2x2 Grid for Hives, Inspections, Feedings, Issues)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.generateHivesCsv { csv ->
                    if (csv != null) {
                      shareTextContent(context, csv, "Kovanlar_Listesi.csv", "text/csv")
                    }
                  }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("export_hives_csv_btn")
              ) {
                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kovanlar (CSV)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }

              OutlinedButton(
                onClick = {
                  viewModel.generateInspectionsCsv { csv ->
                    if (csv != null) {
                      shareTextContent(context, csv, "Kontroller_Listesi.csv", "text/csv")
                    }
                  }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("export_inspections_csv_btn")
              ) {
                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kontroller (CSV)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedButton(
                onClick = {
                  viewModel.generateFeedingsCsv { csv ->
                    if (csv != null) {
                      shareTextContent(context, csv, "Besleme_Listesi.csv", "text/csv")
                    }
                  }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("export_feedings_csv_btn")
              ) {
                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Beslemeler (CSV)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }

              OutlinedButton(
                onClick = {
                  viewModel.generateIssuesCsv { csv ->
                    if (csv != null) {
                      shareTextContent(context, csv, "Sorun_Hastalik_Listesi.csv", "text/csv")
                    }
                  }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("export_issues_csv_btn")
              ) {
                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sorunlar (CSV)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
            }

            HorizontalDivider(
              modifier = Modifier.padding(vertical = 16.dp),
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // 📥 2.4 Yedekten Geri Yükle
            Text(
              text = "📥 Yedekten Geri Yükle",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
              text = "Daha önce aldığınız bir JSON yedek dosyasını seçerek kayıtlarınızı güvenle geri yükleyebilirsiniz.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { openJsonLauncher.launch("application/json") },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenSecondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("choose_backup_file_btn")
              ) {
                Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("JSON Dosyası Seç", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }

              OutlinedButton(
                onClick = {
                  importedJsonContent = ""
                  previewPayload = null
                  importErrorMessage = null
                  showImportModal = true
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f).testTag("paste_backup_json_btn")
              ) {
                Text("Metin Yapıştır", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    // 3. 📊 VERİ ÖZETİ (MANDATORY V1.2 & V1.3)
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
          text = "📊 Veri Özeti (Aktif İstatistikler)",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(2.dp),
          modifier = Modifier.fillMaxWidth().testTag("data_summary_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            DataStatRow(emoji = "🏡", label = "Kayıtlı Arılık Sayısı", value = "${stats.apiariesCount} Arılık")
            DataStatRow(emoji = "🐝", label = "Aktif Kovan Sayısı", value = "${stats.activeHivesCount} Kovan")
            DataStatRow(emoji = "📦", label = "Arşivlenmiş Kovan Sayısı", value = "${stats.archivedHivesCount} Kovan")
            DataStatRow(emoji = "👑", label = "Kayıtlı Ana Arı Sayısı", value = "${allQueens.size} Ana Arı")
            DataStatRow(emoji = "🐝", label = "Oğul / Bölme Olayları", value = "${allSwarmEvents.size} Olay")
            DataStatRow(emoji = "🩺", label = "Hastalık / Sorun Kayıtları", value = "${allIssues.size} (${allIssues.count { it.status != "Çözüldü" }} Aktif)")
            DataStatRow(emoji = "🔍", label = "Kovan Kontrol Kaydı", value = "${stats.inspectionsCount} Kontrol")
            DataStatRow(emoji = "🍯", label = "Besleme Kaydı", value = "${stats.feedingsCount} Besleme")
            DataStatRow(emoji = "📷", label = "Fotoğraf Kaydı (Metadata)", value = "${stats.photosCount} Fotoğraf")
            DataStatRow(emoji = "📅", label = "Hatırlatıcı Kaydı", value = "${stats.remindersCount} Görev")
            DataStatRow(emoji = "🔒", label = "Kovan Numaralama Koruması", value = "Aktif (MAX+1 Sıralı)")
          }
        }
      }
    }

    // 4. Feedback & Support
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
          text = "💬 İletişim & Destek",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Görüş, öneri, eksik özellik veya karşılaştığınız hataları bize ileterek Arı Takip'in gelişmesine katkıda bulunabilirsiniz.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
              onClick = {
                try {
                  val appVersion = "1.0.0 (Build 1)"
                  val androidVersion = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
                  val deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                  val emailBody = "\n\n---\nCihaz Bilgisi:\n• Uygulama: Arı Takip v$appVersion\n• Sistem: $androidVersion\n• Cihaz: $deviceModel\n"

                  val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:gulercrafts@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Arı Takip - Geri Bildirim")
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                  }
                  context.startActivity(Intent.createChooser(emailIntent, "Geri Bildirim Gönder"))
                } catch (e: Exception) {
                  // Fallback
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("send_feedback_btn")
            ) {
              Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Geri Bildirim / Bize Ulaşın", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 5. App Version & Credits
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🐝 Arı Takip",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Black,
          color = HoneyGoldDark
        )
        Text(
          text = "Sürüm 1.0.0 • Google Play Yayın Sürümü",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Türkiye arıcıları için özel tasarlanmıştır 🇹🇷",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Developer: Hakan GÜLER",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = HoneyGoldDark
        )
      }
    }
  }

  // ---------------- MODALS & DIALOGS ----------------

  // PRO Upgrade Dialog
  if (showProDialog) {
    ProUpgradeDialog(
      onDismiss = { showProDialog = false }
    )
  }

  // Edit Beekeeper Name Dialog
  if (showEditNameDialog) {
    var tempName by remember { mutableStateOf(beekeeperName) }
    AlertDialog(
      onDismissRequest = { showEditNameDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("👨‍🌾 ", fontSize = 20.sp)
          Text("Arıcı Adı", fontWeight = FontWeight.Bold, color = HoneyGoldDark)
        }
      },
      text = {
        Column {
          Text(
            text = "Uygulama profilinizde ve raporlarınızda görünecek arıcı adınızı girin:",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            placeholder = { Text("Örn: Mehmet Demir, Balcı Hasan") },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
              focusedBorderColor = HoneyGoldPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("beekeeper_name_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateBeekeeperName(tempName)
            showEditNameDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
          modifier = Modifier.testTag("save_beekeeper_name_btn")
        ) {
          Text("Kaydet", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditNameDialog = false }) {
          Text("İptal")
        }
      }
    )
  }

  // Import / Restore Modal
  if (showImportModal) {
    AlertDialog(
      onDismissRequest = { showImportModal = false },
      title = {
        Text("📥 Yedekten Geri Yükle", fontWeight = FontWeight.Bold, color = HoneyGoldDark)
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (previewPayload == null && importErrorMessage == null) {
            // Text area paste input
            Text(
              text = "Yedek JSON metnini aşağıya yapıştırın:",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
              value = importedJsonContent ?: "",
              onValueChange = {
                importedJsonContent = it
                val preview = viewModel.parseBackupForPreview(it)
                if (preview != null) {
                  previewPayload = preview
                  importErrorMessage = null
                }
              },
              placeholder = { Text("{\"backupVersion\": 1, ...}", fontSize = 11.sp) },
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("import_paste_textfield"),
              maxLines = 6
            )
          } else if (importErrorMessage != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFFE4E6),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "❌ ", fontSize = 16.sp)
                Text(
                  text = importErrorMessage ?: "",
                  color = Color(0xFF9F1239),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          if (previewPayload != null) {
            val p = previewPayload!!
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = HoneyAmberContainer,
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "✓ Geçerli Arı Takip Yedeği (v${p.appVersion})",
                  fontWeight = FontWeight.Bold,
                  color = HoneyOnAmberContainer,
                  fontSize = 13.sp
                )
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
                Text(
                  text = "Tarih: ${sdf.format(Date(p.createdAt))}",
                  fontSize = 11.sp,
                  color = HoneyOnAmberContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "• ${p.apiaries.size} Arılık, ${p.hives.size} Kovan",
                  fontSize = 12.sp,
                  color = HoneyOnAmberContainer
                )
                Text(
                  text = "• ${p.inspections.size} Kontrol, ${p.feedings.size} Besleme, ${p.photos.size} Fotoğraf",
                  fontSize = 12.sp,
                  color = HoneyOnAmberContainer
                )
              }
            }

            Text(
              text = "Geri Yükleme Yöntemi Seçin:",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 6.dp)
            )

            // Option 1: Merge (Recommended)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedImportMode = ImportMode.MERGE }
                .padding(vertical = 4.dp)
            ) {
              RadioButton(
                selected = selectedImportMode == ImportMode.MERGE,
                onClick = { selectedImportMode = ImportMode.MERGE },
                colors = RadioButtonDefaults.colors(selectedColor = HoneyGoldDark)
              )
              Column {
                Text("Verileri birleştir (Önerilen)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Mevcut kovanlar ve numaralar korunur, yedekten eksik kayıtlar eklenir.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            // Option 2: Overwrite
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedImportMode = ImportMode.OVERWRITE }
                .padding(vertical = 4.dp)
            ) {
              RadioButton(
                selected = selectedImportMode == ImportMode.OVERWRITE,
                onClick = { selectedImportMode = ImportMode.OVERWRITE },
                colors = RadioButtonDefaults.colors(selectedColor = HoneyGoldDark)
              )
              Column {
                Text("Mevcut verilerin üzerine yaz", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                Text("Mevcut tüm kayıtlar silinir, sadece yedekteki veriler yüklenir.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      },
      confirmButton = {
        if (previewPayload != null) {
          Button(
            onClick = {
              showImportConfirmDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark),
            modifier = Modifier.testTag("proceed_restore_btn")
          ) {
            Text("Devam Et", fontWeight = FontWeight.Bold)
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showImportModal = false }) {
          Text("Vazgeç")
        }
      }
    )
  }

  // Safety Confirmation Dialog for Import
  if (showImportConfirmDialog && importedJsonContent != null) {
    val isOverwrite = selectedImportMode == ImportMode.OVERWRITE
    val title = if (isOverwrite) "⚠️ Tüm Verilerin Üzerine Yazılacak" else "Verileri Birleştir"
    val msg = if (isOverwrite) {
      "Bu işlem GERİ ALINAMAZ. Cihazınızdaki mevcut tüm kovan ve kontrol kayıtları silinecektir ve yedekteki ${previewPayload?.hives?.size ?: 0} kovan yüklenecektir. Devam etmek istiyor musunuz?"
    } else {
      "Bu işlem mevcut verilerinize yeni kayıtlar ekleyebilir. Kovan numaralarınız bozulmadan korunacaktır. Devam etmek istiyor musunuz?"
    }

    ConfirmationDialog(
      title = title,
      message = msg,
      confirmButtonText = if (isOverwrite) "Evet, Üzerine Yaz" else "Evet, Birleştir",
      onConfirm = {
        showImportConfirmDialog = false
        showImportModal = false
        viewModel.restoreFromBackupJson(
          jsonString = importedJsonContent!!,
          mode = selectedImportMode
        )
      },
      onDismiss = { showImportConfirmDialog = false }
    )
  }

  // Text Backup Viewer / Share Dialog
  if (showExportTextDialog) {
    AlertDialog(
      onDismissRequest = { showExportTextDialog = false },
      title = { Text(exportDialogTitle, fontWeight = FontWeight.Bold, color = HoneyGoldDark) },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Yedek verinizi kopyalayabilir veya cihazınızdaki diğer uygulamalarla paylaşabilirsiniz.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
          ) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
              item {
                Text(
                  text = exportTextContent,
                  color = Color(0xFF4ADE80),
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            shareTextContent(context, exportTextContent, "AriTakip_Yedek.json", "application/json")
            showExportTextDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldDark)
        ) {
          Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Paylaş", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportTextDialog = false }) {
          Text("Kapat")
        }
      }
    )
  }
}

private fun shareTextContent(context: Context, content: String, title: String, mimeType: String) {
  try {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_TEXT, content)
      putExtra(Intent.EXTRA_TITLE, title)
    }
    val shareIntent = Intent.createChooser(sendIntent, "Yedek Dosyasını Paylaş")
    context.startActivity(shareIntent)
  } catch (e: Exception) {
    // Handled
  }
}

@Composable
fun DataStatRow(emoji: String, label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(text = emoji, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = HoneyGoldDark
    )
  }
}
