package cucumber.api.android;

import android.os.Bundle;
import androidx.test.runner.AndroidJUnitRunner;
import cucumber.runtime.android.CucumberJUnitRunnerBuilder;

/**
 * Android Orchestrator compatible replacement for {@link cucumber.api.android.CucumberInstrumentation}.
 */
public class CucumberAndroidJUnitRunner extends AndroidJUnitRunner {

    private static final String ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER = "runnerBuilder";

    private Bundle arguments;

    @Override
    public void onCreate(final Bundle bundle) {
        bundle.putString(ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER, CucumberJUnitRunnerBuilder.class.getName());
        //there is no need to scan all classes - we can fake this execution to be for single class
	    //because we delegate test execution to CucumberJUnitRunner
        bundle.putString("class", CucumberJUnitRunnerBuilder.class.getName());
        arguments = bundle;

        super.onCreate(bundle);
    }

    public Bundle getArguments() {
        return arguments;
    }
}
