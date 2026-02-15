package com.example.tendaysplan.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日期工具类
 */
object DateUtils {

    private const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    private const val DISPLAY_DATE_FORMAT_PATTERN = "MM月dd日"
    private const val DISPLAY_DATE_WITH_WEEKDAY_PATTERN = "MM月dd日 EEEE"

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)
    val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT_PATTERN)
    val displayDateWithWeekdayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DISPLAY_DATE_WITH_WEEKDAY_PATTERN)

    /**
     * 根据日期计算周期序号（1-36）
     */
    fun calculateCycleNumber(date: LocalDate): Int {
        val dayOfYear = date.dayOfYear
        return ((dayOfYear - 1) / 10) + 1
    }

    /**
     * 根据日期计算周期序号（1-36）
     * @param dateStr 日期字符串（yyyy-MM-dd格式）
     */
    fun calculateCycleNumber(dateStr: String): Int {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return calculateCycleNumber(date)
    }

    /**
     * 计算上一个周期的同一天
     */
    fun getPreviousCycleSameDay(date: LocalDate): LocalDate {
        return date.minusDays(10)
    }

    /**
     * 计算上一个周期的同一天
     * @param dateStr 日期字符串（yyyy-MM-dd格式）
     */
    fun getPreviousCycleSameDay(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return getPreviousCycleSameDay(date).format(dateFormatter)
    }

    /**
     * 生成指定年份的所有周期日期范围
     */
    fun generateYearCycles(year: Int): List<Pair<LocalDate, LocalDate>> {
        val cycles = mutableListOf<Pair<LocalDate, LocalDate>>()
        val baseDate = LocalDate.of(year, 1, 1)

        repeat(36) { index ->
            val startDate = baseDate.plusDays(index * 10L)
            val endDate = startDate.plusDays(9)
            cycles.add(Pair(startDate, endDate))
        }

        return cycles
    }

    /**
     * 格式化日期为显示格式
     */
    fun formatDateForDisplay(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return date.format(displayDateFormatter)
    }

    /**
     * 格式化日期为显示格式（包含星期）
     */
    fun formatDateWithWeekday(dateStr: String): String {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return date.format(displayDateWithWeekdayFormatter)
    }

    /**
     * 格式化日期范围
     */
    fun formatDateRange(startDateStr: String, endDateStr: String): String {
        val startDate = LocalDate.parse(startDateStr, dateFormatter)
        val endDate = LocalDate.parse(endDateStr, dateFormatter)
        return "${startDate.format(displayDateFormatter)} - ${endDate.format(displayDateFormatter)}"
    }

    /**
     * 判断两个日期是否在同一年
     */
    fun isSameYear(date1Str: String, date2Str: String): Boolean {
        val date1 = LocalDate.parse(date1Str, dateFormatter)
        val date2 = LocalDate.parse(date2Str, dateFormatter)
        return date1.year == date2.year
    }

    /**
     * 判断日期是否是今天
     */
    fun isToday(dateStr: String): Boolean {
        val date = LocalDate.parse(dateStr, dateFormatter)
        return date.isEqual(LocalDate.now())
    }

    /**
     * 获取当前日期字符串
     */
    fun getCurrentDateStr(): String {
        return LocalDate.now().format(dateFormatter)
    }

    /**
     * 获取当前年份
     */
    fun getCurrentYear(): Int {
        return LocalDate.now().year
    }
}
