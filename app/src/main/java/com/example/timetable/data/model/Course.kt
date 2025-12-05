package com.example.timetable.data.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val room: String,
    val teacher: String,
    val day: Int,
    val startSection: Int,
    val endSection: Int,
    val startWeek: Int,
    val endWeek: Int,
    val color: Color
)
