package com.example.timetable.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.model.AppSettings
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.Schedule
import com.example.timetable.data.model.SectionTime
import com.example.timetable.data.repository.DataManager
import com.example.timetable.utils.UpdateChecker
import com.example.timetable.utils.UpdatePreferences
import com.example.timetable.utils.UpdateResult
import com.example.timetable.utils.calculateCurrentWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MainScreen 的 ViewModel
 * 负责管理课表数据、应用设置和业务逻辑
 */
class MainViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "MainViewModel"
        const val CURRENT_VERSION = "0.9beta"
        private const val AUTO_UPDATE_DELAY_MS = 1000L
        private const val DRAWER_CLOSE_DELAY_MS = 150L
        private const val DEFAULT_SCHEDULE_NAME = "默认课表"
        private const val DEFAULT_TIME_SLOTS = 10
        private const val DEFAULT_START_HOUR = 8
    }
    
    /**
     * 日志输出（仅在 Debug 模式）
     */
    private fun log(message: String, level: LogLevel = LogLevel.DEBUG) {
        // 始终启用日志以便调试
        when (level) {
            LogLevel.DEBUG -> Log.d(TAG, message)
            LogLevel.INFO -> Log.i(TAG, message)
            LogLevel.WARNING -> Log.w(TAG, message)
            LogLevel.ERROR -> Log.e(TAG, message)
        }
    }
    
    private enum class LogLevel {
        DEBUG, INFO, WARNING, ERROR
    }
    
    // 数据状态
    var isLoading by mutableStateOf(true)
        private set
    
    val allSchedules = mutableStateListOf<Schedule>()
    val timeSlots = mutableStateListOf<SectionTime>()
    
    var currentScheduleId by mutableStateOf("")
        private set
    
    var appSettings by mutableStateOf(AppSettings())
        private set
    
    var initialPage by mutableStateOf(0)
        private set
    
    // 对话框状态
    var showCourseDialog by mutableStateOf(false)
        private set
    
    var showDetailDialog by mutableStateOf(false)
        private set
    
    var showConflictDialog by mutableStateOf(false)
        private set
    
    var showSettingsDialog by mutableStateOf(false)
        private set
    
    var showCreateScheduleDialog by mutableStateOf(false)
        private set
    
    var showRenameScheduleDialog by mutableStateOf(false)
        private set
    
    var showDeleteScheduleDialog by mutableStateOf(false)
        private set
    
    var showCalendarSyncDialog by mutableStateOf(false)
        private set
    
    var showAboutDialog by mutableStateOf(false)
        private set
    
    var autoUpdateDialogState by mutableStateOf<UpdateDialogState>(UpdateDialogState.None)
        private set
    
    // 编辑状态
    var editingCourse by mutableStateOf<Course?>(null)
        private set
    
    var viewingCourse by mutableStateOf<Course?>(null)
        private set
    
    var conflictCourses by mutableStateOf<List<Course>>(emptyList())
        private set
    
    var scheduleToRename by mutableStateOf<Schedule?>(null)
        private set
    
    var scheduleToDelete by mutableStateOf<Schedule?>(null)
        private set
    
    // 当前课表
    val currentSchedule: Schedule?
        get() = allSchedules.find { it.id == currentScheduleId } ?: allSchedules.firstOrNull()
    
    /**
     * 加载数据
     */
    fun loadData(context: Context) {
        viewModelScope.launch {
            log("开始加载数据", LogLevel.INFO)
            
            try {
                val data = withContext(Dispatchers.IO) {
                    DataManager.load(context)
                }
                
                if (data != null) {
                    val (savedSchedules, savedTimes, settingsTuple) = data
                    log("加载到 ${savedSchedules.size} 个课表，${savedTimes.size} 个时间段")
                    
                    if (savedSchedules.isNotEmpty()) {
                        allSchedules.addAll(savedSchedules)
                        if (savedSchedules.any { it.id == settingsTuple.first }) {
                            currentScheduleId = settingsTuple.first
                        } else {
                            currentScheduleId = savedSchedules[0].id
                        }
                    }
                    if (savedTimes.isNotEmpty()) {
                        timeSlots.addAll(savedTimes)
                    }
                    appSettings = settingsTuple.second
                    
                    val currentWeek = calculateCurrentWeek(appSettings.semesterStartDate)
                    initialPage = (currentWeek - 1).coerceIn(0, appSettings.totalWeeks - 1)
                    log("当前周数: $currentWeek")
                }
                
                // 如果没有数据，初始化默认值
                if (allSchedules.isEmpty()) {
                    log("初始化默认数据", LogLevel.INFO)
                    val defaultSchedule = Schedule(name = DEFAULT_SCHEDULE_NAME)
                    allSchedules.add(defaultSchedule)
                    currentScheduleId = defaultSchedule.id
                }
                if (timeSlots.isEmpty()) {
                    for (i in 1..DEFAULT_TIME_SLOTS) {
                        timeSlots.add(SectionTime(i, "${DEFAULT_START_HOUR + i - 1}:00", "${DEFAULT_START_HOUR + i - 1}:45"))
                    }
                }
                
                log("数据加载完成", LogLevel.INFO)
            } catch (e: Exception) {
                log("数据加载失败: ${e.message}", LogLevel.ERROR)
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * 保存数据
     */
    fun saveData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                log("开始保存数据")
                DataManager.save(
                    context,
                    allSchedules.toList(),
                    timeSlots.toList(),
                    currentScheduleId,
                    appSettings
                )
                log("数据保存成功")
            } catch (e: Exception) {
                log("数据保存失败: ${e.message}", LogLevel.ERROR)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 检查更新
     */
    fun checkForUpdate(context: Context, isManual: Boolean = false) {
        viewModelScope.launch {
            if (!isManual && !UpdatePreferences.shouldCheckUpdate(context)) {
                log("跳过自动更新检查（今日已检查）")
                return@launch
            }
            
            log("开始检查更新 (${if (isManual) "手动" else "自动"})", LogLevel.INFO)
            
            if (!isManual) {
                delay(AUTO_UPDATE_DELAY_MS)
            }
            
            val result = UpdateChecker.checkForUpdate()
            
            if (!isManual) {
                UpdatePreferences.markUpdateChecked(context)
            }
            
            when (result) {
                is UpdateResult.Available -> {
                    if (isManual) {
                        // 手动检查时清除忽略状态
                        UpdatePreferences.clearIgnoredVersion(context)
                        autoUpdateDialogState = UpdateDialogState.Available(result.releaseInfo)
                    } else {
                        // 自动检查时尊重忽略状态
                        val ignoredVersion = UpdatePreferences.getIgnoredVersion(context)
                        if (ignoredVersion != result.releaseInfo.tagName) {
                            autoUpdateDialogState = UpdateDialogState.Available(result.releaseInfo)
                        }
                    }
                }
                is UpdateResult.NoUpdate -> {
                    if (isManual) {
                        autoUpdateDialogState = UpdateDialogState.NoUpdate
                    }
                }
                is UpdateResult.Error -> {
                    if (isManual) {
                        autoUpdateDialogState = UpdateDialogState.Error(result.message)
                    }
                }
            }
        }
    }
    
    // ========== 对话框控制方法 ==========
    
    fun openCourseDialog(course: Course? = null) {
        editingCourse = course
        showCourseDialog = true
    }
    
    fun closeCourseDialog() {
        showCourseDialog = false
        editingCourse = null
    }
    
    fun openDetailDialog(course: Course) {
        viewingCourse = course
        showDetailDialog = true
    }
    
    fun closeDetailDialog() {
        showDetailDialog = false
        viewingCourse = null
    }
    
    fun openConflictDialog(courses: List<Course>) {
        conflictCourses = courses
        showConflictDialog = true
    }
    
    fun closeConflictDialog() {
        showConflictDialog = false
        conflictCourses = emptyList()
    }
    
    fun openSettingsDialog() {
        showSettingsDialog = true
    }
    
    fun closeSettingsDialog() {
        showSettingsDialog = false
    }
    
    fun openCreateScheduleDialog() {
        showCreateScheduleDialog = true
    }
    
    fun closeCreateScheduleDialog() {
        showCreateScheduleDialog = false
    }
    
    fun openRenameScheduleDialog(schedule: Schedule) {
        scheduleToRename = schedule
        showRenameScheduleDialog = true
    }
    
    fun closeRenameScheduleDialog() {
        showRenameScheduleDialog = false
        scheduleToRename = null
    }
    
    fun openDeleteScheduleDialog(schedule: Schedule) {
        scheduleToDelete = schedule
        showDeleteScheduleDialog = true
    }
    
    fun closeDeleteScheduleDialog() {
        showDeleteScheduleDialog = false
        scheduleToDelete = null
    }
    
    fun openCalendarSyncDialog() {
        showCalendarSyncDialog = true
    }
    
    fun closeCalendarSyncDialog() {
        showCalendarSyncDialog = false
    }
    
    fun openAboutDialog() {
        showAboutDialog = true
    }
    
    fun closeAboutDialog() {
        showAboutDialog = false
    }
    
    fun closeUpdateDialog() {
        autoUpdateDialogState = UpdateDialogState.None
    }
    
    // ========== 数据操作方法 ==========
    
    fun selectSchedule(scheduleId: String) {
        currentScheduleId = scheduleId
    }
    
    fun createSchedule(name: String) {
        log("创建新课表: $name")
        val newSchedule = Schedule(name = name)
        allSchedules.add(newSchedule)
        currentScheduleId = newSchedule.id
    }
    
    fun renameSchedule(schedule: Schedule, newName: String) {
        log("重命名课表: ${schedule.name} -> $newName")
        schedule.name = newName
    }
    
    fun deleteSchedule(schedule: Schedule) {
        log("删除课表: ${schedule.name}")
        allSchedules.remove(schedule)
        if (currentScheduleId == schedule.id && allSchedules.isNotEmpty()) {
            currentScheduleId = allSchedules[0].id
        }
    }
    
    fun addOrUpdateCourse(course: Course, isEditing: Boolean) {
        currentSchedule?.let { schedule ->
            if (isEditing) {
                // 编辑模式：先删除旧课程
                schedule.courses.removeAll { it.id == course.id }
            }
            schedule.courses.add(course)
        }
    }
    
    fun deleteCourse(course: Course) {
        currentSchedule?.courses?.remove(course)
    }
    
    fun updateSettings(newSettings: AppSettings) {
        appSettings = newSettings
    }
    
    fun ignoreUpdate(context: Context, version: String) {
        UpdatePreferences.setIgnoredVersion(context, version)
        closeUpdateDialog()
    }
}

/**
 * 更新对话框状态
 */
sealed class UpdateDialogState {
    object None : UpdateDialogState()
    object NoUpdate : UpdateDialogState()
    data class Available(val releaseInfo: com.example.timetable.utils.ReleaseInfo) : UpdateDialogState()
    data class Error(val message: String) : UpdateDialogState()
}
