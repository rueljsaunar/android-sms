package com.personal.sms.ui.newmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.sms.data.SmsRepository
import com.personal.sms.di.AppContainer
import com.personal.sms.domain.model.ContactPhone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUGGESTED_COUNT = 4

data class NewMessageState(
    val query: String = "",
    val suggested: List<ContactPhone> = emptyList(),
    val grouped: List<Pair<String, List<ContactPhone>>> = emptyList(),
)

class NewMessageViewModel(repo: SmsRepository) : ViewModel() {

    private val allPhones = MutableStateFlow<List<ContactPhone>>(emptyList())
    private val query = MutableStateFlow("")

    val state: StateFlow<NewMessageState> =
        combine(allPhones, query) { phones, q ->
            val filtered = if (q.isBlank()) {
                phones
            } else {
                val digits = q.filter { it.isDigit() }
                phones.filter { phone ->
                    phone.displayName.contains(q, ignoreCase = true) ||
                        (digits.isNotEmpty() && phone.number.filter { it.isDigit() }.contains(digits))
                }
            }
            NewMessageState(
                query = q,
                suggested = if (q.isBlank()) phones.take(SUGGESTED_COUNT) else emptyList(),
                grouped = filtered
                    .groupBy { it.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
                    .toSortedMap()
                    .map { (letter, list) -> letter to list },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NewMessageState())

    init {
        viewModelScope.launch { allPhones.value = repo.allContactPhones() }
    }

    fun setQuery(value: String) { query.value = value }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { NewMessageViewModel(container.smsRepository) }
        }
    }
}
