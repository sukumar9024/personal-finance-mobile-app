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

val Emerald = Color(0xFF34D399)
val EmeraldMuted = Color(0xFF99F6E4)
val Sky = Color(0xFF7DD3FC)
val Amber = Color(0xFFF59E0B)
val Rose = Color(0xFFFB7185)
val Slate950 = Color(0xFF09111F)
val Slate900 = Color(0xFF0F172A)
val Slate850 = Color(0xFF152033)
val Slate800 = Color(0xFF1C2940)
val Slate700 = Color(0xFF2A3954)
val Slate200 = Color(0xFFD7E1F0)
val Slate100 = Color(0xFFF3F7FB)
val TextStrong = Color(0xFFE8EEF8)
val TextMuted = Color(0xFFA8B7CE)
val Purple40 = Emerald

private val DarkColorScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = Slate950,
    primaryContainer = Color(0xFF12372D),
    onPrimaryContainer = EmeraldMuted,
    secondary = Sky,
    onSecondary = Slate950,
    tertiary = Amber,
    onTertiary = Slate950,
    error = Rose,
    background = Slate900,
    onBackground = TextStrong,
    surface = Slate850,
    onSurface = TextStrong,
    surfaceVariant = Slate800,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF4A5D7E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0F9F76),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8FAEF),
    onPrimaryContainer = Color(0xFF053126),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    tertiary = Color(0xFFB7791F),
    onTertiary = Color.White,
    error = Color(0xFFDC2626),
    background = Slate100,
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE7EEF8),
    onSurfaceVariant = Color(0xFF5B6B83),
    outline = Color(0xFFC7D3E4)
)

private val FinanceTypography = Typography(
    headlineMedium = TextStyle(
        fontSize = 30.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FinanceTypography,
        content = content
    )
}
