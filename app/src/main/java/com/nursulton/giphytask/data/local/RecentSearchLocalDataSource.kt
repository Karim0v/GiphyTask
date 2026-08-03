package com.nursulton.giphytask.data.local

import com.nursulton.giphytask.domain.model.RecentSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentSearchLocalDataSource @Inject constructor(
    private val dao: RecentSearchDao
) {
    fun getRecentSearches(): Flow<List<RecentSearch>> {
        return dao.getRecentSearches().map { it.toDomainList() }
    }

    /**
     * The `query` primary key is case-sensitive on its own, so "Cats" typed after "cats" would
     * add a second row. Deleting case-insensitively first keeps one entry per term (with the
     * newest spelling), then [RecentSearchDao.trimHistory] caps the list at 10.
     */
    suspend fun saveSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        dao.deleteSearch(trimmed)
        dao.insertSearch(
            RecentSearchEntity(query = trimmed, timestamp = System.currentTimeMillis())
        )
        dao.trimHistory()
    }

    suspend fun deleteSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            dao.deleteSearch(trimmed)
        }
    }

    suspend fun clearSearches() {
        dao.clearSearches()
    }
}
