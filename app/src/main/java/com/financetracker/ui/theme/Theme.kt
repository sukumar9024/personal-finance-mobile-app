package com.financetracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Primary Colors
val PrimaryGreen = Color(0xFF10B981)
val PrimaryGreenDark = Color(0xFF059669)
val PrimaryGreenLight = Color(0xFF34D399)

// Secondary Colors
val SecondaryBlue = Color(0xFF3B82F6)
val SecondaryBlueDark = Color(0xFF2563EB)
val SecondaryBlueLight = Color(0xFF60A5FA)

// Accent Colors
val AccentAmber = Color(0xFFF59E0B)
val AccentPurple = Color(0xFF8B5CF6)
val AccentRose = Color(0xFFF43F5E)
val AccentTeal = Color(0xFF14B8A6)
val AccentIndigo = Color(0xFF6366F1)

// Neutral Colors - Dark Theme
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkSurfaceElevated = Color(0xFF253044)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkOutline = Color(0xFF475569)
val DarkOutlineVariant = Color(0xFF334155)

// Neutral Colors - Light Theme
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightSurfaceElevated = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF1E293B)
val LightOnSurfaceVariant = Color(0xFF64748B)
val LightOutline = Color(0xFFE2E8F0)
val LightOutlineVariant = Color(0xFFF1F5F9)

// Semantic Colors
val SuccessGreen = Color(0xFF22C55E)
val WarningAmber = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = SecondaryBlueDark,
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = AccentPurple,
    onTertiary = Color.White,
    error = ErrorRed,
    onError = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseSurface = DarkOnBackground,
    inverseOnSurface = DarkSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E3A5F),
    tertiary = AccentPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEDE9FE),
    onTertiaryContainer = Color(0xFF4C1D95),
    error = ErrorRed,
    onError = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = LightOnBackground,
    inverseOnSurface = LightSurface
)

val appTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Normal
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography,
        content = content
    )
}