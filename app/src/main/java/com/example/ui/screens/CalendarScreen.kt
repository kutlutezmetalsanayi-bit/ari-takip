package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBrandHeader
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SeasonalGuide(
  val season: String,
  val months: String,
  val iconEmoji: String,
  val keyTasks: List<String>,
  val color: Color,
  val onColor: Color
)

val SEASONAL_GUIDES = listOf(
  SeasonalGuide(
    season = "İlkbahar Gelişimi & Teşvik",
    months = "Mart — Mayıs",
    iconEmoji = "🌸",
    keyTasks = listOf(
      "İlk kovan açılış kontrolleri (15°C+ rüzgarsız havada)",
      "Ana arı yumurtlama durumu ve varroa yoğunluk kontrolü",
      "Teşvik şerbeti ve polen keki takviyesi",
      "Kabarmış petek ve kat ilaveleri (Oğul önleme)"
    ),
    color = ForestGreenContainer,
    onColor = ForestOnGreenContainer
  ),
  SeasonalGuide(
    season = "Bal Akımı & Üretim Dönemi",
    months = "Haziran — Temmuz",
    iconEmoji = "🌻",
    keyTasks = listOf(
      "Ballık (kat) ilaveleri ve ana arı ızgarası yerleşimi",
      "Kovan havalandırma ve su kaynağı güvenliği",
      "Oğul kontrolü ve yeni ana arı üretimi / bölme",
      "Sır kontrolü ve bal olgunlaşma takibi"
    ),
    color = HoneyAmberContainer,
    onColor = HoneyOnAmberContainer
  ),
  SeasonalGuide(
    season = "Bal Hasadı & Sonbahar Bakımı",
    months = "Ağustos — Ekim",
    iconEmoji = "🍯",
    keyTasks = listOf(
      "Bal sağımı (hasat) ve kovan düzenlemeleri",
      "Hasat sonrası organik asit / varroa mücadelesi",
      "Kış kadrosu yetiştirme ve stok beslemesi (2:1 şurup)",
      "Zayıf kolonilerin birleştirilmesi"
    ),
    color = EarthWoodContainer,
    onColor = Color(0xFF5D4037)
  ),
  SeasonalGuide(
    season = "Kışlatma & Dinlenme",
    months = "Kasım — Şubat",
    iconEmoji = "❄️",
    keyTasks = listOf(
      "Kovan içi nem ve rutubet önlemleri",
      "Rüzgar koruması ve kovan giriş daraltması",
      "Yavrusuz dönem oksalik asit damlatma / buharlaştırma",
      "Gerektiğinde katı fondan şeker veya kek takviyesi"
    ),
    color = Color(0xFFE0F2FE),
    onColor = Color(0xFF0369A1)
  )
)

@Composable
fun CalendarScreen(
  viewModel: BeeViewModel
) {
  val reminders by viewModel.reminders.collectAsStateWithLifecycle()
  var showAddReminderDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("calendar_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      item {
        AppBrandHeader(
          pageSubtitle = "Arı Takip — Takvim & Hatırlatıcılar"
        )
      }

      // Reminders Section Header
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          Text(
            text = "📅 Kovan İşlem Hatırlatıcıları",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
        }
      }

      if (reminders.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "📌", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
              Column {
                Text(
                  text = "Henüz hatırlatıcı bulunmuyor",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Kontrol, ilaçlama veya besleme günlerini hatırlatıcıya kaydedin.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      } else {
        items(reminders) { reminder ->
          val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
          val timeFormat = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
          val dateStr = dateFormat.format(Date(reminder.date))
          val timeStr = timeFormat.format(Date(reminder.date))

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(if (reminder.isCompleted) 0.dp else 2.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp)
              .testTag("reminder_item_${reminder.id}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .weight(1f)
                  .clickable { viewModel.toggleReminder(reminder.id, reminder.isCompleted) }
              ) {
                Icon(
                  imageVector = if (reminder.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                  contentDescription = if (reminder.isCompleted) "Tamamlandı" else "Bekliyor",
                  tint = if (reminder.isCompleted) ForestGreenSecondary else HoneyGoldPrimary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "🗓️ $dateStr • ⏰ $timeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              IconButton(onClick = { viewModel.deleteReminder(reminder.id) }) {
                Icon(
                  Icons.Filled.Delete,
                  contentDescription = "Sil",
                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }

      // Seasonal Guide Section Header (Section 17)
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
          Text(
            text = "🌻 Türkiye Arıcılık Mevsimsel Takvimi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Yıl boyunca arılık ve kovan yönetiminde yapılması gereken temel işlemler",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      items(SEASONAL_GUIDES) { guide ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(text = guide.iconEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Column {
                  Text(
                    text = guide.season,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = guide.months,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            guide.keyTasks.forEach { task ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
              ) {
                Text(text = "• ", fontWeight = FontWeight.Bold, color = HoneyGoldDark)
                Text(
                  text = task,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }

    FloatingActionButton(
      onClick = { showAddReminderDialog = true },
      containerColor = HoneyGoldPrimary,
      contentColor = Color.White,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 20.dp, bottom = 80.dp)
        .testTag("add_reminder_fab")
    ) {
      Icon(Icons.Filled.Add, contentDescription = "Hatırlatıcı Ekle", modifier = Modifier.size(28.dp))
    }
  }

  // Add Reminder Dialog
  if (showAddReminderDialog) {
    AddReminderDialog(
      onDismiss = { showAddReminderDialog = false },
      onSave = { title, timestamp ->
        viewModel.saveReminder(
          title = title,
          date = timestamp
        )
        showAddReminderDialog = false
      }
    )
  }
}

@Composable
fun AddReminderDialog(
  onDismiss: () -> Unit,
  onSave: (title: String, timestamp: Long) -> Unit
) {
  val context = LocalContext.current
  var title by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  val calendar = remember {
    Calendar.getInstance().apply {
      // Default: Tomorrow at 09:00 AM
      add(Calendar.DAY_OF_YEAR, 1)
      set(Calendar.HOUR_OF_DAY, 9)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
  }

  var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
  var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
  var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
  var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
  var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

  val selectedCalendar = Calendar.getInstance().apply {
    set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
    set(Calendar.MILLISECOND, 0)
  }

  val isPastDate = selectedCalendar.timeInMillis < System.currentTimeMillis()

  val turkishDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR"))
  val turkishTimeFormat = SimpleDateFormat("HH:mm", Locale("tr", "TR"))

  val dateDisplayStr = turkishDateFormat.format(selectedCalendar.time)
  val timeDisplayStr = turkishTimeFormat.format(selectedCalendar.time)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "🐝 ", fontSize = 20.sp)
        Text(
          text = "Yeni Hatırlatma Ekle",
          fontWeight = FontWeight.Bold,
          color = HoneyGoldDark
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Title Input
        Column {
          Text(
            text = "📝 Hatırlatma",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = title,
            onValueChange = {
              title = it
              isError = it.isBlank()
            },
            placeholder = { Text("Örn: Kovan 17 kontrolü, Kat atma...") },
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HoneyGoldPrimary),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reminder_title_input")
          )
        }

        // 2. Date Picker Selector Field
        Column {
          Text(
            text = "📅 Tarih",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, HoneyGoldPrimary.copy(alpha = 0.6f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                DatePickerDialog(
                  context,
                  { _, year, month, dayOfMonth ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = dayOfMonth
                  },
                  selectedYear,
                  selectedMonth,
                  selectedDay
                ).show()
              }
              .testTag("reminder_date_picker_trigger")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Filled.CalendarMonth,
                  contentDescription = null,
                  tint = HoneyGoldDark,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = dateDisplayStr,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Text(
                text = "Değiştir",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HoneyGoldDark
              )
            }
          }
        }

        // 3. Time Picker Selector Field
        Column {
          Text(
            text = "⏰ Saat",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, HoneyGoldPrimary.copy(alpha = 0.6f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                TimePickerDialog(
                  context,
                  { _, hourOfDay, minute ->
                    selectedHour = hourOfDay
                    selectedMinute = minute
                  },
                  selectedHour,
                  selectedMinute,
                  true
                ).show()
              }
              .testTag("reminder_time_picker_trigger")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Filled.AccessTime,
                  contentDescription = null,
                  tint = HoneyGoldDark,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = timeDisplayStr,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Text(
                text = "Değiştir",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = HoneyGoldDark
              )
            }
          }
        }

        // Past Date Warning Notice
        if (isPastDate) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFFFE4E6),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "⚠️ Geçmiş bir tarih ve saat seçtiniz. Lütfen ileri bir zaman belirleyin.",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF9F1239),
              modifier = Modifier.padding(8.dp)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isBlank()) {
            isError = true
            return@Button
          }
          onSave(title, selectedCalendar.timeInMillis)
        },
        enabled = title.isNotBlank() && !isPastDate,
        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("save_reminder_btn")
      ) {
        Text("HATIRLATMAYI KAYDET", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("İptal")
      }
    }
  )
}
