package io.cucumber.java

import io.cucumber.core.backend.Glue
import io.cucumber.core.backend.Lookup
import java.lang.reflect.Method

/**
 * Allows internal access to [GlueAdaptor]
 */
internal class GlueAdaptorWrapper(lookup:Lookup, glue: Glue) {

    private val glueAdaptor = GlueAdaptor(lookup, glue)

    fun addDefinition(method: Method, annotation: Annotation){
        glueAdaptor.addDefinition(method, annotation)
    }
}