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
import androidx.media3.common.MediaItem
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
    DrawerClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.customDrawer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        player =
            ExoPlayer.Builder(this).build().apply {
                val uri = "android.resource://$packageName/${R.raw.test_audio2}".toUri()
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        binding.layoutBottomPlayerController.player = player

        binding.layoutBottomNavigation.itemIconTintList = null

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container_view, HomeFragment())
                .commit()
        }

        binding.layoutDrawer.tvDrawerAccountInfo.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container_view, SettingFragment())
                .addToBackStack(null)
                .commit()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.layoutDrawer.tvDrawerPrivacyPolicy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri())
            startActivity(intent)
        }

        binding.layoutDrawer.tvDrawerTermsOfUse.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, TERMS_OF_USE_URL.toUri())
            startActivity(intent)
        }

        binding.layoutBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, ExploreFragment())
                        .commit()
                    true
                }

                R.id.nav_home -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_search -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, SearchFragment())
                        .commit()
                    true
                }

                R.id.nav_library -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container_view, LibraryFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }

    override fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.END)
    }

    companion object {
        private const val PRIVACY_POLICY_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3809b9f92ec3e812ea24b?source=copy_link"
        private const val TERMS_OF_USE_URL =
            "https://glistening-eclipse-58b.notion.site/231d39b9c3c3800eb03cc7e1fc00f6f1?source=copy_link"
    }
}
