package io.cucumber.junit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use it to annotate class which contains Junit {@link org.junit.Rule}
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface WithJunitRule {

    /**
     * By default Cucumber treats feature as test class which is not real class
     * and cannot be treated as such in runtime.
     * E.g HiltAndroidRule relies on having test class passed to its constructor matching
     * that passed to {@link org.junit.runner.Description}
     * @return false by default, true if you want this class to be passed as test
     * class for its junit rules
     */
    boolean useAsTestClassInDescription() default false;

    /**
     * Tag expression. If the expression applies to the current scenario JUnit rule declared in class annotated by {@link WithJunitRule} will be used
     */
    String value() default "";
}
