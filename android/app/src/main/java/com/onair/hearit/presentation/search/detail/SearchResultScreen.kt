package com.onair.hearit.presentation.search.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.search.component.HearitItem
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.noRippleClickable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

enum class SortType(
    @param:StringRes val labelRes: Int,
) {
    Recommend(R.string.search_result_sort_recommend),
    Accuracy(R.string.search_result_sort_accuracy),
    Latest(R.string.search_result_sort_latest),
    Oldest(R.string.search_result_sort_oldest),
}

@Composable
fun SearchResultScreen(
    hearits: ImmutableList<SearchedHearit>,
    onHearitClick: (Long) -> Unit,
    onLoadNext: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadMoreThreshold: Int = 3,
) {
    val listState = rememberLazyListState()
    val latestSize by rememberUpdatedState(hearits.size)
    val latestLoading by rememberUpdatedState(isLoading)

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.filterNotNull()
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (!latestLoading && lastVisibleIndex >= latestSize - loadMoreThreshold) {
                    onLoadNext()
                }
            }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(HearitBlack)
                .padding(top = 8.dp),
    ) {
        if (hearits.isEmpty() && !isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text =
                        buildAnnotatedString {
                            append("검색된 ")
                            withStyle(
                                style =
                                    SpanStyle(
                                        color = HearitPurple1,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append("히어릿")
                            }
                            append("이 없어요!")
                        },
                    color = Gray4,
                    style = HearitTypoGraphy.bodyLarge,
                )
            }
        } else if (hearits.isNotEmpty()) {
            ResultSearchHeader(SortType.Recommend)

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 68.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = hearits,
                    key = { it.id },
                ) { hearit ->
                    HearitItem(
                        title = hearit.title,
                        keywords = hearit.keywords,
                        playTime = hearit.playTime,
                        lastPlayTime = hearit.lastPlayTime,
                        progressColor = HearitPurple1,
                        onClick = { onHearitClick(hearit.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultSearchHeader(
    sortType: SortType,
    modifier: Modifier = Modifier,
    onSortSelected: (SortType) -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.search_result_header_title),
            style = HearitTypoGraphy.titleMedium,
            color = Gray4,
        )

        Box(
            contentAlignment = Alignment.TopEnd,
        ) {
            Row(
                modifier =
                    Modifier
                        .noRippleClickable { menuExpanded = true },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(sortType.labelRes),
                    style = HearitTypoGraphy.titleSmall,
                    color = Gray4,
                )

                Icon(
                    painter = painterResource(R.drawable.ic_down),
                    contentDescription = stringResource(R.string.search_result_sort_icon_desc),
                    modifier = Modifier.size(20.dp),
                    tint = Gray4,
                )
            }

            SortDropdownMenu(
                menuExpanded = menuExpanded,
                onDismiss = { menuExpanded = false },
                onSortSelected = onSortSelected,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDropdownMenu(
    menuExpanded: Boolean,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit,
) {
    var showTooltip by remember { mutableStateOf(false) }
    var infoAnchorBounds by remember { mutableStateOf<Rect?>(null) }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = {
            showTooltip = false
            onDismiss()
        },
        offset = DpOffset(x = 0.dp, y = 8.dp),
        containerColor = Color(0xB33B3F43),
        border = BorderStroke(1.dp, Color(0xB3686F75)),
    ) {
        DropdownMenuItem(
            text = {
                Row(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "추천순",
                        color = HearitPurple1,
                    )

                    Icon(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.search_result_sort_info_desc),
                        tint = HearitPurple1,
                        modifier =
                            Modifier
                                .padding(start = 24.dp)
                                .size(18.dp)
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInWindow()
                                    val size = coords.size
                                    infoAnchorBounds =
                                        Rect(
                                            left = pos.x,
                                            top = pos.y,
                                            right = pos.x + size.width,
                                            bottom = pos.y + size.height,
                                        )
                                }.noRippleClickable {
                                    showTooltip = !showTooltip
                                },
                    )
                }
            },
            onClick = { },
        )

        SortMenuItem(SortType.Accuracy, onDismiss, onSortSelected)
        SortMenuItem(SortType.Latest, onDismiss, onSortSelected)
        SortMenuItem(SortType.Oldest, onDismiss, onSortSelected)
    }

    // DropdownMenu 밖에서 별도 Popup으로 툴팁 렌더링
    if (showTooltip && infoAnchorBounds != null) {
        val bound = infoAnchorBounds!!

        Popup(
            alignment = Alignment.TopStart,
            offset =
                IntOffset(
                    x = bound.left.toInt(),
                    y = bound.top.toInt() - 120,
                ),
            properties = PopupProperties(focusable = false),
            onDismissRequest = { showTooltip = false },
        ) {
            Surface(
                color = HearitPurple1,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.search_result_sort_info_text),
                    modifier =
                        Modifier
                            .widthIn(max = 140.dp)
                            .padding(12.dp),
                    color = Gray4,
                    style = HearitTypoGraphy.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenuItem(
    type: SortType,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text = stringResource(type.labelRes)) },
        onClick = {
            onDismiss()
            onSortSelected(type)
        },
        modifier = Modifier.padding(start = 4.dp),
        colors = MenuDefaults.itemColors(textColor = Gray4),
    )
}

@Preview(showBackground = true)
@Composable
fun SearchResultScreenPreview() {
    val dummyHearits =
        persistentListOf(
            SearchedHearit(
                id = 1L,
                title = "안드로이드 클린 아키텍처 이해하기",
                playTime = 100,
                lastPlayTime = 50,
                keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            ),
            SearchedHearit(
                id = 2L,
                title = "Compose로 화면 구성하기",
                playTime = 100,
                lastPlayTime = 50,
                keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            ),
            SearchedHearit(
                id = 3L,
                title = "Kotlin Flow 완전 정복",
                playTime = 100,
                lastPlayTime = 50,
                keywords = persistentListOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
            ),
        )

    Box(modifier = Modifier.fillMaxSize()) {
        SearchResultScreen(
            hearits = dummyHearits,
            onHearitClick = {},
            onLoadNext = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchResultEmptyPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        SearchResultScreen(
            hearits = persistentListOf(),
            onHearitClick = {},
            onLoadNext = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SortDropdownMenuPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        SortDropdownMenu(
            menuExpanded = true,
            onDismiss = {},
            onSortSelected = {},
        )
    }
}
