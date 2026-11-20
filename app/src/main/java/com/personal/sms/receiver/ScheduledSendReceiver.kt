package com.personal.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.personal.sms.data.local.AppDatabase
import com.personal.sms.data.sms.SmsSender
import com.personal.sms.scheduler.EXTRA_SCHEDULED_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Fires at a scheduled time: sends the stored message, then removes the row. */
class ScheduledSendReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_SCHEDULED_ID, -1L)
        if (id < 0) return

        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(appContext).scheduledMessageDao()
                val entity = dao.byId(id)
                if (entity != null) {
                    SmsSender(appContext).send(entity.address, entity.body, entity.threadId)
                    dao.delete(id)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
