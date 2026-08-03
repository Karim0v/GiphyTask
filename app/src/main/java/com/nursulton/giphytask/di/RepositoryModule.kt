package com.nursulton.giphytask.di

import com.nursulton.giphytask.data.repository.GiphyRepositoryImpl
import com.nursulton.giphytask.data.repository.RecentSearchRepositoryImpl
import com.nursulton.giphytask.domain.repository.GiphyRepository
import com.nursulton.giphytask.domain.repository.RecentSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGiphyRepository(
        impl: GiphyRepositoryImpl
    ): GiphyRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchRepository(
        impl: RecentSearchRepositoryImpl
    ): RecentSearchRepository
}
