package cucumber.api.android;

import android.os.Bundle;
import androidx.test.runner.AndroidJUnitRunner;
import cucumber.runtime.android.CucumberJUnitRunnerBuilder;

/**
 * {@link AndroidJUnitRunner} for cucumber tests. It supports running tests from Android Tests Orchestrator
 * <p>
 * Runner argument are linked to the {@link androidx.test.internal.runner.RunnerArgs}
 */
public class CucumberAndroidJUnitRunner extends AndroidJUnitRunner {

    public static final String CUCUMBER_ANDROID_TEST_CLASS = "cucumberAndroidTestClass";

    /**
     * User default android junit runner.
     */
    protected static final String CUCUMBER_ANDROID_USE_ANDROID_RUNNER = "useAndroidJUnitRunner";

    /**
     * {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_RUNNER_BUILDER}
     */
    private static final String ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER = "runnerBuilder";

    /**
     * {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_TEST_CLASS}
     */
    private static final String ARGUMENT_ORCHESTRATOR_CLASS = "class";
    private Bundle arguments;

    @Override
    public void onCreate(final Bundle bundle) {
        if (Boolean.TRUE.equals(bundle.getBoolean(CUCUMBER_ANDROID_USE_ANDROID_RUNNER))) {
            arguments = bundle;
            super.onCreate(bundle);
            return;
        }

        bundle.putString(ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER, CucumberJUnitRunnerBuilder.class.getName());

        String testClass = bundle.getString(ARGUMENT_ORCHESTRATOR_CLASS);
        if (testClass != null && !testClass.isEmpty()) {
            //if this runner is executed for single class (e.g. from orchestrator or spoon), we set
            //special option to let CucumberJUnitRunner handle this
            bundle.putString(CUCUMBER_ANDROID_TEST_CLASS, testClass);
        }
        //there is no need to scan all classes - we can fake this execution to be for single class
        //because we delegate test execution to CucumberJUnitRunner
        bundle.putString(ARGUMENT_ORCHESTRATOR_CLASS, CucumberJUnitRunnerBuilder.class.getName());

        arguments = bundle;

        super.onCreate(bundle);
    }

    public Bundle getArguments() {
        return arguments;
    }
}
