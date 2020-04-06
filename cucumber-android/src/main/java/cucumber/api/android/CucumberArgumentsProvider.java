package cucumber.api.android;

import androidx.annotation.NonNull;

import cucumber.runtime.android.CucumberAndroidJUnitArguments;

public interface CucumberArgumentsProvider {
    @NonNull
    CucumberAndroidJUnitArguments getArguments();
}
