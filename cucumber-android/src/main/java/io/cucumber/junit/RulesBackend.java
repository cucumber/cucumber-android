package io.cucumber.junit;


import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.Glue;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;

public class RulesBackend implements Backend {

    private final ClassFinder classFinder;
    private final ObjectFactory objectFactory;
    private final List<TestRulesData> classesWithRules = new ArrayList<>();
    private TestRulesExecutor rulesExecutor;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Description description;

    RulesBackend(ClassFinder classFinder, ObjectFactory objectFactory) {
        this.classFinder = classFinder;
        this.objectFactory = objectFactory;
    }


    @Override
    public void loadGlue(Glue glue, List<URI> gluePaths) {
        for (URI path : gluePaths) {
            Collection<Class<?>> descendants = classFinder.getDescendants(Object.class, path);

            for (Class<?> clazz : descendants) {
                WithJunitRule annotation = clazz.getAnnotation(WithJunitRule.class);
                if (annotation!=null){
                    if (objectFactory.addClass(clazz)){
                        classesWithRules.add(new TestRulesData(annotation.useAsTestClassInDescription(), clazz, getAccessors(clazz)));
                    }
                }
            }
        }
    }

    private static List<TestRuleAccessor> getAccessors(Class<?> clazz) {
        List<TestRuleAccessor> accessors = new ArrayList<>(1);
        for (Method m : clazz.getMethods()) {
            Rule annotation = m.getAnnotation(Rule.class);
            if (annotation!=null){
                accessors.add(new MethodRuleAccessor(m, annotation.order()));
            }
        }
        for (Field f : clazz.getFields()) {
            Rule annotation = f.getAnnotation(Rule.class);
            if (annotation!=null){
                accessors.add(new FieldRuleAccessor(f, annotation.order()));
            }
        }
        return accessors;
    }

    @Override
    public void buildWorld() {

        List<TestRulesData> objects = new ArrayList<>(classesWithRules.size());
        for (TestRulesData clazzRules : classesWithRules) {
            Object instance = objectFactory.getInstance(clazzRules.getDeclaringClass());
            objects.add(new TestRulesData(clazzRules.useAsTestClassInDescription(), instance, clazzRules.getAccessors()));
        }
        rulesExecutor = new TestRulesExecutor(objects, executorService);
        rulesExecutor.startRules(description);
    }

    void setDescription(Description description) {
        this.description = description;
    }

    @Override
    public void disposeWorld() {
        rulesExecutor.stopRules();
        description = null;
    }

    @Override
    public List<String> getSnippet(PickleStep step, String keyword, FunctionNameGenerator functionNameGenerator) {
        return Collections.emptyList();
    }

    private static class FieldRuleAccessor implements TestRuleAccessor {
        final Field field;
        final int order;

        public FieldRuleAccessor(Field field, int order) {
            this.field = field;
            this.order = order;
        }

        @Override
        public TestRule getRule(Object obj) throws IllegalAccessException {
            return (TestRule) field.get(obj);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class MethodRuleAccessor implements TestRuleAccessor {
        final Method method;
        final int order;

        public MethodRuleAccessor(Method method, int order) {
            this.method = method;
            this.order = order;
        }

        @Override
        public TestRule getRule(Object obj) throws IllegalAccessException, InvocationTargetException {
            return (TestRule) method.invoke(obj);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }
}
