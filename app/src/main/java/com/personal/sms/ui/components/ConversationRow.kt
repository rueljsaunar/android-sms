package com.personal.sms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.clockTime
import com.personal.sms.domain.detectOtp
import com.personal.sms.domain.model.Conversation
import com.personal.sms.domain.relativeTime
import com.personal.sms.ui.theme.LocalAppExtraColors

@Composable
fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onCopyOtp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val extra = LocalAppExtraColors.current
    val unread = conversation.unreadCount > 0
    val otp = if (!conversation.isScheduled) detectOtp(conversation.snippet) else null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(label = conversation.avatarLabel(), colorKey = conversation.address, size = 52.dp)
        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.size(3.dp))

            if (conversation.isScheduled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = extra.scheduled,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "Scheduled · ${conversation.scheduledAt?.let { clockTime(it) }.orEmpty()}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = extra.scheduled,
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.snippet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (unread) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (otp != null) {
                        Spacer(Modifier.width(8.dp))
                        OtpCopyChip(onCopy = { onCopyOtp(otp.code) })
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = relativeTime(conversation.date),
                style = MaterialTheme.typography.labelMedium,
                color = if (unread) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (unread) {
                Spacer(Modifier.size(6.dp))
                UnreadBadge(conversation.unreadCount)
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val extra = LocalAppExtraColors.current
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(extra.unreadBadge)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            color = extra.onUnreadBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
