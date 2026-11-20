package com.personal.sms.domain

/** A detected one-time code and the character range it occupies in the message body. */
data class OtpMatch(val code: String, val range: IntRange)

private val OTP_KEYWORDS = listOf(
    "code", "verif", "otp", "pin", "password", "passcode", "one-time", "one time",
)

// Keywords must appear at a word boundary, so "shipping" (contains "pin") and
// "barcode" (contains "code") don't count as verification messages.
private val KEYWORD_REGEX = Regex(
    "\\b(" + OTP_KEYWORDS.joinToString("|") { Regex.escape(it) } + ")",
    RegexOption.IGNORE_CASE,
)

// A 4-8 digit run, allowing single spaces or hyphens between digits (e.g. "418 203").
private val CODE_REGEX = Regex("""\b(\d[\d\s-]{2,8}\d)\b""")

/**
 * Detect a verification code: only when the message carries a verification keyword,
 * so plain numbers in conversation ("room 418") don't trigger a Copy chip.
 */
fun detectOtp(body: String): OtpMatch? {
    if (!KEYWORD_REGEX.containsMatchIn(body)) return null

    val match = CODE_REGEX.find(body) ?: return null
    val digits = match.value.filter { it.isDigit() }
    if (digits.length !in 4..8) return null
    return OtpMatch(digits, match.range)
}
