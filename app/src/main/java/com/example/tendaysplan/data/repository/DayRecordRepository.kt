package com.example.tendaysplan.data.repository

import com.example.tendaysplan.data.dao.DayRecordDao
import com.example.tendaysplan.data.model.DayRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 每日记录数据仓库
 * 提供每日记录数据的业务逻辑层
 */
@Singleton
class DayRecordRepository @Inject constructor(
    private val dayRecordDao: DayRecordDao
) {
    /**
     * 获取所有每日记录
     */
    fun getAllDayRecords(): Flow<List<DayRecordEntity>> = dayRecordDao.getAllDayRecords()

    /**
     * 根据日期范围获取每日记录
     */
    fun getDayRecordsByDateRange(startDate: String, endDate: String): Flow<List<DayRecordEntity>> =
        dayRecordDao.getDayRecordsByDateRange(startDate, endDate)

    /**
     * 根据周期ID获取每日记录
     */
    fun getDayRecordsByCycleId(cycleId: Long): Flow<List<DayRecordEntity>> =
        dayRecordDao.getDayRecordsByCycleId(cycleId)

    /**
     * 根据日期获取每日记录
     */
    suspend fun getDayRecordByDate(date: String): DayRecordEntity? =
        dayRecordDao.getDayRecordByDate(date)

    /**
     * 观察特定日期的每日记录
     */
    fun observeDayRecord(date: String): Flow<DayRecordEntity?> =
        dayRecordDao.observeDayRecord(date)

    /**
     * 根据年份获取每日记录
     */
    fun getDayRecordsByYear(year: Int): Flow<List<DayRecordEntity>> =
        dayRecordDao.getDayRecordsByYear(year)

    /**
     * 插入每日记录
     */
    suspend fun insertDayRecord(dayRecord: DayRecordEntity) =
        dayRecordDao.insertDayRecord(dayRecord)

    /**
     * 批量插入每日记录
     */
    suspend fun insertDayRecords(dayRecords: List<DayRecordEntity>) =
        dayRecordDao.insertDayRecords(dayRecords)

    /**
     * 更新每日记录
     */
    suspend fun updateDayRecord(dayRecord: DayRecordEntity) =
        dayRecordDao.updateDayRecord(dayRecord)

    /**
     * 删除每日记录
     */
    suspend fun deleteDayRecord(dayRecord: DayRecordEntity) =
        dayRecordDao.deleteDayRecord(dayRecord)

    /**
     * 根据日期删除每日记录
     */
    suspend fun deleteDayRecordByDate(date: String) =
        dayRecordDao.deleteDayRecordByDate(date)

    /**
     * 删除指定周期的所有每日记录
     */
    suspend fun deleteDayRecordsByCycleId(cycleId: Long) =
        dayRecordDao.deleteDayRecordsByCycleId(cycleId)

    /**
     * 删除所有每日记录
     */
    suspend fun deleteAllDayRecords() =
        dayRecordDao.deleteAllDayRecords()

    /**
     * 为指定周期生成并插入10天记录
     */
    suspend fun generateDayRecordsForCycle(cycleId: Long, startDate: String) {
        // 先删除已存在的该周期数据
        deleteDayRecordsByCycleId(cycleId)

        val dayRecords = mutableListOf<DayRecordEntity>()
        val localDate = java.time.LocalDate.parse(startDate)

        repeat(10) { index ->
            val date = localDate.plusDays(index.toLong())
            dayRecords.add(
                DayRecordEntity(
                    date = date.toString(),
                    cycleId = cycleId,
                    dayInCycle = index + 1
                )
            )
        }

        insertDayRecords(dayRecords)
    }

    /**
     * 获取某一天的所有任务（作为列表返回，便于使用）
     */
    suspend fun getTasksForDate(date: String): List<String> {
        val record = getDayRecordByDate(date) ?: return emptyList()
        return listOfNotNull(
            record.task1,
            record.task2,
            record.task3,
            record.task4,
            record.task5,
            record.task6
        ).filter { !it.isNullOrBlank() }
    }
}
