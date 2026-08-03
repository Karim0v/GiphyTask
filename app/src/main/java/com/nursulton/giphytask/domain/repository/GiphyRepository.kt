package com.nursulton.giphytask.domain.repository

import androidx.paging.PagingData
import com.nursulton.giphytask.core.common.Result
import com.nursulton.giphytask.domain.model.Gif
import kotlinx.coroutines.flow.Flow

interface GiphyRepository {
    fun searchGifs(query: String): Flow<PagingData<Gif>>
    fun getTrendingGifs(): Flow<PagingData<Gif>>
    suspend fun getGifDetails(gifId: String): Result<Gif>
}
