package com.example.timetable.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.timetable.R
import com.example.timetable.data.model.Course
import com.example.timetable.data.model.Schedule
import com.example.timetable.ui.components.CourseGrid
import com.example.timetable.ui.components.WeekHeader
import com.example.timetable.ui.components.AutoUpdateDialog
import com.example.timetable.ui.dialogs.CalendarSyncDialog
import com.example.timetable.ui.dialogs.CourseEditorDialog
import com.example.timetable.ui.dialogs.InputNameDialog
import com.example.timetable.utils.getDayName
import com.example.timetable.utils.getWeekDates
import com.example.timetable.utils.rememberScreenConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // 加载数据
    LaunchedEffect(Unit) {
        viewModel.loadData(context)
        viewModel.checkForUpdate(context, isManual = false)
    }

    // 显示加载界面
    if (viewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = viewModel.initialPage,
        pageCount = { viewModel.appSettings.totalWeeks }
    )

    val scope = rememberCoroutineScope()
    val currentSchedule = viewModel.currentSchedule ?: return
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // 防止初始渲染时侧边栏闪现 - 主动关闭并延迟启用手势
    var gesturesEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(drawerState) {
        // 确保抽屉关闭
        drawerState.snapTo(DrawerValue.Closed)
        kotlinx.coroutines.delay(150)
        gesturesEnabled = true
    }

    AnimatedContent(
        targetState = viewModel.showSettingsDialog,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            } else {
                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            }
        },
        label = "main_settings_transition"
    ) { isSettings ->
        if (isSettings) {
            SettingsScreen(
                timeSlots = viewModel.timeSlots,
                settings = viewModel.appSettings,
                onSettingsChange = { newSettings -> 
                    viewModel.updateSettings(newSettings)
                },
                onBack = { 
                    viewModel.saveData(context)
                    viewModel.closeSettingsDialog()
                }
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = gesturesEnabled,
                drawerContent = {
                    DrawerContent(
                        allSchedules = viewModel.allSchedules,
                        currentScheduleId = viewModel.currentScheduleId,
                        appSettings = viewModel.appSettings,
                        onScheduleSelect = { schedule ->
                            viewModel.selectSchedule(schedule.id)
                            viewModel.saveData(context)
                            scope.launch { drawerState.close() }
                        },
                        onScheduleRename = { schedule ->
                            viewModel.openRenameScheduleDialog(schedule)
                        },
                        onScheduleDelete = { schedule ->
                            viewModel.openDeleteScheduleDialog(schedule)
                        },
                        onCreateSchedule = { 
                            viewModel.openCreateScheduleDialog()
                        }
                    )
                }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(viewModel.appSettings.backgroundColor),
                    topBar = {
                        TimetableTopBar(
                            scheduleName = currentSchedule.name,
                            currentWeek = pagerState.currentPage + 1,
                            backgroundColor = Color(viewModel.appSettings.backgroundColor),
                            fontColor = Color(viewModel.appSettings.fontColor),
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSyncCalendarClick = { viewModel.openCalendarSyncDialog() },
                            onSettingsClick = { viewModel.openSettingsDialog() },
                            onAboutClick = { viewModel.openAboutDialog() }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            viewModel.openCourseDialog(null)
                        }) { Icon(Icons.Default.Add, "添加") }
                    }
                ) { innerPadding ->
                    TimetablePager(
                        pagerState = pagerState,
                        innerPadding = innerPadding,
                        currentSchedule = currentSchedule,
                        timeSlots = viewModel.timeSlots,
                        appSettings = viewModel.appSettings,
                        onCourseClick = { clickedCourses ->
                            if (clickedCourses.size == 1) {
                                viewModel.openDetailDialog(clickedCourses.first())
                            } else if (clickedCourses.size > 1) {
                                viewModel.openConflictDialog(clickedCourses)
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (viewModel.showDetailDialog && viewModel.viewingCourse != null) {
        CourseDetailDialog(
            course = viewModel.viewingCourse!!,
            onDismiss = { viewModel.closeDetailDialog() },
            onEdit = {
                viewModel.openCourseDialog(viewModel.viewingCourse)
                viewModel.closeDetailDialog()
            },
            onDelete = {
                viewModel.deleteCourse(viewModel.viewingCourse!!)
                viewModel.saveData(context)
                viewModel.closeDetailDialog()
            }
        )
    }

    if (viewModel.showCourseDialog) {
        CourseEditorDialog(
            courseToEdit = viewModel.editingCourse,
            totalWeeks = viewModel.appSettings.totalWeeks,
            onDismiss = { viewModel.closeCourseDialog() },
            onConfirm = { name, room, teacher, day, startSec, endSec, startW, endW ->
                val course = if (viewModel.editingCourse == null) {
                    val randomColor = Color((200..255).random(), (200..255).random(), (180..255).random())
                    Course(
                        name = name, 
                        room = room, 
                        teacher = teacher, 
                        day = day, 
                        startSection = startSec, 
                        endSection = endSec, 
                        startWeek = startW, 
                        endWeek = endW, 
                        color = randomColor
                    )
                } else {
                    viewModel.editingCourse!!.copy(
                        name = name, 
                        room = room, 
                        teacher = teacher, 
                        day = day, 
                        startSection = startSec, 
                        endSection = endSec, 
                        startWeek = startW, 
                        endWeek = endW
                    )
                }
                viewModel.addOrUpdateCourse(course, viewModel.editingCourse != null)
                viewModel.saveData(context)
                viewModel.closeCourseDialog()
            }
        )
    }

    if (viewModel.showConflictDialog) {
        ConflictDialog(
            conflictCourses = viewModel.conflictCourses,
            onDismiss = { viewModel.closeConflictDialog() },
            onCourseSelect = { course ->
                viewModel.openDetailDialog(course)
                viewModel.closeConflictDialog()
            }
        )
    }

    if (viewModel.showCreateScheduleDialog) {
        InputNameDialog(
            title = "新建课表", 
            onConfirm = { name ->
                viewModel.createSchedule(name)
                viewModel.saveData(context)
                viewModel.closeCreateScheduleDialog()
                scope.launch { drawerState.close() }
            }, 
            onDismiss = { viewModel.closeCreateScheduleDialog() }
        )
    }

    if (viewModel.showRenameScheduleDialog && viewModel.scheduleToRename != null) {
        InputNameDialog(
            title = "重命名课表", 
            initialValue = viewModel.scheduleToRename!!.name, 
            onConfirm = { name ->
                viewModel.renameSchedule(viewModel.scheduleToRename!!, name)
                viewModel.saveData(context)
                viewModel.closeRenameScheduleDialog()
            }, 
            onDismiss = { viewModel.closeRenameScheduleDialog() }
        )
    }

    if (viewModel.showDeleteScheduleDialog && viewModel.scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteScheduleDialog() },
            title = { Text("删除课表") },
            text = { Text("确定要删除课表「${viewModel.scheduleToDelete!!.name}」吗？\n该操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSchedule(viewModel.scheduleToDelete!!)
                        viewModel.saveData(context)
                        viewModel.closeDeleteScheduleDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    viewModel.closeDeleteScheduleDialog()
                }) { Text("取消") }
            }
        )
    }

    if (viewModel.showCalendarSyncDialog) {
        CalendarSyncDialog(
            courses = currentSchedule.courses.toList(),
            timeSlots = viewModel.timeSlots.toList(),
            semesterStartDate = viewModel.appSettings.semesterStartDate,
            totalWeeks = viewModel.appSettings.totalWeeks,
            onDismiss = { viewModel.closeCalendarSyncDialog() }
        )
    }

    if (viewModel.showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.closeAboutDialog() },
            onCheckUpdate = {
                viewModel.checkForUpdate(context, isManual = true)
            }
        )
    }
    
    // 自动更新检查对话框
    when (val state = viewModel.autoUpdateDialogState) {
        is UpdateDialogState.Available -> {
            AutoUpdateDialog(
                releaseInfo = state.releaseInfo,
                currentVersion = "0.7beta",
                onDownload = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.releaseInfo.htmlUrl))
                    context.startActivity(intent)
                    viewModel.closeUpdateDialog()
                },
                onIgnore = {
                    viewModel.ignoreUpdate(context, state.releaseInfo.tagName)
                },
                onDismiss = {
                    viewModel.closeUpdateDialog()
                }
            )
        }
        is UpdateDialogState.NoUpdate -> {
            com.example.timetable.ui.components.NoUpdateDialog(
                onDismiss = { viewModel.closeUpdateDialog() }
            )
        }
        is UpdateDialogState.Error -> {
            com.example.timetable.ui.components.UpdateErrorDialog(
                errorMessage = state.message,
                onDismiss = { viewModel.closeUpdateDialog() }
            )
        }
        UpdateDialogState.None -> {}
    }
}

@Composable
private fun DrawerContent(
    allSchedules: List<Schedule>,
    currentScheduleId: String,
    appSettings: com.example.timetable.data.model.AppSettings,
    onScheduleSelect: (Schedule) -> Unit,
    onScheduleRename: (Schedule) -> Unit,
    onScheduleDelete: (Schedule) -> Unit,
    onCreateSchedule: () -> Unit
) {
    val screenConfig = rememberScreenConfig()
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(screenConfig.drawerWidthFraction),
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
                        IconButton(onClick = { onScheduleRename(schedule) }) {
                            Icon(Icons.Default.Edit, "重命名", modifier = Modifier.size(16.dp), tint = Color(appSettings.fontColor))
                        }
                        if (allSchedules.size > 1) {
                            IconButton(onClick = { onScheduleDelete(schedule) }) {
                                Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(16.dp), tint = Color.Red)
                            }
                        }
                    }
                },
                selected = schedule.id == currentScheduleId,
                onClick = { onScheduleSelect(schedule) },
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
                onClick = onCreateSchedule,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TimetableTopBar(
    scheduleName: String,
    currentWeek: Int,
    backgroundColor: Color,
    fontColor: Color,
    onMenuClick: () -> Unit,
    onSyncCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.background(backgroundColor)) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(50.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "菜单", tint = fontColor) }
            Text(scheduleName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = fontColor, modifier = Modifier.weight(1f).padding(start = 8.dp))
            Box {
                IconButton(onClick = { showDropdownMenu = true }) {
                    Icon(Icons.Default.MoreVert, "更多选项", tint = fontColor)
                }
                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("同步至日历") },
                        onClick = {
                            showDropdownMenu = false
                            onSyncCalendarClick()
                        },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("设置") },
                        onClick = {
                            showDropdownMenu = false
                            onSettingsClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("关于") },
                        onClick = {
                            showDropdownMenu = false
                            onAboutClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(30.dp).background(backgroundColor.copy(alpha = 0.8f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("第 $currentWeek 周", fontWeight = FontWeight.Bold, color = fontColor)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimetablePager(
    pagerState: PagerState,
    innerPadding: PaddingValues,
    currentSchedule: Schedule,
    timeSlots: List<com.example.timetable.data.model.SectionTime>,
    appSettings: com.example.timetable.data.model.AppSettings,
    onCourseClick: (List<Course>) -> Unit
) {
    // 缓存颜色计算避免重复创建
    val backgroundColor = remember(appSettings.backgroundColor) { Color(appSettings.backgroundColor) }
    val fontColor = remember(appSettings.fontColor) { Color(appSettings.fontColor) }
    val courseColor = remember(appSettings.courseColor) { Color(appSettings.courseColor) }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.padding(innerPadding).fillMaxSize(),
        key = { it }  // 使用页码作为 key 优化重组
    ) { page ->
        val weekIndex = page + 1
        // 缓存日期计算
        val weekDates = remember(appSettings.semesterStartDate, weekIndex, appSettings.weekStartDay) {
            getWeekDates(appSettings.semesterStartDate, weekIndex, appSettings.weekStartDay)
        }
        // 过滤当周课程 - 使用 toList() 确保每次都是新列表
        val weekCourses = currentSchedule.courses.filter { weekIndex in it.startWeek..it.endWeek }

        Column(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
            WeekHeader(
                showWeekends = appSettings.showWeekends,
                weekStartDay = appSettings.weekStartDay,
                dates = weekDates,
                backgroundColor = backgroundColor,
                fontColor = fontColor
            )

            // 使用 LazyColumn 或固定高度滚动
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .background(backgroundColor)
            ) {
                CourseGrid(
                    courses = weekCourses,
                    timeSlots = timeSlots,
                    currentWeek = weekIndex,
                    showWeekends = appSettings.showWeekends,
                    weekStartDay = appSettings.weekStartDay,
                    cellHeightDp = appSettings.cellHeightDp,
                    backgroundColor = backgroundColor,
                    fontColor = fontColor,
                    courseColor = courseColor,
                    onCourseClick = onCourseClick
                )
            }
        }
    }
}

@Composable
private fun CourseDetailDialog(
    course: Course,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("删除") }
                Button(onClick = onEdit) { Text("编辑") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun ConflictDialog(
    conflictCourses: List<Course>,
    onDismiss: () -> Unit,
    onCourseSelect: (Course) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("冲突课程") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conflictCourses.forEachIndexed { index, course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCourseSelect(course) },
                        colors = CardDefaults.cardColors(containerColor = course.color)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                course.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "第${course.startSection}-${course.endSection}节 | ${if(course.room.isBlank()) "未设置教室" else course.room}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit,
    onCheckUpdate: () -> Unit
) {
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    var showEasterEgg by remember { mutableStateOf(false) }
    
    // 丝滑缩放动画
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )
    
    if (showEasterEgg) {
        EasterEggDialog(onDismiss = { showEasterEgg = false })
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "应用图标",
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            clickCount++
                            if (clickCount >= 10) {
                                clickCount = 0
                                showEasterEgg = true
                            }
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "哈基课程表",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "by MOAKIEE",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "v0.7beta",
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // 检查更新按钮
                Button(
                    onClick = onCheckUpdate,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("检查更新")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 代码仓库按钮
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/MOAKIEE/haji-timetable"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("代码仓库")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun EasterEggDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = { /* 禁止点击外部关闭 */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()
            
            AsyncImage(
                model = R.raw.easter_egg,
                contentDescription = "Easter Egg",
                imageLoader = imageLoader,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            
            // 右上角关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
