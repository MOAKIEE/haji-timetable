package com.example.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
