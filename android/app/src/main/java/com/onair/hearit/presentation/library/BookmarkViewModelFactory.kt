package com.onair.hearit.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onair.hearit.data.datasource.MemberRemoteDataSourceImpl
import com.onair.hearit.data.repository.MemberRepositoryImpl
import com.onair.hearit.di.NetworkProvider

@Suppress("UNCHECKED_CAST")
class BookmarkViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val memberRemoteDataSource = MemberRemoteDataSourceImpl(NetworkProvider.memberService)
        val memberRepository = MemberRepositoryImpl(memberRemoteDataSource)
        return BookmarkViewModel(memberRepository) as T
    }
}
