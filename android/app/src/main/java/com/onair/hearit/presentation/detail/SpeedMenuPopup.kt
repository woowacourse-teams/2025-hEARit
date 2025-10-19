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
import androidx.core.graphics.drawable.toDrawable
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
                setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_gray1_radius_8dp,
                    ),
                )
                setAdapter(SpeedAdapter(context, labels))
            }

        popup.show()

        popup.listView?.apply {
            divider = 0x40FFFFFF.toDrawable()
            dividerHeight = context.dp(1)
            setPadding(0, context.dp(4), 0, context.dp(4))
            clipToPadding = false

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

            speedText.text = getItem(position)
            check.visibility = if (position == checkedIndex) VISIBLE else INVISIBLE
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
