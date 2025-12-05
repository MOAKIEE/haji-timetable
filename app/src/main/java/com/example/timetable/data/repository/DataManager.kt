package com.example.timetable.data.repository

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.timetable.data.model.AppSettings
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.Schedule
import com.example.timetable.data.model.SectionTime
import com.example.timetable.utils.getTodayDateString
import org.json.JSONArray
import org.json.JSONObject

object DataManager {
    private const val PREF_NAME = "TimetableData_V4"
    private const val KEY_DATA = "JsonData"

    fun save(context: Context, schedules: List<Schedule>, timeSlots: List<SectionTime>, currentId: String, settings: AppSettings) {
        val root = JSONObject()
        root.put("currentId", currentId)
        root.put("showWeekends", settings.showWeekends)
        root.put("semesterStartDate", settings.semesterStartDate)
        root.put("cellHeightDp", settings.cellHeightDp)
        root.put("backgroundColor", settings.backgroundColor)
        root.put("fontColor", settings.fontColor)
        root.put("totalWeeks", settings.totalWeeks)
        root.put("courseColor", settings.courseColor)

        val timesArray = JSONArray()
        timeSlots.forEach { slot ->
            val tObj = JSONObject().apply {
                put("s", slot.section)
                put("st", slot.start)
                put("et", slot.end)
            }
            timesArray.put(tObj)
        }
        root.put("times", timesArray)

        val schedulesArray = JSONArray()
        schedules.forEach { sch ->
            val sObj = JSONObject()
            sObj.put("id", sch.id)
            sObj.put("name", sch.name)
            val courseArray = JSONArray()
            sch.courses.forEach { c ->
                val cObj = JSONObject().apply {
                    put("id", c.id)
                    put("n", c.name)
                    put("r", c.room)
                    put("t", c.teacher)
                    put("d", c.day)
                    put("ss", c.startSection)
                    put("es", c.endSection)
                    put("sw", c.startWeek)
                    put("ew", c.endWeek)
                    put("c", c.color.toArgb())
                }
                courseArray.put(cObj)
            }
            sObj.put("courses", courseArray)
            schedulesArray.put(sObj)
        }
        root.put("schedules", schedulesArray)

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(KEY_DATA, root.toString()).apply()
    }

    fun load(context: Context): Triple<List<Schedule>, List<SectionTime>, Triple<String, AppSettings, Unit>>? {
        val jsonStr = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_DATA, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val currentId = root.optString("currentId", "")

            val settings = AppSettings(
                showWeekends = root.optBoolean("showWeekends", true),
                semesterStartDate = root.optString("semesterStartDate", getTodayDateString()),
                cellHeightDp = root.optInt("cellHeightDp", 65),
                backgroundColor = root.optInt("backgroundColor", 0xFF6200EE.toInt()),
                fontColor = root.optInt("fontColor", 0xFFFFFFFF.toInt()),
                totalWeeks = root.optInt("totalWeeks", 20),
                courseColor = root.optInt("courseColor", 0xFFE8F5E9.toInt())
            )

            val loadedTimes = mutableListOf<SectionTime>()
            val timesArray = root.optJSONArray("times")
            if (timesArray != null) {
                for (i in 0 until timesArray.length()) {
                    val t = timesArray.getJSONObject(i)
                    loadedTimes.add(SectionTime(t.getInt("s"), t.getString("st"), t.getString("et")))
                }
            }

            val loadedSchedules = mutableListOf<Schedule>()
            val sArray = root.getJSONArray("schedules")
            for (i in 0 until sArray.length()) {
                val sObj = sArray.getJSONObject(i)
                val schedule = Schedule(sObj.getString("id"), sObj.getString("name"))
                val cArray = sObj.optJSONArray("courses")
                if (cArray != null) {
                    for (j in 0 until cArray.length()) {
                        val c = cArray.getJSONObject(j)
                        val startSec = c.optInt("ss", c.optInt("s", 1))
                        val endSec = c.optInt("es", startSec)
                        schedule.courses.add(Course(
                            id = c.getString("id"),
                            name = c.getString("n"),
                            room = c.optString("r", ""),
                            teacher = c.optString("t", ""),
                            day = c.getInt("d"),
                            startSection = startSec,
                            endSection = endSec,
                            startWeek = c.optInt("sw", 1),
                            endWeek = c.optInt("ew", 20),
                            color = Color(c.getInt("c"))
                        ))
                    }
                }
                loadedSchedules.add(schedule)
            }
            Triple(loadedSchedules, loadedTimes, Triple(currentId, settings, Unit))
        } catch (e: Exception) { e.printStackTrace(); null }
    }
}
