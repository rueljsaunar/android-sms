package com.personal.sms.ui.newmessage

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.formatNumber
import com.personal.sms.domain.initials
import com.personal.sms.domain.model.ContactPhone
import com.personal.sms.ui.components.ContactListItem
import com.personal.sms.ui.components.SectionHeader
import com.personal.sms.ui.theme.AppTheme

@Composable
fun NewMessageScreen(
    viewModel: NewMessageViewModel,
    onBack: () -> Unit,
    onStartThread: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    NewMessageContent(
        state = state,
        onQueryChange = viewModel::setQuery,
        onStartThread = onStartThread,
        onBack = onBack,
    )
}

@Composable
fun NewMessageContent(
    state: NewMessageState,
    onQueryChange: (String) -> Unit,
    onStartThread: (String) -> Unit,
    onBack: () -> Unit,
) {
    val looksLikeNumber = state.query.isNotBlank() &&
        state.query.any { it.isDigit() } && state.query.none { it.isLetter() }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "New message",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "To:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (state.query.isEmpty()) {
                        Text(
                            "Name or number",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    BasicTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        contentDescription = "Contacts",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (looksLikeNumber) {
                    item {
                        ContactListItem(
                            name = "Send to ${formatNumber(state.query.trim())}",
                            subtitle = null,
                            avatarLabel = "#",
                            avatarKey = state.query,
                            onClick = { onStartThread(state.query.trim()) },
                        )
                    }
                }
                if (state.suggested.isNotEmpty()) {
                    item { SectionHeader("Suggested") }
                    items(state.suggested) { phone -> PhoneRow(phone, onStartThread) }
                }
                state.grouped.forEach { (letter, phones) ->
                    item(key = "header_$letter") { SectionHeader(letter) }
                    items(phones) { phone -> PhoneRow(phone, onStartThread) }
                }
            }
        }
    }
}

@Composable
private fun PhoneRow(phone: ContactPhone, onStartThread: (String) -> Unit) {
    ContactListItem(
        name = phone.displayName.ifBlank { phone.number },
        subtitle = "${phone.typeLabel} · ${formatNumber(phone.number)}",
        avatarLabel = initials(phone.displayName.ifBlank { phone.number }),
        avatarKey = phone.number,
        onClick = { onStartThread(phone.number) },
    )
}

@Preview(showBackground = true)
@Composable
private fun NewMessagePreview() {
    AppTheme {
        NewMessageContent(
            state = NewMessageState(
                query = "",
                suggested = listOf(
                    ContactPhone(1, "Maya Rios", "+14155550198", "Mobile", null),
                    ContactPhone(2, "Alex Brenner", "+16285550143", "Mobile", null),
                ),
                grouped = listOf(
                    "A" to listOf(
                        ContactPhone(3, "Amara Okafor", "+19175550176", "Mobile", null),
                        ContactPhone(4, "Anita Trevino", "+13125550129", "Work", null),
                    ),
                    "D" to listOf(ContactPhone(5, "Dad", "+14155550110", "Mobile", null)),
                ),
            ),
            onQueryChange = {}, onStartThread = {}, onBack = {},
        )
    }
}
