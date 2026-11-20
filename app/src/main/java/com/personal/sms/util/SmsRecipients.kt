package com.personal.sms.util

import android.net.Uri

/**
 * Extract recipient numbers from a sms:/smsto:/mms:/mmsto: URI, as used by the
 * ACTION_SENDTO and ACTION_RESPOND_VIA_MESSAGE contracts. Strips any ?body=
 * query and splits multiple recipients on ';' or ','.
 */
fun recipientsFromUri(uri: Uri?): List<String> {
    if (uri == null) return emptyList()
    val raw = uri.schemeSpecificPart ?: return emptyList()
    val withoutQuery = raw.substringBefore('?')
    return withoutQuery.split(';', ',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
