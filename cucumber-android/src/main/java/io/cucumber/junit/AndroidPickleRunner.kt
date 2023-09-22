package io.cucumber.junit

import io.cucumber.android.RulesBackend
import io.cucumber.core.gherkin.Pickle
import io.cucumber.core.runtime.CucumberAndroidExecutionContext
import io.cucumber.junit.PickleRunners.PickleId
import io.cucumber.junit.PickleRunners.PickleRunner
import io.cucumber.plugin.event.Step
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier

internal class AndroidPickleRunner(
    private val pickle: Pickle,
    private val name: String,
    private val featureName: String,
    private val rulesBackend: RulesBackend,
    private val executionContext: CucumberAndroidExecutionContext,
    private val jUnitOptions: JUnitOptions,
) : PickleRunner {

    private val _description by lazy {
        Description.createTestDescription(featureName, name, PickleId(pickle))
    }

    override fun run(notifier: RunNotifier) {
        executionContext.runTestCase{ runner ->
            val jUnitReporter = JUnitReporter(runner.bus, jUnitOptions)
            jUnitReporter.startExecutionUnit(this, notifier)
            rulesBackend.setDescription(_description, pickle.tags)
            runner.runPickle(pickle)
            jUnitReporter.finishExecutionUnit()
        }
    }

    override fun getDescription(): Description = _description

    override fun describeChild(step: Step?): Description {
        throw UnsupportedOperationException("This pickle runner does not wish to describe its children")
    }
}