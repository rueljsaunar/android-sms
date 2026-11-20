package com.personal.sms.ui.thread

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.clockTime
import com.personal.sms.domain.model.MessageStatus

/** "Delivered · 9:41 AM ✓✓" line shown under the last sent message. */
@Composable
fun DeliveryStatus(status: MessageStatus?, date: Long, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val label: String
    val icon: ImageVector
    val tint = when (status) {
        MessageStatus.DELIVERED -> {
            label = "Delivered · ${clockTime(date)}"; icon = Icons.Rounded.DoneAll; colorScheme.primary
        }
        MessageStatus.SENT -> {
            label = "Sent · ${clockTime(date)}"; icon = Icons.Rounded.Check; colorScheme.onSurfaceVariant
        }
        MessageStatus.SENDING -> {
            label = "Sending…"; icon = Icons.Rounded.Schedule; colorScheme.onSurfaceVariant
        }
        MessageStatus.FAILED -> {
            label = "Failed to send"; icon = Icons.Rounded.ErrorOutline; colorScheme.error
        }
        null -> return
    }
    Row(modifier = modifier.padding(top = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
    }
}
