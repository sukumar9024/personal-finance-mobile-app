package com.financetracker.ui.theme

import android.app.Activity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.financetracker.data.model.Category

// Professional Color Palette - Finance-themed
val PrimaryBlue = Color(0xFF1976D2)
val PrimaryBlueDark = Color(0xFF0D47A1)
val AccentGreen = Color(0xFF4CAF50)
val AccentOrange = Color(0xFFFF9800)
val AccentRed = Color(0xFFE53935)
val AccentPurple = Color(0xFF7B1FA2)

// Light Theme Colors
val LightBackground = Color(0xFFFFFEFE)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF5F5F5)
val LightOutline = Color(0xFFE0E0E0)
val LightOnBackground = Color(0xFF212121)
val LightOnSurface = Color(0xFF212121)
val LightOnSurfaceVariant = Color(0xFF616161)

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val DarkOutline = Color(0xFF404040)
val DarkOnBackground = Color(0xFFE1E1E1)
val DarkOnSurface = Color(0xFFE1E1E1)
val DarkOnSurfaceVariant = Color(0xFFB0B0B0)

// Category Colors (vibrant but professional)
val CategoryFood = Color(0xFFFF6B6B)
val CategoryTransport = Color(0xFF4ECDC4)
val CategoryShopping = Color(0xFF45B7D1)
val CategoryBills = Color(0xFF96CEB4)
val CategoryEntertainment = Color(0xFFFFEAA7)
val CategoryHealth = Color(0xFFDDA0DD)
val CategoryEducation = Color(0xFF98D8C8)
val CategoryOther = Color(0xFFB0BEC5)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = LightSurface,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    error = AccentRed,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    inverseSurface = DarkSurfaceVariant,
    inverseOnSurface = DarkOnBackground
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = PrimaryBlueDark,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    onTertiary = Color.White,
    error = AccentRed,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    inverseSurface = LightSurfaceVariant,
    inverseOnSurface = LightOnBackground
)

private val FinanceTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 40.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = TextStyle(
        fontSize = 34.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineLarge = TextStyle(
        fontSize = 26.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineMedium = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    headlineSmall = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
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
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium
    )
)

// Shape definitions
val CardShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(12.dp)
val InputShape = RoundedCornerShape(14.dp)

// Spacing constants
val ScreenPadding = 20.dp
val CardPadding = 20.dp
val ElementSpacing = 12.dp

// Format currency with proper formatting
fun formatCurrency(amount: Double): String {
    return "₹${"%,.2f".format(amount)}"
}

fun formatCurrencyRounded(amount: Double): String {
    return "₹${"%,.0f".format(amount)}"
}

// Get category color from theme or fallback
fun categoryColor(category: Category?): Color {
    if (category == null) return PrimaryBlue
    
    return when (category.name.lowercase()) {
        "food" -> CategoryFood
        "transport" -> CategoryTransport
        "shopping" -> CategoryShopping
        "bills" -> CategoryBills
        "entertainment" -> CategoryEntertainment
        "health" -> CategoryHealth
        "education" -> CategoryEducation
        else -> CategoryOther
    }
}

// Enhanced Gradient Colors for Premium Feel
val GradientStart = Color(0xFF667eea)
val GradientEnd = Color(0xFF764ba2)
val GradientGreenStart = Color(0xFF11998e)
val GradientGreenEnd = Color(0xFF38ef7d)
val GradientOrangeStart = Color(0xFFf093fb)
val GradientOrangeEnd = Color(0xFFf5576c)

// Shimmer animation for loading states
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
}

// Reusable UI Components
@Composable
fun FinanceHeroCard(
    modifier: Modifier = Modifier,
    useGradient: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (useGradient) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (useGradient) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(GradientStart, GradientEnd),
                                start = Offset.Zero,
                                end = Offset.Infinite
                            ),
                            shape = CardShape
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(CardPadding)
        ) {
            content()
        }
    }
}

@Composable
fun FinanceInlineBadge(
    text: String,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = false
) {
    val transition = if (isAnimated) {
        rememberInfiniteTransition(label = "badge")
    } else null

    val alpha = if (isAnimated && transition != null) {
        transition.animateFloat(
            initialValue = 0.18f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "badge_alpha"
        ).value
    } else {
        0.18f
    }

    Surface(
        modifier = modifier,
        shape = ButtonShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

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

@Composable
fun FinanceStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val backgroundColor = accentColor?.copy(alpha = 0.12f) 
        ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accentColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FinanceProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Int = 10
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(height.dp))
            .background(trackColor)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(height.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(color, color.copy(alpha = 0.7f))
                    )
                )
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .padding(vertical = (height / 2).dp)
        )
    }
}

@Composable
fun FinanceAnimatedIcon(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val scale = if (isActive) 1.1f else 1.0f
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

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
