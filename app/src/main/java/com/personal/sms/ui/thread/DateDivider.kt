package com.personal.sms.ui.thread

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.TimeBucket
import com.personal.sms.domain.timeBucket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateDivider(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp),
    )
}

/** Header label for a message's day: Today / Yesterday / weekday / date. */
fun dayLabel(epochMs: Long, now: Long = System.currentTimeMillis()): String =
    when (timeBucket(epochMs, now)) {
        TimeBucket.TODAY -> "Today"
        TimeBucket.YESTERDAY -> "Yesterday"
        TimeBucket.THIS_WEEK -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(epochMs))
        TimeBucket.OLDER -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(epochMs))
    }
