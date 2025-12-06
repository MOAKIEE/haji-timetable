package com.example.timetable.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

object UpdatePreferences {
    private const val PREF_NAME = "update_preferences"
    private const val KEY_LAST_CHECK_DATE = "last_check_date"
    private const val KEY_IGNORED_VERSION = "ignored_version"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 检查今天是否已经检查过更新
     */
    fun shouldCheckUpdate(context: Context): Boolean {
        val prefs = getPrefs(context)
        val lastCheckDate = prefs.getString(KEY_LAST_CHECK_DATE, "")
        val today = getCurrentDate()
        return lastCheckDate != today
    }
    
    /**
     * 记录今天已检查更新
     */
    fun markUpdateChecked(context: Context) {
        getPrefs(context).edit()
            .putString(KEY_LAST_CHECK_DATE, getCurrentDate())
            .apply()
    }
    
    /**
     * 保存被忽略的版本号
     */
    fun setIgnoredVersion(context: Context, version: String) {
        getPrefs(context).edit()
            .putString(KEY_IGNORED_VERSION, version)
            .apply()
    }
    
    /**
     * 获取被忽略的版本号
     */
    fun getIgnoredVersion(context: Context): String? {
        return getPrefs(context).getString(KEY_IGNORED_VERSION, null)
    }
    
    /**
     * 清除忽略的版本（用于手动检查更新时）
     */
    fun clearIgnoredVersion(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_IGNORED_VERSION)
            .apply()
    }
    
    /**
     * 获取当前日期字符串（格式：yyyy-MM-dd）
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
