# DreamTeam

A native Android app (Kotlin + Jetpack Compose) that fuses the core behavioral models of two well-known sleep apps — rolling sleep-debt + circadian energy forecasting (Rise-style) and a smart, sleep-phase-aware wake window (Sleep Cycle-style) — into a single, original product.

**DreamTeam is an original implementation.** It implements the *publicly understood behavioral concepts* of rolling sleep debt, circadian energy curves, and light-sleep-detection wake windows with independently designed algorithms, UI, and architecture. It reproduces no proprietary formulas, code, branding, or assets from either reference app.

## Features

- **Sleep Debt Engine** — 14-day rolling debt (`max(0, need − actual)` per night), editable baseline sleep need (default 8h), missing-night handling with coverage confidence, and trend-vs-previous-window framing. Live-reactive via Room `Flow`.
- **Circadian Energy Timeline** — a per-day energy curve anchored to wake time (morning peak → afternoon slump → secondary peak → wind-down), modulated in amplitude by current sleep debt. Original heuristic inspired by the two-process model of sleep regulation (Process C + Process S); constants are tunable and documented.
- **Smart Wake Alarm** — target wake time + configurable window (10–60 min). Uses `AlarmManager.setAlarmClock()` for both the window-open and hard-fallback triggers (exempt from exact-alarm permission requirements, Doze-safe). A foreground service samples the accelerometer and fires when a light-sleep movement signal is detected inside the window, or at the hard target time otherwise — never later than requested. Gentle volume fade-in + escalating vibration.
- **Sleep & Factors Logger** — one-tap daily factor chips (caffeine, alcohol, exercise, stress, late screens, naps, …) plus manual sleep-session entry, with an honest association view (with/without tag averages over the trailing 30 days — labeled as association, not causation).
- **Immersive Dashboard** — a dark "living instrument panel" home screen tying debt ring, today's energy curve, and tonight's alarm into one glanceable animated screen.

## Architecture

- **Clean Architecture**: `core/` (common, database, ui design system) ← `feature/*` (domain + presentation per feature) ← `app/` (DI root, navigation host, wiring).
- **MVVM + Unidirectional Data Flow** with Kotlin Coroutines + Flow.
- **DI**: Hilt (KSP-based).
- **Local persistence**: Room (sessions, factor logs, baseline, alarm config). Data layer sits behind repository interfaces so a backend can be added later without a rewrite.
- **UI**: Jetpack Compose (Material 3 base, heavily custom-themed "Dark Studio" design system in `core-ui`); the signature energy curve is a bespoke animated Canvas component.
- **Min/target SDK**: 26 / 36. Kotlin 2.4, AGP 9 (built-in Kotlin), Gradle 9.5.

## Building

```bash
./gradlew assembleRelease        # release APK (CI debug-signed)
./gradlew test                   # JVM unit tests
./gradlew lint                   # Android lint
```

The APK lands in `app/build/outputs/apk/release/`. CI (`.github/workflows/android-build.yml`) runs lint, unit tests, and `assembleRelease` on every push to `main` and uploads the APK as an artifact.

## Honest limitations (v1)

- Phone-accelerometer light-sleep detection has modest accuracy (~50–70% vs. clinical polysomnography in public literature). The alarm is best-effort: it may wake a few minutes early, never late.
- All data is local-only; no cloud account, no wearable integration (the sensor layer is an interface, so both can be added later).
- Sleep need is a user-editable baseline, not yet inferred from history.
- Release builds use debug signing for CI distribution only — not production signing.
