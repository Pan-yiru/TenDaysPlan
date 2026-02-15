package com.example.tendaysplan.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 每日记录实体
 * 每天最多记录6项任务
 */
@Entity(
    tableName = "day_records",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["cycleId"])
    ]
)
data class DayRecordEntity(
    @PrimaryKey
    val date: String, // ISO格式: yyyy-MM-dd

    val cycleId: Long,
    val dayInCycle: Int, // 周期内第几天 (1-10)

    // 最多6个任务
    val task1: String? = null,
    val task2: String? = null,
    val task3: String? = null,
    val task4: String? = null,
    val task5: String? = null,
    val task6: String? = null,

    // 任务完成状态
    val task1Completed: Boolean = false,
    val task2Completed: Boolean = false,
    val task3Completed: Boolean = false,
    val task4Completed: Boolean = false,
    val task5Completed: Boolean = false,
    val task6Completed: Boolean = false,

    // 任务详细信息：事件名称
    val task1Name: String? = null,
    val task2Name: String? = null,
    val task3Name: String? = null,
    val task4Name: String? = null,
    val task5Name: String? = null,
    val task6Name: String? = null,

    // 任务详细信息：事件细节
    val task1Detail: String? = null,
    val task2Detail: String? = null,
    val task3Detail: String? = null,
    val task4Detail: String? = null,
    val task5Detail: String? = null,
    val task6Detail: String? = null,

    // 任务详细信息：任务时间
    val task1Time: String? = null,
    val task2Time: String? = null,
    val task3Time: String? = null,
    val task4Time: String? = null,
    val task5Time: String? = null,
    val task6Time: String? = null
)
