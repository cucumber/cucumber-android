package cucumber.cukeulator.test;

import android.os.Bundle;

import java.io.File;

import cucumber.api.CucumberOptions;
import cucumber.api.android.CucumberAndroidJUnitRunner;

/**
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown. This annotation does not have to placed in runner class
*/
@CucumberOptions(
        features = "features"
)
public class CukeulatorAndroidJUnitRunner extends CucumberAndroidJUnitRunner {


    @Override
    public void onCreate(final Bundle bundle) {
        bundle.putString("plugin", getPluginConfigurationString()); // we programmatically create the plugin configuration

        if(bundle.containsKey("class")){
            bundle.putBoolean(CUCUMBER_ANDROID_USE_ANDROID_RUNNER, true);
        }

        super.onCreate(bundle);
    }

    /**
     * Since we want to checkout the external storage directory programmatically, we create the plugin configuration
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
        return new File(directory,"reports").getAbsolutePath();
    }
}
