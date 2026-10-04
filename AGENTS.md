# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android app (`app`) written primarily in Java, with XML layouts and SQLite persistence. Production code lives in `app/src/main/java/app/jjg/nanogym/`; group new screens by feature, such as `ventanasentrenar/`, `pesaje/`, or `calendario/`. Database helpers and transfer objects belong in `database/`.

UI resources are under `app/src/main/res/`: layouts in `layout/`, reusable backgrounds in `drawable/`, and strings, colors, dimensions, and themes in `values/`. SQL seed/migration assets are in `app/src/main/assets/`. Unit tests and device tests live in `app/src/test/` and `app/src/androidTest/`, respectively.

## Build, Test, and Development Commands

Run commands from the repository root on Windows:

```powershell
.\gradlew.bat :app:assembleDebug    # builds a debug APK
.\gradlew.bat :app:testDebugUnitTest # runs local JUnit tests
.\gradlew.bat :app:connectedDebugAndroidTest # runs device/emulator tests
```

Use Android Studio to run the `app` configuration on an emulator or physical device. The project uses compile SDK 35, min SDK 26, and Java 8 compatibility.

## Coding Style & Naming Conventions

Use four-space indentation in Java and XML. Keep Java packages lowercase and feature-oriented. Existing classes use names such as `ventanaPesaje`, `ConexionSQLite`, and `RutinasTL`; match the surrounding feature's convention rather than renaming unrelated code. Name layouts `activity_<screen>.xml`, drawables with lowercase underscores (for example, `halloween_action_button.xml`), and resource IDs descriptively.

Preserve existing view IDs and `android:onClick` method names when changing layouts: they are connected directly to activity code. Put user-facing text in `values/strings.xml` for new work where practical.

## Testing Guidelines

Add focused JUnit tests for non-Android logic under `app/src/test/java`; use `*Test.java` names. Add Espresso/instrumented coverage under `app/src/androidTest/java` for navigation, persistence, or view interactions. At minimum, run the applicable Gradle test task and `:app:assembleDebug` before submitting UI or database changes.

## Commit & Pull Request Guidelines

History uses short, descriptive messages, often with a scope/version prefix (for example, `Referencia - Version 1.4Beta` or `Update README.md`). Prefer an imperative summary such as `Add pesaje validation`. Keep commits focused. Pull requests should explain the user-visible change, list validation performed, link any relevant issue, and include screenshots for layout or visual changes.

## Configuration & Data Safety

Do not commit local SDK paths, keystores, database dumps, or generated `app/build/` output. Review SQLite schema and asset changes carefully because they affect existing user data.
