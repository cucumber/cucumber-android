package io.cucumber.android

import io.cucumber.core.runtime.ObjectFactorySupplier
import io.cucumber.junit.TestRuleAccessor
import io.cucumber.junit.TestRulesData
import io.cucumber.junit.TestRulesExecutor
import io.cucumber.junit.WithJunitRule
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.Executors

class RulesBackend(
    private val objectFactorySupplier: ObjectFactorySupplier
) {
    private val classesWithRules: MutableList<TestRulesData> = ArrayList()
    private var rulesExecutor: TestRulesExecutor? = null
    private val executorService = Executors.newSingleThreadExecutor()
    private var description: Description? = null
    private var tags:List<String> = emptyList()
    fun buildWorld() {
        val objects = ArrayList<TestRulesData>(classesWithRules.size)
        val objectFactory = objectFactorySupplier.get()
        for (clazzRules in classesWithRules) {
            val instance = objectFactory.getInstance(clazzRules.declaringClass)
            objects.add(TestRulesData(clazzRules.useAsTestClassInDescription(), instance, clazzRules.accessors, clazzRules.tagExpression))
        }
        rulesExecutor = TestRulesExecutor(objects, executorService)
        rulesExecutor?.startRules(description, tags)
    }

    fun setDescription(description: Description?, tags:List<String>) {
        this.description = description
        this.tags = tags
    }

    fun disposeWorld() {
        rulesExecutor?.stopRules()
        description = null
        tags = emptyList()
    }

    fun scan(glueClass: Class<*>) {
        val objectFactory = objectFactorySupplier.get()
        val annotation = glueClass.getAnnotation(WithJunitRule::class.java)
        if (annotation != null) {
            if (objectFactory.addClass(glueClass)) {
                classesWithRules.add(TestRulesData(annotation.useAsTestClassInDescription, glueClass, getAccessors(glueClass), annotation.value))
            }
        }
    }

    private class FieldRuleAccessor(val field: Field, private val order: Int) : TestRuleAccessor {
        override fun getRule(obj: Any): TestRule {
            return field[obj] as TestRule
        }

        override fun getOrder(): Int {
            return order
        }
    }

    private class MethodRuleAccessor(val method: Method, private val order: Int) : TestRuleAccessor {
        override fun getRule(obj: Any): TestRule {
            return method.invoke(obj) as TestRule
        }

        override fun getOrder(): Int {
            return order
        }
    }

    private fun getAccessors(clazz: Class<*>): List<TestRuleAccessor> {
        val accessors: MutableList<TestRuleAccessor> = ArrayList(1)
        for (m in clazz.methods) {
            val annotation = m.getAnnotation(Rule::class.java)
            if (annotation != null) {
                accessors.add(MethodRuleAccessor(m, annotation.order))
            }
        }
        for (f in clazz.fields) {
            val annotation = f.getAnnotation(Rule::class.java)
            if (annotation != null) {
                accessors.add(FieldRuleAccessor(f, annotation.order))
            }
        }
        return accessors
    }
}