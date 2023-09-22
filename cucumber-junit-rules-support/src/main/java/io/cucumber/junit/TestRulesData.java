package io.cucumber.junit;

import java.util.List;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

public class TestRulesData {

    private final Class<?> declaringClass;
    private final Object declaringObject;
    private final List<TestRuleAccessor> accessors;
    private boolean useAsTestClassInDescription;
    private Expression tagExpression;

    public TestRulesData(boolean useAsTestClassInDescription, Class<?> declaringClass, List<TestRuleAccessor> accessors, String tagExpression) {
        this(useAsTestClassInDescription, declaringClass, null, accessors, parse(tagExpression));
    }

    private static Expression parse(String tagExpression) {
        return TagExpressionParser.parse(tagExpression);
    }

    public TestRulesData(boolean useAsTestClassInDescription, Object declaringObject, List<TestRuleAccessor> accessors, Expression tagExpression) {
        this(useAsTestClassInDescription, declaringObject.getClass(), declaringObject, accessors, tagExpression);
    }

    public TestRulesData(boolean useAsTestClassInDescription, Object declaringObject, List<TestRuleAccessor> accessors, String tagExpression) {
        this(useAsTestClassInDescription, declaringObject.getClass(), declaringObject, accessors, parse(tagExpression));
    }

    private TestRulesData(boolean useAsTestClassInDescription, Class<?> declaringClass, Object declaringObject, List<TestRuleAccessor> accessors, Expression tagExpression) {
        this.useAsTestClassInDescription = useAsTestClassInDescription;
        this.declaringClass = declaringClass;
        this.declaringObject = declaringObject;
        this.accessors = accessors;
        this.tagExpression = tagExpression;
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

    public Expression getTagExpression() {
        return tagExpression;
    }
}
