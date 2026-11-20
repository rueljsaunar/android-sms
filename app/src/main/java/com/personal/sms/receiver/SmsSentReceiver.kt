package com.personal.sms.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.personal.sms.data.sms.EXTRA_MESSAGE_ID
import com.personal.sms.data.sms.SmsProvider

/** Receives the "sent" PendingIntent callback and advances the message to SENT/FAILED. */
class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
        if (messageId < 0) return
        val provider = SmsProvider(context)
        if (resultCode == Activity.RESULT_OK) {
            provider.setType(messageId, Sms.MESSAGE_TYPE_SENT)
        } else {
            provider.setType(messageId, Sms.MESSAGE_TYPE_FAILED)
        }
    }
}
