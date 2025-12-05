package com.example.timetable.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
