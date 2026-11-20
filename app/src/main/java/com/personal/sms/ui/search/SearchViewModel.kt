package com.personal.sms.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.sms.data.SmsRepository
import com.personal.sms.di.AppContainer
import com.personal.sms.domain.model.ContactPhone
import com.personal.sms.domain.model.SearchHit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

data class SearchResults(
    val contacts: List<ContactPhone> = emptyList(),
    val messages: List<SearchHit> = emptyList(),
)

class SearchViewModel(private val repo: SmsRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results: StateFlow<SearchResults> =
        _query.debounce(200).mapLatest { q ->
            if (q.isBlank()) {
                SearchResults()
            } else {
                SearchResults(
                    contacts = repo.searchContacts(q),
                    messages = repo.searchMessages(q),
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults())

    fun setQuery(value: String) { _query.value = value }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SearchViewModel(container.smsRepository) }
        }
    }
}
