package com.onair.hearit.presentation.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.onair.hearit.R
import com.onair.hearit.domain.model.RecommendHearit
import com.onair.hearit.presentation.theme.Gray4
import com.onair.hearit.presentation.theme.HearitBlack
import com.onair.hearit.presentation.theme.HearitTypoGraphy
import com.onair.hearit.presentation.util.noRippleClickable
import com.onair.hearit.presentation.util.rememberSafeColor
import kotlin.math.absoluteValue

@Composable
fun CarouselCard(
    item: RecommendHearit,
    pageOffset: () -> Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val offset = pageOffset()
    val cardColor =
        rememberSafeColor(
            colorString = item.categoryColor,
            fallback = HearitBlack,
        )

    Card(
        modifier =
            modifier
                .zIndex(1f - offset.absoluteValue)
                .graphicsLayer {
                    val clamped = offset.absoluteValue.coerceIn(0f, 1f)
                    val scale = lerp(0.88f, 1f, 1f - clamped)
                    val alphaValue = lerp(0.6f, 1f, 1f - clamped)
                    scaleX = scale
                    scaleY = scale
                    alpha = alphaValue
                }.noRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (offset.absoluteValue < 0.5f) 8.dp else 2.dp,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = item.categoryName,
                color = Gray4,
                textAlign = TextAlign.Start,
                style = HearitTypoGraphy.titleLarge,
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_home_record),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f),
                )
            }

            Text(
                text = item.title,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                color = Gray4,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = HearitTypoGraphy.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true, name = "Current Card (Center)")
@Composable
private fun CarouselCardPreview() {
    MaterialTheme {
        CarouselCard(
            item =
                RecommendHearit(
                    id = 1L,
                    title = "합격 자기소개서 분석: 몰입 경험으로 개발 역량을 증명하는 법",
                    categoryName = "우테코",
                    categoryColor = "#12C6B0",
                ),
            pageOffset = { 0f },
            onClick = {},
            modifier = Modifier.height(320.dp),
        )
    }
}

@Preview(showBackground = true, name = "Side Card")
@Composable
private fun CarouselCardSidePreview() {
    MaterialTheme {
        CarouselCard(
            item =
                RecommendHearit(
                    id = 2L,
                    title = "Kotlin Coroutines 완벽 가이드",
                    categoryName = "Kotlin",
                    categoryColor = "#7C4DFF",
                ),
            pageOffset = { 1f },
            onClick = {},
            modifier = Modifier.height(320.dp),
        )
    }
}
