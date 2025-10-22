package com.onair.hearit.presentation.detail

import android.content.Context
import android.view.View
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.onair.hearit.R
import com.onair.hearit.presentation.dpToPx
import com.onair.hearit.presentation.indexOfSpeedOrDefault
import java.math.BigDecimal

class SpeedMenuPopup(
    private val context: Context,
    private val anchorView: View,
    private val speedOptions: FloatArray,
    selectedSpeed: Float,
    private val onSelected: (speed: Float, index: Int) -> Unit,
    private val areEqual: (Float, Float) -> Boolean,
) {
    private var checkedIndex: Int =
        speedOptions.indexOfSpeedOrDefault(
            target = selectedSpeed,
            areEqual = areEqual,
            defaultIndex = speedOptions.indexOfSpeedOrDefault(1f, areEqual),
        )

    private val labels: List<String> = speedOptions.map { it.toSpeedLabel() }

    private var popup: ListPopupWindow? = null
    private var adapter: SpeedAdapter? = null

    fun show() {
        val popupWindow = ensurePopup()
        val adapter = ensureSpeedAdapter()

        adapter.updateCheckedIndex(checkedIndex)

        popupWindow.anchorView = anchorView
        popupWindow.height = calculateContentHeight()

        if (!popupWindow.isShowing) {
            popupWindow.show()
            popupWindow.listView?.apply {
                isVerticalScrollBarEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER
            }
        } else {
            popupWindow.listView?.invalidateViews()
        }
    }

    private fun ensurePopup(): ListPopupWindow {
        popup?.let { return it }

        return ListPopupWindow(
            context,
            null,
            androidx.appcompat.R.attr.listPopupWindowStyle,
        ).apply {
            isModal = true
            width = 200.dpToPx(context)

            ContextCompat
                .getDrawable(context, R.drawable.bg_gray1_radius_8dp)
                ?.let { backgroundDrawable ->
                    backgroundDrawable.alpha = (0.95f * 255).toInt()
                    setBackgroundDrawable(backgroundDrawable)
                }

            setAdapter(ensureSpeedAdapter())

            setOnItemClickListener { _, _, position, _ ->
                if (position in speedOptions.indices) {
                    checkedIndex = position
                    adapter?.updateCheckedIndex(position)
                    onSelected(speedOptions[position], position)
                    dismiss()
                }
            }

            popup = this
        }
    }

    private fun ensureSpeedAdapter(): SpeedAdapter {
        adapter?.let { return it }
        return SpeedAdapter(context, labels, checkedIndex).also { adapter = it }
    }

    private fun calculateContentHeight(): Int {
        val itemHeight = 48.dpToPx(context)
        val dividerHeight = 1.dpToPx(context)
        val itemCount = labels.size
        return (itemHeight * itemCount) + (dividerHeight * (itemCount - 1).coerceAtLeast(0))
    }

    private fun Float.toSpeedLabel(): String {
        val s = BigDecimal(this.toDouble()).stripTrailingZeros().toPlainString()
        return "x" + if (this % 1f == 0f) "$s.0" else s
    }
}
