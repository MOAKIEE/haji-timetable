package com.example.timetable.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timetable.R
import com.example.timetable.data.model.AppSettings
import com.example.timetable.data.model.SectionTime
import com.example.timetable.ui.components.ColorPickerRow
import com.example.timetable.ui.components.TimePickerButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    timeSlots: MutableList<SectionTime>,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onBack: () -> Unit
) {
    var previousPage by remember { mutableStateOf("main") }
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf("main") }

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
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                } else {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                }
            },
            label = "settings_page_transition"
        ) { page ->
            when (page) {
                "main" -> SettingsMainPage(innerPadding) { currentPage = it }
                "general" -> GeneralSettingsPage(innerPadding, settings, onSettingsChange) { showDatePicker() }
                "color" -> ColorSettingsPage(innerPadding, settings, onSettingsChange)
                "time" -> TimeSettingsPage(innerPadding, timeSlots)
                "about" -> AboutPage(innerPadding)
            }
        }
    }
}

@Composable
private fun SettingsMainPage(innerPadding: PaddingValues, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        SettingsMenuItem("常规设置") { onNavigate("general") }
        HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))
        SettingsMenuItem("颜色设置") { onNavigate("color") }
        HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))
        SettingsMenuItem("作息时间设置") { onNavigate("time") }
        HorizontalDivider(color = Color.Black.copy(alpha = 0.2f))
        SettingsMenuItem("关于") { onNavigate("about") }
    }
}

@Composable
private fun SettingsMenuItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, modifier = Modifier.weight(1f), color = Color.Black)
            Icon(Icons.Default.KeyboardArrowRight, "进入", tint = Color.Black)
        }
    }
}

@Composable
private fun GeneralSettingsPage(
    innerPadding: PaddingValues,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onShowDatePicker: () -> Unit
) {
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
                .clickable { onShowDatePicker() },
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

@Composable
private fun ColorSettingsPage(
    innerPadding: PaddingValues,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit
) {
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

@Composable
private fun TimeSettingsPage(innerPadding: PaddingValues, timeSlots: MutableList<SectionTime>) {
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

@Composable
private fun AboutPage(innerPadding: PaddingValues) {
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
            "v0.4beta",
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.5f)
        )
    }
}
