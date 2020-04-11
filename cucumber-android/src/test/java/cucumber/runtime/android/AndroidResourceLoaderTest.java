package cucumber.runtime.android;

import android.content.Context;
import android.content.res.AssetManager;
import com.google.common.collect.Lists;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AndroidResourceLoaderTest {

    private final Context context = mock(Context.class);
    private final AssetManager assetManager = mock(AssetManager.class, RETURNS_SMART_NULLS);
    private final MultiLoader multiLoader = mock(MultiLoader.class, RETURNS_SMART_NULLS);
    private final Resource fileResource1 = mock(Resource.class, RETURNS_SMART_NULLS);
    private final Resource fileResource2 = mock(Resource.class, RETURNS_SMART_NULLS);
    private Iterable<Resource> fileResources = mock(Iterable.class, RETURNS_SMART_NULLS);
    private Iterator<Resource> fileResourcesIterator = mock(Iterator.class, RETURNS_SMART_NULLS);
    private final AndroidResourceLoader androidResourceLoader = new AndroidResourceLoader(context, multiLoader);

    @Before
    public void beforeEachTest() {
        when(context.getAssets()).thenReturn(assetManager);
    }

    @Test
    public void retrieves_resource_by_given_path_and_suffix() {

        // given
        final URI path = URI.create("file:some/path/some.feature");
        final String suffix = "feature";

        // when
        final List<Resource> resources = Lists.newArrayList(androidResourceLoader.resources(path, suffix));

        // then
        assertThat(resources.size(), is(1));
        assertThat(resources.get(0).getPath(), is(path));
    }

    @Test
    public void retrieves_resources_recursively_from_given_path() throws IOException {

        // given
        final String path = "file:dir";
        final String dir = "dir";
        final String dirFile = "dir.feature";
        final String subDir = "subdir";
        final String subDirFile = "subdir.feature";
        final String suffix = "feature";

        when(assetManager.list(dir)).thenReturn(new String[]{subDir, dirFile});
        when(assetManager.list(dir + "/" + subDir)).thenReturn(new String[]{subDirFile});

        // when
        final List<Resource> resources = Lists.newArrayList(androidResourceLoader.resources(URI.create(path), suffix));

        // then
        assertThat(resources.size(), is(2));
        assertThat(resources, hasItem(withPath(path + "/" + dirFile)));
        assertThat(resources, hasItem(withPath(path + "/" + subDir + "/" + subDirFile)));
    }

    @Test
    public void only_retrieves_those_resources_which_end_the_specified_suffix() throws IOException {

        // given
        final String dir = "dir";
        String path = "file:" + dir;
        final String expected = "expected.feature";
        final String unexpected = "unexpected.thingy";
        final String suffix = "feature";
        when(assetManager.list(dir)).thenReturn(new String[]{expected, unexpected});

        // when
        final List<Resource> resources = Lists.newArrayList(androidResourceLoader.resources(URI.create(path), suffix));

        // then
        assertThat(resources.size(), is(1));
        assertThat(resources, hasItem(withPath(path + "/" + expected)));
    }

    @Test
    public void retrieves_file_resources_with_an_absolute_path() throws IOException {
        // given
        final String path = "file:/dir";
        final String dir = "dir";
        final String dirFile = "dir.feature";
        final String subDir = "subdir";
        final String subDirFile = "subdir.feature";
        final String suffix = "feature";

        when(fileResource1.getPath()).thenReturn(URI.create(path + "/" + dirFile));
        when(fileResource2.getPath()).thenReturn(URI.create(path + "/" + subDir + "/" + subDirFile));
        when(fileResourcesIterator.hasNext()).thenReturn(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        when(fileResourcesIterator.next()).thenReturn(fileResource1, fileResource2);
        when(fileResources.iterator()).thenReturn(fileResourcesIterator);
        when(multiLoader.resources(URI.create(path), suffix)).thenReturn(fileResources);

        // when
        final List<Resource> resources = Lists.newArrayList(androidResourceLoader.resources(URI.create(path), suffix));

        // then
        assertThat(resources.size(), is(2));
        assertThat(resources, hasItem(withPath(path + "/" + dirFile)));
        assertThat(resources, hasItem(withPath(path + "/" + subDir + "/" + subDirFile)));
    }

    @Test
    public void only_retrieves_android_resources_with_a_relative_path() throws IOException {
        // given
        final String dir = "dir";
        String absPath = "file:/" + dir;
        String relPath = "file:" + dir;
        final String expected = "expected.feature";
        final String unexpected = "unexpected.feature";
        final String suffix = "feature";

        when(assetManager.list(dir)).thenReturn(new String[]{expected});
        when(fileResource1.getPath()).thenReturn(URI.create(absPath + "/" + unexpected));
        when(fileResourcesIterator.hasNext()).thenReturn(Boolean.TRUE, Boolean.FALSE);
        when(fileResourcesIterator.next()).thenReturn(fileResource1);
        when(fileResources.iterator()).thenReturn(fileResourcesIterator);
        when(multiLoader.resources(URI.create(absPath), suffix)).thenReturn(fileResources);

        // when
        final List<Resource> resources = Lists.newArrayList(androidResourceLoader.resources(URI.create(relPath), suffix));

        // then
        assertThat(resources.size(), is(1));
        assertThat(resources, hasItem(withPath(relPath + "/" + expected)));
    }

    @Test
    public void only_retrieves_file_resources_with_an_absolute_path() throws IOException {
        // given
        final String dir = "dir";
        String absPath = "file:/" + dir;
        final String expected = "expected.feature";
        final String unexpected = "unexpected.feature";
        final String suffix = "feature";

        when(fileResource1.getPath()).thenReturn(URI.create(absPath + "/" + expected));
        when(fileResourcesIterator.hasNext()).thenReturn(Boolean.TRUE, Boolean.FALSE);
        when(fileResourcesIterator.next()).thenReturn(fileResource1);
        when(fileResources.iterator()).thenReturn(fileResourcesIterator);
        when(multiLoader.resources(URI.create(absPath), suffix)).thenReturn(fileResources);
        when(assetManager.list(dir)).thenReturn(new String[]{unexpected});

        // when
        final List<Resource> resources = Lists.newArrayList(androidResourceLoader.resources(URI.create(absPath), suffix));

        // then
        assertThat(resources.size(), is(1));
        assertThat(resources, hasItem(withPath(absPath + "/" + expected)));
    }

    private static Matcher<? super Resource> withPath(final String path) {
        return new TypeSafeMatcher<Resource>() {
            @Override
            protected boolean matchesSafely(final Resource item) {
                return item.getPath().toString().equals(path);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("resource with path: " + path);
            }
        };
    }
}