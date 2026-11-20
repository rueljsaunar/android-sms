package com.personal.sms.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.personal.sms.data.ExactAlarmPermission
import com.personal.sms.data.local.dao.ScheduledMessageDao
import com.personal.sms.data.local.entity.ScheduledMessageEntity
import com.personal.sms.data.sms.SmsProvider
import com.personal.sms.receiver.ScheduledSendReceiver

const val EXTRA_SCHEDULED_ID = "com.personal.sms.SCHEDULED_ID"

/**
 * Schedules messages to send later with an exact alarm (setExactAndAllowWhileIdle),
 * persisting them to Room so they survive process death and can be re-armed on boot.
 * Falls back to an inexact alarm if the exact-alarm permission isn't granted.
 */
class ScheduledSender(
    private val context: Context,
    private val scheduledDao: ScheduledMessageDao,
) {
    private val alarmManager get() = context.getSystemService(AlarmManager::class.java)

    suspend fun schedule(address: String, body: String, atEpochMs: Long): Long {
        val threadId = SmsProvider(context).threadIdFor(address)
        val id = scheduledDao.insert(
            ScheduledMessageEntity(
                address = address,
                body = body,
                scheduledAt = atEpochMs,
                threadId = threadId,
            ),
        )
        arm(id, atEpochMs)
        return id
    }

    suspend fun cancel(id: Long) {
        scheduledDao.delete(id)
        alarmManager.cancel(pendingIntent(id))
    }

    /** Re-arm all pending schedules (called on boot). */
    suspend fun rearmAll() {
        scheduledDao.pendingOnce().forEach { arm(it.id, it.scheduledAt) }
    }

    private fun arm(id: Long, atEpochMs: Long) {
        val pi = pendingIntent(id)
        if (ExactAlarmPermission.canScheduleExact(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atEpochMs, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atEpochMs, pi)
        }
    }

    private fun pendingIntent(id: Long): PendingIntent {
        val intent = Intent(context, ScheduledSendReceiver::class.java).apply {
            putExtra(EXTRA_SCHEDULED_ID, id)
        }
        return PendingIntent.getBroadcast(
            context,
            id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
