package io.cucumber.android.runner;

import android.os.Bundle;

import androidx.test.runner.AndroidJUnitRunner;

import io.cucumber.android.CucumberAndroidJUnitArguments;
import io.cucumber.android.CucumberArgumentsProvider;

/**
 * {@link AndroidJUnitRunner} for cucumber tests. It supports running tests from Android Tests Orchestrator
 */
public class CucumberAndroidJUnitRunner extends AndroidJUnitRunner  {

    @Override
    public void onCreate(Bundle bundle) {
        CucumberAndroidJUnitArguments cucumberAndroidJUnitArguments = new CucumberAndroidJUnitArguments(bundle);
        CucumberArgumentsProvider.INSTANCE.setArguments(cucumberAndroidJUnitArguments);
        super.onCreate(cucumberAndroidJUnitArguments.processArgs());
    }
}
