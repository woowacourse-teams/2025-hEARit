package com.onair.hearit.repository

import com.onair.hearit.HearitFixtures.createFakeHearit
import com.onair.hearit.HearitFixtures.createFakeRandomHearit
import com.onair.hearit.HearitFixtures.createFakeRecommendHearit
import com.onair.hearit.HearitFixtures.createSearchHearit
import com.onair.hearit.data.datasource.NetworkResult
import com.onair.hearit.data.datasource.remote.HearitRemoteDataSource
import com.onair.hearit.data.mapper.toDomain
import com.onair.hearit.data.mapper.toSearchedHearit
import com.onair.hearit.data.repository.HearitRepositoryImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
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
            val expectedDomainModel = mockDto.toDomain()
            coEvery {
                mockHearitRemoteDataSource.getHearit(hearitId)
            } returns NetworkResult.Success(mockDto)

            // When
            val result = hearitRepository.getHearit(hearitId)

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(expectedDomainModel)
        }

    @Test
    fun `getHearit 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val networkFailure = NetworkResult.Failure.Unknown
            coEvery {
                mockHearitRemoteDataSource.getHearit(1)
            } returns networkFailure

            // When
            val result = hearitRepository.getHearit(1)

            // Then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }

    @Test
    fun `getRecommendHearits 성공 시 RecommendHearit 도메인 모델 반환`() =
        runTest {
            // Given
            val mockDtoList = listOf(createFakeRecommendHearit())
            val expectedDomainList = mockDtoList.map { it.toDomain() }
            coEvery {
                mockHearitRemoteDataSource.getRecommendHearits()
            } returns NetworkResult.Success(mockDtoList)

            // When
            val result = hearitRepository.getRecommendHearits()

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(expectedDomainList)
        }

    @Test
    fun `getRecommendHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val networkFailure = NetworkResult.Failure.Unknown
            coEvery {
                mockHearitRemoteDataSource.getRecommendHearits()
            } returns networkFailure

            // When
            val result = hearitRepository.getRecommendHearits()

            // Then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }

    @Test
    fun `getExploreHearits 성공 시 PageResult 도메인 모델 반환`() =
        runTest {
            // Given
            val mockPageResultDto = createFakeRandomHearit()
            val expectedDomainResult = mockPageResultDto.toDomain()
            coEvery {
                mockHearitRemoteDataSource.getExploreHearits(1, 10)
            } returns NetworkResult.Success(mockPageResultDto)

            // When
            val result = hearitRepository.getExploreHearits(1, 10)

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(expectedDomainResult)
        }

    @Test
    fun `getExploreHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val networkFailure = NetworkResult.Failure.Unknown
            coEvery {
                mockHearitRemoteDataSource.getExploreHearits(1, 10)
            } returns networkFailure

            // When
            val result = hearitRepository.getExploreHearits(1, 10)

            // Then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }

    @Test
    fun `getKeywordHearits 성공 시 PageResult 도메인 모델 반환`() =
        runTest {
            // Given
            val mockPageResultDto = createSearchHearit()
            val expectedDomainResult = mockPageResultDto.toSearchedHearit()
            coEvery {
                mockHearitRemoteDataSource.getSearchHearits("test", 1, 10)
            } returns NetworkResult.Success(mockPageResultDto)

            // When
            val result = hearitRepository.getKeywordHearits("test", 1, 10)

            // Then
            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrNull()).isEqualTo(expectedDomainResult)
        }

    @Test
    fun `getKeywordHearits 실패 시 Result failure 반환`() =
        runTest {
            // Given
            val networkFailure = NetworkResult.Failure.Unknown
            coEvery {
                mockHearitRemoteDataSource.getSearchHearits("test", 1, 10)
            } returns networkFailure

            // When
            val result = hearitRepository.getKeywordHearits("test", 1, 10)

            // Then
            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        }
}
