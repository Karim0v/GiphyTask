package com.nursulton.giphytask.feature.details.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nursulton.giphytask.core.designsystem.components.ErrorCard
import com.nursulton.giphytask.core.designsystem.theme.GiphyGreen
import com.nursulton.giphytask.core.designsystem.theme.GiphyTaskTheme
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.model.GifImages
import com.nursulton.giphytask.feature.details.state.DetailsUiEffect
import com.nursulton.giphytask.feature.details.state.DetailsUiState
import com.nursulton.giphytask.feature.details.viewmodel.DetailsViewModel
import timber.log.Timber

@Composable
fun DetailsScreenRoute(
    viewModel: DetailsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            // A device with no browser (or no share target) throws ActivityNotFoundException,
            // which would otherwise crash the screen.
            when (effect) {
                is DetailsUiEffect.OpenBrowser ->
                    context.startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)))

                is DetailsUiEffect.ShareGif -> {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Check out this GIF on Giphy: ${effect.title}\n${effect.url}"
                        )
                        type = "text/plain"
                    }
                    context.startActivitySafely(
                        Intent.createChooser(sendIntent, "Share GIF via")
                    )
                }
            }
        }
    }

    DetailsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetryClick = viewModel::retry,
        onOpenBrowser = viewModel::openInBrowser,
        onShareGif = viewModel::shareGif,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOpenBrowser: () -> Unit,
    onShareGif: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GIF Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back to search screen"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCard(
                            error = uiState.error,
                            onRetry = onRetryClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                uiState.gif != null -> {
                    val gif = uiState.gif
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Main Animated GIF View
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            val aspectRatio = (gif.images.originalWidth.toFloat() / gif.images.originalHeight.toFloat())
                                .coerceIn(0.5f, 2.0f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(aspectRatio)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(gif.images.originalUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = gif.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.matchParentSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Creator Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!gif.userAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = gif.userAvatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = gif.userDisplayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (gif.isUserVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified creator",
                                            tint = GiphyGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "@${gif.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Rating Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = gif.rating,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = gif.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metadata Grid Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                MetadataRow(label = "Dimensions", value = "${gif.images.originalWidth} × ${gif.images.originalHeight} px")
                                MetadataRow(label = "Import Date", value = gif.importDate)
                                if (!gif.trendingDate.isNullOrBlank()) {
                                    MetadataRow(label = "Trending Date", value = gif.trendingDate)
                                }
                                if (gif.sourceUrl.isNotBlank()) {
                                    MetadataRow(label = "Source", value = gif.sourceUrl)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onShareGif,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription = "Share GIF link"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share")
                            }

                            OutlinedButton(
                                onClick = onOpenBrowser,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .semantics {
                                        contentDescription = "Open GIF in web browser"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Web")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No activity available to handle %s", intent.action)
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun DetailsScreenPreview() {
    GiphyTaskTheme {
        DetailsScreen(
            uiState = DetailsUiState(
                gif = Gif(
                    id = "1",
                    title = "Super Funny Cat Dancing",
                    username = "cat_master",
                    userDisplayName = "Cat Studio",
                    userAvatarUrl = null,
                    isUserVerified = true,
                    rating = "G",
                    importDate = "2024-01-15",
                    trendingDate = "2024-01-16",
                    sourceUrl = "https://giphy.com",
                    webUrl = "https://giphy.com",
                    images = GifImages(
                        originalUrl = "",
                        originalWidth = 600,
                        originalHeight = 400,
                        previewUrl = "",
                        previewWidth = 300,
                        previewHeight = 200,
                        downsizedUrl = ""
                    )
                )
            ),
            onBackClick = {},
            onRetryClick = {},
            onOpenBrowser = {},
            onShareGif = {}
        )
    }
}
