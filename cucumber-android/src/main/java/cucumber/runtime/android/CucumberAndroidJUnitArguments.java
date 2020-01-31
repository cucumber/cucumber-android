package cucumber.runtime.android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.runner.AndroidJUnitRunner;

/**
 * This class is responsible for preparing bundle to{@link AndroidJUnitRunner}
 * for cucumber tests. It prepares bundle for running tests from Android Tests Orchestrator
 * <p>
 * Runner argument are linked to the {@link androidx.test.internal.runner.RunnerArgs}
 */
public class CucumberAndroidJUnitArguments {

    /**
     * External API argument keys.
     */
    public static class Args {

        /**
         * User default android junit runner. public interface
         */
        public static final String USE_DEFAULT_ANDROID_RUNNER = "useAndroidJUnitRunner";
    }

    /**
     * Cucumber internal use argument keys.
     */
    static class InternalCucumberAndroidArgs {

        static final String CUCUMBER_ANDROID_TEST_CLASS = "cucumberAndroidTestClass";
    }

    /**
     * Runner argument are linked to the {@link androidx.test.internal.runner.RunnerArgs}.
     */
    private static class AndroidJunitRunnerArgs {
        /**
         * {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_RUNNER_BUILDER}
         */
        private static final String ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER = "runnerBuilder";

        /**
         * {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_TEST_CLASS}
         */
        private static final String ARGUMENT_ORCHESTRATOR_CLASS = "class";
    }

    @NonNull
    private final Bundle originalArgs;

    @Nullable
    private Bundle processedArgs;

    public CucumberAndroidJUnitArguments(@NonNull Bundle arguments) {
        this.originalArgs = (Bundle) arguments.clone();
    }

    public Bundle processArgs() {
        processedArgs = (Bundle) originalArgs.clone();

        if (Boolean.TRUE.equals(originalArgs.getBoolean(Args.USE_DEFAULT_ANDROID_RUNNER))) {
            return processedArgs;
        }

        processedArgs.putString(AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER, CucumberJUnitRunnerBuilder.class.getName());

        String testClass = originalArgs.getString(AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_CLASS);
        if (testClass != null && !testClass.isEmpty()) {

            //if this runner is executed for single class (e.g. from orchestrator or spoon), we set
            //special option to let CucumberJUnitRunner handle this
            processedArgs.putString(InternalCucumberAndroidArgs.CUCUMBER_ANDROID_TEST_CLASS, testClass);
        }

        //there is no need to scan all classes - we can fake this execution to be for single class
        //because we delegate test execution to CucumberJUnitRunner
        processedArgs.putString(AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_CLASS, CucumberJUnitRunnerBuilder.class.getName());

        return processedArgs;
    }

    @NonNull
    Bundle getRunnerArgs() {
        if (processedArgs == null) {
            throw new IllegalStateException("Arguments must be processed first");
        }
        return processedArgs;
    }
}
