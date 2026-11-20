package com.personal.sms.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.personal.sms.data.local.dao.ArchiveDao
import com.personal.sms.data.local.dao.DraftDao
import com.personal.sms.data.local.dao.ScheduledMessageDao
import com.personal.sms.data.local.entity.ArchivedThreadEntity
import com.personal.sms.data.local.entity.DraftEntity
import com.personal.sms.data.local.entity.ScheduledMessageEntity

/**
 * App-private database. Holds only state the system SMS provider can't:
 * scheduled sends, per-thread drafts, and archive flags.
 */
@Database(
    entities = [
        ScheduledMessageEntity::class,
        DraftEntity::class,
        ArchivedThreadEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun draftDao(): DraftDao
    abstract fun archiveDao(): ArchiveDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "personal_sms.db",
                ).build().also { instance = it }
            }
    }
}
