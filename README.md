# Scotland Yard App

Android client for the Scotland Yard board game, developed as part of the university course [621.252] Software Engineering 2, Group 2 at AAU Klagenfurt.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Prerequisites](#3-prerequisites)
4. [Running the App](#4-running-the-app)
5. [Server Connection Configuration](#5-server-connection-configuration)
6. [Development Workflow](#6-development-workflow)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Project Overview

This repository contains the Android client for the Scotland Yard project, a turn-based multiplayer game in which one player takes the role of Mr. X while the others play as detectives.

The client connects to a Spring Boot backend via WebSocket using the STOMP protocol. The backend is maintained in a separate repository (`ScotlandYard-Server`) and must be running before the app can establish a connection.

Deployment and backend runtime details are documented in the ScotlandYard-Server repository.

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Networking | WebSocket / STOMP (Krossbow) |
| Async | Kotlin Coroutines |
| Build System | Gradle (Kotlin DSL) |
| CI/CD | GitHub Actions |
| Code Quality | SonarCloud, JaCoCo |
| Testing | JUnit 5 |

---

## 3. Prerequisites

- **Android Studio** (latest stable release recommended)
- **JDK 17** or higher, depending on the local Android Studio and Gradle setup
- **Android SDK** with a device or emulator running Android 8.0 (API 26) or higher
- A running backend server — see the ScotlandYard-Server repository for setup instructions

---

## 4. Running the App

### Clone the repository

```bash
git clone https://github.com/SE2-S26-Gruppe2-ScotlandYard/ScotlandYard-App.git
cd ScotlandYard-App
```

### Build and run

Open the project in Android Studio, select a connected device or emulator, and click **Run**. Alternatively, use the Gradle wrapper from the command line:

```bash
# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Generate code coverage report
./gradlew jacocoTestReport
# Report output: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml
```

---

## 5. Server Connection Configuration

`MainActivity` initializes `BoardConnection` and `ServerConfig` on startup. `ServerConfig.kt` defines three server URIs:

| Constant | URI | Intended Use |
|---|---|---|
| `LOCAL_URI` | `ws://10.0.2.2:8080/scotlandyard` | Android emulator (maps to host machine localhost) |
| `DEVICE_URI` | `ws://192.168.68.109:8080/scotlandyard` | Physical device on the same local network |
| `GLOBAL_URI` | `ws://se2-demo.aau.at:53206/scotlandyard` | Shared remote server |

To switch environments, select the active server URI directly in the app.

> **Note:** The app currently uses unencrypted WebSocket connections (`ws://`), which requires `android:usesCleartextTraffic="true"` in the Android manifest. This is suitable for development and testing only.

---

## 6. Development Workflow

### Branch naming

```
<type>/<short-description>
```

Examples: `feature/player-movement`, `fix/connection-timeout`

### Commit convention

```
[#IssueNumber] <type>: <description>
```

Examples:
- `[#12] feat: add detective movement screen`
- `[#15] fix: handle disconnection on pause`

Common types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

### Pull requests

- All changes are merged into `main` via pull request only.
- Squash and rebase merges are not permitted.
- The `main` branch is protected and must remain buildable at all times.
- CI must pass before a pull request can be merged.

### CI/CD Pipeline

GitHub Actions runs automatically on every push to `main` and on every pull request targeting `main`:

1. Build
2. Unit tests and JaCoCo coverage report
3. SonarCloud scan

---

## 7. Troubleshooting

**The app cannot connect to the server on the emulator.**
Ensure `LOCAL_URI` is active. The address `10.0.2.2` is the emulator's alias for the host machine's localhost. Using `127.0.0.1` will not work.

**The app cannot connect on a physical device.**
Switch to `DEVICE_URI` and verify that the device and the machine running the backend are on the same local network. Update the IP address in `ServerConfig.kt` if your machine's local IP has changed.

**Build fails with a network security error.**
Check that `android:usesCleartextTraffic="true"` is set in `AndroidManifest.xml`. This is required for unencrypted WebSocket traffic during development.

**Unit tests fail locally.**
Run `./gradlew testDebugUnitTest` and review the output. All tests must pass before pushing, as failing tests will block the CI build.

**SonarCloud scan fails.**
Ensure the JaCoCo report has been generated beforehand (`./gradlew jacocoTestReport`) and that test coverage meets the configured threshold.
