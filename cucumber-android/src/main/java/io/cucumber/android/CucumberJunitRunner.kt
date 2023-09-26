package io.cucumber.android

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import io.cucumber.core.eventbus.EventBus
import io.cucumber.core.exception.CucumberException
import io.cucumber.core.feature.FeatureParser
import io.cucumber.core.filter.Filters
import io.cucumber.core.options.CucumberOptionsAnnotationParser
import io.cucumber.core.options.CucumberProperties
import io.cucumber.core.options.CucumberPropertiesParser
import io.cucumber.core.plugin.PluginFactory
import io.cucumber.core.plugin.Plugins
import io.cucumber.core.resource.ClassLoaders
import io.cucumber.core.runtime.BackendSupplier
import io.cucumber.core.runtime.CucumberAndroidExecutionContext
import io.cucumber.core.runtime.ExitStatus
import io.cucumber.core.runtime.ObjectFactoryServiceLoader
import io.cucumber.core.runtime.SynchronizedEventBus
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier
import io.cucumber.core.runtime.TimeServiceEventBus
import io.cucumber.junit.AndroidFeatureRunner
import io.cucumber.junit.CucumberJunitSupport
import io.cucumber.junit.CucumberOptions
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import java.time.Clock
import java.util.UUID

internal class CucumberJunitRunner(testClass: Class<*>) : ParentRunner<AndroidFeatureRunner>(testClass) {
    private var children = emptyList<AndroidFeatureRunner>()
    private val bus: EventBus
    private val plugins: Plugins
    private val executionContext: CucumberAndroidExecutionContext

    init {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.context
        val arguments = CucumberArgumentsProvider.arguments

        val testClassesScanner = TestClassesScanner(instrumentation)
        val cucumberOptionsClass = getCucumberOptionsClass(testClassesScanner, arguments, context)

        // Parse the options early to provide fast feedback about invalid
        // options
        val propertiesFileOptions = CucumberPropertiesParser()
            .parse(CucumberProperties.fromPropertiesFile())
            .build()
        val annotationOptions = CucumberOptionsAnnotationParser()
            .withOptionsProvider(CucumberJunitSupport.jUnitCucumberOptionsProvider())
            .parse(cucumberOptionsClass)
            .build(propertiesFileOptions)
        val environmentOptions = CucumberPropertiesParser()
            .parse(CucumberProperties.fromEnvironment())
            .build(annotationOptions)
        val systemOptions = CucumberPropertiesParser()
            .parse(CucumberProperties.fromSystemProperties())
            .enablePublishPlugin()
            .build(environmentOptions)

        val runtimeOptions = CucumberPropertiesParser()
            .parse(arguments.cucumberOptions)
            .enablePublishPlugin()
            .build(systemOptions)

        bus = SynchronizedEventBus.synchronize(TimeServiceEventBus(Clock.systemUTC()) { UUID.randomUUID() })

        // Parse the features early. Don't proceed when there are lexer errors
        val parser = FeatureParser { bus.generateId() }
        val featureSupplier = AndroidFeatureSupplier(runtimeOptions, parser, context)
        val features = featureSupplier.get()

        if (features.isEmpty()) {
            Log.e(TAG, "No features found")
        }

        // Create plugins after feature parsing to avoid the creation of empty
        // files on lexer errors.
        plugins = Plugins(PluginFactory(), runtimeOptions)
        val exitStatus = ExitStatus(runtimeOptions)
        plugins.addPlugin(exitStatus)
        plugins.addPlugin(AndroidLogcatReporter(TAG))
        val objectFactoryServiceLoader = ObjectFactoryServiceLoader(
            ClassLoaders::getDefaultClassLoader,
            runtimeOptions
        )
        val objectFactorySupplier = ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader)
        val rulesBackend = RulesBackend(objectFactorySupplier)
        val backendSupplier = BackendSupplier {
            val objectFactory = objectFactorySupplier.get()
            listOf(AndroidBackend(objectFactory, objectFactory, testClassesScanner, rulesBackend))
        }
        val runnerSupplier = ThreadLocalRunnerSupplier(
            runtimeOptions, bus, backendSupplier,
            objectFactorySupplier
        )
        executionContext = CucumberAndroidExecutionContext(bus, exitStatus, runnerSupplier)
        val filters = Filters(runtimeOptions)
        val testClassNameFromRunner = arguments.classArgument
        var featureFilter = { _: String -> true }
        var scenarioFilter = { _: String, _:String -> true }
        if (testClassNameFromRunner != null) {
            Log.i(TAG, "${CucumberAndroidJUnitArguments.AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_CLASS}=$testClassNameFromRunner")

            val allFeaturesAndScenarios = getFeaturesAndScenariosNamesFromClassArgument(testClassNameFromRunner)

            if (allFeaturesAndScenarios.isNotEmpty()) {
                featureFilter = { allFeaturesAndScenarios.containsKey(it) }
                scenarioFilter =  { feature,scenario -> allFeaturesAndScenarios[feature]?.let {
                    it.isEmpty() || it.contains(scenario)
                }?:false }
            } else {
                Log.e(TAG, "CucumberJUnitRunner: invalid argument ${CucumberAndroidJUnitArguments.AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_CLASS}=$testClassNameFromRunner")
            }
        }
        children = CucumberJunitSupport.createChildren(
            features = features,
            featureFilter = featureFilter,
            cucumberOptionsClass = cucumberOptionsClass,
            rulesBackend = rulesBackend,
            scenarioFilter = scenarioFilter,
            pickleFilter = filters,
            executionContext = executionContext
        )
    }

    private fun getFeaturesAndScenariosNamesFromClassArgument(testClassNameFromRunner: String): Map<String, List<String>> {
        //it can contain many classes and methods names separated by comma
        //as described in androidx.test.internal.runner.ClassPathScanner
        val allClassesAndMethods = testClassNameFromRunner.split(',').filter { it.isNotEmpty() }

        return allClassesAndMethods.mapNotNull {
            it.split('#').takeIf { it.size in 1..2 }?.let { featureAndScenario ->
                featureAndScenario[0] to featureAndScenario.getOrNull(1)
            }
        }.groupBy(keySelector = {it.first}, valueTransform = {it.second}).mapValues { it.value.filterNotNull() }
    }

    private fun getCucumberOptionsClass(testClassesScanner: TestClassesScanner, arguments: CucumberAndroidJUnitArguments, context: Context): Class<*> {
        val packageName = arguments.optionsAnnotationPackageLocation ?: context.packageName
        return testClassesScanner.getClassesFromRootPackages {
            val classPackage = it.substring(0, it.lastIndexOf('.'))
            classPackage == packageName
        }.firstOrNull { it.isAnnotationPresent(CucumberOptions::class.java) } ?: throw CucumberException("No CucumberOptions annotated class present in package $packageName")
    }

    public override fun getChildren(): List<AndroidFeatureRunner> {
        return children
    }

    override fun describeChild(child: AndroidFeatureRunner): Description {
        return child.description
    }

    override fun runChild(child: AndroidFeatureRunner, notifier: RunNotifier) {
        child.run(notifier)
    }

    override fun childrenInvoker(notifier: RunNotifier): Statement {
        var statement = super.childrenInvoker(notifier)
        statement = StartAndFinishTestRun(statement)
        return statement
    }

    private inner class StartAndFinishTestRun(private val next: Statement) : Statement() {
        override fun evaluate() {
            plugins.setEventBusOnEventListenerPlugins(bus)
            executionContext.runFeatures { next.evaluate() }
        }
    }

    companion object {
        /**
         * The logcat tag to log all cucumber related information to.
         */
        const val TAG = "cucumber-android"
    }
}