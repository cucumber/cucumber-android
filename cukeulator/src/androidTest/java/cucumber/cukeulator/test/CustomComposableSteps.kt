package cucumber.cukeulator.test

import androidx.compose.material.Text
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

@HiltAndroidTest
class CustomComposableSteps(
    val composeRuleHolder: CustomComposableRuleHolder,
) : SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {


    @When("^I show custom composable$")
    fun iShowCustomComposable() {
        composeRuleHolder.composeRule.setContent {
            Text(text = "Custom composable")
        }
    }

    @Then("^custom \"([^\"]*)\" text is presented$")
    fun customTextIsPresented(arg0: String) {
        onNodeWithText(arg0).assertIsDisplayed()
    }
}
