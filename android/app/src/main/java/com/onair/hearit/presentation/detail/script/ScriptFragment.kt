package com.onair.hearit.presentation.detail.script

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
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

    private lateinit var mediaController: MediaController

    private val adapter by lazy { ScriptAdapter() }

    private val hearitId: Long by lazy {
        requireArguments().getLong(HEARIT_ID)
    }
    private val viewModel: PlayerDetailViewModel by viewModels {
        PlayerDetailViewModelFactory(hearitId, CrashlyticsProvider.get())
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
        }
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
