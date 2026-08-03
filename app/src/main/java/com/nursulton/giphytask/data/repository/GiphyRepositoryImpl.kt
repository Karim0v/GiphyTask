package com.nursulton.giphytask.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.core.common.DispatcherProvider
import com.nursulton.giphytask.core.common.Result
import com.nursulton.giphytask.core.network.toAppError
import com.nursulton.giphytask.data.mapper.toDomain
import com.nursulton.giphytask.data.remote.api.GiphyApi
import com.nursulton.giphytask.data.remote.paging.GiphyPagingSource
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.repository.GiphyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GiphyRepositoryImpl @Inject constructor(
    private val giphyApi: GiphyApi,
    private val dispatchers: DispatcherProvider
) : GiphyRepository {

    override fun searchGifs(query: String): Flow<PagingData<Gif>> = pagedGifs(query)

    override fun getTrendingGifs(): Flow<PagingData<Gif>> = pagedGifs(query = null)

    override suspend fun getGifDetails(gifId: String): Result<Gif> = withContext(dispatchers.io) {
        try {
            val response = giphyApi.getGifDetails(gifId)
            val dto = response.data ?: return@withContext Result.Error(AppError.NotFound)
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Error(e.toAppError())
        }
    }

    /** `initialLoadSize` matches `pageSize` so the offset arithmetic stays consistent. */
    private fun pagedGifs(query: String?): Flow<PagingData<Gif>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            initialLoadSize = PAGE_SIZE
        ),
        pagingSourceFactory = { GiphyPagingSource(giphyApi, query) }
    ).flow

    private companion object {
        const val PAGE_SIZE = 25
    }
}
