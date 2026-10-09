# Run guide

This is the practical path for cloning the repo and running the demo suite. The [README](../README.md) has the feature overview and config tables.

## What you are starting

Three things must be up at once for mobile tests:

1. **Appium 2** listening on `http://127.0.0.1:4723` (default `APPIUM_SERVER_URL`)
2. A **booted Android emulator** or **iOS Simulator**
3. **Maven** running the suite on the same machine (`./mvnw`)

That is the same model GitHub Actions uses. Docker is not required; see [Optional Docker Appium](#optional-docker-appium) only if you want a containerized Appium **server**.

## Prerequisites

| Tool | Why |
|---|---|
| JDK 21+ | Compiles and runs the suite (`JAVA_HOME`) |
| Maven Wrapper | `./mvnw` downloads Maven — no global install needed |
| Node.js 18+ | Installs Appium 2 and drivers |
| Android Studio / SDK | Emulator + `adb` for Android |
| Xcode (macOS) | Simulator for iOS |

## One-time setup

```bash
bash scripts/download-apps.sh

npm install -g appium
appium driver install uiautomator2
appium driver install xcuitest   # macOS only
```

Demo binaries land under `apps/`:

- Android: `apps/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk`
- iOS: `apps/iOS.Simulator.SauceLabs.Mobile.Sample.app.2.7.1.app` (simulator `.app` bundle, not an IPA)

Optional: copy `.env.example` → `.env` for local overrides. Leave `ANDROID_APP` / `IOS_APP` empty to use the demo apps and built-in credentials (`standard_user` / `secret_sauce`).

## Start Appium (recommended: host install)

```bash
appium --address 127.0.0.1 --port 4723
```

Keep that terminal open. Confirm with `curl -s http://127.0.0.1:4723/status`.

### Optional: Docker Appium

```bash
docker compose up -d appium
```

This only replaces the Appium process. You still need a host emulator/simulator, and Maven still runs on the host. Prefer the npm install for day-to-day work and for matching CI. iOS Simulator cannot run inside Linux Docker.

## Start a device

**Android** — AVD from Android Studio, or:

```bash
emulator -avd Pixel_6_API_30
adb devices   # should list an `emulator-…` device
```

**iOS (macOS):**

```bash
xcrun simctl boot "iPhone 16"
open -a Simulator
```

## Run

```bash
# No device — same commands as the CI Checks job
./mvnw verify -DskipITs
./mvnw verify -Pcheck

# Full mobile + API suite (Appium + device required)
PLATFORM=android ./mvnw verify -DskipUnitTests=true

# Gherkin only / JUnit specs only
./mvnw verify -DskipUnitTests=true -Pcucumber-only
./mvnw verify -DskipUnitTests=true -Pspecs-only

# iOS
PLATFORM=ios ./mvnw verify -DskipUnitTests=true
```

| Output | Location |
|---|---|
| Cucumber HTML | `target/cucumber-report.html` |
| Failsafe XML | `target/failsafe-reports/` |
| Quarantine HTML (when run) | `target/cucumber-quarantine-report.html` |

## Worked example: add a scenario

1. Add a scenario under `src/test/resources/features/mobile/` (reuse existing steps when you can).
2. Implement any new step in `src/test/java/.../acceptance/steps/`, calling a **screen** method — not `By` / `findElement`.
3. If you need a new locator or action, add it to a screen under `src/main/java/.../framework/screens/`.
4. `./mvnw verify -Pcheck` — every step must be glued exactly once.
5. Run the mobile suite on an emulator/simulator.

Mirror the same flow as a method in `specs/` if you want both styles.

## Pointing at your own app

Set in `.env` (or the environment):

- Android: `ANDROID_APP`, `ANDROID_APP_PACKAGE`, `ANDROID_APP_ACTIVITY`, optional `ANDROID_APP_WAIT_ACTIVITY` (splash → main)
- iOS: `IOS_APP`, `IOS_BUNDLE_ID`
- Always when not using demo apps: `APP_USERNAME`, `APP_PASSWORD`

Then replace screens, features, steps, and specs. Config fails at startup if the binary path is missing or credentials are incomplete for a custom app.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `App binary not found` | `bash scripts/download-apps.sh` (iOS must be the `.app` under `apps/`, not a `Payload/` IPA tree) |
| Connection refused on `:4723` | Start Appium; check `APPIUM_SERVER_URL` |
| Session not created (Android) | Emulator booted? `adb devices`? `appium driver list` shows UiAutomator2? |
| Session not created (iOS) | Simulator booted? XCUITest installed? `DEMO_IOS_APP` / `IOS_APP` points at the `.app` bundle? |
| Login / empty text on RN screens | Prefer accessibility ids; for some labels use visible child text (see `BaseScreen.visibleText`) |
| Spotless failed | `./mvnw spotless:apply` |
| ArchUnit / Gherkin lint failed | Read the rule message; fix design or tags before merging |
| Android ANR / System UI dialog in CI | CI uses `aosp_atd` + ANR dismiss in `BaseScreen` (Android only) |

## Quarantine

Tag a flaky scenario `@quarantine @jira:QA-123`. It leaves the main suite and runs in the non-blocking CI quarantine step until fixed.

## CI mirror (what GitHub runs)

1. **Checks** — `./mvnw verify -DskipITs` then `./mvnw verify -Pcheck` (no Appium, no device)
2. **Android** — npm Appium + UiAutomator2 + emulator (`scripts/ci-android.sh`)
3. **iOS** — npm Appium + XCUITest + simulator on `macos-15`

No Docker in CI.
