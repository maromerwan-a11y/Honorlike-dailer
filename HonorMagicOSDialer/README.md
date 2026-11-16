# Honor MagicOS Phone Dialer — Native Android Project (`ROLE_DIALER`)

This Android project packages the **Honor MagicOS Phone Dialer** into a native Android application capable of registering as the system **Default Phone App** (`android.app.role.DIALER`).

## Included Android System Capabilities
1. **Default Phone App Registration (`MainActivity.kt`)**:
   - Uses `RoleManager.ROLE_DIALER` on Android 10+ (API 29+) and `TelecomManager.ACTION_CHANGE_DEFAULT_DIALER` on Android 8–9.
   - Prompts the user on launch or when tapping **Set Default** in the dialer header.
2. **Mandatory `InCallService` (`HonorInCallService.kt` & `InCallActivity.kt`)**:
   - Android requires any Default Phone App to implement `android.telecom.InCallService` with `BIND_INCALL_SERVICE` and `IN_CALL_SERVICE_UI` metadata.
3. **Dual-SIM Calling (`AndroidDialerBridge`)**:
   - Routes `SIM 1` and `SIM 2` buttons directly to `TelecomManager.placeCall()` with the selected `PhoneAccountHandle`.

## How to Compile the Installable `.apk`

### Option 1: Build in Android Studio (Local PC/Mac)
1. Unzip `HonorMagicOSDialer_AndroidProject.zip` and open the folder in **Android Studio**.
2. Wait for Gradle Sync to finish, then select **Build > Build Bundle(s) / APK(s) > Build APK(s)** from the top menu.
3. Copy `app/build/outputs/apk/debug/app-debug.apk` to your Android phone, install it, and tap **Set as default** when prompted.

### Option 2: Build in the Cloud via GitHub Actions (No Android Studio Needed)
1. Upload these project files to any free GitHub repository.
2. Open the **Actions** tab in your repository and run the **Build Honor MagicOS Dialer APK** workflow.
3. Download the compiled `HonorMagicOSDialer-debug-apk` artifact directly to your phone and install it.
