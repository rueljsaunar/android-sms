package com.personal.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.personal.sms.data.sms.SmsProvider
import com.personal.sms.notification.MessageNotifier

/**
 * Receives SMS_DELIVER (only the default SMS app gets this). Persists the incoming
 * message to the provider and posts a notification. Multipart messages arrive as
 * several SmsMessage parts that are concatenated here into one logical message.
 */
class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val address = messages[0].displayOriginatingAddress
            ?: messages[0].originatingAddress
            ?: return
        val body = messages.joinToString(separator = "") {
            it.displayMessageBody ?: it.messageBody ?: ""
        }
        val date = System.currentTimeMillis()

        SmsProvider(context).insertInbox(address, body, date)
        MessageNotifier(context).notifyIncoming(address, body, date)
    }
}
