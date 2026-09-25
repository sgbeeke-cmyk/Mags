package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Track
import com.example.ui.components.HiResBadge
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
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTrack = Track(
      id = "test_track",
      title = "Neon Odyssey",
      artist = "Aura Resonance",
      album = "Prismatic Horizons",
      durationMs = 45000L,
      uri = "",
      format = "FLAC",
      sampleRate = 96000,
      bitDepth = 24,
      isHiRes = true
    )

    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        HiResBadge(track = sampleTrack)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

