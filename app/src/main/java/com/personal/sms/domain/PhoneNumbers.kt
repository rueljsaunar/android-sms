package com.personal.sms.domain

/**
 * Pure phone-number helpers. Deliberately simple (US-style grouping, no libphonenumber)
 * — this is a personal app on a single SIM. Unusual numbers are left untouched.
 */

/** Strip formatting, keeping a single leading '+' if present. */
fun normalizeNumber(raw: String): String {
    val trimmed = raw.trim()
    val digits = trimmed.filter { it.isDigit() }
    return if (trimmed.startsWith("+")) "+$digits" else digits
}

/** Pretty-print common US numbers; return the input unchanged for anything else. */
fun formatNumber(input: String): String {
    val normalized = normalizeNumber(input)
    val digits = normalized.filter { it.isDigit() }
    val hasPlus = normalized.startsWith("+")
    return when {
        hasPlus && digits.length == 11 && digits.startsWith("1") -> {
            val d = digits.substring(1)
            "+1 (${d.substring(0, 3)}) ${d.substring(3, 6)}-${d.substring(6)}"
        }
        !hasPlus && digits.length == 10 ->
            "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
        else -> input
    }
}
