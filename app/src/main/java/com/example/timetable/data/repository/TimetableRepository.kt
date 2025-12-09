package com.example.timetable.data.repository

import com.example.timetable.data.database.TimetableDatabase
import com.example.timetable.data.database.toEntity
import com.example.timetable.data.database.toModel
import com.example.timetable.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 课程表数据仓库
 * 统一管理数据访问逻辑
 */
class TimetableRepository(private val database: TimetableDatabase) {
    
    private val scheduleDao = database.scheduleDao()
    private val courseDao = database.courseDao()
    private val sectionTimeDao = database.sectionTimeDao()
    private val appSettingsDao = database.appSettingsDao()
    
    // ========== 课表操作 ==========
    
    fun getAllSchedules(): Flow<List<Schedule>> {
        return scheduleDao.getAllSchedules().map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    suspend fun getScheduleById(id: String): Schedule? {
        return scheduleDao.getScheduleById(id)?.toModel()
    }
    
    suspend fun insertSchedule(schedule: Schedule) {
        scheduleDao.insertSchedule(schedule.toEntity())
    }
    
    suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }
    
    suspend fun deleteSchedule(schedule: Schedule) {
        scheduleDao.deleteSchedule(schedule.toEntity())
    }
    
    suspend fun getScheduleCount(): Int {
        return scheduleDao.getScheduleCount()
    }
    
    // ========== 课程操作 ==========
    
    fun getCoursesBySchedule(scheduleId: String): Flow<List<Course>> {
        return courseDao.getCoursesBySchedule(scheduleId).map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    suspend fun getCourseById(id: String): Course? {
        return courseDao.getCourseById(id)?.toModel()
    }
    
    suspend fun insertCourse(course: Course, scheduleId: String) {
        courseDao.insertCourse(course.toEntity(scheduleId))
    }
    
    suspend fun insertCourses(courses: List<Course>, scheduleId: String) {
        courseDao.insertCourses(courses.map { it.toEntity(scheduleId) })
    }
    
    suspend fun updateCourse(course: Course, scheduleId: String) {
        courseDao.updateCourse(course.toEntity(scheduleId))
    }
    
    suspend fun deleteCourse(course: Course, scheduleId: String) {
        courseDao.deleteCourse(course.toEntity(scheduleId))
    }
    
    suspend fun deleteCoursesBySchedule(scheduleId: String) {
        courseDao.deleteCoursesBySchedule(scheduleId)
    }
    
    // ========== 作息时间操作 ==========
    
    fun getAllSectionTimes(): Flow<List<SectionTime>> {
        return sectionTimeDao.getAllSectionTimes().map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    suspend fun insertSectionTime(sectionTime: SectionTime) {
        sectionTimeDao.insertSectionTime(sectionTime.toEntity())
    }
    
    suspend fun insertSectionTimes(sectionTimes: List<SectionTime>) {
        sectionTimeDao.insertSectionTimes(sectionTimes.map { it.toEntity() })
    }
    
    suspend fun deleteAllSectionTimes() {
        sectionTimeDao.deleteAllSectionTimes()
    }
    
    // ========== 应用设置操作 ==========
    
    fun getSettings(): Flow<AppSettings?> {
        return appSettingsDao.getSettings().map { entity ->
            entity?.toModel()
        }
    }
    
    suspend fun getSettingsOnce(): AppSettings? {
        return appSettingsDao.getSettingsOnce()?.toModel()
    }
    
    suspend fun getCurrentScheduleId(): String {
        return appSettingsDao.getSettingsOnce()?.currentScheduleId ?: ""
    }
    
    suspend fun saveSettings(settings: AppSettings, currentScheduleId: String) {
        appSettingsDao.insertSettings(settings.toEntity(currentScheduleId))
    }
    
    // ========== 初始化数据 ==========
    
    suspend fun initializeDefaultData() {
        if (scheduleDao.getScheduleCount() == 0) {
            // 创建默认课表
            val defaultSchedule = Schedule(name = "默认课表")
            insertSchedule(defaultSchedule)
            
            // 创建默认作息时间
            val defaultTimes = (1..10).map { 
                SectionTime(it, "${7 + it}:00", "${7 + it}:45")
            }
            insertSectionTimes(defaultTimes)
            
            // 创建默认设置
            val defaultSettings = AppSettings().toEntity(defaultSchedule.id)
            appSettingsDao.insertSettings(defaultSettings)
        }
    }
}
