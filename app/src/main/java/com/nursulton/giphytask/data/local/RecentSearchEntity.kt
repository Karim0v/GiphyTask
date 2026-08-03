package com.nursulton.giphytask.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nursulton.giphytask.domain.model.RecentSearch

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val timestamp: Long
)

fun RecentSearchEntity.toDomain(): RecentSearch = RecentSearch(query = query, timestamp = timestamp)
fun List<RecentSearchEntity>.toDomainList(): List<RecentSearch> = map { it.toDomain() }
