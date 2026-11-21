package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.util.DateTimeFormatters

@Immutable
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
        get() = "${DateTimeFormatters.formatHourSlot(hourStart)} - ${DateTimeFormatters.formatHourSlot(hourStart + 1)}"
}
