package com.personal.sms.data.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.personal.sms.domain.model.Contact
import com.personal.sms.domain.model.ContactPhone
import com.personal.sms.domain.normalizeNumber
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves phone numbers to device contacts. Lookups are cached by normalized
 * number for the process lifetime. Fails soft to null / empty when READ_CONTACTS
 * is denied, so the app shows numbers instead of crashing.
 */
class ContactsRepository(private val context: Context) {

    private val cache = ConcurrentHashMap<String, Optional>()

    /** A nullable cache value (ConcurrentHashMap can't store null). */
    private class Optional(val contact: Contact?)

    fun resolve(address: String): Contact? {
        if (address.isBlank()) return null
        val key = normalizeNumber(address)
        cache[key]?.let { return it.contact }

        val resolved = try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(address),
            )
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.PhoneLookup._ID,
                    ContactsContract.PhoneLookup.DISPLAY_NAME,
                    ContactsContract.PhoneLookup.PHOTO_URI,
                ),
                null, null, null,
            )?.use { c ->
                if (c.moveToFirst()) {
                    Contact(
                        id = c.getLong(0),
                        displayName = c.getString(1).orEmpty(),
                        photoUri = c.getString(2),
                        numbers = listOf(address),
                    )
                } else null
            }
        } catch (_: SecurityException) {
            null
        }
        cache[key] = Optional(resolved)
        return resolved
    }

    /** All contact phone numbers, sorted by display name — for the new-message picker. */
    fun allPhones(): List<ContactPhone> = try {
        context.contentResolver.query(
            Phone.CONTENT_URI,
            arrayOf(
                Phone.CONTACT_ID,
                Phone.DISPLAY_NAME,
                Phone.NUMBER,
                Phone.PHOTO_URI,
                Phone.TYPE,
                Phone.LABEL,
            ),
            null, null,
            "${Phone.DISPLAY_NAME} COLLATE NOCASE ASC",
        )?.use { c ->
            buildList {
                while (c.moveToNext()) {
                    val type = c.getInt(4)
                    val label = c.getString(5)
                    val typeLabel = Phone.getTypeLabel(context.resources, type, label).toString()
                    add(
                        ContactPhone(
                            contactId = c.getLong(0),
                            displayName = c.getString(1).orEmpty(),
                            number = c.getString(2).orEmpty(),
                            typeLabel = typeLabel,
                            photoUri = c.getString(3),
                        ),
                    )
                }
            }
        } ?: emptyList()
    } catch (_: SecurityException) {
        emptyList()
    }

    fun clearCache() = cache.clear()
}
