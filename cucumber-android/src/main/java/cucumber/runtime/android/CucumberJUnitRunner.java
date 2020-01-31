package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import cucumber.api.CucumberOptions;
import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.android.CucumberArgumentsProvider;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.java.ObjectFactory;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DefaultTypeRegistryConfiguration;
import cucumber.runtime.Env;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.Reflections;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.Utils;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.AndroidLogcatReporter;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.formatter.Stats;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactoryLoader;
import cucumber.runtime.junit.AndroidFeatureRunner;
import cucumber.runtime.junit.AndroidPickleRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import dalvik.system.DexFile;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.TableRow;
import gherkin.events.PickleEvent;
import io.cucumber.core.model.GluePath;
import io.cucumber.stepexpression.TypeRegistry;

import static java.util.Collections.singletonList;

public class CucumberJUnitRunner extends ParentRunner<AndroidFeatureRunner> implements Filterable {


    /**
     * The logcat tag to log all cucumber related information to.
     */
    static final String TAG = "cucumber-android";

    /**
     * The system property name of the cucumber options.
     */
    private static final String CUCUMBER_OPTIONS_SYSTEM_PROPERTY = "cucumber.options";

    /**
     * The {@link cucumber.runtime.RuntimeOptions} to get the {@link CucumberFeature}s from.
     */
    private List<AndroidFeatureRunner> children = new ArrayList<>();

    private EventBus bus;

    public CucumberJUnitRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        Bundle runnerArguments = getRunnerBundle(instrumentation);
        Arguments arguments = new Arguments(runnerArguments);

        trySetCucumberOptionsToSystemProperties(arguments);
        Context context = instrumentation.getContext();
        ClassLoader classLoader = context.getClassLoader();
        ClassFinder classFinder = createDexClassFinder(context);
        RuntimeOptions runtimeOptions = createRuntimeOptions(context, classFinder).noSummaryPrinter();

        ResourceLoader resourceLoader = new AndroidResourceLoader(context);

        bus = new TimeServiceEventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), runtimeOptions);
        plugins.setSerialEventBusOnEventListenerPlugins(bus);
        plugins.setEventBusOnEventListenerPlugins(bus);
        Runner runner = new ThreadLocalRunnerSupplier(runtimeOptions, bus, createBackends(runtimeOptions, classFinder)).get();
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        Filters filters = new Filters(runtimeOptions);
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.setEventPublisher(bus);
        Stats stats = new Stats();
        stats.setEventPublisher(bus);

        plugins.addPlugin(new AndroidLogcatReporter(stats, undefinedStepsTracker, TAG));

        //check if this is for single scenario
        String testClassNameFromRunner = runnerArguments.getString(CucumberAndroidJUnitArguments.InternalCucumberAndroidArgs.CUCUMBER_ANDROID_TEST_CLASS);
        String requestedFeatureName = null;
        String requestedScenarioName = null;
        if (testClassNameFromRunner != null) {
            String[] split = testClassNameFromRunner.split("#");
            if (split.length > 1) {
                requestedFeatureName = split[0];
                requestedScenarioName = split[1];
            } else {
                Log.e(TAG, "CucumberJUnitRunner: invalid argument '" + CucumberAndroidJUnitArguments.InternalCucumberAndroidArgs.CUCUMBER_ANDROID_TEST_CLASS + "' = '" + testClassNameFromRunner + "'");
            }
        }
        // Start the run before reading the features.
        // Allows the test source read events to be broadcast properly
        List<CucumberFeature> features = featureSupplier.get();
        Collection<String> featuresNames = new HashSet<>(features.size());
        StringBuilder duplicateScenariosNameMessage = new StringBuilder();
        bus.send(new TestRunStarted(bus.getTime(), bus.getTimeMillis()));

        JUnitOptions junitOptions = new JUnitOptions(runtimeOptions.isStrict(), runtimeOptions.getJunitOptions());
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
            String featureName = feature.getName();
            if (requestedFeatureName != null && !requestedFeatureName.equals(featureName)) {
                continue;
            }
            List<PickleEvent> pickles = feature.getPickles();
            List<AndroidPickleRunner> pickleRunners = new ArrayList<>(pickles.size());
            Collection<String> pickleNames = new HashSet<>(pickles.size());
            for (PickleEvent pickleEvent : pickles) {

                if (filters.matchesFilters(pickleEvent)) {
                    String currentScenarioName = getScenarioName(pickleEvent, feature.getGherkinFeature().getFeature());
                    if (pickleNames.contains(currentScenarioName)) {
                        // in case of scenario name duplication in single feature:
                        addDuplicateScenarioMessage(duplicateScenariosNameMessage, featureName, currentScenarioName);
                    }
                    pickleNames.add(currentScenarioName);

                    if (requestedScenarioName != null) {
                        if (requestedScenarioName.equals(currentScenarioName)) {
                            AndroidPickleRunner pickleRunner = new AndroidPickleRunner(runner, pickleEvent, junitOptions, feature, currentScenarioName);
                            pickleRunners.add(pickleRunner);
                            children.add(new AndroidFeatureRunner(testClass, feature, pickleRunners));
                            throwErrorIfDuplicateScenarios(duplicateScenariosNameMessage);
                            return;
                        }
                    } else {
                        AndroidPickleRunner pickleRunner = new AndroidPickleRunner(runner, pickleEvent, junitOptions, feature, currentScenarioName);
                        pickleRunners.add(pickleRunner);
                    }
                }
            }
            addFeatureIfHasChildren(testClass, featuresNames, duplicateScenariosNameMessage, feature, featureName, pickleRunners);
        }
        throwErrorIfDuplicateScenarios(duplicateScenariosNameMessage);
    }

    private Bundle getRunnerBundle(Instrumentation instrumentation) throws InitializationError {
        if (!(instrumentation instanceof CucumberArgumentsProvider)) {
            Log.e(TAG, "Runner must implement CucumberArgumentsProvider");
            throw new InitializationError("Use runner that implements CucumberArgumentsProvider class.");
        }

        return ((CucumberArgumentsProvider) instrumentation).getArguments().getRunnerArgs();
    }

    private void addFeatureIfHasChildren(Class<?> testClass, Collection<String> featuresNames, StringBuilder duplicateScenariosNameMessage,
                                         CucumberFeature feature, String featureName, List<AndroidPickleRunner> pickleRunners) throws InitializationError {
        if (!pickleRunners.isEmpty()) {
            if (featuresNames.contains(featureName)) {
                // in case of feature name duplication:
                addDuplicateFeatureMessage(duplicateScenariosNameMessage, featureName);
            }
            featuresNames.add(featureName);
            children.add(new AndroidFeatureRunner(testClass, feature, pickleRunners));
        }
    }

    private static void throwErrorIfDuplicateScenarios(CharSequence duplicateScenariosNameMessage) throws InitializationError {
        if (duplicateScenariosNameMessage.length() > 0) {
            InitializationError error = new InitializationError(duplicateScenariosNameMessage.toString());
            Log.e(TAG, "CucumberJUnitRunner: ", error);
            throw error;
        }
    }

    private static void addDuplicateFeatureMessage(StringBuilder duplicateScenariosNameMessage, String featureName) {
        duplicateScenariosNameMessage
                .append('\n')
                .append("Duplicate feature name '")
                .append(featureName)
                .append("'");
    }

    private static void addDuplicateScenarioMessage(StringBuilder duplicateScenariosNameMessage, String featureName, String currentScenarioName) {
        duplicateScenariosNameMessage.append('\n')
                .append("Duplicate scenario name '")
                .append(currentScenarioName)
                .append("' in feature '")
                .append(featureName)
                .append("'");
    }

    private static String getScenarioName(PickleEvent pickleEvent, Feature feature) {
        int exampleNumber = findExampleNumber(pickleEvent, feature);
        String pickleName = pickleEvent.pickle.getName();
        if (exampleNumber > 0) {
            // if this is example we always add example number in case of sharding
            return Utils.getUniqueTestNameForScenarioExample(pickleName, exampleNumber);
        }
        return pickleName;
    }


    private static int findExampleNumber(PickleEvent pickleEvent, Feature feature) {
        int pickleLine = getLine(pickleEvent);
        for (ScenarioDefinition definition : feature.getChildren()) {
            if (definition instanceof ScenarioOutline) {
                List<Examples> examples = ((ScenarioOutline) definition).getExamples();
                int index = 0;
                for (Examples example : examples) {
                    List<TableRow> tableBody = example.getTableBody();
                    for (TableRow row : tableBody) {
                        if (row.getLocation().getLine() == pickleLine) {
                            return index + 1;
                        }
                        index++;
                    }
                }
            }
        }
        return 0;
    }


    private static int getLine(PickleEvent pickleEvent) {
        return pickleEvent.pickle.getLocations().get(0).getLine();
    }


    @Override
    protected List<AndroidFeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(AndroidFeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(AndroidFeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }


    private static void trySetCucumberOptionsToSystemProperties(final Arguments arguments) {
        final String cucumberOptions = arguments.getCucumberOptions();
        if (!cucumberOptions.isEmpty()) {
            Log.d(TAG, "Setting cucumber.options from arguments: '" + cucumberOptions + "'");
            System.setProperty(CUCUMBER_OPTIONS_SYSTEM_PROPERTY, cucumberOptions);
        }
    }

    private static ClassFinder createDexClassFinder(final Context context) {
        final String apkPath = context.getPackageCodePath();
        return new DexClassFinder(newDexFile(apkPath));
    }

    private static DexFile newDexFile(final String apkPath) {
        try {
            return new DexFile(apkPath);
        } catch (final IOException e) {
            throw new CucumberException("Failed to open " + apkPath);
        }
    }

    private static RuntimeOptions createRuntimeOptions(Context context, ClassFinder classFinder) {
        for (final Class<?> clazz : classFinder.getDescendants(Object.class, GluePath.parse(context.getPackageName()))) {
            if (clazz.isAnnotationPresent(CucumberOptions.class)) {
                Log.d(TAG, "Found CucumberOptions in class " + clazz.getName());
                final RuntimeOptionsFactory factory = new RuntimeOptionsFactory(clazz);
                return factory.create();
            }
        }

        throw new CucumberException("No CucumberOptions annotation");
    }

    private static BackendSupplier createBackends(RuntimeOptions runtimeOptions, ClassFinder classFinder) {
        return new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                final Reflections reflections = new Reflections(classFinder);
                final ObjectFactory delegateObjectFactory = ObjectFactoryLoader.loadObjectFactory(classFinder, Env.INSTANCE.get(ObjectFactory.class.getName()));
                final TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, runtimeOptions.getGlue(), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
                final TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
                typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
                return singletonList(new JavaBackend(delegateObjectFactory, classFinder, typeRegistry));
            }
        };

    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        final Statement features = super.childrenInvoker(notifier);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                features.evaluate();
                bus.send(new TestRunFinished(bus.getTime(), bus.getTimeMillis()));
            }
        };
    }
}
