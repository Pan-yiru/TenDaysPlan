package com.example.tendaysplan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tendaysplan.data.dao.CycleDao
import com.example.tendaysplan.data.dao.DayRecordDao
import com.example.tendaysplan.data.model.CycleEntity
import com.example.tendaysplan.data.model.DayRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 主数据库类
 * 包含两个表：cycles 和 day_records
 */
@Database(
    entities = [CycleEntity::class, DayRecordEntity::class],
    version = 4,  // 添加任务详细字段（名称、细节、时间）
    exportSchema = false
)
abstract class TenDaysPlanDatabase : RoomDatabase() {

    /**
     * 获取周期DAO
     */
    abstract fun cycleDao(): CycleDao

    /**
     * 获取每日记录DAO
     */
    abstract fun dayRecordDao(): DayRecordDao

    companion object {
        private const val DATABASE_NAME = "tendaysplan_database"

        @Volatile
        private var INSTANCE: TenDaysPlanDatabase? = null

        /**
         * 获取数据库实例
         */
        fun getInstance(context: Context): TenDaysPlanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TenDaysPlanDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 数据库创建时，在IO线程初始化数据
                            val scope = CoroutineScope(Dispatchers.IO)
                            scope.launch {
                                val database = getInstance(context)
                                val currentYear = java.time.LocalDate.now().year
                                try {
                                    // 生成当前年份的36个周期
                                    val baseDate = java.time.LocalDate.of(currentYear, 1, 1)
                                    val cycles = mutableListOf<CycleEntity>()

                                    repeat(36) { index ->
                                        val startDate = baseDate.plusDays(index * 10L)
                                        val endDate = startDate.plusDays(9)
                                        val cycleId = (currentYear * 100L + index + 1)

                                        cycles.add(
                                            CycleEntity(
                                                cycleId = cycleId,
                                                year = currentYear,
                                                cycleNumber = index + 1,
                                                startDate = startDate.toString(),
                                                endDate = endDate.toString()
                                            )
                                        )
                                    }

                                    database.cycleDao().insertCycles(cycles)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
