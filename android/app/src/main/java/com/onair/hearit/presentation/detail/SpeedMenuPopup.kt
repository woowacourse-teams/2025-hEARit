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

    fun show() {
        val popup =
            ListPopupWindow(context, null, androidx.appcompat.R.attr.listPopupWindowStyle).apply {
                anchorView = this@SpeedMenuPopup.anchorView
                isModal = true
                width = 200.dpToPx(context)
                val customBackground =
                    ContextCompat.getDrawable(context, R.drawable.bg_gray1_radius_8dp)
                customBackground?.alpha = (0.95f * 255).toInt()
                setBackgroundDrawable(customBackground)
                setAdapter(SpeedAdapter(context, labels))
            }

        val itemHeight = 48.dpToPx(context)
        val dividerHeight = 1.dpToPx(context)
        val count = labels.size
        val contentHeight =
            (itemHeight * count) + (dividerHeight * (count - 1).coerceAtLeast(0))

        popup.height = contentHeight
        popup.show()

        popup.listView?.apply {
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

            view.isActivated = (position == checkedIndex)
            return view
        }
    }

    private fun Float.toSpeedLabel(): String {
        val s = BigDecimal(this.toDouble()).stripTrailingZeros().toPlainString()
        return "x" + if (this % 1f == 0f) "$s.0" else s
    }
}
