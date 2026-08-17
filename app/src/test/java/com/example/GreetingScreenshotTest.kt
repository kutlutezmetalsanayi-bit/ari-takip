package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.TurkeyLocationSelector
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class LocationSelectorScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun location_selector_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        TurkeyLocationSelector(
          selectedCity = "İstanbul",
          selectedDistrict = "Şile",
          onLocationSelected = { _, _, _, _ -> }
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/location_selector.png")
  }
}


