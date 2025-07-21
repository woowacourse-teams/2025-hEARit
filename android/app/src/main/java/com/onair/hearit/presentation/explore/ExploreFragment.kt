package com.onair.hearit.presentation.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.data.dummy.HearitDummyData
import com.onair.hearit.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private lateinit var player: ExoPlayer
    private lateinit var adapter: ShortsAdapter
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

        setupWindowInsets()
        initPlayer()
        setupRecyclerView()
        submitDummyData()

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

    // 홈에서 돌아올 때 사용함 + exoplayer는 UI가 완전히 포커스를 가진 시점 이후에 재생되어야 하기 때문에 onResume에서 실행
    override fun onResume() {
        super.onResume()
        // player가 준비된 상태이고 + 현재 재생중이 아닌경우 => Explore 화면에서는 항상 실행되어야 하기 때문임
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

    private fun initPlayer() {
        player = ExoPlayer.Builder(requireContext()).build()
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
        adapter = ShortsAdapter(player)
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

                        player.setMediaItem(MediaItem.fromUri(item.audioUri))
                        player.prepare()
                        player.play()
                    }
                }
            },
        )
    }

    private fun submitDummyData() {
        val dummyItems = HearitDummyData.getShorts(requireContext().packageName)
        adapter.submitList(dummyItems)
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
