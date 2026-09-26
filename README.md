# Ruler

A native, fullscreen Android ruler with millimetre/centimetre and inch ticks, draggable measurement markers, and manual calibration against a standard 85.60 mm payment card.

The display DPI is only an initial estimate: **calibrate before relying on measurements**. Drag the A/B markers to measure a distance. Use CM/IN to switch scale, and turn the phone for a longer landscape ruler.

## Build

Android Studio: open this repository and run the `app` module. CLI with Android SDK and Gradle 8.9: `gradle assembleDebug`.

GitHub Actions builds and unit-tests the debug APK, installs and launches it in an Android emulator, then publishes the APK as a workflow artifact. The debug APK is unsigned for release distribution but debug-signed for sideload installation.
