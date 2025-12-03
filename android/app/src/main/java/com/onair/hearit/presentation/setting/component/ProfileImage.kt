package com.onair.hearit.presentation.setting.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.onair.hearit.R

@Composable
fun ProfileImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    defaultImage: Int = R.drawable.img_default_profile,
) {
    Box(
        modifier = modifier.clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNullOrEmpty()) {
            Image(
                painter = painterResource(defaultImage),
                contentDescription = "프로필 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "프로필 이미지",
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(defaultImage),
                error = painterResource(defaultImage),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview
@Composable
fun ProfileImagePreview() {
    ProfileImage(imageUrl = null, modifier = Modifier.size(120.dp))
}
