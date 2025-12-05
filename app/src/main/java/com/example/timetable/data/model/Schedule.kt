package com.example.timetable.data.model

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

class Schedule(
    val id: String = UUID.randomUUID().toString(),
    var name: String
) {
    val courses = mutableStateListOf<Course>()
}
