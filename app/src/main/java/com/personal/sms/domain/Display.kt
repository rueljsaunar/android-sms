package com.personal.sms.domain

/** Initials for an avatar: first+last word initial, or first two letters of a lone word. */
fun initials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

/** Short label for a number with no contact, e.g. "+14155550198" -> "+1". */
fun numberLabel(address: String): String {
    val normalized = normalizeNumber(address)
    return if (normalized.startsWith("+")) {
        "+" + normalized.drop(1).take(1)
    } else {
        normalized.filter { it.isDigit() }.take(2)
    }
}

/**
 * Deterministic avatar color index from an identity key (name or number), so the
 * same contact always gets the same color. Returned as an index into the palette.
 */
fun avatarColorIndex(key: String, paletteSize: Int): Int {
    if (paletteSize <= 0) return 0
    var hash = 0
    for (ch in key) hash = (hash * 31 + ch.code) and 0x7fffffff
    return hash % paletteSize
}
