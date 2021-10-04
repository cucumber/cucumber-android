package io.cucumber.junit;


import android.util.Pair;

import androidx.annotation.NonNull;

import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import cucumber.runtime.CucumberException;

class RulesExecutor {

    private final List<RulesData> rulesHolders;
    private CountDownLatch wrappedStatementLatch = new CountDownLatch(1);
    private CountDownLatch rulesExecutionLatch = new CountDownLatch(1);
    private ExecutorService executorService;
    private Future<?> rulesFuture;

    RulesExecutor(List<RulesData> rulesHolders, ExecutorService executorService) {
        this.rulesHolders = rulesHolders;
        this.executorService = executorService;
    }

    void startRules(Description description) {
        if (rulesHolders.isEmpty()) {
            return;
        }
        AtomicReference<Throwable> throwable = new AtomicReference<>();
        try {
            List<Pair<Integer,TestRule>> rulesWithOrders = new ArrayList<>(rulesHolders.size());
            for (RulesData rulesData : rulesHolders) {
                Object obj = rulesData.getDeclaringObject();
                List<RulesBackend.RuleAccessor> accessors = rulesData.getAccessors();

                for (RulesBackend.RuleAccessor accessor : accessors) {
                    TestRule rule = getTestRule(rulesData, obj, accessor);
                    rulesWithOrders.add(Pair.create(accessor.getOrder(),rule));
                }
            }
            List<TestRule> rules = getTestRules(rulesWithOrders);

            rulesFuture = executorService.submit(getTask(description, throwable, rules));
            rulesExecutionLatch.await();
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
        if (throwable.get()!=null){
            throw new CucumberException(throwable.get());
        }
    }

    @NonNull
    private Runnable getTask(Description description, AtomicReference<Throwable> throwable, List<TestRule> rules) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    RunRules runRules = new RunRules(new Statement() {
                        @Override
                        public void evaluate() throws Throwable {
                            rulesExecutionLatch.countDown();
                            wrappedStatementLatch.await();
                        }
                    }, rules, description);
                    runRules.evaluate();
                } catch (Throwable t) {
                    if (rulesExecutionLatch.getCount() != 0) {
                        //exception on rule
                        rulesExecutionLatch.countDown();
                    }
                    throwable.set(t);
                }
            }
        };
    }

    private TestRule getTestRule(RulesData rulesData, Object obj, RulesBackend.RuleAccessor accessor) throws IllegalAccessException, InvocationTargetException {
        TestRule rule = accessor.getRule(obj);
        if (rulesData.getAnnotation().useAsTestClassInDescription()){
            TestRule finalRule = rule;
            rule = (base, description1) -> finalRule.apply(base, Description.createTestDescription(rulesData.getDeclaringClass(), description1.getMethodName()));
        }
        return rule;
    }

    @NonNull
    private static List<TestRule> getTestRules(List<Pair<Integer, TestRule>> rulesWithOrders) {
        //noinspection ComparatorCombinators
        Collections.sort(rulesWithOrders,(o1, o2) -> o1.first.compareTo(o2.first) );
        List<TestRule> rules = new ArrayList<>(rulesWithOrders.size());

        for (Pair<Integer, TestRule> rule : rulesWithOrders) {
            rules.add(rule.second);
        }
        return rules;
    }

    void stopRules() {
        if (rulesFuture == null) {
            return;
        }
        wrappedStatementLatch.countDown();
        try {
            rulesFuture.get();
        } catch (ExecutionException e) {
            throw new CucumberException(e);
        } catch (InterruptedException e) {
            throw new CucumberException(e);
        }
    }
}
