package com.onair.hearit.presentation.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListPopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.onair.hearit.R
import java.math.BigDecimal
import kotlin.math.roundToInt

class SpeedMenuPopup(
    private val context: Context,
    private val anchorView: View,
    private val speedOptions: FloatArray,
    selectedSpeed: Float,
    private val onSelected: (speed: Float, index: Int) -> Unit,
    private val areEqual: (Float, Float) -> Boolean,
) {
    private var checkedIndex: Int =
        speedOptions
            .indexOfFirst { areEqual(it, selectedSpeed) }
            .takeIf { it >= 0 } ?: defaultSpeedIndex()

    private val labels: List<String> = speedOptions.map { it.toSpeedLabel() }

    fun show() {
        val popup =
            ListPopupWindow(context, null, androidx.appcompat.R.attr.listPopupWindowStyle).apply {
                anchorView = this@SpeedMenuPopup.anchorView
                isModal = true
                width = context.dp(200)
                val customBackground =
                    ContextCompat.getDrawable(context, R.drawable.bg_gray1_radius_8dp)
                customBackground?.alpha = (0.95f * 255).toInt()
                setBackgroundDrawable(customBackground)
                setAdapter(SpeedAdapter(context, labels))
            }

        val itemH = context.dp(48)
        val dividerH = context.dp(1)
        val verticalPadding = context.dp(8)
        val count = labels.size
        val contentHeight =
            (itemH * count) +
                (dividerH * (count - 1).coerceAtLeast(0)) +
                verticalPadding

        popup.height = contentHeight
        popup.show()

        popup.listView?.apply {
            setPadding(0, context.dp(4), 0, context.dp(4))
            clipToPadding = false

            isVerticalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER

            setOnItemClickListener { _, _, position, _ ->
                if (position in speedOptions.indices) {
                    checkedIndex = position
                    (adapter as? SpeedAdapter)?.notifyDataSetChanged()
                    onSelected(speedOptions[position], position)
                    popup.dismiss()
                }
            }
        }
    }

    private inner class SpeedAdapter(
        context: Context,
        items: List<String>,
    ) : ArrayAdapter<String>(context, 0, items) {
        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
        ): View {
            val view =
                convertView ?: LayoutInflater
                    .from(context)
                    .inflate(R.layout.item_speed_row, parent, false)
            val speedText = view.findViewById<TextView>(R.id.tv_speed_label)
            val check = view.findViewById<ImageView>(R.id.iv_check)
            val divider = view.findViewById<View>(R.id.view_divider)

            speedText.text = getItem(position)
            check.visibility = if (position == checkedIndex) VISIBLE else INVISIBLE
            divider.visibility = if (position == count - 1) INVISIBLE else VISIBLE
            return view
        }
    }

    private fun defaultSpeedIndex(): Int = speedOptions.indexOfFirst { areEqual(it, 1f) }.takeIf { it >= 0 } ?: 0

    private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun Float.toSpeedLabel(): String {
        val s = BigDecimal(this.toDouble()).stripTrailingZeros().toPlainString()
        return "x" + if (this % 1f == 0f) "$s.0" else s
    }
}
