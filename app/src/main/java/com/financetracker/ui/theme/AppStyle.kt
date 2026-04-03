package com.financetracker.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financetracker.data.model.Category
import kotlin.math.absoluteValue
import kotlin.math.roundToLong
import java.text.NumberFormat
import java.util.Locale
import com.financetracker.data.model.Currency as AppCurrency

// Shape Definitions
val Shapes = ShapeFamily(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
    full = RoundedCornerShape(50)
)

data class ShapeFamily(
    val extraSmall: RoundedCornerShape,
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape,
    val extraLarge: RoundedCornerShape,
    val full: RoundedCornerShape
)

// Spacing Scale
object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

// Component Constants
val ScreenPadding = 16.dp
val CardElevation = 1.dp
val CardBorderWidth = 1.dp

// Currency Formatting
private object CurrencyFormattingState {
    @Volatile
    var selectedCurrency: AppCurrency = AppCurrency.getDefault()
}

fun setPreferredCurrency(currency: AppCurrency) {
    CurrencyFormattingState.selectedCurrency = currency
}

private fun currentCurrency(): AppCurrency = CurrencyFormattingState.selectedCurrency

private fun localeForCurrency(currency: AppCurrency): Locale {
    return when (currency) {
        AppCurrency.USD -> Locale.US
        AppCurrency.EUR -> Locale.GERMANY
        AppCurrency.GBP -> Locale.UK
        AppCurrency.INR -> Locale("en", "IN")
        AppCurrency.JPY -> Locale.JAPAN
        AppCurrency.CNY -> Locale.CHINA
        AppCurrency.AUD -> Locale("en", "AU")
        AppCurrency.CAD -> Locale.CANADA
        AppCurrency.SGD -> Locale("en", "SG")
        AppCurrency.AED -> Locale("en", "AE")
    }
}

private fun fractionDigitsFor(currency: AppCurrency): Int {
    return when (currency) {
        AppCurrency.JPY -> 0
        else -> 2
    }
}

fun formatCurrency(amount: Double, currency: AppCurrency = currentCurrency()): String {
    val format = NumberFormat.getCurrencyInstance(localeForCurrency(currency))
    format.currency = java.util.Currency.getInstance(currency.code)
    val fractionDigits = fractionDigitsFor(currency)
    format.minimumFractionDigits = fractionDigits
    format.maximumFractionDigits = fractionDigits
    return format.format(amount)
}

fun formatCurrencyRounded(amount: Double, currency: AppCurrency = currentCurrency()): String {
    val roundedAmount = amount.absoluteValue.roundToLong()
    val sign = if (amount < 0) "-" else ""
    return if (roundedAmount >= 1000) {
        val thousands = roundedAmount / 1000.0
        if (thousands == thousands.toLong().toDouble()) {
            "$sign${currency.symbol}${thousands.toLong()}k"
        } else {
            "$sign${currency.symbol}${String.format(Locale.US, "%.1f", thousands)}k"
        }
    } else {
        "$sign${currency.symbol}$roundedAmount"
    }
}

// Category Color System
val CategoryColors = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFFF43F5E), // Rose
    Color(0xFFF59E0B), // Amber
    Color(0xFF22C55E), // Green
    Color(0xFF14B8A6), // Teal
    Color(0xFF06B6D4), // Cyan
    Color(0xFF6366F1), // Indigo
    Color(0xFFD946EF)  // Fuchsia
)

fun categoryColor(category: Category?): Color {
    if (category == null) return CategoryColors.first()
    return try {
        val hexColor = category.color
        if (hexColor.isNotBlank()) {
            Color(AndroidColor.parseColor(hexColor))
        } else {
            CategoryColors[category.name.hashCode() % CategoryColors.size]
        }
    } catch (e: Exception) {
        CategoryColors[category.name.hashCode() % CategoryColors.size]
    }
}

// ========================
// REUSABLE COMPONENTS
// ========================

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = CardElevation,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ) else Modifier
            ),
        shape = Shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        content()
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
    ),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = Shapes.extraLarge,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset.Zero,
                        end = androidx.compose.ui.geometry.Offset.Infinite
                    )
                )
                .padding(Spacing.xxl)
        ) {
            content()
        }
    }
}

@Composable
fun SectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            horizontalAlignment = Alignment.Start
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label.uppercase(Locale.US),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BadgeChip(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = Shapes.full,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
        )
    }
}

@Composable
fun IconCircle(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    containerAlpha: Float = 0.12f
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = containerAlpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun AnimatedProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(Shapes.full)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.value)
                .height(height)
                .clip(Shapes.full)
                .background(color)
        )
    }
}

@Composable
fun EmptyStateCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.xxl, vertical = Spacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            IconCircle(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                size = 72.dp,
                iconSize = 36.dp
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis
            )
            
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Surface(
                    shape = Shapes.medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onActionClick)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionText,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun ErrorBanner(
    modifier: Modifier = Modifier,
    message: String
) {
    if (message.isBlank()) return
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconCircle(
                icon = Icons.Default.TrendingUp,
                tint = MaterialTheme.colorScheme.error,
                size = 36.dp,
                iconSize = 18.dp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
