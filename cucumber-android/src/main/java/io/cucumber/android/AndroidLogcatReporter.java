package io.cucumber.android;

import android.util.Log;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestStepStarted;

/**
 * Logs information about the currently executed statements to androids logcat.
 */
public final class AndroidLogcatReporter implements ConcurrentEventListener {

	/**
	 * The log tag to be used when logging to logcat.
	 */
	private final String logTag;
	/**
	 * The event handler that logs the {@link TestCaseStarted} events.
	 */
	private final EventHandler<TestCaseStarted> testCaseStartedHandler = new EventHandler<TestCaseStarted>() {
		@Override
		public void receive(TestCaseStarted event) {
			Log.d(logTag, String.format("%s", event.getTestCase().getName()));
		}
	};
	/**
	 * The event handler that logs the {@link TestStepStarted} events.
	 */
	private final EventHandler<TestStepStarted> testStepStartedHandler = new EventHandler<TestStepStarted>() {
		@Override
		public void receive(TestStepStarted event) {
			if (event.getTestStep() instanceof PickleStepTestStep) {
				PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
				Log.d(logTag, String.format("%s", testStep.getStep().getText()));
			}
		}
	};
	/**
	 * The event handler that logs the {@link TestRunFinished} events.
	 */
	private EventHandler<TestRunFinished> runFinishHandler = new EventHandler<TestRunFinished>() {
		@Override
		public void receive(TestRunFinished event) {
            Throwable error = event.getResult().getError();
            if (error != null) {
                Log.e(logTag, error.toString());
            }
		}
	};

	/**
	 * Creates a new instance for the given parameters.
	 *
	 * @param logTag                the tag to use for logging to logcat
	 */
	public AndroidLogcatReporter(String logTag) {
		this.logTag = logTag;
	}

	@Override
	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
		publisher.registerHandlerFor(TestStepStarted.class, testStepStartedHandler);
		publisher.registerHandlerFor(TestRunFinished.class, runFinishHandler);
	}
}
