package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = ZamZamPrimaryDark,
    secondary = ZamZamSecondaryDark,
    tertiary = SuccessGreen,
    background = ZamZamBackgroundDark,
    surface = ZamZamSurfaceDark,
    onPrimary = ZamZamBackgroundDark,
    onSecondary = ZamZamBackgroundDark,
    onTertiary = ZamZamBackgroundDark,
    onBackground = ZamZamTextLight,
    onSurface = ZamZamTextLight,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ZamZamPrimary,
    secondary = ZamZamSecondary,
    tertiary = ZamZamTertiary,
    background = ZamZamBackground,
    surface = ZamZamSurface,
    onPrimary = ZamZamSurface,
    onSecondary = ZamZamSurface,
    onTertiary = ZamZamSurface,
    onBackground = ZamZamTextDark,
    onSurface = ZamZamTextDark,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain the strict Green & White "Zam Zam" visual identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
