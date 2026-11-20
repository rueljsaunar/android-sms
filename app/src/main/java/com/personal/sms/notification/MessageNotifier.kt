package com.personal.sms.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.personal.sms.R
import com.personal.sms.data.contacts.ContactsRepository
import com.personal.sms.domain.formatNumber

const val MESSAGES_CHANNEL_ID = "messages"
const val MESSAGES_CHANNEL_NAME = "Messages"

/**
 * Posts incoming-message notifications. This is the minimal version created with
 * the default-SMS contract (Task 7); Task 9 upgrades it to MessagingStyle with an
 * inline direct-reply action.
 */
open class MessageNotifier(protected val context: Context) {

    fun notifyIncoming(address: String, body: String, date: Long) {
        ensureChannel()
        if (!hasPermission()) return
        val contact = ContactsRepository(context).resolve(address)
        val title = contact?.displayName ?: formatNumber(address)

        val notification = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setWhen(date)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(notificationIdFor(address), notification)
    }

    protected fun notificationIdFor(address: String): Int = address.hashCode()

    protected fun hasPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    protected fun ensureChannel() {
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
