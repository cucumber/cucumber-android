[![Build](https://github.com/cucumber/cucumber-android/actions/workflows/build.yaml/badge.svg)](https://github.com/cucumber/cucumber-android/actions/workflows/build.yaml)

# Cucumber-Android

This project implements Android support for Cucumber-JVM. It allows
running cucumber tests with Android Test Orchestrator and using
sharding. 

NOTE: Although minSdkVersion for 'cucumber-android' is 14 it requires
Java 7 language features and minimum Android API level 19. This is done
purposely to allow using cucumber in apps with lower minSdk (to avoid
compile errors) but tests should be run on devices with API >= 19

## Developers

### Prerequisites

This is ordinary multimodule Android project

* `cucumber-android` - main library
* `cukeulator` - sample application with instrumented tests

### Building

```sh
./gradlew assemble
```

### Setting up the dependency

The first step is to include cucumber-android into your project, for example, as a Gradle androidTestImplementation dependency:

```groovy
androidTestImplementation "io.cucumber:cucumber-android:$cucumberVersion"
```

### Using Cucumber-Android

1. Create a class in your test package (usually `<package name from AndroidManifest>.test` and add @CucumberOptions annotation to that class. This class doesn't need to have anything in it, but you can also put some codes in it if you want. The purpose of doing this is to provide cucumber options. A simple example can be found in `cukeulator`. Or a more complicated example here:
```java
@CucumberOptions(glue = "com.mytest.steps", format = {"junit:/data/data/com.mytest/JUnitReport.xml", "json:/data/data/com.mytest/JSONReport.json"}, tags = { "~@wip" }, features = "features")
public class MyTests 
{
}
```
glue is the path to step definitions, format is the path for report outputs, tags is the tags you want cucumber-android to run or not run, features is the path to the feature files.  
You can also use command line to provide these options to cucumber-android. Here is the detailed documentation on how to use command line to provide these options: [Command Line Options for Cucumber Android](https://github.com/cucumber/cucumber-jvm/pull/597)

2. Write your .feature files under your test project's assets/<features-folder> folder. If you specify features = "features" like the example above then it's assets/features.

3. Write your step definitions under the package name specified in glue. For example, if you specified glue = "com.mytest.steps", then create a new package under your src folder named "com.mytest.steps" and put your step definitions under it. Note that all subpackages will also be included, so you can also put in "com.mytest.steps.mycomponent".

4. Set instrumentation runner to `io.cucumber.android.runner.CucumberAndroidJUnitRunner` or class that extends it
```groovy
android.defaultConfig.testInstrumentationRunner "io.cucumber.android.runner.CucumberAndroidJUnitRunner"
```


### Debugging
Please read [the Android documentation on debugging](https://developer.android.com/tools/debugging/index.html).

### Examples

Currently there is one example in subproject [cukeulator](https://github.com/cucumber/cucumber-android/tree/master/cukeulator)

To create a virtual device and start an [Android emulator](https://developer.android.com/tools/devices/index.html):

```
$ANDROID_HOME/tools/android avd
```

## Junit rules support

Experimental support for Junit rules was added in version `4.9.0`.
Cucumber works differently than junit - you cannot just add rule to some steps class
because during scenario execution many such steps classes can be instantiated.
Cucumber has its own Before/After mechanism. If you have just 1 steps class then this could work
If you have many steps classes then it is better to separate rule and `@Before/@After` hooks  
 from steps classes

To let Cucumber discover that particular class has rules add
```
@WithJunitRule
class ClassWithRules {
    ...
}
```


### Jetpack Compose rule

```
@WithJunitRule
class ComposeRuleHolder {

    @get:Rule
    val composeRule = createEmptyComposeRule()
}
```

then inject this object in steps, e.g.
(can be also inject as `lateinit var` field (depending on injection framework used)

```
class KotlinSteps(val composeRuleHolder: ComposeRuleHolder, val scenarioHolder: ActivityScenarioHolder):SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

    ...
    
    @Then("^\"([^\"]*)\" text is presented$")
    fun textIsPresented(arg0: String) {
        onNodeWithText(arg0).assertIsDisplayed()
    }
}
```

### Hilt

Hilt requires to have rule in actual test class (which for cucumber is impossible
because there is no such class). To workaround that:

See https://developer.android.com/training/dependency-injection/hilt-testing#multiple-testrules
how to use hilt with other rules (like compose rule)

```
@WithJunitRule(useAsTestClassInDescription = true)
@HiltAndroidTest
class HiltRuleHolder {

    @Rule(order = 0) 
    @JvmField
    val hiltRule = HiltAndroidRule(this)

   //if you need it to be injected   
    @Inject
    lateinit var greetingService: GreetingService

    @Before
    fun init() {
        //if you have anything to inject here and/or used elsewhere in tests    
        hiltRule.inject()
    }

}
```
