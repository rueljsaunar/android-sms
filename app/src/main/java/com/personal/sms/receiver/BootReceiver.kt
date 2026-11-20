package com.personal.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.personal.sms.data.local.AppDatabase
import com.personal.sms.scheduler.ScheduledSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Re-arms all pending scheduled sends after a reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                ScheduledSender(appContext, AppDatabase.get(appContext).scheduledMessageDao())
                    .rearmAll()
            } finally {
                pending.finish()
            }
        }
    }
}
