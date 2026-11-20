package com.personal.sms.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.personal.sms.ui.components.ConversationRow

/** Archived conversations; swipe a row to un-archive it. */
@Composable
fun ArchivedScreen(
    viewModel: ConversationsViewModel,
    onBack: () -> Unit,
    onOpenThread: (Long, String) -> Unit,
) {
    val archived by viewModel.archived.collectAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text("Archived", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        },
    ) { padding ->
        if (archived.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No archived conversations", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(archived, key = { it.threadId }) { conversation ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.unarchive(conversation.threadId); true
                            } else {
                                false
                            }
                        },
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromEndToStart = false,
                        backgroundContent = {
                            Box(
                                Modifier.fillMaxSize().background(Color(0xFF6E5FE0)).padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) { Icon(Icons.Rounded.Unarchive, contentDescription = null, tint = Color.White) }
                        },
                    ) {
                        ConversationRow(
                            conversation = conversation,
                            onClick = { onOpenThread(conversation.threadId, conversation.address) },
                            onCopyOtp = {},
                        )
                    }
                }
            }
        }
    }
}
