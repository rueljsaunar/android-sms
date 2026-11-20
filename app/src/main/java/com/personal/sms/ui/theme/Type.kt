package com.personal.sms.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.personal.sms.R

/** Bundled Inter family (static weights). */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/**
 * M3 type scale rendered in Inter. Only the roles the UI actually uses are
 * customized; the rest inherit sensible defaults with the Inter family applied.
 */
val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 34.sp),
        headlineMedium = headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Normal),
        bodyMedium = bodyMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Normal),
        bodySmall = bodySmall.copy(fontFamily = Inter, fontWeight = FontWeight.Normal),
        labelLarge = labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
    )
}
