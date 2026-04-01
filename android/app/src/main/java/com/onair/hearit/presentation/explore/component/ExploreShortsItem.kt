package com.onair.hearit.presentation.explore.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onair.hearit.R
import com.onair.hearit.domain.model.ExploreHearit
import com.onair.hearit.domain.model.Keyword
import com.onair.hearit.presentation.theme.Gray1
import com.onair.hearit.presentation.theme.Gray2
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.theme.White
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@Composable
fun ExploreShortsItem(
    item: ExploreHearit,
    currentPosition: Long,
    duration: Long,
    onItemPlay: () -> Unit,
    onSpeedChanged: () -> Unit,
    isPlaying: Boolean,
    speed: Float,
    onNavigateToDetail: (id: Long) -> Unit,
    onPositionChanged: (position: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scripts =
        remember(item.script) {
            item.script?.toImmutableList() ?: persistentListOf()
        }

    var showPauseIcon by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }
    var isPressingSpeed by remember { mutableStateOf(false) }

    LaunchedEffect(showPauseIcon, tapCount) {
        if (showPauseIcon) {
            delay(5000L)
            showPauseIcon = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(isPlaying) {
                        detectTapGestures(
                            onTap = {
                                onItemPlay()
                                showPauseIcon = true
                                tapCount++
                            },
                            onLongPress = {
                                if (isPlaying) {
                                    onSpeedChanged()
                                    isPressingSpeed = true
                                }
                            },
                            onPress = {
                                try {
                                    awaitRelease()
                                } finally {
                                    if (isPressingSpeed) {
                                        onSpeedChanged()
                                        isPressingSpeed = false
                                    }
                                }
                            },
                        )
                    },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.18f))

            Text(
                text = item.title,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = HearitTypoGraphy.titleMedium,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.keywords.joinToString(" ") { "#${it.name}" },
                style = HearitTypoGraphy.bodySmall,
                color = Gray2,
                textAlign = TextAlign.Center,
            )

            RotatingLp(item = item, isPlaying = isPlaying, modifier = Modifier.fillMaxWidth())

            ScriptContent(
                scripts = scripts,
                currentPosition = currentPosition,
                modifier = Modifier.weight(1f),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(color = Gray1, shape = RoundedCornerShape(8.dp))
                        .clickable {
                            onNavigateToDetail(item.id)
                        }.padding(vertical = 20.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "팟캐스트 이어듣기",
                    modifier = Modifier.weight(1f),
                    style = HearitTypoGraphy.titleMedium,
                    textAlign = TextAlign.Start,
                    color = White,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_right),
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
            AudioProgressBar(
                modifier = Modifier.fillMaxWidth(),
                currentPositionMs = currentPosition,
                durationMs = duration,
                onPositionChanged = { newPosition ->
                    onPositionChanged(newPosition)
                },
            )
        }

        if (showPauseIcon) {
            Image(
                painter =
                    painterResource(
                        id =
                            if (isPlaying) {
                                R.drawable.img_shorts_play
                            } else {
                                R.drawable.img_shorts_pause
                            },
                    ),
                contentDescription = null,
                modifier = Modifier.size(60.dp),
            )
        }

        if (isPlaying && speed > 1.0f) {
            Image(
                painter = painterResource(id = R.drawable.img_shorts_boost),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(
                            BiasAlignment(
                                horizontalBias = 0f,
                                verticalBias = -0.5f,
                            ),
                        ).size(width = 79.dp, height = 30.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewExploreShortsItem() {
    ExploreShortsItem(
        currentPosition = 1000L,
        duration = 10000L,
        onNavigateToDetail = {},
        item =
            ExploreHearit(
                id = 1,
                title = "Activity를 쪼개며 배운 구조 설계sakdlsakjdkajlkdasa",
                categoryColorCode = "#000000",
                isBookmarked = false,
                bookmarkId = null,
                keywords =
                    listOf(
                        Keyword(id = 1, name = "보안"),
                        Keyword(id = 1, name = "자동화"),
                        Keyword(id = 1, name = "협업"),
                    ),
                cursorId = 1,
                audioUrl = "",
                script = listOf(),
            ),
        onPositionChanged = {},
        onItemPlay = {},
        onSpeedChanged = {},
        isPlaying = false,
        speed = 2.0f,
    )
}
