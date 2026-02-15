package com.example.tendaysplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tendaysplan.data.repository.DayRecordRepository
import com.example.tendaysplan.utils.StatisticsAnalyzer
import com.example.tendaysplan.utils.StatisticsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统计页面ViewModel
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val dayRecordRepository: DayRecordRepository,
    private val statisticsAnalyzer: StatisticsAnalyzer
) : ViewModel() {

    private val _selectedYear = MutableStateFlow<Int?>(null) // null表示所有年份
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _statisticsResults = MutableStateFlow<List<StatisticsResult>>(emptyList())
    val statisticsResults: StateFlow<List<StatisticsResult>> = _statisticsResults.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    init {
        // 默认显示当前年份
        _selectedYear.value = java.time.LocalDate.now().year
    }

    /**
     * 设置筛选年份
     */
    fun setYear(year: Int?) {
        _selectedYear.value = year
    }

    /**
     * 执行统计分析
     */
    fun analyzeStatistics() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val year = _selectedYear.value
                val allRecords = if (year == null) {
                    // 获取所有年份的数据
                    getAllDayRecords()
                } else {
                    // 获取指定年份的数据
                    getDayRecordsForYear(year)
                }

                // 执行统计（结果已按次数降序、次数相同时按时长降序排列）
                val results = statisticsAnalyzer.analyze(allRecords)
                _statisticsResults.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * 获取所有年份的每日记录
     */
    private suspend fun getAllDayRecords(): List<com.example.tendaysplan.data.model.DayRecordEntity> {
        // 使用 first() 获取流的第一个值并立即返回
        return dayRecordRepository.getAllDayRecords().first()
    }

    /**
     * 获取指定年份的每日记录
     */
    private suspend fun getDayRecordsForYear(year: Int): List<com.example.tendaysplan.data.model.DayRecordEntity> {
        // 使用 first() 获取流的第一个值并立即返回
        return dayRecordRepository.getDayRecordsByYear(year).first()
    }
}
