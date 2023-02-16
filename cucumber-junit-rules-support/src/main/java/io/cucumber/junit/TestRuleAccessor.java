package io.cucumber.junit;

import org.junit.rules.TestRule;

import java.lang.reflect.InvocationTargetException;

public interface TestRuleAccessor {

	TestRule getRule(Object obj) throws IllegalAccessException, InvocationTargetException;

	int getOrder();
}
