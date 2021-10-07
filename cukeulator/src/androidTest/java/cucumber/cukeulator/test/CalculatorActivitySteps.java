package cucumber.cukeulator.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import cucumber.cukeulator.CalculatorActivity;
import cucumber.cukeulator.R;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.junit.CucumberJUnitRunner;

/**
 * We use {@link ActivityScenario} in order to have access to methods like getActivity
 * and getInstrumentation.
 * </p>
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown.
 * <p/>
 * The options need to at least specify features = "features". Features must be placed inside
 * assets/features/ of the test project (or a subdirectory thereof).
 */
public class CalculatorActivitySteps {

    /**
     * Since {@link CucumberJUnitRunner} has the control over the
     * test lifecycle, activity test rules must not be launched automatically. Automatic launching of test rules is only
     * feasible for JUnit tests. Fortunately, we are able to launch the activity in Cucumber's {@link Before} method.
     */
    private ActivityScenarioHolder scenario;
    private CalculatorActivity calculatorActivity;

    public CalculatorActivitySteps(SomeDependency dependency,ActivityScenarioHolder scenario) {
        assertNotNull(dependency);
        this.scenario = scenario;
    }


    @Given("I have a CalculatorActivity")
    public void I_have_a_CalculatorActivity() {
       scenario.launch(new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),CalculatorActivity.class));
    }

    @When("I press {digit}")
    public void I_press_d(final int d) {
        switch (d) {
            case 0:
                performClick(R.id.btn_d_0);
                break;
            case 1:
                performClick(R.id.btn_d_1);
                break;
            case 2:
                performClick(R.id.btn_d_2);
                break;
            case 3:
                performClick(R.id.btn_d_3);
                break;
            case 4:
                performClick(R.id.btn_d_4);
                break;
            case 5:
                performClick(R.id.btn_d_5);
                break;
            case 6:
                performClick(R.id.btn_d_6);
                break;
            case 7:
                performClick(R.id.btn_d_7);
                break;
            case 8:
                performClick(R.id.btn_d_8);
                break;
            case 9:
                performClick(R.id.btn_d_9);
                break;
        }
    }

    @When("I press {operator}")
    public void I_press_op(final char op) {
        switch (op) {
            case '+':
                performClick(R.id.btn_op_add);
                break;
            case '–':
                performClick(R.id.btn_op_subtract);
                break;
            case 'x':
                performClick(R.id.btn_op_multiply);
                break;
            case '/':
                performClick(R.id.btn_op_divide);
                break;
            case '=':
                performClick(R.id.btn_op_equals);
                break;
        }
    }
    
    private void performClick(int id) {
        onView(withId(id)).perform(click());
    }

}
