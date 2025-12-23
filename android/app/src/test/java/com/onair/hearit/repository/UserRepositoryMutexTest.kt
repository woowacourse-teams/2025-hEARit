package com.onair.hearit.repository

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.data.repository.UserRepositoryImpl
import com.onair.hearit.domain.model.UserInfo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class UserRepositoryMutexTest {
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var repository: UserRepositoryImpl

    private val mockUserInfoResponse =
        UserInfoResponse(
            id = 123L,
            nickname = "테스트유저",
            profileImage = "https://example.com/profile.jpg",
        )

    private val mockUserInfo =
        UserInfo(
            id = 123L,
            nickname = "테스트유저",
            profileImage = "https://example.com/profile.jpg",
        )

    @Before
    fun setup() {
        userLocalDataSource = mockk(relaxed = true)
        userRemoteDataSource = mockk(relaxed = true)
        repository = UserRepositoryImpl(userLocalDataSource, userRemoteDataSource)

        coEvery { userLocalDataSource.getUserInfo() } returns Result.failure(Exception("No data"))
    }

    @Test
    fun `동시 호출 시 메모리 캐시 일관성 보장`() =
        runTest {
            // Given
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                delay(100)
                NetworkResult.Success(mockUserInfoResponse)
            }

            // When: 100개 동시 호출
            val results =
                List(100) {
                    async { repository.getUserInfo() }
                }.awaitAll()

            // Then: 모든 결과가 동일 (race condition 없음)
            assertTrue(results.all { it.isSuccess })
            val firstResult = results[0].getOrNull()
            assertTrue(results.all { it.getOrNull() == firstResult })
            println("✅ 메모리 캐시 일관성 유지")
        }

    @Test
    fun `캐시가 있을 때는 네트워크 호출 없음`() =
        runTest {
            // Given: 캐시 생성
            coEvery {
                userRemoteDataSource.getUserInfo()
            } returns NetworkResult.Success(mockUserInfoResponse)

            repository.getUserInfo() // 첫 호출로 캐시 생성

            // When: 캐시가 있는 상태에서 호출
            repository.getUserInfo()
            repository.getUserInfo()

            // Then: 추가 API 호출 없음
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
            println("✅ 캐시 히트 - 추가 네트워크 호출 없음")
        }

    @Test
    fun `캐시 클리어 후에는 다시 네트워크 호출`() =
        runTest {
            // Given
            coEvery {
                userRemoteDataSource.getUserInfo()
            } returns NetworkResult.Success(mockUserInfoResponse)

            repository.getUserInfo()
            repository.clearUserData()

            // When: 캐시 클리어 후 재호출
            repository.getUserInfo()

            // Then: 2번 호출됨
            coVerify(exactly = 2) { userRemoteDataSource.getUserInfo() }
            println("✅ 캐시 클리어 후 재호출")
        }

    @Test
    fun `API 실패는 캐시하지 않음`() =
        runTest {
            // Given: API 실패
            coEvery {
                userRemoteDataSource.getUserInfo()
            } returns NetworkResult.Failure.Unknown

            // When: 3번 호출
            repository.getUserInfo()
            repository.getUserInfo()
            repository.getUserInfo()

            // Then: 실패는 캐시 안 되므로 3번 호출
            coVerify(exactly = 3) { userRemoteDataSource.getUserInfo() }
            println("✅ 실패는 캐시 안 됨")
        }
}
