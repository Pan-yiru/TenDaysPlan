package com.example.tendaysplan.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tendaysplan.data.model.CycleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 周期数据访问对象
 */
@Dao
interface CycleDao {
    /**
     * 获取所有周期
     */
    @Query("SELECT * FROM cycles ORDER BY year, cycleNumber")
    fun getAllCycles(): Flow<List<CycleEntity>>

    /**
     * 根据年份获取所有周期
     */
    @Query("SELECT * FROM cycles WHERE year = :year ORDER BY cycleNumber")
    fun getCyclesByYear(year: Int): Flow<List<CycleEntity>>

    /**
     * 根据周期ID获取周期
     */
    @Query("SELECT * FROM cycles WHERE cycleId = :cycleId")
    suspend fun getCycleById(cycleId: Long): CycleEntity?

    /**
     * 根据年份和周期序号获取周期
     */
    @Query("SELECT * FROM cycles WHERE year = :year AND cycleNumber = :cycleNumber")
    suspend fun getCycleByYearAndNumber(year: Int, cycleNumber: Int): CycleEntity?

    /**
     * 插入周期
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleEntity): Long

    /**
     * 批量插入周期
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycles(cycles: List<CycleEntity>)

    /**
     * 更新周期
     */
    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    /**
     * 删除周期
     */
    @Delete
    suspend fun deleteCycle(cycle: CycleEntity)

    /**
     * 删除指定年份的所有周期
     */
    @Query("DELETE FROM cycles WHERE year = :year")
    suspend fun deleteCyclesByYear(year: Int)

    /**
     * 删除所有周期
     */
    @Query("DELETE FROM cycles")
    suspend fun deleteAllCycles()
}
