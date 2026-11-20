package com.personal.sms.domain.model

/** A device contact resolved for a phone number. */
data class Contact(
    val id: Long?,
    val displayName: String,
    val photoUri: String?,
    val numbers: List<String>,
)
