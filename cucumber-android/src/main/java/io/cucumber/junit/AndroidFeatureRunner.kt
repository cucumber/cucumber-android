package io.cucumber.junit

import android.annotation.SuppressLint
import io.cucumber.android.RulesBackend
import io.cucumber.core.exception.UnrecoverableExceptions
import io.cucumber.core.gherkin.Feature
import io.cucumber.core.gherkin.Pickle
import io.cucumber.core.runtime.CucumberAndroidExecutionContext
import io.cucumber.junit.PickleRunners.PickleRunner
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import java.util.function.Predicate

@SuppressLint("NewApi")
internal class AndroidFeatureRunner(
    private val feature: Feature,
    private val name: String,
    pickleRunnerFilter: (String) -> Boolean,
    pickleFilter: Predicate<Pickle>,
    private val executionContext: CucumberAndroidExecutionContext,
    private val options: JUnitOptions,
    private val rulesBackend: RulesBackend,
) : ParentRunner<PickleRunner>(null as Class<*>?) {
    private val children: List<PickleRunner>
    init {
        val groupedByName = feature.pickles.groupBy { it.name }
        val featureName = name
        children = feature.pickles.mapNotNull { pickle->
            if (!pickleFilter.test(pickle)) return@mapNotNull null

            val scenarioName = CucumberJunitSupport.createName(pickle, options,groupedByName, { it.name }, { it })

            if (!pickleRunnerFilter(scenarioName)) return@mapNotNull null

            AndroidPickleRunner(pickle, scenarioName, featureName, rulesBackend, executionContext, options)
        }

    }

    val isEmpty: Boolean
        get() = children.isEmpty()

    public override fun getName(): String {
        return this.name
    }

    override fun getChildren(): List<PickleRunner> = children

    override fun describeChild(child: PickleRunner): Description = child.description

    override fun run(notifier: RunNotifier) {
        executionContext.beforeFeature(feature)
        super.run(notifier)
    }

    override fun runChild(child: PickleRunner, notifier: RunNotifier) {
        notifier.fireTestStarted(describeChild(child))
        try {
            child.run(notifier)
        } catch (t: Throwable) {
            UnrecoverableExceptions.rethrowIfUnrecoverable(t)
            notifier.fireTestFailure(Failure(describeChild(child), t))
            notifier.pleaseStop()
        } finally {
            notifier.fireTestFinished(describeChild(child))
        }
    }
}