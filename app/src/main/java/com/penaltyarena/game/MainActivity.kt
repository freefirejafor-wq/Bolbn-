package com.penaltyarena.game

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.penaltyarena.game.ui.screens.HomeScreen
import com.penaltyarena.game.ui.screens.MatchScreen
import com.penaltyarena.game.ui.screens.MatchSetupScreen
import com.penaltyarena.game.ui.screens.ResultsScreen
import com.penaltyarena.game.ui.screens.SettingsScreen
import com.penaltyarena.game.ui.screens.TrainingScreen
import com.penaltyarena.game.ui.screens.TutorialScreen
import com.penaltyarena.game.ui.theme.PenaltyArenaTheme
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.viewmodel.GameAudioEvent
import com.penaltyarena.game.viewmodel.PenaltyArenaViewModel
import com.penaltyarena.game.viewmodel.Screen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: PenaltyArenaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PenaltyArenaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StadiumNightDark
                ) {
                    PenaltyArenaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun PenaltyArenaApp(viewModel: PenaltyArenaViewModel) {
    val state by viewModel.uiState.collectAsState()
    val careerStats by viewModel.careerStats.collectAsState()
    val settings by viewModel.gameSettings.collectAsState()
    val view = LocalView.current

    // Listen to audio & haptic events
    LaunchedEffect(Unit) {
        viewModel.audioEvents.collectLatest { event ->
            if (settings.hapticsEnabled) {
                when (event) {
                    is GameAudioEvent.Kick -> view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    is GameAudioEvent.GoalCrowd -> view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    is GameAudioEvent.PostHitWoodwork -> view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    else -> Unit
                }
            }
        }
    }

    // System Back Press handling
    BackHandler(enabled = state.currentScreen != Screen.HOME) {
        when (state.currentScreen) {
            Screen.MATCH_SETUP, Screen.TRAINING, Screen.TUTORIAL, Screen.SETTINGS, Screen.RESULTS -> {
                viewModel.navigateTo(Screen.HOME)
            }
            Screen.MATCH -> {
                // Match screen has its own forfeit dialog
            }
            Screen.HOME -> Unit
        }
    }

    when (state.currentScreen) {
        Screen.HOME -> HomeScreen(
            careerStats = careerStats,
            onNavigate = { viewModel.navigateTo(it) }
        )
        Screen.MATCH_SETUP -> MatchSetupScreen(
            currentDifficulty = state.difficulty,
            onSelectDifficulty = { viewModel.setDifficulty(it) },
            onStartMatch = { viewModel.startNewMatch() },
            onNavigateBack = { viewModel.navigateTo(Screen.HOME) }
        )
        Screen.MATCH -> MatchScreen(
            state = state,
            onUserShoot = { viewModel.userShoot(it) },
            onUserDive = { viewModel.userDefend(it) },
            onAbandonMatch = { viewModel.navigateTo(Screen.HOME) }
        )
        Screen.TRAINING -> TrainingScreen(
            score = state.trainingScore,
            attempts = state.trainingAttempts,
            outcome = state.trainingOutcome,
            onPracticeShot = { viewModel.practiceShot(it) },
            onResetTraining = { viewModel.resetTraining() },
            onNavigateBack = { viewModel.navigateTo(Screen.HOME) }
        )
        Screen.TUTORIAL -> TutorialScreen(
            onNavigateBack = { viewModel.navigateTo(Screen.HOME) },
            onStartTraining = { viewModel.navigateTo(Screen.TRAINING) }
        )
        Screen.RESULTS -> ResultsScreen(
            state = state,
            onPlayAgain = { viewModel.startNewMatch() },
            onNavigateHome = { viewModel.navigateTo(Screen.HOME) }
        )
        Screen.SETTINGS -> SettingsScreen(
            settings = settings,
            onUpdateSettings = { viewModel.updateSettings(it) },
            onResetStats = { viewModel.resetCareerStats() },
            onNavigateBack = { viewModel.navigateTo(Screen.HOME) }
        )
    }
}
