package com.nursulton.giphytask.data.mapper

import com.nursulton.giphytask.data.remote.dto.GifDto
import com.nursulton.giphytask.data.remote.dto.ImageVariantDto
import com.nursulton.giphytask.data.remote.dto.ImagesDto
import com.nursulton.giphytask.data.remote.dto.UserDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GifMapperTest {

    @Test
    fun `toDomain maps full GifDto correctly`() {
        val dto = GifDto(
            id = "test_123",
            title = "Dancing Cat",
            username = "cat_creator",
            rating = "pg-13",
            importDatetime = "2024-01-01 12:00:00",
            trendingDatetime = "2024-01-02 12:00:00",
            source = "https://source.com",
            url = "https://giphy.com/gifs/123",
            images = ImagesDto(
                original = ImageVariantDto(url = "https://media.giphy.com/original.gif", width = "500", height = "500"),
                fixedHeight = ImageVariantDto(url = "https://media.giphy.com/fixed.gif", width = "200", height = "200")
            ),
            user = UserDto(
                username = "cat_creator",
                displayName = "Cat Creator Studio",
                avatarUrl = "https://avatar.com/cat.jpg",
                isVerified = true
            )
        )

        val domain = dto.toDomain()

        assertEquals("test_123", domain.id)
        assertEquals("Dancing Cat", domain.title)
        assertEquals("cat_creator", domain.username)
        assertEquals("Cat Creator Studio", domain.userDisplayName)
        assertEquals("PG-13", domain.rating)
        assertEquals("https://media.giphy.com/original.gif", domain.images.originalUrl)
        assertEquals(500, domain.images.originalWidth)
        assertEquals(true, domain.isUserVerified)
        assertEquals("2024-01-02 12:00:00", domain.trendingDate)
    }

    @Test
    fun `toDomain handles null and zero trending date correctly`() {
        val dto = GifDto(
            id = "test_456",
            trendingDatetime = "0000-00-00 00:00:00"
        )

        val domain = dto.toDomain()

        assertNull(domain.trendingDate)
        assertEquals("Anonymous Creator", domain.userDisplayName)
        assertEquals("G", domain.rating)
    }
}
