package com.personal.sms.domain

/** A detected one-time code and the character range it occupies in the message body. */
data class OtpMatch(val code: String, val range: IntRange)

private val OTP_KEYWORDS = listOf(
    "code", "verif", "otp", "pin", "password", "passcode", "one-time", "one time", "auth",
)

// A 4-8 digit run, allowing single spaces or hyphens between digits (e.g. "418 203").
private val CODE_REGEX = Regex("""\b(\d[\d\s-]{2,8}\d)\b""")

/**
 * Detect a verification code: only when the message carries a verification keyword,
 * so plain numbers in conversation ("room 418") don't trigger a Copy chip.
 */
fun detectOtp(body: String): OtpMatch? {
    val lower = body.lowercase()
    if (OTP_KEYWORDS.none { lower.contains(it) }) return null

    val match = CODE_REGEX.find(body) ?: return null
    val digits = match.value.filter { it.isDigit() }
    if (digits.length !in 4..8) return null
    return OtpMatch(digits, match.range)
}
