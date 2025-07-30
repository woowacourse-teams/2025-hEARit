package com.onair.hearit.presentation.detail.script

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.FragmentScriptBinding
import com.onair.hearit.di.CrashlyticsProvider
import com.onair.hearit.presentation.detail.PlaybackService
import com.onair.hearit.presentation.detail.PlayerDetailViewModel
import com.onair.hearit.presentation.detail.PlayerDetailViewModelFactory
import kotlinx.coroutines.launch

class ScriptFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentScriptBinding? = null
    private val binding get() = _binding!!

    private var isUserScrolling = false
    private var lastUserScrollTime = 0L

    private lateinit var mediaController: MediaController

    private val adapter by lazy { ScriptAdapter() }

    private val hearitId: Long by lazy {
        requireArguments().getLong(HEARIT_ID)
    }
    private val viewModel: PlayerDetailViewModel by viewModels {
        PlayerDetailViewModelFactory(hearitId, CrashlyticsProvider.get())
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 300L

    private val itemHeightPx by lazy {
        val scale = resources.displayMetrics.density
        (16 * scale + 0.5f).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentScriptBinding.inflate(inflater, container, false)
        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        setupWindowInsets()
        setupRecyclerView()
        setupBackPressedHandler()
        observeViewModel()
        connectToMediaController()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
    }

    private fun setupRecyclerView() {
        binding.rvScript.adapter = adapter
        binding.rvScript.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int,
                ) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING ||
                        newState == RecyclerView.SCROLL_STATE_SETTLING
                    ) {
                        isUserScrolling = true
                        lastUserScrollTime = System.currentTimeMillis()
                    }
                }
            },
        )
    }

    private fun setupBackPressedHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    parentFragmentManager.popBackStack()
                }
            },
        )
    }

    private fun observeViewModel() {
        viewModel.hearit.observe(viewLifecycleOwner) { hearit ->
            binding.hearit = hearit
            adapter.submitList(hearit.script)
        }
    }

    @UnstableApi
    private fun connectToMediaController() {
        val sessionToken =
            SessionToken(
                requireContext(),
                ComponentName(requireContext(), PlaybackService::class.java),
            )

        lifecycleScope.launch {
            mediaController =
                MediaController.Builder(requireContext(), sessionToken).buildAsync().await()

            binding.playerView.player = mediaController
            binding.baseController.setPlayer(mediaController)

            startScriptSync(mediaController)
        }
    }

    private fun startScriptSync(controller: Player) {
        val updateRunnable =
            object : Runnable {
                override fun run() {
                    val now = System.currentTimeMillis()

                    val pos = controller.currentPosition
                    val currentItem =
                        adapter.currentList.firstOrNull { pos in it.start until it.end }
                    val currentIndex = adapter.currentList.indexOf(currentItem)

                    if (isUserScrolling) {
                        val isVisible = isItemVisible(currentIndex)

                        if (now - lastUserScrollTime > 3000L && isVisible) {
                            isUserScrolling = false
                        }
                    }

                    if (!isUserScrolling && currentItem != null) {
                        adapter.highlightScriptLine(currentItem.id)

                        val centerOffset = binding.rvScript.height / 2 - itemHeightPx / 2
                        (binding.rvScript.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(currentIndex, centerOffset)
                    }

                    handler.postDelayed(this, updateInterval)
                }
            }
        handler.post(updateRunnable)
    }

    private fun isItemVisible(position: Int): Boolean {
        val layoutManager = binding.rvScript.layoutManager as? LinearLayoutManager ?: return false
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        return position in first..last
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        private const val HEARIT_ID = "hearit_id"

        fun newInstance(hearitId: Long) =
            ScriptFragment().apply {
                arguments = Bundle().apply { putLong(HEARIT_ID, hearitId) }
            }
    }
}
