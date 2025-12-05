package com.example.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeekHeader(showWeekends: Boolean, dates: List<String>, backgroundColor: Color, fontColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().background(backgroundColor).padding(vertical = 8.dp)) {
        Spacer(modifier = Modifier.width(40.dp))
        val days = if (showWeekends) listOf("一", "二", "三", "四", "五", "六", "日") else listOf("一", "二", "三", "四", "五")

        days.forEachIndexed { index, day ->
            val dateStr = dates.getOrElse(index) { "" }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(day, color = fontColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(dateStr, color = fontColor.copy(alpha = 0.7f), fontSize = 10.sp)
            }
        }
    }
}
