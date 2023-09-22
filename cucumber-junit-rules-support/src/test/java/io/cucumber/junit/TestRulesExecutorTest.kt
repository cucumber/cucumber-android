package io.cucumber.junit

import io.cucumber.core.exception.CucumberException
import org.junit.After
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class TestRulesExecutorTest {
    private val service = Executors.newSingleThreadExecutor()

    @After
    fun tearDown() {
        service.shutdown()
    }

    @Test
    fun `does not wait forever for deadlock in rule teardown`() {

        val rulesData = testRulesData(after = {Thread.sleep(10_000)})
        val rulesExecutor = TestRulesExecutor(rulesData, service, 1,TimeUnit.SECONDS)

        rulesExecutor.startRules(Description.createTestDescription(javaClass,"test"), emptyList())
        assertFailsWith(CucumberException::class) { rulesExecutor.stopRules() }
    }

    @Test
    fun `does not wait forever for deadlock in rule setup`() {

        val rulesData = testRulesData(before = {Thread.sleep(10_000)})
        val rulesExecutor = TestRulesExecutor(rulesData, service, 1,TimeUnit.SECONDS)

        assertFailsWith(CucumberException::class) { rulesExecutor.startRules(Description.createTestDescription(javaClass,"test"), emptyList()) }

    }

    private fun testRulesData(before:() -> Unit = {},after:() -> Unit = {}): List<TestRulesData> {
        val rulesData = listOf(TestRulesData(false, this, listOf(object : TestRuleAccessor {
            override fun getRule(obj: Any?): TestRule = TestRule { base, _ ->
                object : Statement() {
                    override fun evaluate() {
                        before()
                        base.evaluate()
                        after()
                    }
                }
            }

            override fun getOrder(): Int = 0
        }), ""))
        return rulesData
    }
}