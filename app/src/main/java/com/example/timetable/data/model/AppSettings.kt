package com.example.timetable.data.model

import androidx.compose.ui.graphics.toArgb
import com.example.timetable.utils.getTodayDateString

data class AppSettings(
    var showWeekends: Boolean = true,
    var weekStartDay: Int = 1, // 1=周一, 0=周日
    var semesterStartDate: String = getTodayDateString(),
    var cellHeightDp: Int = 65,
    var backgroundColor: Int = 0xFFFFFFFF.toInt(),
    var fontColor: Int = 0xFF000000.toInt(),
    var totalWeeks: Int = 20,
    var courseColor: Int = 0xFFB3E5FC.toInt()
)
