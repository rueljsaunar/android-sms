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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Header info for the thread screen. */
data class ThreadHeader(val address: String, val contact: Contact?)

/**
 * @param threadId system thread id, or a negative synthetic id / 0 for a brand-new thread.
 * @param initialAddress the recipient when starting a new thread (null for an existing one).
 */
class ThreadViewModel(
    private val repo: SmsRepository,
    private val sender: SmsSender,
    private val scheduledSender: ScheduledSender,
    private val threadId: Long,
    initialAddress: String?,
) : ViewModel() {

    private val addressState = MutableStateFlow(initialAddress.orEmpty())

    val messages: StateFlow<List<Message>> =
        if (threadId > 0) {
            repo.messages(threadId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        } else {
            MutableStateFlow(emptyList<Message>()).asStateFlow()
        }

    val header: StateFlow<ThreadHeader> =
        messages.map { msgs ->
            val address = addressState.value.ifEmpty { msgs.firstOrNull()?.address.orEmpty() }
            if (address.isNotEmpty() && addressState.value.isEmpty()) addressState.value = address
            ThreadHeader(address, repo.contactFor(address))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ThreadHeader(initialAddress.orEmpty(), null),
        )

    val draft: StateFlow<String> =
        if (threadId > 0) {
            repo.draft(threadId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")
        } else {
            MutableStateFlow("").asStateFlow()
        }

    fun markRead() {
        if (threadId > 0) viewModelScope.launch { repo.markThreadRead(threadId) }
    }

    fun send(text: String) {
        val address = currentAddress()
        if (text.isBlank() || address.isEmpty()) return
        sender.send(address, text.trim(), threadId.takeIf { it > 0 })
        if (threadId > 0) viewModelScope.launch { repo.clearDraft(threadId) }
    }

    fun schedule(text: String, atEpochMs: Long) {
        val address = currentAddress()
        if (text.isBlank() || address.isEmpty()) return
        viewModelScope.launch {
            scheduledSender.schedule(address, text.trim(), atEpochMs)
            if (threadId > 0) repo.clearDraft(threadId)
        }
    }

    fun saveDraft(text: String) {
        if (threadId > 0) viewModelScope.launch { repo.saveDraft(threadId, text) }
    }

    fun deleteMessage(messageId: Long) = viewModelScope.launch { repo.deleteMessage(messageId) }

    fun deleteThread() = viewModelScope.launch { if (threadId > 0) repo.deleteThread(threadId) }

    private fun currentAddress(): String =
        addressState.value.ifEmpty { messages.value.firstOrNull()?.address.orEmpty() }

    companion object {
        fun factory(container: AppContainer, threadId: Long, address: String?) = viewModelFactory {
            initializer {
                ThreadViewModel(
                    repo = container.smsRepository,
                    sender = container.smsSender,
                    scheduledSender = container.scheduledSender,
                    threadId = threadId,
                    initialAddress = address,
                )
            }
        }
    }
}
