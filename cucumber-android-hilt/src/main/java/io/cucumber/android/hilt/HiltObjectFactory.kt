package io.cucumber.android.hilt

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.internal.testing.HiltExposer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.internal.GeneratedComponentManager
import io.cucumber.core.backend.ObjectFactory
import io.cucumber.junit.TestRuleAccessor
import io.cucumber.junit.TestRulesData
import io.cucumber.junit.TestRulesExecutor
import org.junit.rules.TestRule
import org.junit.runner.Description
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Provider

@HiltAndroidTest
class HiltObjectFactory : ObjectFactory {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var rulesExecutor: TestRulesExecutor
    private val testDescription = Description.createTestDescription(javaClass, "start")

    private val objects = hashMapOf<Class<*>, Any?>()

    override fun start() {

        rulesExecutor = TestRulesExecutor(listOf(TestRulesData(false, this, listOf(ruleAccessor()), "")), executor)

        rulesExecutor.startRules(testDescription, emptyList())
    }

    private fun ruleAccessor(): TestRuleAccessor {
        val hiltRule = HiltAndroidRule(this)
        return object : TestRuleAccessor {
            override fun getRule(obj: Any?): TestRule = hiltRule

            override fun getOrder(): Int = 0
        }
    }

    override fun stop() {
        rulesExecutor.stopRules()
        objects.clear()
    }

    override fun addClass(glueClass: Class<*>?): Boolean = true

    override fun <T : Any?> getInstance(glueClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return objects.getOrPut(glueClass) {
            val instance = createInstance(glueClass)
            injectWithHilt(glueClass, instance)
            instance
        } as T
    }

    private fun <T : Any?> injectWithHilt(glueClass: Class<T>, instance: T) {
        HiltExposer.getTestComponentData(glueClass)?.testInjector()?.injectTest(instance)
    }

    private fun <T : Any?> createInstance(glueClass: Class<T>): T {
        return tryFindProviderInHiltComponent(glueClass) ?: createInstanceUsingConstructor(glueClass)
    }

    private fun <T : Any?> createInstanceUsingConstructor(glueClass: Class<T>) = glueClass.declaredConstructors.single().let { constructor ->
        @Suppress("UNCHECKED_CAST")
        constructor.newInstance(*constructor.parameterTypes.map { getInstance(it) }.toTypedArray()) as T
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T : Any?> tryFindProviderInHiltComponent(glueClass: Class<T>): T? {
        val component = (ApplicationProvider.getApplicationContext<Application>() as GeneratedComponentManager<Any>).generatedComponent()
        return component.javaClass.declaredFields.find { it.name == "${glueClass.simpleName.replaceFirstChar { it.lowercase(Locale.ROOT) }}Provider" }?.let {
            it.isAccessible = true
            it.get(component) as Provider<T>
        }?.get()
    }
}