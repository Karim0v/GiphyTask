package com.nursulton.giphytask.domain.usecase

import androidx.paging.PagingData
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.repository.GiphyRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class SearchGifsUseCaseTest {

    private val repository: GiphyRepository = mockk()
    private val useCase = SearchGifsUseCase(repository)

    @Test
    fun `invoke with blank query calls getTrendingGifs`() {
        every { repository.getTrendingGifs() } returns flowOf(PagingData.empty())

        useCase("")

        verify(exactly = 1) { repository.getTrendingGifs() }
        verify(exactly = 0) { repository.searchGifs(any()) }
    }

    @Test
    fun `invoke with non-blank query calls searchGifs`() {
        every { repository.searchGifs("dancing cat") } returns flowOf(PagingData.empty())

        useCase("  dancing cat  ")

        verify(exactly = 1) { repository.searchGifs("dancing cat") }
        verify(exactly = 0) { repository.getTrendingGifs() }
    }
}
