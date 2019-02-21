package cucumber.runtime.android;

import androidx.test.platform.app.InstrumentationRegistry;
import cucumber.api.android.CucumberAndroidJUnitRunner;
import cucumber.runtime.formatter.UniqueTestNameProvider;
import gherkin.events.PickleEvent;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

import java.util.Iterator;
import java.util.List;

public class CucumberJUnitRunner extends Runner implements Filterable {

    private final CucumberExecutor cucumberExecutor;

    private final List<PickleEvent> pickleEvents;

    private final UniqueTestNameProvider<PickleEvent> uniqueTestNameProvider = new UniqueTestNameProvider<>();

    public CucumberJUnitRunner(@SuppressWarnings("unused") Class testClass) {
        CucumberAndroidJUnitRunner instrumentationRunner = (CucumberAndroidJUnitRunner) InstrumentationRegistry
                .getInstrumentation();
        cucumberExecutor = new CucumberExecutor(new Arguments(instrumentationRunner.getArguments()),
                                                instrumentationRunner);
        pickleEvents = cucumberExecutor.getPickleEvents();
    }

    @Override
    public Description getDescription() {
        Description rootDescription = Description.createSuiteDescription("All tests", 1);

        for (PickleEvent pickleEvent : pickleEvents) {
            rootDescription.addChild(makeDescriptionFromPickle(pickleEvent));
        }
        return rootDescription;
    }

    private Description makeDescriptionFromPickle(PickleEvent pickleEvent) {
        String testName = uniqueTestNameProvider.calculateUniqueTestName(pickleEvent, pickleEvent.pickle.getName(), pickleEvent.uri);
        return Description.createTestDescription(pickleEvent.uri, testName, testName);
    }

    @Override
    public void run(final RunNotifier notifier) {
        cucumberExecutor.execute();
    }

    @Override
    public int testCount() {
        return pickleEvents.size();
    }

    @Override
    public void filter(final Filter filter) throws NoTestsRemainException {
        for (Iterator<PickleEvent> iterator = pickleEvents.iterator(); iterator.hasNext(); ) {
            PickleEvent method = iterator.next();
            if (!filter.shouldRun(makeDescriptionFromPickle(method))) {
                iterator.remove();
            }
        }

        if (pickleEvents.isEmpty()) {
            throw new NoTestsRemainException();
        }
    }

}
