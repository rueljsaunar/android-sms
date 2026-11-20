package com.personal.sms.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTest {

    @Test
    fun finds_single_match_range() {
        val text = "Can you send the address?"
        val ranges = matchQuery(text, "address")
        assertEquals(1, ranges.size)
        assertEquals("address", text.substring(ranges[0]).lowercase())
    }

    @Test
    fun empty_query_yields_no_ranges() {
        assertEquals(emptyList<IntRange>(), matchQuery("anything", ""))
    }

    @Test
    fun match_is_case_insensitive() {
        val ranges = matchQuery("The Address is 240 Elm St", "address")
        assertEquals(1, ranges.size)
    }

    @Test
    fun finds_every_occurrence() {
        assertEquals(3, matchQuery("ab ab ab", "ab").size)
    }
}
