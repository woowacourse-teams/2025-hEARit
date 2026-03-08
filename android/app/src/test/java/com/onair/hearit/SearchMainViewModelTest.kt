package com.onair.hearit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.presentation.search.main.SearchMainViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchMainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SearchMainViewModel
    private lateinit var categoryRepository: CategoryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        categoryRepository = mockk()

        viewModel =
            SearchMainViewModel(
                categoryRepository = categoryRepository,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchCategories 성공 시 categories에 데이터가 설정된다`() =
        runTest {
            // Given
            val mockCategories =
                listOf(
                    Category(id = 1, name = "Kotlin", colorCode = "#FF5733"),
                    Category(id = 2, name = "Android", colorCode = "#33FF57"),
                    Category(id = 3, name = "Compose", colorCode = "#3357FF"),
                )
            val mockPageResult =
                PageResult(
                    items = mockCategories,
                    paging = Paging(1, 1, 1, 1, isFirst = true, isLast = true),
                )

            coEvery { categoryRepository.getCategories(any()) } returns
                Result.success(
                    mockPageResult,
                )

            // When
            viewModel.fetchCategories()

            // Then
            assertEquals(
                mockCategories.toImmutableList(),
                viewModel.searchMainUiState.value.categories,
            )
            assertEquals(false, viewModel.searchMainUiState.value.isLoading)
            coVerify(exactly = 1) { categoryRepository.getCategories(page = 0) }
        }

    @Test
    fun `fetchCategories 실패 시 snackbarMessage emit 여부 확인`() =
        runTest {
            // Given
            val exception = Exception("Network Error")
            coEvery {
                categoryRepository.getCategories(page = 0)
            } returns Result.failure(exception)

            val emittedMessages = mutableListOf<Int>()

            // backgroundScope를 사용하여 수집 (viewModelScope와 같은 디스패처 사용)
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.snackbarMessage.collect { message ->
                        emittedMessages.add(message)
                    }
                }

            // When
            viewModel.fetchCategories()
            testScheduler.advanceUntilIdle()

            // Then
            assertThat(emittedMessages).containsExactly(R.string.all_toast_categories_load_fail)
            coVerify(exactly = 1) { categoryRepository.getCategories(page = 0) }

            job.cancel()
        }
}
