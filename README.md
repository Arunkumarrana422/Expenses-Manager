# Expense Manager — Native Android Application

A complete, production-ready native Android Expense Manager application built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM architecture**, and **Firebase (Auth & Cloud Firestore)**.

Designed specifically for roommates, friends, family members, trip groups, and small teams to effortlessly track shared expenses, calculate exact equal shares and balances, minimize debts, and manage settlements in Indian Rupees (₹).

---

## 1. Application Overview
Expense Manager provides an end-to-end native Android experience. Users can create shared "Rooms" (e.g., *My Flat 402*, *Goa Trip*, *Family Home*), invite members with unique 6-character room codes (e.g., `RM8K2P`), log categorized expenses, and let the real-time engine calculate who owes whom with greedy debt minimization.

## 2. Key Features
- **Authentication**: Native email/password authentication, password reset, profile management, and persistent session state.
- **Room Management**: Create new rooms with cryptographic unique codes, copy/share codes, join existing rooms with duplicate membership prevention.
- **Room Switcher**: Belongs to multiple rooms simultaneously without data crosstalk.
- **Real-Time Calculations**:
  - Total Group Expenses ($\sum 	ext{expenses}$)
  - Equal Share ($	ext{Total} / N$)
  - Member Balances ($	ext{Paid} - 	ext{Equal Share}$)
  - Positive balance = Member gets back money
  - Negative balance = Member owes money
  - Zero = Member is settled
- **Greedy Debt Minimization**: Automatically generates minimal bilateral transactions (e.g. *Rahul $ightarrow$ Arun ₹500*).
- **Expense History**: Instant search, category filters, sorting (newest, oldest, highest, lowest amount), and date tracking.
- **Settlement Tracking**: Mark settlements as paid with audit timestamp and author logging.
- **Reports & Analytics**: Visual category spending breakdowns and member contribution matrices.
- **Offline First**: Firebase Firestore persistence enabled for zero-friction offline caching and automatic synchronization.
- **In-App & Push Notifications**: Firebase Cloud Messaging (FCM) integration with notifications for added, edited, deleted expenses and member joins.
- **Material 3 Design**: Supports Light Mode, Dark Mode, dynamic status bars, and rounded surfaces.

## 3. Technology Stack
- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose with Material 3 (Compose BOM 2024.06.00)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern + Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose (`NavHost`)
- **Backend & Auth**: Firebase Authentication, Cloud Firestore, Firebase Cloud Messaging (FCM)
- **Image & Avatar Loading**: Coil Compose
- **Build System**: Gradle 8.7 with Kotlin DSL (`build.gradle.kts`) and Version Catalogs (`libs.versions.toml`)

## 4. Android Studio Requirements
- Android Studio Iguana (2023.2.1) or Ladybug (2024.2.1+)
- JDK 17 or higher
- Android SDK 34 (Compile & Target SDK 34, Min SDK 24)

## 5. How to Open the Project
1. Extract `ExpenseManager-Android.zip`.
2. Launch **Android Studio**.
3. Select **Open** $ightarrow$ Navigate to the extracted `ExpenseManager` directory $ightarrow$ Click **OK**.
4. Allow Gradle to sync dependencies and index the project.

## 6. How to Connect Firebase
1. Visit the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project** and name it (e.g., `ExpenseManagerApp`).
3. Click **Add App** $ightarrow$ Select the **Android** icon.
4. Enter package name: `com.example.expensemanager`.
5. Download the `google-services.json` file.
6. Replace the placeholder `app/google-services.json` with your real downloaded file.

## 7. How to Configure Firebase Authentication
1. In Firebase Console, go to **Build** $ightarrow$ **Authentication**.
2. Click **Get Started** $ightarrow$ Enable the **Email/Password** sign-in provider.
3. Save the changes.

## 8. How to Configure Cloud Firestore
1. In Firebase Console, go to **Build** $ightarrow$ **Firestore Database**.
2. Click **Create database** $ightarrow$ Select your nearest region.
3. Start in **Production mode** or **Test mode**.

## 9. How to Configure Firestore Security Rules
1. In Firebase Console, open **Firestore Database** $ightarrow$ **Rules** tab.
2. Copy the contents of `firestore.rules` included in this project:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAuthenticated() {
      return request.auth != null;
    }
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && request.auth.uid == userId;
    }
    match /rooms/{roomId} {
      allow read: if isAuthenticated() && (
        resource.data.createdBy == request.auth.uid ||
        request.auth.uid in resource.data.memberIds
      );
      allow create: if isAuthenticated() && request.resource.data.createdBy == request.auth.uid;
      allow update: if isAuthenticated();
      allow delete: if isAuthenticated() && resource.data.createdBy == request.auth.uid;

      match /{subcollection=**} {
        allow read, write: if isAuthenticated();
      }
    }
  }
}
```
3. Click **Publish**.

## 10. How to Configure Firebase Cloud Messaging (FCM)
1. Go to **Project Settings** $ightarrow$ **Cloud Messaging**.
2. Push notification services are handled via `com.example.expensemanager.service.ExpenseMessagingService`.

## 11. How to Run Unit Tests
From the terminal inside the project:
```bash
./gradlew test
```
This runs the unit tests in `app/src/test/java/com/example/expensemanager/`:
- `ExpenseCalculatorTest.kt`: Tests total expense sum, equal share math, balance signs, and the Rahul $ightarrow$ Arun ₹500 greedy debt settlement algorithm.
- `RoomCodeGeneratorTest.kt`: Tests cryptographic uniqueness and formatting of 6-character room codes.
- `CurrencyUtilsTest.kt`: Verifies Indian Rupee (₹) formatting and decimal precision.

## 12. How to Build Debug APK
Run the following command in terminal:
```bash
./gradlew assembleDebug
```
The APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## 13. How to Build Release AAB (App Bundle for Google Play)
1. Generate a keystore file if you don't have one:
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
```
2. Build the bundle:
```bash
./gradlew bundleRelease
```
The AAB bundle will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

## 14. How to Run on Emulator or Physical Device
1. Connect your Android phone with USB Debugging enabled or start an Android Virtual Device (AVD) from Android Studio.
2. Click the green **Run** (Play) button in Android Studio or run:
```bash
./gradlew installDebug
```

## 15. Troubleshooting
- **Error: google-services.json is missing or invalid**: Ensure you replaced `app/google-services.json` with the file generated from your Firebase console.
- **Compilation error on JDK**: Set your Gradle JDK to JDK 17 under **Settings $ightarrow$ Build, Execution, Deployment $ightarrow$ Build Tools $ightarrow$ Gradle**.
- **Room Code invalid**: Verify that the 6-character uppercase code matches the code displayed in the room owner's screen.
