# Architecture

Static description of DreamTeam. No Gradle build, emulator or device run was
performed.

## Module graph

```
app
 ├─ feature/dashboard   ┐
 ├─ feature/sleepdebt   │
 ├─ feature/circadian   ├─→ core/core-ui
 ├─ feature/smartalarm  │   core/core-database
 ├─ feature/logger      │   core/core-common
 └─ feature/*            ┘
```

- `app` is the DI root and navigation host.
- Each feature owns its domain and presentation layers.
- `core/core-database` owns Room; `core/core-ui` owns the "Dark Studio" design
  system; `core/core-common` holds shared utilities.
- AGP 9 provides built-in Kotlin support, so `app/build.gradle.kts` does not apply
  the Kotlin Android plugin.

## Application and navigation

`DreamTeamApplication` is the `Application` class. `MainActivity` is the single
launcher activity; it requests `POST_NOTIFICATIONS` on API 33+, enables
edge-to-edge, and hosts `DreamTeamNavHost` inside `DreamTeamTheme`.

Seven destinations:

1. Dashboard (start)
2. Debt
3. Circadian
4. AlarmSetup
5. Logger
6. Correlations
7. Settings

The alarm ringing screen is a separate activity, shown over the lock screen with a
full-screen intent.

## Feature responsibilities

### Sleep debt

`feature/sleepdebt/domain/SleepDebtEngine.kt`:

- 14-day default window; nights are attributed to the wake date.
- Nightly deficit is `max(0, baseline − actual)`; a good night cannot refund debt.
- Missing nights are excluded and exposed through a coverage ratio and confidence,
  not assumed to be zero.
- Confidence requires 8 of 14 nights; valid session bounds are 0.5–18 hours.
- Debt is derived, never stored.

### Circadian energy

`feature/circadian/domain/CircadianEngine.kt`:

- Tunable anchors relative to wake time: wake, morning ramp, late-morning peak,
  afternoon slump, secondary peak, wind-down, ideal bedtime.
- Amplitude compression from debt: `scale = 1 / (1 + 0.045 * debtHours)` with an
  energy floor of 12.

### Smart alarm

`feature/smartalarm/`:

- Sensor modes `ACCELEROMETER`, `MICROPHONE`, `OFF`; persisted default
  `ACCELEROMETER`.
- `MovementScoreDetector` fires after two consecutive windows at or above a `0.35`
  movement threshold.
- `AlarmManager.setAlarmClock()` schedules window-open and hard-fallback triggers.
- A foreground monitoring service samples sensors; an alarm receiver handles
  `com.dreamteam.action.WINDOW_OPEN`, `com.dreamteam.action.FIRE_ALARM` and
  `android.intent.action.BOOT_COMPLETED`.
- The alarm fires at the first light-sleep signal inside the window, or at the
  target time at the latest.

### Logger and correlations

`feature/logger/` stores daily factor tags and computes with/without-tag averages
over the trailing 30 days. The UI labels these as associations, not causation.

### Dashboard

`feature/dashboard/` combines the debt ring, today's energy curve and the next alarm
on one screen.

## Data model

Room `DreamTeamDatabase`, version 1, `exportSchema = true`, 4 DAOs:

| Table | Shape |
| --- | --- |
| `sleep_sessions` | id, start/end epoch millis, source (`MANUAL`/`SENSOR`), optional quality score and notes |
| `sleep_baseline` | single row (`default`), target sleep hours (default 8.0), updated timestamp |
| `factor_logs` | epoch day primary key, comma-separated tags, optional note |
| `alarm_config` | single row: target wake minutes (default 07:00), window minutes (default 30), sound URI, vibration flag, sensor mode |

No SQL files or migrations exist; the entities define the schema.

## Permissions

The app manifest declares `SCHEDULE_EXACT_ALARM` (max SDK 32), `USE_EXACT_ALARM`,
`POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`,
`BODY_SENSORS`, `USE_FULL_SCREEN_INTENT`, `WAKE_LOCK`, `VIBRATE`,
`RECEIVE_BOOT_COMPLETED` and `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`. The
`smartalarm` feature manifest repeats the alarm and sensor permissions for
module-level lint and declares the service, receiver and ringing activity.

## Build, tests and CI

- `assembleRelease`, `test`, `lint`; APK under `app/build/outputs/apk/release/`.
- JDK 17 / Kotlin JVM target 17; compileSdk 37, minSdk 26, targetSdk 36.
- Release builds are debug-signed for CI installability.
- 5 JVM tests: MathUtils, CircadianEngine, CorrelationCalculator, SleepDebtEngine,
  MovementScoreDetector. No instrumented tests.
- CI: JDK 17, Gradle setup, lint, unit tests, `assembleRelease`, APK artifact with
  14-day retention.

## Unknowns

- Whether the alarm service is reliable across OEM battery restrictions.
- Whether `MICROPHONE` sensor mode is implemented or a placeholder.
- Whether any store release exists; CI is the only distribution path in the repo.
