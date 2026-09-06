package com.family.menu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseTitle = TextStyle(fontWeight = FontWeight.Bold, color = InkPrimary)
private val baseBody = TextStyle(color = InkPrimary)

val FamilyMenuTypography = Typography(
    displayLarge = baseTitle.copy(fontSize = 36.sp, lineHeight = 44.sp),
    displayMedium = baseTitle.copy(fontSize = 30.sp, lineHeight = 38.sp),
    displaySmall = baseTitle.copy(fontSize = 26.sp, lineHeight = 34.sp),

    headlineLarge = baseTitle.copy(fontSize = 24.sp, lineHeight = 32.sp),
    headlineMedium = baseTitle.copy(fontSize = 22.sp, lineHeight = 30.sp),
    headlineSmall = baseTitle.copy(fontSize = 20.sp, lineHeight = 28.sp),

    titleLarge = baseTitle.copy(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = baseTitle.copy(fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = baseTitle.copy(fontSize = 15.sp, lineHeight = 22.sp),

    bodyLarge = baseBody.copy(fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = baseBody.copy(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = baseBody.copy(fontSize = 13.sp, lineHeight = 20.sp, color = InkSecondary),

    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, color = InkPrimary),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, color = InkPrimary),
    labelSmall = TextStyle(fontSize = 11.sp, color = InkSecondary)
)