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
import com.example.timetable.utils.rememberScreenConfig

@Composable
fun WeekHeader(
    showWeekends: Boolean,
    weekStartDay: Int,
    dates: List<String>,
    backgroundColor: Color,
    fontColor: Color
) {
    val screenConfig = rememberScreenConfig()
    
    Row(modifier = Modifier.fillMaxWidth().background(backgroundColor).padding(vertical = 8.dp)) {
        Spacer(modifier = Modifier.width(screenConfig.timeColumnWidth))
        
        // 根据起始日调整星期顺序
        val allDays = if (weekStartDay == 0) {
            // 周日开始
            if (showWeekends) listOf("日", "一", "二", "三", "四", "五", "六") else listOf("日", "一", "二", "三", "四", "五")
        } else {
            // 周一开始
            if (showWeekends) listOf("一", "二", "三", "四", "五", "六", "日") else listOf("一", "二", "三", "四", "五")
        }

        allDays.forEachIndexed { index, day ->
            val dateStr = dates.getOrElse(index) { "" }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(day, color = fontColor, fontSize = (screenConfig.sectionFontSize).sp, fontWeight = FontWeight.Bold)
                Text(dateStr, color = fontColor.copy(alpha = 0.7f), fontSize = (screenConfig.timeFontSize + 1).sp)
            }
        }
    }
}
