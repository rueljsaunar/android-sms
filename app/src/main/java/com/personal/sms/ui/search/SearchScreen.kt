package com.personal.sms.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.personal.sms.domain.formatNumber
import com.personal.sms.domain.initials
import com.personal.sms.domain.matchQuery
import com.personal.sms.domain.model.Contact
import com.personal.sms.domain.model.ContactPhone
import com.personal.sms.domain.model.SearchHit
import com.personal.sms.domain.relativeTime
import com.personal.sms.ui.components.Avatar
import com.personal.sms.ui.components.ContactListItem
import com.personal.sms.ui.components.HighlightedText
import com.personal.sms.ui.components.SectionHeader
import com.personal.sms.ui.theme.AppTheme

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onOpenThread: (threadId: Long, address: String) -> Unit,
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    SearchContent(
        query = query,
        results = results,
        onQueryChange = viewModel::setQuery,
        onBack = onBack,
        onOpenThread = onOpenThread,
    )
}

@Composable
fun SearchContent(
    query: String,
    results: SearchResults,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenThread: (Long, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 12.dp),
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onClear = { if (query.isEmpty()) onBack() else onQueryChange("") },
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (results.contacts.isNotEmpty()) {
                item { SectionHeader("Contacts") }
                items(results.contacts) { phone ->
                    ContactListItem(
                        name = phone.displayName.ifBlank { phone.number },
                        subtitle = formatNumber(phone.number),
                        avatarLabel = initials(phone.displayName.ifBlank { phone.number }),
                        avatarKey = phone.number,
                        onClick = { onOpenThread(0L, phone.number) },
                    )
                }
            }
            if (results.messages.isNotEmpty()) {
                item { SectionHeader("Messages") }
                items(results.messages) { hit ->
                    MessageHitRow(hit, query, onClick = { onOpenThread(hit.threadId, hit.address) })
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        "Search",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                BasicTextField(
                    value = query,
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onClear),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageHitRow(hit: SearchHit, query: String, onClick: () -> Unit) {
    val name = hit.contact?.displayName ?: formatNumber(hit.address)
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Avatar(label = hit.contact?.displayName?.let { initials(it) } ?: "#", colorKey = hit.address, size = 48.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    relativeTime(hit.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(2.dp))
            HighlightedText(
                text = hit.body,
                ranges = matchQuery(hit.body, query),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchPreview() {
    val now = System.currentTimeMillis()
    fun contact(name: String) = Contact(null, name, null, emptyList())
    AppTheme {
        SearchContent(
            query = "address",
            results = SearchResults(
                contacts = listOf(ContactPhone(1, "Sam Lee", "+15035550188", "Mobile", null)),
                messages = listOf(
                    SearchHit(4, "+15035550188", contact("Sam Lee"), "Can you send the address? I'll meet you there", now - 90_000_000),
                    SearchHit(1, "+14155550198", contact("Maya Rios"), "The address is 240 Elm St, 3rd floor", now - 150_000_000),
                    SearchHit(6, "+14155550162", null, "Delivery to the address on file is complete", now - 200_000_000),
                ),
            ),
            onQueryChange = {}, onBack = {}, onOpenThread = { _, _ -> },
        )
    }
}
