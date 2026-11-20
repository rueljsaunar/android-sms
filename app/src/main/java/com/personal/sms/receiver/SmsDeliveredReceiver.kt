package com.personal.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.personal.sms.data.sms.EXTRA_MESSAGE_ID
import com.personal.sms.data.sms.SmsProvider

/** Receives the "delivered" PendingIntent callback and marks the message DELIVERED. */
class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        if (messageId < 0) return
        SmsProvider(context).setStatus(messageId, Sms.STATUS_COMPLETE)
    }
}
