package com.personal.sms

import android.app.Application

/**
 * Application entry point. Holds process-wide singletons (database, repositories)
 * that are wired manually in [AppContainer] — no DI framework, by design.
 */
class SmsApp : Application()
