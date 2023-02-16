package io.cucumber.junit;

import java.util.List;

public class TestRulesData {

    private final Class<?> declaringClass;
    private final Object declaringObject;
    private final List<TestRuleAccessor> accessors;
    private boolean useAsTestClassInDescription;

    public TestRulesData(boolean useAsTestClassInDescription, Class<?> declaringClass, List<TestRuleAccessor> accessors) {
        this(useAsTestClassInDescription, declaringClass, null,accessors);
    }

    public TestRulesData(boolean useAsTestClassInDescription, Object declaringObject, List<TestRuleAccessor> accessors) {
        this(useAsTestClassInDescription, declaringObject.getClass(), declaringObject, accessors);
    }

    private TestRulesData(boolean useAsTestClassInDescription, Class<?> declaringClass, Object declaringObject, List<TestRuleAccessor> accessors) {
        this.useAsTestClassInDescription = useAsTestClassInDescription;
        this.declaringClass = declaringClass;
        this.declaringObject = declaringObject;
        this.accessors = accessors;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public List<TestRuleAccessor> getAccessors() {
        return accessors;
    }

    public Object getDeclaringObject() {
        return declaringObject;
    }

    public boolean useAsTestClassInDescription() {
        return useAsTestClassInDescription;
    }
}
