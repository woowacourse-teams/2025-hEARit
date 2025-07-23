package com.onair.hearit.di

import android.content.Context
import com.onair.hearit.data.database.HearitDao
import com.onair.hearit.data.database.HearitDatabase

object DatabaseProvider {
    private lateinit var appContext: Context
    private const val ERROR_APP_CONTEXT_NOT_INITIALIZE = "appContext가 초기화되지 않았습니다."

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val database: HearitDatabase by lazy {
        check(::appContext.isInitialized) { ERROR_APP_CONTEXT_NOT_INITIALIZE }
        HearitDatabase.getInstance(appContext)
    }

    val hearitDao: HearitDao by lazy {
        database.hearitDao()
    }
}
