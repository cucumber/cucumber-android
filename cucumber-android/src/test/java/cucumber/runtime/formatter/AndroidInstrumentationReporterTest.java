package cucumber.runtime.formatter;

import android.app.Instrumentation;
import android.os.Bundle;
import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.TestSourceRead;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.formatter.AndroidInstrumentationReporter.StatusCodes;
import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class AndroidInstrumentationReporterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final UndefinedStepsTracker runtime = mock(UndefinedStepsTracker.class);
    private final Instrumentation instrumentation = mock(Instrumentation.class);

    private final TestSourceRead testSourceRead = new TestSourceRead(
        0l,
        "path/file.feature",
        "Feature: feature name\n  Scenario: some important scenario\n");
    private final TestCase testCase = mock(TestCase.class);
    private Result firstResult ;
    private Result secondResult;


    @Before
    public void beforeEachTest() {
        when(testCase.getUri()).thenReturn("path/file.feature");
        when(testCase.getName()).thenReturn("Some important scenario");
    }

    @Test
    public void feature_name_and_keyword_is_contained_in_start_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);

        // when
        formatter.testSourceRead(testSourceRead);
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString("feature name"));
    }

    @Test
    public void feature_name_and_keyword_is_contained_in_end_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult= createResultStatus(Result.Type.PASSED);

        // when
        formatter.testSourceRead(testSourceRead);
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.OK), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.CLASS), containsString("feature name"));
    }

    @Test
    public void scenario_name_is_contained_in_start_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(testCase.getName()));
    }

    @Test
    public void scenario_name_is_contained_in_end_signal() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        verify(instrumentation).sendStatus(eq(StatusCodes.OK), captor.capture());

        final Bundle actualBundle = captor.getValue();

        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST), containsString(testCase.getName()));
    }

    @Test
    public void current_number_is_contained_in_start_and_end_signal() {

        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);

        formatter.setNumberOfTests(2);

        current_number_is_contained_instart_and_end_signal(formatter, captor, 1);
        current_number_is_contained_instart_and_end_signal(formatter, captor, 2);
    }

    private void current_number_is_contained_instart_and_end_signal(AndroidInstrumentationReporter formatter, ArgumentCaptor<Bundle> captor, int expectedCurrent) {

        formatter.startTestCase(testCase);
        firstResult=createResultStatus(Result.Type.PASSED,null);

        verify(instrumentation,times(expectedCurrent)).sendStatus(eq(StatusCodes.START), captor.capture());

        Bundle actualBundle = captor.getValue();

        assertEquals(expectedCurrent, actualBundle.getInt(AndroidInstrumentationReporter.StatusKeys.CURRENT));

        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        verify(instrumentation,times(expectedCurrent)).sendStatus(eq(StatusCodes.OK), captor.capture());

        assertEquals(expectedCurrent,captor.getValue().getInt(AndroidInstrumentationReporter.StatusKeys.CURRENT));
    }

    @Test
    public void any_step_exception_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.FAILED,new RuntimeException("some random runtime exception"));

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some random runtime exception"));

    }

    @Test
    public void any_failing_step_causes_test_failure() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.FAILED,new AssertionError("some test assertion went wrong"));

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some test assertion went wrong"));
    }

    private Result createResultStatus(Result.Type type, Throwable error) {
        return new Result(type, 0L,error);
    }

    @Test
    public void any_undefined_step_causes_test_error() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.UNDEFINED);
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void passing_step_causes_test_success() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);

        // when
        formatter.setNumberOfTests(1);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        // then
        verify(instrumentation).sendStatus(eq(StatusCodes.OK), any(Bundle.class));
    }

    @Test
    public void skipped_step_causes_test_success() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);
        secondResult=createResultStatus(Result.Type.SKIPPED);

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        verify(instrumentation).sendStatus(eq(StatusCodes.OK), any(Bundle.class));

    }

    @Test
    public void first_step_result_exception_is_reported() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.FAILED,new RuntimeException("first exception"));

        secondResult=createResultStatus(Result.Type.FAILED,new RuntimeException("second exception"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("first exception"));
    }

    @Test
    public void undefined_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);

        secondResult=createResultStatus(Result.Type.UNDEFINED);
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void pending_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);

        secondResult=createResultStatus(Result.Type.PENDING,new PendingException("step is pending"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("step is pending"));
    }

    @Test
    public void failed_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);

        secondResult=createResultStatus(Result.Type.FAILED,new AssertionError("some assertion went wrong"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some assertion went wrong"));
    }

    @Test
    public void error_step_overrides_preceding_passed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.PASSED);

        secondResult=createResultStatus(Result.Type.FAILED,new RuntimeException("some exception"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some exception"));
    }

    @Test
    public void failed_step_does_not_overrides_preceding_undefined_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.UNDEFINED);
        when(runtime.getSnippets()).thenReturn(Collections.singletonList("some snippet"));

        secondResult=createResultStatus(Result.Type.FAILED,new AssertionError("some assertion went wrong"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.ERROR), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some snippet"));
    }

    @Test
    public void error_step_does_not_override_preceding_failed_step() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.FAILED,new AssertionError("some assertion went wrong"));

        secondResult=createResultStatus( Result.Type.FAILED,new RuntimeException("some exception"));

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), captor.capture());

        final Bundle actualBundle = captor.getValue();
        assertThat(actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.STACK), containsString("some assertion went wrong"));
    }

    @Test
    public void step_result_contains_only_the_current_scenarios_severest_result() {
        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        firstResult=createResultStatus(Result.Type.FAILED,new AssertionError("some assertion went wrong"));

        secondResult=createResultStatus(Result.Type.PASSED);

        // when
        formatter.setNumberOfTests(2);
        formatter.startTestCase(testCase);
        formatter.finishTestStep(firstResult);
        formatter.finishTestCase();

        formatter.startTestCase(testCase);
        formatter.finishTestStep(secondResult);
        formatter.finishTestCase();

        // then

        final InOrder inOrder = inOrder(instrumentation);
        final ArgumentCaptor<Bundle> firstCaptor = ArgumentCaptor.forClass(Bundle.class);
        final ArgumentCaptor<Bundle> secondCaptor = ArgumentCaptor.forClass(Bundle.class);

        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.FAILURE), firstCaptor.capture());
        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.OK), secondCaptor.capture());
    }

    @Test
    public void test_names_within_feature_are_made_unique_by_appending_blank_and_number() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        TestCase testCase1 = mockTestCase(testCaseName("not unique name"));
        TestCase testCase2 = mockTestCase(testCaseName("not unique name"));
        TestCase testCase3 = mockTestCase(testCaseName("not unique name"));

        // when
        simulateRunningTestCases(formatter, asList(testCase1, testCase2, testCase3));

        // then
        final InOrder inOrder = inOrder(instrumentation);
        assertThat(captureTestName(inOrder), equalTo("not unique name"));
        assertThat(captureTestName(inOrder), equalTo("not unique name 2"));
        assertThat(captureTestName(inOrder), equalTo("not unique name 3"));
    }

    @Test
    public void test_names_within_are_made_unique_by_appending_underscore_and_number_when_no_blank_in_name() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        TestCase testCase1 = mockTestCase(testCaseName("not_unique_name"));
        TestCase testCase2 = mockTestCase(testCaseName("not_unique_name"));
        TestCase testCase3 = mockTestCase(testCaseName("not_unique_name"));

        // when
        simulateRunningTestCases(formatter, asList(testCase1, testCase2, testCase3));

        // then
        final InOrder inOrder = inOrder(instrumentation);
        assertThat(captureTestName(inOrder), equalTo("not_unique_name"));
        assertThat(captureTestName(inOrder), equalTo("not_unique_name_2"));
        assertThat(captureTestName(inOrder), equalTo("not_unique_name_3"));
    }

    @Test
    public void test_names_in_different_features_can_be_the_same() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        TestCase testCase1 = mockTestCase(featureUri("path/file1.feature"), testCaseName("not unique name"));
        TestCase testCase2 = mockTestCase(featureUri("path/file2.feature"), testCaseName("not unique name"));

        // when
        simulateRunningTestCases(formatter, asList(testCase1, testCase2));

        // then
        final InOrder inOrder = inOrder(instrumentation);
        assertThat(captureTestName(inOrder), equalTo("not unique name"));
        assertThat(captureTestName(inOrder), equalTo("not unique name"));
    }

    @Test
    public void test_names_are_made_unique_also_when_not_consecutive() {

        // given
        final AndroidInstrumentationReporter formatter = new AndroidInstrumentationReporter(runtime, instrumentation);
        TestCase testCase1 = mockTestCase(testCaseName("not unique name"));
        TestCase testCase2 = mockTestCase(testCaseName("unique name"));
        TestCase testCase3 = mockTestCase(testCaseName("not unique name"));

        // when
        simulateRunningTestCases(formatter, asList(testCase1, testCase2, testCase3));

        // then
        final InOrder inOrder = inOrder(instrumentation);
        assertThat(captureTestName(inOrder), equalTo("not unique name"));
        assertThat(captureTestName(inOrder), equalTo("unique name"));
        assertThat(captureTestName(inOrder), equalTo("not unique name 2"));
    }

    private Result createResultStatus(Result.Type status) {
        return createResultStatus(status, null);
    }

    private TestCase mockTestCase(String testCaseName) {
        return mockTestCase(featureUri("path/file.feature"), testCaseName);
    }

    private TestCase mockTestCase(String featureUri, String testCaseName) {
        TestCase testCase = mock(TestCase.class);
        when(testCase.getUri()).thenReturn(featureUri);
        when(testCase.getName()).thenReturn(testCaseName);
        return testCase;
    }

    private String testCaseName(String name) {
        return name;
    }

    private String featureUri(String uri) {
        return uri;
    }

    private void simulateRunningTestCases(AndroidInstrumentationReporter formatter, List<TestCase> testCases) {
        firstResult=createResultStatus(Result.Type.PASSED);
        formatter.setNumberOfTests(testCases.size());
        for (TestCase testCase : testCases) {
            formatter.startTestCase(testCase);
            formatter.finishTestStep(firstResult);
            formatter.finishTestCase();
        }
    }

    private String captureTestName(InOrder inOrder) {
        final ArgumentCaptor<Bundle> captor = ArgumentCaptor.forClass(Bundle.class);
        inOrder.verify(instrumentation).sendStatus(eq(StatusCodes.START), captor.capture());
        final Bundle actualBundle = captor.getValue();
        return actualBundle.getString(AndroidInstrumentationReporter.StatusKeys.TEST);
    }
}
