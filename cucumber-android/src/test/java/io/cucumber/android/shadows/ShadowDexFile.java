package io.cucumber.android.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import dalvik.system.DexFile;

@Implements(DexFile.class)
public class ShadowDexFile {

    private static Enumeration<String> entries = Collections.emptyEnumeration();

    /** @noinspection MethodMayBeStatic*/
    @Implementation
    public Enumeration<String> entries() {
        return entries;
    }


    public static void setEntries(Collection<String> classes) {
        entries = Collections.enumeration(classes);
    }

    @Resetter
    public static void reset() {
        entries = Collections.emptyEnumeration();
    }
}
