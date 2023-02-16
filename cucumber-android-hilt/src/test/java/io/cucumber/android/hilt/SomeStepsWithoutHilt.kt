package io.cucumber.android.hilt

import io.cucumber.java.en.Given

class SomeStepsWithoutHiltAndDependencies {


    @Given("Something")
    fun doSomething() {

    }

}

class SomeStepsWithoutHilt(val someStepsWithoutHiltAndDependencies: SomeStepsWithoutHiltAndDependencies) {


    @Given("Something")
    fun doSomething() {

    }

}