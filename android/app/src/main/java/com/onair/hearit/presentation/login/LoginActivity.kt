package com.onair.hearit.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_LOGIN
import com.onair.hearit.analytics.CrashlyticsLogger
import com.onair.hearit.data.AuthEventManager
import com.onair.hearit.databinding.ActivityLoginBinding
import com.onair.hearit.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    @Inject
    lateinit var crashlyticsLogger: CrashlyticsLogger

    private lateinit var kakaoLoginHelper: KakaoLoginHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        setupWindowInsets()
        setupAnimation()
        setupKakaoLogin()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        analyticsLogger.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_LOGIN,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun setupAnimation() {
        binding.layoutLoginSymbol
            .animate()
            .translationY(-400f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun setupKakaoLogin() {
        kakaoLoginHelper =
            KakaoLoginHelper(
                activity = this,
                onSuccess = { token -> handleKakaoLoginSuccess(token) },
                onError = { throwable ->
                    showToast(getString(R.string.login_toast_kakao_login_fail))
                    Timber.w(throwable)
                },
            )
    }

    private fun setupListeners() {
        binding.btnLoginKakao.setOnClickListener {
            kakaoLoginHelper.startLogin()
        }

        binding.tvNoLoginHearit.setOnClickListener {
            AuthEventManager.onLoginSuccess()
            navigateToMain()
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { isLoggedIn ->
            if (isLoggedIn == true) navigateToMain()
        }

        viewModel.toastMessage.observe(this) { resId ->
            showToast(getString(resId))
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken) {
        UserApiClient.instance.me { user, _ ->
            lifecycleScope.launch {
                setUserId(user?.id)
                viewModel.kakaoLogin(token.accessToken)
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUserId(kakaoId: Long?) {
        val userId = kakaoId?.toString() ?: return
        analyticsLogger.setUserId(userId)
        crashlyticsLogger.setUserId(userId)
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
