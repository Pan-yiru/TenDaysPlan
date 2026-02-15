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
 * 今天页面ViewModel
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val dayRecordRepository: DayRecordRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentCycle = MutableStateFlow<CycleEntity?>(null)
    val currentCycle: StateFlow<CycleEntity?> = _currentCycle.asStateFlow()

    private val _todayDayRecord = MutableStateFlow<DayRecordEntity?>(null)
    val todayDayRecord: StateFlow<DayRecordEntity?> = _todayDayRecord.asStateFlow()

    private val _dayInCycle = MutableStateFlow(1)
    val dayInCycle: StateFlow<Int> = _dayInCycle.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTodayData()
    }

    /**
     * 加载今天的数据
     */
    fun loadTodayData() {
        val today = LocalDate.now()
        loadDataForDate(today.toString())
    }

    /**
     * 加载指定日期的数据
     */
    fun loadDataForDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val localDate = LocalDate.parse(date)
                val year = localDate.year

                // 确保该年份数据存在
                cycleRepository.generateCyclesForYear(year)

                // 等待数据生成完成
                cycleRepository.getCyclesByYear(year).first { it.isNotEmpty() }

                // 获取该日期所属的周期
                val cycle = cycleRepository.getCycleByDate(date, year)
                if (cycle != null) {
                    _currentCycle.value = cycle

                    // 计算这是周期的第几天
                    val dayOfYear = localDate.dayOfYear
                    val cycleStartDayOfYear = LocalDate.parse(cycle.startDate).dayOfYear
                    val dayInCycleValue = dayOfYear - cycleStartDayOfYear + 1
                    _dayInCycle.value = dayInCycleValue

                    // 获取或生成每日记录
                    var dayRecord = dayRecordRepository.getDayRecordByDate(date)
                    if (dayRecord == null) {
                        // 生成该周期的所有记录
                        dayRecordRepository.generateDayRecordsForCycle(cycle.cycleId, cycle.startDate)
                        dayRecord = dayRecordRepository.getDayRecordByDate(date)
                    }
                    _todayDayRecord.value = dayRecord
                } else {
                    _currentCycle.value = null
                    _todayDayRecord.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
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
        task6: String?
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
                            task6 = task6?.ifBlank { null }
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadDataForDate(_selectedDate.value)
    }

    /**
     * 更新任务完成状态
     */
    fun updateTaskCompletion(
        date: String,
        taskIndex: Int,
        completed: Boolean
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = dayRecordRepository.getDayRecordByDate(date)
                existingRecord?.let {
                    val updatedRecord = when (taskIndex) {
                        1 -> it.copy(task1Completed = completed)
                        2 -> it.copy(task2Completed = completed)
                        3 -> it.copy(task3Completed = completed)
                        4 -> it.copy(task4Completed = completed)
                        5 -> it.copy(task5Completed = completed)
                        6 -> it.copy(task6Completed = completed)
                        else -> it
                    }
                    dayRecordRepository.updateDayRecord(updatedRecord)
                    // 更新UI上的数据
                    _todayDayRecord.value = updatedRecord
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新单个任务的文本（即时保存）
     */
    fun updateTaskText(
        date: String,
        taskIndex: Int,
        text: String
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = dayRecordRepository.getDayRecordByDate(date)
                existingRecord?.let {
                    val updatedRecord = when (taskIndex) {
                        1 -> it.copy(task1 = text.ifBlank { null })
                        2 -> it.copy(task2 = text.ifBlank { null })
                        3 -> it.copy(task3 = text.ifBlank { null })
                        4 -> it.copy(task4 = text.ifBlank { null })
                        5 -> it.copy(task5 = text.ifBlank { null })
                        6 -> it.copy(task6 = text.ifBlank { null })
                        else -> it
                    }
                    dayRecordRepository.updateDayRecord(updatedRecord)
                    // 更新UI上的数据
                    _todayDayRecord.value = updatedRecord
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 删除任务（将任务文本设为null）
     */
    fun deleteTask(
        date: String,
        taskIndex: Int
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = dayRecordRepository.getDayRecordByDate(date)
                existingRecord?.let {
                    val updatedRecord = when (taskIndex) {
                        1 -> it.copy(task1 = null, task1Completed = false,
                            task1Name = null, task1Detail = null, task1Time = null)
                        2 -> it.copy(task2 = null, task2Completed = false,
                            task2Name = null, task2Detail = null, task2Time = null)
                        3 -> it.copy(task3 = null, task3Completed = false,
                            task3Name = null, task3Detail = null, task3Time = null)
                        4 -> it.copy(task4 = null, task4Completed = false,
                            task4Name = null, task4Detail = null, task4Time = null)
                        5 -> it.copy(task5 = null, task5Completed = false,
                            task5Name = null, task5Detail = null, task5Time = null)
                        6 -> it.copy(task6 = null, task6Completed = false,
                            task6Name = null, task6Detail = null, task6Time = null)
                        else -> it
                    }
                    dayRecordRepository.updateDayRecord(updatedRecord)
                    // 更新UI上的数据
                    _todayDayRecord.value = updatedRecord
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 更新任务详细信息（包括显示文本、事件名称、时间）
     */
    fun updateTaskDetails(
        date: String,
        taskIndex: Int,
        displayText: String,
        name: String,
        time: String
    ) {
        viewModelScope.launch {
            try {
                val existingRecord = dayRecordRepository.getDayRecordByDate(date)
                existingRecord?.let {
                    val updatedRecord = when (taskIndex) {
                        1 -> it.copy(task1 = displayText, task1Name = name.ifBlank { null }, task1Time = time.ifBlank { null })
                        2 -> it.copy(task2 = displayText, task2Name = name.ifBlank { null }, task2Time = time.ifBlank { null })
                        3 -> it.copy(task3 = displayText, task3Name = name.ifBlank { null }, task3Time = time.ifBlank { null })
                        4 -> it.copy(task4 = displayText, task4Name = name.ifBlank { null }, task4Time = time.ifBlank { null })
                        5 -> it.copy(task5 = displayText, task5Name = name.ifBlank { null }, task5Time = time.ifBlank { null })
                        6 -> it.copy(task6 = displayText, task6Name = name.ifBlank { null }, task6Time = time.ifBlank { null })
                        else -> it
                    }
                    dayRecordRepository.updateDayRecord(updatedRecord)
                    // 更新UI上的数据
                    _todayDayRecord.value = updatedRecord
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
