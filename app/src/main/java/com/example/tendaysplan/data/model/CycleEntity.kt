package com.example.tendaysplan.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 周期实体
 * 一个周期包含10天，每年有36个周期
 */
@Entity(
    tableName = "cycles",
    indices = [Index(value = ["year", "cycleNumber"], unique = true)]
)
data class CycleEntity(
    @PrimaryKey(autoGenerate = false)
    val cycleId: Long,

    val year: Int,
    val cycleNumber: Int, // 1-36
    val startDate: String, // ISO格式: yyyy-MM-dd
    val endDate: String,   // ISO格式: yyyy-MM-dd

    // 最多3个目标
    val goal1: String? = null,
    val goal2: String? = null,
    val goal3: String? = null
)
