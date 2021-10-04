package cucumber.cukeulator.test

import android.content.Intent
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import io.cucumber.java.en.Then
import cucumber.cukeulator.R
import org.picocontainer.annotations.Inject
import io.cucumber.java.en.When
import cucumber.api.PendingException
import cucumber.cukeulator.ComposeTestActivity


class KotlinSteps(val composeRuleHolder: ComposeRuleHolder, val scenarioHolder: ActivityScenarioHolder):SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

    @Then("I should see {string} on the display")
    fun I_should_see_s_on_the_display(s: String?) {
        Espresso.onView(withId(R.id.txt_calc_display)).check(ViewAssertions.matches(ViewMatchers.withText(s)))
    }

    @When("^I open compose activity$")
    fun iOpenComposeActivity() {
        iOpenComposeActivityWith(null)
    }

    @Then("^\"([^\"]*)\" text is presented$")
    fun textIsPresented(arg0: String) {
        onNodeWithText(arg0).assertIsDisplayed()
    }

    @When("^I open compose activity with \"([^\"]*)\"$")
    fun iOpenComposeActivityWith(arg0: String?) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        scenarioHolder.launch(ComposeTestActivity.create(instrumentation.targetContext,arg0))
    }
}