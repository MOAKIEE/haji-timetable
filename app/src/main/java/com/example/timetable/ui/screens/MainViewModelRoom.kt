package com.example.timetable.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timetable.data.database.TimetableDatabase
import com.example.timetable.data.migration.DataMigration
import com.example.timetable.data.model.AppSettings
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.Schedule
import com.example.timetable.data.model.SectionTime
import com.example.timetable.data.repository.TimetableRepository
import com.example.timetable.utils.UpdateChecker
import com.example.timetable.utils.UpdatePreferences
import com.example.timetable.utils.UpdateResult
import com.example.timetable.utils.calculateCurrentWeek
import com.example.timetable.utils.ImportExportHelper
import com.example.timetable.utils.ImportExportResult
import android.net.Uri
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * MainScreen 的 ViewModel (Room Database 版本)
 * 负责管理课表数据、应用设置和业务逻辑
 */
class MainViewModelRoom(private val repository: TimetableRepository) : ViewModel() {
    
    companion object {
        private const val CURRENT_VERSION = "0.9beta"
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
    
    var showImportExportDialog by mutableStateOf(false)
        private set
    
    var showImportMethodDialog by mutableStateOf(false)
        private set
    
    var showExportMethodDialog by mutableStateOf(false)
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
    
    // 导入导出状态
    var exportedQRCode by mutableStateOf<Bitmap?>(null)
        private set
    
    var importExportMessage by mutableStateOf<String?>(null)
        private set
    
    var importExportSuccess by mutableStateOf(false)
        private set
    
    var importedSchedulesCount by mutableStateOf(0)
        private set
    
    var isImportOperation by mutableStateOf(true)
        private set
    
    var showImportConfirmDialog by mutableStateOf(false)
        private set
    
    var pendingImportData by mutableStateOf<Triple<List<Schedule>, List<SectionTime>, AppSettings>?>(null)
        private set
    
    // 当前课表（包含课程）
    val currentSchedule: Schedule?
        get() = allSchedules.find { it.id == currentScheduleId } ?: allSchedules.firstOrNull()
    
    /**
     * 加载数据
     */
    fun loadData(context: Context) {
        viewModelScope.launch {
            try {
                // 执行数据迁移
                DataMigration.migrateIfNeeded(context)
                
                // 从 Repository 加载数据
                val schedules = repository.getAllSchedules().firstOrNull() ?: emptyList()
                allSchedules.clear()
                allSchedules.addAll(schedules)
                
                val times = repository.getAllSectionTimes().firstOrNull() ?: emptyList()
                timeSlots.clear()
                timeSlots.addAll(times)
                
                val settings = repository.getSettingsOnce()
                if (settings != null) {
                    appSettings = settings
                }
                
                currentScheduleId = repository.getCurrentScheduleId()
                
                // 加载当前课表的课程
                if (currentScheduleId.isNotEmpty()) {
                    val courses = repository.getCoursesBySchedule(currentScheduleId).firstOrNull() ?: emptyList()
                    currentSchedule?.courses?.clear()
                    currentSchedule?.courses?.addAll(courses)
                }
                
                val currentWeek = calculateCurrentWeek(appSettings.semesterStartDate)
                initialPage = (currentWeek - 1).coerceIn(0, appSettings.totalWeeks - 1)
                
            } catch (e: Exception) {
                // 错误处理
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * 保存数据
     */
    fun saveData() {
        viewModelScope.launch {
            try {
                // 保存设置
                repository.saveSettings(appSettings, currentScheduleId)
                
                // 保存当前课表的课程
                currentSchedule?.let { schedule ->
                    // 删除旧课程并插入新课程
                    repository.deleteCoursesBySchedule(schedule.id)
                    if (schedule.courses.isNotEmpty()) {
                        repository.insertCourses(schedule.courses, schedule.id)
                    }
                }
            } catch (e: Exception) {
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
                return@launch
            }
            
            if (!isManual) {
                delay(1000) // 自动检查延迟1秒
            }
            
            val result = UpdateChecker.checkForUpdate()
            
            if (!isManual) {
                UpdatePreferences.markUpdateChecked(context)
            }
            
            when (result) {
                is UpdateResult.Available -> {
                    if (isManual) {
                        UpdatePreferences.clearIgnoredVersion(context)
                        autoUpdateDialogState = UpdateDialogState.Available(result.releaseInfo)
                    } else {
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
    
    fun openImportExportDialog() {
        showImportExportDialog = true
    }
    
    fun closeImportExportDialog() {
        showImportExportDialog = false
    }
    
    fun openImportMethodDialog() {
        showImportMethodDialog = true
    }
    
    fun closeImportMethodDialog() {
        showImportMethodDialog = false
    }
    
    fun openExportMethodDialog() {
        showExportMethodDialog = true
    }
    
    fun closeExportMethodDialog() {
        showExportMethodDialog = false
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
        viewModelScope.launch {
            // 加载新课表的课程
            val courses = repository.getCoursesBySchedule(scheduleId).firstOrNull() ?: emptyList()
            currentSchedule?.courses?.clear()
            currentSchedule?.courses?.addAll(courses)
        }
    }
    
    fun createSchedule(name: String) {
        viewModelScope.launch {
            val newSchedule = Schedule(name = name)
            repository.insertSchedule(newSchedule)
            allSchedules.add(newSchedule)
            currentScheduleId = newSchedule.id
        }
    }
    
    fun renameSchedule(schedule: Schedule, newName: String) {
        schedule.name = newName
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }
    
    fun deleteSchedule(schedule: Schedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            allSchedules.remove(schedule)
            if (currentScheduleId == schedule.id && allSchedules.isNotEmpty()) {
                currentScheduleId = allSchedules[0].id
            }
        }
    }
    
    fun addOrUpdateCourse(course: Course, isEditing: Boolean) {
        currentSchedule?.let { schedule ->
            if (isEditing) {
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
    
    // 导出为 JSON 文件
    fun exportToJson(context: Context) {
        viewModelScope.launch {
            try {
                val current = currentSchedule ?: return@launch
                val jsonData = ImportExportHelper.exportToJson(
                    schedules = listOf(current),
                    sectionTimes = timeSlots.toList(),
                    settings = appSettings,
                    appVersion = CURRENT_VERSION
                )
                
                val result = ImportExportHelper.saveJsonToFile(context, jsonData)
                when (result) {
                    is ImportExportResult.Success -> {
                        importExportMessage = result.message
                        importExportSuccess = true
                    }
                    is ImportExportResult.Error -> {
                        importExportMessage = result.message
                        importExportSuccess = false
                    }
                }
            } catch (e: Exception) {
                importExportMessage = "导出失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
    
    // 导出到指定文件（用户选择保存位置）
    fun exportToFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val current = currentSchedule ?: return@launch
                val jsonData = ImportExportHelper.exportToJson(
                    schedules = listOf(current),
                    sectionTimes = timeSlots.toList(),
                    settings = appSettings,
                    appVersion = CURRENT_VERSION
                )
                
                // 写入用户选择的文件
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonData.toByteArray())
                }
                
                isImportOperation = false
                importExportMessage = "导出成功"
                importExportSuccess = true
            } catch (e: Exception) {
                importExportMessage = "导出失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
    
    // 生成二维码
    fun exportToQRCode(context: Context) {
        viewModelScope.launch {
            try {
                val current = currentSchedule ?: return@launch
                val jsonData = ImportExportHelper.exportToJson(
                    schedules = listOf(current),
                    sectionTimes = timeSlots.toList(),
                    settings = appSettings,
                    appVersion = CURRENT_VERSION
                )
                
                val qrCode = ImportExportHelper.generateQRCode(jsonData, 800)
                if (qrCode != null) {
                    exportedQRCode = qrCode
                } else {
                    importExportMessage = "二维码生成失败"
                    importExportSuccess = false
                }
            } catch (e: Exception) {
                importExportMessage = "二维码生成失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
    
    // 从文件导入
    fun importFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonData = ImportExportHelper.readJsonFromFile(context, uri)
                val (exportData, result) = ImportExportHelper.importFromJson(jsonData)
                
                when (result) {
                    is ImportExportResult.Success -> {
                        // 转换数据
                        val (newSchedules, newSectionTimes, newSettings) = 
                            ImportExportHelper.convertToAppModels(exportData)
                        
                        // 保存待导入数据并显示确认对话框
                        pendingImportData = Triple(newSchedules, newSectionTimes, newSettings)
                        importedSchedulesCount = newSchedules.size
                        showImportConfirmDialog = true
                    }
                    is ImportExportResult.Error -> {
                        importExportMessage = result.message
                        importExportSuccess = false
                    }
                }
            } catch (e: Exception) {
                importExportMessage = "导入失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
    
    // 从二维码导入
    fun importFromQRCode(context: Context, qrContent: String) {
        viewModelScope.launch {
            try {
                val (exportData, result) = ImportExportHelper.importFromJson(qrContent)
                
                when (result) {
                    is ImportExportResult.Success -> {
                        // 转换数据
                        val (newSchedules, newSectionTimes, newSettings) = 
                            ImportExportHelper.convertToAppModels(exportData)
                        
                        // 保存待导入数据并显示确认对话框
                        pendingImportData = Triple(newSchedules, newSectionTimes, newSettings)
                        importedSchedulesCount = newSchedules.size
                        showImportConfirmDialog = true
                    }
                    is ImportExportResult.Error -> {
                        importExportMessage = result.message
                        importExportSuccess = false
                    }
                }
            } catch (e: Exception) {
                importExportMessage = "导入失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
    
    fun clearImportExportMessage() {
        importExportMessage = null
        exportedQRCode = null
    }
    
    fun showPermissionDeniedMessage() {
        importExportMessage = "需要相机权限才能扫描二维码"
        importExportSuccess = false
    }
    
    fun closeImportConfirmDialog() {
        showImportConfirmDialog = false
        pendingImportData = null
    }
    
    // 覆盖导入 - 覆盖当前课表
    fun confirmImportOverwrite(context: Context) {
        viewModelScope.launch {
            try {
                val data = pendingImportData ?: return@launch
                val current = currentSchedule ?: return@launch
                val (newSchedules, newSectionTimes, _) = data
                val importSchedule = newSchedules.firstOrNull() ?: return@launch
                
                // 删除当前课表的所有课程
                repository.deleteCoursesBySchedule(current.id)
                
                // 更新时间段
                repository.deleteAllSectionTimes()
                newSectionTimes.forEach { repository.insertSectionTime(it) }
                
                // 导入课程到当前课表
                importSchedule.courses.forEach { course ->
                    repository.insertCourse(course, current.id)
                }
                
                // 重新加载数据
                loadData(context)
                
                isImportOperation = true
                importExportMessage = "已覆盖当前课表"
                importExportSuccess = true
                showImportConfirmDialog = false
                pendingImportData = null
            } catch (e: Exception) {
                isImportOperation = true
                importExportMessage = "导入失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
    
    // 新建导入 - 创建新课表
    fun confirmImportAsNew(context: Context, newName: String) {
        viewModelScope.launch {
            try {
                val data = pendingImportData ?: return@launch
                val (newSchedules, newSectionTimes, _) = data
                val importSchedule = newSchedules.firstOrNull() ?: return@launch
                
                // 检查是否需要导入时间段（如果当前没有时间段）
                if (timeSlots.isEmpty()) {
                    newSectionTimes.forEach { repository.insertSectionTime(it) }
                }
                
                // 创建新课表
                val finalName = newName.ifBlank { "${importSchedule.name} (导入)" }
                val newSchedule = Schedule(
                    id = java.util.UUID.randomUUID().toString(),
                    name = finalName
                )
                repository.insertSchedule(newSchedule)
                
                // 导入课程到新课表
                importSchedule.courses.forEach { course ->
                    repository.insertCourse(course, newSchedule.id)
                }
                
                // 重新加载数据
                loadData(context)
                
                isImportOperation = true
                importExportMessage = "已创建新课表: $finalName"
                importExportSuccess = true
                showImportConfirmDialog = false
                pendingImportData = null
            } catch (e: Exception) {
                isImportOperation = true
                importExportMessage = "导入失败: ${e.message}"
                importExportSuccess = false
            }
        }
    }
}

// 工厂函数
fun createMainViewModel(context: Context): MainViewModelRoom {
    val database = TimetableDatabase.getDatabase(context)
    val repository = TimetableRepository(database)
    return MainViewModelRoom(repository)
}
