package com.onair.hearit.presentation

import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.onair.hearit.R
import java.text.SimpleDateFormat
import java.util.Locale

@BindingAdapter("playTimeFormatted")
fun setFormattedPlayTime(
    textView: TextView,
    timeInSeconds: Int,
) {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    textView.text = String.format("%02d:%02d", minutes, seconds)
}

@BindingAdapter("exploreHighlightedStyle")
fun setHighlightedTextSize(
    textView: TextView,
    isHighlighted: Boolean,
) {
    val context = textView.context
    val textColorRes = if (isHighlighted) R.color.white else R.color.hearit_gray2
    val sizeInSp = if (isHighlighted) 16f else 14f

    textView.setTextColor(ContextCompat.getColor(context, textColorRes))
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
}

@BindingAdapter("detailHighlightedStyle")
fun setHighlightedStyle(
    textView: TextView,
    isHighlighted: Boolean,
) {
    val context = textView.context
    val textColor = ContextCompat.getColor(context, R.color.hearit_gray4)
    val transparent = ContextCompat.getColor(context, android.R.color.transparent)

    textView.setTextColor(textColor)
    textView.setBackgroundColor(transparent)

    textView.setTextSize(
        TypedValue.COMPLEX_UNIT_SP,
        if (isHighlighted) 18f else 16f,
    )

    val fontRes =
        if (isHighlighted) {
            R.font.pretendardbold
        } else {
            R.font.pretendardmedium
        }

    textView.typeface = ResourcesCompat.getFont(context, fontRes)
}

@BindingAdapter("formattedMillisToTime")
fun setFormattedMillisToTime(
    textView: TextView,
    millis: Long,
) {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    textView.text = String.format("%02d:%02d", minutes, seconds)
}

@BindingAdapter("formattedDate")
fun setFormattedDate(
    textView: TextView,
    dateString: String?,
) {
    if (dateString.isNullOrEmpty()) return

    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        val date = inputFormat.parse(dateString)
        textView.text = date?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) {
        textView.text = ""
    }
}
