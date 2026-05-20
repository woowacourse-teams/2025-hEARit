package com.onair.hearit.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple2
import timber.log.Timber

class HearitWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val uiState =
            try {
                val entryPoint = context.hearitWidgetEntryPoint()
                BookmarkWidgetStateLoader(
                    bookmarkRepository = entryPoint.bookmarkRepository(),
                    authLocalDataSource = entryPoint.authLocalDataSource(),
                    authHeaderProvider = entryPoint.authHeaderProvider(),
                ).loadBookmarks()
            } catch (e: Exception) {
                Timber.e(e, "Widget provideGlance crashed")
                BookmarkWidgetUiState(
                    status = BookmarkWidgetStatus.UnknownError,
                    bookmarks = emptyList(),
                )
            }
        provideContent {
            HearitWidgetRuntimeContent(uiState)
        }
    }
}

@Composable
private fun HearitWidgetRuntimeContent(uiState: BookmarkWidgetUiState) {
    val context = LocalContext.current
    val size = LocalSize.current
    val isCompact = size.height <= COMPACT_WIDGET_HEIGHT
    HearitWidgetContent(
        uiState = uiState,
        openLibraryAction = actionStartActivity(widgetMainIntent(context)),
        detailActionFor = { hearitId ->
            actionStartActivity(widgetDetailIntent(context, hearitId, WIDGET_SOURCE_ID))
        },
        isCompact = isCompact,
    )
}

@Composable
private fun HearitWidgetContent(
    uiState: BookmarkWidgetUiState,
    openLibraryAction: Action,
    detailActionFor: (Long) -> Action,
    isCompact: Boolean = false,
) {
    val visibleBookmarks =
        uiState.bookmarks.take(if (isCompact) COMPACT_BOOKMARK_COUNT else MAX_BOOKMARK_COUNT)
    val widgetPadding = if (isCompact) 10.dp else 14.dp
    val headline = resolveHeadline(uiState)
    val emptyMessage = resolveEmptyMessage(uiState)

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .cornerRadius(22.dp)
                .background(ColorProvider(day = HearitBlack, night = HearitBlack)),
    ) {
        Column(
            modifier =
                GlanceModifier
                    .fillMaxWidth()
                    .padding(widgetPadding),
        ) {
            Text(
                text = headline,
                style =
                    TextStyle(
                        color = ColorProvider(day = HearitPurple2, night = HearitPurple2),
                        fontSize = if (isCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
                modifier = GlanceModifier.clickable(openLibraryAction),
            )

            Spacer(modifier = GlanceModifier.padding(top = 4.dp))

            if (visibleBookmarks.isEmpty()) {
                EmptyBookmarkCard(
                    message = emptyMessage,
                    action = openLibraryAction,
                )
            } else {
                visibleBookmarks.forEachIndexed { index, item ->
                    BookmarkRow(
                        item = item,
                        action = detailActionFor(item.hearitId),
                    )
                    if (index < visibleBookmarks.lastIndex) {
                        Spacer(modifier = GlanceModifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookmarkCard(
    message: String,
    action: Action,
) {
    Box(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .cornerRadius(14.dp)
                .background(ColorProvider(day = HearitSurface, night = HearitSurface))
                .clickable(action)
                .padding(horizontal = 12.dp, vertical = 14.dp),
    ) {
        Text(
            text = message,
            style =
                TextStyle(
                    color = ColorProvider(day = Gray2, night = Gray2),
                    fontSize = 11.sp,
                ),
        )
    }
}

@Composable
private fun BookmarkRow(
    item: BookmarkWidgetItem,
    action: Action,
) {
    Column(
        modifier =
            GlanceModifier
                .fillMaxWidth()
                .cornerRadius(10.dp)
                .background(ColorProvider(day = HearitSurface, night = HearitSurface))
                .clickable(action)
                .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(
            text = item.title,
            style =
                TextStyle(
                    color = ColorProvider(day = Gray2, night = Gray2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                ),
            maxLines = 1,
        )
        Text(
            text = item.category,
            style =
                TextStyle(
                    color = ColorProvider(day = HearitMuted, night = HearitMuted),
                    fontSize = 10.sp,
                ),
            maxLines = 1,
        )
    }
}

@Preview(widthDp = 250, heightDp = 250, showBackground = true)
@Composable
private fun HearitWidgetContentPreview() {
    HearitWidgetContent(
        uiState =
            BookmarkWidgetUiState(
                totalCount = 6,
                status = BookmarkWidgetStatus.Success,
                bookmarks =
                    listOf(
                        BookmarkWidgetItem(
                            hearitId = 1L,
                            title = "출근길 10분 뉴스 브리핑",
                            category = "뉴스",
                        ),
                        BookmarkWidgetItem(
                            hearitId = 2L,
                            title = "IT 트렌드 핵심 요약",
                            category = "테크",
                        ),
                        BookmarkWidgetItem(
                            hearitId = 3L,
                            title = "주말 문화 큐레이션",
                            category = "문화",
                        ),
                    ),
            ),
        openLibraryAction = actionStartActivity(Intent()),
        detailActionFor = { actionStartActivity(Intent()) },
    )
}

@Preview(widthDp = 110, heightDp = 110, showBackground = true)
@Composable
private fun HearitWidgetEmptyPreview() {
    HearitWidgetContent(
        uiState =
            BookmarkWidgetUiState(
                totalCount = 0,
                status = BookmarkWidgetStatus.Empty,
                bookmarks = emptyList(),
            ),
        openLibraryAction = actionStartActivity(Intent()),
        detailActionFor = { actionStartActivity(Intent()) },
        isCompact = true,
    )
}

private const val COMPACT_BOOKMARK_COUNT = 1
private const val MAX_BOOKMARK_COUNT = 4
private const val WIDGET_SOURCE_ID = "widget"
private val COMPACT_WIDGET_HEIGHT = 110.dp
private val HearitSurface = Color(0xFF1E1E1E)
private val HearitMuted = Color(0xFFB3B3B3)

private fun resolveHeadline(uiState: BookmarkWidgetUiState): String =
    when (uiState.status) {
        BookmarkWidgetStatus.Loading -> "북마크를 불러오는 중이에요"
        BookmarkWidgetStatus.RequireLogin -> "로그인이 필요해요"
        BookmarkWidgetStatus.NetworkError -> "네트워크 확인이 필요해요"
        BookmarkWidgetStatus.UnknownError -> "북마크를 가져오지 못했어요"
        BookmarkWidgetStatus.Empty -> "북마크가 아직 없어요"
        BookmarkWidgetStatus.Success -> "저장한 에피소드 ${uiState.totalCount}개"
    }

private fun resolveEmptyMessage(uiState: BookmarkWidgetUiState): String =
    when (uiState.status) {
        BookmarkWidgetStatus.Loading -> "잠시만 기다려 주세요."
        BookmarkWidgetStatus.RequireLogin -> "앱을 열어 다시 로그인하면 북마크를 보여드릴게요."
        BookmarkWidgetStatus.NetworkError -> "연결 상태를 확인한 뒤 위젯을 다시 불러와 주세요."
        BookmarkWidgetStatus.UnknownError -> "잠시 후 다시 시도해 주세요."
        BookmarkWidgetStatus.Empty -> "앱에서 저장한 콘텐츠를 위젯에서 바로 확인해보세요."
        BookmarkWidgetStatus.Success -> ""
    }
