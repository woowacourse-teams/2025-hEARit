package com.onair.hearit.presentation.explore

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun ExploreRoute(
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDetail: (id: Long, position: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val speed by viewModel.speed.collectAsStateWithLifecycle()

    // --- 생명주기 감지 로직 추가 ---
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        // 상세화면에서 돌아왔을 때 재생 재개 명령
                        viewModel.resumeIfScheduled()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        // 화면을 나갈 때 현재 위치 저장 및 정지
                        viewModel.onPause()
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
                        Toast
                            .makeText(
                                context,
                                context.getString(effect.messageResId),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }

                    ExploreSideEffect.NavigateToBack -> onBackClick()
                    is ExploreSideEffect.NavigateToDetail -> {
                        onNavigateToDetail(effect.hearitId, effect.lastPosition)
                    }
                }
            }
    }

    ExploreScreen(
        modifier = modifier,
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
    )
}
