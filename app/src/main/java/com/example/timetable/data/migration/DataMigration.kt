package com.example.timetable.data.migration

import android.content.Context
import android.util.Log
import com.example.timetable.data.database.TimetableDatabase
import com.example.timetable.data.repository.DataManager
import com.example.timetable.data.repository.TimetableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 数据迁移工具
 * 从 SharedPreferences 迁移到 Room Database
 */
object DataMigration {
    
    private const val TAG = "DataMigration"
    private const val MIGRATION_FLAG = "migration_to_room_completed"
    
    /**
     * 检查是否需要迁移
     */
    suspend fun migrateIfNeeded(context: Context): Boolean = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("migration_flags", Context.MODE_PRIVATE)
        val migrated = prefs.getBoolean(MIGRATION_FLAG, false)
        
        if (migrated) {
            Log.d(TAG, "Data already migrated")
            return@withContext true
        }
        
        try {
            // 从旧的 SharedPreferences 加载数据
            val oldData = DataManager.load(context)
            
            if (oldData == null) {
                Log.d(TAG, "No old data to migrate, initializing fresh")
                // 没有旧数据，初始化默认数据
                val database = TimetableDatabase.getDatabase(context)
                val repository = TimetableRepository(database)
                repository.initializeDefaultData()
            } else {
                Log.d(TAG, "Migrating old data to Room")
                // 迁移数据
                migrateData(context, oldData)
            }
            
            // 标记迁移完成
            prefs.edit().putBoolean(MIGRATION_FLAG, true).apply()
            Log.d(TAG, "Migration completed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed", e)
            false
        }
    }
    
    /**
     * 执行实际的数据迁移
     */
    private suspend fun migrateData(
        context: Context,
        oldData: Triple<List<com.example.timetable.data.model.Schedule>, 
                       List<com.example.timetable.data.model.SectionTime>,
                       Triple<String, com.example.timetable.data.model.AppSettings, Unit>>
    ) {
        val (schedules, timeSlots, settingsTuple) = oldData
        val (currentScheduleId, settings, _) = settingsTuple
        
        val database = TimetableDatabase.getDatabase(context)
        val repository = TimetableRepository(database)
        
        // 迁移课表
        schedules.forEach { schedule ->
            repository.insertSchedule(schedule)
            // 迁移该课表的课程
            schedule.courses.forEach { course ->
                repository.insertCourse(course, schedule.id)
            }
        }
        
        // 迁移作息时间
        if (timeSlots.isNotEmpty()) {
            repository.insertSectionTimes(timeSlots)
        }
        
        // 迁移设置
        val validScheduleId = if (schedules.any { it.id == currentScheduleId }) {
            currentScheduleId
        } else {
            schedules.firstOrNull()?.id ?: ""
        }
        repository.saveSettings(settings, validScheduleId)
        
        Log.d(TAG, "Migrated ${schedules.size} schedules, ${timeSlots.size} time slots")
    }
}
