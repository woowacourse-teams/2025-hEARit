package com.onair.hearit.presentation.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.MemberRemoteDataSourceImpl
import com.onair.hearit.data.repository.MemberRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class SettingViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val memberRemoteDataSource = MemberRemoteDataSourceImpl(NetworkProvider.memberService)
        val memberRepository = MemberRepositoryImpl(memberRemoteDataSource)
        return SettingViewModel(memberRepository) as T
    }
}
