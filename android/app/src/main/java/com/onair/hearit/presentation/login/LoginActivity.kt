package com.onair.hearit.presentation.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivityLoginBinding
import com.onair.hearit.presentation.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val factory by lazy { LoginViewModelFactory(applicationContext) }
    private val viewModel by lazy { ViewModelProvider(this, factory)[LoginViewModel::class.java] }

    private var isKakaoTalkLogin = true

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

    private fun setupKakaoLogin() {
        binding.btnLoginKakao.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                isKakaoTalkLogin = true
                loginWithKakaoTalk()
            } else {
                isKakaoTalkLogin = false
                loginWithKakaoAccount()
            }
        }
    }

    private fun loginWithKakaoTalk() {
        UserApiClient.instance.loginWithKakaoTalk(this, callback = kakaoCallback())
    }

    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback())
    }

    private fun kakaoCallback(): (OAuthToken?, Throwable?) -> Unit {
        return callback@{ token, error ->
            when {
                token != null -> {
                    handleKakaoLoginSuccess(token)
                }

                error != null -> {
                    Log.e("kakao login", "카카오 로그인 실패", error)

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@callback
                    }

                    if (isKakaoTalkLogin) {
                        isKakaoTalkLogin = false
                        loginWithKakaoAccount()
                    } else {
                        showToast("카카오 로그인에 실패했습니다.")
                    }
                }
            }
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken) {
        UserApiClient.instance.me { user, _ ->
            if (user != null) {
                Firebase.analytics.setUserId(user.id.toString())
            }
        }
        viewModel.kakaoLogin(token.accessToken)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
