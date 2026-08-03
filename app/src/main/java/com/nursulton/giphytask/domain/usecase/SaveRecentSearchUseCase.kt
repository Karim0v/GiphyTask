package com.nursulton.giphytask.domain.usecase

import com.nursulton.giphytask.domain.repository.RecentSearchRepository
import javax.inject.Inject

class SaveRecentSearchUseCase @Inject constructor(
    private val repository: RecentSearchRepository
) {
    suspend operator fun invoke(query: String) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            repository.saveRecentSearch(trimmed)
        }
    }
}
