package io.cucumber.junit

import io.cucumber.android.RulesBackend
import io.cucumber.core.gherkin.Feature
import io.cucumber.core.gherkin.Pickle
import io.cucumber.core.options.CucumberOptionsAnnotationParser
import io.cucumber.core.options.CucumberProperties
import io.cucumber.core.runtime.CucumberAndroidExecutionContext
import java.util.function.Predicate

internal object CucumberJunitSupport {

    fun jUnitCucumberOptionsProvider(): CucumberOptionsAnnotationParser.OptionsProvider = JUnitCucumberOptionsProvider()

    private fun createJunitOptions(cucumberOptionsClass: Class<*>): JUnitOptions {

        // Next parse the junit options
        val junitPropertiesFileOptions = JUnitOptionsParser()
            .parse(CucumberProperties.fromPropertiesFile())
            .build()

        val junitAnnotationOptions: JUnitOptions = JUnitOptionsParser()
            .parse(cucumberOptionsClass)
            .build(junitPropertiesFileOptions)

        val junitEnvironmentOptions = JUnitOptionsParser()
            .parse(CucumberProperties.fromEnvironment())
            .build(junitAnnotationOptions)

        return JUnitOptionsParser()
            .parse(CucumberProperties.fromSystemProperties())
            .build(junitEnvironmentOptions)
    }

    fun createChildren(
        features: List<Feature>,
        featureFilter: (String) -> Boolean,
        cucumberOptionsClass: Class<*>,
        rulesBackend: RulesBackend,
        scenarioFilter: (String) -> Boolean,
        pickleFilter: Predicate<Pickle>,
        executionContext: CucumberAndroidExecutionContext,
    ): List<AndroidFeatureRunner> {
        val groupedByName = features.groupBy { it.name }
        val junitOptions = createJunitOptions(cucumberOptionsClass)
        return features.mapNotNull { feature ->
            val featureName = createName(feature, junitOptions, groupedByName, { it.name }, { it.orElse("EMPTY_NAME") })

            if (!featureFilter(featureName)) return@mapNotNull null

            AndroidFeatureRunner(feature, featureName, scenarioFilter, pickleFilter, executionContext, junitOptions, rulesBackend).takeIf { !it.isEmpty}
        }
    }


    fun <K, V> createName(obj: V, options: JUnitOptions, groupedByName: Map<K, List<V>>, key: (V) -> K, name: (K) -> String): String {
        val uniqueSuffix = FileNameCompatibleNames.uniqueSuffix(groupedByName, obj) { key(it) }
            ?.let { " $it" }.orEmpty()
        val originalName = obj.let(key).let(name)
        return FileNameCompatibleNames.createName("${originalName}${uniqueSuffix}", options.filenameCompatibleNames())
    }
}
