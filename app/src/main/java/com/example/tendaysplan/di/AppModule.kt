package com.example.tendaysplan.di

import android.content.Context
import com.example.tendaysplan.data.TenDaysPlanDatabase
import com.example.tendaysplan.data.dao.CycleDao
import com.example.tendaysplan.data.dao.DayRecordDao
import com.example.tendaysplan.data.repository.CycleRepository
import com.example.tendaysplan.data.repository.DayRecordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供数据库实例
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TenDaysPlanDatabase {
        return TenDaysPlanDatabase.getInstance(context)
    }

    /**
     * 提供周期DAO
     */
    @Provides
    @Singleton
    fun provideCycleDao(database: TenDaysPlanDatabase): CycleDao {
        return database.cycleDao()
    }

    /**
     * 提供每日记录DAO
     */
    @Provides
    @Singleton
    fun provideDayRecordDao(database: TenDaysPlanDatabase): DayRecordDao {
        return database.dayRecordDao()
    }

    /**
     * 提供周期Repository
     */
    @Provides
    @Singleton
    fun provideCycleRepository(
        cycleDao: CycleDao
    ): CycleRepository {
        return CycleRepository(cycleDao)
    }

    /**
     * 提供每日记录Repository
     */
    @Provides
    @Singleton
    fun provideDayRecordRepository(
        dayRecordDao: DayRecordDao
    ): DayRecordRepository {
        return DayRecordRepository(dayRecordDao)
    }
}
