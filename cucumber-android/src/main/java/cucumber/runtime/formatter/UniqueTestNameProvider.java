package cucumber.runtime.formatter;

import cucumber.runtime.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class UniqueTestNameProvider<T> {

	/**
	 * The stored unique test name for a test case.
	 * We use an identity hash-map since we want to distinct all test case objects.
	 * Thus, the key is a unique test case object.<br/>
	 * The mapped value is the unique test name, which maybe differs from test case original non-unique name.
	 */
	private final Map<T, String> uniqueTestNameForTestCase = new IdentityHashMap<>();

	/**
	 * The stored unique test names grouped by feature.<br/>
	 * The key contains the feature file.<br/>
	 * The mapped value is a set of unique test names for the feature.
	 */
	private final Map<String, Set<String>> uniqueTestNamesForFeature = new HashMap<>();

	/**
	 * Creates a unique test name for the given test case by filling the internal maps
	 * {@link #uniqueTestNameForTestCase} and {@link #uniqueTestNamesForFeature}.<br/>
	 * If the test case name is unique, it will be used, otherwise, a index will be added " 2", " 3", " 4", ...
	 * @param testCase the test case
	 * @return a unique test name
	 */
	public String calculateUniqueTestName(T testCase,String testCaseName,String testCaseUri) {
		String existingName = uniqueTestNameForTestCase.get(testCase);
		if (existingName != null) {
			// Nothing to do: there is already a test name for the passed test case object
			return existingName;
		}
		String uniqueTestCaseName = testCaseName;
		Set<String> uniqueTestNamesSetForFeature = uniqueTestNamesForFeature.get(testCaseUri);
		if (uniqueTestNamesSetForFeature==null) {
			// First test case of the feature
			uniqueTestNamesSetForFeature=new HashSet<>();
			uniqueTestNamesForFeature.put(testCaseUri, uniqueTestNamesSetForFeature);
		}
		// If "name" already exists, the next one is "name_2" or "name with spaces 2"
		int i = 2;
		while (uniqueTestNamesSetForFeature.contains(uniqueTestCaseName)) {
			uniqueTestCaseName = Utils.getUniqueTestNameForScenarioExample(testCaseName, i);
			i++;
		}
		uniqueTestNamesSetForFeature.add(uniqueTestCaseName);
		uniqueTestNameForTestCase.put(testCase, uniqueTestCaseName);
		return uniqueTestNameForTestCase.get(testCase);
	}
}
