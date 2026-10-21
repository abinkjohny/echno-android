# Echno Android

The native Android app for Echno, the construction management platform. It is a Kotlin and Jetpack
Compose client for the [Echno backend](https://github.com/tornotron/echno-backend): the backend owns
the data and the business rules, and the app signs people in, calls its REST API and shows the results.

## Status

Early development. What works today:

* **Sign in** with Keycloak (authorization code flow with PKCE) in a Custom Tab. The session is
  stored encrypted on the device, and the access token is refreshed when it expires.
* **Create an account**, with the same validation rules as the web app.
* **Choose an organization** when the user belongs to more than one, and switch later.
* **Profile:** the signed-in user's details and the organization they work in. This is the first
  screen after signing in for now.

Projects is next. It waits for a backend change that lets the app's tokens reach the project endpoints.

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation 3 |
| Dependency injection | Hilt |
| Networking | Retrofit, OkHttp, kotlinx.serialization |
| Sign-in | AppAuth (browser half), OkHttp (token calls) |
| Storage | DataStore, with tokens encrypted by an Android Keystore key |
| Logging | Timber |
| Tests | JUnit 4, kotlinx-coroutines-test, MockWebServer, Compose UI tests |
| Style | ktlint (Android Studio style), Android Lint |

Versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Getting started

### Prerequisites

* A recent [Android Studio](https://developer.android.com/studio) that supports Android Gradle
  Plugin 9.3. Gradle itself comes from the wrapper.
* JDK 25 for the Gradle daemon. Gradle downloads it if it isn't installed
  ([`gradle/gradle-daemon-jvm.properties`](gradle/gradle-daemon-jvm.properties)).
* Android SDK platform 37. The app runs on Android 9 (API 28) and newer.
* An emulator or a phone with USB debugging on.

### Build and run

```bash
git clone https://github.com/abinkjohny/echno-android.git
cd echno-android
./gradlew assembleStagingDebug      # or open the folder in Android Studio and press Run
```

Install with `./gradlew installStagingDebug`, or choose the `stagingDebug` build variant in
Android Studio's *Build Variants* panel.

### Build variants

Each environment is a product flavor with its own application id, so all three can be installed
side by side.

| Flavor | Application id | Backend | Keycloak issuer |
|---|---|---|---|
| `dev` | `org.echno.dev` | `http://10.0.2.2:8080/` | `http://10.0.2.2:8180/realms/echno-realm` |
| `staging` | `org.echno.staging` | `https://backend.echno.in/` | `https://auth.echno.in/realms/echno-realm` |
| `prod` | `org.echno` | not deployed yet | not deployed yet |

`10.0.2.2` is how the Android emulator reaches `localhost` on your computer, so `dev` talks to a
backend and Keycloak running locally ([backend README](https://github.com/tornotron/echno-backend#local-development)).
The `prod` URLs point at an `.invalid` domain until production exists, so a release build can never
talk to staging by mistake.

### Local overrides

Point a flavor somewhere else in `local.properties` (git-ignored), for example a phone on your
Wi-Fi reaching your laptop:

```properties
echno.dev.apiBaseUrl=http://192.168.1.20:8080/
echno.dev.authIssuer=http://192.168.1.20:8180/realms/echno-realm
```

The keys are `echno.<dev|staging>.apiBaseUrl` and `echno.<dev|staging>.authIssuer`. Only the `dev`
flavor may use plain HTTP, and only to the hosts it is configured for.

### Signing in

The app signs in through the `echno-android-client` client in Keycloak's `echno-realm`. Staging
already has it. For a local Keycloak, create it as described in
[docs/keycloak-client.md](docs/keycloak-client.md).

## Checks

These run before every pull request:

```bash
./gradlew ktlintCheck lintDevDebug testDevDebugUnitTest assembleDebug assembleProdRelease
./gradlew connectedDevDebugAndroidTest   # Compose UI tests; needs an emulator or phone
```

`./gradlew ktlintFormat` fixes most style findings.

## Project layout

```text
app/src/main/java/org/echno/
├── core/          shared by every feature: auth, network, session, design system, UI helpers
├── feature/       one package per feature, each split into data/, domain/ and ui/
└── navigation/    the routes and the navigation host
```

[docs/architecture.md](docs/architecture.md) explains the layers, how a request travels from a
screen to the backend, and how to add a feature.

## Contributing

Read [CONTRIBUTING.md](CONTRIBUTING.md) for the branch and commit rules, the checks and the review
process. Report security issues as described in [SECURITY.md](SECURITY.md), not in public issues.
