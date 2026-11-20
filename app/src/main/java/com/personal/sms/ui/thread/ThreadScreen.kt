package com.personal.sms.ui.thread

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.formatNumber
import com.personal.sms.domain.initials
import com.personal.sms.domain.model.Message
import com.personal.sms.domain.model.MessageStatus
import com.personal.sms.domain.model.MessageType
import com.personal.sms.domain.numberLabel
import com.personal.sms.ui.components.Avatar
import com.personal.sms.ui.theme.AppTheme

private sealed interface ThreadItem {
    data class Header(val label: String) : ThreadItem
    data class Msg(val message: Message, val showStatus: Boolean) : ThreadItem
}

@Composable
fun ThreadScreen(viewModel: ThreadViewModel, onBack: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    val header by viewModel.header.collectAsState()
    val draft by viewModel.draft.collectAsState()

    var input by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(draft) { if (input.isEmpty() && draft.isNotEmpty()) input = draft }
    LaunchedEffect(Unit) { viewModel.markRead() }

    ThreadContent(
        title = header.contact?.displayName ?: formatNumber(header.address),
        subtitle = header.contact?.let { formatNumber(header.address) },
        avatarLabel = header.contact?.displayName?.let { initials(it) } ?: numberLabel(header.address),
        avatarKey = header.address,
        address = header.address,
        messages = messages,
        input = input,
        onInputChange = { input = it; viewModel.saveDraft(it) },
        onSend = { viewModel.send(input); input = "" },
        onSchedule = { atMillis -> viewModel.schedule(input, atMillis); input = "" },
        onDeleteMessage = viewModel::deleteMessage,
        onDeleteThread = { viewModel.deleteThread(); onBack() },
        onBack = onBack,
    )
}

@Composable
fun ThreadContent(
    title: String,
    subtitle: String?,
    avatarLabel: String,
    avatarKey: String,
    address: String,
    messages: List<Message>,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onSchedule: (Long) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onDeleteThread: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()
    var showSchedule by remember { mutableStateOf(false) }
    var pendingDeleteMessage by remember { mutableStateOf<Message?>(null) }
    var showDeleteThread by remember { mutableStateOf(false) }

    val items = remember(messages) { buildThreadItems(messages) }
    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) listState.animateScrollToItem(items.size - 1)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ThreadHeaderBar(
                title = title,
                subtitle = subtitle,
                avatarLabel = avatarLabel,
                avatarKey = avatarKey,
                onBack = onBack,
                onCall = {
                    if (address.isNotEmpty()) {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$address")))
                    }
                },
                onDeleteConversation = { showDeleteThread = true },
            )
        },
        bottomBar = {
            Composer(
                text = input,
                onTextChange = onInputChange,
                onSend = onSend,
                onSchedule = { showSchedule = true },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            items(items.size) { index ->
                when (val item = items[index]) {
                    is ThreadItem.Header -> DateDivider(item.label)
                    is ThreadItem.Msg -> MessageBubble(
                        message = item.message,
                        onLongPress = { pendingDeleteMessage = item.message },
                        onCopyOtp = { code ->
                            clipboard.setText(AnnotatedString(code))
                            Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                        },
                        statusSlot = if (item.showStatus) {
                            { DeliveryStatus(item.message.status, item.message.date) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }

    if (showSchedule) {
        ScheduleDialog(
            onDismiss = { showSchedule = false },
            onConfirm = { atMillis ->
                onSchedule(atMillis)
                showSchedule = false
                Toast.makeText(context, "Message scheduled", Toast.LENGTH_SHORT).show()
            },
        )
    }

    if (showDeleteThread) {
        AlertDialog(
            onDismissRequest = { showDeleteThread = false },
            title = { Text("Delete conversation?") },
            text = { Text("This permanently deletes the whole conversation.") },
            confirmButton = {
                TextButton(onClick = { showDeleteThread = false; onDeleteThread() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteThread = false }) { Text("Cancel") }
            },
        )
    }

    pendingDeleteMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { pendingDeleteMessage = null },
            title = { Text("Delete message?") },
            text = { Text("This permanently deletes this message.") },
            confirmButton = {
                TextButton(onClick = { onDeleteMessage(message.id); pendingDeleteMessage = null }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteMessage = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ThreadHeaderBar(
    title: String,
    subtitle: String?,
    avatarLabel: String,
    avatarKey: String,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onDeleteConversation: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Avatar(label = avatarLabel, colorKey = avatarKey, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onCall) {
                Icon(Icons.Rounded.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Delete conversation") },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null) },
                        onClick = { menuOpen = false; onDeleteConversation() },
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

private fun buildThreadItems(messages: List<Message>): List<ThreadItem> {
    val items = ArrayList<ThreadItem>()
    var lastDay = ""
    val lastSentId = messages.lastOrNull { it.type == MessageType.SENT }?.id
    for (message in messages) {
        val label = dayLabel(message.date)
        if (label != lastDay) {
            items.add(ThreadItem.Header(label))
            lastDay = label
        }
        items.add(ThreadItem.Msg(message, showStatus = message.id == lastSentId))
    }
    return items
}

@Preview(showBackground = true)
@Composable
private fun ThreadPreview() {
    AppTheme {
        ThreadContent(
            title = "Maya Rios",
            subtitle = "+1 (415) 555-0198",
            avatarLabel = "MR",
            avatarKey = "+14155550198",
            address = "+14155550198",
            messages = sampleMessages(),
            input = "",
            onInputChange = {}, onSend = {}, onSchedule = {}, onDeleteMessage = {},
            onDeleteThread = {}, onBack = {},
        )
    }
}

private fun sampleMessages(): List<Message> {
    val now = System.currentTimeMillis()
    return listOf(
        Message(1, 1, "+1", "Hey! Are we still on for coffee at 10?", now - 500_000, MessageType.INBOX),
        Message(2, 1, "+1", "Yes! Running 5 min late but I'll be there", now - 400_000, MessageType.SENT, MessageStatus.DELIVERED),
        Message(3, 1, "+1", "No worries, I grabbed us a table by the window ☕", now - 300_000, MessageType.INBOX),
        Message(4, 1, "+1", "Perfect, see you then 🎉", now - 200_000, MessageType.SENT, MessageStatus.DELIVERED),
        Message(5, 1, "+1", "Awesome, heading out now", now - 100_000, MessageType.INBOX),
    )
}
