package com.personal.sms.domain.model

/** One phone number from the device contacts, for the new-message picker. */
data class ContactPhone(
    val contactId: Long?,
    val displayName: String,
    val number: String,
    val typeLabel: String,
    val photoUri: String?,
)
