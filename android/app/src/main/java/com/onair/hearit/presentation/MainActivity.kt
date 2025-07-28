package com.onair.hearit.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.onair.hearit.R
import com.onair.hearit.databinding.ActivityMainBinding
import com.onair.hearit.presentation.explore.ExploreFragment
import com.onair.hearit.presentation.home.HomeFragment
import com.onair.hearit.presentation.library.LibraryFragment
import com.onair.hearit.presentation.search.SearchFragment
import com.onair.hearit.presentation.setting.SettingFragment

class MainActivity :
    AppCompatActivity(),
    DrawerClickListener,
    PlayerControllerView {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer
    private var backPressedTime: Long = 0L
    private val backPressInterval = 1000L
    private var mediaController: MediaController? = null

    private val playerViewModel: PlayerViewModel by viewModels { PlayerViewModelFactory() }

    override fun onResume() {
        super.onResume()

        mediaController?.let { controller ->
            val isPlaying =
                controller.playbackState == Player.STATE_READY && controller.playWhenReady

            if (isPlaying) {
                showPlayerControlView()
            } else {
                hidePlayerControlView()
            }
        }
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
                        Toast
                            .makeText(
                                this@MainActivity,
                                "뒤로가기 버튼을 한 번 더 누르면 종료됩니다.",
                                Toast.LENGTH_SHORT,
                            ).show()
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
            when (item.itemId) {
                R.id.nav_home -> {
                    showFragment(HomeFragment())
                    true
                }

                R.id.nav_search -> {
                    showPlayerControlView()
                    showFragment(SearchFragment())
                    true
                }

                R.id.nav_explore -> {
                    hidePlayerControlView()
                    showFragment(ExploreFragment())
                    true
                }

                R.id.nav_library -> {
                    showPlayerControlView()
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

    override fun hidePlayerControlView() {
        binding.layoutBottomPlayerController.post {
            binding.layoutBottomPlayerController.apply {
                animate().translationY(height.toFloat()).setDuration(200).start()
            }
        }
    }

    override fun play(hearitId: Long) {
        playerViewModel.preparePlayback(hearitId)
        showPlayerControlView()
    }

    override fun pause() {
        mediaController?.pause()
    }

    private fun setupPlayer() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                binding.layoutBottomPlayerController.player = mediaController
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    private fun observeViewModel() {
        playerViewModel.playbackInfo.observe(this) { playbackInfo ->
            startPlayback(playbackInfo.audioUrl, playbackInfo.title)
            showPlayerControlView()
        }
    }

    private fun startPlayback(
        audioUrl: String,
        title: String,
    ) {
        val intent =
            Intent(this, PlaybackService::class.java).apply {
                putExtra("AUDIO_URL", audioUrl)
                putExtra("TITLE", title)
            }
        ContextCompat.startForegroundService(this, intent)
    }

    override fun showPlayerControlView() {
        binding.layoutBottomPlayerController
            .animate()
            .translationY(0f)
            .setDuration(200)
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val PRIVACY_POLICY_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3809b9f92ec3e812ea24b?source=copy_link"
        private const val TERMS_OF_USE_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3800eb03cc7e1fc00f6f1?source=copy_link"
    }
}
