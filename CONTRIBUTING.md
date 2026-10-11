# Contributing

## Build

Requirements: JDK 17 or newer and an Android SDK with platform 37 and matching build-tools. Set `ANDROID_HOME` or add `sdk.dir=/path/to/sdk` to `local.properties`.

```
./gradlew assembleDebug
```

## Run tests

```
./gradlew testDebugUnitTest
```

The tests are JVM unit tests. They cover NDEF building and parsing, hex formatting, message size calculation and history summaries. They need no device and no NFC tag. Changes to NDEF code should come with tests.

Writing, locking and tag reading paths use `android.nfc` and have to be checked by hand on a device with real tags. Locking a tag is irreversible, so use a tag you can spare.

## Submit changes

1. Fork the repository and create a branch for one change.
2. Keep the change focused. Unrelated fixes go into separate pull requests.
3. Run the build and the unit tests before submitting.
4. Open a pull request and describe what changed and why.
5. User-visible text goes into `app/src/main/res/values/strings.xml`, not into Kotlin code.
6. New source files start with the SPDX header used in the existing files.

## License

By contributing you agree that your contribution is licensed under GPL-3.0-only, the same license as the project.
