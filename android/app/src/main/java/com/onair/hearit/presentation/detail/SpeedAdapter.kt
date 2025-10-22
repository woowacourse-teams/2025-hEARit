package com.onair.hearit.presentation.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.onair.hearit.R

class SpeedAdapter(
    context: Context,
    private val items: List<String>,
    private var checkedIndex: Int,
) : ArrayAdapter<String>(context, 0, items) {
    fun updateCheckedIndex(newIndex: Int) {
        checkedIndex = newIndex
        notifyDataSetChanged()
    }

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
