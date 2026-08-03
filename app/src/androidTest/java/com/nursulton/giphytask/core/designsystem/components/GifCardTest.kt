package com.nursulton.giphytask.core.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.nursulton.giphytask.core.designsystem.theme.GiphyTaskTheme
import com.nursulton.giphytask.domain.model.Gif
import com.nursulton.giphytask.domain.model.GifImages
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GifCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gifCard_displaysTitleAndTriggersClick() {
        var clicked = false
        val fakeGif = Gif(
            id = "1",
            title = "Test Dancing Cat",
            username = "test_user",
            userDisplayName = "Test Studio",
            userAvatarUrl = null,
            isUserVerified = false,
            rating = "G",
            importDate = "2024-01-01",
            trendingDate = null,
            sourceUrl = "",
            webUrl = "",
            images = GifImages("", 200, 200, "", 100, 100, "")
        )

        composeTestRule.setContent {
            GiphyTaskTheme {
                GifCard(
                    gif = fakeGif,
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("GIF titled Test Dancing Cat by Test Studio")
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked)
    }
}
