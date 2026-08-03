package com.nursulton.giphytask.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GiphyResponseDto(
    @SerialName("data") val data: List<GifDto> = emptyList(),
    @SerialName("pagination") val pagination: PaginationDto? = null,
    @SerialName("meta") val meta: MetaDto? = null
)

@Serializable
data class GiphySingleResponseDto(
    @SerialName("data") val data: GifDto? = null,
    @SerialName("meta") val meta: MetaDto? = null
)

@Serializable
data class GifDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("import_datetime") val importDatetime: String? = null,
    @SerialName("trending_datetime") val trendingDatetime: String? = null,
    @SerialName("source") val source: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("images") val images: ImagesDto? = null,
    @SerialName("user") val user: UserDto? = null
)

@Serializable
data class ImagesDto(
    @SerialName("original") val original: ImageVariantDto? = null,
    @SerialName("fixed_height") val fixedHeight: ImageVariantDto? = null,
    @SerialName("fixed_width") val fixedWidth: ImageVariantDto? = null,
    @SerialName("downsized_medium") val downsizedMedium: ImageVariantDto? = null
)

@Serializable
data class ImageVariantDto(
    @SerialName("url") val url: String? = null,
    @SerialName("width") val width: String? = null,
    @SerialName("height") val height: String? = null,
    @SerialName("size") val size: String? = null,
    @SerialName("mp4") val mp4: String? = null,
    @SerialName("webp") val webp: String? = null
)

@Serializable
data class UserDto(
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("banner_url") val bannerUrl: String? = null,
    @SerialName("profile_url") val profileUrl: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("is_verified") val isVerified: Boolean? = null
)

@Serializable
data class PaginationDto(
    @SerialName("total_count") val totalCount: Int? = null,
    @SerialName("count") val count: Int? = null,
    @SerialName("offset") val offset: Int? = null
)

@Serializable
data class MetaDto(
    @SerialName("status") val status: Int? = null,
    @SerialName("msg") val msg: String? = null,
    @SerialName("response_id") val responseId: String? = null
)
