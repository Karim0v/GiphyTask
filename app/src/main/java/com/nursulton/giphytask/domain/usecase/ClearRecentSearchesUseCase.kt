package com.nursulton.giphytask.domain.usecase

import com.nursulton.giphytask.domain.repository.RecentSearchRepository
import javax.inject.Inject

class ClearRecentSearchesUseCase @Inject constructor(
    private val repository: RecentSearchRepository
) {
    suspend operator fun invoke() {
        repository.clearRecentSearches()
    }
}
