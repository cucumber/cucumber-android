# Release process for cucumber-android

## Prerequisites

To release `cucumber-android`, you'll need to be a member of the core team

## Automatic release process

See [.github/RELEASING](https://github.com/cucumber/.github/blob/main/RELEASING.md).

## Manual release process

- Remove `-SNAPSHOT` in `build.gradle` `version =` entry
- Update `CHANGELOG.md` with the upcoming version number and create a new `In Git` section
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
- Monitor the [github workflow](https://github.com/cucumber/cucumber-android/actions/workflows/release.yml)
- Check the release has been successfully pushed to [Maven Central](https://search.maven.org/artifact/io.cucumber/cucumber-android)