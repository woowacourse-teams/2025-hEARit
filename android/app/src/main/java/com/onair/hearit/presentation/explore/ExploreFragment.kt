package com.onair.hearit.presentation.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.FragmentExploreBinding
import com.onair.hearit.presentation.detail.PlayerDetailActivity

class ExploreFragment :
    Fragment(),
    ShortsClickListener {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExploreViewModel by viewModels { ExploreViewModelFactory() }

    private val player by lazy { ExoPlayer.Builder(requireContext()).build() }
    private val adapter by lazy { ShortsAdapter(player, this) }
    private val snapHelper = PagerSnapHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setupWindowInsets()
        setupRecyclerView()
        observeViewModel()

        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        scrollToNextItem()
                    }
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        if (!player.isPlaying && player.playbackState == Player.STATE_READY) {
            player.play()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun scrollToNextItem() {
        val layoutManager = binding.rvExplore.layoutManager ?: return
        val currentSnapView = snapHelper.findSnapView(layoutManager) ?: return
        val currentPosition = layoutManager.getPosition(currentSnapView)

        val nextPosition = currentPosition + 1
        if (nextPosition < adapter.itemCount) {
            binding.rvExplore.smoothScrollToPosition(nextPosition)
        }
    }

    private fun setupRecyclerView() {
        binding.rvExplore.adapter = adapter
        snapHelper.attachToRecyclerView(binding.rvExplore)

        binding.rvExplore.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int,
                ) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager ?: return
                        val snapView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(snapView)
                        val item = adapter.currentList.getOrNull(position) ?: return

                        player.setMediaItem(MediaItem.fromUri(item.audioUrl))
                        player.prepare()
                        player.play()

                        checkAndLoadNextPage(position)
                    }
                }
            },
        )
    }

    private fun observeViewModel() {
        viewModel.shortsHearits.observe(viewLifecycleOwner) { shortsHearits ->
            adapter.submitList(shortsHearits)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { resId ->
            showToast(getString(resId))
        }
    }

    private fun checkAndLoadNextPage(position: Int) {
        if (position >= adapter.itemCount - 2) {
            viewModel.fetchNextPage()
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onClickHearitInfo(hearitId: Long) {
        val intent = PlayerDetailActivity.newIntent(requireActivity(), hearitId)
        startActivity(intent)
    }

    override fun onClickBookmark(hearitId: Long) {
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
