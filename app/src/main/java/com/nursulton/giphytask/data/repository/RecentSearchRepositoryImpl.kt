package com.nursulton.giphytask.data.repository

import com.nursulton.giphytask.core.common.DispatcherProvider
import com.nursulton.giphytask.data.local.RecentSearchLocalDataSource
import com.nursulton.giphytask.domain.model.RecentSearch
import com.nursulton.giphytask.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentSearchRepositoryImpl @Inject constructor(
    private val localDataSource: RecentSearchLocalDataSource,
    private val dispatchers: DispatcherProvider
) : RecentSearchRepository {

    override fun getRecentSearches(): Flow<List<RecentSearch>> {
        return localDataSource.getRecentSearches()
    }

    override suspend fun saveRecentSearch(query: String) = withContext(dispatchers.io) {
        localDataSource.saveSearch(query)
    }

    override suspend fun clearRecentSearches() = withContext(dispatchers.io) {
        localDataSource.clearSearches()
    }

    override suspend fun removeRecentSearch(query: String) = withContext(dispatchers.io) {
        localDataSource.deleteSearch(query)
    }
}
