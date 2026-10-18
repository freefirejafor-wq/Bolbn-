# Penalty Arena - Native Android Football Penalty Shootout

Penalty Arena is a complete, native Android football penalty shootout game built using **Kotlin** and **Jetpack Compose**. It features an authentic 5-round shootout format, sudden-death tiebreakers, AI striker/goalkeeper difficulties, realistic ball trajectory physics with woodwork collisions, and full offline playability.

---

## Repository Structure

```
├── android/                   # Complete Native Android Studio Project
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/
│   │       │   ├── AndroidManifest.xml
│   │       │   ├── assets/models/
│   │       │   │   ├── manifest.json
│   │       │   │   └── README.txt
│   │       │   ├── java/com/penaltyarena/game/
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── engine/PenaltyMatchEngine.kt
│   │       │   │   ├── model/GameModels.kt
│   │       │   │   ├── repository/MatchStatsRepository.kt
│   │       │   │   ├── repository/MultiplayerRepository.kt
│   │       │   │   ├── viewmodel/PenaltyArenaViewModel.kt
│   │       │   │   └── ui/
│   │       │   │       ├── components/ArenaStadiumView.kt
│   │       │   │       ├── screens/HomeScreen.kt
│   │       │   │       ├── screens/MatchScreen.kt
│   │       │   │       ├── screens/MatchSetupScreen.kt
│   │       │   │       ├── screens/ResultsScreen.kt
│   │       │   │       ├── screens/SettingsScreen.kt
│   │       │   │       ├── screens/TrainingScreen.kt
│   │       │   │       └── screens/TutorialScreen.kt
│   │       │   └── res/
│   │       └── test/java/com/penaltyarena/game/
│   │           └── PenaltyMatchEngineTest.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   └── gradlew
├── src/                       # Interactive Web Preview, Simulator & APK Inspector
└── index.html
```

---

## Build & Test Commands

```bash
cd android
chmod +x gradlew
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

**Debug APK Output Path:**
`android/app/build/outputs/apk/debug/app-debug.apk`
