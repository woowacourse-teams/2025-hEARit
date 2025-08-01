package com.onair.hearit.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MemberRemoteDataSourceImpl
import com.onair.hearit.data.repository.DataStoreRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MemberRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val context: Context,
    private val crashlyticsLogger: CrashlyticsLogger,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val dataStoreRepository = DataStoreRepositoryImpl(context, crashlyticsLogger)
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource, crashlyticsLogger)
        val memberRemoteDataSource = MemberRemoteDataSourceImpl(NetworkProvider.memberService)
        val memberRepository = MemberRepositoryImpl(memberRemoteDataSource, crashlyticsLogger)
        return HomeViewModel(
            dataStoreRepository,
            hearitRepository,
            memberRepository,
        ) as T
    }
}
