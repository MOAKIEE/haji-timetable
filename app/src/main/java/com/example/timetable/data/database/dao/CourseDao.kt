package com.example.timetable.data.database.dao

import androidx.room.*
import com.example.timetable.data.database.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

/**
 * 课程数据访问对象
 */
@Dao
interface CourseDao {
    
    @Query("SELECT * FROM courses WHERE scheduleId = :scheduleId ORDER BY day, startSection")
    fun getCoursesBySchedule(scheduleId: String): Flow<List<CourseEntity>>
    
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: String): CourseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)
    
    @Update
    suspend fun updateCourse(course: CourseEntity)
    
    @Delete
    suspend fun deleteCourse(course: CourseEntity)
    
    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: String)
    
    @Query("DELETE FROM courses WHERE scheduleId = :scheduleId")
    suspend fun deleteCoursesBySchedule(scheduleId: String)
    
    @Query("SELECT * FROM courses WHERE scheduleId = :scheduleId AND day = :day AND startWeek <= :week AND endWeek >= :week")
    suspend fun getCoursesForWeek(scheduleId: String, week: Int, day: Int): List<CourseEntity>
}
