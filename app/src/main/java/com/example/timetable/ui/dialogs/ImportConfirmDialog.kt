package com.example.timetable.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 导入确认对话框 - 选择覆盖或新建
 */
@Composable
fun ImportConfirmDialog(
    schedulesCount: Int,
    onDismiss: () -> Unit,
    onOverwrite: () -> Unit,
    onCreateNew: (String) -> Unit
) {
    var showNameInput by remember { mutableStateOf(false) }
    var newScheduleName by remember { mutableStateOf("") }
    
    if (showNameInput) {
        // 输入课表名称对话框
        AlertDialog(
            onDismissRequest = { showNameInput = false },
            icon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("输入新课表名称") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newScheduleName,
                        onValueChange = { newScheduleName = it },
                        label = { Text("课表名称") },
                        placeholder = { Text("例如：大二上学期") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "留空将使用默认名称",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNameInput = false
                        onCreateNew(newScheduleName)
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameInput = false }) {
                    Text("取消")
                }
            }
        )
    } else {
        // 选择导入方式对话框
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("导入课表") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("检测到 $schedulesCount 个课表，请选择导入方式：")
                    
                    // 覆盖当前课表
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOverwrite()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("覆盖当前课表")
                    }
                    
                    Text(
                        "将替换当前课表的所有课程",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Divider()
                    
                    // 新建课表
                    OutlinedButton(
                        onClick = {
                            showNameInput = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("创建为新课表")
                    }
                    
                    Text(
                        "保留当前课表，将导入的数据创建为新课表",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}
