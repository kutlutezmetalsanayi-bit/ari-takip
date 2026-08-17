package com.example.data.repository

import com.example.data.billing.PlanManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ApiaryEntity
import com.example.data.local.entity.FeedingEntity
import com.example.data.local.entity.HiveEntity
import com.example.data.local.entity.InspectionEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.PhotoEntity
import com.example.data.local.entity.QueenEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.SwarmEntity
import com.example.data.local.entity.SyncEntity
import com.example.data.util.BackupPayload
import com.example.data.util.ImportMode
import com.example.data.util.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

data class DatabaseStats(
  val apiariesCount: Int,
  val activeHivesCount: Int,
  val archivedHivesCount: Int,
  val totalHivesCount: Int,
  val inspectionsCount: Int,
  val feedingsCount: Int,
  val photosCount: Int,
  val remindersCount: Int,
  val queensCount: Int = 0,
  val swarmsCount: Int = 0,
  val activeIssuesCount: Int = 0,
  val totalIssuesCount: Int = 0
)

class BeeRepository(private val db: AppDatabase) {
  private val apiaryDao = db.apiaryDao()
  private val hiveDao = db.hiveDao()
  private val inspectionDao = db.inspectionDao()
  private val feedingDao = db.feedingDao()
  private val photoDao = db.photoDao()
  private val reminderDao = db.reminderDao()
  private val syncDao = db.syncDao()
  private val queenDao = db.queenDao()
  private val swarmDao = db.swarmDao()
  private val issueDao = db.issueDao()

  // Mutex to guarantee atomic hive numbering sequentially even under concurrent clicks
  private val hiveNumberMutex = Mutex()

  // --- SYNC QUEUE (Offline-first support) ---
  val pendingSyncCount: Flow<Int> = syncDao.getPendingSyncCountFlow()
  val pendingSyncItems: Flow<List<SyncEntity>> = syncDao.getPendingSyncItemsFlow()

  suspend fun enqueueSync(
    entityType: String,
    operation: String,
    recordId: String,
    payloadJson: String = ""
  ) = withContext(Dispatchers.IO) {
    syncDao.insertSyncItem(
      SyncEntity(
        entityType = entityType,
        operation = operation,
        recordId = recordId,
        payloadJson = payloadJson,
        createdAt = System.currentTimeMillis()
      )
    )
  }

  suspend fun clearPendingSyncQueue() = withContext(Dispatchers.IO) {
    syncDao.clearAll()
  }

  // --- DATABASE STATS (For Data Summary in Settings) ---
  suspend fun getDatabaseStats(): DatabaseStats = withContext(Dispatchers.IO) {
    val apiaries = apiaryDao.getAllApiariesSync()
    val hives = hiveDao.getAllHivesSync()
    val activeHives = hives.count { it.status == "active" }
    val archivedHives = hives.count { it.status == "archived" }
    val inspections = inspectionDao.getAllInspectionsSync()
    val feedings = feedingDao.getAllFeedingsSync()
    val photos = photoDao.getAllPhotosSync()
    val reminders = reminderDao.getAllRemindersSync()
    val queens = queenDao.getAllQueensSync()
    val swarms = swarmDao.getAllSwarmEventsSync()
    val issues = issueDao.getAllIssuesSync()
    val activeIssues = issues.count { it.status != "Çözüldü" }

    DatabaseStats(
      apiariesCount = apiaries.size,
      activeHivesCount = activeHives,
      archivedHivesCount = archivedHives,
      totalHivesCount = hives.size,
      inspectionsCount = inspections.size,
      feedingsCount = feedings.size,
      photosCount = photos.size,
      remindersCount = reminders.size,
      queensCount = queens.size,
      swarmsCount = swarms.size,
      activeIssuesCount = activeIssues,
      totalIssuesCount = issues.size
    )
  }

  // --- BACKUP DATA COLLECTION ---
  suspend fun getAllDataForBackup(): BackupPayload = withContext(Dispatchers.IO) {
    val apiaries = apiaryDao.getAllApiariesSync()
    val hives = hiveDao.getAllHivesSync()
    val inspections = inspectionDao.getAllInspectionsSync()
    val feedings = feedingDao.getAllFeedingsSync()
    val photos = photoDao.getAllPhotosSync()
    val reminders = reminderDao.getAllRemindersSync()
    val queens = queenDao.getAllQueensSync()
    val swarms = swarmDao.getAllSwarmEventsSync()
    val issues = issueDao.getAllIssuesSync()

    BackupPayload(
      backupVersion = 2,
      appVersion = "1.3.0",
      createdAt = System.currentTimeMillis(),
      apiaries = apiaries,
      hives = hives,
      inspections = inspections,
      feedings = feedings,
      photos = photos,
      reminders = reminders,
      queens = queens,
      swarms = swarms,
      issues = issues
    )
  }

  // --- IMPORT & RESTORE LOGIC ---
  suspend fun importBackupData(backupData: BackupPayload, mode: ImportMode): ImportResult =
    withContext(Dispatchers.IO) {
      if (mode == ImportMode.OVERWRITE) {
        // Clear all tables first
        photoDao.clearAllPhotos()
        reminderDao.clearAllReminders()
        feedingDao.clearAllFeedings()
        inspectionDao.clearAllInspections()
        queenDao.clearAllQueens()
        swarmDao.clearAllSwarmEvents()
        issueDao.clearAllIssues()
        hiveDao.clearAllHives()
        apiaryDao.clearAllApiaries()
        syncDao.clearAll()
      }

      // Insert or replace records (MANDATORY: preserve hiveNumber from backup)
      backupData.apiaries.forEach { apiaryDao.insertApiary(it) }
      backupData.hives.forEach { hiveDao.insertHive(it) }
      backupData.inspections.forEach { inspectionDao.insertInspection(it) }
      backupData.feedings.forEach { feedingDao.insertFeeding(it) }
      backupData.photos.forEach { photoDao.insertPhoto(it) }
      backupData.reminders.forEach { reminderDao.insertReminder(it) }
      backupData.queens.forEach { queenDao.insertQueen(it) }
      backupData.swarms.forEach { swarmDao.insertSwarmEvent(it) }
      backupData.issues.forEach { issueDao.insertIssue(it) }

      val modeStr = if (mode == ImportMode.MERGE) "birleştirildi" else "üzerine yazıldı"
      ImportResult(
        mode = mode,
        apiariesCount = backupData.apiaries.size,
        hivesCount = backupData.hives.size,
        inspectionsCount = backupData.inspections.size,
        feedingsCount = backupData.feedings.size,
        photosCount = backupData.photos.size,
        remindersCount = backupData.reminders.size,
        queensCount = backupData.queens.size,
        swarmsCount = backupData.swarms.size,
        issuesCount = backupData.issues.size,
        message = "Yedek başarıyla $modeStr. (${backupData.hives.size} kovan, ${backupData.inspections.size} kontrol aktarıldı)."
      )
    }

  suspend fun clearAllData() = withContext(Dispatchers.IO) {
    photoDao.clearAllPhotos()
    reminderDao.clearAllReminders()
    feedingDao.clearAllFeedings()
    inspectionDao.clearAllInspections()
    queenDao.clearAllQueens()
    swarmDao.clearAllSwarmEvents()
    issueDao.clearAllIssues()
    hiveDao.clearAllHives()
    apiaryDao.clearAllApiaries()
    syncDao.clearAll()
  }

  // --- APIARIES ---
  val activeApiaries: Flow<List<ApiaryEntity>> = apiaryDao.getActiveApiaries()
  val allApiaries: Flow<List<ApiaryEntity>> = apiaryDao.getAllApiaries()

  fun getApiaryById(id: String): Flow<ApiaryEntity?> = apiaryDao.getApiaryById(id)

  suspend fun saveApiary(
    name: String,
    country: String = "Türkiye",
    city: String = "",
    district: String = "",
    address: String = "",
    latitude: Double = 37.1305,
    longitude: Double = 28.3228,
    notes: String = "",
    id: String? = null
  ): ApiaryEntity = withContext(Dispatchers.IO) {
    if (id == null) {
      val apiariesCount = apiaryDao.getAllApiariesSync().size
      if (!PlanManager.canAddApiary(apiariesCount)) {
        throw IllegalStateException("Ücretsiz kullanım sınırına ulaştınız. Ücretsiz planda en fazla 1 arılık ekleyebilirsiniz. 2. arılığı eklemek için Arı Takip PRO'ya yükseltin.")
      }
    }

    val finalAddress = if (address.isNotBlank()) {
      address.trim()
    } else if (city.isNotBlank() && district.isNotBlank()) {
      "$city / $district"
    } else if (city.isNotBlank()) {
      city.trim()
    } else {
      ""
    }

    val apiary = ApiaryEntity(
      id = id ?: UUID.randomUUID().toString(),
      name = name.trim(),
      country = country.trim().ifBlank { "Türkiye" },
      city = city.trim(),
      district = district.trim(),
      address = finalAddress,
      latitude = latitude,
      longitude = longitude,
      notes = notes.trim(),
      updatedAt = System.currentTimeMillis()
    )
    apiaryDao.insertApiary(apiary)
    apiary
  }

  suspend fun deleteApiary(id: String) = withContext(Dispatchers.IO) {
    apiaryDao.deleteApiaryById(id)
  }

  // --- HIVES ---
  val activeHives: Flow<List<HiveEntity>> = hiveDao.getActiveHives()
  val allHives: Flow<List<HiveEntity>> = hiveDao.getAllHives()

  fun getHivesByApiary(apiaryId: String, includeArchived: Boolean = false): Flow<List<HiveEntity>> {
    return if (includeArchived) {
      hiveDao.getAllHivesByApiary(apiaryId)
    } else {
      hiveDao.getActiveHivesByApiary(apiaryId)
    }
  }

  fun getHiveById(id: String): Flow<HiveEntity?> = hiveDao.getHiveById(id)
  suspend fun getHiveByIdSync(id: String): HiveEntity? = hiveDao.getHiveByIdSync(id)

  /**
   * ATOMIC HIVE CREATION & NUMBERING (CRITICAL RULE):
   * Hive numbers are auto-assigned per Apiary using MAX(hiveNumber) + 1.
   * If a hive is archived or deleted, its number is never reused.
   * Hive number cannot be altered by user.
   */
  suspend fun createHive(
    apiaryId: String,
    hiveType: String,
    queenYear: Int,
    queenBreed: String,
    colonyStrength: String,
    notes: String,
    photoUri: String? = null
  ): HiveEntity = withContext(Dispatchers.IO) {
    hiveNumberMutex.withLock {
      val activeHivesCount = hiveDao.getAllHivesSync().count { it.status == "active" }
      if (!PlanManager.canAddActiveHive(activeHivesCount)) {
        throw IllegalStateException("Ücretsiz kullanım sınırına ulaştınız. Ücretsiz planda bir arılıkta en fazla 10 aktif kovan kullanabilirsiniz. 11. kovanı eklemek için Arı Takip PRO'ya yükseltin.")
      }

      val maxNumber = hiveDao.getMaxHiveNumberForApiary(apiaryId) ?: 0
      val nextNumber = maxNumber + 1

      val newHive = HiveEntity(
        id = UUID.randomUUID().toString(),
        apiaryId = apiaryId,
        hiveNumber = nextNumber,
        photoUri = photoUri,
        hiveType = hiveType,
        queenYear = queenYear,
        queenBreed = queenBreed,
        colonyStrength = colonyStrength,
        notes = notes.trim(),
        status = "active",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      hiveDao.insertHive(newHive)
      newHive
    }
  }

  suspend fun updateHive(
    hiveId: String,
    apiaryId: String,
    hiveType: String,
    queenYear: Int,
    queenBreed: String,
    colonyStrength: String,
    notes: String,
    photoUri: String?
  ) = withContext(Dispatchers.IO) {
    val existing = hiveDao.getHiveByIdSync(hiveId) ?: return@withContext
    // Hive number remains strictly preserved!
    val updated = existing.copy(
      apiaryId = apiaryId,
      hiveType = hiveType,
      queenYear = queenYear,
      queenBreed = queenBreed,
      colonyStrength = colonyStrength,
      notes = notes.trim(),
      photoUri = photoUri ?: existing.photoUri,
      updatedAt = System.currentTimeMillis()
    )
    hiveDao.updateHive(updated)
  }

  suspend fun archiveHive(hiveId: String) = withContext(Dispatchers.IO) {
    hiveDao.archiveHive(hiveId)
  }

  suspend fun unarchiveHive(hiveId: String) = withContext(Dispatchers.IO) {
    val activeHivesCount = hiveDao.getAllHivesSync().count { it.status == "active" }
    if (!PlanManager.canAddActiveHive(activeHivesCount)) {
      throw IllegalStateException("Ücretsiz kullanım sınırına ulaştınız. Arşivden kovan açmak için en fazla 10 aktif kovanınız olabilir veya Arı Takip PRO'ya geçebilirsiniz.")
    }
    hiveDao.unarchiveHive(hiveId)
  }

  suspend fun deleteHive(hiveId: String) = withContext(Dispatchers.IO) {
    hiveDao.deleteHiveById(hiveId)
  }

  // --- 👑 QUEEN MANAGEMENT (V1.3) ---
  fun getActiveQueenForHive(hiveId: String): Flow<QueenEntity?> = queenDao.getActiveQueenForHive(hiveId)
  fun getQueenHistoryForHive(hiveId: String): Flow<List<QueenEntity>> = queenDao.getQueenHistoryForHive(hiveId)
  fun getAllQueens(): Flow<List<QueenEntity>> = queenDao.getAllQueens()

  suspend fun saveQueen(
    hiveId: String,
    apiaryId: String,
    status: String,
    year: Int,
    breed: String,
    markingColor: String,
    source: String,
    installedDate: Long,
    notes: String,
    isNewActiveQueen: Boolean = true,
    id: String? = null
  ): QueenEntity = withContext(Dispatchers.IO) {
    if (isNewActiveQueen) {
      queenDao.deactivateQueensForHive(hiveId)
    }

    val queen = QueenEntity(
      id = id ?: UUID.randomUUID().toString(),
      hiveId = hiveId,
      apiaryId = apiaryId,
      status = status,
      year = year,
      breed = breed,
      markingColor = markingColor,
      source = source,
      installedDate = installedDate,
      notes = notes.trim(),
      isActive = isNewActiveQueen,
      createdAt = System.currentTimeMillis()
    )
    queenDao.insertQueen(queen)

    // Sync hive's queen fields
    val hive = hiveDao.getHiveByIdSync(hiveId)
    if (hive != null) {
      hiveDao.updateHive(hive.copy(queenYear = year, queenBreed = breed, updatedAt = System.currentTimeMillis()))
    }

    queen
  }

  suspend fun updateQueen(queen: QueenEntity) = withContext(Dispatchers.IO) {
    queenDao.updateQueen(queen)
  }

  suspend fun deleteQueen(id: String) = withContext(Dispatchers.IO) {
    queenDao.deleteQueenById(id)
  }

  // --- 🐝 SWARM & SPLIT MANAGEMENT (V1.3) ---
  fun getSwarmEventsForHive(hiveId: String): Flow<List<SwarmEntity>> = swarmDao.getSwarmEventsForHive(hiveId)
  fun getRelatedSwarmEventsForHive(hiveId: String): Flow<List<SwarmEntity>> = swarmDao.getRelatedSwarmEventsForHive(hiveId)
  fun getAllSwarmEvents(): Flow<List<SwarmEntity>> = swarmDao.getAllSwarmEvents()

  suspend fun addSwarmEvent(
    hiveId: String,
    apiaryId: String,
    eventType: String,
    eventDate: Long,
    tendencyLevel: String,
    queenCellsStatus: String,
    relatedHiveId: String? = null,
    relatedHiveNumber: Int? = null,
    actionTaken: String = "",
    notes: String = "",
    photoUri: String? = null
  ): SwarmEntity = withContext(Dispatchers.IO) {
    val swarm = SwarmEntity(
      id = UUID.randomUUID().toString(),
      hiveId = hiveId,
      apiaryId = apiaryId,
      eventType = eventType,
      eventDate = eventDate,
      tendencyLevel = tendencyLevel,
      queenCellsStatus = queenCellsStatus,
      relatedHiveId = relatedHiveId,
      relatedHiveNumber = relatedHiveNumber,
      actionTaken = actionTaken.trim(),
      notes = notes.trim(),
      photoUri = photoUri,
      createdAt = System.currentTimeMillis()
    )
    swarmDao.insertSwarmEvent(swarm)
    swarm
  }

  suspend fun updateSwarmEvent(event: SwarmEntity) = withContext(Dispatchers.IO) {
    swarmDao.updateSwarmEvent(event)
  }

  suspend fun deleteSwarmEvent(id: String) = withContext(Dispatchers.IO) {
    swarmDao.deleteSwarmEventById(id)
  }

  // --- 🩺 ISSUES & DISEASES (V1.3) ---
  fun getIssuesForHive(hiveId: String): Flow<List<IssueEntity>> = issueDao.getIssuesForHive(hiveId)
  fun getActiveIssuesForHive(hiveId: String): Flow<List<IssueEntity>> = issueDao.getActiveIssuesForHive(hiveId)
  fun getAllActiveIssues(): Flow<List<IssueEntity>> = issueDao.getAllActiveIssues()
  fun getAllIssues(): Flow<List<IssueEntity>> = issueDao.getAllIssues()

  suspend fun addIssue(
    hiveId: String,
    apiaryId: String,
    category: String,
    severity: String,
    status: String = "Açık",
    detectedDate: Long = System.currentTimeMillis(),
    treatmentNotes: String = "",
    notes: String = "",
    photoUris: String = ""
  ): IssueEntity = withContext(Dispatchers.IO) {
    val issue = IssueEntity(
      id = UUID.randomUUID().toString(),
      hiveId = hiveId,
      apiaryId = apiaryId,
      category = category,
      severity = severity,
      status = status,
      detectedDate = detectedDate,
      resolvedDate = if (status == "Çözüldü") System.currentTimeMillis() else null,
      treatmentNotes = treatmentNotes.trim(),
      notes = notes.trim(),
      photoUris = photoUris,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
    issueDao.insertIssue(issue)
    issue
  }

  suspend fun updateIssue(issue: IssueEntity) = withContext(Dispatchers.IO) {
    issueDao.updateIssue(issue.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun markIssueResolved(id: String, resolutionNotes: String? = null) = withContext(Dispatchers.IO) {
    issueDao.updateIssueStatus(
      id = id,
      status = "Çözüldü",
      resolvedDate = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
  }

  suspend fun deleteIssue(id: String) = withContext(Dispatchers.IO) {
    issueDao.deleteIssueById(id)
  }

  // Helper for International Queen Year Marking Colors
  fun getQueenColorForYear(year: Int): String {
    return when (year % 10) {
      1, 6 -> "Beyaz"
      2, 7 -> "Sarı"
      3, 8 -> "Kırmızı"
      4, 9 -> "Yeşil"
      0, 5 -> "Mavi"
      else -> "İşaretsiz"
    }
  }

  // --- INSPECTIONS ---
  val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()

  fun getInspectionsForHive(hiveId: String): Flow<List<InspectionEntity>> =
    inspectionDao.getInspectionsForHive(hiveId)

  fun getLatestInspectionForHive(hiveId: String): Flow<InspectionEntity?> =
    inspectionDao.getLatestInspectionForHive(hiveId)

  suspend fun saveInspection(
    hiveId: String,
    apiaryId: String,
    queenSeen: Boolean,
    broodEgg: Boolean,
    broodLarva: Boolean,
    broodCapped: Boolean,
    colonyStrength: String,
    honeyStatus: String,
    pollenStatus: String,
    behavior: String,
    frameChanges: String,
    problems: String,
    notes: String,
    weatherSummary: String,
    photoUris: List<String>,
    inspectionDate: Long = System.currentTimeMillis()
  ): InspectionEntity = withContext(Dispatchers.IO) {
    val inspection = InspectionEntity(
      id = UUID.randomUUID().toString(),
      hiveId = hiveId,
      apiaryId = apiaryId,
      inspectionDate = inspectionDate,
      queenSeen = queenSeen,
      broodEgg = broodEgg,
      broodLarva = broodLarva,
      broodCapped = broodCapped,
      colonyStrength = colonyStrength,
      honeyStatus = honeyStatus,
      pollenStatus = pollenStatus,
      behavior = behavior,
      frameChanges = frameChanges.trim(),
      problems = problems.trim(),
      notes = notes.trim(),
      weatherSummary = weatherSummary,
      photoUris = photoUris.joinToString(","),
      createdAt = System.currentTimeMillis()
    )
    inspectionDao.insertInspection(inspection)

    // Also link photos to PhotoEntity repository
    photoUris.forEach { uri ->
      if (uri.isNotBlank()) {
        photoDao.insertPhoto(
          PhotoEntity(
            id = UUID.randomUUID().toString(),
            hiveId = hiveId,
            apiaryId = apiaryId,
            targetType = "INSPECTION",
            targetId = inspection.id,
            localUri = uri,
            date = inspectionDate,
            notes = "Kovan Kontrolü Fotoğrafı"
          )
        )
      }
    }

    // Also update hive's colony strength if changed during inspection
    val hive = hiveDao.getHiveByIdSync(hiveId)
    if (hive != null && hive.colonyStrength != colonyStrength) {
      hiveDao.updateHive(hive.copy(colonyStrength = colonyStrength, updatedAt = System.currentTimeMillis()))
    }

    inspection
  }

  suspend fun deleteInspection(id: String) = withContext(Dispatchers.IO) {
    inspectionDao.deleteInspectionById(id)
  }

  // --- FEEDINGS ---
  val allFeedings: Flow<List<FeedingEntity>> = feedingDao.getAllFeedings()

  fun getFeedingsForHive(hiveId: String): Flow<List<FeedingEntity>> =
    feedingDao.getFeedingsForHive(hiveId)

  suspend fun saveFeeding(
    hiveId: String,
    apiaryId: String,
    feedingType: String,
    amount: Double,
    unit: String,
    notes: String,
    photoUri: String? = null,
    feedingDate: Long = System.currentTimeMillis()
  ): FeedingEntity = withContext(Dispatchers.IO) {
    val feeding = FeedingEntity(
      id = UUID.randomUUID().toString(),
      hiveId = hiveId,
      apiaryId = apiaryId,
      feedingType = feedingType,
      amount = amount,
      unit = unit,
      feedingDate = feedingDate,
      notes = notes.trim(),
      photoUri = photoUri,
      createdAt = System.currentTimeMillis()
    )
    feedingDao.insertFeeding(feeding)

    if (!photoUri.isNullOrBlank()) {
      photoDao.insertPhoto(
        PhotoEntity(
          id = UUID.randomUUID().toString(),
          hiveId = hiveId,
          apiaryId = apiaryId,
          targetType = "FEEDING",
          targetId = feeding.id,
          localUri = photoUri,
          date = feedingDate,
          notes = "$amount $unit $feedingType Beslemesi"
        )
      )
    }

    feeding
  }

  suspend fun deleteFeeding(id: String) = withContext(Dispatchers.IO) {
    feedingDao.deleteFeedingById(id)
  }

  // --- PHOTOS ---
  val allPhotos: Flow<List<PhotoEntity>> = photoDao.getAllPhotos()
  fun getPhotosForHive(hiveId: String): Flow<List<PhotoEntity>> = photoDao.getPhotosForHive(hiveId)

  suspend fun savePhoto(
    hiveId: String?,
    apiaryId: String?,
    targetType: String,
    targetId: String,
    localUri: String,
    notes: String
  ) = withContext(Dispatchers.IO) {
    photoDao.insertPhoto(
      PhotoEntity(
        id = UUID.randomUUID().toString(),
        hiveId = hiveId,
        apiaryId = apiaryId,
        targetType = targetType,
        targetId = targetId,
        localUri = localUri,
        notes = notes
      )
    )
  }

  suspend fun deletePhoto(id: String) = withContext(Dispatchers.IO) {
    photoDao.deletePhotoById(id)
  }

  // --- REMINDERS ---
  val pendingReminders: Flow<List<ReminderEntity>> = reminderDao.getPendingReminders()
  val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

  suspend fun saveReminder(
    title: String,
    date: Long,
    apiaryId: String? = null,
    hiveId: String? = null
  ) = withContext(Dispatchers.IO) {
    reminderDao.insertReminder(
      ReminderEntity(
        id = UUID.randomUUID().toString(),
        title = title.trim(),
        date = date,
        apiaryId = apiaryId,
        hiveId = hiveId
      )
    )
  }

  suspend fun setReminderCompleted(id: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
    reminderDao.setReminderCompleted(id, isCompleted)
  }

  suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
    reminderDao.deleteReminderById(id)
  }

  // --- DEMO DATA LOADER (Clearly marked as DEMO) ---
  suspend fun loadRealisticTurkishDemoData() = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    val dayMillis = 86400000L

    // Apiary 1: Muğla Çam Arılığı
    val apiary1 = ApiaryEntity(
      id = "demo_apiary_mugla",
      name = "🏡 Muğla Çam Arılığı",
      address = "Marmaris / Muğla (Çam Balı Ormanı)",
      latitude = 36.8550,
      longitude = 28.2740,
      notes = "Güneye bakan korunaklı yamaç. Çam balı basımı için ideal konum.",
      createdAt = now - (30 * dayMillis)
    )
    apiaryDao.insertApiary(apiary1)

    // Apiary 2: Kaz Dağları Kestane Arılığı
    val apiary2 = ApiaryEntity(
      id = "demo_apiary_kazdaglari",
      name = "🌲 Kaz Dağları Arılığı",
      address = "Edremit / Balıkesir (Kestane & Kır Çiçeği)",
      latitude = 39.7042,
      longitude = 26.8505,
      notes = "Yüksek rakımlı yayla. Bahar ve yaz polen akımı çok zengin.",
      createdAt = now - (20 * dayMillis)
    )
    apiaryDao.insertApiary(apiary2)

    // Hives for Apiary 1 (Sequential Numbers: 1, 2, 3, 4)
    val hive1 = HiveEntity(
      id = "demo_hive_1",
      apiaryId = apiary1.id,
      hiveNumber = 1,
      hiveType = "Langstroth",
      queenYear = 2025,
      queenBreed = "Kafkas",
      colonyStrength = "Çok Güçlü",
      notes = "2 Katlı üretim kovanı. Bal verimi yüksek.",
      createdAt = now - (28 * dayMillis)
    )
    val hive2 = HiveEntity(
      id = "demo_hive_2",
      apiaryId = apiary1.id,
      hiveNumber = 2,
      hiveType = "Langstroth",
      queenYear = 2024,
      queenBreed = "Karniyol",
      colonyStrength = "Güçlü",
      notes = "Sakin mizaçlı, petek örme hızı mükemmel.",
      createdAt = now - (27 * dayMillis)
    )
    val hive3 = HiveEntity(
      id = "demo_hive_3",
      apiaryId = apiary1.id,
      hiveNumber = 3,
      hiveType = "Dadant",
      queenYear = 2025,
      queenBreed = "Anadolu",
      colonyStrength = "Orta",
      notes = "Yeni bölme yapıldı. Gelişimi takip edilecek.",
      createdAt = now - (20 * dayMillis)
    )
    val hive4 = HiveEntity(
      id = "demo_hive_4",
      apiaryId = apiary1.id,
      hiveNumber = 4,
      hiveType = "Langstroth",
      queenYear = 2023,
      queenBreed = "Belfast (Buckfast)",
      colonyStrength = "Zayıf",
      notes = "Eski ana arı değiştirilecek. Destek besleme verildi.",
      createdAt = now - (15 * dayMillis)
    )
    hiveDao.insertHive(hive1)
    hiveDao.insertHive(hive2)
    hiveDao.insertHive(hive3)
    hiveDao.insertHive(hive4)

    // Hives for Apiary 2 (Sequential Numbers: 1, 2)
    val hive5 = HiveEntity(
      id = "demo_hive_5",
      apiaryId = apiary2.id,
      hiveNumber = 1,
      hiveType = "Langstroth",
      queenYear = 2025,
      queenBreed = "Kafkas",
      colonyStrength = "Güçlü",
      notes = "Kestane balı akımına hazır.",
      createdAt = now - (18 * dayMillis)
    )
    val hive6 = HiveEntity(
      id = "demo_hive_6",
      apiaryId = apiary2.id,
      hiveNumber = 2,
      hiveType = "Karakovan",
      queenYear = 2025,
      queenBreed = "Muğla Yerli",
      colonyStrength = "Orta",
      notes = "Doğal petekli karakovan üretimi.",
      createdAt = now - (17 * dayMillis)
    )
    hiveDao.insertHive(hive5)
    hiveDao.insertHive(hive6)

    // 👑 Queens demo
    queenDao.insertQueen(
      QueenEntity(
        id = "demo_queen_1",
        hiveId = hive1.id,
        apiaryId = apiary1.id,
        status = "Var",
        year = 2025,
        breed = "Kafkas",
        markingColor = "Mavi",
        source = "Kendi Üretimimiz",
        installedDate = now - (28 * dayMillis),
        notes = "Genç ve verimli ana arı. Yumurtlama düzeni çok sıkı ve blok şeklinde.",
        isActive = true
      )
    )
    queenDao.insertQueen(
      QueenEntity(
        id = "demo_queen_2",
        hiveId = hive2.id,
        apiaryId = apiary1.id,
        status = "Var",
        year = 2024,
        breed = "Karniyol",
        markingColor = "Yeşil",
        source = "Satın Alındı (Sertifikalı)",
        installedDate = now - (27 * dayMillis),
        notes = "F1 Karniyol ana arı. Mizaç sakin, yavru atımı mükemmel.",
        isActive = true
      )
    )
    queenDao.insertQueen(
      QueenEntity(
        id = "demo_queen_3",
        hiveId = hive3.id,
        apiaryId = apiary1.id,
        status = "Var",
        year = 2025,
        breed = "Anadolu",
        markingColor = "Mavi",
        source = "Bölmeden (Kovan 1'den)",
        installedDate = now - (20 * dayMillis),
        notes = "Kovan 1'den yapılan bölmeden yetiştirilen genç ana arı.",
        isActive = true
      )
    )
    queenDao.insertQueen(
      QueenEntity(
        id = "demo_queen_4",
        hiveId = hive4.id,
        apiaryId = apiary1.id,
        status = "Yok",
        year = 2023,
        breed = "Belfast (Buckfast)",
        markingColor = "Kırmızı",
        source = "Satın Alındı",
        installedDate = now - (15 * dayMillis),
        notes = "Ana arı yaşlandı, verim düştü. Yeni ana arı takılacak.",
        isActive = true
      )
    )

    // 🐝 Swarm Events demo
    swarmDao.insertSwarmEvent(
      SwarmEntity(
        id = "demo_swarm_1",
        hiveId = hive1.id,
        apiaryId = apiary1.id,
        eventType = "Bölme Yapıldı (Kaynak)",
        eventDate = now - (20 * dayMillis),
        tendencyLevel = "Düşük",
        queenCellsStatus = "Yok",
        relatedHiveId = hive3.id,
        relatedHiveNumber = 3,
        actionTaken = "3 çerçeve yavru ve arı ile Kovan 3 bölmesi oluşturuldu.",
        notes = "Kovan 1 çok güçlendiği için oğula yatmaması amacıyla bölündü."
      )
    )
    swarmDao.insertSwarmEvent(
      SwarmEntity(
        id = "demo_swarm_2",
        hiveId = hive3.id,
        apiaryId = apiary1.id,
        eventType = "Bölme Oluşturuldu (Yeni Kovan)",
        eventDate = now - (20 * dayMillis),
        tendencyLevel = "Yok",
        queenCellsStatus = "Yok",
        relatedHiveId = hive1.id,
        relatedHiveNumber = 1,
        actionTaken = "Kovan 1'den bölme yapılarak yeni kovan kuruldu.",
        notes = "Ana arı kabul edildi ve yumurtlamaya başladı."
      )
    )

    // 🩺 Issues demo
    issueDao.insertIssue(
      IssueEntity(
        id = "demo_issue_1",
        hiveId = hive4.id,
        apiaryId = apiary1.id,
        category = "Zayıf Koloni / Sönme Riski",
        severity = "Orta",
        status = "Takipte",
        detectedDate = now - (7 * dayMillis),
        treatmentNotes = "Protein keki ve 1:1 şurup desteği verildi. Ana arı değişimi planlandı.",
        notes = "Nüfus 3 çerçeveye geriledi. Yağmalamaya karşı uçuş deliği daraltıldı."
      )
    )
    issueDao.insertIssue(
      IssueEntity(
        id = "demo_issue_2",
        hiveId = hive1.id,
        apiaryId = apiary1.id,
        category = "Varroa",
        severity = "Hafif",
        status = "Çözüldü",
        detectedDate = now - (25 * dayMillis),
        resolvedDate = now - (10 * dayMillis),
        treatmentNotes = "Organik Formik Asit uygulaması yapıldı. Alt tabla sayımında döküntü normal seviyeye indi.",
        notes = "İlkbahar koruyucu varroa mücadelesi tamamlandı."
      )
    )

    // Inspections
    inspectionDao.insertInspection(
      InspectionEntity(
        id = "demo_insp_1",
        hiveId = hive1.id,
        apiaryId = apiary1.id,
        inspectionDate = now - (2 * dayMillis),
        queenSeen = true,
        broodEgg = true,
        broodLarva = true,
        broodCapped = true,
        colonyStrength = "Çok Güçlü",
        honeyStatus = "Çok İyi",
        pollenStatus = "Yeterli",
        behavior = "Sakin",
        frameChanges = "1 Kat eklendi, 2 ham petek eklendi",
        problems = "",
        notes = "Ana arı görüldü ve işaretlendi. 7 çerçeve kapalı yavru var, oğul eğilimi yok.",
        weatherSummary = "28°C, Açık ve Güneşli, %42 Nem",
        createdAt = now - (2 * dayMillis)
      )
    )

    inspectionDao.insertInspection(
      InspectionEntity(
        id = "demo_insp_2",
        hiveId = hive2.id,
        apiaryId = apiary1.id,
        inspectionDate = now - (5 * dayMillis),
        queenSeen = true,
        broodEgg = true,
        broodLarva = true,
        broodCapped = true,
        colonyStrength = "Güçlü",
        honeyStatus = "İyi",
        pollenStatus = "Çok",
        behavior = "Sakin",
        frameChanges = "1 Çerçeve eklendi",
        problems = "",
        notes = "Koloni düzeni gayet sağlıklı. Polen depolaması yoğun.",
        weatherSummary = "26°C, Parçalı Bulutlu, %48 Nem",
        createdAt = now - (5 * dayMillis)
      )
    )

    // Feedings
    feedingDao.insertFeeding(
      FeedingEntity(
        id = "demo_feed_1",
        hiveId = hive3.id,
        apiaryId = apiary1.id,
        feedingType = "Şurup (1:1)",
        amount = 1.5,
        unit = "Litre",
        feedingDate = now - (1 * dayMillis),
        notes = "Bölme koloniyi desteklemek için teşvik şurubu verildi.",
        createdAt = now - (1 * dayMillis)
      )
    )

    feedingDao.insertFeeding(
      FeedingEntity(
        id = "demo_feed_2",
        hiveId = hive4.id,
        apiaryId = apiary1.id,
        feedingType = "Protein Keki",
        amount = 500.0,
        unit = "Gram",
        feedingDate = now - (3 * dayMillis),
        notes = "Zayıf koloniye protein keki verildi.",
        createdAt = now - (3 * dayMillis)
      )
    )

    // Reminders
    reminderDao.insertReminder(
      ReminderEntity(
        id = "demo_rem_1",
        title = "Kovan 3 Teşvik Şurubu Kontrolü",
        date = now + (2 * dayMillis),
        apiaryId = apiary1.id,
        hiveId = hive3.id
      )
    )
    reminderDao.insertReminder(
      ReminderEntity(
        id = "demo_rem_2",
        title = "Varroa Kontrolü ve Alt Tabla Temizliği",
        date = now + (5 * dayMillis),
        apiaryId = apiary1.id
      )
    )
  }
}
