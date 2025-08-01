package com.onair.hearit.presentation

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivityMainBinding
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.presentation.explore.ExploreFragment
import com.onair.hearit.presentation.home.HomeFragment
import com.onair.hearit.presentation.library.LibraryFragment
import com.onair.hearit.presentation.search.SearchFragment
import com.onair.hearit.presentation.setting.SettingFragment
import com.onair.hearit.service.PlaybackService

@OptIn(UnstableApi::class)
class MainActivity :
    AppCompatActivity(),
    DrawerClickListener,
    PlayerControllerView,
    PlaybackPositionSaver {
    private lateinit var binding: ActivityMainBinding
    private var backPressedTime: Long = 0L
    private val backPressInterval = 1000L
    private var mediaController: MediaController? = null
    private var currentSelectedItemId: Int = R.id.nav_home

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory(
            CrashlyticsProvider.get(),
        )
    }

    override fun onResume() {
        super.onResume()
        setPlayerControlViewVisibility()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupBackPressHandler()
        setupWindowInsets()
        setupPlayer()
        setupNavigation()
        setupDrawer()

        observeViewModel()

        showFragment(HomeFragment())
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

    fun selectTab(itemId: Int) {
        binding.layoutBottomNavigation.selectedItemId = itemId
    }

    private fun setupNavigation() {
        binding.layoutBottomNavigation.itemIconTintList = null
        binding.layoutBottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == currentSelectedItemId) {
                return@setOnItemSelectedListener true
            }
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

        binding.layoutDrawer.tvDrawerPrivacyPolicy.setOnClickListener {
            openUrl(PRIVACY_POLICY_URL)
        }

        binding.layoutDrawer.tvDrawerTermsOfUse.setOnClickListener {
            openUrl(TERMS_OF_USE_URL)
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun showFragment(
        fragment: androidx.fragment.app.Fragment,
        addToBackStack: Boolean = false,
    ) {
        supportFragmentManager
            .beginTransaction()
            .apply {
                replace(R.id.fragment_container_view, fragment)
                if (addToBackStack) addToBackStack(null)
            }.commit()
    }

    override fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayer() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                mediaController?.let {
                    binding.layoutBottomPlayerController.setPlayer(it)
                    setPlayerControlViewVisibility()
                }
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun observeViewModel() {
        playerViewModel.recentHearit.observe(this) {
            setPlayerControlViewVisibility()
            it?.let { playerViewModel.preparePlayback(it.id) }
        }

        playerViewModel.playbackInfo.observe(this) { playbackInfo ->
            startPlayback(
                playbackInfo.audioUrl,
                playbackInfo.title,
                playbackInfo.hearitId,
                playbackInfo.lastPosition,
            )
            showPlayerControlView()
            val title =
                playbackInfo.title.ifBlank {
                    getString(R.string.main_bottom_player_default_title)
                }
            binding.layoutBottomPlayerController.setTitle(title)
        }

        playerViewModel.toastMessage.observe(this) { resId ->
            showToast(getString(resId))
        }
    }

    private fun startPlayback(
        audioUrl: String,
        title: String,
        hearitId: Long,
        startPosition: Long = 0L,
    ) {
        val intent =
            PlaybackService.newIntent(
                context = this,
                audioUrl = audioUrl,
                title = title,
                hearitId = hearitId,
                startPosition = startPosition,
            )
        ContextCompat.startForegroundService(this, intent)
    }

    private fun setPlayerControlViewVisibility() {
        val controller = mediaController ?: return

        val isRecentAvailable = playerViewModel.recentHearit.value != null
        val isPlaying = controller.isPlaying || controller.playbackState == Player.STATE_READY

        if (currentSelectedItemId != R.id.nav_explore && (isRecentAvailable || isPlaying)) {
            showPlayerControlView()
        } else {
            hidePlayerControlView()
        }
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
            if (binding.layoutBottomPlayerController.translationY != binding.layoutBottomPlayerController.height.toFloat()) {
                binding.layoutBottomPlayerController
                    .animate()
                    .translationY(binding.layoutBottomPlayerController.height.toFloat())
                    .setDuration(200)
                    .start()
            }
        }
    }

    override fun pause() {
        mediaController?.pause()
        savePlaybackPosition()
    }

    private fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        savePlaybackPosition()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.release()
        mediaController = null
    }

    override fun savePlaybackPosition() {
        val controller = mediaController ?: return
        val position = controller.currentPosition
        val duration = controller.duration
        val hearitId = playerViewModel.recentHearit.value?.id ?: return

        playerViewModel.savePlaybackPosition(position, duration, hearitId)
    }

    companion object {
        private const val PRIVACY_POLICY_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3809b9f92ec3e812ea24b?source=copy_link"
        private const val TERMS_OF_USE_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3800eb03cc7e1fc00f6f1?source=copy_link"
    }
}
