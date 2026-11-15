package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aether Launcher", appName)
    }

    @Test
    fun `home renders super island dock and spotlight math works`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("super_island_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_search_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_dock").assertIsDisplayed()

        // Open Super Island
        composeTestRule.onNodeWithTag("super_island_pill").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("island_timer_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("super_island_close").performClick()
        composeTestRule.waitForIdle()

        // Open Spotlight Search and evaluate math inline
        composeTestRule.onNodeWithTag("home_search_pill").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("spotlight_input").performTextInput("25*4")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("spotlight_math_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("= 100").assertIsDisplayed()
    }

    @Test
    fun `control center opens and closes cleanly`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_control_center_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("control_center_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cc_torch_tile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("control_center_close").performClick()
        composeTestRule.waitForIdle()
    }
}
