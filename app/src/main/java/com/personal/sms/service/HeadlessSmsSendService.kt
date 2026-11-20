package com.personal.sms.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.TelephonyManager
import com.personal.sms.data.sms.SmsSender
import com.personal.sms.util.recipientsFromUri

/**
 * Handles ACTION_RESPOND_VIA_MESSAGE ("quick reply" from the in-call screen) —
 * required by the default-SMS-app contract. Sends the reply over SMS, headless.
 */
class HeadlessSmsSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == TelephonyManager.ACTION_RESPOND_VIA_MESSAGE) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val recipient = recipientsFromUri(intent.data).firstOrNull()
            if (!text.isNullOrBlank() && !recipient.isNullOrBlank()) {
                SmsSender(applicationContext).send(recipient, text)
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
