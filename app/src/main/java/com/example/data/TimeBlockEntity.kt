package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey val hourStart: Int,
    val taskId: Long? = null,
    val title: String = "",
    val subtitle: String = "",
    val quadrantOrdinal: Int? = null,
    val isCompleted: Boolean = false
) {
    val quadrant: Quadrant?
        get() = quadrantOrdinal?.let { Quadrant.entries.getOrNull(it) }

    val timeRangeLabel: String
        get() = String.format("%02d:00 - %02d:00", hourStart, hourStart + 1)
}
