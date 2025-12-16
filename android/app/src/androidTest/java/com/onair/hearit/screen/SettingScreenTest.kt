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

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `화면에_설정_메뉴_항목들이_표시된다`() {
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
            .onNodeWithText("내 정보")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("개인정보처리방침")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("이용 약관")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("오픈 라이선스")
            .assertIsDisplayed()
    }

    @Test
    fun `로그인_안된_경우_로그인_하러가기가_표시된다`() {
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
            .onNodeWithText("로그인 하러가기")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("로그아웃")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("회원탈퇴")
            .assertDoesNotExist()
    }

    @Test
    fun `로그인한_경우_로그아웃과_회원탈퇴가_표시된다`() {
        // given
        val testUser =
            UserInfo(
                id = 1L,
                nickname = "테스트유저",
                profileImage = null,
            )
        every { mockViewModel.userInfo } returns MutableStateFlow(testUser).asStateFlow()

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
            .onNodeWithText("로그아웃")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("회원탈퇴")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("로그인 하러가기")
            .assertDoesNotExist()
    }
}
