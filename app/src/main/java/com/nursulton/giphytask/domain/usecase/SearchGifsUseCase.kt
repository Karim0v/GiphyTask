package com.nursulton.giphytask.domain.usecase

import androidx.paging.PagingData
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.repository.GiphyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchGifsUseCase @Inject constructor(
    private val repository: GiphyRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Gif>> {
        val trimmedQuery = query.trim()
        return if (trimmedQuery.isEmpty()) {
            repository.getTrendingGifs()
        } else {
            repository.searchGifs(trimmedQuery)
        }
    }
}
