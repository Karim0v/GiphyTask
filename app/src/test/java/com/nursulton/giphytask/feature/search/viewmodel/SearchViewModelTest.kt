package com.nursulton.giphytask.feature.search.viewmodel

import androidx.paging.PagingData
import app.cash.turbine.test
import com.nursulton.giphytask.core.common.DispatcherProvider
import com.nursulton.giphytask.core.network.NetworkMonitor
import com.nursulton.giphytask.domain.model.RecentSearch
import com.nursulton.giphytask.domain.usecase.ClearRecentSearchesUseCase
import com.nursulton.giphytask.domain.usecase.GetRecentSearchesUseCase
import com.nursulton.giphytask.domain.usecase.RemoveRecentSearchUseCase
import com.nursulton.giphytask.domain.usecase.SaveRecentSearchUseCase
import com.nursulton.giphytask.domain.usecase.SearchGifsUseCase
import com.nursulton.giphytask.feature.search.state.SearchMode
import com.nursulton.giphytask.feature.search.state.SearchUiEvent
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val searchGifsUseCase: SearchGifsUseCase = mockk(relaxed = true)
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase = mockk(relaxed = true)
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase = mockk(relaxed = true)
    private val removeRecentSearchUseCase: RemoveRecentSearchUseCase = mockk(relaxed = true)
    private val clearRecentSearchesUseCase: ClearRecentSearchesUseCase = mockk(relaxed = true)
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { networkMonitor.isOnline } returns flowOf(true)
        every { getRecentSearchesUseCase() } returns flowOf(emptyList())
        every { searchGifsUseCase(any()) } returns flowOf(PagingData.empty())

        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = SearchViewModel(
        searchGifsUseCase = searchGifsUseCase,
        getRecentSearchesUseCase = getRecentSearchesUseCase,
        saveRecentSearchUseCase = saveRecentSearchUseCase,
        removeRecentSearchUseCase = removeRecentSearchUseCase,
        clearRecentSearchesUseCase = clearRecentSearchesUseCase,
        networkMonitor = networkMonitor,
        dispatchers = dispatchers
    )

    @Test
    fun `initial state has empty query and TRENDING mode`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.query)
            assertEquals(SearchMode.TRENDING, state.searchMode)
        }
    }

    @Test
    fun `QueryChanged event updates query and SEARCH mode`() = runTest {
        viewModel.onEvent(SearchUiEvent.QueryChanged("Cats"))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Cats", state.query)
            assertEquals(SearchMode.SEARCH, state.searchMode)
        }
    }

    @Test
    fun `ClearQuery event resets back to TRENDING mode`() = runTest {
        viewModel.onEvent(SearchUiEvent.QueryChanged("Cats"))
        viewModel.onEvent(SearchUiEvent.ClearQuery)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.query)
            assertEquals(SearchMode.TRENDING, state.searchMode)
        }
    }

    @Test
    fun `ExecuteSearch trims the term and saves it to history`() = runTest {
        viewModel.onEvent(SearchUiEvent.ExecuteSearch("  Super Cat  "))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { saveRecentSearchUseCase("Super Cat") }
    }

    @Test
    fun `blank ExecuteSearch is ignored`() = runTest {
        viewModel.onEvent(SearchUiEvent.ExecuteSearch("   "))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 0) { saveRecentSearchUseCase(any()) }
    }

    @Test
    fun `RemoveRecentSearch event deletes the single history entry`() = runTest {
        viewModel.onEvent(SearchUiEvent.RemoveRecentSearch("Old Search"))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { removeRecentSearchUseCase("Old Search") }
    }

    @Test
    fun `ClearSearchHistory event clears the whole history`() = runTest {
        viewModel.onEvent(SearchUiEvent.ClearSearchHistory)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { clearRecentSearchesUseCase() }
    }

    @Test
    fun `recent searches from the repository are exposed in ui state`() = runTest {
        val history = listOf(RecentSearch("dogs", 1L), RecentSearch("cats", 2L))
        every { getRecentSearchesUseCase() } returns flowOf(history)

        val freshViewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        freshViewModel.uiState.test {
            assertEquals(history, awaitItem().recentSearches)
        }
    }

    @Test
    fun `offline network state is reflected in ui state`() = runTest {
        every { networkMonitor.isOnline } returns flowOf(false)

        val freshViewModel = buildViewModel()
        testScheduler.advanceUntilIdle()

        freshViewModel.uiState.test {
            assertEquals(true, awaitItem().isOffline)
        }
    }
}
