package com.personal.sms.data.sms

import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony.Sms
import com.personal.sms.domain.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Read/write access to the system SMS content provider. Reads come back as Flows
 * that re-query whenever the provider changes (via ContentObserver), so incoming
 * texts refresh the UI automatically. All queries run on IO and fail soft to empty
 * results when READ_SMS isn't granted.
 */
class SmsProvider(private val context: Context) {

    private val resolver get() = context.contentResolver

    // ---- Reads ----

    fun observeConversations(): Flow<List<RawConversation>> =
        changesOf(Sms.CONTENT_URI).map { queryConversations() }.flowOn(Dispatchers.IO)

    fun observeMessages(threadId: Long): Flow<List<Message>> =
        changesOf(Sms.CONTENT_URI).map { queryMessages(threadId) }.flowOn(Dispatchers.IO)

    fun searchMessages(query: String): List<Message> {
        if (query.isBlank()) return emptyList()
        return safeQuery(
            uri = Sms.CONTENT_URI,
            selection = "${Sms.BODY} LIKE ?",
            selectionArgs = arrayOf("%$query%"),
            sortOrder = "${Sms.DATE} DESC",
        ) { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.toMessage()) }
        } ?: emptyList()
    }

    private fun queryMessages(threadId: Long): List<Message> =
        safeQuery(
            uri = Sms.CONTENT_URI,
            selection = "${Sms.THREAD_ID} = ?",
            selectionArgs = arrayOf(threadId.toString()),
            sortOrder = "${Sms.DATE} ASC",
        ) { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.toMessage()) }
        } ?: emptyList()

    /**
     * Build the conversation list by scanning messages newest-first and keeping the
     * first (latest) row per thread for the snippet, plus an unread tally.
     */
    private fun queryConversations(): List<RawConversation> {
        val latest = LinkedHashMap<Long, RawConversation>()
        val unread = HashMap<Long, Int>()
        safeQuery(
            uri = Sms.CONTENT_URI,
            selection = null,
            selectionArgs = null,
            sortOrder = "${Sms.DATE} DESC",
        ) { cursor ->
            while (cursor.moveToNext()) {
                val msg = cursor.toMessage()
                val read = cursor.getInt(cursor.getColumnIndexOrThrow(Sms.READ))
                if (msg.type == com.personal.sms.domain.model.MessageType.INBOX && read == 0) {
                    unread[msg.threadId] = (unread[msg.threadId] ?: 0) + 1
                }
                if (!latest.containsKey(msg.threadId)) {
                    latest[msg.threadId] = RawConversation(
                        threadId = msg.threadId,
                        address = msg.address,
                        snippet = msg.body,
                        date = msg.date,
                        unreadCount = 0,
                        lastIsSent = msg.type == com.personal.sms.domain.model.MessageType.SENT,
                    )
                }
            }
        }
        return latest.values.map { it.copy(unreadCount = unread[it.threadId] ?: 0) }
    }

    // ---- Writes (default SMS app is responsible for persisting messages) ----

    /** Persist an incoming message to the inbox. Returns the new row id (-1 on failure). */
    fun insertInbox(address: String, body: String, date: Long): Long {
        val values = ContentValues().apply {
            put(Sms.ADDRESS, address)
            put(Sms.BODY, body)
            put(Sms.DATE, date)
            put(Sms.READ, 0)
            put(Sms.SEEN, 0)
            put(Sms.TYPE, Sms.MESSAGE_TYPE_INBOX)
            threadIdFor(address)?.let { put(Sms.THREAD_ID, it) }
        }
        return insert(Sms.CONTENT_URI, values)
    }

    /** Persist an outbound message as OUTBOX (sending). Returns the new row id. */
    fun insertOutbox(address: String, body: String): Long {
        val values = ContentValues().apply {
            put(Sms.ADDRESS, address)
            put(Sms.BODY, body)
            put(Sms.DATE, System.currentTimeMillis())
            put(Sms.READ, 1)
            put(Sms.SEEN, 1)
            put(Sms.TYPE, Sms.MESSAGE_TYPE_OUTBOX)
            threadIdFor(address)?.let { put(Sms.THREAD_ID, it) }
        }
        return insert(Sms.CONTENT_URI, values)
    }

    /** Resolve (or create) the system thread id for an address, OEM-safe. */
    fun threadIdFor(address: String): Long? = try {
        android.provider.Telephony.Threads.getOrCreateThreadId(context, address)
    } catch (_: Exception) {
        null
    }

    fun setType(messageId: Long, type: Int) {
        update(messageId, ContentValues().apply { put(Sms.TYPE, type) })
    }

    fun setStatus(messageId: Long, status: Int) {
        update(messageId, ContentValues().apply { put(Sms.STATUS, status) })
    }

    fun markThreadRead(threadId: Long) {
        try {
            resolver.update(
                Sms.CONTENT_URI,
                ContentValues().apply { put(Sms.READ, 1); put(Sms.SEEN, 1) },
                "${Sms.THREAD_ID} = ? AND ${Sms.READ} = 0",
                arrayOf(threadId.toString()),
            )
        } catch (_: SecurityException) {
        }
    }

    fun deleteMessage(messageId: Long) {
        try {
            resolver.delete(Uri.withAppendedPath(Sms.CONTENT_URI, messageId.toString()), null, null)
        } catch (_: SecurityException) {
        }
    }

    fun deleteThread(threadId: Long) {
        try {
            resolver.delete(Sms.CONTENT_URI, "${Sms.THREAD_ID} = ?", arrayOf(threadId.toString()))
        } catch (_: SecurityException) {
        }
    }

    // ---- Internals ----

    private fun insert(uri: Uri, values: ContentValues): Long = try {
        resolver.insert(uri, values)?.lastPathSegment?.toLongOrNull() ?: -1L
    } catch (_: SecurityException) {
        -1L
    }

    private fun update(messageId: Long, values: ContentValues) {
        try {
            resolver.update(
                Uri.withAppendedPath(Sms.CONTENT_URI, messageId.toString()),
                values,
                null,
                null,
            )
        } catch (_: SecurityException) {
        }
    }

    private fun <T> safeQuery(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
        block: (android.database.Cursor) -> T,
    ): T? = try {
        resolver.query(uri, SMS_PROJECTION, selection, selectionArgs, sortOrder)?.use(block)
    } catch (_: SecurityException) {
        null
    }

    /** Emits once immediately, then on every change to [uri]. */
    private fun changesOf(uri: Uri): Flow<Unit> = callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(uri, true, observer)
        trySend(Unit)
        awaitClose { resolver.unregisterContentObserver(observer) }
    }
}
