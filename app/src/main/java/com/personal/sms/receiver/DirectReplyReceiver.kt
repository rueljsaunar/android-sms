package com.personal.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.personal.sms.data.sms.SmsSender
import com.personal.sms.notification.EXTRA_NOTIF_ADDRESS
import com.personal.sms.notification.EXTRA_NOTIF_BODY
import com.personal.sms.notification.EXTRA_NOTIF_DATE
import com.personal.sms.notification.EXTRA_NOTIF_SENDER
import com.personal.sms.notification.KEY_REPLY
import com.personal.sms.notification.MessageNotifier

/** Handles the inline reply typed in the notification shade: sends it, updates the notification. */
class DirectReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reply = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(KEY_REPLY)?.toString()?.trim()
        val address = intent.getStringExtra(EXTRA_NOTIF_ADDRESS)
        if (reply.isNullOrEmpty() || address.isNullOrEmpty()) return

        val senderName = intent.getStringExtra(EXTRA_NOTIF_SENDER) ?: address
        val originalBody = intent.getStringExtra(EXTRA_NOTIF_BODY).orEmpty()
        val originalDate = intent.getLongExtra(EXTRA_NOTIF_DATE, System.currentTimeMillis())

        SmsSender(context.applicationContext).send(address, reply)
        MessageNotifier(context.applicationContext)
            .postReplySent(address, senderName, originalBody, originalDate, reply)
    }
}
