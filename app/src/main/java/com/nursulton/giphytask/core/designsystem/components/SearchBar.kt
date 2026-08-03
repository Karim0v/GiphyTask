package com.nursulton.giphytask.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nursulton.giphytask.core.designsystem.theme.GiphyTaskTheme
import com.nursulton.giphytask.domain.model.RecentSearch

private const val VISIBLE_HISTORY_ITEMS = 5

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchExecute: (String) -> Unit,
    onClearQuery: () -> Unit,
    recentSearches: List<RecentSearch>,
    onSelectRecentSearch: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .onFocusChanged { isFocused = it.isFocused }
                .semantics {
                    contentDescription = "Search GIF text input field"
                },
            placeholder = {
                Text(
                    text = "Search GIFs, reactions, memes...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search query"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    onSearchExecute(query)
                }
            )
        )

        AnimatedVisibility(
            visible = isFocused && query.isBlank() && recentSearches.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            RecentSearchList(
                recentSearches = recentSearches,
                onSelectRecentSearch = {
                    onSelectRecentSearch(it)
                    keyboardController?.hide()
                },
                onRemoveRecentSearch = onRemoveRecentSearch,
                onClearHistory = onClearHistory
            )
        }
    }
}

@Composable
private fun RecentSearchList(
    recentSearches: List<RecentSearch>,
    onSelectRecentSearch: (String) -> Unit,
    onRemoveRecentSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Clear all",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onClearHistory() }
                        .semantics { contentDescription = "Clear all recent searches" }
                        .padding(4.dp)
                )
            }

            recentSearches.take(VISIBLE_HISTORY_ITEMS).forEach { recent ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectRecentSearch(recent.query) }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = recent.query,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    // Makes the per-item RemoveRecentSearch event reachable from the UI.
                    IconButton(
                        onClick = { onRemoveRecentSearch(recent.query) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove ${recent.query} from recent searches",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchBarPreview() {
    GiphyTaskTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SearchBar(
                query = "Cat memes",
                onQueryChange = {},
                onSearchExecute = {},
                onClearQuery = {},
                recentSearches = listOf(
                    RecentSearch("Funny cats", 0),
                    RecentSearch("Reaction gifs", 0)
                ),
                onSelectRecentSearch = {},
                onRemoveRecentSearch = {},
                onClearHistory = {}
            )
        }
    }
}
