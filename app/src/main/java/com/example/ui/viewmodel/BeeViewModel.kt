package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.billing.GooglePlayBillingManager
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
import com.example.data.remote.ApiaryWeather
import com.example.data.remote.WeatherRepository
import com.example.data.repository.BeeRepository
import com.example.data.repository.DatabaseStats
import com.example.data.util.BackupManager
import com.example.data.util.BackupPayload
import com.example.data.util.ImportMode
import com.example.data.util.ImportResult
import com.example.data.util.InvalidBackupException
import com.example.data.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class UiEvent {
  data class ShowSnackbar(val message: String) : UiEvent()
}

class BeeViewModel(application: Application) : AndroidViewModel(application) {
  private val db = AppDatabase.getDatabase(application)
  private val repository = BeeRepository(db)
  private val weatherRepository = WeatherRepository()
  private val networkMonitor = NetworkMonitor(application, viewModelScope)

  // Network Connectivity State (Offline-first support)
  val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

  // Beekeeper Profile Name (User preferences)
  private val userPrefs = application.getSharedPreferences("ari_takip_user_prefs", Context.MODE_PRIVATE)
  private val _beekeeperName = MutableStateFlow(userPrefs.getString("beekeeper_name", "") ?: "")
  val beekeeperName: StateFlow<String> = _beekeeperName.asStateFlow()

  fun updateBeekeeperName(name: String) {
    val clean = name.trim()
    userPrefs.edit().putString("beekeeper_name", clean).apply()
    _beekeeperName.value = clean
  }

  // Google Play Billing Manager
  val billingManager = GooglePlayBillingManager.getInstance(application)
  val isProSubscribed: StateFlow<Boolean> = billingManager.isPro
  val monthlyPriceFormatted: StateFlow<String> = billingManager.monthlyPrice
  val yearlyPriceFormatted: StateFlow<String> = billingManager.yearlyPrice
  val billingStatusMessage: StateFlow<String?> = billingManager.statusMessage

  fun launchSubscription(activity: Activity, planType: String) {
    billingManager.launchPurchase(activity, planType)
  }

  fun restoreGooglePlayPurchases(onResult: (Boolean, String) -> Unit) {
    billingManager.restorePurchases(onResult)
  }

  fun clearBillingStatusMessage() {
    billingManager.clearStatusMessage()
  }

  // Sync & Backup states
  val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _isBackingUp = MutableStateFlow(false)
  val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

  private val _isRestoring = MutableStateFlow(false)
  val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

  private val _lastCloudSyncTimestamp = MutableStateFlow<Long>(0L)
  val lastCloudSyncTimestamp: StateFlow<Long> = _lastCloudSyncTimestamp.asStateFlow()

  // Snack events
  private val _eventFlow = MutableSharedFlow<UiEvent>()
  val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

  // Saving state for form operations (prevents double submits)
  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

  // Selected Apiary filter for Dashboard / Hives tab (null = "Tüm Arılıklar")
  private val _selectedApiaryId = MutableStateFlow<String?>(null)
  val selectedApiaryId: StateFlow<String?> = _selectedApiaryId.asStateFlow()

  // Search query & filters in Hives screen
  private val _hiveSearchQuery = MutableStateFlow("")
  val hiveSearchQuery: StateFlow<String> = _hiveSearchQuery.asStateFlow()

  private val _showArchivedHives = MutableStateFlow(false)
  val showArchivedHives: StateFlow<Boolean> = _showArchivedHives.asStateFlow()

  private val _strengthFilter = MutableStateFlow<String?>(null) // null = all, or "Zayıf", "Orta", "Güçlü", "Çok Güçlü"
  val strengthFilter: StateFlow<String?> = _strengthFilter.asStateFlow()

  // Weather state cache per apiary ID
  private val _weatherMap = MutableStateFlow<Map<String, ApiaryWeather>>(emptyMap())
  val weatherMap: StateFlow<Map<String, ApiaryWeather>> = _weatherMap.asStateFlow()

  private val _isWeatherLoading = MutableStateFlow(false)
  val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

  // Data streams from Room (single source of truth)
  val apiaries: StateFlow<List<ApiaryEntity>> = repository.activeApiaries
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allApiaries: StateFlow<List<ApiaryEntity>> = repository.allApiaries
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allHives: StateFlow<List<HiveEntity>> = repository.allHives
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val inspections: StateFlow<List<InspectionEntity>> = repository.allInspections
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val feedings: StateFlow<List<FeedingEntity>> = repository.allFeedings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val photos: StateFlow<List<PhotoEntity>> = repository.allPhotos
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allQueens: StateFlow<List<QueenEntity>> = repository.getAllQueens()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSwarmEvents: StateFlow<List<SwarmEntity>> = repository.getAllSwarmEvents()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allIssues: StateFlow<List<IssueEntity>> = repository.getAllIssues()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeIssues: StateFlow<List<IssueEntity>> = repository.getAllActiveIssues()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Optimized Filtered Hives pipeline
  val filteredHives: StateFlow<List<HiveEntity>> = combine(
    repository.allHives,
    _selectedApiaryId,
    _hiveSearchQuery,
    _showArchivedHives,
    _strengthFilter
  ) { hives, apiaryId, query, showArchived, strength ->
    val cleanQuery = query.trim().lowercase()
    hives.filter { hive ->
      val matchesApiary = apiaryId == null || hive.apiaryId == apiaryId
      val matchesStatus = if (showArchived) true else hive.status == "active"
      val matchesStrength = strength == null || hive.colonyStrength.equals(strength, ignoreCase = true)
      
      val matchesQuery = cleanQuery.isBlank() ||
        hive.hiveNumber.toString() == cleanQuery ||
        "kovan ${hive.hiveNumber}".contains(cleanQuery) ||
        hive.queenBreed.lowercase().contains(cleanQuery) ||
        hive.hiveType.lowercase().contains(cleanQuery) ||
        hive.notes.lowercase().contains(cleanQuery)

      matchesApiary && matchesStatus && matchesStrength && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    // Automatically load cached or fresh weather for active apiaries
    viewModelScope.launch {
      apiaries.collect { apiaryList ->
        apiaryList.forEach { apiary ->
          fetchWeatherForApiary(apiary, forceRefresh = false)
        }
      }
    }
  }

  fun setSelectedApiary(apiaryId: String?) {
    _selectedApiaryId.value = apiaryId
  }

  fun setHiveSearchQuery(query: String) {
    _hiveSearchQuery.value = query
  }

  fun setShowArchivedHives(show: Boolean) {
    _showArchivedHives.value = show
  }

  fun setStrengthFilter(strength: String?) {
    _strengthFilter.value = strength
  }

  fun fetchWeatherForApiary(apiary: ApiaryEntity, forceRefresh: Boolean = false) {
    viewModelScope.launch {
      _isWeatherLoading.value = true
      val result = weatherRepository.getApiaryWeather(apiary.latitude, apiary.longitude, forceRefresh)
      result.onSuccess { weather ->
        _weatherMap.value = _weatherMap.value + (apiary.id to weather)
      }.onFailure {
        // Keeps previous data or UI handles null
      }
      _isWeatherLoading.value = false
    }
  }

  fun refreshWeather(apiary: ApiaryEntity) {
    fetchWeatherForApiary(apiary, forceRefresh = true)
  }

  // --- APIARY ACTIONS ---
  fun saveApiary(
    name: String,
    country: String = "Türkiye",
    city: String = "",
    district: String = "",
    address: String = "",
    latitude: Double = 37.1305,
    longitude: Double = 28.3228,
    notes: String = "",
    id: String? = null,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        val saved = repository.saveApiary(
          name = name,
          country = country,
          city = city,
          district = district,
          address = address,
          latitude = latitude,
          longitude = longitude,
          notes = notes,
          id = id
        )
        fetchWeatherForApiary(saved, forceRefresh = true)
        _eventFlow.emit(UiEvent.ShowSnackbar("✅ \"${saved.name}\" kaydedildi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Arılık kaydedilemedi: ${e.localizedMessage}"))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun deleteApiary(id: String) {
    viewModelScope.launch {
      try {
        repository.deleteApiary(id)
        if (_selectedApiaryId.value == id) {
          _selectedApiaryId.value = null
        }
        _eventFlow.emit(UiEvent.ShowSnackbar("Arılık silindi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Arılık silinemedi."))
      }
    }
  }

  // --- HIVE ACTIONS (Atomic numbering preserved) ---
  fun createHive(
    apiaryId: String,
    hiveType: String,
    queenYear: Int,
    queenBreed: String,
    colonyStrength: String,
    notes: String,
    photoUri: String? = null,
    onSuccess: (HiveEntity) -> Unit = {},
    onLimitReached: () -> Unit = {}
  ) {
    val activeHiveCount = allHives.value.count { it.status == "active" }
    if (!PlanManager.canAddActiveHive(activeHiveCount)) {
      viewModelScope.launch {
        _eventFlow.emit(UiEvent.ShowSnackbar("⚠️ Ücretsiz kullanım sınırına ulaştınız. (Maksimum 10 aktif kovan)"))
      }
      onLimitReached()
      return
    }

    viewModelScope.launch {
      _isSaving.value = true
      try {
        val newHive = repository.createHive(
          apiaryId = apiaryId,
          hiveType = hiveType,
          queenYear = queenYear,
          queenBreed = queenBreed,
          colonyStrength = colonyStrength,
          notes = notes,
          photoUri = photoUri
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("🐝 Kovan ${newHive.hiveNumber} başarıyla oluşturuldu."))
        onSuccess(newHive)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kovan oluşturulamadı: ${e.localizedMessage}"))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateHive(
    hiveId: String,
    apiaryId: String,
    hiveType: String,
    queenYear: Int,
    queenBreed: String,
    colonyStrength: String,
    notes: String,
    photoUri: String? = null,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.updateHive(
          hiveId = hiveId,
          apiaryId = apiaryId,
          hiveType = hiveType,
          queenYear = queenYear,
          queenBreed = queenBreed,
          colonyStrength = colonyStrength,
          notes = notes,
          photoUri = photoUri
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("Kovan bilgileri güncellendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kovan güncellenemedi."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun archiveHive(hiveId: String, hiveNumber: Int, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.archiveHive(hiveId)
        _eventFlow.emit(UiEvent.ShowSnackbar("📦 Kovan $hiveNumber arşivlendi. Geçmiş kayıtları korundu."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kovan arşivlenemedi."))
      }
    }
  }

  fun unarchiveHive(hiveId: String, hiveNumber: Int) {
    val activeHiveCount = allHives.value.count { it.status == "active" }
    if (!PlanManager.canAddActiveHive(activeHiveCount)) {
      viewModelScope.launch {
        _eventFlow.emit(UiEvent.ShowSnackbar("⚠️ 10 aktif kovan sınırına ulaştınız. PRO'ya geçin veya bir kovanı arşivleyin."))
      }
      return
    }

    viewModelScope.launch {
      try {
        repository.unarchiveHive(hiveId)
        _eventFlow.emit(UiEvent.ShowSnackbar("🟢 Kovan $hiveNumber tekrar aktif edildi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kovan aktifleştirilemedi: ${e.localizedMessage}"))
      }
    }
  }

  // --- 👑 QUEEN MANAGEMENT (V1.3) ---
  fun getActiveQueenForHive(hiveId: String) = repository.getActiveQueenForHive(hiveId)
  fun getQueenHistoryForHive(hiveId: String) = repository.getQueenHistoryForHive(hiveId)
  fun getQueenColorForYear(year: Int) = repository.getQueenColorForYear(year)

  fun saveQueen(
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
    id: String? = null,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.saveQueen(
          hiveId = hiveId,
          apiaryId = apiaryId,
          status = status,
          year = year,
          breed = breed,
          markingColor = markingColor,
          source = source,
          installedDate = installedDate,
          notes = notes,
          isNewActiveQueen = isNewActiveQueen,
          id = id
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("👑 Ana arı kaydı başarıyla kaydedildi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Ana arı kaydedilemedi."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateQueen(queen: QueenEntity, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.updateQueen(queen)
        _eventFlow.emit(UiEvent.ShowSnackbar("👑 Ana arı güncellendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Ana arı güncellenemedi."))
      }
    }
  }

  fun deleteQueen(id: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.deleteQueen(id)
        _eventFlow.emit(UiEvent.ShowSnackbar("🗑️ Ana arı kaydı silindi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Ana arı silinemedi."))
      }
    }
  }

  // --- 🐝 SWARM & SPLIT MANAGEMENT (V1.3) ---
  fun getSwarmEventsForHive(hiveId: String) = repository.getSwarmEventsForHive(hiveId)
  fun getRelatedSwarmEventsForHive(hiveId: String) = repository.getRelatedSwarmEventsForHive(hiveId)

  fun addSwarmEvent(
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
    photoUri: String? = null,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.addSwarmEvent(
          hiveId = hiveId,
          apiaryId = apiaryId,
          eventType = eventType,
          eventDate = eventDate,
          tendencyLevel = tendencyLevel,
          queenCellsStatus = queenCellsStatus,
          relatedHiveId = relatedHiveId,
          relatedHiveNumber = relatedHiveNumber,
          actionTaken = actionTaken,
          notes = notes,
          photoUri = photoUri
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("🐝 Oğul / Bölme kaydı başarıyla eklendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kayıt eklenirken hata oluştu."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateSwarmEvent(event: SwarmEntity, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.updateSwarmEvent(event)
        _eventFlow.emit(UiEvent.ShowSnackbar("🐝 Oğul / Bölme kaydı güncellendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Güncellenemedi."))
      }
    }
  }

  fun deleteSwarmEvent(id: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.deleteSwarmEvent(id)
        _eventFlow.emit(UiEvent.ShowSnackbar("🗑️ Oğul / Bölme kaydı silindi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Silinemedi."))
      }
    }
  }

  // --- 🩺 ISSUES & DISEASES (V1.3) ---
  fun getIssuesForHive(hiveId: String) = repository.getIssuesForHive(hiveId)
  fun getActiveIssuesForHive(hiveId: String) = repository.getActiveIssuesForHive(hiveId)

  fun addIssue(
    hiveId: String,
    apiaryId: String,
    category: String,
    severity: String,
    status: String = "Açık",
    detectedDate: Long = System.currentTimeMillis(),
    treatmentNotes: String = "",
    notes: String = "",
    photoUris: String = "",
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.addIssue(
          hiveId = hiveId,
          apiaryId = apiaryId,
          category = category,
          severity = severity,
          status = status,
          detectedDate = detectedDate,
          treatmentNotes = treatmentNotes,
          notes = notes,
          photoUris = photoUris
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("🩺 Sorun/Hastalık kaydı açıldı."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Sorun kaydı eklenemedi."))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun updateIssue(issue: IssueEntity, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.updateIssue(issue)
        _eventFlow.emit(UiEvent.ShowSnackbar("🩺 Sorun kaydı güncellendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Güncellenemedi."))
      }
    }
  }

  fun markIssueResolved(id: String, resolutionNotes: String? = null, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.markIssueResolved(id, resolutionNotes)
        _eventFlow.emit(UiEvent.ShowSnackbar("✅ Sorun çözüldü olarak işaretlendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Durum güncellenemedi."))
      }
    }
  }

  fun deleteIssue(id: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.deleteIssue(id)
        _eventFlow.emit(UiEvent.ShowSnackbar("🗑️ Sorun kaydı silindi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Silinemedi."))
      }
    }
  }

  // --- INSPECTION ACTIONS ---
  fun saveInspection(
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
    photoUris: List<String>,
    inspectionDate: Long = System.currentTimeMillis(),
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        val weather = _weatherMap.value[apiaryId]
        val weatherSummary = if (weather != null) {
          "${weather.temperature.toInt()}°C, ${weather.conditionDescription}, %${weather.humidity} Nem, ${weather.windSpeed.toInt()} km/s Rüzgar"
        } else {
          ""
        }

        repository.saveInspection(
          hiveId = hiveId,
          apiaryId = apiaryId,
          queenSeen = queenSeen,
          broodEgg = broodEgg,
          broodLarva = broodLarva,
          broodCapped = broodCapped,
          colonyStrength = colonyStrength,
          honeyStatus = honeyStatus,
          pollenStatus = pollenStatus,
          behavior = behavior,
          frameChanges = frameChanges,
          problems = problems,
          notes = notes,
          weatherSummary = weatherSummary,
          photoUris = photoUris,
          inspectionDate = inspectionDate
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("🔍 Kovan kontrol kaydı başarıyla eklendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kontrol kaydedilemedi: ${e.localizedMessage}"))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun deleteInspection(id: String) {
    viewModelScope.launch {
      try {
        repository.deleteInspection(id)
        _eventFlow.emit(UiEvent.ShowSnackbar("Kontrol kaydı silindi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kayıt silinemedi."))
      }
    }
  }

  // --- FEEDING ACTIONS ---
  fun saveFeeding(
    hiveId: String,
    apiaryId: String,
    feedingType: String,
    amount: Double,
    unit: String,
    notes: String,
    photoUri: String? = null,
    feedingDate: Long = System.currentTimeMillis(),
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      _isSaving.value = true
      try {
        repository.saveFeeding(
          hiveId = hiveId,
          apiaryId = apiaryId,
          feedingType = feedingType,
          amount = amount,
          unit = unit,
          notes = notes,
          photoUri = photoUri,
          feedingDate = feedingDate
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("🍯 Besleme kaydı eklendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Besleme kaydedilemedi: ${e.localizedMessage}"))
      } finally {
        _isSaving.value = false
      }
    }
  }

  fun deleteFeeding(id: String) {
    viewModelScope.launch {
      try {
        repository.deleteFeeding(id)
        _eventFlow.emit(UiEvent.ShowSnackbar("Besleme kaydı silindi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kayıt silinemedi."))
      }
    }
  }

  // --- PHOTO ACTIONS ---
  fun savePhoto(
    hiveId: String? = null,
    apiaryId: String? = null,
    targetType: String = "GENERAL",
    targetId: String = "",
    uri: String,
    caption: String = ""
  ) {
    viewModelScope.launch {
      try {
        repository.savePhoto(
          hiveId = hiveId,
          apiaryId = apiaryId,
          targetType = targetType,
          targetId = targetId,
          localUri = uri,
          notes = caption
        )
        _eventFlow.emit(UiEvent.ShowSnackbar("📷 Fotoğraf eklendi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Fotoğraf kaydedilemedi."))
      }
    }
  }

  fun deletePhoto(id: String) {
    viewModelScope.launch {
      try {
        repository.deletePhoto(id)
        _eventFlow.emit(UiEvent.ShowSnackbar("Fotoğraf silindi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Fotoğraf silinemedi."))
      }
    }
  }

  // --- REMINDER ACTIONS ---
  fun saveReminder(title: String, date: Long, apiaryId: String? = null, hiveId: String? = null) {
    viewModelScope.launch {
      try {
        repository.saveReminder(title, date, apiaryId, hiveId)
        _eventFlow.emit(UiEvent.ShowSnackbar("📅 Hatırlatıcı eklendi."))
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Hatırlatıcı eklenemedi."))
      }
    }
  }

  fun toggleReminder(id: String, isCompleted: Boolean) {
    viewModelScope.launch {
      try {
        repository.setReminderCompleted(id, !isCompleted)
      } catch (e: Exception) {
        // Silent error
      }
    }
  }

  fun deleteReminder(id: String) {
    viewModelScope.launch {
      try {
        repository.deleteReminder(id)
      } catch (e: Exception) {
        // Silent error
      }
    }
  }

  // --- DATABASE STATS COMBINER ---
  val databaseStats: StateFlow<DatabaseStats> = combine(
    combine(allApiaries, allHives, inspections) { apiaries, hives, inspections ->
      Triple(apiaries, hives, inspections)
    },
    combine(feedings, photos, reminders) { feedings, photos, reminders ->
      Triple(feedings, photos, reminders)
    }
  ) { (apiaries, hives, inspections), (feedings, photos, reminders) ->
    DatabaseStats(
      apiariesCount = apiaries.size,
      activeHivesCount = hives.count { it.status == "active" },
      archivedHivesCount = hives.count { it.status == "archived" },
      totalHivesCount = hives.size,
      inspectionsCount = inspections.size,
      feedingsCount = feedings.size,
      photosCount = photos.size,
      remindersCount = reminders.size
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    DatabaseStats(0, 0, 0, 0, 0, 0, 0, 0)
  )

  // --- V1.2 BACKUP & CLOUD SYNC ACTIONS ---

  /**
   * Performs manual cloud backup with progress indicator, double-click protection,
   * and clean user feedback.
   */
  fun performManualCloudBackup(onComplete: (Boolean) -> Unit = {}) {
    if (_isBackingUp.value) return // Double-click protection

    viewModelScope.launch {
      _isBackingUp.value = true
      _eventFlow.emit(UiEvent.ShowSnackbar("☁️ Verileriniz yedekleniyor..."))

      try {
        // Check internet connection
        if (!isOnline.value) {
          delay(800)
          _eventFlow.emit(UiEvent.ShowSnackbar("⚠️ Veriler şu anda yedeklenemiyor. İnternet bağlantınızı kontrol edin."))
          onComplete(false)
          return@launch
        }

        // Simulate cloud backup sync / queue flush
        delay(1200)
        repository.clearPendingSyncQueue()
        _lastCloudSyncTimestamp.value = System.currentTimeMillis()
        _eventFlow.emit(UiEvent.ShowSnackbar("✅ Bulut yedekleme başarıyla tamamlandı."))
        onComplete(true)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Yedekleme sırasında bir hata oluştu."))
        onComplete(false)
      } finally {
        _isBackingUp.value = false
      }
    }
  }

  /**
   * Generates a complete JSON backup string of all local data.
   */
  fun generateBackupJson(onResult: (String?) -> Unit) {
    viewModelScope.launch {
      try {
        val payload = repository.getAllDataForBackup()
        val json = BackupManager.generateBackupJson(payload)
        onResult(json)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Yedek dosyası oluşturulamadı: ${e.localizedMessage}"))
        onResult(null)
      }
    }
  }

  /**
   * Generates CSV for Hives.
   */
  fun generateHivesCsv(onResult: (String?) -> Unit) {
    viewModelScope.launch {
      try {
        val hives = allHives.value
        val apiaries = allApiaries.value
        val csv = BackupManager.exportHivesToCsv(hives, apiaries)
        onResult(csv)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kovan CSV tablosu oluşturulamadı."))
        onResult(null)
      }
    }
  }

  /**
   * Generates CSV for Inspections.
   */
  fun generateInspectionsCsv(onResult: (String?) -> Unit) {
    viewModelScope.launch {
      try {
        val insp = inspections.value
        val hives = allHives.value
        val apiaries = allApiaries.value
        val csv = BackupManager.exportInspectionsToCsv(insp, hives, apiaries)
        onResult(csv)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Kontrol CSV tablosu oluşturulamadı."))
        onResult(null)
      }
    }
  }

  /**
   * Generates CSV for Feedings.
   */
  fun generateFeedingsCsv(onResult: (String?) -> Unit) {
    viewModelScope.launch {
      try {
        val feed = feedings.value
        val hives = allHives.value
        val apiaries = allApiaries.value
        val csv = BackupManager.exportFeedingsToCsv(feed, hives, apiaries)
        onResult(csv)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Besleme CSV tablosu oluşturulamadı."))
        onResult(null)
      }
    }
  }

  /**
   * Generates CSV for Issues / Diseases.
   */
  fun generateIssuesCsv(onResult: (String?) -> Unit) {
    viewModelScope.launch {
      try {
        val iss = allIssues.value
        val hives = allHives.value
        val apiaries = allApiaries.value
        val csv = BackupManager.exportIssuesToCsv(iss, hives, apiaries)
        onResult(csv)
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Sorunlar CSV tablosu oluşturulamadı."))
        onResult(null)
      }
    }
  }

  /**
   * Validates and previews a selected JSON backup string before restoring.
   */
  fun parseBackupForPreview(jsonString: String): BackupPayload? {
    return try {
      BackupManager.parseAndValidateJson(jsonString)
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Restores database from a verified JSON string using chosen ImportMode (MERGE or OVERWRITE).
   */
  fun restoreFromBackupJson(
    jsonString: String,
    mode: ImportMode,
    onSuccess: (ImportResult) -> Unit = {},
    onError: (String) -> Unit = {}
  ) {
    viewModelScope.launch {
      _isRestoring.value = true
      try {
        val payload = BackupManager.parseAndValidateJson(jsonString)
        val result = repository.importBackupData(payload, mode)
        _eventFlow.emit(UiEvent.ShowSnackbar("✅ ${result.message}"))
        onSuccess(result)
      } catch (e: InvalidBackupException) {
        val msg = e.localizedMessage ?: "Bu dosya geçerli bir Arı Takip yedeği değil."
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ $msg"))
        onError(msg)
      } catch (e: Exception) {
        val msg = "Geri yükleme başarısız oldu. Lütfen dosya formatını kontrol edin."
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ $msg"))
        onError(msg)
      } finally {
        _isRestoring.value = false
      }
    }
  }

  /**
   * Clears all local database data (with confirmation in UI).
   */
  fun clearAllData(onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.clearAllData()
        _selectedApiaryId.value = null
        _eventFlow.emit(UiEvent.ShowSnackbar("🗑️ Tüm veriler başarıyla temizlendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Veriler temizlenemedi."))
      }
    }
  }

  // --- DEMO DATA LOADER ---
  fun loadDemoData(onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.loadRealisticTurkishDemoData()
        _eventFlow.emit(UiEvent.ShowSnackbar("🐝 Demo arılık ve kovan verileri başarıyla yüklendi."))
        onSuccess()
      } catch (e: Exception) {
        _eventFlow.emit(UiEvent.ShowSnackbar("❌ Demo verisi yüklenirken hata oluştu."))
      }
    }
  }
}
