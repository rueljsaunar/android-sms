package com.personal.sms.ui.conversations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.personal.sms.domain.model.Conversation
import com.personal.sms.ui.components.ConversationRow
import com.personal.sms.ui.components.SearchFieldButton
import com.personal.sms.ui.theme.AppTheme

@Composable
fun ConversationsScreen(
    viewModel: ConversationsViewModel,
    onOpenThread: (threadId: Long, address: String) -> Unit,
    onCompose: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenArchived: () -> Unit,
) {
    val conversations by viewModel.conversations.collectAsState()
    ConversationsContent(
        conversations = conversations,
        onOpenThread = onOpenThread,
        onCompose = onCompose,
        onOpenSearch = onOpenSearch,
        onOpenOnboarding = onOpenOnboarding,
        onOpenArchived = onOpenArchived,
        onArchive = viewModel::archive,
        onDelete = { conv ->
            if (conv.threadId < 0) viewModel.deleteScheduledOnly(conv.address)
            else viewModel.delete(conv.threadId)
        },
    )
}

@Composable
fun ConversationsContent(
    conversations: List<Conversation>,
    onOpenThread: (Long, String) -> Unit,
    onCompose: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onOpenArchived: () -> Unit,
    onArchive: (Long) -> Unit,
    onDelete: (Conversation) -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var pendingDelete by remember { mutableStateOf<Conversation?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCompose,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                text = { Text("Compose", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            HeaderBar(onOpenOnboarding = onOpenOnboarding, onOpenArchived = onOpenArchived)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                SearchFieldButton(onClick = onOpenSearch)
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            ) {
                items(conversations, key = { it.threadId }) { conversation ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    onArchive(conversation.threadId); conversation.threadId > 0
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    pendingDelete = conversation; false
                                }
                                else -> false
                            }
                        },
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = conversation.threadId > 0,
                        backgroundContent = { SwipeBackground(dismissState.targetValue) },
                    ) {
                        ConversationRow(
                            conversation = conversation,
                            onClick = { onOpenThread(conversation.threadId, conversation.address) },
                            onCopyOtp = { code ->
                                clipboard.setText(AnnotatedString(code))
                                Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { conv ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete conversation?") },
            text = { Text("This permanently deletes the conversation with ${conv.displayName()}.") },
            confirmButton = {
                TextButton(onClick = { onDelete(conv); pendingDelete = null }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun HeaderBar(onOpenOnboarding: () -> Unit, onOpenArchived: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { menuOpen = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Archived") },
                    leadingIcon = { Icon(Icons.Rounded.Archive, null) },
                    onClick = { menuOpen = false; onOpenArchived() },
                )
                DropdownMenuItem(
                    text = { Text("Default SMS app") },
                    leadingIcon = { Icon(Icons.Rounded.Settings, null) },
                    onClick = { menuOpen = false; onOpenOnboarding() },
                )
            }
        }
    }
}

@Composable
private fun SwipeBackground(target: SwipeToDismissBoxValue) {
    val (color, icon, alignment) = when (target) {
        SwipeToDismissBoxValue.StartToEnd -> Triple(Color(0xFF22A06B), Icons.Rounded.Archive, Alignment.CenterStart)
        SwipeToDismissBoxValue.EndToStart -> Triple(Color(0xFFD14D6E), Icons.Rounded.Delete, Alignment.CenterEnd)
        else -> Triple(Color.Transparent, Icons.Rounded.Archive, Alignment.Center)
    }
    Box(
        modifier = Modifier.fillMaxSize().background(color).padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        if (target != SwipeToDismissBoxValue.Settled) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConversationsPreview() {
    AppTheme {
        ConversationsContent(
            conversations = sampleConversations(),
            onOpenThread = { _, _ -> }, onCompose = {}, onOpenSearch = {},
            onOpenOnboarding = {}, onOpenArchived = {}, onArchive = {}, onDelete = {},
        )
    }
}

internal fun sampleConversations(): List<Conversation> {
    val now = System.currentTimeMillis()
    return listOf(
        Conversation(1, "+14155550198", null, "Perfect, see you then 🎉", now, 2).withName("Maya Rios"),
        Conversation(2, "+14155550110", null, "You: Landed safe, calling tonight", now - 3_600_000, 0).withName("Dad"),
        Conversation(3, "VERIFY", null, "Code 418 203", now - 7_200_000, 0).withName("Verify"),
        Conversation(4, "+15035550188", null, "Can you send the address?", now - 90_000_000, 0).withName("Sam Lee"),
        Conversation(5, "+14155550133", null, "", now - 180_000_000, 0, isScheduled = true, scheduledAt = now + 3_600_000).withName("Jordan T."),
        Conversation(6, "+14155550162", null, "Your package is out for delivery", now - 200_000_000, 0),
    )
}

private fun Conversation.withName(name: String): Conversation =
    copy(contact = com.personal.sms.domain.model.Contact(null, name, null, listOf(address)))
