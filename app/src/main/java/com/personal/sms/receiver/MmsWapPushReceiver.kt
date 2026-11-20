package com.personal.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * WAP_PUSH_DELIVER receiver. Required by the default-SMS-app contract even though
 * MMS (picture messaging) is out of scope for v1.
 *
 * MMS would slot in here: parse the WAP push PDU from the intent, download the
 * message body/parts from the carrier MMSC, and persist it to the Telephony.Mms
 * tables. Left intentionally empty for v1 (SMS only).
 */
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // No-op: MMS not handled in v1. See class doc for where it would go.
    }
}
