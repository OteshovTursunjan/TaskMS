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
    primary = ElectricCobalt,
    onPrimary = Color(0xFF061630),
    primaryContainer = CobaltContainerDark,
    onPrimaryContainer = Color(0xFFD8E6FF),
    secondary = AmberFlame,
    onSecondary = Color(0xFF281700),
    secondaryContainer = AmberContainerDark,
    onSecondaryContainer = Color(0xFFFDE68A),
    tertiary = EmeraldMint,
    onTertiary = Color(0xFF002117),
    tertiaryContainer = EmeraldContainerDark,
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = CoralCrimson,
    onError = Color.White,
    errorContainer = CoralContainerDark,
    onErrorContainer = Color(0xFFFECACA),
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = SlateSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorder,
    outlineVariant = Color(0xFF202938)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepCobalt,
    onPrimary = Color.White,
    primaryContainer = CobaltContainerLight,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = AmberDeep,
    onSecondary = Color.White,
    secondaryContainer = AmberContainerLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = EmeraldDeep,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldContainerLight,
    onTertiaryContainer = Color(0xFF064E3B),
    error = CoralCrimson,
    onError = Color.White,
    errorContainer = CoralContainerLight,
    onErrorContainer = Color(0xFF7F1D1D),
    background = PaperBg,
    onBackground = TextPrimaryLight,
    surface = PaperSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = PaperBorder,
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun SprintFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
        typography = SprintFlowTypography,
        content = content
    )
}
