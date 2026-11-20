package com.personal.sms.domain.model

/** A single message that matched a search query. */
data class SearchHit(
    val threadId: Long,
    val address: String,
    val contact: Contact?,
    val body: String,
    val date: Long,
)
