package com.personal.sms.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun normalize_strips_formatting_keeps_plus() {
        assertEquals("+14155550198", normalizeNumber("+1 (415) 555-0198"))
    }

    @Test
    fun normalize_ten_digit_without_country() {
        assertEquals("6285550143", normalizeNumber("(628) 555-0143"))
    }

    @Test
    fun format_us_e164() {
        assertEquals("+1 (415) 555-0198", formatNumber("+14155550198"))
    }

    @Test
    fun format_ten_digit() {
        assertEquals("(415) 555-0198", formatNumber("4155550198"))
    }

    @Test
    fun format_leaves_short_code_untouched() {
        assertEquals("262966", formatNumber("262966"))
    }
}
