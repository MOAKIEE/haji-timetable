package com.example.timetable.data.database.dao

import androidx.room.*
import com.example.timetable.data.database.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 课表数据访问对象
 */
@Dao
interface ScheduleDao {
    
    @Query("SELECT * FROM schedules ORDER BY createdAt ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>
    
    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: String): ScheduleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)
    
    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
    
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
    
    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String)
    
    @Query("SELECT COUNT(*) FROM schedules")
    suspend fun getScheduleCount(): Int
}
