package com.onair.hearit.repository

import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.local.UserLocalDataSource
import com.onair.hearit.data.datasource.remote.UserRemoteDataSource
import com.onair.hearit.data.dto.UserInfoResponse
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.toDomainResult
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.domain.repository.UserRepository
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

class UserRepositoryTest {
    private lateinit var userLocalDataSource: UserLocalDataSource
    private lateinit var userRemoteDataSource: UserRemoteDataSource
    private lateinit var repository: UserRepositoryWithoutMutex

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
        repository = UserRepositoryWithoutMutex(userLocalDataSource, userRemoteDataSource)

        coEvery { userLocalDataSource.getUserInfo() } returns Result.failure(Exception("No data"))
    }

    @Test
    fun `동시 호출 시 race condition 발생 - API 중복 호출`() =
        runTest {
            // Given
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                delay(100)
                NetworkResult.Success(mockUserInfoResponse)
            }

            // When: 동시에 두 번 호출
            val job1 = async { repository.getUserInfo() }
            val job2 = async { repository.getUserInfo() }

            awaitAll(job1, job2)

            // Then: API가 2번 호출됨
            coVerify(exactly = 2) { userRemoteDataSource.getUserInfo() }
        }

    @Test
    fun `동시에 여러 번 호출 시 race condition 발생`() =
        runTest {
            // Given
            var apiCallCount = 0
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                apiCallCount++
                delay(50)
                NetworkResult.Success(mockUserInfoResponse)
            }

            // When: 10개의 코루틴이 동시에 호출
            val jobs =
                List(10) {
                    async { repository.getUserInfo() }
                }
            val results = jobs.awaitAll()

            // Then: 여러 번 호출됨 (Mutex 없으면 2번 이상)
            assertTrue(apiCallCount >= 2, "실제 호출 횟수: $apiCallCount")
            println("실제 API 호출 횟수: $apiCallCount")

            // 모든 결과는 성공
            assertTrue(results.all { it.isSuccess })
        }

    @Test
    fun `캐시가 있을 때는 race condition이 발생하지 않음`() =
        runTest {
            // Given
            coEvery { userRemoteDataSource.getUserInfo() } returns
                NetworkResult.Success(mockUserInfoResponse)

            // 첫 호출로 캐시 생성
            val firstResult = repository.getUserInfo()
            assertTrue(firstResult.isSuccess) // 첫 호출이 성공했는지 확인
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }

            // When: 캐시가 있는 상태에서 동시 호출
            val job1 = async { repository.getUserInfo() }
            val job2 = async { repository.getUserInfo() }

            awaitAll(job1, job2)

            // Then: 여전히 1번만 호출됨 (캐시 사용)
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
        }

    @Test
    fun `로컬 데이터가 있을 때도 race condition 발생 가능`() =
        runTest {
            // Given: 로컬에 데이터 있음
            coEvery { userLocalDataSource.getUserInfo() } coAnswers {
                delay(50) // 로컬 조회도 시간이 걸린다고 가정
                Result.success(mockUserInfo)
            }

            // When: 동시 호출
            val job1 = async { repository.getUserInfo() }
            val job2 = async { repository.getUserInfo() }

            awaitAll(job1, job2)

            // Then: 로컬이 2번 호출될 수 있음
            coVerify(atLeast = 2) { userLocalDataSource.getUserInfo() }
        }
}

private class UserRepositoryWithoutMutex(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserRepository {
    private var cachedUserInfo: UserInfo? = null

    override fun getCachedUserInfo(): UserInfo? = null

    override suspend fun getUserInfo(): Result<UserInfo> =
        runCatching {
            cachedUserInfo?.let { return@runCatching it }

            val local =
                userLocalDataSource
                    .getUserInfo()
                    .getOrNull()

            if (local != null) {
                cachedUserInfo = local
                return@runCatching local
            }

            val remote =
                userRemoteDataSource
                    .getUserInfo()
                    .toDomainResult { it.toDomain() }
                    .getOrThrow()

            userLocalDataSource.saveUserInfo(remote).getOrThrow()
            cachedUserInfo = remote

            remote
        }

    override suspend fun getOrCreateDeviceId(): Result<String> = Result.success("")

    override suspend fun clearUserData(): Result<Unit> = Result.success(Unit)
}
