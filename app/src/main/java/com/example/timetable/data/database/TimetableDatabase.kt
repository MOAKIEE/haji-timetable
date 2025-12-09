package com.example.timetable.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.timetable.data.database.dao.*
import com.example.timetable.data.database.entity.*

/**
 * 课程表数据库
 */
@Database(
    entities = [
        ScheduleEntity::class,
        CourseEntity::class,
        SectionTimeEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TimetableDatabase : RoomDatabase() {
    
    abstract fun scheduleDao(): ScheduleDao
    abstract fun courseDao(): CourseDao
    abstract fun sectionTimeDao(): SectionTimeDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: TimetableDatabase? = null
        
        fun getDatabase(context: Context): TimetableDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimetableDatabase::class.java,
                    "timetable_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
