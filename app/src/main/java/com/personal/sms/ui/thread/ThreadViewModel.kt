package com.personal.sms.ui.thread

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.sms.data.SmsRepository
import com.personal.sms.data.sms.SmsSender
import com.personal.sms.di.AppContainer
import com.personal.sms.domain.model.Contact
import com.personal.sms.domain.model.Message
import com.personal.sms.scheduler.ScheduledSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Header info for the thread screen. */
data class ThreadHeader(val address: String, val contact: Contact?)

/**
 * @param threadId system thread id, or a negative synthetic id / 0 for a brand-new thread.
 * @param initialAddress the recipient when starting a new thread (null for an existing one).
 *
 * When [threadId] is not a real system id (<= 0), the real id is resolved from
 * [initialAddress] so the thread shows history and live updates (e.g. opened from a
 * notification tap, the sendto intent, or a new-message/contact pick).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ThreadViewModel(
    private val repo: SmsRepository,
    private val sender: SmsSender,
    private val scheduledSender: ScheduledSender,
    initialThreadId: Long,
    private val initialAddress: String?,
) : ViewModel() {

    private val threadIdFlow = MutableStateFlow(if (initialThreadId > 0) initialThreadId else 0L)

    init {
        if (initialThreadId <= 0 && !initialAddress.isNullOrBlank()) {
            viewModelScope.launch { threadIdFlow.value = repo.resolveThreadId(initialAddress) }
        }
    }

    val messages: StateFlow<List<Message>> =
        threadIdFlow.flatMapLatest { id ->
            if (id > 0) repo.messages(id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val header: StateFlow<ThreadHeader> =
        combine(messages, threadIdFlow) { msgs, _ ->
            initialAddress?.takeIf { it.isNotBlank() } ?: msgs.firstOrNull()?.address.orEmpty()
        }.distinctUntilChanged()
            .map { address -> ThreadHeader(address, repo.contactFor(address)) }
            .flowOn(Dispatchers.IO)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                ThreadHeader(initialAddress.orEmpty(), null),
            )

    val draft: StateFlow<String> =
        threadIdFlow.flatMapLatest { id ->
            if (id > 0) repo.draft(id) else flowOf("")
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    fun markRead() {
        val id = threadIdFlow.value
        if (id > 0) viewModelScope.launch { repo.markThreadRead(id) }
    }

    fun send(text: String) {
        val address = currentAddress()
        if (text.isBlank() || address.isEmpty()) return
        val id = threadIdFlow.value
        sender.send(address, text.trim(), id.takeIf { it > 0 })
        if (id > 0) viewModelScope.launch { repo.clearDraft(id) }
    }

    fun schedule(text: String, atEpochMs: Long) {
        val address = currentAddress()
        if (text.isBlank() || address.isEmpty()) return
        val id = threadIdFlow.value
        viewModelScope.launch {
            scheduledSender.schedule(address, text.trim(), atEpochMs)
            if (id > 0) repo.clearDraft(id)
        }
    }

    fun saveDraft(text: String) {
        val id = threadIdFlow.value
        if (id > 0) viewModelScope.launch { repo.saveDraft(id, text) }
    }

    fun deleteMessage(messageId: Long) = viewModelScope.launch { repo.deleteMessage(messageId) }

    fun deleteThread() {
        val id = threadIdFlow.value
        if (id > 0) viewModelScope.launch { repo.deleteThread(id) }
    }

    private fun currentAddress(): String =
        initialAddress?.takeIf { it.isNotBlank() } ?: messages.value.firstOrNull()?.address.orEmpty()

    companion object {
        fun factory(container: AppContainer, threadId: Long, address: String?) = viewModelFactory {
            initializer {
                ThreadViewModel(
                    repo = container.smsRepository,
                    sender = container.smsSender,
                    scheduledSender = container.scheduledSender,
                    initialThreadId = threadId,
                    initialAddress = address,
                )
            }
        }
    }
}
