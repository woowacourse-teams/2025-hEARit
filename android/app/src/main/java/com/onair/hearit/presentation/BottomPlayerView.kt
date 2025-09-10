package com.onair.hearit.presentation

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.TimeBar
import com.onair.hearit.R
import com.onair.hearit.databinding.LayoutBottomPlayerControllerBinding
import com.onair.hearit.presentation.main.playlist.PlaylistBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class BottomPlayerView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : ConstraintLayout(context, attrs, defStyleAttr) {
        private var player: Player? = null
        private var listener: PlayerListener? = null

        private val binding =
            LayoutBottomPlayerControllerBinding.inflate(
                LayoutInflater.from(context),
                this,
                true,
            )

        private val window = Timeline.Window()

        private var uiScope: CoroutineScope? = null
        private var progressJob: Job? = null

        init {
            setupScrubListener()
            binding.exoPlay.setOnClickListener { togglePlayPause() }
            binding.exoPlaylist.setOnClickListener { showPlaylist() }
        }

        // 제대로 Player를 끊었다가 다시 연결해서 문제가 없도록 하기 위함.
        // 새로운 플레이어에 리스너를 달아주고,프로그레스바 연결
        fun setPlayer(newPlayer: Player): BottomPlayerView =
            apply {
                detachPlayer()
                player = newPlayer
                listener = PlayerListener().also { newPlayer.addListener(it) }
                refresh()

                // 뷰가 아직 화면에 그려지지 않았을 경우를 대비해 post로 ensureProgress를 호출
                post { ensureProgress() }
            }

        fun setTitle(title: String) {
            binding.tvBottomPlayerTitle.isSelected = true
            binding.tvBottomPlayerTitle.text = title
        }

        private fun setupScrubListener() {
            binding.exoProgress.addListener(
                object : TimeBar.OnScrubListener {
                    override fun onScrubStart(
                        timeBar: TimeBar,
                        position: Long,
                    ) = Unit

                    override fun onScrubMove(
                        timeBar: TimeBar,
                        position: Long,
                    ) = Unit

                    override fun onScrubStop(
                        timeBar: TimeBar,
                        position: Long,
                        canceled: Boolean,
                    ) {
                        player?.seekTo(position)
                    }
                },
            )
        }

        // 전체 UI를 새로고침하여 업데이트 하는 함수
        private fun refresh() {
            player?.let {
                updateTimeline(it)
                updatePlayPauseButton(it)
                updateTitle(it)
            }
        }

        private fun updateTitle(player: Player) {
            val title =
                listOf(
                    player.currentMediaItem
                        ?.mediaMetadata
                        ?.title
                        ?.toString(),
                    player.mediaMetadata.title?.toString(),
                    player.currentMediaItem?.mediaId,
                ).firstOrNull { !it.isNullOrBlank() } ?: ""

            setTitle(title)
        }

        // 플레이어의 타임라인(총 길이)을 업데이트
        private fun updateTimeline(player: Player) {
            val timeline = player.currentTimeline
            if (timeline.isEmpty) {
                binding.exoProgress.setDuration(0L)
                return
            }
            timeline.getWindow(player.currentMediaItemIndex, window)
            val duration = window.durationMs.coerceAtLeast(0L)
            binding.exoProgress.setDuration(duration)
        }

        // 일단 한 번 바로 업데이트하고, 만약 아직 루프가 시작되지 않았다면 지금 시작해서 계속 업데이트하도록 보장하는 함수
        private fun ensureProgress() {
            updateProgressOnce()
            // 프로그레스 잡이 활성화되어 있지 않으면 새로 시작
            if (progressJob?.isActive != true) startProgressLoop()
        }

        // 프로그레스 바를 한 번 업데이트합니다.
        private fun updateProgressOnce() {
            val currentPlayer = player ?: return
            val duration =
                if (currentPlayer.duration != C.TIME_UNSET) {
                    currentPlayer.duration
                } else {
                    window.durationMs.takeIf { it > 0 } ?: 0L
                }
            binding.exoProgress.setDuration(duration)
            binding.exoProgress.setPosition(currentPlayer.currentPosition)
            binding.exoProgress.setBufferedPosition(currentPlayer.bufferedPosition)
        }

        // 프로그레스 바 업데이트를 위한 코루틴 루프를 시작
        private fun startProgressLoop() {
            progressJob?.cancel()
            progressJob =
                uiScope?.launch {
                    while (isActive && isAttachedToWindow) {
                        updateProgressOnce()
                        val preferred = binding.exoProgress.preferredUpdateDelay
                        val delayMs = if (preferred in 1L..999L) preferred else 50L
                        delay(delayMs)
                    }
                }
        }

        // 프로그레스 바 업데이트 루프를 멈춤
        private fun stopProgressLoop() {
            progressJob?.cancel()
            progressJob = null
        }

        private fun updatePlayPauseButton(current: Player) {
            val isPlaying = current.playWhenReady && current.playbackState == Player.STATE_READY
            val icon = if (isPlaying) R.drawable.ic_bottom_pause else R.drawable.ic_bottom_play
            binding.exoPlay.setImageResource(icon)
        }

        private fun togglePlayPause() {
            val currentPlayer = player
            if (currentPlayer == null) {
                (context as? PlaybackStarter)?.startPlayback()
                return
            }

            if (currentPlayer.isPlaying) {
                currentPlayer.pause()
            } else {
                currentPlayer.play()
            }
        }

        private fun showPlaylist() {
            val activity = context as? AppCompatActivity ?: return
            val playlist = PlaylistBottomSheet.newInstance()
            playlist.show(activity.supportFragmentManager, "PlaylistBottomSheet")
        }

        // 뷰가 분리될 때 모든 리소스를 정리
        private fun detachPlayer() {
            progressJob?.cancel()
            listener?.let { player?.removeListener(it) }
            listener = null
            player = null
        }

        // 뷰가 화면에 붙을 때 호출되는 콜백.
        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            // 뷰가 다시 붙으면 프로그레스 루프를 다시 시작
            if (player != null && player?.isPlaying == true) {
                startProgressLoop()
            }
        }

        // 뷰가 화면에서 분리될 때 호출되는 콜백
        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            uiScope?.cancel()
            uiScope = null
            detachPlayer()
        }

        private inner class PlayerListener : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                // 메타데이터, 타임라인 등 중요한 이벤트 발생 시에  UI를 새로고침
                if (events.containsAny(
                        Player.EVENT_MEDIA_METADATA_CHANGED,
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_TIMELINE_CHANGED,
                        Player.EVENT_POSITION_DISCONTINUITY,
                    )
                ) {
                    refresh()
                    // 재생/일시정지 상태 변경 이벤트 발생 시 버튼과 프로그레스 루프를 업데이트
                } else if (events.containsAny(
                        Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED,
                    )
                ) {
                    updatePlayPauseButton(player)
                    if (player.isPlaying) {
                        startProgressLoop()
                    } else {
                        stopProgressLoop()
                    }
                }
            }
        }
    }
