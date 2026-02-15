package com.example.tendaysplan.utils

import com.example.tendaysplan.data.model.DayRecordEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统计分析结果
 */
data class StatisticsResult(
    val name: String,           // 任务名称
    val frequency: Int,         // 出现次数
    val totalHours: Double = 0.0 // 累计时长（小时）
)

/**
 * 统计分析工具类
 * 负责统计相同任务名称的出现次数和累计时长
 */
@Singleton
class StatisticsAnalyzer @Inject constructor() {

    /**
     * 分析任务列表，生成统计结果
     * @param dayRecords 每日记录列表
     * @return 按次数降序、次数相同时按时长降序排列的统计结果
     */
    fun analyze(dayRecords: List<DayRecordEntity>): List<StatisticsResult> {
        val statisticsMap = mutableMapOf<String, TaskStatistics>()

        dayRecords.forEach { record ->
            // 遍历每个任务的名称和时间
            val taskDataList = listOf(
                record.task1Name to record.task1Time,
                record.task2Name to record.task2Time,
                record.task3Name to record.task3Time,
                record.task4Name to record.task4Time,
                record.task5Name to record.task5Time,
                record.task6Name to record.task6Time
            )

            taskDataList.forEach { (taskName, taskTime) ->
                if (!taskName.isNullOrBlank()) {
                    val normalizedKey = normalizeTaskName(taskName)
                    val hours = if (!taskTime.isNullOrBlank()) {
                        extractHours(taskTime)
                    } else {
                        0.0
                    }

                    val stats = statisticsMap.getOrPut(normalizedKey) {
                        TaskStatistics(taskName)
                    }

                    stats.frequency++
                    stats.totalHours += hours
                }
            }
        }

        // 转换为结果列表并排序：先按次数降序，次数相同时按时长降序
        return statisticsMap.values.map { stats ->
            StatisticsResult(
                name = stats.displayName,
                frequency = stats.frequency,
                totalHours = stats.totalHours
            )
        }.sortedWith(compareByDescending<StatisticsResult> { it.frequency }
            .thenByDescending { it.totalHours })
    }

    /**
     * 从时间文本中提取时长（小时）
     * 支持格式：1小时、1hour、1h、30分钟、0.5h等
     */
    private fun extractHours(timeText: String): Double {
        var totalHours = 0.0

        // 匹配小时格式：1小时、1hour、1h、1hours
        val hourPattern = """(\d+(?:\.\d+)?)\s*(?:小时|hour|h|hours)\s*""".toRegex(RegexOption.IGNORE_CASE)
        hourPattern.findAll(timeText).forEach { match ->
            match.groupValues[1].toDoubleOrNull()?.let { totalHours += it }
        }

        // 如果没有找到小时，尝试匹配分钟：30分钟、30min、30m、30minute、30minutes
        if (totalHours == 0.0) {
            val minutePattern = """(\d+(?:\.\d+)?)\s*(?:分钟|minute|min|m|minutes)\s*""".toRegex(RegexOption.IGNORE_CASE)
            minutePattern.find(timeText)?.let { match ->
                match.groupValues[1].toDoubleOrNull()?.let { minutes ->
                    totalHours += minutes / 60.0
                }
            }
        }

        return totalHours
    }

    /**
     * 标准化任务名称（用于匹配相同任务）
     */
    private fun normalizeTaskName(task: String): String {
        return task.trim()
            .lowercase()
            .replace(Regex("""[^\w\u4e00-\u9fff]"""), "") // 移除标点符号
            .replace(Regex("""\s+"""), "") // 移除空格
    }

    /**
     * 任务统计累加器（内部类）
     */
    private class TaskStatistics(
        val displayName: String // 显示名称（原始格式）
    ) {
        var frequency = 0       // 出现次数
        var totalHours = 0.0    // 累计时长（小时）
    }
}
