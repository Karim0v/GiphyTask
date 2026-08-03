package com.nursulton.giphytask.domain.repository

import com.nursulton.giphytask.domain.model.RecentSearch
import kotlinx.coroutines.flow.Flow

interface RecentSearchRepository {
    fun getRecentSearches(): Flow<List<RecentSearch>>
    suspend fun saveRecentSearch(query: String)
    suspend fun clearRecentSearches()
    suspend fun removeRecentSearch(query: String)
}
