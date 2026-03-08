package com.onair.hearit.presentation.detail.script

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.concurrent.futures.await
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.firebase.analytics.FirebaseAnalytics
import com.onair.hearit.R
import com.onair.hearit.analytics.AnalyticsEventNames
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.analytics.AnalyticsParamKeys
import com.onair.hearit.analytics.AnalyticsParamKeys.SCREEN_NAME_SCRIPT
import com.onair.hearit.databinding.FragmentScriptBinding
import com.onair.hearit.domain.model.ScriptLine
import com.onair.hearit.presentation.LoginRequiredDialogFragment
import com.onair.hearit.presentation.detail.PlayerDetailActivity.Companion.LOGIN_REQUIRED_DIALOG_TAG
import com.onair.hearit.presentation.detail.PlayerDetailViewModel
import com.onair.hearit.presentation.detail.script.component.Scripts
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.service.PlaybackService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScriptFragment : Fragment() {
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentScriptBinding? = null
    private val binding get() = _binding!!

    private var mediaController: MediaController? = null
    private val viewModel: PlayerDetailViewModel by activityViewModels()

    private val scriptViewModel: ScriptViewModel by viewModels()

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    private val updateInterval = SCRIPT_SYNC_INTERVAL_MS

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
        setUpScripts()
        setupBackPressedHandler()
        observeViewModel()
        connectToMediaController()
        setupBaseControllerBookmark()
    }

    override fun onResume() {
        super.onResume()
        analyticsLogger.logEvent(
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
                    val hearit = viewModel.hearit.value
                    val scripts: List<ScriptLine> = hearit?.script.orEmpty()

                    val position: Long = controller.currentPosition
                    scriptViewModel.tick(position, scripts)

                    delay(updateInterval)
                }
            }
        }
    }

    private fun setUpScripts() {
        binding.cvScript.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
        )
        binding.cvScript.setContent {
            val hearit = viewModel.hearit.observeAsState().value
            val scripts: List<ScriptLine> = hearit?.script.orEmpty()

            val highlightedId: Long? =
                scriptViewModel.highlightedId.collectAsStateWithLifecycle().value
            val isUserScrolling: Boolean =
                scriptViewModel.isUserScrolling.collectAsStateWithLifecycle().value
            val followHighlight: Boolean =
                scriptViewModel.followModeEnabled.collectAsStateWithLifecycle().value

            Scripts(
                scriptLines = scripts,
                highlightedId = highlightedId,
                isUserScrolling = isUserScrolling,
                followHighlight = followHighlight,
                onLineClick = { item ->
                    scriptViewModel.resumeFollowMode()
                    mediaController?.seekTo(item.start)
                },
                onUserScrollStateChange = { isScrolling ->
                    scriptViewModel.onUserScrollStateChange(isScrolling)
                },
                onStopFollow = {
                    scriptViewModel.stopFollowMode()
                },
            )
        }
    }

    private fun showLoginRequiredDialog() {
        LoginRequiredDialogFragment(
            messageRes = R.string.all_login_required_bookmark,
            onPositive = { navigateToLogin() },
        ).show(parentFragmentManager, LOGIN_REQUIRED_DIALOG_TAG)
    }

    private fun navigateToLogin() {
        analyticsLogger.logEvent(
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

        fun newInstance() = ScriptFragment()
    }
}
