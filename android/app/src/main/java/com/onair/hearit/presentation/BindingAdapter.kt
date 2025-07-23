package com.onair.hearit.presentation

import android.util.TypedValue
import android.widget.TextView
import androidx.databinding.BindingAdapter
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

@BindingAdapter("highlightedTextSize")
fun setHighlightedTextSize(
    textView: TextView,
    isHighlighted: Boolean,
) {
    val sizeInSp = if (isHighlighted) 16f else 14f
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
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
