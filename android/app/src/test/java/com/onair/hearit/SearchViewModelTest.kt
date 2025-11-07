package com.onair.hearit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.onair.hearit.domain.model.Category
import com.onair.hearit.domain.model.PageResult
import com.onair.hearit.domain.model.Paging
import com.onair.hearit.domain.repository.CategoryRepository
import com.onair.hearit.domain.repository.HearitRepository
import com.onair.hearit.domain.repository.RecentKeywordRepository
import com.onair.hearit.presentation.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var viewModel: SearchViewModel
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var hearitRepository: HearitRepository
    private lateinit var recentKeywordRepository: RecentKeywordRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        categoryRepository = mockk()
        hearitRepository = mockk()
        recentKeywordRepository = mockk()

        viewModel =
            SearchViewModel(
                categoryRepository = categoryRepository,
                hearitRepository = hearitRepository,
                recentKeywordRepository = recentKeywordRepository,
                initialInput = null,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getCategories_성공_시_categories에_데이터가_설정된다() =
        runTest {
            // Given
            val mockCategories =
                listOf(
                    Category(id = 1, name = "Kotlin", colorCode = "#FF5733"),
                    Category(id = 2, name = "Android", colorCode = "#33FF57"),
                    Category(id = 3, name = "Compose", colorCode = "#3357FF"),
                )
            val mockPageCategories =
                PageResult(
                    items = mockCategories,
                    paging = Paging(1, 1, 1, 1, isFirst = true, isLast = true),
                )

            coEvery {
                categoryRepository.getCategories(any())
            } returns Result.success(mockPageCategories)

            // When
            viewModel.getCategories()
            advanceUntilIdle()

            // Then
            assertEquals(mockCategories, viewModel.categories.value)
            coVerify(exactly = 1) { categoryRepository.getCategories(page = 0) }
        }

    @Test
    fun getCategories_실패_시_toast_메시지가_발생한다() =
        runTest {
            // Given
            val exception = Exception("Network Error")
            coEvery {
                categoryRepository.getCategories(page = 0)
            } returns Result.failure(exception)

            // When
            viewModel.getCategories()
            advanceUntilIdle()

            // Then
            assertEquals(
                R.string.all_toast_categories_load_fail,
                viewModel.toastMessage.getOrAwaitValue(),
            )
        }
}
