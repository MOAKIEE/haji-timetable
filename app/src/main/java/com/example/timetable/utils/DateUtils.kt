package com.example.timetable.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun calculateCurrentWeek(startDateStr: String): Int {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val start = sdf.parse(startDateStr) ?: return 1
        val now = Date()
        val diff = now.time - start.time
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return (days / 7).toInt() + 1
    } catch (e: Exception) {
        return 1
    }
}

fun getWeekDates(startDateStr: String, weekIndex: Int): List<String> {
    val list = mutableListOf<String>()
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val start = sdf.parse(startDateStr)
        val calendar = Calendar.getInstance()
        if (start != null) {
            calendar.time = start
            calendar.add(Calendar.DAY_OF_YEAR, (weekIndex - 1) * 7)
            for (i in 0..6) {
                list.add(displayFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    } catch (e: Exception) {
        for(i in 0..6) list.add("--")
    }
    return list
}

fun getDayName(day: Int): String = listOf("", "一", "二", "三", "四", "五", "六", "日").getOrElse(day) { "?" }
