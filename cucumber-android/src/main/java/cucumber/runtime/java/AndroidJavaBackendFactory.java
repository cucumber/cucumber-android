package cucumber.runtime.java;

import java.util.Arrays;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.DefaultTypeRegistryConfiguration;
import cucumber.runtime.Env;
import cucumber.runtime.Reflections;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.stepexpression.TypeRegistry;

/**
 * This factory is responsible for creating the {@see JavaBackend} with dex class finder.
 */
public class AndroidJavaBackendFactory {
    public static BackendSupplier createBackend(RuntimeOptions runtimeOptions, ClassFinder classFinder, ObjectFactory delegateObjectFactory, Backend additional) {
        return () -> {
            final Reflections reflections = new Reflections(classFinder);
            final TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class,
                    runtimeOptions.getGlue(), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
            final TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
            typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
            return Arrays.asList(new JavaBackend(delegateObjectFactory, classFinder, typeRegistry), additional);
        };
    }

    public static ObjectFactory getDelegateObjectFactory(ClassFinder classFinder) {
        return ObjectFactoryLoader.loadObjectFactory(classFinder,
                JavaBackend.getObjectFactoryClassName(Env.INSTANCE), JavaBackend.getDeprecatedObjectFactoryClassName(Env.INSTANCE));
    }
}
