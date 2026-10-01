package io.cucumber.android

import io.cucumber.core.backend.Backend
import io.cucumber.core.backend.Container
import io.cucumber.core.backend.Glue
import io.cucumber.core.backend.Lookup
import io.cucumber.core.backend.Snippet
import io.cucumber.core.backend.discovery.GlueDiscoveryRequest
import io.cucumber.core.backend.discovery.UriGlueDiscoverySelector
import io.cucumber.java.GlueAdaptorWrapper
import io.cucumber.java.MethodScannerWrapper
import java.lang.reflect.Method

internal class AndroidBackend(
    private val lookup: Lookup,
    private val container: Container,
    private val testClassesScanner: TestClassesScanner,
    private val rulesBackend: RulesBackend
) : Backend {

    override fun loadGlue(glue: Glue, request: GlueDiscoveryRequest) {
        val glueAdaptor = GlueAdaptorWrapper(lookup, glue)

        val packages = request.getSelectorsByType(UriGlueDiscoverySelector::class.java)
            .stream()
            .map { it.uri().path.removePrefix("/").replace('/', '.') }
            .toList()

        testClassesScanner.getClassesFromRootPackages { fqn -> packages.any { fqn.startsWith(it) } }.forEach {

            MethodScannerWrapper.scan(it) { method: Method, annotation: Annotation ->
                container.addClass(method.declaringClass)
                glueAdaptor.addDefinition(method, annotation)
            }
            rulesBackend.scan(it)
        }
    }

    override fun buildWorld() {
        rulesBackend.buildWorld()
    }

    override fun disposeWorld() {
        rulesBackend.disposeWorld()
    }

    override fun getSnippet(): Snippet = KotlinSnippet()
}