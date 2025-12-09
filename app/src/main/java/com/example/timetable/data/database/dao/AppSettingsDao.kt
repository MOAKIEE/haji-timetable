package com.example.timetable.data.database.dao

import androidx.room.*
import com.example.timetable.data.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 应用设置数据访问对象
 */
@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)
    
    @Update
    suspend fun updateSettings(settings: AppSettingsEntity)
}
