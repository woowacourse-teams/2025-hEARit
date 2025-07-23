package com.onair.hearit.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.CategoryRemoteDataSourceImpl
import com.onair.hearit.data.datasource.HearitRemoteDataSourceImpl
import com.onair.hearit.data.datasource.MemberRemoteDataSourceImpl
import com.onair.hearit.data.datasource.local.HearitLocalDataSourceImpl
import com.onair.hearit.data.repository.CategoryRepositoryImpl
import com.onair.hearit.data.repository.DataStoreRepositoryImpl
import com.onair.hearit.data.repository.HearitRepositoryImpl
import com.onair.hearit.data.repository.MemberRepositoryImpl
import com.onair.hearit.data.repository.RecentHearitRepositoryImpl
import com.onair.hearit.di.DatabaseProvider
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val categoryDataSource = CategoryRemoteDataSourceImpl(NetworkProvider.categoryService)
        val categoryRepository = CategoryRepositoryImpl(categoryDataSource)
        val dataStoreRepository = DataStoreRepositoryImpl(context)
        val hearitLocalDataSource = HearitLocalDataSourceImpl(DatabaseProvider.hearitDao)
        val recentHearitRepository = RecentHearitRepositoryImpl(hearitLocalDataSource)
        val hearitRemoteDataSource = HearitRemoteDataSourceImpl(NetworkProvider.hearitService)
        val hearitRepository = HearitRepositoryImpl(hearitRemoteDataSource)
        val memberRemoteDataSource = MemberRemoteDataSourceImpl(NetworkProvider.memberService)
        val memberRepository = MemberRepositoryImpl(memberRemoteDataSource)
        return HomeViewModel(
            categoryRepository,
            dataStoreRepository,
            hearitRepository,
            memberRepository,
            recentHearitRepository,
        ) as T
    }
}
