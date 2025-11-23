package com.onair.hearit.presentation.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.compose.rememberNavController

class SettingComposeFragment : Fragment() {
    private val viewModel: SettingViewModel by viewModels {
        SettingViewModelFactory()
    }

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
                    navController,
                    viewModel,
                    onExitSetting = {
                        parentFragmentManager.popBackStack()
                    },
                )
            }
        }
}
