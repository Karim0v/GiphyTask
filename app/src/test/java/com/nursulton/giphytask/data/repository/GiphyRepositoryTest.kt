package com.nursulton.giphytask.data.repository

import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.core.common.DispatcherProvider
import com.nursulton.giphytask.core.common.Result
import com.nursulton.giphytask.data.remote.api.GiphyApi
import com.nursulton.giphytask.data.remote.dto.GifDto
import com.nursulton.giphytask.data.remote.dto.GiphySingleResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GiphyRepositoryTest {

    private val giphyApi: GiphyApi = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var repository: GiphyRepositoryImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = GiphyRepositoryImpl(giphyApi, dispatchers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getGifDetails returns Success when API returns valid GifDto`() = runTest {
        val dto = GifDto(id = "gif_1", title = "Test GIF")
        coEvery { giphyApi.getGifDetails("gif_1") } returns GiphySingleResponseDto(data = dto)

        val result = repository.getGifDetails("gif_1")

        assertTrue(result is Result.Success)
        val success = result as Result.Success
        assertEquals("gif_1", success.data.id)
        assertEquals("Test GIF", success.data.title)
    }

    @Test
    fun `getGifDetails returns Error NotFound when API returns null data`() = runTest {
        coEvery { giphyApi.getGifDetails("gif_999") } returns GiphySingleResponseDto(data = null)

        val result = repository.getGifDetails("gif_999")

        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals(AppError.NotFound, error.error)
    }
}
