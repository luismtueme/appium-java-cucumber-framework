# Appium + Cucumber Automation Framework (Java)

Native **Android** and **iOS** test automation with [Appium](https://appium.io), [Cucumber](https://cucumber.io) and [JUnit](https://junit.org), using the Screen Object Model.

Write tests as **Gherkin scenarios**, **plain JUnit specs**, or both. Both runners share one `Config`, the same screen objects, and the same API client.

Stack: Java 21 · Appium Java Client 9 · Cucumber-JVM · Maven Wrapper.

[![Appium Tests](https://github.com/luismtueme/appium-java-cucumber-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/luismtueme/appium-java-cucumber-framework/actions/workflows/ci.yml)

New here? Follow the [run guide](docs/GUIDE.md) (setup → device → first run → troubleshooting).

## How this framework runs (important)

| Layer | What we use | Notes |
|---|---|---|
| Test runner | Maven on the host (`./mvnw verify`) | Always — never inside a container in CI |
| Appium server | **npm-installed Appium 2** on localhost `:4723` | Same approach locally and in GitHub Actions |
| Android device | Host AVD / Studio emulator, or CI `android-emulator-runner` | UiAutomator2 driver |
| iOS device | Host Simulator (macOS), or CI `simulator-action` | XCUITest driver |
| Demo app | Sauce Labs Mobile Sample App **2.7.1** | `scripts/download-apps.sh` |

**Docker is optional and not part of the main path.** CI does not use Docker. Locally you can start Appium with `docker compose up -d appium` instead of `npm install -g appium`, but the emulator/simulator and Maven suite still run on the host. See [Optional: Appium via Docker](#optional-appium-via-docker).

## Which repo should I use?

| Repository | Tests are written as | Stack | Choose it when |
|---|---|---|---|
| **appium-java-cucumber-framework** (this one) | Gherkin + JUnit specs | Java, Appium 2 | Native Android / iOS |
| [selenium-java-cucumber-framework](https://github.com/luismtueme/selenium-java-cucumber-framework) | Gherkin | Java, Selenium | Web UI, Selenium Grid |
| [playwright-cucumber-typescript-framework](https://github.com/luismtueme/playwright-cucumber-typescript-framework) | Gherkin + Playwright specs | TypeScript, Playwright | Web UI with BDD in TS |
| [playwright-typescript-framework](https://github.com/luismtueme/playwright-typescript-framework) | Playwright specs | TypeScript, Playwright | Web UI, engineers-only |
| [playwright-cucumber-automation-framework](https://github.com/luismtueme/playwright-cucumber-automation-framework) | Gherkin + Playwright specs | JavaScript, Playwright | Web UI with Cucumber, no TS |

## What's included

| Area | How it works |
|---|---|
| Screen objects | `framework/screens`: accessibility-id locators, user actions, explicit waits — no assertions |
| Dual runners | Cucumber features + JUnit `*IT` specs sharing screens and config |
| Platforms | `PLATFORM=android\|ios` → UiAutomator2 / XCUITest |
| Demo apps | Pinned Sauce sample binaries so a clone runs without your APK/IPA |
| API tests | REST Assured `ApiClient`; request/response attached to Cucumber with secrets masked |
| Configuration | One `Config` record; env / `.env` / defaults; bad values fail fast with the variable name |
| Waiting | Screens wait for elements; specs/steps use `Eventually` (Awaitility). No `Thread.sleep` / implicit waits (ArchUnit) |
| Failure evidence | Screenshot, page source, platform on failed `@mobile` scenarios |
| Reporting | `target/cucumber-report.html` + Failsafe JUnit XML |
| Quality gates | `-Werror`, Spotless, Gherkin lint, ArchUnit, unit coverage, Cucumber dry-run |
| Flaky tests | `@quarantine @jira:ABC-123` → separate non-blocking CI step |
| CI | Checks (no device) → Android emulator job → iOS simulator job |

## Quick start (recommended path)

Needs **JDK 21+**, **Node.js 18+**, and an **Android emulator** or **iOS simulator**.

```bash
# 1. Demo apps (Sauce Labs Mobile Sample App 2.7.1)
bash scripts/download-apps.sh

# 2. Appium 2 + drivers (once per machine)
npm install -g appium
appium driver install uiautomator2   # Android
appium driver install xcuitest       # iOS (macOS only)

# 3. Boot a device (separate terminal / Android Studio / Simulator)
#    Android: emulator -avd <your_avd>   |   iOS: xcrun simctl boot "iPhone 16"

# 4. Start Appium (separate terminal)
appium --address 127.0.0.1 --port 4723

# 5. Lint + unit tests (no device) — same as the CI Checks job
./mvnw verify -DskipITs
./mvnw verify -Pcheck

# 6. Full suite (Cucumber + JUnit specs) against the running emulator
PLATFORM=android ./mvnw verify -DskipUnitTests=true
```

Demo login credentials (built into defaults): `standard_user` / `secret_sauce`.

On Windows use `mvnw.cmd`. Reports: `target/cucumber-report.html`, `target/failsafe-reports/`.

## Optional: Appium via Docker

Use this only if you prefer not to install Appium with npm. It starts an Appium 2 **server** container; it does **not** replace the emulator, simulator, or Maven.

```bash
docker compose up -d appium
# still need a host emulator/simulator, then:
APPIUM_SERVER_URL=http://127.0.0.1:4723 PLATFORM=android ./mvnw verify -DskipUnitTests=true
```

Caveats: wiring ADB from the container to a host emulator can be fiddly (`host.docker.internal`); iOS Simulator cannot run in Linux Docker. **CI and the docs default to host Appium** for that reason.

## Testing your own application

1. Copy `.env.example` → `.env` and set at least:
   ```bash
   PLATFORM=android
   ANDROID_APP=/absolute/path/to/your.apk
   ANDROID_APP_PACKAGE=com.your.app
   ANDROID_APP_ACTIVITY=.SplashActivity
   ANDROID_APP_WAIT_ACTIVITY=.MainActivity
   APP_USERNAME=your-test-user
   APP_PASSWORD=your-test-password
   ```
   For iOS: `PLATFORM=ios`, `IOS_APP` (simulator `.app` bundle), `IOS_BUNDLE_ID`, and credentials.
2. Replace screen objects under `src/main/java/.../framework/screens/` and the features / steps / specs.
3. Stop relying on `apps/` once nothing points at the demo binaries.

Config precedence (first match wins): **`-D` system properties** → **environment variables** → **`.env`** → **`defaults.properties`**. Full list: [`.env.example`](.env.example).

## Configuration cheat sheet

| Variable | Default | Purpose |
|---|---|---|
| `PLATFORM` | `android` | `android` or `ios` |
| `APPIUM_SERVER_URL` | `http://127.0.0.1:4723` | Appium server |
| `ANDROID_APP` / `IOS_APP` | empty → demo apps | Path to your binary |
| `ANDROID_APP_PACKAGE` / `ACTIVITY` / `WAIT_ACTIVITY` | demo values | Android identity / post-launch wait |
| `IOS_BUNDLE_ID` | demo value | iOS identity |
| `DEVICE_NAME` / `PLATFORM_VERSION` / `DEVICE_UDID` | sensible / CI-set | Device targeting |
| `APP_USERNAME` / `APP_PASSWORD` | demo `standard_user` / `secret_sauce` | Login (required when using your own app) |
| `WAIT_TIMEOUT` | `15000` ms | Explicit waits (`45000` in Android CI) |
| `API_BASE_URL` | jsonplaceholder | `@api` scenarios |
| `TEST_ENV` | `local` | Environment label |

## Running tests

| Command | What it runs |
|---|---|
| `./mvnw verify -DskipITs` | Unit tests, Spotless, ArchUnit, Gherkin lint, coverage (no device) |
| `./mvnw verify -Pcheck` | Cucumber dry-run: every step defined exactly once |
| `./mvnw verify -DskipUnitTests=true` | Cucumber **and** JUnit mobile specs |
| `./mvnw verify -DskipUnitTests=true -Pcucumber-only` | Gherkin only |
| `./mvnw verify -DskipUnitTests=true -Pspecs-only` | JUnit specs only |
| `./mvnw verify -DskipUnitTests=true -Dcucumber.filter.tags="@Smoke"` | Tag filter (`@Smoke`, `@Regression`, `@mobile`, `@api`, `@quarantine`, `@jira:ABC-123`) |
| `./mvnw verify -Pquarantine` | Only `@quarantine` scenarios |
| `PLATFORM=ios ./mvnw verify -DskipUnitTests=true` | Same suite on iOS |
| `./mvnw spotless:apply` | Fix formatting |

## Project structure

```
├── src/main/java/io/github/luismtueme/framework/
│   ├── config/          # Config, Platform, Credentials
│   ├── driver/          # DriverFactory (AndroidDriver / IOSDriver)
│   ├── screens/         # Screen objects (BaseScreen, Login, Catalog, …)
│   └── api/             # ApiClient, exchange recording, masking
├── src/main/resources/config/defaults.properties
├── src/test/java/io/github/luismtueme/
│   ├── acceptance/      # Cucumber runner, hooks, steps, TestContext
│   ├── specs/           # Plain JUnit mobile specs (*IT.java)
│   ├── lint/            # Gherkin lint
│   └── ArchitectureTest.java / ConsistencyTest.java
├── src/test/resources/features/   # Gherkin: mobile/, api/
├── scripts/             # download-apps.sh, ci-android.sh
├── docker-compose.yml   # Optional Appium server only
└── .env.example
```

## Writing tests

### Gherkin

```gherkin
@mobile @Smoke
Feature: Login

  Scenario: Log in with valid credentials
    Given I am on the login screen
    When I log in with the configured credentials
    Then I see the products catalog
```

### JUnit spec (same screens)

```java
@Test
void logsInWithConfiguredCredentials() {
    Credentials credentials = config.requireCredentials();
    loginScreen.login(credentials.username(), credentials.password());
    Eventually.assertThat(() -> assertThat(catalogScreen.isDisplayed()).isTrue());
}
```

### Screen object

```java
public class LoginScreen extends BaseScreen {
    private static final By USERNAME = accessId("test-Username");
    private static final By PASSWORD = accessId("test-Password");
    private static final By LOGIN = accessId("test-LOGIN");

    public void login(String username, String password) {
        waitUntilLoaded();
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN);
    }
}
```

Expose user actions and visible-state reads; keep assertions out of screens.

## CI

`.github/workflows/ci.yml` on every PR and push to `main`:

| Job | What runs |
|---|---|
| **Checks** | Compile (`-Werror`), Spotless, unit tests + coverage, ArchUnit, Gherkin lint, consistency, Cucumber dry-run — **no device** |
| **Android** | Host Appium + UiAutomator2 + `aosp_atd` emulator; Cucumber + specs; quarantine (non-blocking) |
| **iOS** | Host Appium + XCUITest + simulator on `macos-15` |

Dependabot opens weekly PRs for Maven and GitHub Actions.

## License

MIT. See [LICENSE](LICENSE).
