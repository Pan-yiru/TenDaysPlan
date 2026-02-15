package com.example.tendaysplan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tendaysplan.data.model.DayRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 每日记录数据访问对象
 */
@Dao
interface DayRecordDao {
    /**
     * 获取所有每日记录
     */
    @Query("SELECT * FROM day_records ORDER BY date")
    fun getAllDayRecords(): Flow<List<DayRecordEntity>>

    /**
     * 根据日期范围获取每日记录
     */
    @Query("SELECT * FROM day_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getDayRecordsByDateRange(startDate: String, endDate: String): Flow<List<DayRecordEntity>>

    /**
     * 根据周期ID获取每日记录
     */
    @Query("SELECT * FROM day_records WHERE cycleId = :cycleId ORDER BY dayInCycle")
    fun getDayRecordsByCycleId(cycleId: Long): Flow<List<DayRecordEntity>>

    /**
     * 根据日期获取每日记录
     */
    @Query("SELECT * FROM day_records WHERE date = :date")
    suspend fun getDayRecordByDate(date: String): DayRecordEntity?

    /**
     * 观察特定日期的每日记录
     */
    @Query("SELECT * FROM day_records WHERE date = :date")
    fun observeDayRecord(date: String): Flow<DayRecordEntity?>

    /**
     * 根据年份获取每日记录
     */
    @Query("SELECT * FROM day_records WHERE date LIKE :year || '-%' ORDER BY date")
    fun getDayRecordsByYear(year: Int): Flow<List<DayRecordEntity>>

    /**
     * 插入每日记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayRecord(dayRecord: DayRecordEntity)

    /**
     * 批量插入每日记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayRecords(dayRecords: List<DayRecordEntity>)

    /**
     * 更新每日记录
     */
    @Update
    suspend fun updateDayRecord(dayRecord: DayRecordEntity)

    /**
     * 删除每日记录
     */
    @Delete
    suspend fun deleteDayRecord(dayRecord: DayRecordEntity)

    /**
     * 根据日期删除每日记录
     */
    @Query("DELETE FROM day_records WHERE date = :date")
    suspend fun deleteDayRecordByDate(date: String)

    /**
     * 删除指定周期的所有每日记录
     */
    @Query("DELETE FROM day_records WHERE cycleId = :cycleId")
    suspend fun deleteDayRecordsByCycleId(cycleId: Long)

    /**
     * 删除所有每日记录
     */
    @Query("DELETE FROM day_records")
    suspend fun deleteAllDayRecords()
}
