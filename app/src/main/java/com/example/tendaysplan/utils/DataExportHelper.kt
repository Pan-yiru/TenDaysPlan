package com.example.tendaysplan.utils

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.tendaysplan.data.model.CycleEntity
import com.example.tendaysplan.data.model.DayRecordEntity
import com.example.tendaysplan.data.repository.CycleRepository
import com.example.tendaysplan.data.repository.DayRecordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 导出数据模型
 */
@Serializable
data class ExportData(
    val version: Int = 1,
    val exportDate: String,
    val cycles: List<CycleExport>,
    val dayRecords: List<DayRecordExport>
)

@Serializable
data class CycleExport(
    val cycleId: Long,
    val year: Int,
    val cycleNumber: Int,
    val startDate: String,
    val endDate: String,
    val goal1: String? = null,
    val goal2: String? = null,
    val goal3: String? = null
)

@Serializable
data class DayRecordExport(
    val date: String,
    val cycleId: Long,
    val dayInCycle: Int,
    val task1: String? = null,
    val task2: String? = null,
    val task3: String? = null,
    val task4: String? = null,
    val task5: String? = null,
    val task6: String? = null
)

/**
 * 数据导入导出辅助类
 */
@Singleton
class DataExportHelper @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val dayRecordRepository: DayRecordRepository,
    @ApplicationContext private val context: Application
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * 导出数据为JSON文件
     * @return Pair<文件URI, 记录数量>
     */
    suspend fun exportToJson(): Pair<Uri, Int> = withContext(Dispatchers.IO) {
        // 收集所有数据
        val cycles = cycleRepository.getAllCycles().first()
        val dayRecords = dayRecordRepository.getAllDayRecords().first()

        // 转换为导出格式
        val cyclesExport = cycles.map { cycle ->
            CycleExport(
                cycleId = cycle.cycleId,
                year = cycle.year,
                cycleNumber = cycle.cycleNumber,
                startDate = cycle.startDate,
                endDate = cycle.endDate,
                goal1 = cycle.goal1,
                goal2 = cycle.goal2,
                goal3 = cycle.goal3
            )
        }

        val dayRecordsExport = dayRecords.map { record ->
            DayRecordExport(
                date = record.date,
                cycleId = record.cycleId,
                dayInCycle = record.dayInCycle,
                task1 = record.task1,
                task2 = record.task2,
                task3 = record.task3,
                task4 = record.task4,
                task5 = record.task5,
                task6 = record.task6
            )
        }

        // 创建导出数据
        val exportData = ExportData(
            exportDate = java.time.LocalDate.now().toString(),
            cycles = cyclesExport,
            dayRecords = dayRecordsExport
        )

        // 序列化为JSON
        val jsonString = json.encodeToString(exportData)

        // Base64编码
        val encodedString = android.util.Base64.encodeToString(
            jsonString.toByteArray(),
            android.util.Base64.DEFAULT
        )

        // 保存到文件
        val uri = saveToFile(encodedString)

        Pair(uri, cycles.size + dayRecords.size)
    }

    /**
     * 从JSON导入数据
     * @return Pair<周期数量, 记录数量>
     */
    suspend fun importFromJson(jsonString: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        try {
            // Base64解码
            val decodedBytes = android.util.Base64.decode(jsonString, android.util.Base64.DEFAULT)
            val decodedString = String(decodedBytes)

            // 反序列化
            val exportData = json.decodeFromString<ExportData>(decodedString)

            // 清空现有数据
            cycleRepository.deleteAllCycles()
            dayRecordRepository.deleteAllDayRecords()

            // 导入周期数据
            val cycles = exportData.cycles.map { cycleExport ->
                CycleEntity(
                    cycleId = cycleExport.cycleId,
                    year = cycleExport.year,
                    cycleNumber = cycleExport.cycleNumber,
                    startDate = cycleExport.startDate,
                    endDate = cycleExport.endDate,
                    goal1 = cycleExport.goal1,
                    goal2 = cycleExport.goal2,
                    goal3 = cycleExport.goal3
                )
            }
            cycleRepository.insertCycles(cycles)

            // 导入每日记录数据
            val dayRecords = exportData.dayRecords.map { recordExport ->
                DayRecordEntity(
                    date = recordExport.date,
                    cycleId = recordExport.cycleId,
                    dayInCycle = recordExport.dayInCycle,
                    task1 = recordExport.task1,
                    task2 = recordExport.task2,
                    task3 = recordExport.task3,
                    task4 = recordExport.task4,
                    task5 = recordExport.task5,
                    task6 = recordExport.task6
                )
            }
            dayRecordRepository.insertDayRecords(dayRecords)

            Pair(cycles.size, dayRecords.size)
        } catch (e: Exception) {
            throw Exception("导入失败: ${e.message}", e)
        }
    }

    /**
     * 保存到文件
     */
    private fun saveToFile(content: String): Uri {
        val fileName = "tendaysplan_backup_${System.currentTimeMillis()}.txt"

        val resolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            } else {
                @Suppress("DEPRECATION")
                put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/$fileName")
            }
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            ?: throw Exception("无法创建文件")

        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(content.toByteArray())
            outputStream.flush()
        } ?: throw Exception("无法写入文件")

        return uri
    }

    /**
     * 验证导入数据的格式
     */
    fun validateImportData(jsonString: String): Boolean {
        return try {
            val decodedBytes = android.util.Base64.decode(jsonString, android.util.Base64.DEFAULT)
            val decodedString = String(decodedBytes)
            json.decodeFromString<ExportData>(decodedString)
            true
        } catch (e: Exception) {
            false
        }
    }
}
