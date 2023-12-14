package cucumber.cukeulator.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import cucumber.cukeulator.ComposeTestActivity
import cucumber.cukeulator.R
import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert

@HiltAndroidTest
class KotlinSteps(
    val scenarioHolder: ActivityScenarioHolder
): BaseKotlinSteps() {



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

    @Then("^greeting service returns \"([^\"]*)\"$")
    fun greetingServiceReturns(arg0: String) {
        Assert.assertEquals(arg0,greetingService.greeting(ApplicationProvider.getApplicationContext()))
    }
}