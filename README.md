[![Build Status](https://secure.travis-ci.org/cucumber/cucumber-android.svg)](http://travis-ci.org/cucumber/cucumber-android)

# Cucumber-Android

This project implements Android support for Cucumber-JVM. It allows running cucumber tests with Android Test Orchestrator and using sharding

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

4. Set instrumentation runner to `cucumber.api.android.CucumberAndroidJUnitRunner` or class that extends it
```groovy
android.defaultConfig.testInstrumentationRunner "cucumber.cukeulator.test.CukeulatorAndroidJUnitRunner"
```


### Debugging
Please read [the Android documentation on debugging](https://developer.android.com/tools/debugging/index.html).

### Examples

Currently there is one example in subproject [cukeulator](https://github.com/cucumber/cucumber-android/tree/master/cukeulator)

To create a virtual device and start an [Android emulator](https://developer.android.com/tools/devices/index.html):

```
$ANDROID_HOME/tools/android avd
```
