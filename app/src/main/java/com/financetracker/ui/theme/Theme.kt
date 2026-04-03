package com.financetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.financetracker.data.model.Category

// ==================== FUTURISTIC COLOR PALETTE ====================

// Primary accent - Electric Blue gradient
val ElectricBlue = Color(0xFF00D4FF)
val ElectricBlueDark = Color(0xFF0099CC)
val NeonPurple = Color(0xFF8B5CF6)
val NeonPink = Color(0xFFF472B6)
val CyberGreen = Color(0xFF10B981)
val CyberTeal = Color(0xFF14B8A6)
val CyberOrange = Color(0xFFF59E0B)
val CyberRed = Color(0xFFEF4444)
val CyberYellow = Color(0xFFFBBF24)

// Glass morphism surfaces
val GlassSurfaceLight = Color(0x80FFFFFF)
val GlassSurfaceDark = Color(0x801A1A2E)
val GlassBorderLight = Color(0x4000D4FF)
val GlassBorderDark = Color(0x408B5CF6)

// Deep space backgrounds
val DeepSpaceLight = Color(0xFFF8FAFC)
val DeepSpaceDark = Color(0xFF0A0A1A)
val NebulaSurface = Color(0xFF12122A)
val StardustSurface = Color(0xFF1E1E3F)

// Light Theme - Quantum Glass
private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FF),
    onPrimaryContainer = Color(0xFF006699),
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0EBFF),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = CyberGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F9F0),
    onTertiaryContainer = Color(0xFF047857),
    error = CyberRed,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFFB91C1C),
    background = DeepSpaceLight,
    onBackground = Color(0xFF1E293B),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    inverseSurface = Color(0xFF0F172A),
    inverseOnSurface = Color(0xFFF1F5F9),
    inversePrimary = NeonPurple,
    surfaceDim = Color(0xFFF1F5F9),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color(0xFFFAFBFC),
    surfaceContainerLow = Color(0xFFEFF3F8),
    surfaceContainer = Color(0xFFE8EDF3),
    surfaceContainerHigh = Color(0xFFDFE6EE),
    surfaceContainerHighest = Color(0xFFD6DEE8)
)

// Dark Theme - Neon Cyberpunk
private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color(0xFF0A0A1A),
    primaryContainer = Color(0xFF003D5C),
    onPrimaryContainer = Color(0xFF80EFFF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF0A0A1A),
    secondaryContainer = Color(0xFF3B1F7D),
    onSecondaryContainer = Color(0xFFD8B4FE),
    tertiary = CyberGreen,
    onTertiary = Color(0xFF0A0A1A),
    tertiaryContainer = Color(0xFF065F46),
    onTertiaryContainer = Color(0xFF6EE7B7),
    error = CyberRed,
    onError = Color(0xFF0A0A1A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFCA5A5),
    background = DeepSpaceDark,
    onBackground = Color(0xFFE2E8F0),
    surface = NebulaSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = StardustSurface,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155),
    inverseSurface = Color(0xFFF1F5F9),
    inverseOnSurface = Color(0xFF0F172A),
    inversePrimary = ElectricBlue,
    surfaceDim = Color(0xFF0A0A15),
    surfaceBright = Color(0xFF1A1A2E),
    surfaceContainerLowest = Color(0xFF050510),
    surfaceContainerLow = Color(0xFF0D0D1F),
    surfaceContainer = Color(0xFF12122A),
    surfaceContainerHigh = Color(0xFF1A1A35),
    surfaceContainerHighest = Color(0xFF222240)
)

// ==================== MODERN TYPOGRAPHY ====================

private val ModernTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 42.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )
)

// ==================== ADVANCED SHAPES ====================

val GlassShape = RoundedCornerShape(20.dp)
val ModernCardShape = RoundedCornerShape(24.dp)
val ModernButtonShape = RoundedCornerShape(16.dp)
val ModernInputShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(50)
val OctagonShape = RoundedCornerShape(28.dp)

// Backward compatibility aliases
@Deprecated("Use ModernCardShape or GlassShape instead", ReplaceWith("ModernCardShape"))
val CardShape = ModernCardShape

// Spacing constants
val ModernScreenPadding = 16.dp
val ModernCardPadding = 20.dp
val ModernElementSpacing = 12.dp

// Backward compatibility alias
@Deprecated("Use SectionHeader from AppStyle or this function instead")
@Composable
fun FinanceSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showDivider) {
            Surface(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(0.3f),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ) {
                Box(modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

// ==================== GRADIENT DEFINITIONS ====================

val PrimaryGradient = listOf(ElectricBlue, NeonPurple)
val SuccessGradient = listOf(CyberGreen, CyberTeal)
val WarningGradient = listOf(CyberOrange, CyberYellow)
val DangerGradient = listOf(CyberRed, NeonPink)
val BrandGradient = listOf(ElectricBlue, NeonPurple, NeonPink)

// Category colors - Futuristic neon palette (used if not specified in AppStyle)
val NeonCategoryColors = listOf(
    Color(0xFFFF6B6B), // Neon Red
    Color(0xFF4ECDC4), // Cyan
    Color(0xFF45B7D1), // Sky Blue
    Color(0xFF96CEB4), // Mint
    Color(0xFFFFEAA7), // Soft Yellow
    Color(0xFFDDA0DD), // Lilac
    Color(0xFF98D8C8), // Seafoam
    Color(0xFFF7DC6F), // Gold
    Color(0xFFBB8FCE), // Violet
    Color(0xFF85C1E9), // Baby Blue
    Color(0xFFF8C471), // Peach
    Color(0xFF82E0AA), // Light Green
    Color(0xFFD2B4DE), // Lavender
    Color(0xFFAED6F1), // Powder Blue
    Color(0xFFF9E79F), // Pale Yellow
    Color(0xFFA3E4D7)  // Aqua
)

// ==================== ANIMATIONS ====================

@Composable
fun shimmerBrush(targetValue: Float = 1000f): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.2f),
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

@Composable
fun gradientBrush(colors: List<Color>): Brush {
    val transition = rememberInfiniteTransition(label = "gradient")
    val angle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_rotation"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(0f, 0f),
        end = Offset(cos(angle.value * Math.PI / 180f).toFloat() * 1000f, sin(angle.value * Math.PI / 180f).toFloat() * 1000f)
    )
}

// Math helpers for gradient animation
private fun cos(radians: Double): Float {
    return kotlin.math.cos(radians).toFloat()
}

private fun sin(radians: Double): Float {
    return kotlin.math.sin(radians).toFloat()
}

// ==================== REUSABLE UI COMPONENTS ====================

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShape,
    darkTheme: Boolean = isSystemInDarkTheme(),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val containerColor = if (darkTheme) GlassSurfaceDark else GlassSurfaceLight
    val borderColor = if (darkTheme) GlassBorderDark else GlassBorderLight
    
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = { onClick?.invoke() }
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            containerColor.copy(alpha = 0.7f),
                            containerColor.copy(alpha = 0.3f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(colors = listOf(borderColor, borderColor.copy(alpha = 0.1f))),
                    shape = shape
                )
                .padding(ModernCardPadding)
        ) {
            content()
        }
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = PrimaryGradient,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = ModernButtonShape,
        onClick = onClick,
        enabled = enabled
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(colors = gradientColors)
                )
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.invoke()
                if (icon != null) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = PrimaryGradient,
    icon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = OctagonShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = if (icon == null) Alignment.CenterHorizontally else Alignment.Start
            ) {
                if (icon != null) {
                    icon()
                }
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// ==================== THEME COMPOSABLE ====================

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Edge-to-edge display
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Status bar and navigation bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModernTypography,
        content = content
    )
}

// Dynamic color support for Android 12+
@Composable
private fun dynamicLightColorScheme(context: android.content.Context): androidx.compose.material3.ColorScheme {
    return lightColorScheme(
        primary = ElectricBlue,
        secondary = NeonPurple,
        tertiary = CyberGreen,
        background = DeepSpaceLight,
        surface = Color.White,
        onPrimary = Color.White
    )
}

@Composable
private fun dynamicDarkColorScheme(context: android.content.Context): androidx.compose.material3.ColorScheme {
    return darkColorScheme(
        primary = ElectricBlue,
        secondary = NeonPurple,
        tertiary = CyberGreen,
        background = DeepSpaceDark,
        surface = NebulaSurface,
        onPrimary = Color(0xFF0A0A1A)
    )
}