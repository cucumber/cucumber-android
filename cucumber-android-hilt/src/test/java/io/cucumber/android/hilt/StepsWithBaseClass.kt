package io.cucumber.android.hilt

import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject

abstract class BaseSteps {

    @Inject
    lateinit var someCucumberHook: SomeCucumberHook

    @Inject
    lateinit var someDependencies: SomeDependencies
}

@HiltAndroidTest
class StepsWithBaseClass:BaseSteps() {
    @Inject
    lateinit var someSingletonDependency: SomeSingletonDependency
}