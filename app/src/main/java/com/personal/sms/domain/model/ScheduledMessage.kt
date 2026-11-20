package com.personal.sms.domain.model

/** A message composed now to be sent later by an exact alarm. */
data class ScheduledMessage(
    val id: Long,
    val address: String,
    val body: String,
    val scheduledAt: Long,
    val threadId: Long?,
)
