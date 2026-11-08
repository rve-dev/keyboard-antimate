# Antimate Keyboard

An experimental Android keyboard with opt-in Russian word-replacement suggestions.

> Type naturally. Choose a softer alternative when you want one.

## Current MVP

- Android input method service (IME)
- Russian on-screen keyboard
- Optional replacement suggestions (for example, `пиздец → пипец`)
- Suggestions are never applied automatically
- Simple setup screen for enabling and selecting the keyboard

## Project status

Early prototype. Number/symbol layouts, shift state, real T9 predictions, settings, tests, and accessibility polish are not implemented yet.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35, allow Gradle sync, then run the `app` configuration to build and install it.

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy

The current prototype processes text locally. It does not send typed text to a server or use a cloud AI service.

## License

License has not been selected yet.
