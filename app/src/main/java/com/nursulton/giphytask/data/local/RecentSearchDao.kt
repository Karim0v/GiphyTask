package com.nursulton.giphytask.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Note: `query` is backtick-quoted in the raw SQL below because it collides with a keyword in
 * some SQLite builds. Limits are literals rather than interpolated constants so the annotation
 * argument stays a plain compile-time string for the Room processor.
 */
@Dao
interface RecentSearchDao {

    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<RecentSearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE LOWER(`query`) = LOWER(:query)")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearSearches()

    /** Drops everything past the newest 10 rows so the history cannot grow unbounded. */
    @Query(
        "DELETE FROM recent_searches WHERE `query` NOT IN " +
            "(SELECT `query` FROM recent_searches ORDER BY timestamp DESC LIMIT 10)"
    )
    suspend fun trimHistory()
}
