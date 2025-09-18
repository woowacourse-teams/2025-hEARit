package com.onair.hearit.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.search.SearchViewModel
import com.onair.hearit.presentation.search.SearchViewModelFactory
import com.onair.hearit.presentation.theme.DarkGray
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.Gray3
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.Pretendard

class EmptyActivity : AppCompatActivity() {
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(SearchInput.Category(2, "Android", "#73A01A"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SearchResultScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding),
                    ) { finish() }
                }
            }
        }
    }
}

@Composable
fun SearchResultScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val hearits by viewModel.categoryHearits.collectAsStateWithLifecycle()

    GradientBackgroundScreen(
        colorCode = "#73A01A",
        hearits = hearits,
        modifier = modifier,
        onBack = onBack,
    )
}

@Composable
fun GradientBackgroundScreen(
    colorCode: String,
    hearits: List<SearchedHearit>,
    modifier: Modifier,
    onBack: () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to Color(colorCode.toColorInt()),
                                    0.2f to HearitBlack,
                                ),
                        ),
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Gray4,
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp, start = 24.dp),
        ) {
            Text(
                text = "Android",
                style =
                    TextStyle(
                        color = Gray4,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Pretendard,
                    ),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = painterResource(R.drawable.ic_down),
                contentDescription = "categoryList",
                modifier =
                    Modifier
                        .size(20.dp)
                        .alpha(0f),
                tint = Gray4,
            )
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 168.dp, bottom = 72.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(hearits) { item ->
                SearchedHearitItem(item = item, color = Color(colorCode.toColorInt()), {}, modifier)
            }
        }
    }
}

@Composable
fun SearchedHearitItem(
    item: SearchedHearit,
    color: Color,
    onClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Gray1,
                    shape = RoundedCornerShape(8.dp),
                )
                .clickable { onClick(item.id) }
                .padding(vertical = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 36.dp),
        ) {
            Text(
                text = item.title,
                modifier =
                    Modifier
                        .padding(start = 20.dp, end = 8.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style =
                    TextStyle(
                        color = Gray4,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp,
                        fontFamily = Pretendard,
                    ),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp, start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.keywords.forEach { keyword ->
                    Text(
                        text = "#${keyword.name}",
                        style =
                            TextStyle(
                                color = Gray2,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = Pretendard,
                            ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = item.playTime.toTimeString(),
                    color = Gray4,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard,
                )
            }

            val lastPlayTimeSec = (item.lastPlayTime ?: 0L) / 1000f
            val ratio = lastPlayTimeSec / item.playTime.toFloat()
            CustomLinearProgressBar(
                progress = ratio,
                backgroundColor = DarkGray,
                progressColor = color,
                cornerRadius = 48.dp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .padding(start = 20.dp, end = 8.dp),
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .padding(end = 8.dp),
            tint = Gray4,
        )
    }
}

@Composable
fun CustomLinearProgressBar(
    progress: Float,
    backgroundColor: Color,
    progressColor: Color,
    cornerRadius: Dp,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped) // 진행률 비율
                    .background(progressColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GradientBackgroundScreenPreview() {
    val dummyHearits =
        listOf(
            SearchedHearit(
                0,
                "이건 첫 번째 레슨, 좋은 건 너만 알기",
                playTime = 123,
                lastPlayTime = 83782,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "이제 두 번째 레슨, 슬픔도 너만 갖기",
                playTime = 1234,
                lastPlayTime = 192013,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "드디어 세 번째 레슨, 일희일비 않기",
                playTime = 1234,
                lastPlayTime = 99999,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "드디어 세 번째 레슨, 일희일비 않기",
                playTime = 1234,
                lastPlayTime = 99999,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "드디어 세 번째 레슨, 일희일비 않기",
                playTime = 1234,
                lastPlayTime = 99999,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "드디어 세 번째 레슨, 일희일비 않기",
                playTime = 1234,
                lastPlayTime = 99999,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
        )

    MaterialTheme {
        GradientBackgroundScreen(
            colorCode = "#73A01A",
            hearits = dummyHearits,
            modifier = Modifier.padding(4.dp),
            onBack = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun SearchedHearitItemPreview() {
    val dummy =
        SearchedHearit(
            0,
            "드디어 세 번째 레슨, 일희일비 않기. 좀 더 강해져야 돼. 웃어 넘길 수 있게...",
            playTime = 350,
            lastPlayTime = 99999,
            keywords = listOf(Keyword(1, "유노윤호"), Keyword(2, "U-KNOW")),
        )

    MaterialTheme {
        SearchedHearitItem(dummy, Gray3, {})
    }
}
