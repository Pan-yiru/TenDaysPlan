package com.example.tendaysplan.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tendaysplan.data.model.CycleEntity
import com.example.tendaysplan.data.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 年总览页面ViewModel
 */
@HiltViewModel
class YearOverviewViewModel @Inject constructor(
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(java.time.LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _cycles = MutableStateFlow<List<CycleEntity>>(emptyList())
    val cycles: StateFlow<List<CycleEntity>> = _cycles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCyclesForYear(_selectedYear.value)
    }

    /**
     * 加载指定年份的周期
     */
    fun loadCyclesForYear(year: Int) {
        _selectedYear.value = year
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 确保该年份数据存在
                cycleRepository.generateCyclesForYear(year)

                // 等待数据生成完成，然后加载
                cycleRepository.getCyclesByYear(year).collect { cycleList ->
                    _cycles.value = cycleList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    /**
     * 切换到上一年
     */
    fun previousYear() {
        loadCyclesForYear(_selectedYear.value - 1)
    }

    /**
     * 切换到下一年
     */
    fun nextYear() {
        loadCyclesForYear(_selectedYear.value + 1)
    }

    /**
     * 更新周期目标
     */
    fun updateCycleGoals(
        cycleId: Long,
        goal1: String?,
        goal2: String?,
        goal3: String?
    ) {
        viewModelScope.launch {
            try {
                val cycle = cycleRepository.getCycleById(cycleId)
                cycle?.let {
                    cycleRepository.updateCycle(
                        it.copy(
                            goal1 = goal1?.ifBlank { null },
                            goal2 = goal2?.ifBlank { null },
                            goal3 = goal3?.ifBlank { null }
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取周期列表（用于UI展示）
     */
    fun getCyclesForYear(year: Int) = cycleRepository.getCyclesByYear(year)
}
