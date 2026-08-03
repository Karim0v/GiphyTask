package com.nursulton.giphytask.feature.details.state

import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.domain.model.Gif

data class DetailsUiState(
    val gif: Gif? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null
)

sealed interface DetailsUiEffect {
    data class OpenBrowser(val url: String) : DetailsUiEffect
    data class ShareGif(val url: String, val title: String) : DetailsUiEffect
}
