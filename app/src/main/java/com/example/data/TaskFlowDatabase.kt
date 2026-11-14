package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TaskEntity::class, TimeBlockEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun timeBlockDao(): TimeBlockDao

    companion object {
        @Volatile
        private var INSTANCE: TaskFlowDatabase? = null

        fun getInstance(context: Context): TaskFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskFlowDatabase::class.java,
                    "taskflow_mahasiswa.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
