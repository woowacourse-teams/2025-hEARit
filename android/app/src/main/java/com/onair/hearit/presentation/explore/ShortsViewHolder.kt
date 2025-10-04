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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onair.hearit.databinding.ItemShortsBinding
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.presentation.flash
import com.onair.hearit.presentation.hideFlashImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren

@SuppressLint("ClickableViewAccessibility")
class ShortsViewHolder(
    private val binding: ItemShortsBinding,
    private val player: ExoPlayer,
    private val listener: ShortsClickListener,
) : RecyclerView.ViewHolder(binding.root) {
    private val scriptAdapter = ExploreScriptAdapter()
    private var rotateAnimator: ObjectAnimator? = null
    private var item: ExploreHearit? = null

    private val interactiveRect = android.graphics.Rect()
    private var isBoosting = false

    // 코루틴 스코프: UI 스레드에서 코루틴을 실행하고 관리
    // MainScope의 경우 최상위 스코프이기 떄문에 자식 스코프를 무조건 취소해줘야 하고, 간단한 애니메이션 같은 경우를 여기서 실행하는 경우가 많음
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // 각각의 job을 부여하고 cancel하면서 멈추지 않을 수도 있는 오류를 방지함
    private var playJob: Job? = null
    private var pauseJob: Job? = null
    private var boostJob: Job? = null

    init {
        binding.shortsClickListener = listener

        binding.viewGestureLayer.setOnTouchListener { _, event ->
            // ACTION_DOWN -> 처음 화면에 터치했을 때, 상세 정보 영역(isOnInteractive)이 아닌 경우에만 터치 이벤트 처리
            if (event.action == MotionEvent.ACTION_DOWN && isOnInteractive(event)) {
                // 상세 정보 영역 터치 시 이벤트 처리를 하지 않고, RecyclerView로 이벤트를 전달
                return@setOnTouchListener false
            }

            // ACTION_UP(손을 뗐을 때) 또는 ACTION_CANCEL(제스처 취소) 시 부스트 멈춤
            val shouldStopBoost =
                (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) && isBoosting
            if (shouldStopBoost) stopBoost()

            // detector가 터치 이벤트를 처리했는지(단일 탭, 롱 탭 등) 확인
            val handled = detector.onTouchEvent(event)

            // GestureDetector가 이벤트를 처리했거나(handled), 부스트를 멈췄을 때만 이벤트를 소비(true 반환)
            // 그 외의 경우(스크롤 등)는 false를 반환하여 RecyclerView가 이벤트를 처리하도록 함
            handled || shouldStopBoost
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

    @OptIn(UnstableApi::class)
    fun bind(item: ExploreHearit) {
        this.item = item
        binding.hearitItem = item

        binding.rvExploreItemScript.adapter = scriptAdapter
        scriptAdapter.submitList(item.script)

        binding.layoutExplorePlayer.player = player
        startLpRotation()
    }

    fun highlightScriptLine(positionMs: Long) {
        val script = item?.script
        if (script.isNullOrEmpty()) {
            scriptAdapter.highlightSubtitle(null)
            return
        }

        val index = script.indexOfLast { it.start <= positionMs }
        val id = script.getOrNull(index)?.id

        scriptAdapter.highlightSubtitle(id)
        (binding.rvExploreItemScript.layoutManager as? LinearLayoutManager)
            ?.scrollToPositionWithOffset(index, binding.rvExploreItemScript.height / 3)
    }

    fun onRecycled() {
        scope.coroutineContext.cancelChildren()

        if (isBoosting) stopBoost() else player.setPlaybackSpeed(DEFAULT_SPEED)
        stopLpRotation()

        binding.rvExploreItemScript.adapter = null

        playJob = null
        pauseJob = null
        boostJob = null
        item = null
    }

    // 상세로 넘어가는 부분에서는 일시정지/배속의 제스쳐를 허용하면 안되기 때문에 해당 위치를 제외할 수 있도록 하는 코드
    private fun isOnInteractive(event: MotionEvent): Boolean {
        binding.layoutExploreHearitInfo.getGlobalVisibleRect(interactiveRect)
        return interactiveRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }

    private fun togglePlayPause() {
        playJob?.cancel()
        pauseJob?.cancel()

        if (player.isPlaying) {
            binding.viewExplorePlay.hideFlashImmediately()
            pauseJob = binding.viewExplorePause.flash(scope)
            player.pause()
        } else {
            binding.viewExplorePause.hideFlashImmediately()
            playJob = binding.viewExplorePlay.flash(scope)
            player.play()
        }

        updateLpRotation(player.isPlaying)
    }

    private fun startBoost() {
        if (isBoosting || !player.isPlaying) {
            return
        }
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

    private fun stopLpRotation() {
        rotateAnimator?.cancel()
        rotateAnimator = null
    }

    fun updateLpRotation(isPlaying: Boolean) {
        if (isPlaying) resumeLpRotation() else pauseLpRotation()
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
