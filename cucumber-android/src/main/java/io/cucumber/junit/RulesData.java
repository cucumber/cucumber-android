package io.cucumber.junit;

import java.util.List;

class RulesData {

    private final WithJunitRule annotation;
    private final Class<?> declaringClass;
    private final Object declaringObject;
    private final List<RulesBackend.RuleAccessor> accessors;

    RulesData(WithJunitRule annotation, Class<?> declaringClass, List<RulesBackend.RuleAccessor> accessors) {
        this(annotation, declaringClass, null,accessors);
    }

    RulesData(WithJunitRule annotation, Object declaringObject, List<RulesBackend.RuleAccessor> accessors) {
        this(annotation, declaringObject.getClass(), declaringObject, accessors);
    }

    private RulesData(WithJunitRule annotation, Class<?> declaringClass, Object declaringObject, List<RulesBackend.RuleAccessor> accessors) {
        this.annotation = annotation;
        this.declaringClass = declaringClass;
        this.declaringObject = declaringObject;
        this.accessors = accessors;
    }

    public WithJunitRule getAnnotation() {
        return annotation;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public List<RulesBackend.RuleAccessor> getAccessors() {
        return accessors;
    }

    public Object getDeclaringObject() {
        return declaringObject;
    }
}
