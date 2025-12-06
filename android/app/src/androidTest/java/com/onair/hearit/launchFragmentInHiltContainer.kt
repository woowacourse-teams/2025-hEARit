package com.onair.hearit

import android.R
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider

inline fun <reified F : Fragment> launchFragmentInHiltContainer(
    crossinline fragmentFactory: () -> F = { F::class.java.newInstance() },
    crossinline onFragmentReady: (F) -> Unit = {},
) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val intent = Intent(context, HiltTestActivity::class.java)

    ActivityScenario.launch<FragmentActivity>(intent).onActivity { activity ->
        val fragment = fragmentFactory()
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.content, fragment, "")
            .commitNow()
        onFragmentReady(fragment)
    }
}
