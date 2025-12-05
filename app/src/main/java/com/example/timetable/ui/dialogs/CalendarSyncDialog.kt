package com.example.timetable.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.SectionTime
import com.example.timetable.utils.CalendarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSyncDialog(
    courses: List<Course>,
    timeSlots: List<SectionTime>,
    semesterStartDate: String,
    totalWeeks: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var startWeek by remember { mutableIntStateOf(1) }
    var endWeek by remember { mutableIntStateOf(totalWeeks) }
    var selectedCalendarId by remember { mutableLongStateOf(-1L) }
    var availableCalendars by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var enableReminder by remember { mutableStateOf(false) }
    var reminderMinutes by remember { mutableIntStateOf(15) }
    var reminderMinutesText by remember { mutableStateOf("15") }
    var hasPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    var calendarDropdownExpanded by remember { mutableStateOf(false) }
    
    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.READ_CALENDAR] == true &&
                       permissions[Manifest.permission.WRITE_CALENDAR] == true
        if (hasPermission) {
            availableCalendars = CalendarHelper.getAvailableCalendars(context)
            if (availableCalendars.isNotEmpty()) {
                selectedCalendarId = availableCalendars[0].first
            }
        }
    }
    
    // 加载日历列表
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            availableCalendars = CalendarHelper.getAvailableCalendars(context)
            if (availableCalendars.isNotEmpty()) {
                selectedCalendarId = availableCalendars[0].first
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("同步至日历", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!hasPermission) {
                    // 请求权限界面
                    Text(
                        "需要日历权限才能同步课程",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (availableCalendars.isEmpty()) {
                    Text(
                        "未找到可用的日历账户，请先在设备上设置日历账户",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // 选择日历
                    Text("选择日历", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    ExposedDropdownMenuBox(
                        expanded = calendarDropdownExpanded,
                        onExpandedChange = { calendarDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = availableCalendars.find { it.first == selectedCalendarId }?.second ?: "请选择",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calendarDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = calendarDropdownExpanded,
                            onDismissRequest = { calendarDropdownExpanded = false }
                        ) {
                            availableCalendars.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name, maxLines = 1) },
                                    onClick = {
                                        selectedCalendarId = id
                                        calendarDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // 周范围选择
                    Text("同步范围", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("第")
                        WeekSelector(
                            value = startWeek,
                            totalWeeks = totalWeeks,
                            onValueChange = { 
                                startWeek = it
                                if (endWeek < it) endWeek = it
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Text("周 至 第")
                        WeekSelector(
                            value = endWeek,
                            totalWeeks = totalWeeks,
                            onValueChange = { 
                                endWeek = it
                                if (startWeek > it) startWeek = it
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Text("周")
                    }
                    
                    // 闹钟提醒选项
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("开启闹钟提醒", fontSize = 14.sp)
                        Switch(
                            checked = enableReminder,
                            onCheckedChange = { enableReminder = it }
                        )
                    }
                    
                    // 提醒时间选择
                    if (enableReminder) {
                        Text(
                            "由于日历应用差异，闹钟提醒可能不生效",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("提前", fontSize = 14.sp)
                            OutlinedTextField(
                                value = reminderMinutesText,
                                onValueChange = { input ->
                                    val filtered = input.filter { it.isDigit() }
                                    reminderMinutesText = filtered
                                    val num = filtered.toIntOrNull()
                                    if (num != null && num > 0) {
                                        reminderMinutes = num.coerceIn(1, 1440)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Text("分钟提醒", fontSize = 14.sp)
                        }
                    }
                    
                    // 课程数量提示
                    val coursesInRange = courses.filter { course ->
                        course.startWeek <= endWeek && course.endWeek >= startWeek
                    }
                    Text(
                        "将同步 ${coursesInRange.size} 门课程",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            if (!hasPermission) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            )
                        )
                    }
                ) {
                    Text("授予权限")
                }
            } else if (hasPermission && availableCalendars.isNotEmpty()) {
                Button(
                    onClick = {
                        if (selectedCalendarId >= 0) {
                            isLoading = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    CalendarHelper.syncCoursesToCalendar(
                                        context = context,
                                        calendarId = selectedCalendarId,
                                        courses = courses,
                                        timeSlots = timeSlots,
                                        semesterStartDate = semesterStartDate,
                                        startWeek = startWeek,
                                        endWeek = endWeek,
                                        enableReminder = enableReminder,
                                        reminderMinutes = reminderMinutes
                                    )
                                }
                                isLoading = false
                                when {
                                    result > 0 -> {
                                        Toast.makeText(context, "成功同步 $result 个课程事件", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                    result == 0 -> {
                                        Toast.makeText(context, "没有需要同步的课程", Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {
                                        Toast.makeText(context, "同步失败，请重试", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading && selectedCalendarId >= 0
                ) {
                    Text(if (isLoading) "同步中..." else "同步")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekSelector(
    value: Int,
    totalWeeks: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value.toString(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..totalWeeks).forEach { week ->
                DropdownMenuItem(
                    text = { Text(week.toString()) },
                    onClick = {
                        onValueChange(week)
                        expanded = false
                    }
                )
            }
        }
    }
}
