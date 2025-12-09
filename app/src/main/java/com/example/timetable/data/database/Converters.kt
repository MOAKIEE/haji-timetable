package com.example.timetable.data.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.timetable.data.database.entity.*
import com.example.timetable.data.model.*

/**
 * Entity 和 Model 之间的转换工具
 */

// Schedule 转换
fun ScheduleEntity.toModel(): Schedule {
    return Schedule(id = id, name = name)
}

fun Schedule.toEntity(): ScheduleEntity {
    return ScheduleEntity(id = id, name = name)
}

// Course 转换
fun CourseEntity.toModel(): Course {
    return Course(
        id = id,
        name = name,
        room = room,
        teacher = teacher,
        day = day,
        startSection = startSection,
        endSection = endSection,
        startWeek = startWeek,
        endWeek = endWeek,
        color = Color(colorArgb)
    )
}

fun Course.toEntity(scheduleId: String): CourseEntity {
    return CourseEntity(
        id = id,
        scheduleId = scheduleId,
        name = name,
        room = room,
        teacher = teacher,
        day = day,
        startSection = startSection,
        endSection = endSection,
        startWeek = startWeek,
        endWeek = endWeek,
        colorArgb = color.toArgb()
    )
}

// SectionTime 转换
fun SectionTimeEntity.toModel(): SectionTime {
    return SectionTime(
        section = section,
        start = startTime,
        end = endTime
    )
}

fun SectionTime.toEntity(): SectionTimeEntity {
    return SectionTimeEntity(
        section = section,
        startTime = start,
        endTime = end
    )
}

// AppSettings 转换
fun AppSettingsEntity.toModel(): AppSettings {
    return AppSettings(
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

fun AppSettings.toEntity(currentScheduleId: String): AppSettingsEntity {
    return AppSettingsEntity(
        currentScheduleId = currentScheduleId,
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
