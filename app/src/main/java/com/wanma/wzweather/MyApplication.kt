package com.wanma.wzweather

import android.app.Application
import android.content.Context

class MyApplication : Application() {
    companion object {
        //彩云天气token
        const val TOKEN = "DQIRiY6pHqHU8kdY"
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}