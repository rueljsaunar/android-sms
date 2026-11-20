package com.personal.sms.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.sms.data.SmsRepository
import com.personal.sms.di.AppContainer
import com.personal.sms.domain.model.Conversation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationsViewModel(private val repo: SmsRepository) : ViewModel() {

    val conversations: StateFlow<List<Conversation>> =
        repo.conversations(includeArchived = false)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val archived: StateFlow<List<Conversation>> =
        repo.conversations(includeArchived = true)
            .map { list -> list.filter { it.archived } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun archive(threadId: Long) = viewModelScope.launch { repo.setArchived(threadId, true) }
    fun unarchive(threadId: Long) = viewModelScope.launch { repo.setArchived(threadId, false) }
    fun delete(threadId: Long) = viewModelScope.launch { repo.deleteThread(threadId) }
    fun markRead(threadId: Long) = viewModelScope.launch { repo.markThreadRead(threadId) }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ConversationsViewModel(container.smsRepository) }
        }
    }
}
