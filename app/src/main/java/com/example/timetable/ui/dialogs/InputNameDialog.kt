package com.example.timetable.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun InputNameDialog(
    title: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }) },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
