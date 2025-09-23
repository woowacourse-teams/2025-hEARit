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
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.R
import com.onair.hearit.data.AuthEventManager
import com.onair.hearit.databinding.ActivityLoginBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.di.TokenInterceptorProvider
import com.onair.hearit.presentation.UserIdManager
import com.onair.hearit.presentation.main.MainActivity
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels { LoginViewModelFactory() }

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
            lifecycleScope.launch {
                viewModel.clearData()
                setUserId(null)
                navigateToMain()
            }
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

    private suspend fun setUserId(kakaoId: Long?) {
        val uuid = UserIdManager.getOrCreateUserId(this)
        val userId = kakaoId?.toString() ?: uuid
        TokenInterceptorProvider.setDeviceUuid(uuid)
        AnalyticsProvider.get().setUserId(userId)
        CrashlyticsProvider.get().setUserId(userId)
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}
