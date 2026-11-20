package com.personal.sms.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.personal.sms.data.local.entity.ScheduledMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {

    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledAt ASC")
    fun pending(): Flow<List<ScheduledMessageEntity>>

    /** Snapshot for boot re-arm (no Flow needed). */
    @Query("SELECT * FROM scheduled_messages")
    suspend fun pendingOnce(): List<ScheduledMessageEntity>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id")
    suspend fun byId(id: Long): ScheduledMessageEntity?

    @Insert
    suspend fun insert(entity: ScheduledMessageEntity): Long

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM scheduled_messages WHERE address = :address")
    suspend fun deleteByAddress(address: String)
}
