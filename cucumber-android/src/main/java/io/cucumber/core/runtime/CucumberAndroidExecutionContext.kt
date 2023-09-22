package io.cucumber.core.runtime

import android.annotation.SuppressLint
import android.os.Build
import io.cucumber.android.cucumber_android.BuildConfig
import io.cucumber.core.eventbus.EventBus
import io.cucumber.core.exception.ExceptionUtils
import io.cucumber.core.exception.UnrecoverableExceptions
import io.cucumber.core.gherkin.Feature
import io.cucumber.core.logging.LoggerFactory
import io.cucumber.core.runner.Runner
import io.cucumber.messages.Convertor
import io.cucumber.messages.ProtocolVersion
import io.cucumber.messages.types.Ci
import io.cucumber.messages.types.Envelope
import io.cucumber.messages.types.Git
import io.cucumber.messages.types.Meta
import io.cucumber.messages.types.Product
import io.cucumber.plugin.event.Node
import io.cucumber.plugin.event.Result
import io.cucumber.plugin.event.Status
import io.cucumber.plugin.event.TestRunFinished
import io.cucumber.plugin.event.TestRunStarted
import io.cucumber.plugin.event.TestSourceParsed
import io.cucumber.plugin.event.TestSourceRead
import java.time.Duration
import java.time.Instant

/**
 * This class is copied from [CucumberExecutionContext] to workaround issue with exception in [Ci] resolving
 *
 * ```
 * Caused by: java.util.regex.PatternSyntaxException: Syntax error in regexp pattern near index 32
 * \$\{(.*?)(?:(?<!\\)/(.*)/(.*))?}
 * ```
 */
@SuppressLint("NewApi")
internal class CucumberAndroidExecutionContext(
    private val bus: EventBus,
    private val exitStatus: ExitStatus,
    private val runnerSupplier: RunnerSupplier,
) {
    private val collector = RethrowingThrowableCollector()
    private var start: Instant? = null

    fun interface ThrowingRunnable {
        @Throws(Throwable::class)
        fun run()
    }

    fun startTestRun() {
        emitMeta()
        emitTestRunStarted()
    }

    private fun emitMeta() {
        bus.send(Envelope.of(createMeta()))
    }

    private fun createMeta(): Meta {
        @Suppress("DEPRECATION")
        return Meta(
            ProtocolVersion.getVersion(),
            Product("cucumber-android", BuildConfig.VERSION),
            Product("Android", "API " + Build.VERSION.SDK_INT),
            Product(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.BASE_OS else "Android", Build.VERSION.RELEASE),
            Product(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) Build.SUPPORTED_ABIS[0] else Build.CPU_ABI, null),
            Ci("unknown", "unknown", "unknown", Git("unknown", "unknown", "unknown", "unknown"))
        )
    }

    private fun emitTestRunStarted() {
        log.debug { "Sending run test started event" }
        start = bus.instant
        bus.send(TestRunStarted(start))
        bus.send(Envelope.of(io.cucumber.messages.types.TestRunStarted(Convertor.toMessage(start))))
    }

    fun runBeforeAllHooks() {
        val runner = runner
        collector.executeAndThrow { runner.runBeforeAllHooks() }
    }

    fun runAfterAllHooks() {
        val runner = runner
        collector.executeAndThrow { runner.runAfterAllHooks() }
    }

    fun finishTestRun() {
        log.debug { "Sending test run finished event" }
        val cucumberException = throwable
        emitTestRunFinished(cucumberException)
    }

    val throwable: Throwable?
        get() = collector.throwable

    private fun emitTestRunFinished(exception: Throwable?) {
        val instant = bus.instant
        val result = Result(
            if (exception != null) Status.FAILED else exitStatus.status,
            Duration.between(start, instant),
            exception
        )
        bus.send(TestRunFinished(instant, result))
        val testRunFinished = io.cucumber.messages.types.TestRunFinished(
            if (exception != null) ExceptionUtils.printStackTrace(exception) else null,
            exception == null && exitStatus.isSuccess,
            Convertor.toMessage(instant),
            if (exception == null) null else Convertor.toMessage(exception)
        )
        bus.send(Envelope.of(testRunFinished))
    }

    fun beforeFeature(feature: Feature) {
        log.debug { "Sending test source read event for " + feature.uri }
        bus.send(TestSourceRead(bus.instant, feature.uri, feature.source))
        bus.send(TestSourceParsed(bus.instant, feature.uri, listOf<Node>(feature)))
        bus.sendAll(feature.parseEvents)
    }

    fun runTestCase(execution: (Runner) -> Unit) {
        val runner = runner
        collector.executeAndThrow { execution(runner) }
    }

    private val runner: Runner
        get() = collector.executeAndThrow<Runner> { runnerSupplier.get() }

    fun runFeatures(executeFeatures: ThrowingRunnable) {
        startTestRun()
        execute {
            runBeforeAllHooks()
            executeFeatures.run()
        }
        try {
            execute { runAfterAllHooks() }
        } finally {
            finishTestRun()
        }
        val throwable = throwable
        if (throwable != null) {
            ExceptionUtils.throwAsUncheckedException(throwable)
        }
    }

    private fun execute(runnable: ThrowingRunnable) {
        try {
            runnable.run()
        } catch (t: Throwable) {
            // Collected in CucumberExecutionContext
            UnrecoverableExceptions.rethrowIfUnrecoverable(t)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CucumberAndroidExecutionContext::class.java)
    }
}