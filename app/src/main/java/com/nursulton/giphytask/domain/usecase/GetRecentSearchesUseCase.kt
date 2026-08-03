package com.nursulton.giphytask.domain.usecase

import com.nursulton.giphytask.domain.model.RecentSearch
import com.nursulton.giphytask.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentSearchesUseCase @Inject constructor(
    private val repository: RecentSearchRepository
) {
    operator fun invoke(): Flow<List<RecentSearch>> {
        return repository.getRecentSearches()
    }
}
