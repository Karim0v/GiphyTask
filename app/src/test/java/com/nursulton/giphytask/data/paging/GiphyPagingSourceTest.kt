package com.nursulton.giphytask.data.paging

import androidx.paging.PagingSource
import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.data.remote.api.GiphyApi
import com.nursulton.giphytask.data.remote.dto.GifDto
import com.nursulton.giphytask.data.remote.dto.GiphyResponseDto
import com.nursulton.giphytask.data.remote.dto.PaginationDto
import com.nursulton.giphytask.data.remote.paging.GiphyPagingSource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val PAGE_SIZE = 25

class GiphyPagingSourceTest {

    private val giphyApi: GiphyApi = mockk()

    private fun gifDtos(count: Int): List<GifDto> =
        (1..count).map { GifDto(id = "$it", title = "GIF $it") }

    private fun refresh(key: Int?) = PagingSource.LoadParams.Refresh<Int>(
        key = key,
        loadSize = PAGE_SIZE,
        placeholdersEnabled = false
    )

    @Test
    fun `load returns a full page with the next offset when more results exist`() = runTest {
        val response = GiphyResponseDto(
            data = gifDtos(PAGE_SIZE),
            pagination = PaginationDto(totalCount = 100, count = PAGE_SIZE, offset = 0)
        )
        coEvery { giphyApi.searchGifs(query = "cat", limit = PAGE_SIZE, offset = 0) } returns response

        val result = GiphyPagingSource(giphyApi, query = "cat").load(refresh(key = 0))

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(PAGE_SIZE, page.data.size)
        assertEquals("1", page.data.first().id)
        assertNull(page.prevKey)
        // Offset paging: the next key advances by the number of items actually returned.
        assertEquals(PAGE_SIZE, page.nextKey)
    }

    @Test
    fun `load ends pagination when the API returns a short page`() = runTest {
        val response = GiphyResponseDto(
            data = gifDtos(2),
            pagination = PaginationDto(totalCount = 100, count = 2, offset = 0)
        )
        coEvery { giphyApi.searchGifs(query = "cat", limit = PAGE_SIZE, offset = 0) } returns response

        val result = GiphyPagingSource(giphyApi, query = "cat").load(refresh(key = 0))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(2, page.data.size)
        // Fewer items than requested means the result set is exhausted, even though
        // total_count still claims there are 100 matches.
        assertNull(page.nextKey)
    }

    @Test
    fun `load uses the trending endpoint when the query is null`() = runTest {
        val response = GiphyResponseDto(
            data = gifDtos(PAGE_SIZE),
            pagination = PaginationDto(totalCount = 500, count = PAGE_SIZE, offset = 0)
        )
        coEvery { giphyApi.getTrendingGifs(limit = PAGE_SIZE, offset = 0) } returns response

        val result = GiphyPagingSource(giphyApi, query = null).load(refresh(key = null))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(PAGE_SIZE, page.data.size)
        assertEquals(PAGE_SIZE, page.nextKey)
    }

    @Test
    fun `load maps thrown exceptions to an AppError result`() = runTest {
        coEvery {
            giphyApi.getTrendingGifs(limit = PAGE_SIZE, offset = 0)
        } throws RuntimeException("Network Error")

        val result = GiphyPagingSource(giphyApi, query = null).load(refresh(key = 0))

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertTrue(error.throwable is AppError.Unknown)
    }
}
