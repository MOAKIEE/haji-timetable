package com.example.timetable.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作息时间实体
 */
@Entity(tableName = "section_times")
data class SectionTimeEntity(
    @PrimaryKey
    val section: Int,
    val startTime: String,
    val endTime: String
)
