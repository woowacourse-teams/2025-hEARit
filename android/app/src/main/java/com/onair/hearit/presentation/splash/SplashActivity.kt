package com.onair.hearit.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivitySplashBinding
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.presentation.MainActivity
import com.onair.hearit.presentation.login.LoginActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels {
        SplashViewModelFactory(CrashlyticsProvider.get())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        setupWindowInsets()
        observeViewModel()
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.checkValidAccessToken()
        }, 1000)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun observeViewModel() {
        viewModel.checkToken.observe(this) { checkToken ->
            when (checkToken) {
                true -> navigateToMain()
                false -> navigateToLogin()
            }
        }

        viewModel.toastMessage.observe(this) { messageResId ->
            Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent =
                Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
            startActivity(intent)
            finish()
        }, 1000)
    }
}
