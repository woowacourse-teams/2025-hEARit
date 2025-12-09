package com.onair.hearit.presentation.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.compose.rememberNavController
import com.onair.hearit.presentation.login.LoginActivity
import com.onair.hearit.presentation.main.MainViewModel
import com.onair.hearit.analytics.AnalyticsLogger
import com.onair.hearit.databinding.FragmentSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val mainViewModel: MainViewModel by activityViewModels()
    @Suppress("ktlint:standard:backing-property-naming")
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingViewModel by viewModels()

    @Inject
    lateinit var analyticsLogger: AnalyticsLogger

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val navController = rememberNavController()
                SettingNavHost(
                    navController = navController,
                    viewModel = viewModel,
                    onExitSetting = {
                        parentFragmentManager.popBackStack()
                    },
                    onLogin = {
                        startActivity(
                            Intent(
                                requireContext(),
                                LoginActivity::class.java,
                            ),
                        )
                    },
                    onLogout = mainViewModel::logout,
                    onWithdraw = mainViewModel::withdraw,
                )
            }
        }
}
