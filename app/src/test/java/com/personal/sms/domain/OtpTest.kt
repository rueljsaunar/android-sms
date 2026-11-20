package com.personal.sms.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OtpTest {

    @Test
    fun detects_spaced_code_after_keyword() {
        assertEquals("418203", detectOtp("Code 418 203")?.code)
    }

    @Test
    fun detects_inline_code() {
        assertEquals("992213", detectOtp("Your code is 992213")?.code)
    }

    @Test
    fun ignores_plain_number_without_verification_keyword() {
        assertNull(detectOtp("meet in room 418"))
    }

    @Test
    fun ignores_message_with_no_code() {
        assertNull(detectOtp("see you then 🎉"))
    }

    @Test
    fun ignores_keyword_as_substring_of_a_word() {
        // "shipping" contains "pin"; "barcode" contains "code" — neither is a
        // verification keyword, so no Copy chip.
        assertNull(detectOtp("Your shipping number is 12345678"))
        assertNull(detectOtp("Scan the barcode 12345678"))
    }

    @Test
    fun range_covers_the_matched_code() {
        val body = "Your code is 992213"
        val match = detectOtp(body)!!
        assertEquals("992213", body.substring(match.range).filter { it.isDigit() })
    }
}
