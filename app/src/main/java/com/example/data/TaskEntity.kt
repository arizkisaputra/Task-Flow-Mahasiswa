package com.example.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val course: String,
    val deadlineMillis: Long,
    val isImportant: Boolean,
    val isUrgent: Boolean,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val quadrant: Quadrant
        get() = Quadrant.fromImportanceAndUrgency(isImportant = isImportant, isUrgent = isUrgent)
}
