package com.personal.sms.di

import android.content.Context
import com.personal.sms.data.DefaultSmsAppManager
import com.personal.sms.data.SmsRepository
import com.personal.sms.data.contacts.ContactsRepository
import com.personal.sms.data.local.AppDatabase
import com.personal.sms.data.sms.SmsProvider
import com.personal.sms.data.sms.SmsSender
import com.personal.sms.scheduler.ScheduledSender

/**
 * Manual dependency container (no DI framework, by design). Holds the process-wide
 * singletons and is exposed by [com.personal.sms.SmsApp].
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database by lazy { AppDatabase.get(appContext) }
    val smsProvider by lazy { SmsProvider(appContext) }
    val contactsRepository by lazy { ContactsRepository(appContext) }
    val smsSender by lazy { SmsSender(appContext) }
    val scheduledSender by lazy { ScheduledSender(appContext, database.scheduledMessageDao()) }
    val defaultSmsAppManager by lazy { DefaultSmsAppManager(appContext) }

    val smsRepository by lazy {
        SmsRepository(
            provider = smsProvider,
            contacts = contactsRepository,
            scheduledDao = database.scheduledMessageDao(),
            draftDao = database.draftDao(),
            archiveDao = database.archiveDao(),
        )
    }
}
