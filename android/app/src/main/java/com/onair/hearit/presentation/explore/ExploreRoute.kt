package com.onair.hearit.presentation.explore

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.take

@Composable
fun ExploreRoute(
    onBackClick: () -> Unit,
    onNavigateToDetail: (id: Long, position: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val speed by viewModel.speed.collectAsStateWithLifecycle()

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        viewModel.resumeIfScheduled()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        val currentIndex = uiState.currentPageIndex
                        viewModel.scheduleResume(
                            resumeIndex = currentIndex,
                        )
                    }

                    else -> {}
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler {
        onBackClick()
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .filterIsInstance<ExploreSideEffect>()
            .collect { effect ->
                when (effect) {
                    is ExploreSideEffect.ShowToast -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(effect.messageResId),
                            duration = SnackbarDuration.Short,
                        )
                    }

                    ExploreSideEffect.NavigateToBack -> {
                        onBackClick()
                    }

                    is ExploreSideEffect.NavigateToDetail -> {
                        onNavigateToDetail(effect.hearitId, effect.lastPosition)
                    }
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { uiState }
            .filter { state ->
                !state.isLoading && state.shortsHearits.isNotEmpty()
            }.take(1)
            .collect {
                viewModel.loadAnimation()
            }
    }

    ExploreScreen(
        modifier = Modifier.fillMaxSize(),
        shortsHearit = uiState.shortsHearits.toImmutableList(),
        currentPosition = currentPosition,
        currentPageIndex = uiState.currentPageIndex,
        duration = duration,
        onPageChanged = { index -> viewModel.onPageChanged(index) },
        onNavigateToDetail = { id ->
            viewModel.onHearitSelected(id)
        },
        onPositionChanged = { position ->
            viewModel.onPositionChanged(position)
        },
        onItemPlay = {
            viewModel.onPlayerStateChanged()
        },
        onSpeedChanged = {
            viewModel.onSetPlayerSpeed()
        },
        isPlaying = isPlaying,
        speed = speed,
        showSwipeGuide = uiState.showGuideAnimation,
        onGuideFinished = { viewModel.dismissAnimation() },
    )
}
