# Contributing

## Before you open a PR

1. `./mvnw spotless:apply`
2. `./mvnw verify -DskipITs` — unit tests, Spotless check, ArchUnit, Gherkin lint, coverage
3. `./mvnw verify -Pcheck` — Cucumber dry run
4. If you changed mobile flows, run Android and/or iOS locally: host Appium (`npm` install) + emulator/simulator + `PLATFORM=… ./mvnw verify -DskipUnitTests=true`

The **Checks** job on GitHub Actions is the merge gate for design and lint. Android and iOS jobs exercise real devices the same way (host Appium, not Docker). See [docs/GUIDE.md](docs/GUIDE.md).

## Design rules (enforced)

- Screen objects do not assert
- Steps do not use locators (`By` / `findElement`) — call screens instead
- No `Thread.sleep`; no implicit waits outside `DriverFactory`
- Framework code in `src/main` does not depend on Cucumber or specs

## Tags

Allowed Gherkin tags are listed in `GherkinLint.ALLOWED_TAGS`. `@quarantine` requires `@jira:ABC-123`.
