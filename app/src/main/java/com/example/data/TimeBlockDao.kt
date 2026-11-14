package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_blocks ORDER BY hourStart ASC")
    fun observeAllBlocks(): Flow<List<TimeBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlock(block: TimeBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlocks(blocks: List<TimeBlockEntity>)

    @Query("UPDATE time_blocks SET isCompleted = :completed WHERE hourStart = :hourStart")
    suspend fun setBlockCompleted(hourStart: Int, completed: Boolean)

    @Query("DELETE FROM time_blocks WHERE hourStart = :hourStart")
    suspend fun clearBlock(hourStart: Int)

    @Query("DELETE FROM time_blocks WHERE taskId = :taskId")
    suspend fun clearBlocksForTask(taskId: Long)

    @Query("DELETE FROM time_blocks")
    suspend fun clearAllBlocks()
}
