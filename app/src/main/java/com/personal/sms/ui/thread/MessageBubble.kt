package com.personal.sms.ui.thread

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.detectOtp
import com.personal.sms.domain.model.Message
import com.personal.sms.domain.model.MessageType
import com.personal.sms.ui.components.OtpCopyChip
import com.personal.sms.ui.theme.BubbleShapeReceived
import com.personal.sms.ui.theme.BubbleShapeSent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    onLongPress: () -> Unit,
    onCopyOtp: (String) -> Unit,
    statusSlot: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val sent = message.type == MessageType.SENT
    val otp = if (!sent) detectOtp(message.body) else null

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
        horizontalAlignment = if (sent) Alignment.End else Alignment.Start,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (sent) Arrangement.End else Arrangement.Start) {
            Column(horizontalAlignment = if (sent) Alignment.End else Alignment.Start) {
                Text(
                    text = message.body,
                    color = if (sent) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .clip(if (sent) BubbleShapeSent else BubbleShapeReceived)
                        .background(
                            if (sent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .combinedClickable(onClick = {}, onLongClick = onLongPress)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
                if (otp != null) {
                    OtpCopyChip(
                        onCopy = { onCopyOtp(otp.code) },
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        statusSlot?.invoke()
    }
}
