package studio.cardia

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class StudyFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun start(organ: String = "Heart") {
        if(compose.onAllNodesWithText("Begin exploring").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Begin exploring").performScrollTo().performClick()
        }
        compose.onAllNodesWithText(organ).onFirst().performClick()
    }
    @Test fun openStudioSwitchOrganAndReturn() {
        start()
        compose.onNodeWithText("Open 3D studio").performClick()
        compose.onNodeWithText("Heart studio").assertIsDisplayed()
        compose.onNodeWithContentDescription("Reset camera").performClick()
        compose.onNodeWithText("Kidneys & urinary tract").performClick()
        compose.onNodeWithText("Kidneys & urinary tract studio").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("cardia.").assertExists()
    }
    @Test fun completeFirstHeartLesson() {
        start()
        compose.onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasText("Four chambers. One rhythm."))
        compose.onNodeWithText("Four chambers. One rhythm.").performClick()
        repeat(3) {compose.onNodeWithText("Next chapter").performClick()}
        compose.onNodeWithText("Complete lesson").performClick()
        compose.onNodeWithText("LESSON COMPLETE").assertExists()
    }
    @Test fun kidneyStoneLessonShowsMarkers() {
        start("Kidneys & urinary tract")
        compose.onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(hasText("How a kidney stone forms"))
        compose.onNodeWithText("How a kidney stone forms").performClick()
        repeat(2) {compose.onNodeWithText("Next chapter").performClick()}
        compose.onNodeWithText("Randall's plaque stone").assertExists()
        repeat(6) {compose.onNodeWithText("Next chapter").performClick()}
        compose.onNodeWithText("Complete lesson").performClick()
        compose.onNodeWithText("LESSON COMPLETE").assertExists()
    }
}
