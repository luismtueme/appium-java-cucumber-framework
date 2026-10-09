#!/usr/bin/env bash
# Invoked as a single command by reactivecircus/android-emulator-runner (one sh -c argv).
set -euo pipefail

echo "Waiting for Android settings service..."
for _ in $(seq 1 60); do
  if adb shell service check settings 2>/dev/null | grep -q "found"; then
    echo "settings service ready"
    break
  fi
  sleep 2
done

./mvnw -B verify -DskipUnitTests=true
# Quarantine is non-blocking; keep the job green when only quarantine fails.
# Reports go to target/failsafe-reports-quarantine (see pom quarantine profile) so they
# do not overwrite the main suite's Failsafe XML uploaded as CI artifacts.
./mvnw -B verify -Pquarantine || true
