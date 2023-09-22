package io.cucumber.java

import java.lang.reflect.Method
import java.util.function.BiConsumer

/**
 * Allows internal access to [MethodScanner]
 */
internal object MethodScannerWrapper {
    fun scan(aClass: Class<*>, consumer: BiConsumer<Method, Annotation>){
        MethodScanner.scan(aClass, consumer)
    }
}