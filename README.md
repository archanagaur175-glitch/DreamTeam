# DreamTeam

A native Android app (Kotlin + Jetpack Compose) that combines rolling sleep-debt and
circadian energy forecasting with a sleep-phase-aware wake window in one product.

**DreamTeam is an original implementation.** It applies the publicly understood
ideas of rolling sleep debt, circadian energy curves and light-sleep detection with
independently designed algorithms, UI and architecture. It reproduces no
proprietary formulas, code, branding or assets from any reference app.

## Features

- **Sleep Debt Engine** — 14-day rolling debt (`max(0, need − actual)` per night),
  editable baseline sleep need (default 8 h), missing-night handling with coverage
  confidence, and trend framing against the previous window. Reactive through Room
  `Flow`.
- **Circadian Energy Timeline** — a per-day curve anchored to wake time (morning
  peak → afternoon slump → secondary peak → wind-down), with amplitude compressed by
  current sleep debt.
- **Smart Wake Alarm** — target wake time plus a configurable 10–60 minute window.
  `AlarmManager.setAlarmClock()` schedules the window-open and hard-fallback
  triggers. A foreground service samples the accelerometer and fires when a
  light-sleep movement signal appears inside the window, or at the target time
  otherwise — never later than requested. Volume fades in and vibration escalates.
- **Sleep & Factors Logger** — daily factor chips (caffeine, alcohol, exercise,
  stress, late screens, naps and more), manual sleep-session entry, and a
  with/without-tag association view over the trailing 30 days, labelled as
  association rather than causation.
- **Immersive Dashboard** — a dark, animated home screen tying the debt ring,
  today's energy curve and tonight's alarm into one view.

## Architecture

- **Clean Architecture** — `core/` (common, database, UI design system) ←
  `feature/*` (domain + presentation) ← `app/` (DI root, navigation, wiring).
- **MVVM + unidirectional data flow** with Kotlin Coroutines and Flow.
- **DI** — Hilt with KSP.
- **Persistence** — Room for sessions, factors, baseline and alarm config, behind
  repository interfaces so a backend could be added later.
- **UI** — Jetpack Compose Material 3 with a custom "Dark Studio" design system in
  `core-ui`; the energy curve is a bespoke animated Canvas component.

### Module map

| Module | Responsibility |
| --- | --- |
| `core/core-common` | Shared utilities (including math) |
| `core/core-database` | Room entities, DAOs, database |
| `core/core-ui` | Theme and design-system components |
| `feature/sleepdebt` | Debt calculation and presentation |
| `feature/circadian` | Energy curve engine and presentation |
| `feature/smartalarm` | Alarm config, monitoring service, receivers, ringing UI |
| `feature/logger` | Factor logs and correlations |
| `feature/dashboard` | Home dashboard |
| `app` | DI root, `MainActivity`, navigation host |

AGP 9 supplies built-in Kotlin support, so `app/build.gradle.kts` does not apply the
Kotlin Android plugin separately. The root build forces `kotlin-metadata-jvm` to the
Kotlin version so Hilt KSP processors can read Kotlin 2.4 classes.

## Navigation and alarm flow

Seven Compose destinations: Dashboard (start), Debt, Circadian, AlarmSetup, Logger,
Correlations and Settings. The ringing screen is a separate activity shown over the
lock screen via full-screen intent.

```
AlarmManager setAlarmClock
  ├─ WINDOW_OPEN  → start foreground monitoring service
  │                   └─ movement score ≥ 0.35 for 2 consecutive windows → fire
  ├─ FIRE_ALARM   → hard fallback at target time
  └─ BOOT_COMPLETED → restore scheduled alarms
```

Movement detection fires after two consecutive windows at or above a `0.35`
threshold. The alarm fires at the first light-sleep signal inside the window, or at
the target time at the latest.

## Data model

Room database `DreamTeamDatabase`, version 1, `exportSchema = true`, with 4 DAOs:

| Table | Columns |
| --- | --- |
| `sleep_sessions` | id, start/end epoch millis, source (`MANUAL`/`SENSOR`), optional quality score and notes |
| `sleep_baseline` | single row (`default`), target sleep hours (default 8.0), updated timestamp |
| `factor_logs` | epoch day primary key, comma-separated tags, optional note |
| `alarm_config` | single row: target wake minutes (default 07:00), window minutes (default 30), sound URI, vibration flag, sensor mode |

No SQL files or migrations; the schema is defined by the entities.

## Permissions

Eleven permissions are declared: `SCHEDULE_EXACT_ALARM` (max SDK 32),
`USE_EXACT_ALARM`, `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`,
`FOREGROUND_SERVICE_HEALTH`, `BODY_SENSORS`, `USE_FULL_SCREEN_INTENT`, `WAKE_LOCK`,
`VIBRATE`, `RECEIVE_BOOT_COMPLETED` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.

They cover the alarm window, the foreground monitoring service, sensor sampling,
the lock-screen ringing activity, vibration and restoring alarms after reboot.
`POST_NOTIFICATIONS` is requested at runtime on API 33+.

## Building

```bash
./gradlew assembleRelease        # release APK (CI debug-signed)
./gradlew test                   # JVM unit tests
./gradlew lint                   # Android lint
```

The APK lands in `app/build/outputs/apk/release/`. Min/target SDK are 26/36;
compileSdk is 37. Kotlin 2.4.10, AGP 9.3.0, Gradle 9.5.0.

CI (`.github/workflows/android-build.yml`) runs on push and pull request to `main`:
JDK 17, Gradle setup, lint, unit tests, `assembleRelease`, and APK artifact upload
with 14-day retention.

## Tests

Five JVM unit test files cover `MathUtils`, `CircadianEngine`,
`CorrelationCalculator`, `SleepDebtEngine` and `MovementScoreDetector`. There are no
instrumented tests. No test run was performed for this documentation.

## Honest limitations (v1)

- Phone-accelerometer light-sleep detection has modest accuracy. The figure of
  roughly 50–70% versus clinical polysomnography comes from public literature, not
  from measurements in this repository. The alarm is best-effort: it may wake you a
  few minutes early, never late.
- The code and persisted config expose `ACCELEROMETER`, `MICROPHONE` and `OFF`
  sensor modes. The README documents accelerometer behaviour; whether microphone
  mode is implemented or a placeholder is unknown.
- All data is local-only; there is no cloud account and no wearable integration.
- Sleep need is a user-editable baseline, not inferred from history.
- Release builds are debug-signed for CI distribution, not production signing.
- No dependency lockfile; the Gradle version catalog is used instead.

## License

MIT. See [`LICENSE`](LICENSE).
