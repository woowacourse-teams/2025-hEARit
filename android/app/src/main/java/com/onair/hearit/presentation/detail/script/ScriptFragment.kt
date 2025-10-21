package com.onair.hearit.presentation.detail.script

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_SCRIPT
import com.onair.hearit.databinding.FragmentScriptBinding
import com.onair.hearit.di.AnalyticsProvider
import com.onair.hearit.presentation.IntentKeys.HEARIT_ID_KEY
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.detail.PlayerDetailActivity.Companion.LOGIN_REQUIRED_DIALOG_TAG
import com.onair.hearit.presentation.detail.PlayerDetailViewModel
import com.onair.hearit.presentation.detail.PlayerDetailViewModelFactory
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.service.PlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ScriptFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentScriptBinding? = null
    private val binding get() = _binding!!

    private var isUserScrolling = false
    private var lastUserScrollTime = 0L

    private var mediaController: MediaController? = null

    private val adapter: ScriptAdapter by lazy {
        ScriptAdapter({ item ->
            mediaController?.seekTo(item.start)
        })
    }

    private val hearitId: Long by lazy {
        requireArguments().getLong(HEARIT_ID_KEY)
    }
    private val viewModel: PlayerDetailViewModel by activityViewModels {
        PlayerDetailViewModelFactory(hearitId)
    }

    private val updateInterval = SCRIPT_SYNC_INTERVAL_MS

    private val itemHeightPx: Int by lazy { SCRIPT_ITEM_HEIGHT_DP.dpToPx(requireContext()) }

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
        setupBaseControllerBookmark()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsProvider.get().logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            mapOf(
                FirebaseAnalytics.Param.SCREEN_NAME to SCREEN_NAME_SCRIPT,
                FirebaseAnalytics.Param.SCREEN_CLASS to this::class.simpleName.orEmpty(),
            ),
        )
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
        val popAction = {
            parentFragmentManager
                .beginTransaction()
                .setCustomAnimations(0, R.anim.slide_down)
                .remove(this)
                .commit()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    popAction()
                }
            },
        )

        binding.ibScriptDown.setOnClickListener {
            popAction()
        }
    }

    @UnstableApi
    private fun observeViewModel() {
        viewModel.hearit.observe(viewLifecycleOwner) { hearit ->
            binding.hearit = hearit
            adapter.submitList(hearit?.script)
        }

        viewModel.bookmarkId.observe(viewLifecycleOwner) { bookmarkId ->
            binding.baseController.setBookmarkSelected(bookmarkId != null)
        }

        viewModel.showLoginDialog.observe(viewLifecycleOwner) {
            showLoginRequiredDialog()
        }
    }

    @UnstableApi
    private fun setupBaseControllerBookmark() {
        binding.baseController.setOnBookmarkClickListener {
            viewModel.toggleBookmark()
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

            mediaController?.let { controller ->
                binding.playerView.player = controller
                binding.baseController.setPlayer(controller)

                startScriptSync(controller)
            }
        }
    }

    @UnstableApi
    private fun startScriptSync(controller: Player) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    val position = controller.currentPosition
                    val currentItem =
                        adapter.currentList
                            .firstOrNull { position in it.start until it.end }
                    val currentIndex = adapter.currentList.indexOf(currentItem)

                    val now = System.currentTimeMillis()

                    if (isUserScrolling) {
                        val isVisible = isItemVisible(currentIndex)
                        if (now - lastUserScrollTime > USER_SCROLL_IDLE_THRESHOLD_MS && isVisible) {
                            isUserScrolling = false
                        }
                    }

                    currentItem?.let { adapter.highlightScriptLine(it.id) }

                    if (!isUserScrolling && currentItem != null) {
                        val scriptHeight = binding.rvScript.height
                        if (scriptHeight > 0) {
                            val centerOffset = scriptHeight / 2 - itemHeightPx / 2
                            (binding.rvScript.layoutManager as? LinearLayoutManager)
                                ?.scrollToPositionWithOffset(currentIndex, centerOffset)
                        }
                    }

                    delay(updateInterval)
                }
            }
        }
    }

    private fun isItemVisible(position: Int): Boolean {
        val layoutManager = binding.rvScript.layoutManager as? LinearLayoutManager ?: return false
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        return position in first..last
    }

    private fun showLoginRequiredDialog() {
        LoginRequiredDialogFragment {
            navigateToLogin()
        }.show(parentFragmentManager, LOGIN_REQUIRED_DIALOG_TAG)
    }

    private fun navigateToLogin() {
        AnalyticsProvider.get().logEvent(
            AnalyticsEventNames.LOGIN_EVENT,
            mapOf(AnalyticsParamKeys.SOURCE_NAME to "script_login"),
        )

        val intent = LoginActivity.newIntent(requireContext())
        startActivity(intent)

        val serviceIntent = Intent(requireContext(), PlaybackService::class.java)
        requireContext().stopService(serviceIntent)

        parentFragmentManager
            .beginTransaction()
            .remove(this)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playerView.player = null
        mediaController?.release()
        _binding = null
    }

    companion object {
        private const val SCRIPT_SYNC_INTERVAL_MS = 300L
        private const val USER_SCROLL_IDLE_THRESHOLD_MS = 3000L
        private const val SCRIPT_ITEM_HEIGHT_DP = 16

        fun newInstance(hearitId: Long) =
            ScriptFragment().apply {
                arguments = Bundle().apply { putLong(HEARIT_ID_KEY, hearitId) }
            }
    }
}
