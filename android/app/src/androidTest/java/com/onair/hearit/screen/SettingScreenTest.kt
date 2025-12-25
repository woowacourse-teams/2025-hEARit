package com.onair.hearit.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.screen.SettingScreen
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.After
import org.junit.Assert.assertTrue
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
                onAlarmClick = {},
                onLogin = {},
                onLogout = {},
                onWithdraw = {},
            )
        }

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

    @Test
    fun `뒤로가기_버튼을_클릭하면_onBackClick이_호출된다`() {
        // given
        every { mockViewModel.userInfo } returns MutableStateFlow<UserInfo?>(null).asStateFlow()
        var backClicked = false

        composeTestRule.setContent {
            SettingScreen(
                viewModel = mockViewModel,
                onBackClick = { backClicked = true },
                onProfileClick = {},
                onLogin = {},
                onLogout = {},
                onWithdraw = {},
            )
        }

        // when
        composeTestRule
            .onNodeWithContentDescription("뒤로가기")
            .performClick()

        // then
        assertTrue(backClicked)
    }

    @Test
    fun `로그인_하러가기를_클릭하면_onLogin이_호출된다`() {
        // given
        every { mockViewModel.userInfo } returns MutableStateFlow<UserInfo?>(null).asStateFlow()
        var loginClicked = false

        composeTestRule.setContent {
            SettingScreen(
                viewModel = mockViewModel,
                onBackClick = {},
                onProfileClick = {},
                onLogin = { loginClicked = true },
                onLogout = {},
                onWithdraw = {},
            )
        }

        // when
        composeTestRule
            .onNodeWithText("로그인 하러가기")
            .performClick()

        // then
        assertTrue(loginClicked)
    }

    @Test
    fun `로그아웃을_클릭하면_onLogout이_호출된다`() {
        // given
        val testUser = UserInfo(id = 1L, nickname = "테스트", profileImage = null)
        every { mockViewModel.userInfo } returns MutableStateFlow(testUser).asStateFlow()
        var logoutClicked = false

        composeTestRule.setContent {
            SettingScreen(
                viewModel = mockViewModel,
                onBackClick = {},
                onProfileClick = {},
                onLogin = {},
                onLogout = { logoutClicked = true },
                onWithdraw = {},
            )
        }

        // when
        composeTestRule
            .onNodeWithText("로그아웃")
            .performClick()

        // then
        assertTrue(logoutClicked)
    }

    @Test
    fun `회원탈퇴를_클릭하면_onWithdraw가_호출된다`() {
        // given
        val testUser = UserInfo(id = 1L, nickname = "테스트", profileImage = null)
        every { mockViewModel.userInfo } returns MutableStateFlow(testUser).asStateFlow()
        var withdrawClicked = false

        composeTestRule.setContent {
            SettingScreen(
                viewModel = mockViewModel,
                onBackClick = {},
                onProfileClick = {},
                onLogin = {},
                onLogout = {},
                onWithdraw = { withdrawClicked = true },
            )
        }

        // when
        composeTestRule
            .onNodeWithText("회원탈퇴")
            .performClick()

        // then
        assertTrue(withdrawClicked)
    }

    @Test
    fun `내_정보를_클릭하면_onProfileClick이_호출된다`() {
        // given
        val testUser = UserInfo(id = 1L, nickname = "테스트", profileImage = null)
        every { mockViewModel.userInfo } returns MutableStateFlow(testUser).asStateFlow()
        var profileClicked = false

        composeTestRule.setContent {
            SettingScreen(
                viewModel = mockViewModel,
                onBackClick = {},
                onProfileClick = { profileClicked = true },
                onLogin = {},
                onLogout = {},
                onWithdraw = {},
            )
        }

        // when - 프로필 영역 클릭 (이름 또는 전체 영역)
        composeTestRule
            .onNodeWithText("내 정보")
            .performClick()

        // then
        assertTrue(profileClicked)
    }
}
