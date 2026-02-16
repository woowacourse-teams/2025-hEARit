package com.onair.hearit.presentation.search.detail

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.search.component.SearchedHearitItem
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitPurple1
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.noRippleClickable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private enum class SortType { Accuracy, Latest, Oldest }

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
            ResultSearchHeader()

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
                    SearchedHearitItem(
                        item = hearit,
                        color = Gray3,
                        onClick = onHearitClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultSearchHeader(
    modifier: Modifier = Modifier,
    sortLabel: String = "추천순",
    onSortSelected: (SortType) -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "검색된 히어릿 목록",
            style = HearitTypoGraphy.titleMedium,
            color = Gray4,
        )

        // ✅ 앵커 고정용 Box
        Box(
            contentAlignment = Alignment.TopEnd,
        ) {
            Row(
                modifier =
                    Modifier
                        .noRippleClickable { menuExpanded = true },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = sortLabel,
                    style = HearitTypoGraphy.titleSmall,
                    color = Gray4,
                )

                Icon(
                    painter = painterResource(R.drawable.ic_down),
                    contentDescription = "검색 히어릿 정렬 아이콘",
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
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 0.dp, y = 8.dp),
        containerColor = Color(0xB33B3F43),
        border =
            BorderStroke(
                width = 1.dp,
                color = Color(0xB3686F75),
            ),
    ) {
        DropdownMenuItem(
            enabled = false,
            colors =
                MenuDefaults.itemColors(
                    disabledTextColor = HearitPurple1,
                    disabledTrailingIconColor = HearitPurple1,
                ),
            text = {
                HeaderWithClickableTooltip(
                    title = "추천순",
                    titleColor = HearitPurple1,
                    tooltipBg = HearitPurple1,
                    tooltipTextColor = Gray4,
                    infoTint = HearitPurple1,
                )
            },
            onClick = {},
        )

        SortMenuItem(
            label = "최신순",
            onDismiss = onDismiss,
            onSortSelected = onSortSelected,
        )
        SortMenuItem(
            label = "정확도순",
            onDismiss = onDismiss,
            onSortSelected = onSortSelected,
        )
        SortMenuItem(
            label = "오래된순",
            onDismiss = onDismiss,
            onSortSelected = onSortSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortMenuItem(
    label: String,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit,
) {
    val type =
        when (label) {
            "정확도순" -> SortType.Accuracy
            "최신순" -> SortType.Latest
            "오래된순" -> SortType.Oldest
            else -> return
        }

    DropdownMenuItem(
        text = { Text(text = label) },
        onClick = {
            onDismiss()
            onSortSelected(type)
        },
        colors = MenuDefaults.itemColors(textColor = Gray4),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderWithClickableTooltip(
    title: String,
    titleColor: Color,
    tooltipBg: Color,
    tooltipTextColor: Color,
    infoTint: Color,
) {
    val scope = rememberCoroutineScope()
    val tooltipState = rememberTooltipState(isPersistent = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = titleColor)

        TooltipBox(
            positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Above,
                    spacingBetweenTooltipAndAnchor = 4.dp,
                ),
            state = tooltipState,
            tooltip = {
                RichTooltip(
                    caretShape = TooltipDefaults.richTooltipContainerShape,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        TooltipDefaults.richTooltipColors(
                            containerColor = tooltipBg,
                            contentColor = tooltipTextColor,
                        ),
                    text = {
                        Text("추천순은 정확도, 조회수,\n날짜 등을 기준으로\n정렬됩니다.")
                    },
                )
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = "정렬 기준 안내",
                tint = infoTint,
                modifier =
                    Modifier
                        .size(20.dp)
                        .noRippleClickable {
                            scope.launch {
                                if (tooltipState.isVisible) {
                                    tooltipState.dismiss()
                                } else {
                                    tooltipState.show()
                                }
                            }
                        },
            )
        }
    }
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
