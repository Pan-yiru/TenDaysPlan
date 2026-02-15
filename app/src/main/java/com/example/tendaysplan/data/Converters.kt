package com.example.tendaysplan.data

import androidx.room.TypeConverter

/**
 * Room数据库类型转换器
 * 用于处理Room不直接支持的类型
 */
class Converters {
    // 这里可以添加需要的类型转换器
    // 目前我们的实体都使用基本类型，暂时不需要额外的转换器
    // 如果将来需要添加列表或其他复杂类型，可以在这里添加转换方法

    // 示例：
    // @TypeConverter
    // fun fromStringList(value: List<String>?): String? {
    //     return value?.joinToString(",")
    // }
    //
    // @TypeConverter
    // fun toStringList(value: String?): List<String>? {
    //     return value?.split(",")?.toList()
    // }
}
