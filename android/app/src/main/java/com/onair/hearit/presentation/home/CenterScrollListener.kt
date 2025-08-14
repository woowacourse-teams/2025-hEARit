package com.onair.hearit.presentation.home

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import kotlin.math.abs

class CenterScrollListener(
    private val snapHelper: SnapHelper,
    private val onSnapPositionChanged: (Int) -> Unit,
) : RecyclerView.OnScrollListener() {
    override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int,
    ) {
        val centerX = recyclerView.width / 2
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i) ?: continue
            applyCenterScalingEffect(child, centerX, recyclerView)
        }
    }

    override fun onScrollStateChanged(
        recyclerView: RecyclerView,
        newState: Int,
    ) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val snapView = snapHelper.findSnapView(layoutManager) ?: return
            val position = layoutManager.getPosition(snapView)
            onSnapPositionChanged(position)
        }
    }

    private fun applyCenterScalingEffect(
        child: View,
        centerX: Int,
        recyclerView: RecyclerView,
    ) {
        val childCenterX = (child.left + child.right) / 2
        val distanceFromCenter = (centerX - childCenterX).toFloat()
        val d = abs(distanceFromCenter) / recyclerView.width.coerceAtLeast(1)
        val scale = MIN_SCALE + (1 - d).coerceIn(0f, 1f) * MAX_SCALE_DELTA
        val translationX = distanceFromCenter * TRANSLATION_FACTOR

        child.pivotY = child.height / 2f
        child.translationY = 0f
        child.scaleX = scale
        child.scaleY = scale
        child.translationX = translationX

        // 중심에 가까울수록 불투명, 멀수록 더 투명
        child.z = (1 - d) * MAX_ELEVATION
        child.alpha = MIN_ALPHA + (1 - d) * MAX_ALPHA_DELTA
    }

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MAX_SCALE_DELTA = 0.15f
        private const val TRANSLATION_FACTOR = 0.2f
        private const val MAX_ELEVATION = 20f
        private const val MIN_ALPHA = 0.3f
        private const val MAX_ALPHA_DELTA = 0.8f
    }
}
