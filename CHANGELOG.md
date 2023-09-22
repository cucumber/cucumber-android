# Changelog

## [7.14.0-SNAPSHOT] - 2023-09-22


* update `cucumber-jvm` dependencies to `7.14.0`
  From now `cucumber-android` requires Java 8 api and Android API >= 26 or [desugaring](https://developer.android.com/studio/write/java8-support-table)

  For changes in behaviour of `cucumber-jvm` check https://github.com/cucumber/cucumber-jvm/tree/main/release-notes
* target android sdk version `34`
* rewritten most of classes to [Kotlin](https://kotlinlang.org/) and moved to package `io.cucumber.android`
* removed support for some of the arguments passed by instrumentation, check `CucumberAndroidJUnitArguments.PublicArgs` method for supported properties

### Added

* tag expression support in `@WithJunitRule` to execute particular rules in specific scenarios only

### Fixed

* [#118 @WithJunitRule executes rule for every scenario](https://github.com/cucumber/cucumber-android/issues/118)
* [#102 Testing reusable composables without activity not possible](https://github.com/cucumber/cucumber-android/issues/102)
* 

## [4.10.0] - 2023-02-17

### Added

* add `HiltObjectFactory`
* 
### Fixed
* [#111 Hilt gives me "Called inject() multiple times" exception](https://github.com/cucumber/cucumber-android/issues/111)

## [4.9.0] - 2021-11-16

### Added

* add support for Junit rules

### Fixed
* [#87 Jetpack Compose support](https://github.com/cucumber/cucumber-android/issues/87)
* [#81 Hilt support](https://github.com/cucumber/cucumber-android/issues/81)

## 4.8.2
* exclude kotlin generated classes for inlined functions from glue scanning
* does not fail if `Class.getMethods()` throws `NoClassDefFoundError`

## 4.8.1
* upgrade `cucumber-core` to `4.8.1`

## 4.7.4
* upgrade `cucumber-core` to `4.7.4`
* extract the 'create backend supplier' method into the factory.

## 4.6.0
* upgrade `cucumber-core` to `4.6.0`

## 4.5.4
* upgrade `cucumber-core` to `4.5.4`
* upgrade package names to match JVM project package names.

## 4.4.1
* [#43](https://github.com/cucumber/cucumber-android/issues/43) resolved by PR [#39](https://github.com/cucumber/cucumber-android/pull/39) - (Roman Havran)
 - option to run regular android junit tests with `CucumberAndroidJUnitRunner`

## 4.4.0
* upgrade `cucumber-core` to `4.4.0`
* upgrade `junit` to `4.13`

## 4.3.1
* upgrade `cucumber-core` to `4.3.1`
* properly create `JUnitOptions` to respect strict setting

## 4.3.0
* upgrade `cucumber-core` to `4.3.0`

## 4.2.5
### Fixed

* [#17](https://github.com/cucumber/cucumber-android/pull/17) - reports & rerun require TestRunFinishedEvent to be posted (kaskasi)

## 4.2.4
### Changed
From PR [#14](https://github.com/cucumber/cucumber-android/pull/14) (Viacheslav Iankovyi, Łukasz Suski)
  * set target sdk to `28`
  * migrate to `androidx` and `AndroidJunitRunner`
  * add support for Android Test Orchestrator and spoon sharding
  * ensure uniqueness of `<feature name>#<scenario name>`
    * each scenario outline example receives continues number starting from 1
    * if duplicate feature name or scenario in single feature is detected then error is thrown 
     
### Fixed
  * [#2](https://github.com/cucumber/cucumber-android/issues/2) - cucumber-android does not integrate very well with Android Orchestrator

## 4.2.2

 * upgrade cucumber-java to `4.2.2`

## 4.0.0

### Changed
 * migrate everything to Gradle
 
### Removed
  * android-studio sample - now [cukeulator](https://github.com/cucumber/cucumber-android/tree/master/cukeulator) is the only valid sample (for Gradle and Android Studio)
  * cukeulator-test and cucumber-android-test

### Fixed
 * [#5](https://github.com/cucumber/cucumber-android/issues/5) - Sample Does Not Work
 * [#4](https://github.com/cucumber/cucumber-android/issues/4) - Support for parallel cukes 
 * [#3](https://github.com/cucumber/cucumber-android/issues/3) - Reported duration time of scenario is about 0ms on Android  