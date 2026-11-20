package com.personal.sms.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.personal.sms.MainActivity
import com.personal.sms.R
import com.personal.sms.data.contacts.ContactsRepository
import com.personal.sms.domain.formatNumber
import com.personal.sms.receiver.DirectReplyReceiver

const val MESSAGES_CHANNEL_ID = "messages"
const val MESSAGES_CHANNEL_NAME = "Messages"
const val KEY_REPLY = "key_reply"
const val EXTRA_NOTIF_ADDRESS = "com.personal.sms.NOTIF_ADDRESS"
const val EXTRA_NOTIF_SENDER = "com.personal.sms.NOTIF_SENDER"
const val EXTRA_NOTIF_BODY = "com.personal.sms.NOTIF_BODY"
const val EXTRA_NOTIF_DATE = "com.personal.sms.NOTIF_DATE"
const val EXTRA_OPEN_THREAD_ADDRESS = "com.personal.sms.OPEN_THREAD_ADDRESS"

/** One line in a MessagingStyle notification. */
data class NotifLine(val text: String, val fromUser: Boolean, val time: Long)

/**
 * Posts incoming-message notifications as MessagingStyle with an inline direct-reply
 * action. After a reply is sent, [postReplySent] re-posts the conversation with the
 * reply appended. Fails soft (no crash) when POST_NOTIFICATIONS is denied.
 */
class MessageNotifier(private val context: Context) {

    fun notifyIncoming(address: String, body: String, date: Long) {
        val name = ContactsRepository(context).resolve(address)?.displayName ?: formatNumber(address)
        post(address, name, listOf(NotifLine(body, fromUser = false, time = date)))
    }

    fun postReplySent(
        address: String,
        senderName: String,
        originalBody: String,
        originalDate: Long,
        replyText: String,
    ) {
        post(
            address = address,
            senderName = senderName,
            lines = listOf(
                NotifLine(originalBody, fromUser = false, time = originalDate),
                NotifLine(replyText, fromUser = true, time = System.currentTimeMillis()),
            ),
        )
    }

    private fun post(address: String, senderName: String, lines: List<NotifLine>) {
        ensureChannel()
        if (!hasPermission()) return

        val you = Person.Builder().setName("You").build()
        val sender = Person.Builder().setName(senderName).build()
        val style = NotificationCompat.MessagingStyle(you)
        lines.forEach { style.addMessage(it.text, it.time, if (it.fromUser) you else sender) }

        val notification = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(style)
            .setWhen(lines.last().time)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openThreadIntent(address))
            .addAction(replyAction(address, senderName, lines.first()))
            .build()

        NotificationManagerCompat.from(context).notify(notificationIdFor(address), notification)
    }

    private fun replyAction(
        address: String,
        senderName: String,
        original: NotifLine,
    ): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder(KEY_REPLY).setLabel("Reply").build()
        val intent = Intent(context, DirectReplyReceiver::class.java).apply {
            putExtra(EXTRA_NOTIF_ADDRESS, address)
            putExtra(EXTRA_NOTIF_SENDER, senderName)
            putExtra(EXTRA_NOTIF_BODY, original.text)
            putExtra(EXTRA_NOTIF_DATE, original.time)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationIdFor(address),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Action.Builder(R.drawable.ic_notification, "Reply", pendingIntent)
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun openThreadIntent(address: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_THREAD_ADDRESS, address)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            notificationIdFor(address),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun notificationIdFor(address: String): Int = address.hashCode()

    private fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MESSAGES_CHANNEL_ID,
                MESSAGES_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Incoming text messages" }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
