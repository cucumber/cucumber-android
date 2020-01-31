package cucumber.cukeulator.test;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.test.runner.AndroidJUnitRunner;

import java.io.File;

import cucumber.api.CucumberOptions;
import cucumber.api.android.CucumberArgumentsProvider;
import cucumber.runtime.android.CucumberAndroidJUnitArguments;

/**
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown. This annotation does not have to placed in runner class.
 * <p>
 * It is possible to write your own configuration as in the example or to extend {@link cucumber.api.android.CucumberAndroidJUnitRunner}.
 */
@CucumberOptions(
        features = "features"
)
public class CukeulatorAndroidJUnitRunner extends AndroidJUnitRunner implements CucumberArgumentsProvider {

    private CucumberAndroidJUnitArguments cucumberAndroidJUnitArguments;

    @Override
    public void onCreate(final Bundle bundle) {
        bundle.putString("plugin", getPluginConfigurationString()); // we programmatically processArgs the plugin configuration

        if (bundle.containsKey("class")) {
            bundle.putBoolean(CucumberAndroidJUnitArguments.Args.USE_DEFAULT_ANDROID_RUNNER, true);
        }

        cucumberAndroidJUnitArguments = new CucumberAndroidJUnitArguments(bundle);
        super.onCreate(cucumberAndroidJUnitArguments.processArgs());
    }


    /**
     * Since we want to checkout the external storage directory programmatically, we processArgs the plugin configuration
     * here, instead of the {@link CucumberOptions} annotation.
     *
     * @return the plugin string for the configuration, which contains XML, HTML and JSON paths
     */
    private String getPluginConfigurationString() {
        String cucumber = "cucumber";
        String separator = "--";
        return "junit:" + getAbsoluteFilesPath() + "/" + cucumber + ".xml" + separator +
                "html:" + getAbsoluteFilesPath() + "/" + cucumber + ".html";
    }

    /**
     * The path which is used for the report files.
     *
     * @return the absolute path for the report files
     */
    private String getAbsoluteFilesPath() {

        //sdcard/Android/data/cucumber.cukeulator
        File directory = getTargetContext().getExternalFilesDir(null);
        return new File(directory, "reports").getAbsolutePath();
    }

    @NonNull
    @Override
    public CucumberAndroidJUnitArguments getArguments() {
        return cucumberAndroidJUnitArguments;
    }
}
