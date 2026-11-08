package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = Amber400,
    onPrimary = Slate900,
    primaryContainer = Color(0xFF452707),
    onPrimaryContainer = Amber100,
    secondary = Color(0xFF2DD4BF),
    onSecondary = Slate900,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFA78BFA),
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200
)

private val LightColorScheme = lightColorScheme(
    primary = Amber600,
    onPrimary = Color.White,
    primaryContainer = Amber100,
    onPrimaryContainer = Color(0xFF451A03),
    secondary = Teal600,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF134E4A),
    tertiary = Color(0xFF6D28D9),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Color(0xFFEFF3F8),
    onSurfaceVariant = Slate700
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = Color.Black,
    surface = Color(0xFF0A0D14),
    surfaceVariant = Color(0xFF161B26)
)

private val SepiaColorScheme = LightColorScheme.copy(
    background = ReaderSepiaBg,
    onBackground = ReaderSepiaText,
    surface = Color(0xFFFAF3E3),
    onSurface = ReaderSepiaText,
    surfaceVariant = Color(0xFFEADFC7),
    onSurfaceVariant = Color(0xFF5C4934),
    primary = Color(0xFF9A5B13),
    primaryContainer = Color(0xFFEAD8B8)
)

private val PaperColorScheme = LightColorScheme.copy(
    background = ReaderPaperBg,
    onBackground = ReaderPaperText,
    surface = Color(0xFFFFFDF9),
    onSurface = ReaderPaperText,
    surfaceVariant = Color(0xFFEFECE4),
    onSurfaceVariant = Color(0xFF4A423B)
)

@Composable
fun ChapterReaderTheme(
    themeMode: String = "SYSTEM",
    language: String = "ar",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        "LIGHT" -> LightColorScheme
        "DARK" -> DarkColorScheme
        "AMOLED" -> AmoledColorScheme
        "SEPIA" -> SepiaColorScheme
        "PAPER" -> PaperColorScheme
        else -> if (systemDark) DarkColorScheme else LightColorScheme
    }

    val layoutDirection = if (language == "en") LayoutDirection.Ltr else LayoutDirection.Rtl

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
