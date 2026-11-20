package com.personal.sms.domain.model

import com.personal.sms.domain.formatNumber
import com.personal.sms.domain.initials
import com.personal.sms.domain.numberLabel

/**
 * A conversation thread, as shown in the list. Composed from the system provider
 * (threadId, address, snippet, date, unreadCount) plus app-owned state
 * (isScheduled/scheduledAt from Room, archived from Room) and a resolved contact.
 */
data class Conversation(
    val threadId: Long,
    val address: String,
    val contact: Contact?,
    val snippet: String,
    val date: Long,
    val unreadCount: Int,
    val isScheduled: Boolean = false,
    val scheduledAt: Long? = null,
    val archived: Boolean = false,
) {
    /** Contact name if known, otherwise the formatted phone number. */
    fun displayName(): String = contact?.displayName ?: formatNumber(address)

    /** Avatar initials from the contact name, or a "+1"-style prefix for a bare number. */
    fun avatarLabel(): String =
        contact?.displayName?.let { initials(it) } ?: numberLabel(address)
}
