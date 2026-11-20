package com.personal.sms.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeBucket { TODAY, YESTERDAY, THIS_WEEK, OLDER }

private fun startOfDayMillis(epochMs: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = epochMs
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

/** Classify a timestamp relative to [now] by calendar-day distance. */
fun timeBucket(epochMs: Long, now: Long): TimeBucket {
    val days = ((startOfDayMillis(now) - startOfDayMillis(epochMs)) / 86_400_000L).toInt()
    return when {
        days <= 0 -> TimeBucket.TODAY
        days == 1 -> TimeBucket.YESTERDAY
        days in 2..6 -> TimeBucket.THIS_WEEK
        else -> TimeBucket.OLDER
    }
}

/** Human timestamp for list rows: clock time today, "Yesterday", weekday, or short date. */
fun relativeTime(epochMs: Long, now: Long = System.currentTimeMillis()): String {
    val locale = Locale.getDefault()
    return when (timeBucket(epochMs, now)) {
        TimeBucket.TODAY -> SimpleDateFormat("h:mm a", locale).format(Date(epochMs))
        TimeBucket.YESTERDAY -> "Yesterday"
        TimeBucket.THIS_WEEK -> SimpleDateFormat("EEE", locale).format(Date(epochMs))
        TimeBucket.OLDER -> SimpleDateFormat("M/d/yy", locale).format(Date(epochMs))
    }
}

/** Full timestamp for message rows / "Delivered · 9:41 AM". */
fun clockTime(epochMs: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMs))
