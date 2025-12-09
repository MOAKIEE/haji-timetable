package com.example.timetable.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 课程实体
 */
@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class CourseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val scheduleId: String,
    val name: String,
    val room: String,
    val teacher: String,
    val day: Int,
    val startSection: Int,
    val endSection: Int,
    val startWeek: Int,
    val endWeek: Int,
    val colorArgb: Int
)
