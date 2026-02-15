package com.example.tendaysplan

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序入口类
 * 用于Hilt依赖注入
 */
@HiltAndroidApp
class TenDaysPlanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
