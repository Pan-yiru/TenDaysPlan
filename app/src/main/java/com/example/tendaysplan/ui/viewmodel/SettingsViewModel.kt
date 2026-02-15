package com.example.tendaysplan.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tendaysplan.data.repository.CycleRepository
import com.example.tendaysplan.data.repository.DayRecordRepository
import com.example.tendaysplan.utils.DataExportHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * 设置页面ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val dayRecordRepository: DayRecordRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val exportHelper = DataExportHelper(
        cycleRepository,
        dayRecordRepository,
        application
    )

    private val _exportResult = MutableStateFlow<ExportResult?>(null)
    val exportResult: StateFlow<ExportResult?> = _exportResult.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    sealed class ExportResult {
        data class Success(val uri: Uri, val recordCount: Int) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    sealed class ImportResult {
        data class Success(val cycleCount: Int, val recordCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    /**
     * 导出数据为JSON
     */
    fun exportData() {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = exportHelper.exportToJson()
                _exportResult.value = ExportResult.Success(result.first, result.second)
            } catch (e: Exception) {
                _exportResult.value = ExportResult.Error(e.message ?: "导出失败")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * 导入JSON数据
     */
    fun importData(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val content = readUriContent(uri)
                val result = exportHelper.importFromJson(content)
                _importResult.value = ImportResult.Success(result.first, result.second)
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "导入失败")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * 清空所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                cycleRepository.deleteAllCycles()
                dayRecordRepository.deleteAllDayRecords()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 读取URI内容
     */
    private fun readUriContent(uri: Uri): String {
        val content = StringBuilder()
        application.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line)
                }
            }
        }
        return content.toString()
    }

    /**
     * 重置结果状态
     */
    fun resetResults() {
        _exportResult.value = null
        _importResult.value = null
    }
}
