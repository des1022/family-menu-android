package com.family.menu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
    // 阶段三将扩展深色模式配色，先固定浅色
    val colors = LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgWarm.toArgb()
            window.navigationBarColor = BgWarm.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = FamilyMenuTypography,
        shapes = FamilyShapes,
        content = content
    )
}