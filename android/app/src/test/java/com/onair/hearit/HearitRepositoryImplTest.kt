package com.onair.hearit

import com.onair.hearit.HearitFixtures.createFakeHearit
import com.onair.hearit.HearitFixtures.createFakeRandomHearit
import com.onair.hearit.HearitFixtures.createFakeRecommendHearit
import com.onair.hearit.HearitFixtures.createGroupedCategory
import com.onair.hearit.HearitFixtures.createSearchHearit
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.repository.HearitRepositoryImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HearitRepositoryImplTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var mockHearitRemoteDataSource: HearitRemoteDataSource

    private lateinit var hearitRepository: HearitRepositoryImpl

    @Before
    fun setUp() {
        hearitRepository = HearitRepositoryImpl(mockHearitRemoteDataSource)
    }

    @Test
    fun `getHearit 성공 시 SingleHearit 도메인 모델 반환`() =
        runTest {
            // Given
            val hearitId = 1L
            val mockDto = createFakeHearit(hearitId)
            val expectedDomainModel = mockDto.toDomain() // SingleHearit 반환

            coEvery { mockHearitRemoteDataSource.getHearit(hearitId) } returns
                Result.success(NetworkResult.Success(mockDto))

            // When
            val result = hearitRepository.getHearit(hearitId)

            // Then
            assertThat(result.isSuccess, `is`(true))
            assertThat(result.getOrNull(), `is`(expectedDomainModel))
        }

    @Test
    fun `getHearit 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val hearitId = 1L
            val expectedException = RuntimeException("네트워크 오류")
            coEvery { mockHearitRemoteDataSource.getHearit(hearitId) } returns
                Result.failure(
                    expectedException,
                )

            // When
            val result = hearitRepository.getHearit(hearitId)

            // Then
            assertThat(result.isFailure, `is`(true))
            assertThat(result.exceptionOrNull(), `is`(expectedException))
        }

    @Test
    fun `getRecommendHearits 성공 시 RecommendHearit 도메인 모델 반환`() =
        runTest {
            // Given
            val mockDtoList = listOf(createFakeRecommendHearit())
            val expectedDomainList = mockDtoList.map { it.toDomain() }

            coEvery { mockHearitRemoteDataSource.getRecommendHearits() } returns
                Result.success(NetworkResult.Success(mockDtoList))

            // When
            val result = hearitRepository.getRecommendHearits()

            // Then
            assertThat(result.isSuccess, `is`(true))
            assertThat(result.getOrNull(), `is`(expectedDomainList))
        }

    @Test
    fun `getRecommendHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val expectedException = RuntimeException("네트워크 오류")
            coEvery { mockHearitRemoteDataSource.getRecommendHearits() } returns
                Result.failure(
                    expectedException,
                )

            // When
            val result = hearitRepository.getRecommendHearits()

            // Then
            assertThat(result.isFailure, `is`(true))
            assertThat(result.exceptionOrNull(), `is`(expectedException))
        }

    @Test
    fun `getRandomHearits 성공 시 PageResult 도메인 모델 반환`() =
        runTest {
            // Given
            val mockPageResultDto = createFakeRandomHearit()
            val expectedDomainResult = mockPageResultDto.toDomain()

            coEvery { mockHearitRemoteDataSource.getRandomHearits(1, 10) } returns
                Result.success(NetworkResult.Success(mockPageResultDto))

            // When
            val result = hearitRepository.getRandomHearits(1, 10)

            // Then
            assertThat(result.isSuccess, `is`(true))
            assertThat(result.getOrNull(), `is`(expectedDomainResult))
        }

    @Test
    fun `getRandomHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val expectedException = RuntimeException("랜덤 데이터 오류")
            coEvery { mockHearitRemoteDataSource.getRandomHearits(1, 10) } returns
                Result.failure(
                    expectedException,
                )

            // When
            val result = hearitRepository.getRandomHearits(1, 10)

            // Then
            assertThat(result.isFailure, `is`(true))
            assertThat(result.exceptionOrNull(), `is`(expectedException))
        }

    @Test
    fun `getSearchHearits 성공 시 PageResult 도메인 모델 반환`() =
        runTest {
            // Given
            val mockPageResultDto = createSearchHearit()
            val expectedDomainResult = mockPageResultDto.toDomain()

            coEvery {
                mockHearitRemoteDataSource.getSearchHearits(
                    "test",
                    1,
                    10,
                )
            } returns Result.success(NetworkResult.Success(mockPageResultDto))

            // When
            val result = hearitRepository.getSearchHearits("test", 1, 10)

            // Then
            assertThat(result.isSuccess, `is`(true))
            assertThat(result.getOrNull(), `is`(expectedDomainResult))
        }

    @Test
    fun `getSearchHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val expectedException = RuntimeException("검색 오류")
            coEvery {
                mockHearitRemoteDataSource.getSearchHearits(
                    "test",
                    1,
                    10,
                )
            } returns Result.failure(expectedException)

            // When
            val result = hearitRepository.getSearchHearits("test", 1, 10)

            // Then
            assertThat(result.isFailure, `is`(true))
            assertThat(result.exceptionOrNull(), `is`(expectedException))
        }

    @Test
    fun `getCategoryHearits 성공 시 GroupedCategory 도메인 모델 반환`() =
        runTest {
            // Given
            val mockDtoList = listOf(createGroupedCategory())
            val expectedDomainList = mockDtoList.map { it.toDomain() }

            coEvery { mockHearitRemoteDataSource.getCategoryHearits() } returns
                Result.success(NetworkResult.Success(mockDtoList))

            // When
            val result = hearitRepository.getCategoryHearits()

            // Then
            assertThat(result.isSuccess, `is`(true))
            assertThat(result.getOrNull(), `is`(expectedDomainList))
        }

    @Test
    fun `getCategoryHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val expectedException = RuntimeException("카테고리 오류")
            coEvery { mockHearitRemoteDataSource.getCategoryHearits() } returns
                Result.failure(
                    expectedException,
                )

            // When
            val result = hearitRepository.getCategoryHearits()

            // Then
            assertThat(result.isFailure, `is`(true))
            assertThat(result.exceptionOrNull(), `is`(expectedException))
        }
}
