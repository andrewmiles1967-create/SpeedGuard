# SpeedGuard v0.1

Minimal Android proof-of-concept for GPS speed monitoring and future D-TRO speed-limit alerts.

## Current behaviour
- Proper native Android app (Kotlin).
- Start/Stop Drive.
- Uses phone GPS to show actual speed in mph.
- Foreground location service keeps monitoring while app is backgrounded.
- D-TRO provider boundary is already present.
- Until D-TRO data is connected, the legal limit deliberately displays `--`; the app never guesses a limit.
- Warning logic is already wired and activates when a trusted provider supplies a limit and GPS speed exceeds it.

## Add D-TRO later
Implement `DtroSpeedLimitProvider.limitFor()` in `SpeedLimitProvider.kt`, ideally against a locally cached, validated road-segment dataset. Do not put a D-TRO client secret in the APK.

## Build
Open the folder in a current Android Studio, let Gradle sync, then Build > Build APK(s).

Target: Android 8+ (API 26), target/compile API 35.

## Safety
This is an experimental driver-awareness aid, not a certified speedometer or authoritative statement of the legal limit. GPS speeds and road matching can be inaccurate. The driver remains responsible for obeying signs and road conditions.
