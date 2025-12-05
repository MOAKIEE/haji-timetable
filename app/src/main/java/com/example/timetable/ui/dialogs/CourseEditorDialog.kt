package com.example.timetable.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timetable.data.model.Course

@Composable
fun CourseEditorDialog(
    courseToEdit: Course?,
    totalWeeks: Int,
    defaultStartWeek: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int, Int, Int, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf(courseToEdit?.name ?: "") }
    var room by remember { mutableStateOf(courseToEdit?.room ?: "") }
    var teacher by remember { mutableStateOf(courseToEdit?.teacher ?: "") }
    var selectedDay by remember { mutableStateOf(courseToEdit?.day ?: 1) }
    var selectedStartSection by remember { mutableStateOf(courseToEdit?.startSection ?: 1) }
    var selectedEndSection by remember { mutableStateOf(courseToEdit?.endSection ?: 1) }
    var selectedStartWeek by remember { mutableStateOf(courseToEdit?.startWeek ?: defaultStartWeek) }
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
