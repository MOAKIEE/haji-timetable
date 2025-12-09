package com.example.timetable.data.database.dao

import androidx.room.*
import com.example.timetable.data.database.entity.SectionTimeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 作息时间数据访问对象
 */
@Dao
interface SectionTimeDao {
    
    @Query("SELECT * FROM section_times ORDER BY section ASC")
    fun getAllSectionTimes(): Flow<List<SectionTimeEntity>>
    
    @Query("SELECT * FROM section_times WHERE section = :section")
    suspend fun getSectionTime(section: Int): SectionTimeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectionTime(sectionTime: SectionTimeEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectionTimes(sectionTimes: List<SectionTimeEntity>)
    
    @Update
    suspend fun updateSectionTime(sectionTime: SectionTimeEntity)
    
    @Delete
    suspend fun deleteSectionTime(sectionTime: SectionTimeEntity)
    
    @Query("DELETE FROM section_times")
    suspend fun deleteAllSectionTimes()
}
