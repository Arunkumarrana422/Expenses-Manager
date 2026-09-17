# Expense Manager

Native Kotlin Android Expense Manager using Jetpack Compose, MVVM and Firebase.

## Important
This project intentionally contains no HTML, CSS, JavaScript, React, Flutter or WebView.

## Open
Open the project folder in Android Studio. Let Gradle sync complete.

## Firebase
1. Create a Firebase project.
2. Add an Android app with package `com.example.expensemanager`.
3. Download `google-services.json` and place it in `app/`.
4. Enable Email/Password Authentication.
5. Create a Cloud Firestore database.
6. Apply `firestore.rules`.
7. Enable Storage/FCM if you extend profile photos and push notifications.

## Build APK
Android Studio: Build > Build APK(s).

Command line after a working Gradle installation:
`./gradlew assembleDebug`

## Build AAB
`./gradlew bundleRelease`

## Notes
The supplied source is a functional native starter with Firebase authentication, Firestore expense storage, room model, dashboard, expense list, reports, members and profile screens. Firebase-backed room membership, settlements, notifications, advanced filtering, Google Sign-In, profile photo upload and comprehensive automated tests should be completed before production release.
