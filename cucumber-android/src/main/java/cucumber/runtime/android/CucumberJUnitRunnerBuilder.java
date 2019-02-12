package cucumber.runtime.android;

import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

public class CucumberJUnitRunnerBuilder extends RunnerBuilder {
    @Override
    public Runner runnerForClass(final Class<?> testClass) {
        if (testClass.equals(this.getClass())) {
            return new CucumberJUnitRunner(testClass);
        }

        return null;
    }
}
