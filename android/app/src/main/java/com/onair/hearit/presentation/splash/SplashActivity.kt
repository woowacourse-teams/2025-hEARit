package com.onair.hearit.presentation.splash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivitySplashBinding
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        setupWindowInsets()
        setupUpdateLauncher()
        observeViewModel()
        checkForUpdate()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.checkValidAccessTokenWithDelay()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun setupUpdateLauncher() {
        updateResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.checkValidAccessTokenWithDelay()
                } else {
                    finish()
                }
            }
    }

    private fun checkForUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                    )
                } else {
                    viewModel.checkValidAccessTokenWithDelay()
                }
            }.addOnFailureListener {
                viewModel.checkValidAccessTokenWithDelay()
            }
    }

    private fun observeViewModel() {
        viewModel.checkToken.observe(this) { isValid ->
            if (isValid) processIncomingIntent() else navigateToLogin()
        }
        viewModel.navigateToMain.observe(this) { hearitId ->
            navigateToMain(hearitId)
        }
        viewModel.toastMessage.observe(this) { messageResId ->
            Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun processIncomingIntent() {
        val uri = intent?.data
        viewModel.handleDeeplink(uri)
        intent?.data = null
        intent?.removeExtra(HEARIT_ID_KEY)
    }

    private fun navigateToMain(deeplinkHearitId: Long?) {
        val intent =
            Intent(this, MainActivity::class.java).apply {
                if (deeplinkHearitId != null) {
                    putExtra(OPEN_DETAIL_FROM_DEEPLINK, true)
                    putExtra(HEARIT_ID_KEY, deeplinkHearitId)
                }
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        lifecycleScope.launch {
            delay(1000)
            val intent =
                Intent(this@SplashActivity, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                }
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val OPEN_DETAIL_FROM_DEEPLINK = "OPEN_DETAIL_FROM_DEEPLINK"
    }
}
