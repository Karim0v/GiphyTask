package com.nursulton.giphytask.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nursulton.giphytask.core.designsystem.theme.GiphyTaskTheme
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.model.GifImages

@Composable
fun GifCard(
    gif: Gif,
    onClick: (Gif) -> Unit,
    modifier: Modifier = Modifier
) {
    val aspectRatio = (gif.images.previewWidth.toFloat() / gif.images.previewHeight.toFloat())
        .coerceIn(0.7f, 1.8f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) { onClick(gif) }
            .semantics {
                contentDescription = "GIF titled ${gif.title} by ${gif.userDisplayName}"
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(gif.images.previewUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null, // described by the Card's semantics below
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            // Dark gradient overlay at bottom for title readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = gif.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun GifCardPreview() {
    GiphyTaskTheme {
        GifCard(
            gif = Gif(
                id = "1",
                title = "Cute Funny Cat Dancing",
                username = "giphytask",
                userDisplayName = "Giphy Creator",
                userAvatarUrl = null,
                isUserVerified = true,
                rating = "G",
                importDate = "2024-01-01",
                trendingDate = null,
                sourceUrl = "",
                webUrl = "",
                images = GifImages(
                    originalUrl = "",
                    originalWidth = 480,
                    originalHeight = 480,
                    previewUrl = "",
                    previewWidth = 480,
                    previewHeight = 480,
                    downsizedUrl = ""
                )
            ),
            onClick = {}
        )
    }
}
