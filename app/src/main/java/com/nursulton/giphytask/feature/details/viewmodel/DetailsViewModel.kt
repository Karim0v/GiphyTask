package com.nursulton.giphytask.feature.details.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.core.common.Result
import com.nursulton.giphytask.domain.usecase.GetGifDetailsUseCase
import com.nursulton.giphytask.feature.details.state.DetailsUiEffect
import com.nursulton.giphytask.feature.details.state.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getGifDetailsUseCase: GetGifDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gifId: String = savedStateHandle.get<String>(KEY_GIF_ID)?.trim().orEmpty()

    private val _uiState = MutableStateFlow(DetailsUiState(isLoading = true))
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DetailsUiEffect>()
    val effects: SharedFlow<DetailsUiEffect> = _effects.asSharedFlow()

    init {
        loadGifDetails()
    }

    fun retry() {
        loadGifDetails()
    }

    fun openInBrowser() {
        val webUrl = uiState.value.gif?.webUrl
        if (webUrl.isNullOrBlank()) return
        viewModelScope.launch {
            _effects.emit(DetailsUiEffect.OpenBrowser(webUrl))
        }
    }

    fun shareGif() {
        val gif = uiState.value.gif ?: return
        if (gif.webUrl.isBlank()) return
        viewModelScope.launch {
            _effects.emit(DetailsUiEffect.ShareGif(url = gif.webUrl, title = gif.title))
        }
    }

    private fun loadGifDetails() {
        // A missing nav argument used to throw from init and crash the screen; surface it as a
        // retryable error state instead.
        if (gifId.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = AppError.NotFound) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getGifDetailsUseCase(gifId)) {
                is Result.Success ->
                    _uiState.update { it.copy(gif = result.data, isLoading = false, error = null) }

                is Result.Error ->
                    _uiState.update { it.copy(error = result.error, isLoading = false) }

                is Result.Loading ->
                    _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    companion object {
        /** Shared with the navigation graph so the argument name is declared exactly once. */
        const val KEY_GIF_ID = "gifId"
    }
}
