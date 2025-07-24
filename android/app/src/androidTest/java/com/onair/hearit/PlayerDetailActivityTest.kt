package com.onair.hearit

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerDetailActivityTest {
    @Before
    fun setup() {
        ActivityScenario.launch(PlayerDetailActivity::class.java)
    }

    @Test
    fun `뒤로가기_버튼이_화면에_표시된다`() {
        onView(withId(R.id.ib_player_detail_back))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `히어릿_제목이_화면에_표시된다`() {
        onView(withId(R.id.tv_hearit_player_title))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `북마크_버튼이_화면에_표시된다`() {
        onView(withId(R.id.btn_hearit_player_bookmark))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `스크립트_리스트가_표시된다`() {
        onView(withId(R.id.rv_script))
            .check(matches(isDisplayed()))
    }
}
