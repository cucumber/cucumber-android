package io.cucumber.android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.runner.AndroidJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import io.cucumber.core.options.Constants;

/**
 * This class is responsible for preparing bundle to{@link AndroidJUnitRunner}
 * for cucumber tests. It prepares bundle for running tests from Android Tests Orchestrator
 * <p>
 * Runner argument are linked to the {@link androidx.test.internal.runner.RunnerArgs}
 */
public class CucumberAndroidJUnitArguments {

	/**
	 * Public arguments passed by instrumentation as {@code -e} parameters.
	 */
	public static class PublicArgs {

		/**
		 * User default android junit runner. public interface
		 */
		public static final String USE_DEFAULT_ANDROID_RUNNER = "cucumberUseAndroidJUnitRunner";
		/**
		 * Custom package location of the {@link io.cucumber.junit.CucumberOptions} annotated class.
		 */
		public static final String OPTIONS_ANNOTATION_LOCATION = "optionsAnnotationPackage";

		/**
		 * Path for features. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.features}
		 */
		public static final String FEATURES = "features";

		/**
		 * Glue packages. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.glue}
		 */
		public static final String GLUE = "glue";
		/**
		 * Plugins. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.plugin}
		 */
		public static final String PLUGIN = "plugin";
		/**
		 * Tags. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.filter.tags}
		 */
		public static final String TAGS = "tags";
		/**
		 * Regexp for scenarios name. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.filter.name}
		 */
		public static final String NAME = "name";
		/**
		 * Only log scenarios, does not execute steps. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.execution.dry-run}
		 */
		public static final String DRY_RUN = "dryRun";

		/**
		 * Only log scenarios, does not execute steps. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.execution.dry-run}
		 */
		public static final String LOG = "log";
		/**
		 * Snippet type. See <a href="https://github.com/cucumber/cucumber-jvm/blob/main/cucumber-core/src/main/resources/io/cucumber/core/options/USAGE.txt">Usage</a> {@code cucumber.snippet-type}
		 */
		public static final String SNIPPETS = "snippets";
	}

	/**
	 * Cucumber internal use argument keys.
	 */
	static class InternalCucumberAndroidArgs {

		static final String CUCUMBER_ANDROID_TEST_CLASS = "cucumberAndroidTestClass";
	}

	/**
	 * Runner argument are linked to the {@link androidx.test.internal.runner.RunnerArgs}.
	 */
	private static class AndroidJunitRunnerArgs {

		/**
		 * {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_RUNNER_BUILDER}
		 */
		private static final String ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER = "runnerBuilder";
		/**
		 * {@link androidx.test.internal.runner.RunnerArgs#ARGUMENT_TEST_CLASS}
		 */
		private static final String ARGUMENT_ORCHESTRATOR_CLASS = "class";
	}

	private static final String TRUE = Boolean.TRUE.toString();
	private static final String FALSE = Boolean.FALSE.toString();
	@NonNull
	private final Bundle originalArgs;
	@Nullable
	private Bundle processedArgs;

	public CucumberAndroidJUnitArguments(@NonNull Bundle arguments) {
		this.originalArgs = new Bundle(arguments);
	}

	@NonNull
	public Bundle processArgs() {
		processedArgs = new Bundle(originalArgs);

		if (TRUE.equals(originalArgs.getString(PublicArgs.USE_DEFAULT_ANDROID_RUNNER, FALSE))) {
			return processedArgs;
		}

		processedArgs.putString(AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_RUNNER_BUILDER, CucumberJUnitRunnerBuilder.class.getName());

		String testClass = originalArgs.getString(AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_CLASS);
		if (testClass != null && !testClass.isEmpty()) {

			//if this runner is executed for single class (e.g. from orchestrator or spoon), we set
			//special option to let CucumberJUnitRunner handle this
			processedArgs.putString(InternalCucumberAndroidArgs.CUCUMBER_ANDROID_TEST_CLASS, testClass);
		}

		//there is no need to scan all classes - we can fake this execution to be for single class
		//because we delegate test execution to CucumberJUnitRunner
		processedArgs.putString(AndroidJunitRunnerArgs.ARGUMENT_ORCHESTRATOR_CLASS, CucumberJUnitRunnerBuilder.class.getName());

		return processedArgs;
	}

	@NonNull
	Bundle getRunnerArgs() {
		if (processedArgs == null) {
			processedArgs = processArgs();
		}
		return processedArgs;
	}

	/**
	 * Returns a Cucumber options {@link Map} where keys are supported properties<a href="https://github.com/cucumber/cucumber-jvm/tree/main/cucumber-core#properties-environment-variables-system-options">Properties, Environment variables, System Options</a>
	 *
	 * @return {@link Map}
	 */
	@NonNull
    Map<String, String> getCucumberOptions() {

		Bundle bundle = getRunnerArgs();
		Map<String, String> map = new HashMap<>(bundle.size());

		for (String key : bundle.keySet()) {
			if (PublicArgs.GLUE.equals(key)) {
				appendOption(map, Constants.GLUE_PROPERTY_NAME, bundle.getString(key));
			}
			else if (PublicArgs.PLUGIN.equals(key)) {
				appendOption(map, Constants.PLUGIN_PROPERTY_NAME, bundle.getString(key));
			}
			else if (PublicArgs.TAGS.equals(key)) {
				appendOption(map, Constants.FILTER_TAGS_PROPERTY_NAME, bundle.getString(key));
			}
			else if (PublicArgs.NAME.equals(key)) {
				appendOption(map, Constants.FILTER_NAME_PROPERTY_NAME, bundle.getString(key));
			}
			else if ((PublicArgs.DRY_RUN.equals(key) || PublicArgs.LOG.equals(key)) && getBooleanArgument(bundle, key)) {
				appendOption(map, Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, "true");
			}
			else if (PublicArgs.SNIPPETS.equals(key)) {
				appendOption(map, Constants.SNIPPET_TYPE_PROPERTY_NAME, bundle.getString(key));
			}
			else if (PublicArgs.FEATURES.equals(key)) {
				appendOption(map, Constants.FEATURES_PROPERTY_NAME, bundle.getString(key));
			}
			else if (key.startsWith("cucumber.")) {
				appendOption(map, key, bundle.getString(key));
			}
		}
		return map;
	}

    /**
     * Adds the given {@code optionKey} and its {@code optionValue} to the given {@code map}
     */
    private static void appendOption(Map<String, String> map, String optionKey, String optionValue) {
        map.put(optionKey, optionValue);
    }

    /**
     * Extracts a boolean value from the bundle which is stored as string.
     * Given the string value is "true" the boolean value will be {@code true},
     * given the string value is "false the boolean value will be {@code false}.
     * The case in the string is ignored. In case no value is found this method
     * returns false. In case the given {@code bundle} is {@code null} {@code false}
     * will be returned.
     *
     * @param bundle the {@link Bundle} to get the value from
     * @param key    the key to get the value for
     * @return the boolean representation of the string value found for the given key,
     * or false in case no value was found
     */
    private static boolean getBooleanArgument(Bundle bundle, String key) {

        if (bundle == null) {
            return false;
        }

        String tagString = bundle.getString(key);
        return tagString != null && Boolean.parseBoolean(tagString);
    }

    @Nullable
    String getTestClassAndMethod(){
        return getRunnerArgs().getString(InternalCucumberAndroidArgs.CUCUMBER_ANDROID_TEST_CLASS);
    }

    @Nullable
    String getOptionsAnnotationPackageLocation(){
        return getRunnerArgs().getString(PublicArgs.OPTIONS_ANNOTATION_LOCATION);
    }
}
