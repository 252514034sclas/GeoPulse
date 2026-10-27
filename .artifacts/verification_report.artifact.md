# GeoPulse — Connected Device Runtime Verification Report

## 1. Project Inspection (`MainActivity`)
- Confirmed that [MainActivity.kt](file:///C:/Users/SCAD%20LAB/AndroidStudioProjects/GeoPulse/app/src/main/java/com/geopulse/app/MainActivity.kt) correctly acts as the launcher activity, starting [SplashActivity](class://com.geopulse.app.SplashActivity) via an explicit `Intent` and calling `finish()` immediately:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
```

## 2. Connected Device & ADB Status
- **Connected Device Name / ADB Status**: No device detected (`List of devices attached` returned empty).
- **Wireless Debugging / USB Status**: The physical device was not reachable via ADB at the time of execution.

## 3. Build & Installation Results
- **Build Result**: `app:assembleDebug` completed successfully.
- **Installation Result**: Skipped because no active device was detected by ADB.

## 4. Runtime & Logcat Observations
- **App Launch**: Not executed on physical device due to lack of an active ADB connection.
- **Logcat / Crashes**: No logcat entries or runtime exceptions generated.

## 5. Next Steps to Connect Device
If the device cannot be reached, follow these steps:
1. Ensure **Wireless Debugging** or **USB Debugging** is enabled in Developer Options on your physical Android device.
2. Connect the device via USB cable or pair via wireless debugging (`adb connect <IP_ADDRESS>:<PORT>`).
3. Verify connection status using `adb devices` until the status shows `device`.
4. Re-deploy the application via Android Studio or build tools.
