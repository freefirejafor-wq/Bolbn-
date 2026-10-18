# Penalty Arena - Native Android Football Penalty Shootout

Penalty Arena is a complete, native Android football penalty shootout game built using **Kotlin** and **Jetpack Compose**. It features an authentic 5-round shootout format, sudden-death tiebreakers, AI striker/goalkeeper difficulties, realistic ball trajectory physics with woodwork collisions, and full offline playability.

---

## Architecture & Tech Stack

- **Platform:** Native Android (Min SDK: 24, Target/Compile SDK: 34)
- **UI Framework:** Jetpack Compose (Compose BOM 2024.02.02, Material 3)
- **Language & Runtime:** Kotlin 1.9.23 with Java 17 Compatibility
- **Build System:** Gradle 8.5 with Android Gradle Plugin (AGP) 8.3.2
- **State Management:** Architecture Components `ViewModel` + Kotlin `StateFlow`
- **Persistence:** Local `SharedPreferences` / `DataStore` for career statistics and game settings
- **Decoupled Engine:** `PenaltyMatchEngine.kt` is a pure Kotlin testable engine completely isolated from UI rendering and networking.

---

## Delivery Plan: Phase 1 vs. Phase 2

### Phase 1: Complete Single-Player (Active & Fully Working)
- **Offline Match Mode:** 5-round shootout against AI.
- **Early Clinch Algorithm:** Mathematically ends match when an insurmountable lead is secured (e.g. 3-0 after Round 3).
- **Sudden Death:** Automatic transition if tied after 5 rounds.
- **Player Striker Mode:** Touch drag aiming, power slider with green sweet spot (65%–85%), spin curve, and shot resolution.
- **Player Goalkeeper Mode:** 6-sector dive selection (Top-Left, Bottom-Left, High-Mid, Low-Mid, Top-Right, Bottom-Right) with timing reaction against AI strikes.
- **AI Difficulties:**
  - *Amateur (Easy):* Slower reaction latency, central placement, occasional errant strikes.
  - *Semi-Pro (Normal):* Realistic corner diving, good power, balanced reaction.
  - *World Class (Hard):* Elite anticipation, explosive top-corner placement.
- **Visuals & 2.5D Canvas:** Night stadium floodlights, mowed grass pitch perspective, 3D goal mesh with net bulge on goals, articulated goalkeeper joint animation, and shadow-projected ball physics.
- **Career Statistics:** Matches played, wins, win rate %, goals scored, saves made, clean sheets, and win streaks.
- **Training Camp:** Free-practice penalty shootout with instant ball return and conversion tracking.
- **Interactive Tutorial:** 4-step walkthrough of aiming, power calibration, goalkeeping, and rules.
- **Settings:** Sound FX toggle, haptic feedback toggle, reduced motion mode, and graphics quality.
- **Zero Cloud Dependencies:** Requires no login, Firebase, or external API keys.

### Phase 2: Online Multiplayer (Architectural Roadmap)
The `MultiplayerRepository` interface (`MultiplayerRepository.kt`) isolates networking so multiplayer can be added without rewriting the match engine.

**Required Phase 2 Backend Components:**
1. **Matchmaking Service:** Region-based matchmaking queue with latency ranking and Elo rating.
2. **WebSocket Room Lobbies:** Ephemeral rooms for peer-to-peer or server-authoritative matches.
3. **Server-Verified Physics:** Server validates shot vectors and dive sector timestamps to prevent client-side tampering.
4. **Disconnection Handling:** Reconnection grace period (15s) and automatic forfeit resolution if a player disconnects mid-shootout.

---

## Rigged 3D Player Assets Specification & Fictional Squad

- **Current Status:** Original fictional players active:
  - **Leo Vance #9 (Fictional Striker)**
  - **Marc Sterling #1 (Fictional Goalkeeper)**
- **Notice on Real-Player Likenesses:** Lionel Messi, Cristiano Ronaldo, Kylian Mbappé, and Erling Haaland are not included to adhere to intellectual property and licensing regulations.
- **Custom glTF 2.0 Rigged Models Integration:**
  - Target Folder: `app/src/main/assets/models/`
  - Model Files: `striker_rigged.glb` and `goalkeeper_rigged.glb`
  - Required Animation Clips: `idle`, `run_up`, `kick`, `dive_left`, `dive_right`, `dive_center`, `celebration`, `disappointed`.
  - Fallback Engine: High-performance 2.5D Compose Canvas Engine automatically runs when GLB models are not loaded.

---

## Build & Test Instructions

### Requirements
- **JDK:** OpenJDK 17 or higher
- **Android SDK:** Platform 34, Build-Tools 34.0.0
- **Android Studio:** Hedgehog (2023.1.1) or newer

### Building the Debug APK
```bash
# Clone the repository and enter the project folder
cd android

# Grant execution permissions to gradlew
chmod +x gradlew

# Run unit test suite
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug
```

### Debug APK Output Path:
```
android/app/build/outputs/apk/debug/app-debug.apk
```
