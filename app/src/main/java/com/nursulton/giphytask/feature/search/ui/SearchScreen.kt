package com.nursulton.giphytask.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.core.designsystem.components.EmptyState
import com.nursulton.giphytask.core.designsystem.components.ErrorCard
import com.nursulton.giphytask.core.designsystem.components.GifCard
import com.nursulton.giphytask.core.designsystem.components.OfflineBanner
import com.nursulton.giphytask.core.designsystem.components.SearchBar
import com.nursulton.giphytask.core.designsystem.components.ShimmerGrid
import com.nursulton.giphytask.core.designsystem.theme.GiphyGreen
import com.nursulton.giphytask.core.designsystem.theme.GiphyTaskTheme
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.feature.search.state.SearchMode
import com.nursulton.giphytask.feature.search.state.SearchUiEvent
import com.nursulton.giphytask.feature.search.state.SearchUiState
import com.nursulton.giphytask.feature.search.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchScreenRoute(
    viewModel: SearchViewModel,
    onGifClick: (Gif) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedGifs = viewModel.gifPagingData.collectAsLazyPagingItems()

    SearchScreen(
        uiState = uiState,
        pagedGifs = pagedGifs,
        onEvent = viewModel::onEvent,
        onGifClick = onGifClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    pagedGifs: LazyPagingItems<Gif>,
    onEvent: (SearchUiEvent) -> Unit,
    onGifClick: (Gif) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = GiphyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GiphyTask",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                OfflineBanner(isOffline = uiState.isOffline)
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = uiState.query,
                onQueryChange = { onEvent(SearchUiEvent.QueryChanged(it)) },
                onSearchExecute = { onEvent(SearchUiEvent.ExecuteSearch(it)) },
                onClearQuery = { onEvent(SearchUiEvent.ClearQuery) },
                recentSearches = uiState.recentSearches,
                onSelectRecentSearch = { onEvent(SearchUiEvent.SelectRecentSearch(it)) },
                onRemoveRecentSearch = { onEvent(SearchUiEvent.RemoveRecentSearch(it)) },
                onClearHistory = { onEvent(SearchUiEvent.ClearSearchHistory) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.searchMode == SearchMode.TRENDING) {
                        "Trending GIFs"
                    } else {
                        "Results for \"${uiState.query}\""
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (val refreshState = pagedGifs.loadState.refresh) {
                is LoadState.Loading -> ShimmerGrid(modifier = Modifier.fillMaxSize())

                is LoadState.Error -> {
                    val appError = (refreshState.error as? AppError)
                        ?: AppError.Unknown(refreshState.error.message)
                    ErrorCard(
                        error = appError,
                        onRetry = { pagedGifs.retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is LoadState.NotLoading -> {
                    if (pagedGifs.itemCount == 0) {
                        EmptyState(
                            query = uiState.query,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        GifGrid(
                            pagedGifs = pagedGifs,
                            onGifClick = onGifClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GifGrid(
    pagedGifs: LazyPagingItems<Gif>,
    onGifClick: (Gif) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // itemKey/itemContentType are the Paging-provided helpers; indexing the list inside a
        // key lambda marks items as accessed and triggers unwanted prefetching.
        items(
            count = pagedGifs.itemCount,
            key = pagedGifs.itemKey { gif -> gif.id },
            contentType = pagedGifs.itemContentType { "gif" }
        ) { index ->
            pagedGifs[index]?.let { gif ->
                GifCard(
                    gif = gif,
                    onClick = onGifClick
                )
            }
        }

        when (pagedGifs.loadState.append) {
            is LoadState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            is LoadState.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { pagedGifs.retry() },
                            modifier = Modifier.semantics {
                                contentDescription = "Retry loading next page of GIFs"
                            }
                        ) {
                            Text("Retry loading more")
                        }
                    }
                }
            }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Preview
@Composable
private fun SearchScreenPreview() {
    val pagedGifs = flowOf(PagingData.from(emptyList<Gif>())).collectAsLazyPagingItems()
    GiphyTaskTheme {
        SearchScreen(
            uiState = SearchUiState(query = "Funny Cats"),
            pagedGifs = pagedGifs,
            onEvent = {},
            onGifClick = {}
        )
    }
}
