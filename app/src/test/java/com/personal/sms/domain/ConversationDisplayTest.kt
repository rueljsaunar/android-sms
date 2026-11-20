package com.personal.sms.domain

import com.personal.sms.domain.model.Contact
import com.personal.sms.domain.model.Conversation
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationDisplayTest {

    @Test
    fun unknown_number_shows_formatted_number_and_prefix_label() {
        val c = Conversation(
            threadId = 1,
            address = "+14155550198",
            contact = null,
            snippet = "hi",
            date = 0,
            unreadCount = 0,
        )
        assertEquals("+1 (415) 555-0198", c.displayName())
        assertEquals("+1", c.avatarLabel())
    }

    @Test
    fun contact_shows_name_and_initials() {
        val contact = Contact(
            id = null,
            displayName = "Maya Rios",
            photoUri = null,
            numbers = listOf("+14155550198"),
        )
        val c = Conversation(
            threadId = 1,
            address = "+14155550198",
            contact = contact,
            snippet = "hi",
            date = 0,
            unreadCount = 0,
        )
        assertEquals("Maya Rios", c.displayName())
        assertEquals("MR", c.avatarLabel())
    }
}
