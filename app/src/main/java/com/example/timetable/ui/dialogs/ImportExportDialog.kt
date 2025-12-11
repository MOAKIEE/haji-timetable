package com.example.timetable.ui.dialogs

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 导入导出主对话框
 */
@Composable
fun ImportExportDialog(
    onDismiss: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("数据管理") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 导入按钮
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onImport()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入课表")
                }
                
                // 导出按钮
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onExport()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出课表")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 导入方式选择对话框
 */
@Composable
fun ImportMethodDialog(
    onDismiss: () -> Unit,
    onImportFile: () -> Unit,
    onScanQRCode: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择导入方式") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 从文件导入
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onImportFile()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("从文件导入")
                }
                
                // 扫描二维码
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onScanQRCode()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("扫描二维码")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 导出方式选择对话框
 */
@Composable
fun ExportMethodDialog(
    onDismiss: () -> Unit,
    onExportJson: () -> Unit,
    onExportQRCode: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择导出方式") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 导出为 JSON 文件
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onExportJson()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出为JSON文件")
                }
                
                // 生成二维码
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onExportQRCode()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成二维码分享")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 二维码显示对话框
 */
@Composable
fun QRCodeDialog(
    qrCodeBitmap: Bitmap?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("扫描二维码导入") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (qrCodeBitmap != null) {
                    Image(
                        bitmap = qrCodeBitmap.asImageBitmap(),
                        contentDescription = "二维码",
                        modifier = Modifier.size(300.dp)
                    )
                    Text(
                        "使用其他设备扫描此二维码导入课表",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CircularProgressIndicator()
                    Text("生成二维码中...")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 导入结果对话框
 */
@Composable
fun ImportResultDialog(
    success: Boolean,
    message: String,
    schedulesCount: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        title = { Text(if (success) "导入成功" else "导入失败") },
        text = {
            Column {
                Text(message)
                if (success && schedulesCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "共导入 $schedulesCount 个课表",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    if (success) onConfirm()
                }
            ) {
                Text("确定")
            }
        }
    )
}

/**
 * 导出结果对话框
 */
@Composable
fun ExportResultDialog(
    success: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        title = { Text(if (success) "导出成功" else "导出失败") },
        text = if (!success && message.isNotBlank()) {
            { Text(message) }
        } else null,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
