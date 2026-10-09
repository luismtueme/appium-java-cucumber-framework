# Run guide

## Prerequisites

| Tool | Why |
|---|---|
| JDK 21+ | Compiles and runs the suite (`JAVA_HOME`) |
| Maven Wrapper | `./mvnw` downloads Maven 3.9 — no global Maven needed |
| Node.js 18+ | Installs Appium 2 |
| Android Studio / SDK | Emulator + platform tools for Android |
| Xcode (macOS) | Simulator for iOS |

## One-time setup

```bash
bash scripts/download-apps.sh
npm install -g appium
appium driver install uiautomator2
appium driver install xcuitest   # macOS only
```

Copy `.env.example` to `.env` if you want local overrides.

## Start Appium

```bash
appium --address 127.0.0.1 --port 4723
```

Keep that terminal open. The suite connects to `APPIUM_SERVER_URL` (default `http://127.0.0.1:4723`).

## Start a device

**Android:** open an AVD from Android Studio, or:

```bash
emulator -avd Pixel_6_API_30
```

**iOS (macOS):**

```bash
xcrun simctl boot "iPhone 16"
open -a Simulator
```

## Run

```bash
# No device — what every PR Checks job runs
./mvnw verify -DskipITs
./mvnw verify -Pcheck

# Full mobile + API suite
PLATFORM=android ./mvnw verify -DskipUnitTests=true

# Gherkin only / specs only
./mvnw verify -DskipUnitTests=true -Pcucumber-only
./mvnw verify -DskipUnitTests=true -Pspecs-only
```

Reports land in `target/cucumber-report.html` and `target/failsafe-reports/`.

## Worked example: add a scenario

1. Add a step-free claim to a feature under `src/test/resources/features/mobile/`.
2. Implement the step in `acceptance/steps`, calling a screen method.
3. If the locator is new, add it to a screen object under `framework/screens`.
4. Run `./mvnw verify -Pcheck` to confirm the step is glued exactly once.
5. Run the mobile suite on an emulator.

Mirror the same flow as a method in `specs/` if you want both styles.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `App binary not found` | `bash scripts/download-apps.sh` |
| Connection refused to `:4723` | Start Appium; check `APPIUM_SERVER_URL` |
| Session not created (Android) | Emulator booted? `adb devices` shows a device? UiAutomator2 installed? |
| Session not created (iOS) | Simulator booted? XCUITest driver installed? Correct `.app` path under `apps/`? |
| Spotless failed | `./mvnw spotless:apply` |
| ArchUnit / Gherkin lint failed | Read the rule message; fix the design or tag before merging |

## Quarantine

Tag a flaky scenario `@quarantine @jira:QA-123`. It leaves the main suite and runs in the non-blocking CI step until fixed.
