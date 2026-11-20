package com.personal.sms.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.sms.ui.theme.LocalAppExtraColors

/** Purple caps section header, e.g. SUGGESTED / CONTACTS / A. */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = LocalAppExtraColors.current.sectionHeader,
        modifier = modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
    )
}
