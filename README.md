# Personalised Tracker

Local-first Android app I'm building for myself: workouts, body metrics, sleep,
food and habits in one place — exportable as a single JSON or Markdown report I
can paste into an AI for analysis.

> **Status: Phases 1–4 complete.**
> Workouts · Body + Sleep · Food + Habits · Markdown reports. Phase 5 (Health Connect, notifications, widgets) is explicitly out of scope.

## Goals

1. Log sets / reps / weight in under 30 s
2. Unlimited custom routines (Hevy free tier caps at 3 — this solves that)
3. Daily body weight + monthly InBody CSV import *(Phase 2)*
4. Sleep duration + quality *(Phase 2)*
5. Quick calorie / protein log *(Phase 3)*
6. Daily habit checklist with streaks *(Phase 3)*
7. JSON + Markdown export to share with an external AI

## Tech stack

| Layer        | Choice                                            |
|--------------|---------------------------------------------------|
| Language     | Kotlin 2.2                                        |
| UI           | Jetpack Compose + Material 3                      |
| Architecture | MVVM + Clean Architecture (data / domain / UI)    |
| DI           | Hilt                                              |
| DB           | Room (single source of truth, local-only)         |
| Async        | Coroutines + Flow                                 |
| Navigation   | Compose Navigation                                |
| Serialization| kotlinx.serialization (JSON)                      |
| Tests        | JUnit4 + MockK + Turbine                          |
| Min SDK      | 26 (Android 8) — Target 36                        |

**No backend. No Firebase. No analytics. No accounts.**

## Architecture

Package-by-feature, not by layer:

```
com.example.personalisedtracker/
├── TrackerApplication.kt          // @HiltAndroidApp
├── MainActivity.kt                // single Activity
├── core/
│   ├── common/                    // DataResult, AppError, DispatcherProvider, DateInt
│   ├── data/db/                   // TrackerDatabase (v2 — all 12 entities)
│   ├── di/                        // DatabaseModule, CoreBindingsModule
│   ├── navigation/                // AppNavHost, Routes
│   └── ui/                        // UiState
└── feature/
    ├── workout/                   // routines · sets · history (Phase 1)
    ├── body/                      // weight · waist · InBody CSV import · Vico chart (Phase 2)
    ├── sleep/                     // duration · quality · 30-day heatmap (Phase 2)
    ├── food/                      // quick-add · meal grouping · common foods (Phase 3)
    ├── habit/                     // daily checklist · streaks · 30-day rate (Phase 3)
    ├── logs/                      // Sleep + Food tabbed screen
    ├── reports/                   // Markdown generator + writer (Phase 4)
    └── settings/                  // JSON + Markdown export / import
```

Every feature follows the same 3-layer shape:
```
feature/<name>/
  data/      entity · dao · repository · mapper
  domain/    model · repository (interface) · usecase
  presentation/  ViewModel · Screen composables
```

### Layer rules

- ViewModels → UseCases → Repository → DAO
- Domain is pure Kotlin — knows nothing about Room or Android
- All DB work happens on `Dispatchers.IO` via `DispatcherProvider`
- No `GlobalScope` / `runBlocking` anywhere
- Errors cross layer boundaries as `DataResult` (sealed) — never thrown
- UI state is hoisted; screens take callbacks and `StateFlow`s

### Persisting an in-progress workout

The active-workout state lives entirely in Room. `WorkoutEntity.endedAt = null`
means "in progress". The active-workout screen subscribes to the DB and is
re-built on every emission — so process death, config change, or backgrounding
all resume cleanly. Acceptance criterion #5/#6.

## Build

```bash
./gradlew assembleDebug          # → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # to a connected device
./gradlew testDebugUnitTest      # JVM unit tests (VMs + use cases)
./gradlew lint                   # Android lint
```

Open in **Android Studio Iguana+** (AGP 9.x).
Min SDK 26, target SDK 36, JVM target 17.

### Toolchain notes

- AGP **9.2.1**, Gradle **9.4.1**
- Kotlin **2.2.20** (bundled with AGP) — Kotlin Android plugin is **not** applied manually
- KSP **2.2.20-2.0.3** for Room + Hilt code generation
- Hilt **2.59.2** (oldest Hilt that supports AGP 9)
- Room **2.8.4** (needed for Kotlin 2.2 metadata support)
- `gradle.properties` opts into `android.disallowKotlinSourceSets=false` so KSP-generated Kotlin sources are picked up by the built-in Kotlin source set DSL

## Export format

Export from **Settings → Export all data (JSON)**. The file is written to the
app cache and shared via the system share-sheet (WhatsApp, Drive, Gmail…).

```json
{
  "exported_at": "2026-05-14T10:30:00Z",
  "app_version": "1.0.0",
  "schema_version": 2,
  "exercises": [...],
  "routines": [...],
  "workouts": [ { "id": 1, "routine_name": "Monday Push", "sets": [...] } ],
  "body_weights": [...],
  "waist_measurements": [...],
  "inbody_scans": [...],
  "sleep_entries": [...],
  "food_entries": [...],
  "habits": [...],
  "habit_completions": [...]
}
```

Re-import via **Settings → Import from JSON**. Import is **additive** (never
destructive). Exercises and habits with matching names are reused — never
duplicated.

### Markdown report

**Settings → Markdown report → Share 7d / 30d / 90d** generates a single
`tracker-report-Nd-…md` file with a structured summary:

```markdown
# Health & Workout Report
**Period**: 14 Apr 2026 – 14 May 2026

## Body Composition
- Weight: 75.0 kg → 74.2 kg (-0.8 kg)
- Body fat %: 17.4% (last InBody: 07 May 2026)
- Muscle mass: 35.2 kg
- Waist: 86.0 cm → 84.0 cm (-2.0 cm)

## Workouts (12 sessions)
### Monday Push (4 sessions)
- Dumbbell floor press: 10×12.5kg → 10×15.0kg ↑
...

## Sleep
- Average: 6.4 hrs/night (over 24 nights)
- Best: 7.5 hrs (10 May 2026)
- Worst: 5.0 hrs (03 May 2026)
- Quality avg: 3.6/5

## Habits (% completed)
- Finasteride + Minoxidil (AM): 92%
...

## Food
- Logged: 18 of 30 days
- Average calories: 2240 kcal
- Average protein: 108 g
```

Paste directly into a chat with an external AI for analysis.

## Phase plan

| Phase | Scope                                                          | Status |
|-------|----------------------------------------------------------------|--------|
| 1     | Workouts (routines, sets, history, JSON export/import)         | ✅ |
| 2     | Body weight + InBody CSV + Waist + Sleep + Vico charts         | ✅ |
| 3     | Food entries + Habits + streaks                                | ✅ |
| 4     | Markdown reports (7/30/90-day) + cross-domain summary          | ✅ |
| 5     | Health Connect, notifications, widgets                         | ⏳ deferred |

## What's NOT built yet

- Phase 5 only: Health Connect integration, notifications/reminders, home-screen widgets
- Drag handle for routine reorder (works via ▲▼ buttons)
- One UI critical-path instrumentation test (Phase exit task)

## Tests

- `LogSetUseCaseTest` — use-case + MockK
- `RoutinesViewModelTest` — VM with Turbine + StandardTestDispatcher
- `SetHabitCompletionUseCaseTest` — use-case + MockK
- `InBodyCsvParserTest` — CSV parsing + lb→kg conversion + bad-date rejection

## Conventions

- Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`)
- KDoc on every public API
- One use case = one verb
- One Composable file per screen
- Sealed UI state (`Loading / Success / Error`)

# personalised-health-tracker
