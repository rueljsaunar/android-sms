package com.personal.sms.domain

/**
 * Case-insensitive substring search returning each match as an inclusive IntRange
 * (start..endInclusive), suitable for highlighting with AnnotatedString.
 */
fun matchQuery(text: String, query: String): List<IntRange> {
    if (query.isEmpty()) return emptyList()
    val ranges = mutableListOf<IntRange>()
    var index = text.indexOf(query, startIndex = 0, ignoreCase = true)
    while (index >= 0) {
        ranges.add(index until index + query.length)
        index = text.indexOf(query, startIndex = index + query.length, ignoreCase = true)
    }
    return ranges
}
