package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.remote.ApiaryWeather
import com.example.ui.theme.ForestGreenContainer
import com.example.ui.theme.ForestGreenSecondary
import com.example.ui.theme.ForestOnGreenContainer
import com.example.ui.theme.HoneyAmberContainer
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyOnAmberContainer

data class NavItem(
  val title: String,
  val route: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
)

val bottomNavItems = listOf(
  NavItem("Ana Sayfa", "dashboard", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
  NavItem("Arılıklar", "apiaries", Icons.Filled.LocationOn, Icons.Outlined.LocationOn, "nav_apiaries"),
  NavItem("Kovanlar", "hives", Icons.Filled.GridView, Icons.Outlined.GridView, "nav_hives"),
  NavItem("Takvim", "calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_calendar"),
  NavItem("Ayarlar", "settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
)

@Composable
fun BeeBottomNavBar(
  currentRoute: String?,
  onNavigate: (String) -> Unit
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 6.dp,
    modifier = Modifier.fillMaxWidth().testTag("bottom_navigation_bar")
  ) {
    bottomNavItems.forEach { item ->
      val isSelected = currentRoute == item.route
      NavigationBarItem(
        alwaysShowLabel = true,
        icon = {
          Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            modifier = Modifier.size(22.dp)
          )
        },
        label = {
          Text(
            text = item.title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            maxLines = 1,
            softWrap = false
          )
        },
        selected = isSelected,
        onClick = { onNavigate(item.route) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = HoneyGoldDark,
          selectedTextColor = HoneyGoldDark,
          indicatorColor = HoneyAmberContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag(item.testTag)
      )
    }
  }
}

@Composable
fun AppBrandHeader(
  pageSubtitle: String,
  modifier: Modifier = Modifier,
  actions: @Composable (() -> Unit)? = null
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f, fill = false)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "🐝",
          fontSize = 24.sp,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = "Arı Takip",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Black,
          color = HoneyGoldDark
        )
      }
      Text(
        text = pageSubtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )
    }
    if (actions != null) {
      Spacer(modifier = Modifier.width(8.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
      ) {
        actions()
      }
    }
  }
}

// ---------------- SKELETON LOADERS (V1.1 Section 4) ----------------

@Composable
fun SkeletonBox(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
  val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
  val alpha by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.7f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "skeleton_alpha"
  )

  Box(
    modifier = modifier
      .clip(shape)
      .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha * 0.15f))
  )
}

@Composable
fun WeatherCardSkeleton(modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = HoneyAmberContainer.copy(alpha = 0.6f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("weather_skeleton_card")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "🌤️", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
          Text(
            text = "Hava durumu yükleniyor...",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = HoneyOnAmberContainer
          )
        }
        CircularProgressIndicator(
          modifier = Modifier.size(16.dp),
          color = HoneyGoldDark,
          strokeWidth = 2.dp
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
      SkeletonBox(modifier = Modifier.fillMaxWidth().height(48.dp))
    }
  }
}

@Composable
fun HiveCardSkeleton(modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .testTag("hive_card_skeleton")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          SkeletonBox(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            SkeletonBox(modifier = Modifier.width(100.dp).height(16.dp))
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBox(modifier = Modifier.width(70.dp).height(12.dp))
          }
        }
        SkeletonBox(modifier = Modifier.width(60.dp).height(24.dp), shape = RoundedCornerShape(8.dp))
      }
      Spacer(modifier = Modifier.height(12.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SkeletonBox(modifier = Modifier.width(110.dp).height(22.dp))
        SkeletonBox(modifier = Modifier.width(80.dp).height(22.dp))
      }
    }
  }
}

// ---------------- WEATHER CARD WITH REFRESH & CACHE (V1.1 Section 15) ----------------

@Composable
fun WeatherCard(
  weather: ApiaryWeather?,
  apiaryName: String,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
  onRefresh: (() -> Unit)? = null
) {
  if (isLoading && weather == null) {
    WeatherCardSkeleton(modifier = modifier)
    return
  }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(3.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("weather_card")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Header: Apiary name and Refresh
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(HoneyAmberContainer),
            contentAlignment = Alignment.Center
          ) {
            Text(text = weather?.conditionIcon ?: "🌤️", fontSize = 22.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "$apiaryName — Canlı Hava",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (weather != null) {
                if (weather.lastUpdatedFormatted.isNotBlank()) "Open-Meteo • 30 dk Önbellek • Güncelleme: ${weather.lastUpdatedFormatted}"
                else "Open-Meteo • 30 dk Önbellek"
              } else {
                "Konum hava durumu"
              },
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = HoneyGoldDark,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Yenileniyor...",
              fontSize = 11.sp,
              color = HoneyGoldDark
            )
          } else if (onRefresh != null) {
            IconButton(
              onClick = onRefresh,
              modifier = Modifier.size(32.dp).testTag("refresh_weather_btn")
            ) {
              Icon(
                Icons.Filled.Refresh,
                contentDescription = "Hava Durumunu Yenile",
                tint = HoneyGoldDark,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (weather != null) {
        // Main Temperature & Condition Row (Highly prominent display)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HoneyAmberContainer.copy(alpha = 0.55f))
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = weather.conditionIcon,
              fontSize = 40.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "${weather.temperature.toInt()}°C",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = HoneyOnAmberContainer
              )
              Text(
                text = weather.conditionDescription,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = HoneyGoldDark
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "Hissedilen: ${weather.apparentTemperature.toInt()}°C",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = HoneyOnAmberContainer
            )
            Text(
              text = "Rüzgar: ${weather.windSpeed.toInt()} km/s",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = HoneyOnAmberContainer.copy(alpha = 0.85f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Detailed Metrics 4-Col Grid
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "🌡️ Hissedilen", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(text = "${weather.apparentTemperature.toInt()}°C", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "💧 Nem", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(text = "%${weather.humidity}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "💨 Rüzgar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(text = "${weather.windSpeed.toInt()} km/s", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "🌧️ Yağış", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(text = "%${weather.precipitationProbability}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bee flight advisory banner
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (weather.isBeeFlyingOptimal) ForestGreenContainer else Color(0xFFFFE4E6),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (weather.isBeeFlyingOptimal) "🟢 " else "🔴 ",
              fontSize = 14.sp
            )
            Text(
              text = weather.beeFlightMessage,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (weather.isBeeFlyingOptimal) ForestOnGreenContainer else Color(0xFF9F1239)
            )
          }
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Hava durumu bilgisi yüklenemedi.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (onRefresh != null) {
            TextButton(onClick = onRefresh) {
              Text("Tekrar Dene", color = HoneyGoldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

// ---------------- PHOTO GALLERY & THUMBNAIL DIALOG (V1.1 Sections 3, 11, 12) ----------------

data class GalleryPhotoItem(
  val uri: String,
  val caption: String = "",
  val date: String = ""
)

@Composable
fun PhotoThumbnailCard(
  uri: String,
  caption: String = "",
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    elevation = CardDefaults.cardElevation(2.dp),
    modifier = modifier
      .size(90.dp)
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .testTag("photo_thumbnail")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      AsyncImage(
        model = uri,
        contentDescription = caption.ifBlank { "Arılık Fotoğrafı" },
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      if (caption.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
          color = Color.Black.copy(alpha = 0.65f),
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
        ) {
          Text(
            text = caption,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}

@Composable
fun PhotoGalleryDialog(
  photos: List<GalleryPhotoItem>,
  initialIndex: Int = 0,
  onDismiss: () -> Unit
) {
  if (photos.isEmpty()) return

  var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, photos.size - 1)) }
  val currentPhoto = photos[currentIndex]

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.92f))
        .testTag("photo_gallery_dialog")
    ) {
      // Top Bar: Counter & Close
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 24.dp)
          .align(Alignment.TopCenter),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.White.copy(alpha = 0.2f)
        ) {
          Text(
            text = "📷 ${currentIndex + 1} / ${photos.size}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .testTag("gallery_close_btn")
        ) {
          Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = Color.White)
        }
      }

      // Center Image
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center)
          .padding(horizontal = 16.dp)
      ) {
        AsyncImage(
          model = currentPhoto.uri,
          contentDescription = currentPhoto.caption,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
        )
      }

      // Prev / Next Navigation Arrows
      if (photos.size > 1) {
        if (currentIndex > 0) {
          IconButton(
            onClick = { currentIndex-- },
            modifier = Modifier
              .align(Alignment.CenterStart)
              .padding(start = 12.dp)
              .size(44.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
          ) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Önceki", tint = Color.White, modifier = Modifier.size(32.dp))
          }
        }

        if (currentIndex < photos.size - 1) {
          IconButton(
            onClick = { currentIndex++ },
            modifier = Modifier
              .align(Alignment.CenterEnd)
              .padding(end = 12.dp)
              .size(44.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
          ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Sonraki", tint = Color.White, modifier = Modifier.size(32.dp))
          }
        }
      }

      // Bottom Caption Card
      if (currentPhoto.caption.isNotBlank() || currentPhoto.date.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color(0xFF1E1E1E).copy(alpha = 0.85f),
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(16.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            if (currentPhoto.caption.isNotBlank()) {
              Text(
                text = "🏷️ ${currentPhoto.caption}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
            if (currentPhoto.date.isNotBlank()) {
              Text(
                text = "📅 ${currentPhoto.date}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }
        }
      }
    }
  }
}

// ---------------- UI SELECTION CHIPS ----------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LargeOutdoorChipGroup(
  title: String,
  options: List<String>,
  selectedOption: String,
  onOptionSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
  tagPrefix: String = "chip"
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      options.forEach { option ->
        val isSelected = option == selectedOption
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (isSelected) HoneyGoldPrimary else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) HoneyGoldDark else MaterialTheme.colorScheme.outline,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { onOptionSelected(option) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("${tagPrefix}_$option"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = option,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LargeOutdoorMultiSelectGroup(
  title: String,
  options: List<String>,
  selectedOptions: Set<String>,
  onOptionToggled: (String) -> Unit,
  modifier: Modifier = Modifier,
  tagPrefix: String = "multichip"
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 6.dp)
    )
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      options.forEach { option ->
        val isSelected = selectedOptions.contains(option)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (isSelected) ForestGreenSecondary else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) ForestGreenSecondary else MaterialTheme.colorScheme.outline,
              shape = RoundedCornerShape(12.dp)
            )
            .clickable { onOptionToggled(option) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("${tagPrefix}_$option"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (isSelected) "✓ " else "+ ",
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = option,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun EmptyStateCard(
  title: String,
  description: String,
  buttonText: String,
  iconEmoji: String = "🐝",
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "empty_state_card"
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(2.dp),
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp)
      .testTag(testTag)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(text = iconEmoji, fontSize = 44.sp)
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = HoneyGoldPrimary,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .clickable { onButtonClick() }
          .testTag("empty_state_action_button")
      ) {
        Text(
          text = buttonText,
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
      }
    }
  }
}

@Composable
fun ConfirmationDialog(
  title: String,
  message: String,
  confirmButtonText: String = "Evet, Onayla",
  dismissButtonText: String = "Vazgeç",
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(text = title, fontWeight = FontWeight.Bold) },
    text = { Text(text = message) },
    confirmButton = {
      TextButton(onClick = onConfirm, modifier = Modifier.testTag("confirm_dialog_btn")) {
        Text(text = confirmButtonText, fontWeight = FontWeight.Bold, color = HoneyGoldDark)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = dismissButtonText)
      }
    }
  )
}
