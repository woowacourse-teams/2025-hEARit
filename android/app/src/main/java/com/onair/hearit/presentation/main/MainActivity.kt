package com.onair.hearit.presentation.main

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.common.util.concurrent.ListenableFuture
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.data.AuthEventManager
import com.onair.hearit.databinding.ActivityMainBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.PlaybackStarter
import com.onair.hearit.presentation.PlayerControllerView
import com.onair.hearit.presentation.detail.PlayerDetailActivity
import com.onair.hearit.presentation.explore.ExploreFragment
import com.onair.hearit.presentation.home.HomeFragment
import com.onair.hearit.presentation.library.LibraryFragment
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.navigate
import com.onair.hearit.presentation.search.SearchFragment
import com.onair.hearit.presentation.setting.SettingFragment
import com.onair.hearit.presentation.splash.SplashActivity
import com.onair.hearit.presentation.toDetailResult
import com.onair.hearit.service.PlaybackService
import com.onair.hearit.service.PlaybackSessionCallback
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class MainActivity :
    AppCompatActivity(),
    DrawerClickListener,
    PlayerControllerView,
    PlaybackStarter {
    private lateinit var binding: ActivityMainBinding
    private lateinit var detailResultLauncher: ActivityResultLauncher<Intent>
    private val backPressInterval = 1000L
    private var backPressedTime: Long = 0L
    private var loadingDialog: AlertDialog? = null
    private var mediaController: MediaController? = null
    private var currentSelectedItemId: Int = R.id.nav_home
    private var hasSentPreload = false
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null

    private val mainViewModel: MainViewModel by viewModels { MainViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.layoutDrawer.viewModel = mainViewModel
        binding.lifecycleOwner = this

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthEventManager.logoutEvent.collect {
                    if (!AuthEventManager.isValidSession()) {
                        handleForceLogout()
                    }
                }
            }
        }

        setupResultLauncher()
        setupBackPressHandler()
        setupWindowInsets()
        setupNavigation()
        setupDrawer()
        attachController()
        observeViewModel()
        showFragment(HomeFragment())
        setupBottomControllerClick()
        mainViewModel.handleDeepLinkIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!AuthEventManager.isValidSession()) {
            handleForceLogout()
        }
    }

    fun launchDetailActivity(intent: Intent) {
        detailResultLauncher.launch(intent)
    }

    fun selectTab(itemId: Int) {
        binding.layoutBottomNavigation.selectedItemId = itemId
    }

    private fun setupResultLauncher() {
        detailResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val detailResult =
                        result.data.toDetailResult() ?: return@registerForActivityResult
                    detailResult.navigate(this)
                }
                mainViewModel.notifyCategoryUpdated()
                mainViewModel.hearitUpdated.value = Unit
                setPlayerControlViewVisibility()
            }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime <= backPressInterval) {
                        finish()
                    } else {
                        backPressedTime = currentTime
                        showToast(getString(R.string.main_toast_finish_back_pressed))
                    }
                }
            },
        )
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.customDrawer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    private fun setupNavigation() {
        binding.layoutBottomNavigation.itemIconTintList = null
        binding.layoutBottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == currentSelectedItemId) return@setOnItemSelectedListener true
            currentSelectedItemId = item.itemId
            when (item.itemId) {
                R.id.nav_home -> {
                    setPlayerControlViewVisibility()
                    showFragment(HomeFragment())
                    true
                }

                R.id.nav_search -> {
                    setPlayerControlViewVisibility()
                    showFragment(SearchFragment())
                    true
                }

                R.id.nav_explore -> {
                    hidePlayerControlView()
                    showFragment(ExploreFragment())
                    true
                }

                R.id.nav_library -> {
                    setPlayerControlViewVisibility()
                    showFragment(LibraryFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun setupDrawer() {
        binding.layoutDrawer.tvDrawerAccountInfo.setOnClickListener {
            showFragment(SettingFragment(), addToBackStack = true)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
        binding.layoutDrawer.tvDrawerPrivacyPolicy.setOnClickListener { openUrl(PRIVACY_POLICY_URL) }
        binding.layoutDrawer.tvTermsOfUse.setOnClickListener { openUrl(TERMS_OF_USE_URL) }
        binding.layoutDrawer.tvOpenLicense.setOnClickListener { navigateToLicense() }
        binding.layoutDrawer.tvDrawerLogin.setOnClickListener {
            stopService(PlaybackService.stopIntent(this))
            navigateToLogin()
        }
        binding.layoutDrawer.tvDrawerLogout.setOnClickListener {
            stopService(PlaybackService.stopIntent(this))
            mainViewModel.performLogout()
        }
        binding.layoutDrawer.tvDrawerWithdrawal.setOnClickListener {
            stopService(PlaybackService.stopIntent(this))
            confirmAndWithdraw()
        }

        binding.layoutDrawer.tvDrawerFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, FEEDBACK_URL.toUri())
            startActivity(intent)
        }
    }

    private fun attachController() {
        if (mediaController != null || mediaControllerFuture != null) {
            maybePreloadRecent()
            return
        }

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            try {
                mediaController = mediaControllerFuture?.get()
                mediaController?.let { controller ->
                    binding.layoutBottomPlayerController.setPlayer(controller)
                    setPlayerControlViewVisibility()
                    maybePreloadRecent()
                }
            } catch (_: Exception) {
                mediaControllerFuture = null
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun observeViewModel() {
        mainViewModel.recentHearit.observe(this) {
            setPlayerControlViewVisibility()
            maybePreloadRecent()
        }

        mainViewModel.isLoggingOut.observe(this) { isLoading ->
            if (isLoading) {
                showLoadingDialog()
            } else {
                hideLoadingDialog()
                navigateToLogin()
            }
        }

        mainViewModel.withdrawState.observe(this) { state ->
            if (state) {
                navigateToLogin()
            } else {
                showToast(getString(R.string.withdraw_fail))
            }
        }

        mainViewModel.toastMessage.observe(this) { resId ->
            showToast(getString(resId))
        }

        mainViewModel.navigateToDetail.observe(this) { hearitId ->
            navigateToDetail(hearitId)
        }
    }

    private fun setupBottomControllerClick() {
        binding.layoutBottomPlayerController.setOnClickListener {
            val mediaId = mediaController?.currentMediaItem?.mediaId?.toLongOrNull()
            if (mediaId != null) {
                navigateToDetail(mediaId)
            } else {
                mainViewModel.recentHearit.value
                    ?.id
                    ?.let { navigateToDetail(it) }
            }
        }
    }

    private fun confirmAndWithdraw() {
        AlertDialog
            .Builder(this)
            .setTitle(R.string.dialog_withdraw_title)
            .setMessage(R.string.dialog_withdraw_message)
            .setPositiveButton(R.string.dialog_withdraw) { _, _ -> mainViewModel.withdraw() }
            .setNegativeButton(R.string.all_cancel, null)
            .show()
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun showFragment(
        fragment: Fragment,
        addToBackStack: Boolean = false,
    ) {
        supportFragmentManager
            .beginTransaction()
            .apply {
                replace(R.id.fragment_container_view, fragment)
                if (addToBackStack) addToBackStack(null)
            }.commit()
    }

    private fun showLoadingDialog() {
        if (loadingDialog?.isShowing == true) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        loadingDialog =
            AlertDialog
                .Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
        loadingDialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        loadingDialog?.show()
    }

    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun maybePreloadRecent() {
        val controller = mediaController ?: return
        if (hasSentPreload) return

        val hasRecent = mainViewModel.recentHearit.value != null
        val preparedOrHasItem =
            (controller.playbackState == Player.STATE_READY) || (controller.mediaItemCount > 0)

        if (hasRecent && !preparedOrHasItem) {
            hasSentPreload = true
            controller.sendCustomCommand(
                PlaybackSessionCallback.PRELOAD_RECENT_COMMAND,
                Bundle.EMPTY,
            )
        }
    }

    private fun setPlayerControlViewVisibility() {
        val controller = mediaController
        val isPreparedOrPlaying =
            controller?.let { it.isPlaying || it.playbackState == Player.STATE_READY } == true
        val hasRecent = mainViewModel.recentHearit.value != null

        if (currentSelectedItemId != R.id.nav_explore && (hasRecent || isPreparedOrPlaying)) {
            showPlayerControlView()
        } else {
            hidePlayerControlView()
        }
    }

    private fun navigateToLogin() {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "drawer_login"),
        )

        val intent =
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        startActivity(intent)
        finish()
    }

    private fun navigateToDetail(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(this, hearitId)
        launchDetailActivity(intent)
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLicense() {
        OssLicensesMenuActivity.setActivityTitle(HEARIT_OPEN_LICENSE_TITLE)
        val intent = Intent(this, OssLicensesMenuActivity::class.java)
        startActivity(intent)
    }

    private fun handleForceLogout() {
        lifecycleScope.launch {
            val intent =
                Intent(this@MainActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            startActivity(intent)
            Toast
                .makeText(
                    applicationContext,
                    "세션이 만료되어 다시 로그인해주세요",
                    Toast.LENGTH_LONG,
                ).show()
        }
    }

    override fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    override fun showPlayerControlView() {
        if (binding.layoutBottomPlayerController.translationY == 0f) return
        binding.layoutBottomPlayerController
            .animate()
            .translationY(0f)
            .setDuration(200)
            .start()
    }

    override fun hidePlayerControlView() {
        binding.layoutBottomPlayerController.post {
            val target = binding.layoutBottomPlayerController.height.toFloat() + PLAYER_HIDE_OFFSET
            if (binding.layoutBottomPlayerController.translationY != target) {
                binding.layoutBottomPlayerController
                    .animate()
                    .translationY(target)
                    .setDuration(200)
                    .start()
            }
        }
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.release()
        mediaController = null
        mediaControllerFuture?.cancel(true)
        mediaControllerFuture = null
    }

    override fun startPlayback() {
        val controller = mediaController
        if (controller != null) {
            controller.play()
            return
        }

        attachController()
        mediaControllerFuture?.addListener({
            mediaController?.play()
            setPlayerControlViewVisibility()
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        mainViewModel.handleDeepLinkIntent(intent)
        intent.removeExtra(SplashActivity.OPEN_DETAIL_FROM_DEEPLINK)
        intent.removeExtra(HEARIT_ID_KEY)
    }

    companion object {
        private const val HEARIT_OPEN_LICENSE_TITLE = "hEARit Open Source Licenses"
        private const val PLAYER_HIDE_OFFSET = 100f

        private const val PRIVACY_POLICY_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3809b9f92ec3e812ea24b?source=copy_link"
        private const val TERMS_OF_USE_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3800eb03cc7e1fc00f6f1?source=copy_link"
        private const val FEEDBACK_URL =
            "https://docs.google.com/forms/d/e/1FAIpQLSfHy20uq3LGUmxngS38QmDjGbJLHPXSlgUcp_yYfsQygXzC_Q/viewform"
    }
}
