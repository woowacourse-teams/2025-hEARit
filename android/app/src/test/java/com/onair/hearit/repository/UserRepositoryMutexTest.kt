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
import kotlin.test.assertEquals
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
    fun `Mutex로 동시 호출 방어 - API 1번만 호출`() =
        runTest {
            // Given
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                delay(100)
                Result.success(NetworkResult.Success(mockUserInfoResponse))
            }

            // When: 동시에 두 번 호출
            val job1 = async { repository.getUserInfo() }
            val job2 = async { repository.getUserInfo() }

            val results = awaitAll(job1, job2)

            // Then: API는 단 1번만 호출
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
            println("✅ API 호출 횟수: 1회 (Race Condition 방어 성공)")

            // 두 결과 모두 성공하고 동일한 데이터
            assertTrue(results.all { it.isSuccess })
            assertEquals(results[0].getOrNull(), results[1].getOrNull())
        }

    @Test
    fun `10개의 동시 호출도 API는 1번만 호출`() =
        runTest {
            // Given
            var apiCallCount = 0
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                apiCallCount++
                delay(50)
                Result.success(NetworkResult.Success(mockUserInfoResponse))
            }

            // When: 10개의 코루틴이 동시에 호출
            val results =
                List(10) {
                    async { repository.getUserInfo() }
                }.awaitAll()

            // Then: API는 정확히 1번만 호출
            assertEquals(1, apiCallCount, "실제 API 호출 횟수: $apiCallCount")
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
            println("✅ 10개 동시 요청 → API 호출 1회")

            // 모든 결과가 성공하고 동일
            assertTrue(results.all { it.isSuccess })
            assertTrue(results.all { it.getOrNull() == results[0].getOrNull() })
        }

    @Test
    fun `100개의 동시 호출도 API는 1번만 호출`() =
        runTest {
            // Given
            var apiCallCount = 0
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                apiCallCount++
                delay(100)
                Result.success(NetworkResult.Success(mockUserInfoResponse))
            }

            // When: 100개의 코루틴이 동시에 호출
            val results =
                List(100) {
                    async { repository.getUserInfo() }
                }.awaitAll()

            // Then: API는 정확히 1번만 호출
            assertEquals(1, apiCallCount, "실제 API 호출 횟수: $apiCallCount")
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
            println("✅ 100개 동시 요청 → API 호출 1회")

            // 모든 결과가 성공이고 동일
            assertTrue(results.all { it.isSuccess })
            assertTrue(results.all { it.getOrNull() == results[0].getOrNull() })
        }

    @Test
    fun `로컬 저장도 1번만 실행됨`() =
        runTest {
            // Given
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                delay(100)
                Result.success(NetworkResult.Success(mockUserInfoResponse))
            }

            // When: 5개의 동시 호출
            val jobs =
                List(5) {
                    async { repository.getUserInfo() }
                }
            awaitAll(*jobs.toTypedArray())

            // Then: 로컬 저장도 1번만
            coVerify(exactly = 1) { userLocalDataSource.saveUserInfo(mockUserInfo) }
            println("✅ 로컬 저장도 1번만 실행")
        }

    @Test
    fun `캐시가 있을 때는 API 호출 없이 즉시 반환`() =
        runTest {
            // Given: 캐시 생성
            coEvery { userRemoteDataSource.getUserInfo() } returns
                Result.success(NetworkResult.Success(mockUserInfoResponse))

            repository.getUserInfo() // 첫 호출로 캐시 생성
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }

            // When: 캐시가 있는 상태에서 동시 호출
            val job1 = async { repository.getUserInfo() }
            val job2 = async { repository.getUserInfo() }

            awaitAll(job1, job2)

            // Then: 여전히 1번만 호출됨 (추가 API 호출 없음)
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
            println("✅ 캐시 사용으로 추가 API 호출 없음")
        }

    @Test
    fun `로컬 데이터가 있을 때도 중복 조회 방지`() =
        runTest {
            // Given: 로컬에 데이터 있음
            var localCallCount = 0
            coEvery { userLocalDataSource.getUserInfo() } coAnswers {
                localCallCount++
                delay(50)
                Result.success(mockUserInfo)
            }

            // When: 동시 호출
            val job1 = async { repository.getUserInfo() }
            val job2 = async { repository.getUserInfo() }

            val results = awaitAll(job1, job2)

            // Then: 로컬도 1번만 호출됨 ✅
            assertEquals(1, localCallCount, "로컬 호출 횟수: $localCallCount")
            println("✅ 로컬 데이터 조회도 1번만 실행")

            // 결과는 동일
            assertTrue(results.all { it.isSuccess })
            assertEquals(results[0].getOrNull(), results[1].getOrNull())
        }

    @Test
    fun `API 실패 시 매번 재시도 - 캐시하지 않음`() =
        runTest {
            // Given: API가 실패하는 상황
            var apiCallCount = 0
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                apiCallCount++
                delay(50)
                Result.success(NetworkResult.Failure.Unknown)
            }

            // When: 동시 호출
            val results =
                List(5) {
                    async { repository.getUserInfo() }
                }.awaitAll()

            // Then: 실패는 캐시되지 않으므로 5번 호출됨 (정상 동작)
            assertEquals(5, apiCallCount, "실패는 캐시되지 않아 매번 재시도: $apiCallCount")
            println("✅ API 실패는 캐시하지 않음 - 매번 재시도")

            // 모든 결과가 실패
            assertTrue(results.all { it.isFailure })
        }

    @Test
    fun `Mutex는 코루틴을 차단하지 않고 순차 실행`() =
        runTest {
            // Given
            val callOrder = mutableListOf<Int>()
            coEvery { userRemoteDataSource.getUserInfo() } coAnswers {
                delay(100)
                Result.success(NetworkResult.Success(mockUserInfoResponse))
            }

            // When: 순서를 추적하며 동시 호출
            val jobs =
                List(3) { index ->
                    async {
                        callOrder.add(index)
                        repository.getUserInfo()
                    }
                }

            awaitAll(*jobs.toTypedArray())

            // Then: 3개 모두 실행되었고 API는 1번만
            assertEquals(3, callOrder.size)
            coVerify(exactly = 1) { userRemoteDataSource.getUserInfo() }
            println("✅ 호출 순서: $callOrder, API 호출: 1회")
        }
}
