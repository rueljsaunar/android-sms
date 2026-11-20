package com.personal.sms.data.sms

import android.database.Cursor
import android.provider.Telephony.Sms
import com.personal.sms.domain.model.Message
import com.personal.sms.domain.model.MessageStatus
import com.personal.sms.domain.model.MessageType

/** One row of the system SMS table, before contact resolution / Room merge. */
data class RawConversation(
    val threadId: Long,
    val address: String,
    val snippet: String,
    val date: Long,
    val unreadCount: Int,
    val lastIsSent: Boolean,
)

/** Projection shared by the message/conversation queries. */
val SMS_PROJECTION = arrayOf(
    Sms._ID,
    Sms.THREAD_ID,
    Sms.ADDRESS,
    Sms.BODY,
    Sms.DATE,
    Sms.READ,
    Sms.TYPE,
    Sms.STATUS,
)

/** Map the current cursor row to a domain [Message]. */
fun Cursor.toMessage(): Message {
    val id = getLong(getColumnIndexOrThrow(Sms._ID))
    val threadId = getLong(getColumnIndexOrThrow(Sms.THREAD_ID))
    val address = getString(getColumnIndexOrThrow(Sms.ADDRESS)).orEmpty()
    val body = getString(getColumnIndexOrThrow(Sms.BODY)).orEmpty()
    val date = getLong(getColumnIndexOrThrow(Sms.DATE))
    val rawType = getInt(getColumnIndexOrThrow(Sms.TYPE))
    val rawStatus = getInt(getColumnIndexOrThrow(Sms.STATUS))

    val type = if (rawType == Sms.MESSAGE_TYPE_INBOX) MessageType.INBOX else MessageType.SENT
    val status = when (type) {
        MessageType.INBOX -> null
        MessageType.SENT -> when {
            rawType == Sms.MESSAGE_TYPE_FAILED -> MessageStatus.FAILED
            rawType == Sms.MESSAGE_TYPE_OUTBOX || rawType == Sms.MESSAGE_TYPE_QUEUED -> MessageStatus.SENDING
            rawStatus == Sms.STATUS_COMPLETE -> MessageStatus.DELIVERED
            rawStatus == Sms.STATUS_FAILED -> MessageStatus.FAILED
            else -> MessageStatus.SENT
        }
    }
    return Message(id, threadId, address, body, date, type, status)
}
