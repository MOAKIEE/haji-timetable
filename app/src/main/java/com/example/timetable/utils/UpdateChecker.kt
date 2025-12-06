package com.example.timetable.utils

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

data class ReleaseInfo(
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("assets")
    val assets: List<Asset>
)

data class Asset(
    @SerializedName("name")
    val name: String,
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String
)

sealed class UpdateResult {
    data class Available(val releaseInfo: ReleaseInfo) : UpdateResult()
    object NoUpdate : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

object UpdateChecker {
    private const val GITHUB_API_URL = "https://api.github.com/repos/MOAKIEE/haji-timetable/releases/latest"
    private const val CURRENT_VERSION = "0.6beta"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_API_URL)
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext UpdateResult.Error("请求失败: ${response.code}")
            }
            
            val body = response.body?.string() ?: return@withContext UpdateResult.Error("响应为空")
            val releaseInfo = Gson().fromJson(body, ReleaseInfo::class.java)
            
            // 比较版本
            if (isNewerVersion(releaseInfo.tagName, CURRENT_VERSION)) {
                UpdateResult.Available(releaseInfo)
            } else {
                UpdateResult.NoUpdate
            }
        } catch (e: Exception) {
            UpdateResult.Error("检查更新失败: ${e.message}")
        }
    }
    
    /**
     * 比较版本号
     * 支持格式: v1.0, 1.0, 0.6beta 等
     */
    private fun isNewerVersion(newVersion: String, currentVersion: String): Boolean {
        try {
            // 移除 "v" 前缀
            val new = newVersion.removePrefix("v").lowercase()
            val current = currentVersion.removePrefix("v").lowercase()
            
            // 如果版本号完全相同，不需要更新
            if (new == current) return false
            
            // 提取数字部分进行比较
            val newNumbers = extractVersionNumbers(new)
            val currentNumbers = extractVersionNumbers(current)
            
            // 逐位比较版本号
            for (i in 0 until maxOf(newNumbers.size, currentNumbers.size)) {
                val newPart = newNumbers.getOrNull(i) ?: 0
                val currentPart = currentNumbers.getOrNull(i) ?: 0
                
                if (newPart > currentPart) return true
                if (newPart < currentPart) return false
            }
            
            // 如果数字部分相同，检查是否是稳定版 vs beta版
            // 稳定版 > beta版
            val newIsBeta = new.contains("beta") || new.contains("alpha")
            val currentIsBeta = current.contains("beta") || current.contains("alpha")
            
            if (!newIsBeta && currentIsBeta) return true
            if (newIsBeta && !currentIsBeta) return false
            
            return false
        } catch (e: Exception) {
            // 如果解析失败，简单比较字符串
            return newVersion != currentVersion
        }
    }
    
    /**
     * 从版本字符串中提取数字部分
     * 例如: "0.6beta" -> [0, 6]
     */
    private fun extractVersionNumbers(version: String): List<Int> {
        val numbers = mutableListOf<Int>()
        var currentNumber = ""
        
        for (char in version) {
            if (char.isDigit() || char == '.') {
                if (char == '.') {
                    if (currentNumber.isNotEmpty()) {
                        numbers.add(currentNumber.toInt())
                        currentNumber = ""
                    }
                } else {
                    currentNumber += char
                }
            } else {
                // 遇到非数字字符，结束数字提取
                if (currentNumber.isNotEmpty()) {
                    numbers.add(currentNumber.toInt())
                    break
                }
            }
        }
        
        if (currentNumber.isNotEmpty()) {
            numbers.add(currentNumber.toInt())
        }
        
        return numbers
    }
}
