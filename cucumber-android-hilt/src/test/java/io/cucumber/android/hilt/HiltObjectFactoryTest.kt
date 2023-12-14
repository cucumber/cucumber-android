package io.cucumber.android.hilt

import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class HiltObjectFactoryTest {

    private var hiltObjectFactory = HiltObjectFactory()

    @Test
    fun `add class returns true`() {
        assertTrue(hiltObjectFactory.addClass(String::class.java))
    }

    @Test
    fun `injects into fields with proper scope`() {

        hiltObjectFactory.start()

        val someSteps = hiltObjectFactory.getInstance(SomeSteps::class.java)
        val someOtherSteps = hiltObjectFactory.getInstance(SomeOtherSteps::class.java)

        someSteps.doSomething()
        someOtherSteps.doSomething()

        assertSame(someOtherSteps.someSingletonDependency,someSteps.someSingletonDependency)
        assertNotSame(someOtherSteps.someDependency,someSteps.someDependency)

        hiltObjectFactory.stop()
    }

    @Test
    fun `returns the same instance of steps`() {

        hiltObjectFactory.start()

        val someSteps1 = hiltObjectFactory.getInstance(SomeSteps::class.java)
        val someSteps2 = hiltObjectFactory.getInstance(SomeSteps::class.java)


        assertSame(someSteps1,someSteps2)
        assertSame(someSteps1.someSingletonDependency,someSteps2.someSingletonDependency)
        assertSame(someSteps1.someDependency,someSteps2.someDependency)

        hiltObjectFactory.stop()
    }

    @Test
    fun `returns new instance for second scenario`() {

        hiltObjectFactory.start()

        val someSteps1 = hiltObjectFactory.getInstance(SomeSteps::class.java)

        hiltObjectFactory.stop()
        hiltObjectFactory.start()

        val someSteps2 = hiltObjectFactory.getInstance(SomeSteps::class.java)


        assertNotSame(someSteps1,someSteps2)
        assertNotSame(someSteps1.someSingletonDependency,someSteps2.someSingletonDependency)

        hiltObjectFactory.stop()

    }

    @Test
    fun `creates instance without hilt dependencies`() {
        hiltObjectFactory.start()

        val someSteps1 = hiltObjectFactory.getInstance(SomeStepsWithoutHilt::class.java)

        val someSteps2 = hiltObjectFactory.getInstance(SomeStepsWithoutHiltAndDependencies::class.java)


        assertSame(someSteps1.someStepsWithoutHiltAndDependencies,someSteps2)

        hiltObjectFactory.stop()

    }

    @Test
    fun `inject cucumber and hilt dependencies using field injection into base class`() {
        hiltObjectFactory.start()

        val someSteps1 = hiltObjectFactory.getInstance(StepsWithBaseClass::class.java)
        val hook = hiltObjectFactory.getInstance(SomeCucumberHook::class.java)


        assertNotNull(someSteps1.someDependencies)
        assertNotNull(someSteps1.someSingletonDependency)
        assertSame(someSteps1.someCucumberHook, hook)
    }

}