package com.example.tendaysplan.data.repository

import com.example.tendaysplan.data.dao.CycleDao
import com.example.tendaysplan.data.model.CycleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 周期数据仓库
 * 提供周期数据的业务逻辑层
 */
@Singleton
class CycleRepository @Inject constructor(
    private val cycleDao: CycleDao
) {
    /**
     * 获取所有周期
     */
    fun getAllCycles(): Flow<List<CycleEntity>> = cycleDao.getAllCycles()

    /**
     * 根据年份获取周期
     */
    fun getCyclesByYear(year: Int): Flow<List<CycleEntity>> =
        cycleDao.getCyclesByYear(year)

    /**
     * 根据周期ID获取周期
     */
    suspend fun getCycleById(cycleId: Long): CycleEntity? =
        cycleDao.getCycleById(cycleId)

    /**
     * 根据年份和周期序号获取周期
     */
    suspend fun getCycleByYearAndNumber(year: Int, cycleNumber: Int): CycleEntity? =
        cycleDao.getCycleByYearAndNumber(year, cycleNumber)

    /**
     * 插入周期
     */
    suspend fun insertCycle(cycle: CycleEntity): Long =
        cycleDao.insertCycle(cycle)

    /**
     * 批量插入周期
     */
    suspend fun insertCycles(cycles: List<CycleEntity>) =
        cycleDao.insertCycles(cycles)

    /**
     * 更新周期
     */
    suspend fun updateCycle(cycle: CycleEntity) =
        cycleDao.updateCycle(cycle)

    /**
     * 删除周期
     */
    suspend fun deleteCycle(cycle: CycleEntity) =
        cycleDao.deleteCycle(cycle)

    /**
     * 删除指定年份的所有周期
     */
    suspend fun deleteCyclesByYear(year: Int) =
        cycleDao.deleteCyclesByYear(year)

    /**
     * 删除所有周期
     */
    suspend fun deleteAllCycles() =
        cycleDao.deleteAllCycles()

    /**
     * 为指定年份生成并插入36个周期
     */
    suspend fun generateCyclesForYear(year: Int) {
        // 先检查是否已存在该年份数据
        val existingCycle = getCycleByYearAndNumber(year, 1)
        if (existingCycle != null) {
            // 数据已存在，无需重复生成
            return
        }

        val cycles = mutableListOf<CycleEntity>()
        val baseDate = java.time.LocalDate.of(year, 1, 1)

        repeat(36) { index ->
            val startDate = baseDate.plusDays(index * 10L)
            val endDate = startDate.plusDays(9)

            // 使用预先生成的ID: year * 100 + cycleNumber
            // 这样可以确保ID唯一且可预测
            val cycleId = (year * 100L + index + 1)

            cycles.add(
                CycleEntity(
                    cycleId = cycleId,
                    year = year,
                    cycleNumber = index + 1,
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )
            )
        }

        insertCycles(cycles)
    }

    /**
     * 根据日期获取所属周期
     */
    suspend fun getCycleByDate(date: String, year: Int): CycleEntity? {
        val localDate = java.time.LocalDate.parse(date)
        val baseDate = java.time.LocalDate.of(year, 1, 1)
        val dayOfYear = localDate.dayOfYear

        val cycleNumber = ((dayOfYear - 1) / 10) + 1
        return if (cycleNumber <= 36) {
            getCycleByYearAndNumber(year, cycleNumber)
        } else {
            null
        }
    }
}
