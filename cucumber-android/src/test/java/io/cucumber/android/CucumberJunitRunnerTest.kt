package io.cucumber.android

import android.os.Bundle
import io.cucumber.android.runner.CucumberAndroidJUnitRunner
import io.cucumber.android.shadows.ShadowDexFile
import io.cucumber.junit.CucumberOptions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Filter
import org.robolectric.RobolectricTestRunner
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
class CucumberJunitRunnerTest {

    private val arguments = Bundle()

    @Before
    fun setUp() {
        addClassToDex()
        arguments.putString(CucumberAndroidJUnitArguments.PublicArgs.OPTIONS_ANNOTATION_LOCATION,"io.cucumber.android")
    }

    private fun addClassToDex() {
        ShadowDexFile.setEntries(
            listOf(
                CucumberOptionsClass::class.qualifiedName!!
            )
        )
    }

    private fun setArguments(function: Bundle.() -> Unit) {
        arguments.apply(function)
        CucumberArgumentsProvider.arguments = CucumberAndroidJUnitArguments(arguments).also { it.processArgs() }
    }

    @Test
    fun `description test count is correct when tests are filtered`() {

        setArguments { }
        val numShards = 2
        val shardIndex = 0
        val runner = createCucumberJunitRunner()
        runner.filter(object :Filter() {
            override fun shouldRun(description: Description): Boolean {
                return if (description.isTest) {
                    abs(description.hashCode()) % numShards == shardIndex
                } else true
            }

            override fun describe(): String  = "sharding"
        })
        assertEquals(2,runner.testCount())
        val allTests = runner.children.flatMap { it.description.children }.map { it.displayName }
        assertEquals(listOf(
            "Scenario Outline 1 1(Feature 1)",
            "Scenario Outline 1 2(Feature 2)",
        ),allTests)
    }

    @Test
    fun `single scenario is executed if specified as class and method name`() {
        setArguments {
            putString("class","Feature 2#Scenario Outline 1 2")
        }
        val runner = createCucumberJunitRunner()

        assertEquals(1,runner.testCount())
        val scenario = getSingleDescription(runner)
        assertEquals("Scenario Outline 1 2(Feature 2)",scenario.displayName)
    }

    @Test
    fun `single scenario is executed if specified as feature path with line`() {
        setArguments {
            putString(CucumberAndroidJUnitArguments.PublicArgs.FEATURES,"assets:features/feature2.feature:11")
        }
        val runner = createCucumberJunitRunner()

        assertEquals(1,runner.testCount())
        val scenario = getSingleDescription(runner)
        assertEquals("Scenario Outline 1 2(Feature 2)",scenario.displayName)
    }

    private fun createCucumberJunitRunner() = CucumberJunitRunner(CucumberAndroidJUnitRunner::class.java)

    private fun getSingleDescription(runner: CucumberJunitRunner): Description = runner.children.single().description.children.single()
}


@CucumberOptions(
    features = ["assets:features"],
)
class CucumberOptionsClass