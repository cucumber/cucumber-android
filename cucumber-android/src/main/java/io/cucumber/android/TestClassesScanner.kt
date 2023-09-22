package io.cucumber.android

import android.annotation.SuppressLint
import android.app.Instrumentation
import androidx.test.internal.runner.ClassPathScanner

@SuppressLint("RestrictedApi")
internal class TestClassesScanner(instrumentation: Instrumentation) {

    private val classPathEntries by lazy {
        ClassPathScanner(ClassPathScanner.getDefaultClasspaths(instrumentation)).classPathEntries
    }

    fun getClassesFromRootPackages(filter: (fqn:String) -> Boolean): List<Class<*>> {
        return classPathEntries.mapNotNull { className -> if (filter(className)) Class.forName(className) else null }
    }

}