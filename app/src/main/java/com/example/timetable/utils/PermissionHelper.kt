package com.example.timetable.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * 权限处理辅助类
 */
object PermissionHelper {
    
    /**
     * 检查相机权限
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 请求相机权限
     */
    fun requestCameraPermission(
        launcher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        launcher.launch(Manifest.permission.CAMERA)
    }
}
