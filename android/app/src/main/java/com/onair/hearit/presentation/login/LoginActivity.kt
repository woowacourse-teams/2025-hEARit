package com.onair.hearit.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivityLoginBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.presentation.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val factory by lazy { LoginViewModelFactory(CrashlyticsProvider.get()) }
    private val viewModel by lazy { ViewModelProvider(this, factory)[LoginViewModel::class.java] }

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
                onError = { showToast("카카오 로그인에 실패했습니다.") },
            )

        binding.btnLoginKakao.setOnClickListener {
            kakaoLoginHelper.startLogin()
        }
    }

    private fun setupListeners() {
        binding.tvLoginHearit.setOnClickListener {
            navigateToMain()
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { isLoggedIn ->
            if (isLoggedIn == true) {
                navigateToMain()
            }
        }
        viewModel.toastMessage.observe(this) { resId ->
            showToast(getString(resId))
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken) {
        UserApiClient.instance.me { user, _ ->
            if (user != null) {
                AnalyticsProvider.get().setUserId(user.id.toString())
                CrashlyticsProvider.get().setUserId(user.id.toString())
            }
            viewModel.kakaoLogin(token.accessToken)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
