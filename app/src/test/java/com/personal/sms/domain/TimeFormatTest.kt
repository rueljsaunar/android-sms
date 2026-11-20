package com.personal.sms.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class TimeFormatTest {

    private fun at(year: Int, month0: Int, day: Int, hour: Int, min: Int): Long {
        val c = Calendar.getInstance()
        c.set(year, month0, day, hour, min, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    // Reference "now": Fri 2026-11-20 14:30 (month is 0-based).
    private val now = at(2026, 10, 20, 14, 30)

    @Test
    fun bucket_same_day_is_today() {
        assertEquals(TimeBucket.TODAY, timeBucket(at(2026, 10, 20, 9, 41), now))
    }

    @Test
    fun bucket_prior_day_is_yesterday() {
        assertEquals(TimeBucket.YESTERDAY, timeBucket(at(2026, 10, 19, 23, 0), now))
    }

    @Test
    fun bucket_three_days_ago_is_this_week() {
        assertEquals(TimeBucket.THIS_WEEK, timeBucket(at(2026, 10, 17, 9, 0), now))
    }

    @Test
    fun bucket_last_month_is_older() {
        assertEquals(TimeBucket.OLDER, timeBucket(at(2026, 9, 1, 9, 0), now))
    }

    @Test
    fun relative_today_renders_clock_time() {
        assertTrue(relativeTime(at(2026, 10, 20, 9, 41), now).contains(":"))
    }

    @Test
    fun relative_yesterday_label() {
        assertEquals("Yesterday", relativeTime(at(2026, 10, 19, 9, 0), now))
    }
}
