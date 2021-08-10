# Cukeulator Example Application

This is the example test-project for the Cukeulator app for Android Studio 3.0+

## Setup

Features must be placed in `androidTest/assets/features/`. Subdirectories are allowed.

The rest of the dependencies are added automatically in `cukeulator/build.gradle`.

The cucumber-android dependency is added as (see `cukeulator/build.gradle`):

    androidTestImplementation 'io.cucumber:cucumber-android:<version>'

## Building the APK files using gradle

To build the cukeulator apk:

    ./gradlew --parallel :cukeulator:assembleDebug

The build generates an apk in cukeulator/build/outputs/apk/debug/cukeulator-debug.apk.

To build the instrumentation test apk:

    ./gradlew --parallel :cukeulator:assembleDebugAndroidTest

## Installing the app and test on the Android device

To install the application apk on a device:

    adb install -r cukeulator/build/outputs/apk/debug/cukeulator-debug.apk

To install the test apk on a device:

    adb install -r cukeulator/build/outputs/apk/androidTest/debug/cukeulator-debug-androidTest.apk

To verify that the test is installed, run:

    adb shell pm list instrumentation

The command output should display;

    instrumentation:cucumber.cukeulator.test/.CukeulatorAndroidJUnitRunner (target=cucumber.cukeulator)

## Running the tests

There are two ways to run the test, either with Gradle or directly with adb.

- `gradlew`- is simpler, and more fail safe - but 10% slower. Note. Gradle deletes the APK files after use.
- `adb` - is more advanced, gives better output, and faster

To run the test using `gradlew`:

    ./gradlew :cukeulator:connectedCheck

To run the test using `adb`:

    // Requires that the apk files are already on the device - and not deleted by gradlew
    adb shell am instrument -w cucumber.cukeulator.test/cucumber.cukeulator.test.CukeulatorAndroidJUnitRunner

To find the output Cucumber report files, use this adb command:

    adb shell find /sdcard/ -name *cucumber*

To extract the Cucumber Report files, using `adb` into a folder called cucumber-device-reports. Remember to create this first.

    adb pull -a /sdcard/Android/data/cucumber.cukeulator/files/reports cucumber-device-reports

## Using an Android Studio IDE

1. Import the example to Android Studio: `File > Import Project`.
2. Create a test run configuration:
   1. Run > Edit Configurations
   2. Click `+` button and select Android Instrumented Tests
   3. Specify test name: `CalculatorTest`
   4. Select module: `cukeulator`
   5. Enter a Specific instrumentation runner: `cucumber.cukeulator.test/cucumber.cukeulator.test.CukeulatorAndroidJUnitRunner`
   6. Click Ok

### Output in Android Studio - logcat

Filter for the logcat tag `cucumber-android` in [DDMS](https://developer.android.com/tools/debugging/ddms.html).

### Using this project with locally built Cucumber-JVM

See the `dependencies` section in the [cukeulator/build.gradle](build.gradle) file.  
There is a source-code comment which explains how to use a locally built Cucumber-JVM Android library.
