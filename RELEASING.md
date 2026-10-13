# Release process for cucumber-android

## Prerequisites

To release `cucumber-android`, you'll need to be a member of the core team. The `Release` GitHub environment must provide `SONATYPE_USERNAME` and `SONATYPE_PASSWORD` with a Maven Central Portal user token, plus `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE` for artifact signing.

## Releasing cucumber-android

- Remove `-SNAPSHOT` in `build.gradle` `version =` entry
- Update `CHANGELOG.md` with the upcoming version number and create a new `Unreleased` section
- Remove empty sections from `CHANGELOG.md`
- Commit the changes preferably using a verified signature, and push to main branch
  ```shell
  git commit --gpg-sign -am "Release X.Y.Z"
  git push
  ```
- To trigger the release process, `git push` to a dedicated `release/` branch:
  ```shell
  git push origin main:release/vX.Y.Z
  ```
- Monitor the [Maven Central workflow](https://github.com/cucumber/cucumber-android/actions/workflows/release-gradle.yaml). Gradle signs all three libraries, then Nmcp uploads them together through the Maven Central Publisher API.
- Check the release has been successfully pushed to [Maven Central](https://search.maven.org/artifact/io.cucumber/cucumber-android)
