package com.personal.sms.data.sms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import com.personal.sms.receiver.SmsDeliveredReceiver
import com.personal.sms.receiver.SmsSentReceiver

const val EXTRA_MESSAGE_ID = "com.personal.sms.MESSAGE_ID"
const val ACTION_SMS_SENT = "com.personal.sms.SMS_SENT"
const val ACTION_SMS_DELIVERED = "com.personal.sms.SMS_DELIVERED"

/**
 * Sends SMS over the SIM with multipart support. Each logical message is persisted
 * to the provider as OUTBOX first, then advanced to SENT/FAILED (sent callback) and
 * DELIVERED (delivery callback). For multipart messages the callbacks are attached
 * only to the final part, so one logical message yields one status transition.
 */
class SmsSender(private val context: Context) {

    private val provider = SmsProvider(context)

    /** Persist + send. Returns the provider message id so callers can observe status. */
    fun send(address: String, body: String, threadId: Long? = null): Long {
        val messageId = provider.insertOutbox(address, body)
        val sms = smsManager()
        val parts = sms.divideMessage(body)

        val sentIntent = statusIntent(SmsSentReceiver::class.java, ACTION_SMS_SENT, messageId, even = true)
        val deliveredIntent =
            statusIntent(SmsDeliveredReceiver::class.java, ACTION_SMS_DELIVERED, messageId, even = false)

        try {
            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent?>(parts.size)
                val deliveredIntents = ArrayList<PendingIntent?>(parts.size)
                for (i in parts.indices) {
                    val last = i == parts.size - 1
                    sentIntents.add(if (last) sentIntent else null)
                    deliveredIntents.add(if (last) deliveredIntent else null)
                }
                sms.sendMultipartTextMessage(address, null, parts, sentIntents, deliveredIntents)
            } else {
                sms.sendTextMessage(address, null, body, sentIntent, deliveredIntent)
            }
        } catch (_: Exception) {
            provider.setType(messageId, Sms.MESSAGE_TYPE_FAILED)
        }
        return messageId
    }

    private fun statusIntent(
        target: Class<*>,
        action: String,
        messageId: Long,
        even: Boolean,
    ): PendingIntent {
        val intent = Intent(context, target).apply {
            this.action = action
            putExtra(EXTRA_MESSAGE_ID, messageId)
        }
        // Distinct request codes keep the sent/delivered PendingIntents from colliding.
        val requestCode = (messageId.toInt() shl 1) or (if (even) 0 else 1)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    @Suppress("DEPRECATION")
    private fun smsManager(): SmsManager =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
}
