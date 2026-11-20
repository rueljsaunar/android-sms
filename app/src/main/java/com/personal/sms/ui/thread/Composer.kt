package com.personal.sms.ui.thread

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun Composer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val canSend = text.isNotBlank()

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        // Attach (+) — MMS is out of scope for v1, so this is informational.
        CircleButton(
            background = MaterialTheme.colorScheme.surfaceVariant,
            onClick = {
                Toast.makeText(context, "Attachments aren't supported in v1 (SMS only)", Toast.LENGTH_SHORT).show()
            },
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        "Text message",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Rounded.Schedule,
                contentDescription = "Schedule send",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp).clickable(onClick = onSchedule),
            )
        }

        Spacer(Modifier.width(8.dp))

        CircleButton(
            size = 48.dp,
            background = if (canSend) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            onClick = { if (canSend) onSend() },
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun CircleButton(
    background: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(background).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}
