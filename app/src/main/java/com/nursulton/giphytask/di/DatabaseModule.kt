package com.nursulton.giphytask.di

import android.content.Context
import androidx.room.Room
import com.nursulton.giphytask.data.local.AppDatabase
import com.nursulton.giphytask.data.local.RecentSearchDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "giphy_task.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRecentSearchDao(
        database: AppDatabase
    ): RecentSearchDao {
        return database.recentSearchDao()
    }
}
