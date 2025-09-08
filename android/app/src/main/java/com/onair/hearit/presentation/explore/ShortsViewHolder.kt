package com.onair.hearit.presentation.explore

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemShortsBinding
import com.onair.hearit.domain.model.ShortsHearit
import com.onair.hearit.presentation.flash
import com.onair.hearit.presentation.hideFlashImmediately
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren

@SuppressLint("ClickableViewAccessibility")
class ShortsViewHolder(
    private val binding: ItemShortsBinding,
    private val player: ExoPlayer,
    private val listener: ShortsClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val scriptAdapter = ExploreScriptAdapter()
    private var rotateAnimator: ObjectAnimator? = null
    private var item: ShortsHearit? = null

    private val interactiveRect = android.graphics.Rect()
    private var isBoosting = false

    // 코루틴 스코프: UI 스레드에서 코루틴을 실행하고 관리
    // MainScope의 경우 최상위 스코프이기 떄문에 자식 스코프를 무조건 취소해줘야 하고, 간단한 애니메이션 같은 경우를 여기서 실행하는 경우가 많음
    private val scope = MainScope()

    // 각각의 job을 부여하고 cancel하면서 멈추지 않을 수도 있는 오류를 방지함
    private var playJob: Job? = null
    private var pauseJob: Job? = null
    private var boostJob: Job? = null

    init {
        binding.shortsClickListener = listener

        // ACTION_DOWN -> 처음 화면에 터치했을 때
        binding.viewGestureLayer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && isOnInteractive(event)) return@setOnTouchListener false

            // 손을 뗐을 떄 일어나는 현상 + 제스처를 중간에 취소하는 경우 -> 부스트 멈춤
            if ((event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) && isBoosting) {
                stopBoost()
            }

            // 단일탭, 롱탭 등의 이벤트를 사용하기 위한 호출 + 계속 이벤트를 받을 수 있도록 true로 설정
            detector.onTouchEvent(event)
            true
        }
    }

    private val detector =
        GestureDetector(
            binding.root.context,
            object : GestureDetector.SimpleOnGestureListener() {
                // 여기서 true를 해주어야 계속해서 제스처를 인식할 수 있음
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (!isOnInteractive(e)) togglePlayPause()

                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    if (!isOnInteractive(e)) startBoost()
                }

                // 스크롤의 영역은 여기서 제어하지 않도록 함
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    dx: Float,
                    dy: Float,
                ) = false
            },
        )

    private val playerListener =
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) resumeLpRotation() else pauseLpRotation()
            }

            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED, Player.STATE_IDLE -> stopLpRotation()
                    Player.STATE_BUFFERING -> pauseLpRotation()
                }
            }
        }

    @OptIn(UnstableApi::class)
    fun bind(item: ShortsHearit) {
        this.item = item
        binding.hearitItem = item

        binding.rvExploreItemScript.adapter = scriptAdapter
        scriptAdapter.submitList(item.script)

        binding.layoutExplorePlayer.player = player
        player.addListener(playerListener)

//        binding.btnExploreItemBookmark.apply {
//            isSelected = item.isBookmarked
//            setOnClickListener {
//                listener.onClickBookmark(item.id) { id ->
//                    if (id != -1L) isSelected = !isSelected
//                }
//            }
//        }

        syncRotationWithPlayer()
    }

    fun highlightScriptLine(positionMs: Long) {
        val item = item ?: return
        val index = item.script.indexOfLast { script -> script.start <= positionMs }
        val id = item.script.getOrNull(index)?.id

        scriptAdapter.highlightSubtitle(id)
        (binding.rvExploreItemScript.layoutManager as? LinearLayoutManager)
            ?.scrollToPositionWithOffset(index, binding.rvExploreItemScript.height / 3)
    }

    fun onRecycled() {
        // 다양한 job들을 cancelChildren을 통해서 모두 취소함
        scope.coroutineContext.cancelChildren()
//        player.removeListener(playerListener)
        stopLpRotation()
        binding.rvExploreItemScript.adapter = null
        item = null
    }

    // 상세로 넘어가는 부분에서는 일시정지/배속의 제스쳐를 허용하면 안되기 때문에 해당 위치를 제외할 수 있도록 하는 코드
    private fun isOnInteractive(event: MotionEvent): Boolean {
        binding.layoutExploreHearitInfo.getHitRect(interactiveRect)
        return interactiveRect.contains(event.x.toInt(), event.y.toInt())
    }

    private fun togglePlayPause() {
        if (player.isPlaying) {
            playJob?.cancel()
            binding.viewExplorePlay.hideFlashImmediately()

            pauseJob?.cancel()
            pauseJob = binding.viewExplorePause.flash(scope)

            player.pause()
        } else {
            pauseJob?.cancel()
            binding.viewExplorePause.hideFlashImmediately()

            playJob?.cancel()
            playJob = binding.viewExplorePlay.flash(scope)

            player.play()
        }
    }

    private fun startBoost() {
        if (isBoosting) return
        boostJob?.cancel()
        boostJob = binding.viewExploreBoost.flash(scope)
        player.setPlaybackSpeed(BOOST_SPEED)
        isBoosting = true
    }

    private fun stopBoost() {
        if (!isBoosting) return
        boostJob?.cancel()
        binding.viewExploreBoost.hideFlashImmediately()
        player.setPlaybackSpeed(DEFAULT_SPEED)
        isBoosting = false
    }

    private fun startLpRotation() {
        rotateAnimator?.cancel()
        rotateAnimator =
            ObjectAnimator.ofFloat(binding.imgExploreLp, View.ROTATION, 0f, 360f).apply {
                duration = 3000L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
    }

    private fun syncRotationWithPlayer() {
        when {
            player.playbackState == Player.STATE_ENDED ||
                player.playbackState == Player.STATE_IDLE -> stopLpRotation()

            player.isPlaying -> resumeLpRotation()
            else -> pauseLpRotation()
        }
    }

    private fun stopLpRotation() {
        rotateAnimator?.cancel()
        rotateAnimator = null
    }

    private fun resumeLpRotation() {
        if (rotateAnimator == null) startLpRotation() else rotateAnimator?.resume()
    }

    private fun pauseLpRotation() {
        rotateAnimator?.pause()
    }

    companion object {
        private const val DEFAULT_SPEED = 1.0f
        private const val BOOST_SPEED = 2.0f

        fun create(
            parent: ViewGroup,
            player: ExoPlayer,
            listener: ShortsClickListener,
        ): ShortsViewHolder {
            val binding =
                ItemShortsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ShortsViewHolder(binding, player, listener)
        }
    }
}
