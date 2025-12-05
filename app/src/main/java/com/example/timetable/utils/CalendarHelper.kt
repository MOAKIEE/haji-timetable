package com.example.timetable.utils

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.SectionTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object CalendarHelper {
    
    /**
     * 获取设备上可用的日历账户列表
     */
    fun getAvailableCalendars(context: Context): List<Pair<Long, String>> {
        val calendars = mutableListOf<Pair<Long, String>>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        
        try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                "${CalendarContract.Calendars.VISIBLE} = 1",
                null,
                null
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val nameIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accountIndex = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIndex)
                    val name = cursor.getString(nameIndex) ?: "未知日历"
                    val account = cursor.getString(accountIndex) ?: ""
                    calendars.add(id to "$name ($account)")
                }
            }
        } catch (e: SecurityException) {
            // 权限未授予
        }
        return calendars
    }
    
    /**
     * 同步课程到日历
     * @param context Context
     * @param calendarId 目标日历ID
     * @param courses 要同步的课程列表
     * @param timeSlots 时间段配置
     * @param semesterStartDate 学期开始日期 (yyyy-MM-dd)
     * @param startWeek 同步起始周
     * @param endWeek 同步结束周
     * @param enableReminder 是否开启闹钟提醒
     * @param reminderMinutes 提前多少分钟提醒
     * @return 同步成功的事件数量，-1表示失败
     */
    fun syncCoursesToCalendar(
        context: Context,
        calendarId: Long,
        courses: List<Course>,
        timeSlots: List<SectionTime>,
        semesterStartDate: String,
        startWeek: Int,
        endWeek: Int,
        enableReminder: Boolean = false,
        reminderMinutes: Int = 15
    ): Int {
        if (calendarId < 0 || courses.isEmpty() || timeSlots.isEmpty()) return 0
        
        var syncedCount = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeZone = TimeZone.getDefault()
        
        try {
            val semesterStart = sdf.parse(semesterStartDate) ?: return -1
            val calendar = Calendar.getInstance()
            
            for (course in courses) {
                // 计算课程在指定周范围内的实际周
                val courseStartWeek = maxOf(course.startWeek, startWeek)
                val courseEndWeek = minOf(course.endWeek, endWeek)
                
                if (courseStartWeek > courseEndWeek) continue
                
                // 获取课程对应的时间段
                val startTimeSlot = timeSlots.getOrNull(course.startSection - 1) ?: continue
                val endTimeSlot = timeSlots.getOrNull(course.endSection - 1) ?: continue
                
                // 解析时间
                val startTimeParts = startTimeSlot.start.split(":")
                val endTimeParts = endTimeSlot.end.split(":")
                if (startTimeParts.size != 2 || endTimeParts.size != 2) continue
                
                val startHour = startTimeParts[0].toIntOrNull() ?: continue
                val startMinute = startTimeParts[1].toIntOrNull() ?: continue
                val endHour = endTimeParts[0].toIntOrNull() ?: continue
                val endMinute = endTimeParts[1].toIntOrNull() ?: continue
                
                // 为每一周创建事件
                for (week in courseStartWeek..courseEndWeek) {
                    calendar.time = semesterStart
                    // 计算到该周对应日期的天数
                    // 学期第一天是周一，course.day: 1=周一, 7=周日
                    val daysToAdd = (week - 1) * 7 + (course.day - 1)
                    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    
                    // 设置开始时间
                    val eventStartCalendar = calendar.clone() as Calendar
                    eventStartCalendar.set(Calendar.HOUR_OF_DAY, startHour)
                    eventStartCalendar.set(Calendar.MINUTE, startMinute)
                    eventStartCalendar.set(Calendar.SECOND, 0)
                    eventStartCalendar.set(Calendar.MILLISECOND, 0)
                    
                    // 设置结束时间
                    val eventEndCalendar = calendar.clone() as Calendar
                    eventEndCalendar.set(Calendar.HOUR_OF_DAY, endHour)
                    eventEndCalendar.set(Calendar.MINUTE, endMinute)
                    eventEndCalendar.set(Calendar.SECOND, 0)
                    eventEndCalendar.set(Calendar.MILLISECOND, 0)
                    
                    // 重置calendar为学期开始日期，以便下次循环使用
                    calendar.time = semesterStart
                    
                    // 创建日历事件
                    val values = ContentValues().apply {
                        put(CalendarContract.Events.CALENDAR_ID, calendarId)
                        put(CalendarContract.Events.TITLE, course.name)
                        put(CalendarContract.Events.DESCRIPTION, buildDescription(course))
                        put(CalendarContract.Events.EVENT_LOCATION, course.room)
                        put(CalendarContract.Events.DTSTART, eventStartCalendar.timeInMillis)
                        put(CalendarContract.Events.DTEND, eventEndCalendar.timeInMillis)
                        put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.id)
                        put(CalendarContract.Events.HAS_ALARM, if (enableReminder) 1 else 0)
                    }
                    
                    val eventUri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                    if (eventUri != null) {
                        syncedCount++
                        
                        // 如果开启提醒，添加闹钟
                        if (enableReminder) {
                            val eventId = eventUri.lastPathSegment?.toLongOrNull()
                            if (eventId != null) {
                                // 尝试多种提醒方式以确保兼容性
                                val reminderMethods = listOf(
                                    CalendarContract.Reminders.METHOD_ALERT,
                                    CalendarContract.Reminders.METHOD_DEFAULT
                                )
                                
                                for (method in reminderMethods) {
                                    try {
                                        val reminderValues = ContentValues().apply {
                                            put(CalendarContract.Reminders.EVENT_ID, eventId)
                                            put(CalendarContract.Reminders.MINUTES, reminderMinutes)
                                            put(CalendarContract.Reminders.METHOD, method)
                                        }
                                        val reminderUri = context.contentResolver.insert(
                                            CalendarContract.Reminders.CONTENT_URI, 
                                            reminderValues
                                        )
                                        if (reminderUri != null) {
                                            break // 成功添加，跳出循环
                                        }
                                    } catch (e: Exception) {
                                        // 尝试下一种方法
                                        continue
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
        
        return syncedCount
    }
    
    private fun buildDescription(course: Course): String {
        val sb = StringBuilder()
        if (course.teacher.isNotBlank()) {
            sb.append("教师: ${course.teacher}\n")
        }
        sb.append("第${course.startSection}-${course.endSection}节\n")
        sb.append("周数: ${course.startWeek}-${course.endWeek}周")
        return sb.toString()
    }
}
