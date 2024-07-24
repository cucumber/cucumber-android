package cucumber.cukeulator.test;

import android.app.Application;
import android.content.Context;

import dagger.hilt.android.testing.HiltTestApplication;
import io.cucumber.android.runner.CucumberAndroidJUnitRunner;
import io.cucumber.junit.CucumberOptions;

/**
 * The CucumberOptions annotation is mandatory for exactly one of the classes in the test project.
 * Only the first annotated class that is found will be used, others are ignored. If no class is
 * annotated, an exception is thrown. This annotation does not have to placed in runner class
 */
@CucumberOptions(
        features = "features"
//        ,useFileNameCompatibleName = true
)
public class CukeulatorAndroidJUnitRunner extends CucumberAndroidJUnitRunner {

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return super.newApplication(cl, HiltTestApplication.class.getName(), context);
    }
}
