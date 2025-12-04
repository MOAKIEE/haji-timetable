package com.example.timetable

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

// --- 1. 数据结构 ---
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

data class SectionTime(
    val section: Int,
    var start: String,
    var end: String
)

class Schedule(
    val id: String = UUID.randomUUID().toString(),
    var name: String
) {
    val courses = mutableStateListOf<Course>()
}

data class AppSettings(
    var showWeekends: Boolean = true,
    var semesterStartDate: String = getTodayDateString(), // 格式 "yyyy-MM-dd"
    var cellHeightDp: Int = 65, // 格子高度，默认65
    var backgroundColor: Int = 0xFFFFFFFF.toInt(), // 背景颜色（白色）
    var fontColor: Int = 0xFF000000.toInt(), // 字体颜色（黑色）
    var totalWeeks: Int = 20, // 学期总周数
    var courseColor: Int = 0xFFB3E5FC.toInt() // 课程方框默认颜色（浅蓝色）
)

// --- 2. 存储工具类 ---
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

// 辅助函数
fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun calculateCurrentWeek(startDateStr: String): Int {
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val start = sdf.parse(startDateStr) ?: return 1
        val now = Date()
        val diff = now.time - start.time
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return (days / 7).toInt() + 1
    } catch (e: Exception) {
        return 1
    }
}

fun getWeekDates(startDateStr: String, weekIndex: Int): List<String> {
    val list = mutableListOf<String>()
    try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val start = sdf.parse(startDateStr)
        val calendar = Calendar.getInstance()
        if (start != null) {
            calendar.time = start
            calendar.add(Calendar.DAY_OF_YEAR, (weekIndex - 1) * 7)
            for (i in 0..6) {
                list.add(displayFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    } catch (e: Exception) {
        for(i in 0..6) list.add("--")
    }
    return list
}

fun getDayName(day: Int): String = listOf("", "一", "二", "三", "四", "五", "六", "日").getOrElse(day) { "?" }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MainScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // --- 状态 ---
    val allSchedules = remember { mutableStateListOf(Schedule(name = "默认课表")) }
    val timeSlots = remember { mutableStateListOf<SectionTime>().apply { for (i in 1..10) add(SectionTime(i, "${7 + i}:00", "${7 + i}:45")) }}
    var currentScheduleId by remember { mutableStateOf(allSchedules[0].id) }
    var appSettings by remember { mutableStateOf(AppSettings()) }

    val pagerState = rememberPagerState(pageCount = { appSettings.totalWeeks })

    // --- 启动加载 ---
    LaunchedEffect(Unit) {
        val data = DataManager.load(context)
        if (data != null) {
            val (savedSchedules, savedTimes, settingsTuple) = data
            if (savedSchedules.isNotEmpty()) { allSchedules.clear(); allSchedules.addAll(savedSchedules) }
            if (savedTimes.isNotEmpty()) { timeSlots.clear(); timeSlots.addAll(savedTimes) }
            if (savedSchedules.any { it.id == settingsTuple.first }) currentScheduleId = settingsTuple.first
            appSettings = settingsTuple.second

            val currentWeek = calculateCurrentWeek(appSettings.semesterStartDate)
            val targetPage = (currentWeek - 1).coerceIn(0, appSettings.totalWeeks - 1)
            pagerState.scrollToPage(targetPage)
        }
    }

    fun saveData() {
        DataManager.save(context, allSchedules, timeSlots, currentScheduleId, appSettings)
    }

    val currentSchedule = allSchedules.find { it.id == currentScheduleId } ?: allSchedules[0]
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- 弹窗状态 ---
    var showCourseDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCreateScheduleDialog by remember { mutableStateOf(false) }
    var showRenameScheduleDialog by remember { mutableStateOf(false) }

    var editingCourse by remember { mutableStateOf<Course?>(null) }
    var viewingCourse by remember { mutableStateOf<Course?>(null) }
    var conflictCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var scheduleToRename by remember { mutableStateOf<Schedule?>(null) }

    // 主界面和设置界面的切换动画
    AnimatedContent(
        targetState = showSettingsDialog,
        transitionSpec = {
            if (targetState) {
                // 进入设置：从右边滑入
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            } else {
                // 返回主界面：从左边滑入
                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            }
        },
        label = "main_settings_transition"
    ) { isSettings ->
        if (isSettings) {
            SettingsScreen(
                timeSlots = timeSlots,
                settings = appSettings,
                onSettingsChange = { newSettings -> appSettings = newSettings },
                onBack = { saveData(); showSettingsDialog = false }
            )
        } else {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.67f),
                drawerContainerColor = Color(appSettings.backgroundColor)
            ) {
                Spacer(Modifier.height(16.dp))
                Text("课表", modifier = Modifier.padding(16.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(appSettings.fontColor))
                HorizontalDivider(color = Color(appSettings.fontColor).copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                allSchedules.forEach { schedule ->
                    NavigationDrawerItem(
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(schedule.name, modifier = Modifier.weight(1f), color = Color(appSettings.fontColor))
                                IconButton(onClick = {
                                    scheduleToRename = schedule
                                    showRenameScheduleDialog = true
                                }) { Icon(Icons.Default.Edit, "重命名", modifier = Modifier.size(16.dp), tint = Color(appSettings.fontColor)) }
                                // 只有多于一个课表时才显示删除按钮
                                if (allSchedules.size > 1) {
                                    IconButton(onClick = {
                                        // 如果删除的是当前选中的课表，切换到第一个课表
                                        if (schedule.id == currentScheduleId) {
                                            val newCurrent = allSchedules.firstOrNull { it.id != schedule.id }
                                            if (newCurrent != null) currentScheduleId = newCurrent.id
                                        }
                                        allSchedules.remove(schedule)
                                        saveData()
                                    }) { Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(16.dp), tint = Color.Red) }
                                }
                            }
                        },
                        selected = schedule.id == currentScheduleId,
                        onClick = {
                            currentScheduleId = schedule.id
                            saveData()
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    NavigationDrawerItem(
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, "创建", tint = Color(appSettings.fontColor))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("创建新课表", color = Color(appSettings.fontColor))
                            }
                        },
                        selected = false,
                        onClick = { showCreateScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(appSettings.backgroundColor),
            topBar = {
                Column(modifier = Modifier.background(Color(appSettings.backgroundColor))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding().height(50.dp).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "菜单", tint = Color(appSettings.fontColor)) }
                        Text(currentSchedule.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(appSettings.fontColor), modifier = Modifier.weight(1f).padding(start = 8.dp))
                        IconButton(onClick = { showSettingsDialog = true }) { Icon(Icons.Default.Settings, "设置", tint = Color(appSettings.fontColor)) }
                    }
                    val displayWeek = pagerState.currentPage + 1
                    Row(
                        modifier = Modifier.fillMaxWidth().height(30.dp).background(Color(appSettings.backgroundColor).copy(alpha = 0.8f)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("第 $displayWeek 周", fontWeight = FontWeight.Bold, color = Color(appSettings.fontColor))
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    editingCourse = null
                    showCourseDialog = true
                }) { Icon(Icons.Default.Add, "添加") }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            ) { page ->
                val weekIndex = page + 1
                val weekDates = getWeekDates(appSettings.semesterStartDate, weekIndex)

                Column(modifier = Modifier.background(Color(appSettings.backgroundColor))) {
                    WeekHeader(
                        showWeekends = appSettings.showWeekends,
                        dates = weekDates,
                        backgroundColor = Color(appSettings.backgroundColor),
                        fontColor = Color(appSettings.fontColor)
                    )

                    Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).background(Color(appSettings.backgroundColor))) {
                        CourseGrid(
                            courses = currentSchedule.courses,
                            timeSlots = timeSlots,
                            currentWeek = weekIndex,
                            showWeekends = appSettings.showWeekends,
                            cellHeightDp = appSettings.cellHeightDp,
                            backgroundColor = Color(appSettings.backgroundColor),
                            fontColor = Color(appSettings.fontColor),
                            courseColor = Color(appSettings.courseColor),
                            onCourseClick = { clickedCourses ->
                                if (clickedCourses.size == 1) {
                                    viewingCourse = clickedCourses.first()
                                    showDetailDialog = true
                                } else if (clickedCourses.size > 1) {
                                    conflictCourses = clickedCourses
                                    showConflictDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    }
    }

    // --- 弹窗 ---

    if (showDetailDialog && viewingCourse != null) {
        val course = viewingCourse!!
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(course.name, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("地点: ${if(course.room.isBlank()) "未设置" else course.room}")
                    Text("教师: ${if(course.teacher.isBlank()) "未设置" else course.teacher}")
                    val sectionText = if (course.startSection == course.endSection) "第${course.startSection}节" else "第${course.startSection}-${course.endSection}节"
                    Text("时间: 周${getDayName(course.day)} $sectionText")
                    Text("周数: ${course.startWeek} - ${course.endWeek}周")
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        currentSchedule.courses.remove(course)
                        saveData()
                        showDetailDialog = false
                    }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("删除") }
                    Button(onClick = {
                        editingCourse = course
                        showDetailDialog = false
                        showCourseDialog = true
                    }) { Text("编辑") }
                }
            },
            dismissButton = { TextButton(onClick = { showDetailDialog = false }) { Text("关闭") } }
        )
    }

    if (showCourseDialog) {
        CourseEditorDialog(
            courseToEdit = editingCourse,
            totalWeeks = appSettings.totalWeeks,
            onDismiss = { showCourseDialog = false },
            onConfirm = { name, room, teacher, day, startSec, endSec, startW, endW ->
                val defaultStart = if(editingCourse == null) (pagerState.currentPage + 1) else startW

                if (editingCourse == null) {
                    val randomColor = Color((200..255).random(), (200..255).random(), (180..255).random())
                    currentSchedule.courses.add(Course(name = name, room = room, teacher = teacher, day = day, startSection = startSec, endSection = endSec, startWeek = defaultStart, endWeek = endW, color = randomColor))
                } else {
                    val index = currentSchedule.courses.indexOf(editingCourse)
                    if (index != -1) {
                        currentSchedule.courses[index] = editingCourse!!.copy(name = name, room = room, teacher = teacher, day = day, startSection = startSec, endSection = endSec, startWeek = startW, endWeek = endW)
                    }
                }
                saveData()
                showCourseDialog = false
            }
        )
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = { Text("冲突列表") },
            text = {
                Column {
                    conflictCourses.forEach { c ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(4.dp).clickable { viewingCourse = c; showConflictDialog = false; showDetailDialog = true },
                            colors = CardDefaults.cardColors(containerColor = c.color)
                        ) { Text(c.name, modifier = Modifier.padding(16.dp)) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showConflictDialog = false }) { Text("取消") } }
        )
    }

    if (showCreateScheduleDialog) {
        InputNameDialog(title = "新建课表", onConfirm = { name ->
            val newSch = Schedule(name = name)
            allSchedules.add(newSch)
            currentScheduleId = newSch.id
            saveData()
            showCreateScheduleDialog = false
            scope.launch { drawerState.close() }
        }, onDismiss = { showCreateScheduleDialog = false })
    }

    if (showRenameScheduleDialog && scheduleToRename != null) {
        InputNameDialog(title = "重命名课表", initialValue = scheduleToRename!!.name, onConfirm = { name ->
            scheduleToRename!!.name = name
            saveData()
            showRenameScheduleDialog = false
        }, onDismiss = { showRenameScheduleDialog = false })
    }
}

// --- 组件部分 ---

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

@Composable
fun CourseGrid(courses: List<Course>, timeSlots: List<SectionTime>, currentWeek: Int, showWeekends: Boolean, cellHeightDp: Int, backgroundColor: Color, fontColor: Color, courseColor: Color, onCourseClick: (List<Course>) -> Unit) {
    val maxDay = if (showWeekends) 7 else 5
    
    Box {
        // 底层：时间列和空白格子
        Column {
            for (section in 1..10) {
                val timeSlot = timeSlots.getOrElse(section - 1) { SectionTime(section, "", "") }
                Row(modifier = Modifier.height(cellHeightDp.dp)) {
                    Column(
                        modifier = Modifier.width(40.dp).fillMaxHeight().background(backgroundColor.copy(alpha = 0.9f)).padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(section.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = fontColor)
                        Text(timeSlot.start, fontSize = 9.sp, color = fontColor.copy(alpha = 0.6f))
                        Text(timeSlot.end, fontSize = 9.sp, color = fontColor.copy(alpha = 0.6f))
                    }
                    for (day in 1..maxDay) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
        
        // 上层：课程方框（绝对定位）
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.width(40.dp))
            for (day in 1..maxDay) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // 找出当前这一天的所有课程
                    val dayCourses = courses.filter { it.day == day && (currentWeek in it.startWeek..it.endWeek) }
                    
                    dayCourses.forEach { course ->
                        // 计算课程的位置和高度
                        val topOffset = (course.startSection - 1) * cellHeightDp
                        val courseHeight = (course.endSection - course.startSection + 1) * cellHeightDp
                        
                        // 检查是否有冲突
                        val conflictCourses = dayCourses.filter { other ->
                            other.id != course.id &&
                            !(other.endSection < course.startSection || other.startSection > course.endSection)
                        }
                        val isConflict = conflictCourses.isNotEmpty()
                        val allConflictCourses = if (isConflict) listOf(course) + conflictCourses else listOf(course)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isConflict) Color(0xFFFFCDD2) else courseColor),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .padding(horizontal = 1.dp, vertical = 1.dp)
                                .offset(y = topOffset.dp)
                                .height(courseHeight.dp - 2.dp)
                                .fillMaxWidth()
                                .clickable { onCourseClick(allConflictCourses.distinctBy { it.id }) }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (isConflict) {
                                        Text("冲突", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        Text("(${allConflictCourses.distinctBy { it.id }.size})", color = Color.Red, fontSize = 9.sp)
                                    } else {
                                        Text(course.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                        if (course.room.isNotBlank()) Text("@${course.room}", fontSize = 8.sp, textAlign = TextAlign.Center, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InputNameDialog(title: String, initialValue: String = "", onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }) },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun ColorPickerRow(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    var showColorDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showColorDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(selectedColor, RoundedCornerShape(8.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("点击选择颜色", color = Color.Gray)
    }
    
    if (showColorDialog) {
        ColorPickerDialog(
            selectedColor = selectedColor,
            onColorSelected = { 
                onColorSelected(it)
                showColorDialog = false
            },
            onDismiss = { showColorDialog = false }
        )
    }
}

@Composable
fun ColorPickerDialog(selectedColor: Color, onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    val colors = listOf(
        // 第一行
        Color(0xFF6200EE), Color(0xFF3700B3), Color(0xFFBB86FC), Color(0xFF9C27B0), Color(0xFFE91E63),
        Color(0xFFF44336), Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFFFFEB3B),
        // 第二行
        Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFF009688), Color(0xFF00BCD4),
        Color(0xFF03A9F4), Color(0xFF2196F3), Color(0xFF3F51B5), Color(0xFF607D8B), Color(0xFF795548),
        // 第三行
        Color(0xFF000000), Color(0xFF424242), Color(0xFF757575), Color(0xFF9E9E9E), Color(0xFFBDBDBD),
        Color(0xFFE0E0E0), Color(0xFFF5F5F5), Color(0xFFFFFFFF), Color(0xFFFFCDD2), Color(0xFFC8E6C9)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.chunked(5).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color, RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (color == selectedColor) 3.dp else 1.dp,
                                        color = if (color == selectedColor) Color.Black else Color.Gray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onColorSelected(color) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(timeSlots: MutableList<SectionTime>, settings: AppSettings, onSettingsChange: (AppSettings) -> Unit, onBack: () -> Unit) {
    var previousPage by remember { mutableStateOf("main") }
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf("main") } // main, general, color, time, about

    // 处理系统返回键/侧滑返回
    BackHandler(enabled = true) {
        if (currentPage == "main") {
            onBack()
        } else {
            previousPage = currentPage
            currentPage = "main"
        }
    }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(settings.semesterStartDate)
            if (date != null) calendar.time = date
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val newDateStr = sdf.format(selectedCal.time)
                onSettingsChange(settings.copy(semesterStartDate = newDateStr))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(when (currentPage) {
                        "general" -> "常规设置"
                        "color" -> "颜色设置"
                        "time" -> "作息时间设置"
                        "about" -> "关于"
                        else -> "设置"
                    })
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentPage == "main") onBack() else {
                            previousPage = currentPage
                            currentPage = "main"
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState == "main") {
                    // 返回主页面：从左边滑入
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                } else {
                    // 进入子页面：从右边滑入
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                }
            },
            label = "settings_page_transition"
        ) { page ->
        when (page) {
            "main" -> {
                // 主设置页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    // 常规设置入口
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previousPage = currentPage; currentPage = "general" },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("常规设置", modifier = Modifier.weight(1f), color = Color.Black)
                            Icon(Icons.Default.KeyboardArrowRight, "进入", tint = Color.Black)
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))

                    // 颜色设置入口
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previousPage = currentPage; currentPage = "color" },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("颜色设置", modifier = Modifier.weight(1f), color = Color.Black)
                            Icon(Icons.Default.KeyboardArrowRight, "进入", tint = Color.Black)
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))

                    // 作息时间设置入口
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previousPage = currentPage; currentPage = "time" },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("作息时间设置", modifier = Modifier.weight(1f), color = Color.Black)
                            Icon(Icons.Default.KeyboardArrowRight, "进入", tint = Color.Black)
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))

                    // 关于入口
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previousPage = currentPage; currentPage = "about" },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text("关于", modifier = Modifier.weight(1f), color = Color.Black)
                            Icon(Icons.Default.KeyboardArrowRight, "进入", tint = Color.Black)
                        }
                    }
                }
            }
            "general" -> {
                // 常规设置页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "格子高度缩放: ${settings.cellHeightDp}dp",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.Black
                    )
                    Slider(
                        value = settings.cellHeightDp.toFloat(),
                        onValueChange = { onSettingsChange(settings.copy(cellHeightDp = it.toInt())) },
                        valueRange = 50f..120f,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker() },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("开学第一天", color = Color.Black)
                                Text(settings.semesterStartDate, fontSize = 12.sp, color = Color.Black.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.DateRange, "选择日期", tint = Color.Black)
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("显示周末", modifier = Modifier.weight(1f), color = Color.Black)
                        Switch(checked = settings.showWeekends, onCheckedChange = { onSettingsChange(settings.copy(showWeekends = it)) })
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    var expandedWeeks by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text("学期周数", modifier = Modifier.weight(1f), color = Color.Black)
                        Box {
                            OutlinedButton(onClick = { expandedWeeks = true }) {
                                Text("${settings.totalWeeks}周")
                            }
                            DropdownMenu(expanded = expandedWeeks, onDismissRequest = { expandedWeeks = false }) {
                                (10..30).forEach { week ->
                                    DropdownMenuItem(
                                        text = { Text("${week}周") },
                                        onClick = {
                                            onSettingsChange(settings.copy(totalWeeks = week))
                                            expandedWeeks = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "color" -> {
                // 颜色设置页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("背景颜色", modifier = Modifier.padding(top = 8.dp, bottom = 8.dp), color = Color.Black)
                    ColorPickerRow(
                        selectedColor = Color(settings.backgroundColor),
                        onColorSelected = { onSettingsChange(settings.copy(backgroundColor = it.toArgb())) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("字体颜色", modifier = Modifier.padding(top = 8.dp, bottom = 8.dp), color = Color.Black)
                    ColorPickerRow(
                        selectedColor = Color(settings.fontColor),
                        onColorSelected = { onSettingsChange(settings.copy(fontColor = it.toArgb())) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("课程方框颜色", modifier = Modifier.padding(top = 8.dp, bottom = 8.dp), color = Color.Black)
                    ColorPickerRow(
                        selectedColor = Color(settings.courseColor),
                        onColorSelected = { onSettingsChange(settings.copy(courseColor = it.toArgb())) }
                    )
                }
            }
            "time" -> {
                // 作息时间设置页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    timeSlots.forEachIndexed { index, slot ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                "第${slot.section}节",
                                modifier = Modifier.width(60.dp),
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            TimePickerButton(
                                time = slot.start,
                                onTimeSelected = { newTime -> timeSlots[index] = slot.copy(start = newTime) }
                            )
                            Text(
                                " - ",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = Color.Black
                            )
                            TimePickerButton(
                                time = slot.end,
                                onTimeSelected = { newTime -> timeSlots[index] = slot.copy(end = newTime) }
                            )
                        }
                        if (index < timeSlots.size - 1) {
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                        }
                    }
                }
            }
            "about" -> {
                // 关于页面
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = "应用图标",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "哈基课程表",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "by MOAKIEE",
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "v0.3beta",
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                }
            }
        }
        }
    }
}

@Composable
fun CourseEditorDialog(courseToEdit: Course?, totalWeeks: Int, onDismiss: () -> Unit, onConfirm: (String, String, String, Int, Int, Int, Int, Int) -> Unit) {
    var name by remember { mutableStateOf(courseToEdit?.name ?: "") }
    var room by remember { mutableStateOf(courseToEdit?.room ?: "") }
    var teacher by remember { mutableStateOf(courseToEdit?.teacher ?: "") }
    var selectedDay by remember { mutableStateOf(courseToEdit?.day ?: 1) }
    var selectedStartSection by remember { mutableStateOf(courseToEdit?.startSection ?: 1) }
    var selectedEndSection by remember { mutableStateOf(courseToEdit?.endSection ?: 1) }
    var selectedStartWeek by remember { mutableStateOf(courseToEdit?.startWeek ?: 1) }
    var selectedEndWeek by remember { mutableStateOf(courseToEdit?.endWeek ?: totalWeeks) }

    var expandedDay by remember { mutableStateOf(false) }
    var expandedStartSection by remember { mutableStateOf(false) }
    var expandedEndSection by remember { mutableStateOf(false) }
    var expandedStartWeek by remember { mutableStateOf(false) }
    var expandedEndWeek by remember { mutableStateOf(false) }

    val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (courseToEdit == null) "添加课程" else "编辑课程") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("课程名称") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("地点") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("教师") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                
                // 时间选择
                Text("时间", fontSize = 12.sp, color = Color.Gray)
                Box {
                    OutlinedButton(onClick = { expandedDay = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(dayNames[selectedDay - 1])
                    }
                    DropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                        dayNames.forEachIndexed { index, dayName ->
                            DropdownMenuItem(
                                text = { Text(dayName) },
                                onClick = { selectedDay = index + 1; expandedDay = false }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 节次选择
                Text("节次", fontSize = 12.sp, color = Color.Gray)
                Row {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedStartSection = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("第${selectedStartSection}节")
                        }
                        DropdownMenu(expanded = expandedStartSection, onDismissRequest = { expandedStartSection = false }) {
                            (1..10).forEach { sec ->
                                DropdownMenuItem(
                                    text = { Text("第${sec}节") },
                                    onClick = { 
                                        selectedStartSection = sec
                                        if (selectedEndSection < sec) selectedEndSection = sec
                                        expandedStartSection = false 
                                    }
                                )
                            }
                        }
                    }
                    Text(" 到 ", modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically))
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedEndSection = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("第${selectedEndSection}节")
                        }
                        DropdownMenu(expanded = expandedEndSection, onDismissRequest = { expandedEndSection = false }) {
                            (selectedStartSection..10).forEach { sec ->
                                DropdownMenuItem(
                                    text = { Text("第${sec}节") },
                                    onClick = { selectedEndSection = sec; expandedEndSection = false }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 周数选择
                Text("周数", fontSize = 12.sp, color = Color.Gray)
                Row {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedStartWeek = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("第${selectedStartWeek}周")
                        }
                        DropdownMenu(expanded = expandedStartWeek, onDismissRequest = { expandedStartWeek = false }) {
                            (1..totalWeeks).forEach { week ->
                                DropdownMenuItem(
                                    text = { Text("第${week}周") },
                                    onClick = { 
                                        selectedStartWeek = week
                                        if (selectedEndWeek < week) selectedEndWeek = week
                                        expandedStartWeek = false 
                                    }
                                )
                            }
                        }
                    }
                    Text(" 到 ", modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterVertically))
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandedEndWeek = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("第${selectedEndWeek}周")
                        }
                        DropdownMenu(expanded = expandedEndWeek, onDismissRequest = { expandedEndWeek = false }) {
                            (selectedStartWeek..totalWeeks).forEach { week ->
                                DropdownMenuItem(
                                    text = { Text("第${week}周") },
                                    onClick = { selectedEndWeek = week; expandedEndWeek = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(name, room, teacher, selectedDay, selectedStartSection, selectedEndSection, selectedStartWeek, selectedEndWeek)
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun TimePickerButton(time: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    var hour by remember { mutableStateOf(time.substringBefore(":").toIntOrNull() ?: 8) }
    var minute by remember { mutableStateOf(time.substringAfter(":").toIntOrNull() ?: 0) }
    
    OutlinedButton(
        onClick = {
            android.app.TimePickerDialog(
                context,
                { _, h, m ->
                    hour = h
                    minute = m
                    onTimeSelected(String.format("%02d:%02d", h, m))
                },
                hour,
                minute,
                true
            ).show()
        },
        modifier = Modifier.width(80.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(time.ifEmpty { "--:--" }, fontSize = 12.sp)
    }
}