package com.nursulton.giphytask.data.remote.api

import com.nursulton.giphytask.data.remote.dto.GiphyResponseDto
import com.nursulton.giphytask.data.remote.dto.GiphySingleResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GiphyApi {

    @GET("gifs/search")
    suspend fun searchGifs(
        @Query("q") query: String,
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String? = null
    ): GiphyResponseDto

    @GET("gifs/trending")
    suspend fun getTrendingGifs(
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String? = null
    ): GiphyResponseDto

    @GET("gifs/{gif_id}")
    suspend fun getGifDetails(
        @Path("gif_id") gifId: String
    ): GiphySingleResponseDto

    companion object {
        const val BASE_URL = "https://api.giphy.com/v1/"
        const val DEFAULT_LIMIT = 25
    }
}
