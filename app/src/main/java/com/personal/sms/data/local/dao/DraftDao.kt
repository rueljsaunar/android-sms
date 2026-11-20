package com.personal.sms.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.personal.sms.data.local.entity.DraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {

    @Query("SELECT * FROM drafts WHERE threadId = :threadId")
    fun draftFor(threadId: Long): Flow<DraftEntity?>

    @Upsert
    suspend fun upsert(entity: DraftEntity)

    @Query("DELETE FROM drafts WHERE threadId = :threadId")
    suspend fun clear(threadId: Long)
}
