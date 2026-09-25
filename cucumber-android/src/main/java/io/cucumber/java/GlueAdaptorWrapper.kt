package io.cucumber.java

import io.cucumber.core.backend.Glue
import io.cucumber.core.backend.Lookup
import java.lang.reflect.Method

/**
 * Allows internal access to [GlueAdaptor]
 */
internal class GlueAdaptorWrapper(private val lookup:Lookup, private val glue: Glue) {

    private val glueAdaptor = GlueAdaptor(lookup, glue)

    fun addDefinition(method: Method, annotation: Annotation){
        val annotationType: Class<out Annotation?> = annotation.annotationClass.java
        if (annotationType.getAnnotation(StepDefinitionAnnotation::class.java) != null) {
            val expression = expression(annotation, annotationType)
            glue.addStepDefinition(JavaStepDefinition(method, expression, lookup))
        } else {
            glueAdaptor.addDefinition(method, annotation)
        }
    }

    private fun expression(annotation: Annotation, annotationType: Class<*>): String {
        try {
            val expressionMethod: Method = annotationType.getDeclaredMethod("value")
            return expressionMethod.invoke(annotation) as String
        } catch (e: NoSuchMethodException) {
            // Should never happen.
            throw IllegalStateException(e)
        }
    }
}