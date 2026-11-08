# Antimate Keyboard

An experimental Android keyboard with configurable appearance and optional softer-word replacement.

## Current prototype

- Android input method service (IME)
- Russian letter layout and a basic number/symbol layout
- System Android sans-serif font
- Themes: System, Light, Dark, AMOLED, Ocean, Crimson and Forest
- Settings screen with independent toggles
- Optional local dictionary-based automatic soft-word replacement
- Optional replacement suggestions (the original text is preserved unless selected)
- Haptic feedback and key-sound preferences
- Keyboard shortcut to open settings

## Not implemented yet

Clipboard history, T9 predictions, glide typing, voice input, handwriting, translation, GIF/sticker search, full multilingual layout switching and cloud AI replacement are not implemented in this prototype.

## AI replacement integration

A `SoftWordProvider` interface is included as an extension point. No API endpoint or key is configured. Do not embed a private API key in the APK. A future AI fallback should use a protected backend and only send text after clear user opt-in.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35, allow Gradle sync, then run the `app` configuration.

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy

The current prototype processes replacement words locally. It does not send typed text to a server.

## License

License has not been selected yet.
