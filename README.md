# ShaktiSetu

ShaktiSetu is an Android safety application focused on emergency response and personal security. It provides fast SOS activation, emergency contact workflows, live location sharing, and safety utilities like fake calls and evidence capture.

## Key Features

- **One-tap SOS flow** with countdown and PIN-based dismiss
- **Emergency SMS alerts** with live OpenStreetMap location links
- **Live SOS status sync** to Firebase Firestore
- **Automatic evidence capture** (photo + audio) during SOS
- **Emergency contacts management** using local Room database
- **Fake call simulation** for unsafe situations
- **Quick emergency dialing** (police/ambulance/women helpline)
- **Push notifications** using Firebase Cloud Messaging
- **Authentication** with email/password and Google sign-in

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Android Views/ViewBinding (hybrid)
- **Architecture components:** AndroidX Lifecycle
- **Local storage:** Room
- **Backend services:** Firebase Auth, Firestore, Storage, Cloud Messaging
- **Location & maps:** Google Play Services Location, osmdroid (OpenStreetMap)
- **Camera/media:** CameraX, MediaRecorder
- **Build system:** Gradle (Kotlin DSL)

## Requirements

- Android Studio (latest stable recommended)
- Android SDK:
  - `compileSdk = 35`
  - `minSdk = 29`
  - `targetSdk = 35`
- JDK 21 (project uses Gradle toolchain resolution)
- Firebase project configured for Android

## Project Setup

1. **Clone the repository** and open it in Android Studio.
2. **Firebase setup**
   - Create/choose a Firebase project.
   - Register Android app with package name: `com.example.shaktisetu`.
   - Add your `google-services.json` to:
     - `/tmp/workspace/harsharma-me/ShaktiSetu/app/google-services.json`
3. **Maps/API configuration**
   - Add your Google Maps API key in `AndroidManifest.xml` (if required for your flows).
4. **Sync Gradle** from Android Studio.
5. **Run on a real device** (recommended for SMS/call/location/camera behavior).

## Build and Run

From project root:

```bash
./gradlew assembleDebug
```

Install and run from Android Studio, or use adb with the generated APK.

## Useful Gradle Commands

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

## Permissions Used

The app requests permissions for:

- Internet/network access
- Location (fine/coarse)
- SMS and phone calls
- Camera and audio recording
- Notifications
- Vibration

Grant runtime permissions to use full emergency functionality.

## Important Notes

- This app is safety-critical; test emergency flows carefully before production use.
- Some features (SMS/call/location/background behavior) work best on physical devices.
- Firebase rules and production hardening should be reviewed before public release.

## Package Name

`com.example.shaktisetu`

---

If you want, I can also add sections for screenshots, architecture diagram, contribution guide, and release checklist.
