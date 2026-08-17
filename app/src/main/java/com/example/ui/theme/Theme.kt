package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = HoneyGoldLight,
  onPrimary = Color(0xFF451A03),
  primaryContainer = HoneyGoldDark,
  onPrimaryContainer = Color(0xFFFEF3C7),
  secondary = ForestGreenLight,
  onSecondary = Color(0xFF052E16),
  secondaryContainer = Color(0xFF166534),
  onSecondaryContainer = Color(0xFFDCFCE7),
  tertiary = Color(0xFFFBBF24),
  onTertiary = Color(0xFF451A03),
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  outline = Color(0xFF443D36)
)

private val LightColorScheme = lightColorScheme(
  primary = HoneyGoldPrimary,
  onPrimary = Color.White,
  primaryContainer = HoneyAmberContainer,
  onPrimaryContainer = HoneyOnAmberContainer,
  secondary = ForestGreenSecondary,
  onSecondary = Color.White,
  secondaryContainer = ForestGreenContainer,
  onSecondaryContainer = ForestOnGreenContainer,
  tertiary = EarthWoodTertiary,
  onTertiary = Color.White,
  tertiaryContainer = EarthWoodContainer,
  onTertiaryContainer = EarthOnWoodContainer,
  background = WarmCreamBackground,
  onBackground = DarkEarthText,
  surface = WarmCreamSurface,
  onSurface = DarkEarthText,
  surfaceVariant = WarmCreamSurfaceVariant,
  onSurfaceVariant = DarkEarthTextSecondary,
  outline = OutlineWarm
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our signature warm beekeeping palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
