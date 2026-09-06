package com.family.menu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7EFE0),
    onSecondaryContainer = BrandGreenDark,
    tertiary = Color(0xFFC8A05C),
    background = BgWarm,
    onBackground = InkPrimary,
    surface = SurfaceCard,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = Color(0xFFD9CCB8),
    outlineVariant = Color(0xFFE5DBC7),
    error = Color(0xFFC0392B),
    onError = Color.White
)

/** 暖棕深色：护眼暗调但保留「橙红主色」家庭感 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB59E),
    onPrimary = Color(0xFF611D08),
    primaryContainer = Color(0xFF7A2F17),
    onPrimaryContainer = Color(0xFFFFDBCE),
    secondary = Color(0xFFB7CDA9),
    onSecondary = Color(0xFF25351D),
    secondaryContainer = Color(0xFF3B4C31),
    onSecondaryContainer = Color(0xFFD3EAC4),
    background = Color(0xFF1B130E),
    onBackground = Color(0xFFEFE1D5),
    surface = Color(0xFF241A13),
    onSurface = Color(0xFFEFE1D5),
    surfaceVariant = Color(0xFF4C3B30),
    onSurfaceVariant = Color(0xFFD2BBA9),
    outline = Color(0xFF9C8578),
    outlineVariant = Color(0xFF4C3B30),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val FamilyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FamilyMenuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val bg = if (darkTheme) Color(0xFF1B130E) else BgWarm

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = bg.toArgb()
            window.navigationBarColor = bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = FamilyMenuTypography,
        shapes = FamilyShapes,
        content = content
    )
}
