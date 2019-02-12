package cucumber.api.android;

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import java.io.File;

import cucumber.api.CucumberOptions;
import cucumber.runtime.android.CucumberJUnitRunnerBuilder;

/**
 * Android Orchestrator compatible replacement for {@link cucumber.api.android.CucumberInstrumentation}.
 */
public class CucumberAndroidJUnitRunner extends AndroidJUnitRunner {

    private static final String ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER = "runnerBuilder";

    private Bundle arguments;

    @Override
    public void onCreate(final Bundle bundle) {
        bundle.putString("plugin", getPluginConfigurationString()); // we programmatically create the plugin configuration
        bundle.putString(ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER, CucumberJUnitRunnerBuilder.class.getName());
        arguments = bundle;

        super.onCreate(bundle);
    }

    /**
     * Since we want to checkout the external storage directory programmatically, we create the plugin configuration
     * here, instead of the {@link CucumberOptions} annotation.
     *
     * @return the plugin string for the configuration, which contains XML, HTML and JSON paths
     */
    private String getPluginConfigurationString() {
        final String cucumber = "cucumber";
        final String separator = "--";
        return
                "junit:" + getAbsoluteFilesPath() + "/" + cucumber + ".xml" + separator +
                "html:" + getAbsoluteFilesPath() + "/" + cucumber + ".html" + separator +
                "json:" + getAbsoluteFilesPath() + "/" + cucumber + ".json";
    }

    /**
     * The path which is used for the report files.
     *
     * @return the absolute path for the report files
     */
    private String getAbsoluteFilesPath() {
        //sdcard/Android/data/cucumber.cukeulator
        File directory = getTargetContext().getExternalFilesDir(null);
        return new File(directory,"reports").getAbsolutePath();
    }

    public Bundle getArguments() {
        return arguments;
    }
}
