package com.personal.sms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personal.sms.data.local.entity.ArchivedThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ArchiveDao {

    @Query("SELECT threadId FROM archived_threads")
    abstract fun archivedThreadIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: ArchivedThreadEntity)

    @Query("DELETE FROM archived_threads WHERE threadId = :threadId")
    abstract suspend fun delete(threadId: Long)

    /** Archive or un-archive a thread by presence/absence of its row. */
    suspend fun setArchived(threadId: Long, archived: Boolean) {
        if (archived) insert(ArchivedThreadEntity(threadId)) else delete(threadId)
    }
}
