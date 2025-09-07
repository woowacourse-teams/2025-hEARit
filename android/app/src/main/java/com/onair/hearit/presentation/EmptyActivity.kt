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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onair.hearit.R
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.domain.model.SearchInput
import com.onair.hearit.domain.model.SearchedHearit
import com.onair.hearit.presentation.search.result.SearchResultViewModel
import com.onair.hearit.presentation.search.result.SearchResultViewModelFactory

class EmptyActivity : AppCompatActivity() {
    private val viewModel: SearchResultViewModel by viewModels {
        SearchResultViewModelFactory(SearchInput.Category(2, "Android"))
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
    viewModel: SearchResultViewModel,
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
    val startColor =
        try {
            Color(colorCode.toColorInt())
        } catch (e: IllegalArgumentException) {
            Color.Black
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0.0f to startColor,
                                    0.2f to colorResource(id = R.color.hearit_black1),
                                    1.0f to colorResource(id = R.color.hearit_black1),
                                ),
                        ),
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding(),
                    ).padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified,
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Android",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_down),
                contentDescription = "categoryList",
                modifier = Modifier.size(20.dp),
                tint = Color.White,
            )
        }

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 180.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(hearits) { item ->
                SearchedHearitItem(item = item) { }
            }
        }
    }
}

@Composable
fun SearchedHearitItem(
    item: SearchedHearit,
    onClick: (Long) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    colorResource(id = R.color.hearit_gray1),
                    shape = RoundedCornerShape(8.dp),
                ).clickable { onClick(item.id) }
                .padding(vertical = 20.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 36.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier =
                    Modifier
                        .padding(start = 20.dp, end = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
            ) {
                items(item.keywords) { keyword ->
                    Keywords(keyword.name)
                }
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .padding(end = 8.dp),
            tint = Color.Unspecified,
        )
    }
}

@Composable
fun Keywords(keyword: String) {
    Text(
        text = "#$keyword",
        color = colorResource(R.color.hearit_gray2),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Preview(showBackground = true)
@Composable
fun GradientBackgroundScreenPreview() {
    val dummyHearits =
        listOf(
            SearchedHearit(
                0,
                "첫 번째",
                playTime = 1234,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "2 번째",
                playTime = 1234,
                keywords = listOf(Keyword(1, "aa"), Keyword(2, "bb")),
            ),
            SearchedHearit(
                0,
                "3 번째",
                playTime = 1234,
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
            "Intent란 무엇인가? Intent란 무엇인가 제목 2줄 표시 2줄 표시 2줄 표시 ...",
            playTime = 1234,
            keywords = listOf(Keyword(1, "Kotlin"), Keyword(2, "Http")),
        )

    MaterialTheme {
        SearchedHearitItem(dummy) {}
    }
}
