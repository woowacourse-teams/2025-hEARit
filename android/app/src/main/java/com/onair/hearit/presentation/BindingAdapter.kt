package com.onair.hearit.presentation

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.transform.RoundedCornersTransformation
import com.facebook.shimmer.ShimmerFrameLayout
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.library.BookmarkUiState
import java.text.SimpleDateFormat
import java.util.Locale

@BindingAdapter("playTimeFormatted")
fun setFormattedPlayTime(
    textView: TextView,
    timeInSeconds: Int,
) {
    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    textView.text = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
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
    val normalTextColor = ContextCompat.getColor(context, R.color.hearit_dark_gray)
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

@BindingAdapter("imageUrl", "shimmerContainer", requireAll = false)
fun loadProfileImage(
    view: ImageView,
    url: String?,
    shimmerContainer: ShimmerFrameLayout?,
) {
    // url 없으면 기본 이미지
    if (url.isNullOrEmpty()) {
        shimmerContainer?.visibility = View.GONE
        view.setImageResource(R.drawable.img_default_profile)
        return
    }

    // 캐시 확인
    val hasCached =
        view.context.imageLoader.memoryCache
            ?.get(MemoryCache.Key(url)) != null

    // 캐시 있으면 shimmer 숨김
    if (hasCached || view.drawable != null) {
        shimmerContainer?.visibility = View.GONE
    }

    view.load(url) {
        crossfade(false)
        placeholder(view.drawable)
        error(R.drawable.img_default_profile)
        memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.ENABLED)
        listener(
            onStart = {
                if (view.drawable == null) {
                    shimmerContainer?.visibility = View.VISIBLE
                    view.visibility = View.INVISIBLE
                }
            },
            onSuccess = { _, _ ->
                shimmerContainer?.visibility = View.GONE
                view.visibility = View.VISIBLE
            },
            onError = { _, _ ->
                shimmerContainer?.visibility = View.GONE
                view.visibility = View.VISIBLE
            },
        )
        transformations(RoundedCornersTransformation(320f))
    }
}

@BindingAdapter("visibleIfCondition")
fun setVisibleIfCondition(
    view: View,
    condition: Boolean,
) {
    view.isVisible = condition
}

@BindingAdapter("visibleBookmarkIfLogin")
fun setBookmarkVisibleIfLogin(
    view: View,
    state: BookmarkUiState?,
) {
    view.isVisible = state is BookmarkUiState.LoggedIn
}

@BindingAdapter("visibleBookmarkIfNotLogin")
fun setBookmarkVisibleIfNotLogin(
    view: View,
    state: BookmarkUiState?,
) {
    view.isVisible = state is BookmarkUiState.NotLoggedIn
}

@BindingAdapter("visibleBookmarkIfNoBookmarks")
fun setBookmarkIfNoBookmarks(
    view: View,
    state: BookmarkUiState?,
) {
    view.isVisible = state is BookmarkUiState.NoBookmarks
}

@BindingAdapter("backgroundColor")
fun setBackgroundColor(
    view: View,
    colorCode: String,
) {
    when (val background = view.background?.mutate()) {
        is GradientDrawable -> background.setColor(colorCode.toColorInt())
        is ColorDrawable -> background.color = colorCode.toColorInt()
        else -> view.setBackgroundColor(colorCode.toColorInt())
    }
}

@BindingAdapter("roundedBackgroundColor")
fun setRoundedBackgroundColor(
    view: View,
    colorCode: String?,
) {
    val radiusPx = 8f * view.resources.displayMetrics.density
    val drawable =
        GradientDrawable().apply {
            cornerRadius = radiusPx
            val colorInt =
                if (colorCode.isNullOrBlank()) {
                    ContextCompat.getColor(view.context, R.color.hearit_gray1)
                } else {
                    colorCode.toColorInt()
                }
            setColor(colorInt)
        }
    view.background = drawable
}

@BindingAdapter("setKeywords")
fun setExploreKeywords(
    textView: TextView,
    keywords: List<Keyword>,
) {
    textView.text = keywords.joinToString(" ") { "#${it.name}" }
}

@BindingAdapter(value = ["lastPlayTime", "totalPlayTime"])
fun setProgressBarRatio(
    progressBar: ProgressBar,
    lastPlayTime: Long?,
    totalPlayTime: Long,
) {
    val lastPlayTimeSec = (lastPlayTime ?: 0L) / 1000f
    val ratio = lastPlayTimeSec / totalPlayTime.toFloat()
    val percent = (ratio.coerceIn(0f, 1f) * 100).toInt()
    progressBar.progress = percent
}

@BindingAdapter("linkUrl")
fun setLinkUrl(
    view: View,
    url: String?,
) {
    val uri = url?.toUri()
    val isAllowedScheme = uri?.scheme in setOf("http", "https")

    if (!isAllowedScheme) {
        view.setOnClickListener(null)
        view.isClickable = false
        return
    }

    view.isClickable = true

    view.setOnClickListener {
        runCatching {
            val intent =
                Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }
            if (intent.resolveActivity(view.context.packageManager) != null) {
                view.context.startActivity(intent)
            }
        }
    }
}
