package com.example.timetable.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.example.timetable.data.model.AppSettings
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.Schedule
import com.example.timetable.data.model.SectionTime
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 导入导出数据格式
 */
data class ExportData(
    @SerializedName("version")
    val version: String = "1.0",
    
    @SerializedName("appVersion")
    val appVersion: String,
    
    @SerializedName("exportDate")
    val exportDate: String,
    
    @SerializedName("schedules")
    val schedules: List<ExportSchedule>,
    
    @SerializedName("sectionTimes")
    val sectionTimes: List<ExportSectionTime>,
    
    @SerializedName("settings")
    val settings: ExportSettings
)

data class ExportSchedule(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("courses")
    val courses: List<ExportCourse>
)

data class ExportCourse(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("room")
    val room: String,
    
    @SerializedName("teacher")
    val teacher: String,
    
    @SerializedName("day")
    val day: Int,
    
    @SerializedName("startSection")
    val startSection: Int,
    
    @SerializedName("endSection")
    val endSection: Int,
    
    @SerializedName("startWeek")
    val startWeek: Int,
    
    @SerializedName("endWeek")
    val endWeek: Int,
    
    @SerializedName("color")
    val color: Long
)

data class ExportSectionTime(
    @SerializedName("section")
    val section: Int,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String
)

data class ExportSettings(
    @SerializedName("showWeekends")
    val showWeekends: Boolean,
    
    @SerializedName("weekStartDay")
    val weekStartDay: Int,
    
    @SerializedName("semesterStartDate")
    val semesterStartDate: String,
    
    @SerializedName("cellHeightDp")
    val cellHeightDp: Int,
    
    @SerializedName("backgroundColor")
    val backgroundColor: Int,
    
    @SerializedName("fontColor")
    val fontColor: Int,
    
    @SerializedName("totalWeeks")
    val totalWeeks: Int,
    
    @SerializedName("courseColor")
    val courseColor: Int
)

/**
 * 导入导出结果
 */
sealed class ImportExportResult {
    data class Success(val message: String) : ImportExportResult()
    data class Error(val message: String) : ImportExportResult()
}

/**
 * 导入导出工具类
 */
object ImportExportHelper {
    
    private val gson = Gson()
    private const val EXPORT_VERSION = "1.0"
    
    /**
     * 导出为 JSON 字符串
     */
    suspend fun exportToJson(
        schedules: List<Schedule>,
        sectionTimes: List<SectionTime>,
        settings: AppSettings,
        appVersion: String
    ): String = withContext(Dispatchers.IO) {
        val exportData = ExportData(
            version = EXPORT_VERSION,
            appVersion = appVersion,
            exportDate = getCurrentDateTime(),
            schedules = schedules.map { schedule ->
                ExportSchedule(
                    id = schedule.id,
                    name = schedule.name,
                    courses = schedule.courses.map { course ->
                        ExportCourse(
                            name = course.name,
                            room = course.room,
                            teacher = course.teacher,
                            day = course.day,
                            startSection = course.startSection,
                            endSection = course.endSection,
                            startWeek = course.startWeek,
                            endWeek = course.endWeek,
                            color = course.color.value.toLong()
                        )
                    }
                )
            },
            sectionTimes = sectionTimes.map { time ->
                ExportSectionTime(
                    section = time.section,
                    startTime = time.start,
                    endTime = time.end
                )
            },
            settings = ExportSettings(
                showWeekends = settings.showWeekends,
                weekStartDay = settings.weekStartDay,
                semesterStartDate = settings.semesterStartDate,
                cellHeightDp = settings.cellHeightDp,
                backgroundColor = settings.backgroundColor,
                fontColor = settings.fontColor,
                totalWeeks = settings.totalWeeks,
                courseColor = settings.courseColor
            )
        )
        
        gson.toJson(exportData)
    }
    
    /**
     * 从 JSON 字符串导入
     */
    suspend fun importFromJson(json: String): Pair<ExportData, ImportExportResult> = withContext(Dispatchers.IO) {
        try {
            val exportData = gson.fromJson(json, ExportData::class.java)
            
            // 验证数据
            if (exportData.schedules.isEmpty()) {
                return@withContext Pair(exportData, ImportExportResult.Error("数据为空"))
            }
            
            Pair(exportData, ImportExportResult.Success("导入成功"))
        } catch (e: Exception) {
            Pair(ExportData("", "", "", emptyList(), emptyList(), ExportSettings(true, 1, "", 65, 0, 0, 20, 0)), 
                ImportExportResult.Error("解析失败: ${e.message}"))
        }
    }
    
    /**
     * 保存 JSON 到文件
     */
    suspend fun saveJsonToFile(context: Context, json: String): ImportExportResult = withContext(Dispatchers.IO) {
        try {
            val fileName = "timetable_${getCurrentDateTime().replace(":", "-").replace(" ", "_")}.json"
            val downloadsDir = context.getExternalFilesDir(null)
            val file = File(downloadsDir, fileName)
            
            file.writeText(json)
            
            ImportExportResult.Success("已保存至: ${file.absolutePath}")
        } catch (e: Exception) {
            ImportExportResult.Error("保存失败: ${e.message}")
        }
    }
    
    /**
     * 从文件读取 JSON
     */
    suspend fun readJsonFromFile(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw Exception("无法读取文件")
        } catch (e: Exception) {
            throw Exception("读取失败: ${e.message}")
        }
    }
    
    /**
     * 生成二维码
     */
    suspend fun generateQRCode(content: String, size: Int = 512): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val qrCodeWriter = QRCodeWriter()
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 1
            
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取当前日期时间
     */
    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * 转换导入数据为应用数据模型
     */
    fun convertToAppModels(exportData: ExportData): Triple<List<Schedule>, List<SectionTime>, AppSettings> {
        val schedules = exportData.schedules.map { exportSchedule ->
            Schedule(
                id = exportSchedule.id,
                name = exportSchedule.name
            ).apply {
                courses.addAll(exportSchedule.courses.map { exportCourse ->
                    Course(
                        name = exportCourse.name,
                        room = exportCourse.room,
                        teacher = exportCourse.teacher,
                        day = exportCourse.day,
                        startSection = exportCourse.startSection,
                        endSection = exportCourse.endSection,
                        startWeek = exportCourse.startWeek,
                        endWeek = exportCourse.endWeek,
                        color = androidx.compose.ui.graphics.Color(exportCourse.color)
                    )
                })
            }
        }
        
        val sectionTimes = exportData.sectionTimes.map { exportTime ->
            SectionTime(
                section = exportTime.section,
                start = exportTime.startTime,
                end = exportTime.endTime
            )
        }
        
        val settings = with(exportData.settings) {
            AppSettings(
                showWeekends = showWeekends,
                weekStartDay = weekStartDay,
                semesterStartDate = semesterStartDate,
                cellHeightDp = cellHeightDp,
                backgroundColor = backgroundColor,
                fontColor = fontColor,
                totalWeeks = totalWeeks,
                courseColor = courseColor
            )
        }
        
        return Triple(schedules, sectionTimes, settings)
    }
}
