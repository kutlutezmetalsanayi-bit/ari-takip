package com.example.data.util

import com.example.data.local.entity.ApiaryEntity
import com.example.data.local.entity.FeedingEntity
import com.example.data.local.entity.HiveEntity
import com.example.data.local.entity.InspectionEntity
import com.example.data.local.entity.IssueEntity
import com.example.data.local.entity.PhotoEntity
import com.example.data.local.entity.QueenEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.SwarmEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupPayload(
  val backupVersion: Int = 2,
  val appVersion: String = "1.3.0",
  val createdAt: Long = System.currentTimeMillis(),
  val apiaries: List<ApiaryEntity> = emptyList(),
  val hives: List<HiveEntity> = emptyList(),
  val inspections: List<InspectionEntity> = emptyList(),
  val feedings: List<FeedingEntity> = emptyList(),
  val photos: List<PhotoEntity> = emptyList(),
  val reminders: List<ReminderEntity> = emptyList(),
  val queens: List<QueenEntity> = emptyList(),
  val swarms: List<SwarmEntity> = emptyList(),
  val issues: List<IssueEntity> = emptyList()
)

data class ImportResult(
  val mode: ImportMode,
  val apiariesCount: Int,
  val hivesCount: Int,
  val inspectionsCount: Int,
  val feedingsCount: Int,
  val photosCount: Int,
  val remindersCount: Int,
  val queensCount: Int = 0,
  val swarmsCount: Int = 0,
  val issuesCount: Int = 0,
  val message: String
)

enum class ImportMode {
  MERGE,    // "Verileri birleştir (Önerilen)" - keeps existing, updates/inserts matching by ID
  OVERWRITE // "Mevcut verilerin üzerine yaz" - removes old, loads backup
}

class InvalidBackupException(message: String) : Exception(message)

object BackupManager {
  private const val CURRENT_BACKUP_VERSION = 2
  private const val APP_VERSION = "1.3.0"

  /**
   * Generates a fully formatted, standardized JSON backup string from current local entities.
   */
  fun generateBackupJson(payload: BackupPayload): String {
    val root = JSONObject()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))

    root.put("backupVersion", CURRENT_BACKUP_VERSION)
    root.put("appVersion", APP_VERSION)
    root.put("createdAt", payload.createdAt)
    root.put("exportDateFormatted", dateFormat.format(Date(payload.createdAt)))

    // Summary metadata
    val summary = JSONObject().apply {
      put("apiaryCount", payload.apiaries.size)
      put("hiveCount", payload.hives.size)
      put("inspectionCount", payload.inspections.size)
      put("feedingCount", payload.feedings.size)
      put("photoCount", payload.photos.size)
      put("reminderCount", payload.reminders.size)
      put("queenCount", payload.queens.size)
      put("swarmCount", payload.swarms.size)
      put("issueCount", payload.issues.size)
    }
    root.put("summary", summary)

    // 1. Apiaries
    val apiariesArray = JSONArray()
    payload.apiaries.forEach { a ->
      val obj = JSONObject().apply {
        put("id", a.id)
        put("userId", a.userId)
        put("name", a.name)
        put("address", a.address)
        put("latitude", a.latitude)
        put("longitude", a.longitude)
        put("notes", a.notes)
        put("createdAt", a.createdAt)
        put("updatedAt", a.updatedAt)
        put("isActive", a.isActive)
      }
      apiariesArray.put(obj)
    }
    root.put("apiaries", apiariesArray)

    // 2. Hives (MANDATORY: preserve hiveNumber, id, apiaryId)
    val hivesArray = JSONArray()
    payload.hives.forEach { h ->
      val obj = JSONObject().apply {
        put("id", h.id)
        put("userId", h.userId)
        put("apiaryId", h.apiaryId)
        put("hiveNumber", h.hiveNumber)
        put("photoUri", h.photoUri ?: JSONObject.NULL)
        put("hiveType", h.hiveType)
        put("queenYear", h.queenYear)
        put("queenBreed", h.queenBreed)
        put("colonyStrength", h.colonyStrength)
        put("notes", h.notes)
        put("status", h.status)
        put("createdAt", h.createdAt)
        put("updatedAt", h.updatedAt)
      }
      hivesArray.put(obj)
    }
    root.put("hives", hivesArray)

    // 3. Inspections
    val inspectionsArray = JSONArray()
    payload.inspections.forEach { i ->
      val obj = JSONObject().apply {
        put("id", i.id)
        put("userId", i.userId)
        put("hiveId", i.hiveId)
        put("apiaryId", i.apiaryId)
        put("inspectionDate", i.inspectionDate)
        put("queenSeen", i.queenSeen)
        put("broodEgg", i.broodEgg)
        put("broodLarva", i.broodLarva)
        put("broodCapped", i.broodCapped)
        put("colonyStrength", i.colonyStrength)
        put("honeyStatus", i.honeyStatus)
        put("pollenStatus", i.pollenStatus)
        put("behavior", i.behavior)
        put("frameChanges", i.frameChanges)
        put("problems", i.problems)
        put("notes", i.notes)
        put("weatherSummary", i.weatherSummary)
        put("photoUris", i.photoUris)
        put("createdAt", i.createdAt)
      }
      inspectionsArray.put(obj)
    }
    root.put("inspections", inspectionsArray)

    // 4. Feedings
    val feedingsArray = JSONArray()
    payload.feedings.forEach { f ->
      val obj = JSONObject().apply {
        put("id", f.id)
        put("userId", f.userId)
        put("hiveId", f.hiveId)
        put("apiaryId", f.apiaryId)
        put("feedingType", f.feedingType)
        put("amount", f.amount)
        put("unit", f.unit)
        put("feedingDate", f.feedingDate)
        put("notes", f.notes)
        put("photoUri", f.photoUri ?: JSONObject.NULL)
        put("createdAt", f.createdAt)
      }
      feedingsArray.put(obj)
    }
    root.put("feedings", feedingsArray)

    // 5. Photos Metadata
    val photosArray = JSONArray()
    payload.photos.forEach { p ->
      val obj = JSONObject().apply {
        put("id", p.id)
        put("userId", p.userId)
        put("hiveId", p.hiveId ?: JSONObject.NULL)
        put("apiaryId", p.apiaryId ?: JSONObject.NULL)
        put("targetType", p.targetType)
        put("targetId", p.targetId)
        put("localUri", p.localUri)
        put("cloudUrl", p.cloudUrl ?: JSONObject.NULL)
        put("date", p.date)
        put("notes", p.notes)
      }
      photosArray.put(obj)
    }
    root.put("photos", photosArray)

    // 6. Reminders
    val remindersArray = JSONArray()
    payload.reminders.forEach { r ->
      val obj = JSONObject().apply {
        put("id", r.id)
        put("userId", r.userId)
        put("apiaryId", r.apiaryId ?: JSONObject.NULL)
        put("hiveId", r.hiveId ?: JSONObject.NULL)
        put("title", r.title)
        put("date", r.date)
        put("isCompleted", r.isCompleted)
        put("createdAt", r.createdAt)
      }
      remindersArray.put(obj)
    }
    root.put("reminders", remindersArray)

    // 7. Queens
    val queensArray = JSONArray()
    payload.queens.forEach { q ->
      val obj = JSONObject().apply {
        put("id", q.id)
        put("userId", q.userId)
        put("hiveId", q.hiveId)
        put("apiaryId", q.apiaryId)
        put("status", q.status)
        put("year", q.year)
        put("breed", q.breed)
        put("markingColor", q.markingColor)
        put("source", q.source)
        put("installedDate", q.installedDate)
        put("notes", q.notes)
        put("isActive", q.isActive)
        put("createdAt", q.createdAt)
      }
      queensArray.put(obj)
    }
    root.put("queens", queensArray)

    // 8. Swarms
    val swarmsArray = JSONArray()
    payload.swarms.forEach { s ->
      val obj = JSONObject().apply {
        put("id", s.id)
        put("userId", s.userId)
        put("hiveId", s.hiveId)
        put("apiaryId", s.apiaryId)
        put("eventType", s.eventType)
        put("eventDate", s.eventDate)
        put("tendencyLevel", s.tendencyLevel)
        put("queenCellsStatus", s.queenCellsStatus)
        put("relatedHiveId", s.relatedHiveId ?: JSONObject.NULL)
        put("relatedHiveNumber", s.relatedHiveNumber ?: JSONObject.NULL)
        put("actionTaken", s.actionTaken)
        put("notes", s.notes)
        put("photoUri", s.photoUri ?: JSONObject.NULL)
        put("createdAt", s.createdAt)
      }
      swarmsArray.put(obj)
    }
    root.put("swarms", swarmsArray)

    // 9. Issues
    val issuesArray = JSONArray()
    payload.issues.forEach { issue ->
      val obj = JSONObject().apply {
        put("id", issue.id)
        put("userId", issue.userId)
        put("hiveId", issue.hiveId)
        put("apiaryId", issue.apiaryId)
        put("category", issue.category)
        put("severity", issue.severity)
        put("status", issue.status)
        put("detectedDate", issue.detectedDate)
        put("resolvedDate", issue.resolvedDate ?: JSONObject.NULL)
        put("treatmentNotes", issue.treatmentNotes)
        put("notes", issue.notes)
        put("photoUris", issue.photoUris)
        put("createdAt", issue.createdAt)
        put("updatedAt", issue.updatedAt)
      }
      issuesArray.put(obj)
    }
    root.put("issues", issuesArray)

    return root.toString(2)
  }

  /**
   * Safely parses and validates a JSON string into a BackupPayload.
   * Throws InvalidBackupException if the structure is invalid.
   */
  fun parseAndValidateJson(jsonString: String): BackupPayload {
    if (jsonString.isBlank()) {
      throw InvalidBackupException("Bu dosya geçerli bir Arı Takip yedeği değil (Dosya boş).")
    }

    try {
      val root = JSONObject(jsonString)

      // Validate required core metadata fields
      val backupVersion = root.optInt("backupVersion", -1)
      if (backupVersion <= 0) {
        throw InvalidBackupException("Bu dosya geçerli bir Arı Takip yedeği değil (Geçersiz format sürümü).")
      }

      val appVersion = root.optString("appVersion", "1.0.0")
      val createdAt = root.optLong("createdAt", System.currentTimeMillis())

      // 1. Apiaries
      val apiariesList = mutableListOf<ApiaryEntity>()
      val apiariesArray = root.optJSONArray("apiaries")
      if (apiariesArray != null) {
        for (i in 0 until apiariesArray.length()) {
          val obj = apiariesArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          apiariesList.add(
            ApiaryEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              name = obj.optString("name", "İsimsiz Arılık"),
              country = obj.optString("country", "Türkiye"),
              city = obj.optString("city", ""),
              district = obj.optString("district", ""),
              address = obj.optString("address", ""),
              latitude = obj.optDouble("latitude", 37.1305),
              longitude = obj.optDouble("longitude", 28.3228),
              notes = obj.optString("notes", ""),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
              updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
              isActive = obj.optBoolean("isActive", true)
            )
          )
        }
      }

      // 2. Hives (MANDATORY: preserve hiveNumber and IDs)
      val hivesList = mutableListOf<HiveEntity>()
      val hivesArray = root.optJSONArray("hives")
      if (hivesArray != null) {
        for (i in 0 until hivesArray.length()) {
          val obj = hivesArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          val hiveNumber = obj.optInt("hiveNumber", i + 1)
          val photoUri = if (obj.isNull("photoUri")) null else obj.optString("photoUri", null)

          hivesList.add(
            HiveEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              apiaryId = obj.optString("apiaryId", ""),
              hiveNumber = hiveNumber, // STRICTLY PRESERVED
              photoUri = photoUri,
              hiveType = obj.optString("hiveType", "Langstroth"),
              queenYear = obj.optInt("queenYear", 2025),
              queenBreed = obj.optString("queenBreed", "Kafkas"),
              colonyStrength = obj.optString("colonyStrength", "Güçlü"),
              notes = obj.optString("notes", ""),
              status = obj.optString("status", "active"),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
              updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
            )
          )
        }
      }

      // 3. Inspections
      val inspectionsList = mutableListOf<InspectionEntity>()
      val inspectionsArray = root.optJSONArray("inspections")
      if (inspectionsArray != null) {
        for (i in 0 until inspectionsArray.length()) {
          val obj = inspectionsArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          inspectionsList.add(
            InspectionEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              hiveId = obj.optString("hiveId", ""),
              apiaryId = obj.optString("apiaryId", ""),
              inspectionDate = obj.optLong("inspectionDate", System.currentTimeMillis()),
              queenSeen = obj.optBoolean("queenSeen", false),
              broodEgg = obj.optBoolean("broodEgg", false),
              broodLarva = obj.optBoolean("broodLarva", false),
              broodCapped = obj.optBoolean("broodCapped", false),
              colonyStrength = obj.optString("colonyStrength", "Güçlü"),
              honeyStatus = obj.optString("honeyStatus", "Orta"),
              pollenStatus = obj.optString("pollenStatus", "Yeterli"),
              behavior = obj.optString("behavior", "Sakin"),
              frameChanges = obj.optString("frameChanges", ""),
              problems = obj.optString("problems", ""),
              notes = obj.optString("notes", ""),
              weatherSummary = obj.optString("weatherSummary", ""),
              photoUris = obj.optString("photoUris", ""),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
      }

      // 4. Feedings
      val feedingsList = mutableListOf<FeedingEntity>()
      val feedingsArray = root.optJSONArray("feedings")
      if (feedingsArray != null) {
        for (i in 0 until feedingsArray.length()) {
          val obj = feedingsArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          val photoUri = if (obj.isNull("photoUri")) null else obj.optString("photoUri", null)
          feedingsList.add(
            FeedingEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              hiveId = obj.optString("hiveId", ""),
              apiaryId = obj.optString("apiaryId", ""),
              feedingType = obj.optString("feedingType", "Şurup (1:1)"),
              amount = obj.optDouble("amount", 1.0),
              unit = obj.optString("unit", "Litre"),
              feedingDate = obj.optLong("feedingDate", System.currentTimeMillis()),
              notes = obj.optString("notes", ""),
              photoUri = photoUri,
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
      }

      // 5. Photos
      val photosList = mutableListOf<PhotoEntity>()
      val photosArray = root.optJSONArray("photos")
      if (photosArray != null) {
        for (i in 0 until photosArray.length()) {
          val obj = photosArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          val hiveId = if (obj.isNull("hiveId")) null else obj.optString("hiveId", null)
          val apiaryId = if (obj.isNull("apiaryId")) null else obj.optString("apiaryId", null)
          val cloudUrl = if (obj.isNull("cloudUrl")) null else obj.optString("cloudUrl", null)

          photosList.add(
            PhotoEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              hiveId = hiveId,
              apiaryId = apiaryId,
              targetType = obj.optString("targetType", "GENERAL"),
              targetId = obj.optString("targetId", ""),
              localUri = obj.optString("localUri", ""),
              cloudUrl = cloudUrl,
              date = obj.optLong("date", System.currentTimeMillis()),
              notes = obj.optString("notes", "")
            )
          )
        }
      }

      // 6. Reminders
      val remindersList = mutableListOf<ReminderEntity>()
      val remindersArray = root.optJSONArray("reminders")
      if (remindersArray != null) {
        for (i in 0 until remindersArray.length()) {
          val obj = remindersArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          val hiveId = if (obj.isNull("hiveId")) null else obj.optString("hiveId", null)
          val apiaryId = if (obj.isNull("apiaryId")) null else obj.optString("apiaryId", null)

          remindersList.add(
            ReminderEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              apiaryId = apiaryId,
              hiveId = hiveId,
              title = obj.optString("title", "Hatırlatıcı"),
              date = obj.optLong("date", System.currentTimeMillis()),
              isCompleted = obj.optBoolean("isCompleted", false),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
      }

      // 7. Queens
      val queensList = mutableListOf<QueenEntity>()
      val queensArray = root.optJSONArray("queens")
      if (queensArray != null) {
        for (i in 0 until queensArray.length()) {
          val obj = queensArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          queensList.add(
            QueenEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              hiveId = obj.optString("hiveId", ""),
              apiaryId = obj.optString("apiaryId", ""),
              status = obj.optString("status", "Var"),
              year = obj.optInt("year", 2025),
              breed = obj.optString("breed", "Kafkas"),
              markingColor = obj.optString("markingColor", "Mavi"),
              source = obj.optString("source", "Kendi Üretimimiz"),
              installedDate = obj.optLong("installedDate", System.currentTimeMillis()),
              notes = obj.optString("notes", ""),
              isActive = obj.optBoolean("isActive", true),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
      }

      // 8. Swarms
      val swarmsList = mutableListOf<SwarmEntity>()
      val swarmsArray = root.optJSONArray("swarms")
      if (swarmsArray != null) {
        for (i in 0 until swarmsArray.length()) {
          val obj = swarmsArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          val relatedHiveId = if (obj.isNull("relatedHiveId")) null else obj.optString("relatedHiveId", null)
          val relatedHiveNumber = if (obj.isNull("relatedHiveNumber")) null else obj.optInt("relatedHiveNumber")

          swarmsList.add(
            SwarmEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              hiveId = obj.optString("hiveId", ""),
              apiaryId = obj.optString("apiaryId", ""),
              eventType = obj.optString("eventType", "Oğul Eğilimi"),
              eventDate = obj.optLong("eventDate", System.currentTimeMillis()),
              tendencyLevel = obj.optString("tendencyLevel", "Yok"),
              queenCellsStatus = obj.optString("queenCellsStatus", "Yok"),
              relatedHiveId = relatedHiveId,
              relatedHiveNumber = relatedHiveNumber,
              actionTaken = obj.optString("actionTaken", ""),
              notes = obj.optString("notes", ""),
              photoUri = if (obj.isNull("photoUri")) null else obj.optString("photoUri", null),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
          )
        }
      }

      // 9. Issues
      val issuesList = mutableListOf<IssueEntity>()
      val issuesArray = root.optJSONArray("issues")
      if (issuesArray != null) {
        for (i in 0 until issuesArray.length()) {
          val obj = issuesArray.getJSONObject(i)
          val id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() }
          val resolvedDate = if (obj.isNull("resolvedDate")) null else obj.optLong("resolvedDate")

          issuesList.add(
            IssueEntity(
              id = id,
              userId = obj.optString("userId", "user_default"),
              hiveId = obj.optString("hiveId", ""),
              apiaryId = obj.optString("apiaryId", ""),
              category = obj.optString("category", "Varroa"),
              severity = obj.optString("severity", "Orta"),
              status = obj.optString("status", "Açık"),
              detectedDate = obj.optLong("detectedDate", System.currentTimeMillis()),
              resolvedDate = resolvedDate,
              treatmentNotes = obj.optString("treatmentNotes", ""),
              notes = obj.optString("notes", ""),
              photoUris = obj.optString("photoUris", ""),
              createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
              updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
            )
          )
        }
      }

      return BackupPayload(
        backupVersion = backupVersion,
        appVersion = appVersion,
        createdAt = createdAt,
        apiaries = apiariesList,
        hives = hivesList,
        inspections = inspectionsList,
        feedings = feedingsList,
        photos = photosList,
        reminders = remindersList,
        queens = queensList,
        swarms = swarmsList,
        issues = issuesList
      )
    } catch (e: InvalidBackupException) {
      throw e
    } catch (e: Exception) {
      throw InvalidBackupException("Bu dosya geçerli bir Arı Takip yedeği değil: ${e.localizedMessage}")
    }
  }

  /**
   * Exports Hives to Excel/Google Sheets compatible UTF-8 CSV.
   */
  fun exportHivesToCsv(hives: List<HiveEntity>, apiaries: List<ApiaryEntity>): String {
    val apiaryMap = apiaries.associateBy { it.id }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
    val sb = StringBuilder()

    // UTF-8 BOM for Excel
    sb.append('\uFEFF')
    sb.append("Kovan No,Arılık Adı,Kovan Tipi,Ana Arı Irkı,Ana Arı Yılı,Koloni Gücü,Durum,Notlar,Kayıt Tarihi\n")

    hives.sortedBy { it.hiveNumber }.forEach { h ->
      val apiaryName = apiaryMap[h.apiaryId]?.name ?: "Bilinmeyen Arılık"
      val statusStr = if (h.status == "active") "Aktif" else "Arşivlenmiş"
      val dateStr = dateFormat.format(Date(h.createdAt))

      sb.append("\"Kovan ${h.hiveNumber}\",")
      sb.append("\"${escapeCsv(apiaryName)}\",")
      sb.append("\"${escapeCsv(h.hiveType)}\",")
      sb.append("\"${escapeCsv(h.queenBreed)}\",")
      sb.append("${h.queenYear},")
      sb.append("\"${escapeCsv(h.colonyStrength)}\",")
      sb.append("\"$statusStr\",")
      sb.append("\"${escapeCsv(h.notes)}\",")
      sb.append("\"$dateStr\"\n")
    }

    return sb.toString()
  }

  /**
   * Exports Inspections to Excel compatible CSV.
   */
  fun exportInspectionsToCsv(
    inspections: List<InspectionEntity>,
    hives: List<HiveEntity>,
    apiaries: List<ApiaryEntity>
  ): String {
    val apiaryMap = apiaries.associateBy { it.id }
    val hiveMap = hives.associateBy { it.id }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR"))
    val sb = StringBuilder()

    sb.append('\uFEFF')
    sb.append("Tarih,Kovan No,Arılık,Ana Arı Görüldü,Günlük Yumurta,Kurtçuk,Kapalı Yavru,Koloni Gücü,Bal Durumu,Polen Durumu,Mizaç,Çerçeve/Kat İşlemi,Sorunlar,Hava Durumu,Notlar\n")

    inspections.sortedByDescending { it.inspectionDate }.forEach { i ->
      val hive = hiveMap[i.hiveId]
      val hiveNo = if (hive != null) "Kovan ${hive.hiveNumber}" else "?"
      val apiaryName = apiaryMap[i.apiaryId]?.name ?: (apiaryMap[hive?.apiaryId]?.name ?: "")
      val dateStr = dateFormat.format(Date(i.inspectionDate))

      sb.append("\"$dateStr\",")
      sb.append("\"$hiveNo\",")
      sb.append("\"${escapeCsv(apiaryName)}\",")
      sb.append("\"${if (i.queenSeen) "Evet" else "Hayır"}\",")
      sb.append("\"${if (i.broodEgg) "Var" else "Yok"}\",")
      sb.append("\"${if (i.broodLarva) "Var" else "Yok"}\",")
      sb.append("\"${if (i.broodCapped) "Var" else "Yok"}\",")
      sb.append("\"${escapeCsv(i.colonyStrength)}\",")
      sb.append("\"${escapeCsv(i.honeyStatus)}\",")
      sb.append("\"${escapeCsv(i.pollenStatus)}\",")
      sb.append("\"${escapeCsv(i.behavior)}\",")
      sb.append("\"${escapeCsv(i.frameChanges)}\",")
      sb.append("\"${escapeCsv(i.problems)}\",")
      sb.append("\"${escapeCsv(i.weatherSummary)}\",")
      sb.append("\"${escapeCsv(i.notes)}\"\n")
    }

    return sb.toString()
  }

  /**
   * Exports Feedings to Excel compatible CSV.
   */
  fun exportFeedingsToCsv(
    feedings: List<FeedingEntity>,
    hives: List<HiveEntity>,
    apiaries: List<ApiaryEntity>
  ): String {
    val apiaryMap = apiaries.associateBy { it.id }
    val hiveMap = hives.associateBy { it.id }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
    val sb = StringBuilder()

    sb.append('\uFEFF')
    sb.append("Tarih,Kovan No,Arılık,Besleme Türü,Miktar,Birim,Notlar\n")

    feedings.sortedByDescending { it.feedingDate }.forEach { f ->
      val hive = hiveMap[f.hiveId]
      val hiveNo = if (hive != null) "Kovan ${hive.hiveNumber}" else "?"
      val apiaryName = apiaryMap[f.apiaryId]?.name ?: (apiaryMap[hive?.apiaryId]?.name ?: "")
      val dateStr = dateFormat.format(Date(f.feedingDate))

      sb.append("\"$dateStr\",")
      sb.append("\"$hiveNo\",")
      sb.append("\"${escapeCsv(apiaryName)}\",")
      sb.append("\"${escapeCsv(f.feedingType)}\",")
      sb.append("${f.amount},")
      sb.append("\"${f.unit}\",")
      sb.append("\"${escapeCsv(f.notes)}\"\n")
    }

    return sb.toString()
  }

  /**
   * Exports Issues & Diseases to Excel compatible CSV.
   */
  fun exportIssuesToCsv(
    issues: List<IssueEntity>,
    hives: List<HiveEntity>,
    apiaries: List<ApiaryEntity>
  ): String {
    val apiaryMap = apiaries.associateBy { it.id }
    val hiveMap = hives.associateBy { it.id }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR"))
    val sb = StringBuilder()

    sb.append('\uFEFF')
    sb.append("Tespit Tarihi,Kovan No,Arılık,Sorun / Hastalık,Şiddet,Durum,Uygulanan Tedavi,Notlar,Çözüm Tarihi\n")

    issues.sortedByDescending { it.detectedDate }.forEach { issue ->
      val hive = hiveMap[issue.hiveId]
      val hiveNo = if (hive != null) "Kovan ${hive.hiveNumber}" else "?"
      val apiaryName = apiaryMap[issue.apiaryId]?.name ?: (apiaryMap[hive?.apiaryId]?.name ?: "")
      val detectedDateStr = dateFormat.format(Date(issue.detectedDate))
      val resolvedDateStr = if (issue.resolvedDate != null) dateFormat.format(Date(issue.resolvedDate)) else "-"

      sb.append("\"$detectedDateStr\",")
      sb.append("\"$hiveNo\",")
      sb.append("\"${escapeCsv(apiaryName)}\",")
      sb.append("\"${escapeCsv(issue.category)}\",")
      sb.append("\"${escapeCsv(issue.severity)}\",")
      sb.append("\"${escapeCsv(issue.status)}\",")
      sb.append("\"${escapeCsv(issue.treatmentNotes)}\",")
      sb.append("\"${escapeCsv(issue.notes)}\",")
      sb.append("\"$resolvedDateStr\"\n")
    }

    return sb.toString()
  }

  private fun escapeCsv(text: String): String {
    return text.replace("\"", "\"\"").replace("\n", " ")
  }
}

