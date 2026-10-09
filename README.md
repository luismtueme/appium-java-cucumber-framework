# Appium + Cucumber Automation Framework (Java)

Native Android and iOS test automation with [Appium](https://appium.io), [Cucumber](https://cucumber.io) and [JUnit](https://junit.org), following the Screen Object Model.

Write tests as **Gherkin scenarios** (Cucumber), as **plain JUnit specs**, or both. The two runners share one configuration, the same screen objects and the same API client.

Built on Java 21, Appium Java Client 9 and Cucumber-JVM. It follows the same design as [selenium-java-cucumber-framework](https://github.com/luismtueme/selenium-java-cucumber-framework) and the Playwright siblings, adapted for native mobile.

New here? Start with the [run guide](docs/GUIDE.md): setup, running, a worked example and troubleshooting.

[![Appium Tests](https://github.com/luismtueme/appium-java-cucumber-framework/actions/workflows/ci.yml/badge.svg)](https://github.com/luismtueme/appium-java-cucumber-framework/actions/workflows/ci.yml)

## Which repo should I use?

This is the mobile member of the same framework family. They share the design and documentation style.

| Repository | Tests are written as | Stack | Choose it when |
|---|---|---|---|
| **appium-java-cucumber-framework** (this one) | Gherkin scenarios and JUnit specs | Java, Appium 2 | Native Android / iOS automation |
| [selenium-java-cucumber-framework](https://github.com/luismtueme/selenium-java-cucumber-framework) | Gherkin scenarios | Java, Selenium WebDriver | Web UI in Java, Selenium Grid, WebDriver tooling |
| [playwright-cucumber-typescript-framework](https://github.com/luismtueme/playwright-cucumber-typescript-framework) | Gherkin scenarios and Playwright specs | TypeScript, Playwright | Web UI with BDD in TypeScript |
| [playwright-typescript-framework](https://github.com/luismtueme/playwright-typescript-framework) | Playwright specs | TypeScript, Playwright | Web UI, engineers-only tests |
| [playwright-cucumber-automation-framework](https://github.com/luismtueme/playwright-cucumber-automation-framework) | Gherkin scenarios and Playwright specs | JavaScript, Playwright | Web UI with Cucumber without TypeScript |

## What's included

| Area | How it works |
|---|---|
| Screen objects | `src/main/.../screens`: accessibility-id locators and user actions, explicit waits, no assertions. ArchUnit fails the build if a screen asserts or a step uses a locator |
| Dual runners | Cucumber features in `features/` and JUnit specs in `specs/`, sharing screens and config |
| Platforms | Android (UiAutomator2) and iOS (XCUITest) via `PLATFORM=android\|ios` |
| Demo apps | Pinned Sauce Labs Mobile Sample App binaries (`scripts/download-apps.sh`) so clones pass out of the box |
| API tests | `ApiClient` on REST Assured. Every request and response is attached to the Cucumber report with passwords and tokens masked |
| Configuration | One `Config` record for everything. Secrets come from environment variables or `.env`, never from committed files. Invalid values fail at startup with the variable name |
| Waiting | Screen objects wait for elements; steps/specs retry assertions on changing state with `Eventually` (Awaitility). No `Thread.sleep` and no implicit waits, enforced by ArchUnit |
| Failure evidence | Screenshot, page source and platform attached to every failed `@mobile` scenario |
| Reporting | Cucumber HTML report for Gherkin; JUnit XML for both runners |
| Quality gates | `-Werror` compile, Spotless formatting, Gherkin lint, ArchUnit design rules, framework unit tests with a coverage threshold, a Cucumber dry run (every step defined exactly once), all required to merge |
| Flaky tests | Tag `@quarantine` (with a ticket): it runs in a separate, non-blocking CI step |
| CI | GitHub Actions: Checks on every PR (no device), then Android emulator + iOS simulator jobs |
| Docker | `docker compose up -d appium` starts an Appium 2 server for local runs |

## Quick start

Requires **JDK 21+**, **Node.js 18+** (for Appium), and an **Android emulator** or **iOS simulator**.

```bash
# 1. Demo apps
bash scripts/download-apps.sh

# 2. Appium 2 + drivers
npm install -g appium
appium driver install uiautomator2   # Android
appium driver install xcuitest       # iOS (macOS)

# 3. Start Appium (separate terminal)
appium --address 127.0.0.1 --port 4723

# 4. Unit tests + lint (no device)
./mvnw verify -DskipITs

# 5. Full suite against a running emulator (Android)
PLATFORM=android ./mvnw verify -DskipUnitTests=true
```

On Windows, use `mvnw.cmd` in Command Prompt or PowerShell.

## Testing your own application

1. Copy `.env.example` to `.env` and set at least:
   ```bash
   PLATFORM=android
   ANDROID_APP=/absolute/path/to/your.apk
   ANDROID_APP_PACKAGE=com.your.app
   ANDROID_APP_ACTIVITY=.SplashActivity
   ANDROID_APP_WAIT_ACTIVITY=.MainActivity
   APP_USERNAME=your-test-user
   APP_PASSWORD=your-test-password
   ```
   For iOS, set `PLATFORM=ios`, `IOS_APP`, `IOS_BUNDLE_ID`, and credentials.
2. Replace the screen objects in `src/main/java/.../framework/screens/` and the features / steps / specs with your own.
3. Stop using the demo binaries under `apps/` once nothing points at them.

## Configuration

Settings are read in this order, first match wins: **`-D` system properties**, **environment variables**, **`.env`**, then **`src/main/resources/config/defaults.properties`**. Every variable is listed in [`.env.example`](.env.example).

| Variable | Default | Purpose |
|---|---|---|
| `PLATFORM` | `android` | `android` or `ios` |
| `APPIUM_SERVER_URL` | `http://127.0.0.1:4723` | Appium server |
| `ANDROID_APP` / `IOS_APP` | empty (demo apps) | Path to your app binary |
| `ANDROID_APP_PACKAGE` / `ANDROID_APP_ACTIVITY` / `ANDROID_APP_WAIT_ACTIVITY` | demo values | Android identity / wait activities when using your app |
| `IOS_BUNDLE_ID` | demo value | iOS identity when using your app |
| `DEVICE_NAME` / `PLATFORM_VERSION` | sensible defaults | Emulator / simulator |
| `APP_USERNAME` / `APP_PASSWORD` | demo credentials for the sample app only | Login |
| `WAIT_TIMEOUT` | `15000` ms | Explicit waits |
| `API_BASE_URL` | `https://jsonplaceholder.typicode.com` | API scenarios |
| `TEST_ENV` | `local` | Environment label |

## Running tests

| Command | What it runs |
|---|---|
| `./mvnw verify -DskipITs` | Unit tests, Spotless, ArchUnit, Gherkin lint, coverage gate (no device) |
| `./mvnw verify -Pcheck` | Cucumber dry run: every step is defined exactly once |
| `./mvnw verify -DskipUnitTests=true` | Cucumber scenarios **and** JUnit mobile specs |
| `./mvnw verify -DskipUnitTests=true -Pcucumber-only` | Only Gherkin scenarios |
| `./mvnw verify -DskipUnitTests=true -Pspecs-only` | Only JUnit specs |
| `./mvnw verify -DskipUnitTests=true -Dcucumber.filter.tags="@Smoke"` | Scenarios by tag. Allowed tags: `@Smoke`, `@Regression`, `@mobile`, `@api`, `@quarantine`, `@jira:ABC-123` |
| `./mvnw verify -Pquarantine` | Only `@quarantine` scenarios |
| `PLATFORM=ios ./mvnw verify -DskipUnitTests=true` | Same suite on iOS |
| `./mvnw spotless:apply` | Fixes formatting |

## Project structure

```
├── src/main/java/io/github/luismtueme/framework/
│   ├── config/          # Config, Platform, Credentials
│   ├── driver/          # DriverFactory (AndroidDriver / IOSDriver)
│   ├── screens/         # Screen objects: BaseScreen, LoginScreen, CatalogScreen
│   └── api/             # ApiClient, ExchangeRecorder, Masking
├── src/main/resources/config/defaults.properties
├── src/test/java/io/github/luismtueme/
│   ├── acceptance/      # Cucumber: RunCucumberIT, hooks, steps, TestContext
│   ├── specs/           # Plain JUnit mobile specs (*IT.java)
│   ├── lint/            # GherkinLint and FeatureFilesTest
│   ├── ArchitectureTest.java
│   └── ConsistencyTest.java
├── src/test/resources/features/   # Gherkin: mobile/, api/
├── scripts/download-apps.sh
├── Dockerfile, docker-compose.yml
└── .env.example
```

## Writing tests

### A Gherkin scenario

```gherkin
@mobile @Smoke
Feature: Login

  Scenario: Log in with valid credentials
    Given I am on the login screen
    When I log in with the configured credentials
    Then I see the products catalog
```

### A JUnit spec (same screens)

```java
@Test
void logsInWithConfiguredCredentials() {
    Credentials credentials = config.requireCredentials();
    loginScreen.login(credentials.username(), credentials.password());
    Eventually.assertThat(() -> assertThat(catalogScreen.isDisplayed()).isTrue());
}
```

### A screen object

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

Expose user actions and reads of what the user sees; keep assertions out of screen objects.

## CI

`.github/workflows/ci.yml` runs on every PR and push to `main`:

| Job | Runs |
|---|---|
| **Checks** | Compile with warnings as errors, Spotless, framework unit tests with a coverage threshold, ArchUnit rules, Gherkin lint, version consistency, Cucumber dry run — **required on every PR**, no device |
| **Android** | Appium + UiAutomator2 + emulator; Cucumber + JUnit specs; quarantined scenarios (non-blocking) |
| **iOS** | Appium + XCUITest + simulator on `macos-15` |

Dependabot opens weekly update PRs for Maven, GitHub Actions and Docker.

## License

MIT. See [LICENSE](LICENSE).
