[![Build](https://github.com/cucumber/cucumber-android/actions/workflows/build.yaml/badge.svg)](https://github.com/cucumber/cucumber-android/actions/workflows/build.yaml)

# Cucumber-Android

This project implements Android support for Cucumber-JVM. It allows
running cucumber tests with Android Test Orchestrator and using
sharding. 

NOTE: Although minSdkVersion for `cucumber-android` is 14 it requires
Java 8 language features and minimum Android API level 26. This is done
purposely to allow using cucumber in apps with lower minSdk (to avoid
compile errors) but tests should be run on devices with API >= 26. 
However with desugaring enabled it may work in some configurations on lower API levels assuming that desugaring covers all the Java 8 api.
Not all features from `cucumber-jvm` are supported in `cucumber-android` due to differences in Android vs JDK (especially junit and html plugins which requires xml factory classes not available in Android)

## Developers

### Prerequisites

This is ordinary multimodule Android project

* `cucumber-android` - main library
* `cucumber-android-hilt` - Hilt object factory
* `cucumber-junit-rules-support` - internal module for Junit rules support
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

1. Create a class in your testApplicationId package (usually it's a `namespace` from `build.gradle` with `.test` suffix) and add `@CucumberOptions` annotation to that class. You can also put such class in different package or have many such classes in different packages but then you have to provide path to it in instrumentation argument `optionsAnnotationPackage`.

Gradle example:
```groovy
android {
    defaultConfig {
        testInstrumentationRunner "io.cucumber.android.runner.CucumberAndroidJUnitRunner"
        testInstrumentationRunnerArguments(optionsAnnotationPackage: "some.other.package")
    }
}
```

Commandline example:
```
adb shell am instrument -w -e optionsAnnotationPackage some.other.package com.mycompany.app.test/com.mycompany.app.test.MyTests
```

This class doesn't need to have anything in it, but you can also put some codes in it if you want. The purpose of doing this is to provide cucumber options. A simple example can be found in `cukeulator`. Or a more complicated example here:
```java
package com.mycompany.app.test;

@CucumberOptions(glue = { "com.mytest.steps" }, tags = "~@wip" , features = { "features" })
public class MyTests 
{
}
```
glue is the list of packages which contain step definitions classes and also classes annotated with `@WithJunitRule`, tags is the tags placed above scenarios titles you want cucumber-android to run or not run, features is the path to the feature files in android test assets directory.

2. Write your .feature files under your project's android test  `assets/<features-folder>` folder. If you specify `features = "features"` in `@CucumberOptions` like the example above then it's `androidTest/assets/features` (might be also `androidTest<Flavor/BuildType>/assets/features`).

3. Write your step definitions under the package name specified in glue. For example, if you specified `glue = ["com.mytest.steps"]`, then create a new package under your `androidTest/java` (or `androidTest/kotlin`) named `com.mytest.steps` and put your step definitions under it. Note that all subpackages will also be included, so you can also put in `com.mytest.steps.mycomponent`.

4. Set instrumentation runner to `io.cucumber.android.runner.CucumberAndroidJUnitRunner` or class that extends it
```groovy
android.defaultConfig.testInstrumentationRunner "io.cucumber.android.runner.CucumberAndroidJUnitRunner"
```

If needed you can specify some cucumber options using instrumentation arguments. Check available options in `io.cucumber.android.CucumberAndroidJUnitArguments.PublicArgs` class 

For example to specify glue package use:
```groovy
android.defaultConfig.testInstrumentationRunnerArguments(glue: "com.mytest.steps")
```

### Debugging
Please read [the Android documentation on debugging](https://developer.android.com/tools/debugging/index.html).

### Examples

Currently there is one example in subproject [cukeulator](https://github.com/cucumber/cucumber-android/tree/master/cukeulator)

To create a virtual device and start an [Android emulator](https://developer.android.com/tools/devices/index.html):

```
$ANDROID_HOME/tools/android avd
```

### Junit rules support

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

and put this class in glue package. Glue packages are specified in `@CucumberOptions` annotation, see [Using Cucumber-Android](#using-cucumber-android)

You can specify tag expression like `@WithJunitRule("@MyTag")` to control for which scenarios this rule should be executed. See `compose.feature` and `ComposeRuleHolder` for example


### Sharding and running with Android Test Orchestrator

`CucumberAndroidJUnitRunner` works with Android Test Orchestrator and sharding because it reports tests and classes as feature names and scenario names like `My feature#My scenario` and is able to parse `-e class` argument from instrumentation. 
It also supports multiple names in `-e class` argument separated by comma. This means that feature and scenario name cannot have comma in it's name because it is reserved for separating multiple names (only if you want to use Orchestrator or in general `class` argument, for other use cases comma is allowed).


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

Check [Junit rules support](#junit-rules-support) for more information of adding classes with JUnit rules

### Hilt

There are 2 solutions for using Hilt with Cucumber:

##### 1. HiltObjectFactory

Add dependency:
```groovy
androidTestImplementation "io.cucumber:cucumber-android-hilt:$cucumberVersion"
```

Don't use any other dependency with `ObjectFactory` like `cucumber-picocontainer`

`HiltObjectFactory` will be automatically used as `ObjectFactory`.

To inject object managed by Hilt into steps or hook or any other class managed by Cucumber:

```kotlin
@HiltAndroidTest
class KotlinSteps(
    val composeRuleHolder: ComposeRuleHolder,
    val scenarioHolder: ActivityScenarioHolder
):SemanticsNodeInteractionsProvider by composeRuleHolder.composeRule {

    @Inject
    lateinit var greetingService:GreetingService

    @Then("I should see {string} on the display")
    fun I_should_see_s_on_the_display(s: String?) {
       Espresso.onView(withId(R.id.txt_calc_display)).check(ViewAssertions.matches(ViewMatchers.withText(s)))
    }

}
```

Such class:
- must have `@HiltAndroidTest` annotation to let Hilt generate injecting code
- can have Cucumber managed objects like hooks injected in constructor
- can have Cucumber managed objects injected in fields but such objects have to be annotated with `@Singleton` annotation and constructor has to be annotated with `@Inject` annotation 
- can have Hilt managed objects injected using field injection or constructor
- can have objects injected in base class

Also:
after each scenario Hilt will clear all objects and create new ones (even these marked as @Singleton) (like it does for each test class in Junit)

##### 2. @WithJunitRule


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

then you can inject such class to steps class using Cucumber dependency injector (like picocontainer) 



### Running scenarios from IDE

There is third-party plugin (not related with Cucumber organisation and this repository) which allows running scenarios directly from Android Studio or Intellij

[Cucumber for Kotlin and Android](https://plugins.jetbrains.com/plugin/22107-cucumber-for-kotlin-and-android)


## Troubleshooting

1. Compose tests fails

`java.lang.IllegalStateException: Test not setup properly. Use a ComposeTestRule in your test to be able to interact with composables`

##### Solution

Check [Jetpack Compose rule](#jetpack-compose-rule) section. Make sure that your class with `@WithJunitRule` annotation is placed in glue package as described in [Using Cucumber-Android](#using-cucumber-android)