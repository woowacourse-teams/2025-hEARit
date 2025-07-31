package com.onair.hearit.presentation

import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import com.onair.hearit.R
import com.onair.hearit.presentation.library.BookmarkUiState
import com.onair.hearit.presentation.search.SearchUiState
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
    val highlightTextColor = ContextCompat.getColor(context, R.color.hearit_gray4)
    val normalTextColor = ContextCompat.getColor(context, R.color.hearit_gray2)
    val transparent = ContextCompat.getColor(context, android.R.color.transparent)

    textView.setTextColor(if (isHighlighted) highlightTextColor else normalTextColor)
    textView.setBackgroundColor(transparent)

    textView.setTextSize(
        TypedValue.COMPLEX_UNIT_SP,
        if (isHighlighted) 16f else 14f,
    )

    val fontRes =
        if (isHighlighted) {
            R.font.pretendardbold
        } else {
            R.font.pretendardmedium
        }

    textView.typeface = ResourcesCompat.getFont(context, fontRes)
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
    } catch (_: Exception) {
        textView.text = ""
    }
}

@BindingAdapter("visibleIfNotNull")
fun setVisibleIfNotNull(
    view: View,
    value: Any?,
) {
    view.isVisible = value != null
}

@BindingAdapter("visibleIfNull")
fun setVisibleIfNull(
    view: View,
    value: Any?,
) {
    view.isVisible = value == null
}

@BindingAdapter("imageUrl")
fun setImageUrl(
    view: ImageView,
    url: String?,
) {
    view
        .load(url) {
            crossfade(true)
            error(R.drawable.img_default_profile)
            placeholder(R.drawable.img_default_profile)
        }
}

@BindingAdapter("visibleIfNotLogin")
fun setVisibleIfNotLogin(
    view: View,
    state: BookmarkUiState?,
) {
    view.isVisible = state is BookmarkUiState.NotLoggedIn
}

@BindingAdapter("visibleIfLogin")
fun setVisibleIfLogin(
    view: View,
    state: BookmarkUiState?,
) {
    view.isVisible = state is BookmarkUiState.LoggedIn
}

@BindingAdapter("setSelectedState")
fun setSelectedState(
    view: ImageView,
    condition: Boolean?,
) {
    view.isSelected = condition == true
}

@BindingAdapter("categoryBackgroundColor")
fun setCategoryBackgroundColor(
    view: View,
    colorCode: String,
) {
    val background = view.background?.mutate()
    if (background is GradientDrawable) {
        background.setColor(colorCode.toColorInt())
    }
}

@BindingAdapter("visibleIfNoHearits")
fun setVisibleIfNoHearits(
    view: View,
    state: SearchUiState?,
) {
    view.isVisible = state is SearchUiState.NoHearits
}

@BindingAdapter("visibleIfHearitsExist")
fun setVisibleIfHearitsExist(
    view: View,
    state: SearchUiState?,
) {
    view.isVisible = state is SearchUiState.HearitsExist
}
