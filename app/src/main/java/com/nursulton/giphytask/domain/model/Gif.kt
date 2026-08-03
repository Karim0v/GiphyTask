package com.nursulton.giphytask.domain.model

data class Gif(
    val id: String,
    val title: String,
    val username: String,
    val userDisplayName: String,
    val userAvatarUrl: String?,
    val isUserVerified: Boolean,
    val rating: String,
    val importDate: String,
    val trendingDate: String?,
    val sourceUrl: String,
    val webUrl: String,
    val images: GifImages
)

data class GifImages(
    val originalUrl: String,
    val originalWidth: Int,
    val originalHeight: Int,
    val previewUrl: String,
    val previewWidth: Int,
    val previewHeight: Int,
    val downsizedUrl: String
)

data class RecentSearch(
    val query: String,
    val timestamp: Long
)
