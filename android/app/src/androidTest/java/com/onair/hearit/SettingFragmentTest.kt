package com.onair.hearit

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onair.hearit.presentation.setting.SettingFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingFragmentTest {
    @Before
    fun setup() {
        launchFragmentInContainer<SettingFragment>(
            themeResId = R.style.Theme_HEARit,
        )
    }

    @Test
    fun `뒤로가기_버튼이_화면에_표시된다`() {
        onView(withId(R.id.ib_back))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `설정_및_계정_정보_텍스트가_화면에_표시된다`() {
        onView(withId(R.id.tv_setting_title))
            .check(matches(isDisplayed()))

        onView(withId(R.id.tv_setting_title))
            .check(matches(withText("설정 및 계정 정보")))
    }

    @Test
    fun `사용자_프로필_이미지가_화면에_표시된다`() {
        onView(withId(R.id.iv_setting_profile))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `사용자_프로필_닉네임이_화면에_표시된다`() {
        onView(withId(R.id.tv_setting_nickname))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `알림_텍스트가_화면에_표시된다`() {
        onView(withId(R.id.tv_setting_notification_text))
            .check(matches(isDisplayed()))

        onView(withId(R.id.tv_setting_notification_text))
            .check(matches(withText("히어릿 추천 알림")))
    }

    @Test
    fun `알림_스위치가_화면에_표시된다`() {
        onView(withId(R.id.switch_notification))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `알림_스위치를_누르면_상태가_변경된다`() {
        onView(withId(R.id.switch_notification))
            .check(matches(isNotChecked()))

        onView(withId(R.id.switch_notification))
            .perform(click())

        onView(withId(R.id.switch_notification))
            .check(matches(isChecked()))

        onView(withId(R.id.switch_notification))
            .perform(click())

        onView(withId(R.id.switch_notification))
            .check(matches(isNotChecked()))
    }
}
