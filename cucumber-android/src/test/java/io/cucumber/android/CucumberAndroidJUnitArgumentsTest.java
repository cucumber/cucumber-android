package io.cucumber.android;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.cucumber.core.options.Constants;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class CucumberAndroidJUnitArgumentsTest {

   
    @Test
    public void handles_empty_bundle_gracefully() {

        // given
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(new Bundle());

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, Matchers.is(Collections.<String, String>emptyMap()));
    }

    @Test
    public void supports_glue_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("glue", "glue/code/path");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.GLUE_PROPERTY_NAME,"glue/code/path"));
    }


    @Test
    public void supports_plugin_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("plugin", "someFormat");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.PLUGIN_PROPERTY_NAME,"someFormat"));
    }

    @Test
    public void supports_tags_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("tags", "@someTag");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.FILTER_TAGS_PROPERTY_NAME,"@someTag"));
    }

    @Test
    public void supports_name_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("name", "someName");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.FILTER_NAME_PROPERTY_NAME,"someName"));
    }

    @Test
    public void supports_dryRun_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("dryRun", "true");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,"true"));
    }

    @Test
    public void supports_log_as_alias_for_dryRun_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("log", "true");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.EXECUTION_DRY_RUN_PROPERTY_NAME,"true"));
    }


    @Test
    public void supports_snippets_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("snippets", "someSnippet");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.SNIPPET_TYPE_PROPERTY_NAME,"someSnippet"));
    }

    @NonNull
    private static Matcher<Map<?, ?>> getMatcher(String optionKey,String optionValue) {
        Map<Object, Object> map = new HashMap<>(1);
        map.put(optionKey, optionValue);
        return is(map);
    }

    @Test
    public void supports_features_as_direct_bundle_argument() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("features", "someFeature");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        assertThat(cucumberOptions, getMatcher(Constants.FEATURES_PROPERTY_NAME,"someFeature"));
    }

    @Test
    public void supports_multiple_values() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("plugin", "Feature1, Feature2");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.PLUGIN_PROPERTY_NAME,"Feature1, Feature2"));
    }

    @Test
    public void supports_spaces_in_values() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString("name", "'Name with spaces'");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions,  getMatcher(Constants.FILTER_NAME_PROPERTY_NAME,"'Name with spaces'"));
    }

    @Test
    public void supports_cucumber_property() {

        // given
        Bundle bundle = new Bundle();
        bundle.putString(Constants.FEATURES_PROPERTY_NAME, "features/path.feature");
        CucumberAndroidJUnitArguments arguments = new CucumberAndroidJUnitArguments(bundle);

        // when
        Map<String, String> cucumberOptions = arguments.getCucumberOptions();

        // then
        assertThat(cucumberOptions, getMatcher(Constants.FEATURES_PROPERTY_NAME,"features/path.feature"));
    }

}
