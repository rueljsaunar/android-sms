package com.personal.sms.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Presence of a row means the thread is archived. */
@Entity(tableName = "archived_threads")
data class ArchivedThreadEntity(
    @PrimaryKey val threadId: Long,
)
