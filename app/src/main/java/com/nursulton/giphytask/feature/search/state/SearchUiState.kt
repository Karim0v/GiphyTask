package com.nursulton.giphytask.feature.search.state

import com.nursulton.giphytask.domain.model.RecentSearch

enum class SearchMode {
    TRENDING,
    SEARCH
}

data class SearchUiState(
    val query: String = "",
    val searchMode: SearchMode = SearchMode.TRENDING,
    val isOffline: Boolean = false,
    val recentSearches: List<RecentSearch> = emptyList()
)

sealed interface SearchUiEvent {
    data class QueryChanged(val newQuery: String) : SearchUiEvent
    data class ExecuteSearch(val query: String) : SearchUiEvent
    data object ClearQuery : SearchUiEvent
    data class SelectRecentSearch(val query: String) : SearchUiEvent
    data object ClearSearchHistory : SearchUiEvent
    data class RemoveRecentSearch(val query: String) : SearchUiEvent
}
