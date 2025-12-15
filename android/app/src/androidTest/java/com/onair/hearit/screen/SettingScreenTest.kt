package com.onair.hearit.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.screen.SettingScreen
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: SettingViewModel
    private val userInfo = MutableStateFlow<UserInfo?>(null)

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun 로그인_안된_경우_로그인_하러가기가_표시된다() {
        // given
        every { mockViewModel.userInfo } returns MutableStateFlow<UserInfo?>(null).asStateFlow()

        composeTestRule.setContent {
            SettingScreen(
                viewModel = mockViewModel,
                onBackClick = {},
                onProfileClick = {},
                onLogin = {},
                onLogout = {},
                onWithdraw = {},
            )
        }

        composeTestRule.waitForIdle()

        // then
        composeTestRule
            .onNodeWithText("설정")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("로그인 하러가기")
            .assertIsDisplayed()
    }
}
