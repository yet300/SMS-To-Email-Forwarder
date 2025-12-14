# SMS-To-Email Forwarder

Android application that listens for incoming SMS messages and forwards them to a configured email inbox. The UI is built with Jetpack Compose & Decompose, while background delivery is handled by a long-lived foreground service to minimize the chances of the OS stopping it.

## Features
- Compose-based UI with Decompose navigation and bottom sheets.
- Settings stored in Jetpack DataStore (`ForwarderSettings`).
- Foreground service with notification that shows the number of forwarded SMS and provides a quick stop action.
- Copy-to-clipboard action in Snackbars for quick sharing of stack traces with developers.
- Warning blocks reminding users not to enter credentials for third parties.
- Koin DI configured via annotations, using `@KoinViewModel`, `@Singleton`, and `@Inject`.

## Architecture
- **UI**: Jetpack Compose, Decompose components (`RootComponent`, `HomeComponent`).
- **State**: `ForwarderViewModel` exposes `StateFlow` consumed by components.
- **DI**: Koin annotation processing (`@Module`, `@Singleton`) + JSR330 interoperability.
- **Background**: `ForwarderService` foreground service with stop action & notification.
- **Data layer**: `SettingsStore` (DataStore Preferences) + SMTP email sender (JavaMail).

## Requirements
- Android Studio Koala+ with AGP 8.1x and Kotlin 2.2+.
- Android device/emulator with API 24+.
- Valid SMTP credentials for forwarding emails.
- SMS permission granted at runtime (RECEIVE_SMS, FOREGROUND_SERVICE, POST_NOTIFICATIONS on Android 13+).

## Getting Started
1. Clone the repo:
   ```bash
   git clone https://github.com/yet/forwarder.git
   cd forwarder
   ```
2. Import into Android Studio (use bundled JDK 17).
3. Configure `local.properties` (if needed) for SDK paths.
4. Build:
   ```bash
   ./gradlew assembleDebug
   ```
5. Install on device/emulator and grant SMS & notification permissions on first launch.

## Local Testing Tips
- Use services like [Mailtrap](https://mailtrap.io/), [Ethereal Email](https://ethereal.email/), or local SMTP docker images for safe SMTP testing.
- Fill in SMTP host, port, username, password, and recipient email in the app.
- Tap “Send Test” to verify connectivity; errors will be shown via Snackbar with a copy action.

## Foreground Service Behavior
- Once “Start” is pressed, `ForwarderService` enters foreground mode showing a persistent notification.
- Notification displays the number of forwarded SMS and has an action button to stop the service.
- Android 14+ foreground requirements are handled by passing `ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC` when starting the service.

## Permissions
- **RECEIVE_SMS**: Required to intercept incoming SMS.
- **FOREGROUND_SERVICE & FOREGROUND_SERVICE_DATA_SYNC**: Needed for long-running service.
- **POST_NOTIFICATIONS**: Required on Android 13+ so the foreground service notification can be shown.

## Contributing
1. Fork & create a feature branch.
2. Ensure Gradle build passes: `./gradlew assembleDebug`.
3. Submit a PR describing the changes (preferably with screenshots/logs for UI or service updates).

## License
Apache License 2.0 — see [`LICENSE`](LICENSE) for details.
