#!/usr/bin/env bash
# M7.4.4: run real instrumented tests on the Emotionskompass emulator.
# Bypass the AGP/UTP ddmlib multi-APK installer, which can falsely report
# "API level=35. Cannot install split APKs with API level < 21".
set -euo pipefail

APP_ROOT="games/Emotionskompass/src"
REPORT_DIR="$APP_ROOT/app/build/outputs/androidTest-results/manual"
TEST_CLASS="com.valoon4.emotionskompass.StrategyNavigationEmulatorTest,com.valoon4.emotionskompass.PersonalReviewEmulatorTest,com.valoon4.emotionskompass.PersonalInsightsEmulatorTest,com.valoon4.emotionskompass.GuidedReflectionEmulatorTest,com.valoon4.emotionskompass.StrategyExperimentsEmulatorTest"
RUNNER="com.valoon4.emotionskompass.test/androidx.test.runner.AndroidJUnitRunner"

mkdir -p "$REPORT_DIR"
echo "::group::Build Emotionskompass app and Android UI test packages"
gradle -p "$APP_ROOT" :app:assembleDebug :app:assembleDebugAndroidTest \
  --no-daemon --stacktrace
echo "::endgroup::"

adb wait-for-device
device_api="$(adb shell getprop ro.build.version.sdk | tr -d '\r')"
device_state="$(adb get-state | tr -d '\r')"
echo "Emulator state: $device_state; Android API: $device_api"
if [[ "$device_state" != "device" || "$device_api" != "35" ]]; then
  echo "::error::Expected ready Android 35 emulator; got $device_state / API $device_api"
  exit 1
fi

# adb 'device' and sys.boot_completed=1 do not imply the Android framework is
# healthy. On a software-emulated CI VM these may appear long before package
# and activity services exist. Never attempt installation until both respond.
ready=false
for attempt in {1..24}; do
  if adb shell service check package 2>/dev/null | grep -q 'found' \
      && adb shell service check activity 2>/dev/null | grep -q 'found'; then
    ready=true
    break
  fi
  echo "Waiting for Android package/activity system services (attempt $attempt/24)"
  sleep 5
done
if [[ "$ready" != true ]]; then
  echo "::error::Android framework did not start package and activity services."
  adb shell service list | head -30 || true
  exit 1
fi
echo "Android package and activity services are ready."

APP_APK="$APP_ROOT/app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="$APP_ROOT/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
test -s "$APP_APK" && test -s "$TEST_APK"

echo "::group::Install app and test APK separately via adb"
# AGP's connectedDebugAndroidTest uses UTP/ddmlib split install and can fail
# before any test launches. ADB install of each standalone APK avoids this bug.
adb install -r -t "$APP_APK"
adb install -r -t "$TEST_APK"
adb shell pm path com.valoon4.emotionskompass | grep -q package:
adb shell pm path com.valoon4.emotionskompass.test | grep -q package:
echo "::endgroup::"

run_suite() {
  local label="$1" scale="$2" out="$REPORT_DIR/$1.log"
  echo "::group::Run all thirty-three Compose tests: $label (font_scale=$scale)"
  adb shell settings put system font_scale "$scale"
  local current_scale
  current_scale="$(adb shell settings get system font_scale | tr -d '\r')"
  if [[ "$current_scale" != "$scale" ]]; then
    echo "::error::System font scale mismatch: $current_scale vs $scale"
    return 1
  fi
  # The instrumentation process is restarted between runs, so Compose sees
  # the changed system configuration. -e class scopes to our four real tests.
  adb shell am instrument -w -r -e class "$TEST_CLASS" "$RUNNER" | tee "$out"
  # am instrument often exits 0 even for failed/zero tests: explicitly assert
  # the instrumentation result and exact count.
  if ! grep -Eq 'OK \(33 tests\)' "$out" \
      || ! grep -q 'INSTRUMENTATION_CODE: -1' "$out" \
      || grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=|Error in ' "$out"; then
    echo "::error::Expected thirty-three passing instrumented tests at $label; see $out"
    return 1
  fi
  echo "::endgroup::"
}

adb shell wm size reset
adb shell wm density reset
run_suite "default-100-percent" "1.0"

# M7.3: exercise the real AndroidKeyStore-backed EKB2/EKB1 restore path in an
# isolated fictional-data sandbox. The existing seven UI tests remain unchanged.
echo "::group::Run three encrypted backup integration tests on Android emulator"
adb shell am instrument -w -r -e class   "com.valoon4.emotionskompass.CombinedBackupEmulatorTest" "$RUNNER"   | tee "$REPORT_DIR/m73-backup-integrity.log"
if ! grep -Fq 'OK (3 tests)' "$REPORT_DIR/m73-backup-integrity.log"     || ! grep -q 'INSTRUMENTATION_CODE: -1' "$REPORT_DIR/m73-backup-integrity.log"     || grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=|Error in '       "$REPORT_DIR/m73-backup-integrity.log"; then
  echo "::error::M7.3 encrypted backup integration tests failed"
  exit 1
fi
echo "::endgroup::"

# M7.4.4: real EKS1/EKB3/legacy import validation with AndroidKeyStore.
echo "::group::Run nine encrypted three-area EKB3 integration tests"
adb shell am instrument -w -r -e class \
  "com.valoon4.emotionskompass.StrategyBackupEmulatorTest" "$RUNNER" \
  | tee "$REPORT_DIR/m741-strategy-backup-integrity.log"
if ! grep -Fq 'OK (9 tests)' "$REPORT_DIR/m741-strategy-backup-integrity.log" \
    || ! grep -q 'INSTRUMENTATION_CODE: -1' "$REPORT_DIR/m741-strategy-backup-integrity.log" \
    || grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=|Error in ' \
      "$REPORT_DIR/m741-strategy-backup-integrity.log"; then
  echo "::error::M7.4.1 encrypted EKB3 integration tests failed"
  exit 1
fi
echo "::endgroup::"

# 720 physical px at 360dpi = 320dp portrait viewport; very small phone.
adb shell wm size 720x1280
adb shell wm density 360
adb shell wm size
adb shell wm density
run_suite "narrow-320dp-200-percent" "2.0"

echo "M7.4.4: 66 Compose UI executions plus 3 EKB2 and 9 EKB3 KeyStore tests passed."
