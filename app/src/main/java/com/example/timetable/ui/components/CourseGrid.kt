package com.example.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.SectionTime
import com.example.timetable.utils.ScreenConfig
import com.example.timetable.utils.rememberScreenConfig

// 显示段数据：可能是单课程或冲突组
@Stable
private data class DisplaySegment(
    val startSection: Int,
    val endSection: Int,
    val courses: List<Course>,  // 该时间段内的所有课程
    val topOffset: Dp,
    val height: Dp,
    val isConflict: Boolean
)

/**
 * 将当天的课程按节次分析，生成显示段列表
 * 处理部分时间重叠的情况
 */
private fun buildDisplaySegments(
    dayCourses: List<Course>,
    cellHeightDp: Int
): List<DisplaySegment> {
    if (dayCourses.isEmpty()) return emptyList()
    
    // 找出当天课程覆盖的节次范围
    val minSection = dayCourses.minOf { it.startSection }
    val maxSection = dayCourses.maxOf { it.endSection }
    
    // 为每个节次找出占用的课程
    val sectionToCourses = (minSection..maxSection).associateWith { section ->
        dayCourses.filter { section in it.startSection..it.endSection }
    }
    
    // 合并连续的相同课程组合段
    val segments = mutableListOf<DisplaySegment>()
    var currentStart = minSection
    var currentCourses = sectionToCourses[minSection] ?: emptyList()
    
    for (section in (minSection + 1)..maxSection) {
        val coursesAtSection = sectionToCourses[section] ?: emptyList()
        val sameGroup = coursesAtSection.map { it.id }.toSet() == currentCourses.map { it.id }.toSet()
        
        if (!sameGroup) {
            // 结束当前段，创建 DisplaySegment
            if (currentCourses.isNotEmpty()) {
                segments.add(
                    DisplaySegment(
                        startSection = currentStart,
                        endSection = section - 1,
                        courses = currentCourses,
                        topOffset = ((currentStart - 1) * cellHeightDp).dp,
                        height = ((section - currentStart) * cellHeightDp - 2).dp,
                        isConflict = currentCourses.size > 1
                    )
                )
            }
            currentStart = section
            currentCourses = coursesAtSection
        }
    }
    
    // 添加最后一段
    if (currentCourses.isNotEmpty()) {
        segments.add(
            DisplaySegment(
                startSection = currentStart,
                endSection = maxSection,
                courses = currentCourses,
                topOffset = ((currentStart - 1) * cellHeightDp).dp,
                height = ((maxSection - currentStart + 1) * cellHeightDp - 2).dp,
                isConflict = currentCourses.size > 1
            )
        )
    }
    
    return segments
}

@Composable
fun CourseGrid(
    courses: List<Course>,
    timeSlots: List<SectionTime>,
    currentWeek: Int,
    showWeekends: Boolean,
    weekStartDay: Int,
    cellHeightDp: Int,
    backgroundColor: Color,
    fontColor: Color,
    courseColor: Color,
    onCourseClick: (List<Course>) -> Unit
) {
    val screenConfig = rememberScreenConfig()
    val maxDay = if (showWeekends) 7 else (if (weekStartDay == 0) 6 else 5)
    val cellHeight = cellHeightDp.dp
    val totalHeight = cellHeight * 10
    
    // 根据起始日生成显示的日期顺序
    // 课程的 day: 1=周一, 2=周二, ..., 7=周日
    val dayOrder = if (weekStartDay == 0) {
        // 周日开始: 7, 1, 2, 3, 4, 5, 6
        if (showWeekends) listOf(7, 1, 2, 3, 4, 5, 6) else listOf(7, 1, 2, 3, 4, 5)
    } else {
        // 周一开始: 1, 2, 3, 4, 5, 6, 7
        if (showWeekends) listOf(1, 2, 3, 4, 5, 6, 7) else listOf(1, 2, 3, 4, 5)
    }
    
    // 直接计算每天的显示段，不使用 remember 缓存以确保实时更新
    val segmentsByDay = (1..7).associateWith { day ->
        val dayCourses = courses.filter { it.day == day }
        buildDisplaySegments(dayCourses, cellHeightDp)
    }

    Row(modifier = Modifier.height(totalHeight)) {
        // 时间列
        TimeColumn(
            timeSlots = timeSlots,
            cellHeight = cellHeight,
            backgroundColor = backgroundColor,
            fontColor = fontColor,
            screenConfig = screenConfig
        )
        
        // 课程列 - 按起始日顺序显示
        dayOrder.forEach { day ->
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val segments = segmentsByDay[day] ?: emptyList()
                segments.forEach { segment ->
                    key("${day}_${segment.startSection}_${segment.endSection}") {
                        SegmentCard(
                            segment = segment,
                            courseColor = courseColor,
                            onCourseClick = onCourseClick,
                            screenConfig = screenConfig
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    timeSlots: List<SectionTime>,
    cellHeight: Dp,
    backgroundColor: Color,
    fontColor: Color,
    screenConfig: ScreenConfig
) {
    val bgAlpha = remember(backgroundColor) { backgroundColor.copy(alpha = 0.9f) }
    val fontAlpha = remember(fontColor) { fontColor.copy(alpha = 0.6f) }
    
    Column(modifier = Modifier.width(screenConfig.timeColumnWidth).fillMaxHeight().background(bgAlpha)) {
        for (section in 1..10) {
            val timeSlot = timeSlots.getOrElse(section - 1) { SectionTime(section, "", "") }
            Column(
                modifier = Modifier.height(cellHeight).fillMaxWidth().padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(section.toString(), fontWeight = FontWeight.Bold, fontSize = screenConfig.sectionFontSize.sp, color = fontColor)
                Text(timeSlot.start, fontSize = screenConfig.timeFontSize.sp, color = fontAlpha)
                Text(timeSlot.end, fontSize = screenConfig.timeFontSize.sp, color = fontAlpha)
            }
        }
    }
}

@Composable
private fun SegmentCard(
    segment: DisplaySegment,
    courseColor: Color,
    onCourseClick: (List<Course>) -> Unit,
    screenConfig: ScreenConfig
) {
    val cardColor = if (segment.isConflict) Color(0xFFFFCDD2) else courseColor
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .padding(horizontal = 1.dp, vertical = 1.dp)
            .offset(y = segment.topOffset)
            .height(segment.height)
            .fillMaxWidth()
            .clickable { onCourseClick(segment.courses) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (segment.isConflict) {
                // 冲突显示
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "冲突",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = screenConfig.courseFontSize.sp
                    )
                    // 显示冲突的课程名
                    segment.courses.forEach { course ->
                        Text(
                            course.name,
                            fontSize = screenConfig.courseLocationFontSize.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            lineHeight = (screenConfig.courseLocationFontSize + 1).sp
                        )
                    }
                }
            } else {
                // 单课程正常显示
                val course = segment.courses.first()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        course.name,
                        fontSize = screenConfig.courseFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = (screenConfig.courseFontSize + 1).sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (course.room.isNotBlank()) {
                        Text(
                            "@${course.room}",
                            fontSize = screenConfig.courseLocationFontSize.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
