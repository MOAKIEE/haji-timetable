package com.example.timetable.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 应用设置实体
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Int = 1, // 单例设置，固定 ID
    val currentScheduleId: String = "",
    val showWeekends: Boolean = true,
    val weekStartDay: Int = 1,
    val semesterStartDate: String = "",
    val cellHeightDp: Int = 65,
    val backgroundColor: Int = 0xFF6200EE.toInt(),
    val fontColor: Int = 0xFFFFFFFF.toInt(),
    val totalWeeks: Int = 20,
    val courseColor: Int = 0xFFE8F5E9.toInt()
)
