package com.example.timetable.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 屏幕尺寸类型
 */
enum class ScreenType {
    PHONE,      // 手机 (< 600dp)
    TABLET,     // 平板 (600dp - 840dp)
    LARGE_TABLET // 大平板 (>= 840dp)
}

/**
 * 屏幕适配参数
 */
data class ScreenConfig(
    val screenType: ScreenType,
    val screenWidth: Dp,
    val screenHeight: Dp,
    // 课程格子高度
    val cellHeight: Int,
    // 时间列宽度
    val timeColumnWidth: Dp,
    // 侧边栏宽度比例
    val drawerWidthFraction: Float,
    // 对话框宽度比例
    val dialogWidthFraction: Float,
    // 课程名字体大小
    val courseFontSize: Int,
    // 课程地点字体大小
    val courseLocationFontSize: Int,
    // 时间字体大小
    val timeFontSize: Int,
    // 节次字体大小
    val sectionFontSize: Int
)

/**
 * 获取当前屏幕配置
 */
@Composable
fun rememberScreenConfig(): ScreenConfig {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    val screenType = when {
        screenWidth >= 840.dp -> ScreenType.LARGE_TABLET
        screenWidth >= 600.dp -> ScreenType.TABLET
        else -> ScreenType.PHONE
    }
    
    return when (screenType) {
        ScreenType.PHONE -> ScreenConfig(
            screenType = ScreenType.PHONE,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            cellHeight = 60,
            timeColumnWidth = 40.dp,
            drawerWidthFraction = 0.75f,
            dialogWidthFraction = 0.9f,
            courseFontSize = 10,
            courseLocationFontSize = 8,
            timeFontSize = 9,
            sectionFontSize = 14
        )
        ScreenType.TABLET -> ScreenConfig(
            screenType = ScreenType.TABLET,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            cellHeight = 70,
            timeColumnWidth = 50.dp,
            drawerWidthFraction = 0.4f,
            dialogWidthFraction = 0.7f,
            courseFontSize = 12,
            courseLocationFontSize = 10,
            timeFontSize = 10,
            sectionFontSize = 16
        )
        ScreenType.LARGE_TABLET -> ScreenConfig(
            screenType = ScreenType.LARGE_TABLET,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            cellHeight = 80,
            timeColumnWidth = 60.dp,
            drawerWidthFraction = 0.3f,
            dialogWidthFraction = 0.5f,
            courseFontSize = 14,
            courseLocationFontSize = 11,
            timeFontSize = 11,
            sectionFontSize = 18
        )
    }
}

/**
 * 判断是否为平板设备
 */
@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}
