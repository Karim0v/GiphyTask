package com.nursulton.giphytask.data.mapper

import com.nursulton.giphytask.data.remote.dto.GifDto
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.model.GifImages

private const val FALLBACK_DIMENSION = 480
private const val PLACEHOLDER_TRENDING_DATE = "0000-00-00"

fun GifDto.toDomain(): Gif {
    val originalImg = images?.original
    val fixedHeightImg = images?.fixedHeight
    val fixedWidthImg = images?.fixedWidth
    val downsizedImg = images?.downsizedMedium

    // Fall back through the variants Giphy actually returns rather than to a fixed 480x480,
    // which would otherwise distort the aspect ratio of every card missing `original`.
    val fallbackImg = fixedHeightImg ?: fixedWidthImg
    val originalUrl = originalImg?.url.orNullIfBlank() ?: fallbackImg?.url.orEmpty()
    val originalWidth = originalImg?.width?.toIntOrNull()
        ?: fallbackImg?.width?.toIntOrNull()
        ?: FALLBACK_DIMENSION
    val originalHeight = originalImg?.height?.toIntOrNull()
        ?: fallbackImg?.height?.toIntOrNull()
        ?: FALLBACK_DIMENSION

    val previewUrl = fixedHeightImg?.url.orNullIfBlank()
        ?: fixedWidthImg?.url.orNullIfBlank()
        ?: originalUrl
    val previewWidth = fixedHeightImg?.width?.toIntOrNull() ?: originalWidth
    val previewHeight = fixedHeightImg?.height?.toIntOrNull() ?: originalHeight

    val downsizedUrl = downsizedImg?.url.orNullIfBlank() ?: previewUrl

    // Giphy sends "0000-00-00 00:00:00" instead of null when a GIF never trended.
    val trending = trendingDatetime
    val formattedTrendingDate =
        if (trending.isNullOrBlank() || trending.startsWith(PLACEHOLDER_TRENDING_DATE)) {
            null
        } else {
            trending
        }

    val resolvedUsername = user?.username.orNullIfBlank()
        ?: username.orNullIfBlank()
        ?: "anonymous"
    val resolvedDisplayName = user?.displayName.orNullIfBlank()
        ?: user?.username.orNullIfBlank()
        ?: username.orNullIfBlank()
        ?: "Anonymous Creator"

    return Gif(
        id = id,
        title = title.orNullIfBlank() ?: "Untitled GIF",
        username = resolvedUsername,
        userDisplayName = resolvedDisplayName,
        userAvatarUrl = user?.avatarUrl.orNullIfBlank(),
        isUserVerified = user?.isVerified ?: false,
        rating = rating.orNullIfBlank()?.uppercase() ?: "G",
        importDate = importDatetime.orNullIfBlank() ?: "Unknown date",
        trendingDate = formattedTrendingDate,
        sourceUrl = source.orNullIfBlank() ?: url.orEmpty(),
        webUrl = url.orEmpty(),
        images = GifImages(
            originalUrl = originalUrl,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            previewUrl = previewUrl,
            previewWidth = previewWidth,
            previewHeight = previewHeight,
            downsizedUrl = downsizedUrl
        )
    )
}

fun List<GifDto>.toDomainList(): List<Gif> = map { it.toDomain() }

/** Collapses both `null` and blank strings to `null` so `?:` chains skip empty API fields. */
private fun String?.orNullIfBlank(): String? = this?.takeIf { it.isNotBlank() }
