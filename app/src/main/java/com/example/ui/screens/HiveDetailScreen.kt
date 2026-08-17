package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FeedingEntity
import com.example.data.local.entity.InspectionEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.QueenEntity
import com.example.data.local.entity.SwarmEntity
import com.example.ui.components.ActiveQueenCard
import com.example.ui.components.AddIssueDialog
import com.example.ui.components.AddSwarmEventDialog
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GalleryPhotoItem
import com.example.ui.components.IssueItemCard
import com.example.ui.components.IssueOverviewCard
import com.example.ui.components.PhotoCaptureSection
import com.example.ui.components.PhotoGalleryDialog
import com.example.ui.components.PhotoThumbnailCard
import com.example.ui.components.QueenEditDialog
import com.example.ui.components.QueenHistoryItemCard
import com.example.ui.components.SwarmEventItemCard
import com.example.ui.components.SwarmOverviewCard
import com.example.ui.components.getCategoryEmoji
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HiveDetailScreen(
  viewModel: BeeViewModel,
  hiveId: String,
  onNavigateBack: () -> Unit,
  onNavigateToEditHive: (String) -> Unit,
  onNavigateToAddInspection: (String) -> Unit,
  onNavigateToAddFeeding: (String) -> Unit
) {
  val allHives by viewModel.allHives.collectAsStateWithLifecycle()
  val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
  val inspections by viewModel.inspections.collectAsStateWithLifecycle()
  val feedings by viewModel.feedings.collectAsStateWithLifecycle()
  val photos by viewModel.photos.collectAsStateWithLifecycle()
  val allQueens by viewModel.allQueens.collectAsStateWithLifecycle()
  val allSwarmEvents by viewModel.allSwarmEvents.collectAsStateWithLifecycle()
  val allIssues by viewModel.allIssues.collectAsStateWithLifecycle()

  val hive = allHives.find { it.id == hiveId }
  val apiary = apiaries.find { it.id == hive?.apiaryId }

  val hiveInspections = remember(inspections, hiveId) {
    inspections.filter { it.hiveId == hiveId }.sortedByDescending { it.inspectionDate }
  }
  val hiveFeedings = remember(feedings, hiveId) {
    feedings.filter { it.hiveId == hiveId }.sortedByDescending { it.feedingDate }
  }
  val hiveQueens = remember(allQueens, hiveId) {
    allQueens.filter { it.hiveId == hiveId }.sortedByDescending { it.installedDate }
  }
  val activeQueen = remember(hiveQueens) {
    hiveQueens.find { it.status != "Öldü" && it.status != "Değiştirildi" } ?: hiveQueens.firstOrNull()
  }
  val hiveQueenHistory = remember(hiveQueens, activeQueen) {
    hiveQueens.filter { it.id != activeQueen?.id }
  }
  val hiveSwarmEvents = remember(allSwarmEvents, hiveId) {
    allSwarmEvents.filter { it.hiveId == hiveId || it.relatedHiveId == hiveId }.sortedByDescending { it.eventDate }
  }
  val hiveIssues = remember(allIssues, hiveId) {
    allIssues.filter { it.hiveId == hiveId }.sortedByDescending { it.detectedDate }
  }

  val latestInspection = hiveInspections.firstOrNull()

  // Collect all gallery photos for this hive
  val galleryPhotos = remember(hiveInspections, hiveFeedings, photos, hiveId) {
    val list = mutableListOf<GalleryPhotoItem>()
    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))

    // From photos table
    photos.filter { it.hiveId == hiveId || it.targetId == hiveId }.forEach { p ->
      list.add(GalleryPhotoItem(uri = p.localUri, caption = p.notes.ifBlank { "Kovan Fotoğrafı" }, date = dateFormat.format(Date(p.date))))
    }

    // From inspection photo URIs
    hiveInspections.forEach { insp ->
      if (insp.photoUris.isNotBlank()) {
        insp.photoUris.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { uri ->
          list.add(
            GalleryPhotoItem(
              uri = uri,
              caption = "Kovan Kontrol Fotoğrafı",
              date = dateFormat.format(Date(insp.inspectionDate))
            )
          )
        }
      }
    }

    // From feedings
    hiveFeedings.forEach { f ->
      if (!f.photoUri.isNullOrBlank()) {
        list.add(
          GalleryPhotoItem(
            uri = f.photoUri,
            caption = "Besleme: ${f.feedingType}",
            date = dateFormat.format(Date(f.feedingDate))
          )
        )
      }
    }

    list
  }

  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var showMenu by remember { mutableStateOf(false) }
  var showArchiveConfirm by remember { mutableStateOf(false) }
  var showFeaturePreviewDialog by remember { mutableStateOf<String?>(null) }
  var selectedGalleryIndex by remember { mutableStateOf<Int?>(null) }

  // V1.3 Dialog States
  var showQueenEditDialog by remember { mutableStateOf(false) }
  var showAddSwarmDialog by remember { mutableStateOf(false) }
  var showAddIssueDialog by remember { mutableStateOf(false) }

  if (hive == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Kovan bulunamadı.")
    }
    return
  }

  val isArchived = hive.status == "archived"
  val queenColor = getQueenColorForYear(hive.queenYear)
  val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr", "TR")) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("hive_detail_screen"),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // Top App Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("detail_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
          }
          Spacer(modifier = Modifier.width(4.dp))
          Column {
            Text(
              text = "🐝 Kovan ${hive.hiveNumber}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = HoneyGoldDark
            )
            Text(
              text = "Arı Takip — Kovan Detayı",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Box {
          IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("detail_menu_btn")) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Seçenekler")
          }
          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Kovanı Düzenle") },
              leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
              onClick = {
                showMenu = false
                onNavigateToEditHive(hive.id)
              }
            )
            if (!isArchived) {
              DropdownMenuItem(
                text = { Text("Kovanı Arşivle") },
                leadingIcon = { Icon(Icons.Filled.Archive, contentDescription = null) },
                onClick = {
                  showMenu = false
                  showArchiveConfirm = true
                }
              )
            } else {
              DropdownMenuItem(
                text = { Text("Tekrar Aktifleştir") },
                leadingIcon = { Icon(Icons.Filled.Unarchive, contentDescription = null) },
                onClick = {
                  showMenu = false
                  viewModel.unarchiveHive(hive.id, hive.hiveNumber)
                }
              )
            }
          }
        }
      }
    }

    // Hero Hive Overview Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(if (isArchived) EarthWoodContainer else HoneyAmberContainer),
                contentAlignment = Alignment.Center
              ) {
                Text(text = "🐝", fontSize = 28.sp)
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = "KOVAN ${hive.hiveNumber}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Black,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "🏡 ${apiary?.name ?: "Arılık"}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isArchived) EarthWoodContainer else ForestGreenContainer
            ) {
              Text(
                text = if (isArchived) "📦 Arşivde" else "🟢 Aktif",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant else ForestOnGreenContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Detail badges grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            DetailChip(
              emoji = "👑",
              label = "Ana Arı",
              value = "${hive.queenYear} ${hive.queenBreed}",
              dotColor = queenColor,
              modifier = Modifier.weight(1f)
            )
            DetailChip(
              emoji = "💪",
              label = "Koloni Gücü",
              value = hive.colonyStrength,
              modifier = Modifier.weight(1f)
            )
            DetailChip(
              emoji = "🪵",
              label = "Kovan Tipi",
              value = hive.hiveType,
              modifier = Modifier.weight(1f)
            )
          }

          if (hive.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "📝 ${hive.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      }
    }

    // ---------------- SON KONTROL (Last Inspection Summary Card - V1.1 Section 8) ----------------
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (latestInspection != null) HoneyAmberContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
          .testTag("last_inspection_summary_card")
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = "📋", fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
              Text(
                text = "SON KONTROL ÖZETİ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = HoneyOnAmberContainer
              )
            }
            if (latestInspection != null) {
              val shortDate = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date(latestInspection.inspectionDate))
              Text(
                text = shortDate,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = HoneyOnAmberContainer.copy(alpha = 0.8f)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (latestInspection != null) {
            // Metrics summary row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (latestInspection.queenSeen) ForestGreenContainer else Color(0xFFFFE4E6),
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = "👑 Ana",
                    fontSize = 10.sp,
                    color = if (latestInspection.queenSeen) ForestOnGreenContainer else Color(0xFF9F1239)
                  )
                  Text(
                    text = if (latestInspection.queenSeen) "Görüldü" else "Görülmedi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (latestInspection.queenSeen) ForestOnGreenContainer else Color(0xFF9F1239)
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(text = "🍯 Bal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(text = latestInspection.honeyStatus, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(text = "🌻 Polen", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(text = latestInspection.pollenStatus, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f)
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(text = "💪 Koloni", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(text = latestInspection.colonyStrength, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }
            }

            if (latestInspection.notes.isNotBlank()) {
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "📝 Not: ${latestInspection.notes}",
                fontSize = 12.sp,
                color = HoneyOnAmberContainer,
                maxLines = 2
              )
            }
          } else {
            Text(
              text = "Bu kovana henüz kontrol kaydı girilmedi. İlk kontrolü yaparak koloni durumunu kaydedin.",
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // ---------------- PROMINENT QUICK ACTIONS (Section 7) ----------------
    item {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ActionTile(
            emoji = "🔍",
            label = "HIZLI KONTROL",
            color = ForestGreenSecondary,
            textColor = Color.White,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToAddInspection(hive.id) },
            testTag = "detail_action_inspection"
          )
          ActionTile(
            emoji = "🍯",
            label = "BESLEME EKLE",
            color = HoneyGoldDark,
            textColor = Color.White,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateToAddFeeding(hive.id) },
            testTag = "detail_action_feeding"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary feature action chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            SecondaryActionChip(
              emoji = "👑",
              label = "Ana Arı Yönetimi",
              onClick = { selectedTabIndex = 1 }
            )
          }
          item {
            SecondaryActionChip(
              emoji = "🐝",
              label = "Oğul / Bölme Ekle",
              onClick = { showAddSwarmDialog = true }
            )
          }
          item {
            SecondaryActionChip(
              emoji = "🩺",
              label = "Hastalık / Sorun Ekle",
              onClick = { showAddIssueDialog = true }
            )
          }
          item {
            SecondaryActionChip(
              emoji = "🍯",
              label = "Hasat Kaydı",
              onClick = { showFeaturePreviewDialog = "Bal Hasadı ve Verim Takibi (V2)" }
            )
          }
        }
      }
    }

    // ---------------- TABS: Scrollable Tab Row with 7 dedicated tabs ----------------
    item {
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = HoneyGoldDark,
        edgePadding = 16.dp,
        modifier = Modifier.padding(top = 8.dp)
      ) {
        Tab(
          selected = selectedTabIndex == 0,
          onClick = { selectedTabIndex = 0 },
          text = { Text("📜 Geçmiş (${hiveInspections.size + hiveFeedings.size + hiveSwarmEvents.size + hiveIssues.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_history")
        )
        Tab(
          selected = selectedTabIndex == 1,
          onClick = { selectedTabIndex = 1 },
          text = { Text("👑 Ana Arı", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_queen")
        )
        Tab(
          selected = selectedTabIndex == 2,
          onClick = { selectedTabIndex = 2 },
          text = { Text("🐝 Oğul / Bölme (${hiveSwarmEvents.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_swarms")
        )
        Tab(
          selected = selectedTabIndex == 3,
          onClick = { selectedTabIndex = 3 },
          text = { Text("🩺 Hastalık / Sorun (${hiveIssues.count { it.status != "Çözüldü" }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_issues")
        )
        Tab(
          selected = selectedTabIndex == 4,
          onClick = { selectedTabIndex = 4 },
          text = { Text("🔍 Kontrol (${hiveInspections.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_inspections")
        )
        Tab(
          selected = selectedTabIndex == 5,
          onClick = { selectedTabIndex = 5 },
          text = { Text("🍯 Besleme (${hiveFeedings.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_feedings")
        )
        Tab(
          selected = selectedTabIndex == 6,
          onClick = { selectedTabIndex = 6 },
          text = { Text("📷 Galeri (${galleryPhotos.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
          modifier = Modifier.testTag("tab_gallery")
        )
      }
    }

    // Tab Content
    when (selectedTabIndex) {
      0 -> {
        // Combined Timeline (Inspections, Feedings, Swarms, Issues)
        val timelineList = buildList {
          hiveInspections.forEach { add(TimelineItem.InspectionItem(it)) }
          hiveFeedings.forEach { add(TimelineItem.FeedingItem(it)) }
          hiveSwarmEvents.forEach { add(TimelineItem.SwarmItem(it)) }
          hiveIssues.forEach { add(TimelineItem.IssueItem(it)) }
        }.sortedByDescending { it.date }

        if (timelineList.isEmpty()) {
          item {
            EmptyStateCard(
              title = "Henüz işlem kaydı yok",
              description = "Bu kovana ait kontroller, beslemeler, oğul olayları ve hastalık kayıtları kronolojik olarak burada listelenecektir.",
              buttonText = "+ İlk Kontrolü Yap",
              iconEmoji = "🔍",
              onButtonClick = { onNavigateToAddInspection(hive.id) },
              testTag = "empty_timeline_card"
            )
          }
        } else {
          items(
            items = timelineList,
            key = {
              when (it) {
                is TimelineItem.InspectionItem -> "insp_${it.inspection.id}"
                is TimelineItem.FeedingItem -> "feed_${it.feeding.id}"
                is TimelineItem.SwarmItem -> "swarm_${it.swarm.id}"
                is TimelineItem.IssueItem -> "issue_${it.issue.id}"
              }
            }
          ) { item ->
            when (item) {
              is TimelineItem.InspectionItem -> InspectionTimelineCard(
                inspection = item.inspection,
                onPhotoClick = { uri ->
                  val idx = galleryPhotos.indexOfFirst { it.uri == uri }
                  if (idx >= 0) selectedGalleryIndex = idx
                }
              )
              is TimelineItem.FeedingItem -> FeedingTimelineCard(
                feeding = item.feeding,
                onPhotoClick = { uri ->
                  val idx = galleryPhotos.indexOfFirst { it.uri == uri }
                  if (idx >= 0) selectedGalleryIndex = idx
                }
              )
              is TimelineItem.SwarmItem -> {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                  SwarmEventItemCard(
                    event = item.swarm,
                    onDeleteClick = { viewModel.deleteSwarmEvent(item.swarm.id) }
                  )
                }
              }
              is TimelineItem.IssueItem -> {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                  IssueItemCard(
                    issue = item.issue,
                    onResolveClick = { viewModel.markIssueResolved(item.issue.id) },
                    onDeleteClick = { viewModel.deleteIssue(item.issue.id) }
                  )
                }
              }
            }
          }
        }
      }
      1 -> {
        // 👑 Ana Arı Tab
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            ActiveQueenCard(
              queen = activeQueen,
              defaultBreed = hive.queenBreed,
              defaultYear = hive.queenYear,
              onEditQueenClick = { showQueenEditDialog = true }
            )

            if (hiveQueenHistory.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "📜 Geçmiş Ana Arılar (${hiveQueenHistory.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              hiveQueenHistory.forEach { q ->
                QueenHistoryItemCard(
                  queen = q,
                  onDeleteClick = { viewModel.deleteQueen(q.id) }
                )
              }
            }
          }
        }
      }
      2 -> {
        // 🐝 Oğul / Bölme Tab
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            SwarmOverviewCard(
              events = hiveSwarmEvents,
              onAddEventClick = { showAddSwarmDialog = true }
            )

            if (hiveSwarmEvents.isEmpty()) {
              EmptyStateCard(
                title = "Oğul / Bölme Kaydı Yok",
                description = "Bu kovanda henüz oğul eğilimi veya bölme işlemi kaydedilmedi.",
                buttonText = "+ Oğul / Bölme Ekle",
                iconEmoji = "🐝",
                onButtonClick = { showAddSwarmDialog = true },
                testTag = "empty_swarms_card"
              )
            } else {
              Text(
                text = "📋 Olay Geçmişi (${hiveSwarmEvents.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              hiveSwarmEvents.forEach { event ->
                SwarmEventItemCard(
                  event = event,
                  onDeleteClick = { viewModel.deleteSwarmEvent(event.id) }
                )
              }
            }
          }
        }
      }
      3 -> {
        // 🩺 Hastalık / Sorun Tab
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            IssueOverviewCard(
              issues = hiveIssues,
              onAddIssueClick = { showAddIssueDialog = true }
            )

            if (hiveIssues.isEmpty()) {
              EmptyStateCard(
                title = "Hastalık veya Sorun Yok",
                description = "Bu kovana ait bildirilmiş aktif veya geçmiş sağlık sorunu bulunmuyor.",
                buttonText = "+ Yeni Sorun Bildir",
                iconEmoji = "🌿",
                onButtonClick = { showAddIssueDialog = true },
                testTag = "empty_issues_card"
              )
            } else {
              Text(
                text = "📋 Sağlık ve Müdahale Kayıtları (${hiveIssues.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              hiveIssues.forEach { issue ->
                IssueItemCard(
                  issue = issue,
                  onResolveClick = { viewModel.markIssueResolved(issue.id) },
                  onDeleteClick = { viewModel.deleteIssue(issue.id) }
                )
              }
            }
          }
        }
      }
      4 -> {
        // Inspections list only
        if (hiveInspections.isEmpty()) {
          item {
            EmptyStateCard(
              title = "Henüz kontrol yapılmamış",
              description = "Kovan kontrolü yaparak ana arı varlığı, yavru durumu, bal ve polen seviyesini kaydedin.",
              buttonText = "+ Kontrol Ekle",
              iconEmoji = "🔍",
              onButtonClick = { onNavigateToAddInspection(hive.id) },
              testTag = "empty_inspection_tab_card"
            )
          }
        } else {
          items(items = hiveInspections, key = { it.id }) { inspection ->
            InspectionTimelineCard(
              inspection = inspection,
              onPhotoClick = { uri ->
                val idx = galleryPhotos.indexOfFirst { it.uri == uri }
                if (idx >= 0) selectedGalleryIndex = idx
              }
            )
          }
        }
      }
      5 -> {
        // Feedings list only
        if (hiveFeedings.isEmpty()) {
          item {
            EmptyStateCard(
              title = "Henüz besleme yapılmamış",
              description = "Şurup, kek, protein keki veya su besleme kayıtlarını ekleyin.",
              buttonText = "+ Besleme Ekle",
              iconEmoji = "🍯",
              onButtonClick = { onNavigateToAddFeeding(hive.id) },
              testTag = "empty_feeding_tab_card"
            )
          }
        } else {
          items(items = hiveFeedings, key = { it.id }) { feeding ->
            FeedingTimelineCard(
              feeding = feeding,
              onPhotoClick = { uri ->
                val idx = galleryPhotos.indexOfFirst { it.uri == uri }
                if (idx >= 0) selectedGalleryIndex = idx
              }
            )
          }
        }
      }
      6 -> {
        // Photo Gallery Grid (V1.1 Section 11 & V1.3.2 Camera Support)
        item {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            PhotoCaptureSection(
              title = "📷 Kovana Fotoğraf Çek / Ekle",
              photoPaths = emptyList(),
              onPhotosUpdated = { newPhotos ->
                newPhotos.forEach { uri ->
                  viewModel.savePhoto(
                    hiveId = hive.id,
                    apiaryId = hive.apiaryId,
                    targetType = "HIVE",
                    targetId = hive.id,
                    uri = uri,
                    caption = "Kovan Fotoğrafı"
                  )
                }
              },
              allowMultiple = true
            )
          }
        }

        if (galleryPhotos.isEmpty()) {
          item {
            EmptyStateCard(
              title = "Henüz fotoğraf eklenmemiş",
              description = "Kovan kontrolü yaparken, besleme eklerken veya yukarıdaki butonla doğrudan fotoğraf çekerek buraya ekleyebilirsiniz.",
              buttonText = "+ Kontrol Yap & Fotoğraf Çek",
              iconEmoji = "📷",
              onButtonClick = { onNavigateToAddInspection(hive.id) },
              testTag = "empty_gallery_tab_card"
            )
          }
        } else {
          item {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "📸 Toplam ${galleryPhotos.size} Fotoğraf",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 10.dp)
              )
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                galleryPhotos.forEachIndexed { index, photo ->
                  PhotoThumbnailCard(
                    uri = photo.uri,
                    caption = photo.caption,
                    onClick = { selectedGalleryIndex = index }
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // V1.3 Dialogs
  if (showQueenEditDialog) {
    QueenEditDialog(
      currentQueen = activeQueen,
      defaultBreed = hive.queenBreed,
      defaultYear = hive.queenYear,
      onDismiss = { showQueenEditDialog = false },
      onSave = { status, year, breed, markingColor, source, notes, isNewActive ->
        viewModel.saveQueen(
          hiveId = hive.id,
          apiaryId = hive.apiaryId,
          status = status,
          year = year,
          breed = breed,
          markingColor = markingColor,
          source = source,
          installedDate = System.currentTimeMillis(),
          notes = notes,
          isNewActiveQueen = isNewActive
        ) {
          showQueenEditDialog = false
        }
      }
    )
  }

  if (showAddSwarmDialog) {
    AddSwarmEventDialog(
      availableHives = allHives,
      currentHiveId = hive.id,
      onDismiss = { showAddSwarmDialog = false },
      onSave = { eventType, tendencyLevel, queenCellsStatus, relatedHiveId, relatedHiveNumber, actionTaken, notes ->
        viewModel.addSwarmEvent(
          hiveId = hive.id,
          apiaryId = hive.apiaryId,
          eventType = eventType,
          eventDate = System.currentTimeMillis(),
          tendencyLevel = tendencyLevel,
          queenCellsStatus = queenCellsStatus,
          relatedHiveId = relatedHiveId,
          relatedHiveNumber = relatedHiveNumber,
          actionTaken = actionTaken,
          notes = notes
        ) {
          showAddSwarmDialog = false
        }
      }
    )
  }

  if (showAddIssueDialog) {
    AddIssueDialog(
      onDismiss = { showAddIssueDialog = false },
      onSave = { category, severity, status, treatmentNotes, notes ->
        viewModel.addIssue(
          hiveId = hive.id,
          apiaryId = hive.apiaryId,
          category = category,
          severity = severity,
          status = status,
          detectedDate = System.currentTimeMillis(),
          treatmentNotes = treatmentNotes,
          notes = notes
        ) {
          showAddIssueDialog = false
        }
      }
    )
  }

  // Fullscreen Photo Gallery Dialog
  if (selectedGalleryIndex != null && selectedGalleryIndex!! in galleryPhotos.indices) {
    PhotoGalleryDialog(
      photos = galleryPhotos,
      initialIndex = selectedGalleryIndex!!,
      onDismiss = { selectedGalleryIndex = null }
    )
  }

  // Archive confirmation dialog
  if (showArchiveConfirm) {
    ConfirmationDialog(
      title = "Kovanı Arşivle",
      message = "Kovan ${hive.hiveNumber} arşivlenecektir. Kovanın geçmiş kontrol, besleme ve fotoğraf kayıtları kesinlikle silinmeyecektir. Devam etmek istiyor musunuz?",
      confirmButtonText = "Evet, Arşivle",
      onConfirm = {
        viewModel.archiveHive(hive.id, hive.hiveNumber) {
          showArchiveConfirm = false
        }
      },
      onDismiss = { showArchiveConfirm = false }
    )
  }

  // Feature preview dialog for V2
  if (showFeaturePreviewDialog != null) {
    AlertDialog(
      onDismissRequest = { showFeaturePreviewDialog = null },
      title = { Text(text = "✨ $showFeaturePreviewDialog", fontWeight = FontWeight.Bold) },
      text = {
        Text("Bu modülün veri şeması ve arayüz altyapısı hazırlandı. V2 sürümünde tam entegrasyon ile aktifleşecektir.")
      },
      confirmButton = {
        TextButton(onClick = { showFeaturePreviewDialog = null }) {
          Text("Tamam", fontWeight = FontWeight.Bold, color = HoneyGoldDark)
        }
      }
    )
  }
}

sealed class TimelineItem(val date: Long) {
  data class InspectionItem(val inspection: InspectionEntity) : TimelineItem(inspection.inspectionDate)
  data class FeedingItem(val feeding: FeedingEntity) : TimelineItem(feeding.feedingDate)
  data class SwarmItem(val swarm: SwarmEntity) : TimelineItem(swarm.eventDate)
  data class IssueItem(val issue: IssueEntity) : TimelineItem(issue.detectedDate)
}

@Composable
fun InspectionTimelineCard(
  inspection: InspectionEntity,
  onPhotoClick: (String) -> Unit = {}
) {
  val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr", "TR")) }
  val dateStr = dateFormat.format(Date(inspection.inspectionDate))

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("inspection_card_${inspection.id}")
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
              .size(36.dp)
              .clip(CircleShape)
              .background(ForestGreenContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "🔍", fontSize = 18.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Kovan Kontrolü",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = dateStr,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (inspection.queenSeen) ForestGreenContainer else HoneyAmberContainer
        ) {
          Text(
            text = if (inspection.queenSeen) "👑 Ana Görüldü" else "⚪ Ana Görülmedi",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (inspection.queenSeen) ForestOnGreenContainer else HoneyOnAmberContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Brood and Colony findings chips
      val findings = buildList {
        if (inspection.broodEgg) add("🥚 Yumurta")
        if (inspection.broodLarva) add("🐛 Larva")
        if (inspection.broodCapped) add("🟫 Kapalı Yavru")
        add("💪 ${inspection.colonyStrength}")
        add("🍯 Bal: ${inspection.honeyStatus}")
        add("🌻 Polen: ${inspection.pollenStatus}")
        add("🐝 ${inspection.behavior}")
      }

      Text(
        text = findings.joinToString(" • "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium
      )

      if (inspection.frameChanges.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "🪵 Çerçeve: ${inspection.frameChanges}",
          style = MaterialTheme.typography.bodySmall,
          color = HoneyGoldDark,
          fontWeight = FontWeight.SemiBold
        )
      }

      if (inspection.problems.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "⚠️ Sorunlar: ${inspection.problems}",
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFE11D48),
          fontWeight = FontWeight.Bold
        )
      }

      if (inspection.weatherSummary.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "🌤️ Hava: ${inspection.weatherSummary}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (inspection.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "📝 ${inspection.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(8.dp)
          )
        }
      }

      // Photos in this inspection
      if (inspection.photoUris.isNotBlank()) {
        val photosList = inspection.photoUris.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (photosList.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(photosList) { uri ->
              PhotoThumbnailCard(
                uri = uri,
                caption = "Kontrol Fotoğrafı",
                onClick = { onPhotoClick(uri) },
                modifier = Modifier.size(70.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun FeedingTimelineCard(
  feeding: FeedingEntity,
  onPhotoClick: (String) -> Unit = {}
) {
  val dateFormat = remember { SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr", "TR")) }
  val dateStr = dateFormat.format(Date(feeding.feedingDate))

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("feeding_card_${feeding.id}")
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
              .size(36.dp)
              .clip(CircleShape)
              .background(HoneyAmberContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "🍯", fontSize = 18.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Besleme: ${feeding.feedingType}",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = dateStr,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = HoneyAmberContainer
        ) {
          Text(
            text = "${feeding.amount} ${feeding.unit}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = HoneyOnAmberContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
          )
        }
      }

      if (feeding.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "📝 ${feeding.notes}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (!feeding.photoUri.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        PhotoThumbnailCard(
          uri = feeding.photoUri,
          caption = "Besleme Fotoğrafı",
          onClick = { onPhotoClick(feeding.photoUri) },
          modifier = Modifier.size(70.dp)
        )
      }
    }
  }
}

@Composable
fun DetailChip(
  emoji: String,
  label: String,
  value: String,
  dotColor: Color? = null,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 16.sp)
        if (dotColor != null) {
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(dotColor)
              .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
          )
        }
      }
      Text(
        text = label,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1
      )
    }
  }
}

@Composable
fun ActionTile(
  emoji: String,
  label: String,
  color: Color,
  textColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = color,
    shadowElevation = 2.dp,
    modifier = modifier
      .height(54.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(text = emoji, fontSize = 20.sp)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        color = textColor,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp
      )
    }
  }
}

@Composable
fun SecondaryActionChip(
  emoji: String,
  label: String,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(text = emoji, fontSize = 14.sp)
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}
