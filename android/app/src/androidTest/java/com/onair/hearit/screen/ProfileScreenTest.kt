package com.onair.hearit.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.onair.hearit.R
import com.onair.hearit.domain.model.UserInfo
import com.onair.hearit.presentation.setting.SettingViewModel
import com.onair.hearit.presentation.setting.screen.ProfileScreen
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

class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

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
    fun `프로필_화면_항목들이_정상적으로_표시된다`() {
        val myInfoTitle = composeTestRule.activity.getString(R.string.setting_profile)

        // given
        val testUser =
            UserInfo(
                id = 1L,
                nickname = "nickname",
                profileImage = null,
            )
        every { mockViewModel.userInfo } returns MutableStateFlow(testUser).asStateFlow()
        every { mockViewModel.appVersion } returns "1.4.1"

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = mockViewModel,
                onBackClick = {},
            )
        }

        // then
        composeTestRule
            .onNodeWithText(myInfoTitle)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("nickname")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("1.4.1", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("hearit2025@gmail.com", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `사용자_정보가_null인_경우에_hearit_닉네임이_표시된다`() {
        // given
        every { mockViewModel.userInfo } returns MutableStateFlow<UserInfo?>(null).asStateFlow()

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = mockViewModel,
                onBackClick = {},
            )
        }

        // then
        composeTestRule
            .onNodeWithText("hearit", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `뒤로가기_버튼을_클릭하면_onBackClick이_호출된다`() {
        // given
        val testUser = UserInfo(id = 1L, nickname = "nickname", profileImage = null)
        every { mockViewModel.userInfo } returns MutableStateFlow(testUser).asStateFlow()
        var backClicked = false

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = mockViewModel,
                onBackClick = { backClicked = true },
            )
        }

        // when
        composeTestRule
            .onNodeWithContentDescription("뒤로가기")
            .performClick()

        // then
        assertTrue(backClicked)
    }
}
