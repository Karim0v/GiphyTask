package com.nursulton.giphytask.domain.usecase

import com.nursulton.giphytask.core.common.Result
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.repository.GiphyRepository
import javax.inject.Inject

class GetGifDetailsUseCase @Inject constructor(
    private val repository: GiphyRepository
) {
    suspend operator fun invoke(gifId: String): Result<Gif> {
        return repository.getGifDetails(gifId)
    }
}
