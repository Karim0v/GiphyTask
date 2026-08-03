package com.nursulton.giphytask.feature.search.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nursulton.giphytask.core.common.DispatcherProvider
import com.nursulton.giphytask.core.network.NetworkMonitor
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.usecase.ClearRecentSearchesUseCase
import com.nursulton.giphytask.domain.usecase.GetRecentSearchesUseCase
import com.nursulton.giphytask.domain.usecase.RemoveRecentSearchUseCase
import com.nursulton.giphytask.domain.usecase.SaveRecentSearchUseCase
import com.nursulton.giphytask.domain.usecase.SearchGifsUseCase
import com.nursulton.giphytask.feature.search.state.SearchMode
import com.nursulton.giphytask.feature.search.state.SearchUiEvent
import com.nursulton.giphytask.feature.search.state.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchGifsUseCase: SearchGifsUseCase,
    getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val removeRecentSearchUseCase: RemoveRecentSearchUseCase,
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /** Internal trigger for the paging stream; [uiState] stays the single source of truth for UI. */
    private val queryTrigger = MutableStateFlow("")

    val gifPagingData: Flow<PagingData<Gif>> = queryTrigger
        // Only debounce actual typing. A flat debounce(400) would also delay the very first
        // trending load (and every "clear"), making the screen look sluggish on launch.
        .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
        .flatMapLatest { searchGifsUseCase(it) }
        .cachedIn(viewModelScope)

    init {
        observeNetworkState()
        observeRecentSearches(getRecentSearchesUseCase)
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> updateQuery(event.newQuery)

            is SearchUiEvent.ExecuteSearch -> {
                val trimmed = event.query.trim()
                if (trimmed.isNotBlank()) {
                    updateQuery(trimmed)
                    launchIo { saveRecentSearchUseCase(trimmed) }
                }
            }

            is SearchUiEvent.ClearQuery -> updateQuery("")

            is SearchUiEvent.SelectRecentSearch ->
                onEvent(SearchUiEvent.ExecuteSearch(event.query))

            is SearchUiEvent.ClearSearchHistory ->
                launchIo { clearRecentSearchesUseCase() }

            is SearchUiEvent.RemoveRecentSearch ->
                launchIo { removeRecentSearchUseCase(event.query) }
        }
    }

    private fun updateQuery(newQuery: String) {
        queryTrigger.value = newQuery
        _uiState.update { state ->
            state.copy(
                query = newQuery,
                searchMode = if (newQuery.isBlank()) SearchMode.TRENDING else SearchMode.SEARCH
            )
        }
    }

    private fun launchIo(block: suspend () -> Unit) {
        viewModelScope.launch(dispatchers.io) { block() }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    private fun observeRecentSearches(getRecentSearchesUseCase: GetRecentSearchesUseCase) {
        viewModelScope.launch {
            getRecentSearchesUseCase().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
    }
}
