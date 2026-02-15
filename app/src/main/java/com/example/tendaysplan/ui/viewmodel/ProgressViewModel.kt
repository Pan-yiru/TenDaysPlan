package com.example.tendaysplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tendaysplan.data.model.CycleEntity
import com.example.tendaysplan.data.model.DayRecordEntity
import com.example.tendaysplan.data.repository.CycleRepository
import com.example.tendaysplan.data.repository.DayRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 执行进度页面ViewModel
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val dayRecordRepository: DayRecordRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _currentCycleIndex = MutableStateFlow(0)
    val currentCycleIndex: StateFlow<Int> = _currentCycleIndex.asStateFlow()

    private val _currentCycle = MutableStateFlow<CycleEntity?>(null)
    val currentCycle: StateFlow<CycleEntity?> = _currentCycle.asStateFlow()

    private val _dayRecords = MutableStateFlow<List<DayRecordEntity>>(emptyList())
    val dayRecords: StateFlow<List<DayRecordEntity>> = _dayRecords.asStateFlow()

    private val _previousCycleDayRecord = MutableStateFlow<DayRecordEntity?>(null)
    val previousCycleDayRecord: StateFlow<DayRecordEntity?> = _previousCycleDayRecord.asStateFlow()

    private val _selectedDayInCycle = MutableStateFlow(1) // 默认选中第1天
    val selectedDayInCycle: StateFlow<Int> = _selectedDayInCycle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        initializeWithToday()
    }

    /**
     * 初始化为当前日期所在的周期
     */
    private fun initializeWithToday() {
        val today = LocalDate.now()
        _selectedYear.value = today.year
        loadCycleForDate(today.toString())

        // 计算今天是周期的第几天
        val cycle = _currentCycle.value
        if (cycle != null) {
            val dayOfYear = today.dayOfYear
            val cycleStartDayOfYear = LocalDate.parse(cycle.startDate).dayOfYear
            val dayInCycle = dayOfYear - cycleStartDayOfYear + 1
            _selectedDayInCycle.value = dayInCycle.coerceIn(1, 10)
        }
    }

    /**
     * 加载指定日期的周期
     */
    fun loadCycleForDate(date: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val localDate = LocalDate.parse(date)
                val year = localDate.year
                _selectedYear.value = year

                // 确保该年份数据存在
                cycleRepository.generateCyclesForYear(year)

                // 等待数据生成完成
                cycleRepository.getCyclesByYear(year).first { it.isNotEmpty() }

                // 获取该日期所属的周期
                val cycle = cycleRepository.getCycleByDate(date, year)
                if (cycle != null) {
                    _currentCycle.value = cycle
                    _currentCycleIndex.value = cycle.cycleNumber - 1

                    // 计算这是周期的第几天
                    val dayOfYear = localDate.dayOfYear
                    val cycleStartDayOfYear = LocalDate.parse(cycle.startDate).dayOfYear
                    val dayInCycle = dayOfYear - cycleStartDayOfYear + 1

                    // 加载该周期的每日记录
                    loadDayRecordsForCycle(cycle.cycleId, cycle.startDate)

                    // 加载上一个周期同一天的记录
                    loadPreviousCycleDayRecord(cycle, dayInCycle)
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载指定序号的周期
     */
    fun loadCycleByIndex(year: Int, cycleNumber: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedYear.value = year
                _currentCycleIndex.value = cycleNumber - 1

                // 确保该年份数据存在
                cycleRepository.generateCyclesForYear(year)

                // 等待数据生成完成
                cycleRepository.getCyclesByYear(year).first { it.isNotEmpty() }

                val cycle = cycleRepository.getCycleByYearAndNumber(year, cycleNumber)
                if (cycle != null) {
                    _currentCycle.value = cycle

                    // 默认显示该周期的第1天
                    loadDayRecordsForCycle(cycle.cycleId, cycle.startDate)
                    loadPreviousCycleDayRecord(cycle, 1)

                    _isLoading.value = false
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    /**
     * 加载周期的每日记录
     */
    private fun loadDayRecordsForCycle(cycleId: Long, startDate: String) {
        viewModelScope.launch {
            try {
                // 先检查是否有记录
                val existingRecords = dayRecordRepository.getDayRecordsByCycleId(cycleId).first()

                if (existingRecords.isEmpty()) {
                    // 如果没有记录，自动生成
                    dayRecordRepository.generateDayRecordsForCycle(cycleId, startDate)
                }

                // 加载记录
                dayRecordRepository.getDayRecordsByCycleId(cycleId).collect { records ->
                    _dayRecords.value = records
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 加载上一个周期同一天的记录
     * @param currentCycle 当前周期
     * @param dayInCycle 当前是周期的第几天 (1-10)
     */
    private fun loadPreviousCycleDayRecord(currentCycle: CycleEntity, dayInCycle: Int) {
        viewModelScope.launch {
            try {
                // 计算上一个周期
                val previousCycleNumber = if (currentCycle.cycleNumber == 1) {
                    // 如果是第1周期，上一个周期是去年的第36周期
                    if (currentCycle.year > 2024) { // 避免年份太早
                        Pair(currentCycle.year - 1, 36)
                    } else {
                        null
                    }
                } else {
                    // 否则是同年上一周期
                    Pair(currentCycle.year, currentCycle.cycleNumber - 1)
                }

                if (previousCycleNumber != null) {
                    // 确保上一个周期的数据存在
                    cycleRepository.generateCyclesForYear(previousCycleNumber.first)

                    // 获取上一个周期
                    val previousCycle = cycleRepository.getCycleByYearAndNumber(
                        previousCycleNumber.first,
                        previousCycleNumber.second
                    )

                    previousCycle?.let { prevCycle ->
                        // 确保上一个周期的每日记录存在
                        val existingRecords = dayRecordRepository.getDayRecordsByCycleId(prevCycle.cycleId).first()
                        if (existingRecords.isEmpty()) {
                            dayRecordRepository.generateDayRecordsForCycle(prevCycle.cycleId, prevCycle.startDate)
                        }

                        // 计算上一个周期同一天的日期
                        val prevCycleStartDate = LocalDate.parse(prevCycle.startDate)
                        val previousCycleSameDayDate = prevCycleStartDate.plusDays((dayInCycle - 1).toLong())

                        // 获取上一个周期同一天的记录
                        val record = dayRecordRepository.getDayRecordByDate(previousCycleSameDayDate.toString())
                        _previousCycleDayRecord.value = record
                    } ?: run {
                        _previousCycleDayRecord.value = null
                    }
                } else {
                    _previousCycleDayRecord.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _previousCycleDayRecord.value = null
            }
        }
    }

    /**
     * 当用户点击某一天时，重新加载上一个周期同一天
     */
    fun onDaySelected(dayInCycle: Int) {
        val cycle = _currentCycle.value ?: return
        _selectedDayInCycle.value = dayInCycle
        loadPreviousCycleDayRecord(cycle, dayInCycle)
    }

    /**
     * 设置选中的天数（用于水平滑动）
     */
    fun setSelectedDayInCycle(dayInCycle: Int) {
        val cycle = _currentCycle.value ?: return
        val clampedDay = dayInCycle.coerceIn(1, 10)
        _selectedDayInCycle.value = clampedDay
        loadPreviousCycleDayRecord(cycle, clampedDay)
    }

    /**
     * 切换到上一个周期
     */
    fun previousCycle() {
        val currentIndex = _currentCycleIndex.value
        if (currentIndex > 0) {
            loadCycleByIndex(_selectedYear.value, currentIndex)
        }
    }

    /**
     * 切换到下一个周期
     */
    fun nextCycle() {
        val currentIndex = _currentCycleIndex.value
        if (currentIndex < 35) { // 36个周期，索引0-35
            loadCycleByIndex(_selectedYear.value, currentIndex + 2)
        }
    }

    /**
     * 更新每日任务
     */
    fun updateDayTasks(
        date: String,
        task1: String?,
        task2: String?,
        task3: String?,
        task4: String?,
        task5: String?,
        task6: String?,
        task1Name: String? = null,
        task2Name: String? = null,
        task3Name: String? = null,
        task4Name: String? = null,
        task5Name: String? = null,
        task6Name: String? = null,
        task1Time: String? = null,
        task2Time: String? = null,
        task3Time: String? = null,
        task4Time: String? = null,
        task5Time: String? = null,
        task6Time: String? = null
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = dayRecordRepository.getDayRecordByDate(date)
                existingRecord?.let {
                    dayRecordRepository.updateDayRecord(
                        it.copy(
                            task1 = task1?.ifBlank { null },
                            task2 = task2?.ifBlank { null },
                            task3 = task3?.ifBlank { null },
                            task4 = task4?.ifBlank { null },
                            task5 = task5?.ifBlank { null },
                            task6 = task6?.ifBlank { null },
                            task1Name = task1Name?.ifBlank { null } ?: it.task1Name,
                            task2Name = task2Name?.ifBlank { null } ?: it.task2Name,
                            task3Name = task3Name?.ifBlank { null } ?: it.task3Name,
                            task4Name = task4Name?.ifBlank { null } ?: it.task4Name,
                            task5Name = task5Name?.ifBlank { null } ?: it.task5Name,
                            task6Name = task6Name?.ifBlank { null } ?: it.task6Name,
                            task1Time = task1Time?.ifBlank { null } ?: it.task1Time,
                            task2Time = task2Time?.ifBlank { null } ?: it.task2Time,
                            task3Time = task3Time?.ifBlank { null } ?: it.task3Time,
                            task4Time = task4Time?.ifBlank { null } ?: it.task4Time,
                            task5Time = task5Time?.ifBlank { null } ?: it.task5Time,
                            task6Time = task6Time?.ifBlank { null } ?: it.task6Time
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取每日记录
     */
    fun observeDayRecord(date: String) = dayRecordRepository.observeDayRecord(date)

    /**
     * 获取周期数据
     */
    fun getCyclesForYear(year: Int) = cycleRepository.getCyclesByYear(year)
}
