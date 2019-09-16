Releasing
=========

The process of deploying to maven central has been automated based on 
the [Complete guide to continuous deployment to maven central from Travis CI](http://www.debonair.io/post/maven-cd/)
and will be executed whenever a non-snapshot version is committed.

It is preferable to use the automated deployment process over the manual process. However should travis.ci fail or should the 
need arise to setup another continuous integration system the [Manual deployment](#manual-deployment) section 
describes how this works.

## Check [![Build Status](https://travis-ci.org/cucumber/cucumber-android.svg?branch=master)](https://travis-ci.org/cucumber/cucumber-android) ##

Is the build passing?

```
git checkout master
```

## Prepare for release ##

Replace version numbers in:

* `build.gradle`
* `CHANGELOG.md`

Then run (replace X.Y.Z below with the next release number): 

```
git commit -am "Release X.Y.Z"
git tag vX.Y.Z
git push --tags
```
Travis will now deploy everything.

Finally update the version in `build.gradle` to the next snapshot version.


# Manual deployment #

It is preferable to use the automated deployment process over the manual process.

The deployment process of `cucumber-android` is based on 
[Deploying to OSSRH with Gradle](https://central.sonatype.org/pages/gradle.html).
This process is nearly identical for both snapshot deployments and releases. Whether a snapshot 
deployment or release is executed is determined by the version number.

To make a release you must have the `cukebot@cucumber.io` GPG private key imported in gpg2.

```
gpg --import devs-cucumber.io.key
```

Additionally upload privileges to the Sonatype repositories are required
or you can use cukebot credentials. See the
[OSSRH Guide](http://central.sonatype.org/pages/ossrh-guide.html) for
instructions. Then an administrator will have to grant you access to the
cucumber repository.

Finally both your OSSRH credentials and private key must be setup in
your `~/.gradle/gradle.properties` - for example:

```
ossrhUsername=sonatype-user-name
ossrhPassword=sonatype-password
signing.keyId=123456AB # cukebot@cucumber.io id
signing.password=*****
signing.secretKeyRingFile=/home/cukebot/.gnupg/secring.gpg
```


# Deploy the release #

```
"./gradlew clean build publishToNexus closeAndReleaseRepositoryIfRelease --stacktrace"
```

