package io.cucumber.android.hilt

import dagger.hilt.android.testing.HiltAndroidTest
import io.cucumber.java.en.Given
import javax.inject.Inject

@HiltAndroidTest
class SomeSteps {

    @Inject
    lateinit var someSingletonDependency: SomeSingletonDependency

    @Inject
    lateinit var someDependency: SomeDependency


    @Given("Something")
    fun doSomething() {
        someDependency.doSomething()
        someSingletonDependency.doSomething()
    }

}