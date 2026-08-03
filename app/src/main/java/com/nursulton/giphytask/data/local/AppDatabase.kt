package com.nursulton.giphytask.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RecentSearchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
}
