# AGENTS.md – AI Agent Guidance for Scotland Yard App

## Project Overview
**Scotland Yard** is a turn-based multiplayer Android game built in **Kotlin + Jetpack Compose** with WebSocket networking. This is a university SE2 project (AAU Klagenfurt, 5-person team). The app communicates with a **Spring Boot backend** via STOMP/WebSocket protocol.

---

## Architecture & Data Flow

### Layering: MVVM Pattern (Required)
- **UI Layer** (`ui/`): Pure Jetpack Compose screens, no XML layouts
  - Activities use `setContent { }` with Compose theme
  - Navigation managed via `NavHost` and composable routes
- **Logic Layer** (`network/`): Business logic, game state, network calls
  - `MyStomp.kt`: STOMP WebSocket client with Kotlin Coroutines
  - Uses `Dispatchers.IO` for network, `Dispatchers.Main` for UI callbacks
- **Network Layer** (`network/`): Krossbow STOMP library abstracts WebSocket details

### WebSocket Communication Flow
1. **Connect**: `MyStomp.connect()` → OkHttpWebSocketClient → `ws://10.0.2.2:8080/scotlandyard`
2. **Subscribe**: Listen to STOMP topics (`/topic/hello-response`, `/topic/rcv-object`, `/topic/game/{gameId}/movements`)
3. **Send**: Publish to endpoints (`/app/hello`, `/app/object`, `/app/game/{gameId}/move`)
4. **Callbacks**: Messages trigger `Callbacks.onResponse()` → UI updates via `Handler(Looper.getMainLooper())`

### Key Dependencies for Network
- `krossbow-stomp-core`: STOMP protocol client
- `krossbow-websocket-okhttp`: WebSocket transport
- Uses `JSONObject` for message serialization (not Kotlinx serialization)

---

## Critical Workflows

### Local Build & Test
```bash
# Build debug APK (emulator testing)
./gradlew build

# Run unit tests (JUnit 5 Platform)
./gradlew testDebugUnitTest

# Generate JaCoCo code coverage report
./gradlew jacocoTestReport
# Output: app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml

# Run SonarCloud scan (GitHub Actions does this automatically on main/PR)
./gradlew build sonar --info
```

### CI/CD Pipeline (GitHub Actions)
- **Trigger**: Push to `main` or PR to `main`
- **Steps**: Build → Unit tests + JaCoCo → SonarCloud scan
- **Quality Gate**: SonarCloud must pass; `main` branch protected

### Feature Development Workflow
1. Create feature branch: `feature/description` or `fix/description`
2. Commit with Conventional Commits: `[#IssueNumber] feat: description`
3. Push → GitHub Actions runs CI
4. Create Pull Request (no squash/rebase merge)
5. Merge only after CI passes and PR approved

---

## Project-Specific Conventions & Patterns

### Code Style
- **Kotlin Coroutines**: Always use for async/network
  - `scope.launch { }` for background tasks (e.g., network calls)
  - `Dispatchers.IO` for I/O, `Dispatchers.Main` for UI
  - Never block the main thread
- **Logging**: Use `Log.d("TAG", "message")` from `android.util.Log` for Logcat
- **Jetpack Compose**: Composables are pure functions, no side effects during render
  - Use `@Composable` annotation
  - Preview annotations: `@Preview(showBackground = true)`
  - No XML layouts — all UI in Compose DSL

### File Structure
```
app/src/main/java/at/aau/serg/scotlandyard/
├── ui/activity/          # Screens (Jetpack Compose)
├── ui/theme/             # Theme, colors, typography
├── network/              # Network/STOMP logic
├── Callbacks.kt          # Interface for network callbacks
└── assets/               # Game data (board.json)
```

### Testing Requirements
- **Unit tests only** in `src/test/` (no instrumented tests for game logic)
- Use **JUnit 5 Platform** (`@Test` with `org.junit.jupiter.api.Test`)
- All logic MUST have unit tests before merging
- Test files follow `*Test.kt` naming convention

### SonarCloud & Quality Gates
- JaCoCo coverage must exceed project threshold
- Code smell, bug, security hotspot counts reviewed
- Automatic scans on PR + main push
- **Action required**: Ensure tests pass locally before pushing

---

## Integration Points & Network Details

### STOMP Message Format
**Outgoing moves** (JSON to `/app/game/{gameId}/move`):
```json
{
  "gameId": "game123",
  "playerId": "player456",
  "ticket": "bus",
  "targetPosition": 42,
  "timestamp": 1712761234567
}
```

**Incoming subscription** (`/topic/game/{gameId}/movements`):
Prefixed with `"movement:"` in callback (see `MyStomp.kt:119`)

### Hardcoded Server URL
- **Local development**: `ws://10.0.2.2:8080/scotlandyard`
- `10.0.2.2` is the emulator's bridge to host machine
- Change in `MyStomp.kt` line 19 for different environments

### Game State & Roles
- Board data in `app/src/main/assets/board.json`
- Two roles: Detectives vs Mr. X
- Turn-based movement with tickets (bus, taxi, underground, etc.)

---

## Common Tasks for AI Agents

### Adding a New Compose Screen
1. Create `ScreenName.kt` in `ui/activity/` or appropriate subdirectory
2. Define `@Composable fun ScreenName(...)` function
3. Add route to `NavHost` in `MainActivity.kt`
4. Use callbacks (e.g., `onNavigate: () -> Unit`) for navigation
5. Add `@Preview` for development testing

### Sending Network Messages
1. Call method on `MyStomp` instance (e.g., `sendMove()`)
2. Build JSON payload with `JSONObject`
3. Send via `session?.sendText("/app/endpoint", jsonString)`
4. Handle errors in catch block (don't block coroutine)

### Adding Unit Tests
1. Create `TestClass.kt` in `src/test/java/` with same package structure
2. Use `@Test` from `org.junit.jupiter.api`
3. Assertions: `assertEquals()`, `assertTrue()`, etc.
4. Test must run locally before PR: `./gradlew testDebugUnitTest`

---

## Critical Gotchas

1. **Never call network operations on Main thread** — always use `Dispatchers.IO`
2. **Callbacks must post to Main thread** — see `MyStomp.callback()` pattern with `Handler`
3. **No XML layouts** — everything must be Jetpack Compose
4. **Merge strategy is strict** — PRs will fail if they don't follow Conventional Commits or if tests don't pass
5. **SonarCloud tokens required** — GitHub Actions needs `SONAR_TOKEN` secret to scan (already configured)
6. **Emulator networking** — Use `10.0.2.2` for localhost, NOT `127.0.0.1`
7. **Cleartext traffic allowed** — `usesCleartextTraffic="true"` in manifest for local testing; remove for production

---

## Team Coordination

- **UI + Game Logic**: 2 developers (including you)
- **Game Logic only**: 1 developer  
- **Backend/Networking**: 2 developers (own repository)

**Communication**: Game logic changes → notify teammates; network message formats → coordinate with backend team before implementing.

---

## Useful Resources
- **Krossbow Docs**: https://github.com/joffrey-bion/krossbow
- **Jetpack Compose**: https://developer.android.com/develop/ui/compose
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **GitHub Conventional Commits**: https://www.conventionalcommits.org/

