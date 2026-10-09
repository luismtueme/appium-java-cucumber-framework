# Changelog

## 1.0.0

- Initial Appium 2 + Java 21 framework for native Android and iOS
- Dual runners: Cucumber Gherkin scenarios and plain JUnit specs sharing screen objects
- Screen Object Model with accessibility-id locators and ArchUnit design rules
- ApiClient with masked exchange recording for `@api` scenarios
- Demo apps: Sauce Labs Mobile Sample App 2.7.1 via `scripts/download-apps.sh`
- CI: Checks (lint, unit, dry-run) on every PR; Android emulator and iOS simulator jobs (host Appium via npm — Docker not used in CI)
- Optional `docker compose` Appium server for local use only; docs clarify the recommended host-Appium path
