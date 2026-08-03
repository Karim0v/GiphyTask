package com.nursulton.giphytask.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nursulton.giphytask.core.network.toAppError
import com.nursulton.giphytask.data.mapper.toDomainList
import com.nursulton.giphytask.data.remote.api.GiphyApi
import com.nursulton.giphytask.domain.model.Gif
import timber.log.Timber

/**
 * Offset-based [PagingSource]. Keys are Giphy `offset` values, so the next key advances by the
 * number of items actually returned rather than by the requested page size.
 */
class GiphyPagingSource(
    private val giphyApi: GiphyApi,
    private val query: String? = null
) : PagingSource<Int, Gif>() {

    override fun getRefreshKey(state: PagingState<Int, Gif>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Gif> {
        val offset = params.key ?: INITIAL_OFFSET
        val loadSize = params.loadSize

        return try {
            val response = if (query.isNullOrBlank()) {
                giphyApi.getTrendingGifs(limit = loadSize, offset = offset)
            } else {
                giphyApi.searchGifs(query = query, limit = loadSize, offset = offset)
            }

            val gifs = response.data.toDomainList()
            val totalCount = response.pagination?.totalCount ?: 0
            val nextOffset = offset + gifs.size

            // A short page means the result set is exhausted. total_count is only used as a
            // secondary signal because Giphy does not always report it for the trending feed.
            val endReached = gifs.isEmpty() ||
                gifs.size < loadSize ||
                (totalCount > 0 && nextOffset >= totalCount)

            LoadResult.Page(
                data = gifs,
                prevKey = if (offset == INITIAL_OFFSET) null else (offset - loadSize).coerceAtLeast(INITIAL_OFFSET),
                nextKey = if (endReached) null else nextOffset
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to load GIFs (query=%s, offset=%d)", query, offset)
            LoadResult.Error(e.toAppError())
        }
    }

    companion object {
        const val INITIAL_OFFSET = 0
    }
}
