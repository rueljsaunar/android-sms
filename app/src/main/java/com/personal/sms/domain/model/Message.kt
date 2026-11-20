package com.personal.sms.domain.model

/** Direction of a message within a thread. */
enum class MessageType { INBOX, SENT }

/** Delivery lifecycle for an outbound message. Inbound messages carry null. */
enum class MessageStatus { SENDING, SENT, DELIVERED, FAILED }

/** A single SMS, mapped from the system provider (or pending in the composer). */
data class Message(
    val id: Long,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: MessageType,
    val status: MessageStatus? = null,
)
