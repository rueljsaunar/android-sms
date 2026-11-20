package com.personal.sms

import android.app.Application
import com.personal.sms.di.AppContainer

/**
 * Application entry point. Holds the process-wide [AppContainer] (database,
 * repositories, sender) — wired manually, no DI framework, by design.
 */
class SmsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
