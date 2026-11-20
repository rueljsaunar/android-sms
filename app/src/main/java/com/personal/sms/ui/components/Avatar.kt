package com.personal.sms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.sms.domain.avatarColorIndex
import com.personal.sms.ui.theme.LocalAppExtraColors

/**
 * Colored circular avatar with initials, matching the mockups. Color is derived
 * deterministically from [colorKey] so a contact keeps the same color. (Contact
 * photos aren't rendered — every mockup avatar is an initials circle, and showing
 * photos would require an image-loading library.)
 */
@Composable
fun Avatar(
    label: String,
    colorKey: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val palette = LocalAppExtraColors.current.avatarPalette
    val color: Color = palette[avatarColorIndex(colorKey, palette.size)]
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.34f).sp,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
