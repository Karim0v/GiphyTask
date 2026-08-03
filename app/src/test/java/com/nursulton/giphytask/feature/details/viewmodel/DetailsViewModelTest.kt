package com.nursulton.giphytask.feature.details.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.nursulton.giphytask.core.common.AppError
import com.nursulton.giphytask.core.common.Result
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.model.GifImages
import com.nursulton.giphytask.domain.usecase.GetGifDetailsUseCase
import com.nursulton.giphytask.feature.details.state.DetailsUiEffect
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val getGifDetailsUseCase: GetGifDetailsUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val fakeGif = Gif(
        id = "test_gif_1",
        title = "Funny Dog",
        username = "doggo",
        userDisplayName = "Dog Studio",
        userAvatarUrl = null,
        isUserVerified = false,
        rating = "G",
        importDate = "2024-01-01",
        trendingDate = null,
        sourceUrl = "https://source.com",
        webUrl = "https://giphy.com/gifs/test_gif_1",
        images = GifImages("", 300, 300, "", 150, 150, "")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads GIF details successfully`() = runTest {
        coEvery { getGifDetailsUseCase("test_gif_1") } returns Result.Success(fakeGif)

        val savedStateHandle = SavedStateHandle(mapOf(DetailsViewModel.KEY_GIF_ID to "test_gif_1"))
        val viewModel = DetailsViewModel(getGifDetailsUseCase, savedStateHandle)

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("test_gif_1", state.gif?.id)
            assertEquals("Funny Dog", state.gif?.title)
        }
    }

    @Test
    fun `shareGif emits ShareGif effect`() = runTest {
        coEvery { getGifDetailsUseCase("test_gif_1") } returns Result.Success(fakeGif)

        val savedStateHandle = SavedStateHandle(mapOf(DetailsViewModel.KEY_GIF_ID to "test_gif_1"))
        val viewModel = DetailsViewModel(getGifDetailsUseCase, savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.effects.test {
            viewModel.shareGif()
            val effect = awaitItem()
            assertEquals(DetailsUiEffect.ShareGif("https://giphy.com/gifs/test_gif_1", "Funny Dog"), effect)
        }
    }

    @Test
    fun `error result is surfaced in ui state`() = runTest {
        coEvery { getGifDetailsUseCase("test_gif_1") } returns Result.Error(AppError.NoInternet)

        val savedStateHandle = SavedStateHandle(mapOf(DetailsViewModel.KEY_GIF_ID to "test_gif_1"))
        val viewModel = DetailsViewModel(getGifDetailsUseCase, savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(AppError.NoInternet, state.error)
        }
    }

    @Test
    fun `missing gifId argument yields an error state instead of crashing`() = runTest {
        val viewModel = DetailsViewModel(getGifDetailsUseCase, SavedStateHandle())
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(AppError.NotFound, state.error)
        }
    }
}
