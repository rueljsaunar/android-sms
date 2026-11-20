package com.personal.sms.ui

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.personal.sms.data.DefaultSmsAppManager
import com.personal.sms.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Tracks whether this app is the default SMS app (gates onboarding). */
class MainViewModel(private val defaultSmsAppManager: DefaultSmsAppManager) : ViewModel() {

    private val _isDefault = MutableStateFlow(defaultSmsAppManager.isDefault())
    val isDefault: StateFlow<Boolean> = _isDefault.asStateFlow()

    fun refresh() {
        _isDefault.value = defaultSmsAppManager.isDefault()
    }

    fun requestDefaultIntent(): Intent = defaultSmsAppManager.requestDefaultIntent()

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { MainViewModel(container.defaultSmsAppManager) }
        }
    }
}
