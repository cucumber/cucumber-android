package io.cucumber.android.hilt

import javax.inject.Inject
import javax.inject.Singleton


class SomeDependencies @Inject constructor() {
    fun doSomething() {

    }
}


@Singleton
class SomeSingletonDependency @Inject constructor() {
    fun doSomething() {

    }
}

class SomeDependency @Inject constructor() {
    fun doSomething() {

    }
}