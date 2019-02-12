package cucumber.runtime.android;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;

import android.support.test.InstrumentationRegistry;
import android.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cucumber.api.android.CucumberAndroidJUnitRunner;
import cucumber.runtime.Utils;
import gherkin.events.PickleEvent;

public class CucumberJUnitRunner extends Runner implements Filterable {

    private final CucumberAndroidJUnitRunner instrumentationRunner;

    private final CucumberExecutor cucumberExecutor;

    private final List<PickleEvent> pickleEvents;

    public CucumberJUnitRunner(Class testClass) {
        instrumentationRunner = (CucumberAndroidJUnitRunner) InstrumentationRegistry
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
        Pair<String, String> testName = calculateUniqueTestName(pickleEvent);
        return Description.createTestDescription(testName.first, testName.second, testName.second);
    }

    @Override
    public void run(final RunNotifier notifier) {
        cucumberExecutor.execute();
    }

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

    /**
     * The stored unique test name for a test case.
     * We use an identity hash-map since we want to distinct all test case objects.
     * Thus, the key is a unique test case object.<br/>
     * The mapped value is the unique test name, which maybe differs from test case original
     * non-unique name.
     */
    private final Map<PickleEvent, String>
            uniqueTestNameForTestCase = new IdentityHashMap<>();

    /**
     * The stored unique test names grouped by feature.<br/>
     * The key contains the feature file.<br/>
     * The mapped value is a set of unique test names for the feature.
     */
    private final Map<String, Set<String>> uniqueTestNamesForFeature = new HashMap<>();

    /**
     * Creates a unique test name for the given test case by filling the internal maps
     * {@link #uniqueTestNameForTestCase} and {@link #uniqueTestNamesForFeature}.<br/>
     * If the test case name is unique, it will be used, otherwise, a index will be added " 2", "
     * 3", " 4", ...
     *
     * @param pickleEvent the test case
     * @return a unique test name
     */
    private Pair<String, String> calculateUniqueTestName(PickleEvent pickleEvent) {
        String existingName = uniqueTestNameForTestCase.get(pickleEvent);
        if (existingName != null) {
            // Nothing to do: there is already a test name for the passed test case object
            return new Pair<>(pickleEvent.uri, existingName);
        }
        final String feature = pickleEvent.uri;
        String uniqueTestCaseName = pickleEvent.pickle.getName();
        if (!uniqueTestNamesForFeature.containsKey(feature)) {
            // First test case of the feature
            uniqueTestNamesForFeature.put(feature, new HashSet<String>());
        }
        final Set<String> uniqueTestNamesSetForFeature = uniqueTestNamesForFeature.get(feature);
        // If "name" already exists, the next one is "name_2" or "name with spaces 2"
        int i = 2;
        while (uniqueTestNamesSetForFeature.contains(uniqueTestCaseName)) {
            uniqueTestCaseName = Utils
                    .getUniqueTestNameForScenarioExample(pickleEvent.pickle.getName(), i);
            i++;
        }
        uniqueTestNamesSetForFeature.add(uniqueTestCaseName);
        uniqueTestNameForTestCase.put(pickleEvent, uniqueTestCaseName);
        return new Pair<>(pickleEvent.uri, uniqueTestNameForTestCase.get(pickleEvent));
    }
}
