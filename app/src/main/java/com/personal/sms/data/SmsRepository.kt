package com.personal.sms.data

import com.personal.sms.data.contacts.ContactsRepository
import com.personal.sms.data.local.dao.ArchiveDao
import com.personal.sms.data.local.dao.DraftDao
import com.personal.sms.data.local.dao.ScheduledMessageDao
import com.personal.sms.data.local.entity.DraftEntity
import com.personal.sms.data.sms.RawConversation
import com.personal.sms.data.sms.SmsProvider
import com.personal.sms.domain.model.Contact
import com.personal.sms.domain.model.ContactPhone
import com.personal.sms.domain.model.Conversation
import com.personal.sms.domain.model.Message
import com.personal.sms.domain.model.SearchHit
import com.personal.sms.domain.normalizeNumber
import com.personal.sms.data.local.entity.ScheduledMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val SEARCH_LIMIT = 50

/**
 * Single source of truth for the UI: the system SMS provider merged with the
 * app-owned Room state (scheduled sends, drafts, archive flags) and resolved
 * contacts.
 */
class SmsRepository(
    private val provider: SmsProvider,
    private val contacts: ContactsRepository,
    private val scheduledDao: ScheduledMessageDao,
    private val draftDao: DraftDao,
    private val archiveDao: ArchiveDao,
) {

    fun conversations(includeArchived: Boolean = false): Flow<List<Conversation>> =
        combine(
            provider.observeConversations(),
            scheduledDao.pending(),
            archiveDao.archivedThreadIds(),
        ) { raws, scheduled, archivedIds ->
            mapConversations(raws, scheduled, archivedIds.toSet(), includeArchived)
        }.flowOn(Dispatchers.IO)

    fun messages(threadId: Long): Flow<List<Message>> = provider.observeMessages(threadId)

    fun draft(threadId: Long): Flow<String> =
        draftDao.draftFor(threadId).map { it?.body.orEmpty() }

    suspend fun saveDraft(threadId: Long, body: String) {
        if (body.isBlank()) draftDao.clear(threadId) else draftDao.upsert(DraftEntity(threadId, body))
    }

    suspend fun clearDraft(threadId: Long) = draftDao.clear(threadId)

    suspend fun setArchived(threadId: Long, archived: Boolean) =
        archiveDao.setArchived(threadId, archived)

    suspend fun deleteThread(threadId: Long) = withContext(Dispatchers.IO) {
        provider.deleteThread(threadId)
        archiveDao.setArchived(threadId, false)
    }

    suspend fun deleteMessage(messageId: Long) = withContext(Dispatchers.IO) {
        provider.deleteMessage(messageId)
    }

    /**
     * Cancel scheduled sends to an address (used to dismiss a scheduled-only thread).
     * Any already-armed alarm fires harmlessly later: the row is gone, so no send.
     */
    suspend fun deleteSchedulesForAddress(address: String) = withContext(Dispatchers.IO) {
        scheduledDao.deleteByAddress(address)
    }

    suspend fun markThreadRead(threadId: Long) = withContext(Dispatchers.IO) {
        provider.markThreadRead(threadId)
    }

    fun contactFor(address: String): Contact? = contacts.resolve(address)

    suspend fun allContactPhones(): List<ContactPhone> =
        withContext(Dispatchers.IO) { contacts.allPhones() }

    suspend fun searchContacts(query: String): List<ContactPhone> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val q = query.trim()
        val digits = q.filter { it.isDigit() }
        contacts.allPhones().filter { phone ->
            phone.displayName.contains(q, ignoreCase = true) ||
                (digits.isNotEmpty() && phone.number.filter { it.isDigit() }.contains(digits))
        }.distinctBy { it.contactId ?: it.number }
    }

    suspend fun searchMessages(query: String): List<SearchHit> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        provider.searchMessages(query).take(SEARCH_LIMIT).map { msg ->
            SearchHit(
                threadId = msg.threadId,
                address = msg.address,
                contact = contacts.resolve(msg.address),
                body = msg.body,
                date = msg.date,
            )
        }
    }

    private fun mapConversations(
        raws: List<RawConversation>,
        scheduled: List<ScheduledMessageEntity>,
        archivedIds: Set<Long>,
        includeArchived: Boolean,
    ): List<Conversation> {
        val scheduledByAddress = scheduled.groupBy { normalizeNumber(it.address) }
        val handledAddresses = HashSet<String>()
        val result = ArrayList<Conversation>()

        for (raw in raws) {
            val archived = raw.threadId in archivedIds
            if (archived && !includeArchived) continue
            val norm = normalizeNumber(raw.address)
            handledAddresses.add(norm)
            val sched = scheduledByAddress[norm]
            val snippet = (if (raw.lastIsSent) "You: " else "") + raw.snippet
            result.add(
                Conversation(
                    threadId = raw.threadId,
                    address = raw.address,
                    contact = contacts.resolve(raw.address),
                    snippet = snippet,
                    date = raw.date,
                    unreadCount = raw.unreadCount,
                    isScheduled = sched != null,
                    scheduledAt = sched?.minByOrNull { it.scheduledAt }?.scheduledAt,
                    archived = archived,
                ),
            )
        }

        // Scheduled sends to a brand-new recipient appear as their own rows
        // (negative, synthetic threadId) until the first real message exists.
        for ((norm, list) in scheduledByAddress) {
            if (norm in handledAddresses) continue
            val soonest = list.minByOrNull { it.scheduledAt } ?: continue
            result.add(
                Conversation(
                    threadId = -soonest.id,
                    address = soonest.address,
                    contact = contacts.resolve(soonest.address),
                    snippet = soonest.body,
                    date = soonest.scheduledAt,
                    unreadCount = 0,
                    isScheduled = true,
                    scheduledAt = soonest.scheduledAt,
                    archived = false,
                ),
            )
        }

        return result.sortedByDescending { it.date }
    }
}
