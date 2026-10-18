package com.penaltyarena.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penaltyarena.game.engine.PenaltyMatchEngine
import com.penaltyarena.game.model.Difficulty
import com.penaltyarena.game.model.DiveSpec
import com.penaltyarena.game.model.GoalSector
import com.penaltyarena.game.model.KickAttempt
import com.penaltyarena.game.model.PlayerRosterItem
import com.penaltyarena.game.model.ShotOutcome
import com.penaltyarena.game.model.ShotSpec
import com.penaltyarena.game.model.Team
import com.penaltyarena.game.model.TeamRole
import com.penaltyarena.game.repository.GameSettings
import com.penaltyarena.game.repository.MatchStatsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    MATCH_SETUP,
    MATCH,
    TRAINING,
    TUTORIAL,
    RESULTS,
    SETTINGS
}

enum class MatchPhase {
    AWAITING_INPUT,
    BALL_IN_FLIGHT,
    OUTCOME_CELEBRATION,
    MATCH_OVER
}

data class BallFlightState(
    val startX: Float = 0.0f,
    val startY: Float = 0.0f,
    val targetX: Float = 0.0f,
    val targetY: Float = 0.0f,
    val progress: Float = 0.0f, // 0.0 to 1.0
    val goalkeeperProgress: Float = 0.0f,
    val goalkeeperDiveSector: GoalSector = GoalSector.CENTER_LOW,
    val outcome: ShotOutcome? = null
)

data class MatchUiState(
    val currentScreen: Screen = Screen.HOME,
    val difficulty: Difficulty = Difficulty.NORMAL,
    val currentRound: Int = 1,
    val isSuddenDeath: Boolean = false,
    val isUserShooting: Boolean = true,
    val userScore: Int = 0,
    val aiScore: Int = 0,
    val phase: MatchPhase = MatchPhase.AWAITING_INPUT,
    val recentAttempt: KickAttempt? = null,
    val matchWinner: Team? = null,
    val history: List<KickAttempt> = emptyList(),
    val flightState: BallFlightState = BallFlightState(),
    val selectedStriker: PlayerRosterItem = PlayerRosterItem("st_01", "Leo Vance (Fictional)", 9, true, TeamRole.STRIKER),
    val selectedGoalkeeper: PlayerRosterItem = PlayerRosterItem("gk_01", "Marc Sterling (Fictional)", 1, true, TeamRole.GOALKEEPER),
    val trainingScore: Int = 0,
    val trainingAttempts: Int = 0,
    val trainingOutcome: ShotOutcome? = null
)

sealed class GameAudioEvent {
    object Whistle : GameAudioEvent()
    object Kick : GameAudioEvent()
    object GoalCrowd : GameAudioEvent()
    object SaveCheer : GameAudioEvent()
    object PostHitWoodwork : GameAudioEvent()
    object MissGroan : GameAudioEvent()
    object FullTimeWhistle : GameAudioEvent()
}

class PenaltyArenaViewModel(
    private val statsRepository: MatchStatsRepository = MatchStatsRepository()
) : ViewModel() {

    private var engine = PenaltyMatchEngine(Difficulty.NORMAL)

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    val careerStats = statsRepository.statsFlow
    val gameSettings = statsRepository.settingsFlow

    private val _audioEvents = MutableSharedFlow<GameAudioEvent>()
    val audioEvents: SharedFlow<GameAudioEvent> = _audioEvents.asSharedFlow()

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setDifficulty(difficulty: Difficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun startNewMatch() {
        engine = PenaltyMatchEngine(_uiState.value.difficulty)
        _uiState.update {
            it.copy(
                currentScreen = Screen.MATCH,
                currentRound = 1,
                isSuddenDeath = false,
                isUserShooting = true,
                userScore = 0,
                aiScore = 0,
                phase = MatchPhase.AWAITING_INPUT,
                recentAttempt = null,
                matchWinner = null,
                history = emptyList(),
                flightState = BallFlightState()
            )
        }
        triggerAudio(GameAudioEvent.Whistle)
    }

    /**
     * User executes shot against AI Goalkeeper.
     */
    fun userShoot(shot: ShotSpec) {
        if (_uiState.value.phase != MatchPhase.AWAITING_INPUT || !_uiState.value.isUserShooting) return

        val aiDive = engine.generateAiGoalkeeperDive(shot)
        executeTurn(shot, aiDive)
    }

    /**
     * User defends as Goalkeeper against AI Striker.
     */
    fun userDefend(userDiveSector: GoalSector) {
        if (_uiState.value.phase != MatchPhase.AWAITING_INPUT || _uiState.value.isUserShooting) return

        val aiShot = engine.generateAiShot()
        val userDive = DiveSpec(sector = userDiveSector, timingOffsetMs = 0L)
        executeTurn(aiShot, userDive)
    }

    private fun executeTurn(shot: ShotSpec, dive: DiveSpec) {
        triggerAudio(GameAudioEvent.Kick)

        // Enter flight phase
        _uiState.update {
            it.copy(
                phase = MatchPhase.BALL_IN_FLIGHT,
                flightState = BallFlightState(
                    startX = 0.0f,
                    startY = 0.0f,
                    targetX = shot.aimX,
                    targetY = shot.aimY,
                    progress = 0.0f,
                    goalkeeperProgress = 0.0f,
                    goalkeeperDiveSector = dive.sector
                )
            )
        }

        viewModelScope.launch {
            // Animate flight in steps
            val steps = 12
            for (i in 1..steps) {
                delay(40)
                val p = i.toFloat() / steps
                _uiState.update {
                    it.copy(
                        flightState = it.flightState.copy(
                            progress = p,
                            goalkeeperProgress = (p * 1.1f).coerceAtMost(1.0f)
                        )
                    )
                }
            }

            // Resolve kick in game engine
            val attempt = engine.executeKick(shot, dive)

            // Trigger outcome sounds
            when (attempt.outcome) {
                ShotOutcome.GOAL -> triggerAudio(GameAudioEvent.GoalCrowd)
                ShotOutcome.SAVED -> triggerAudio(GameAudioEvent.SaveCheer)
                ShotOutcome.POST_HIT, ShotOutcome.CROSSBAR_HIT -> triggerAudio(GameAudioEvent.PostHitWoodwork)
                ShotOutcome.WIDE, ShotOutcome.OVER_THE_BAR -> triggerAudio(GameAudioEvent.MissGroan)
            }

            _uiState.update {
                it.copy(
                    phase = MatchPhase.OUTCOME_CELEBRATION,
                    recentAttempt = attempt,
                    userScore = engine.userScore,
                    aiScore = engine.aiScore,
                    currentRound = engine.currentRound,
                    isSuddenDeath = engine.isSuddenDeath,
                    history = engine.history.toList(),
                    flightState = it.flightState.copy(outcome = attempt.outcome)
                )
            }

            // Celebration pause
            delay(1800)

            if (engine.isMatchFinished) {
                val won = engine.winner == Team.USER
                val userGoals = engine.userAttempts.count { it.outcome.isGoal }
                val userSaves = engine.aiAttempts.count { it.outcome == ShotOutcome.SAVED }
                val aiGoals = engine.aiAttempts.count { it.outcome.isGoal }

                statsRepository.recordMatchResult(
                    won = won,
                    goals = userGoals,
                    saves = userSaves,
                    opponentGoals = aiGoals
                )

                triggerAudio(GameAudioEvent.FullTimeWhistle)
                _uiState.update {
                    it.copy(
                        phase = MatchPhase.MATCH_OVER,
                        matchWinner = engine.winner,
                        currentScreen = Screen.RESULTS
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        phase = MatchPhase.AWAITING_INPUT,
                        isUserShooting = engine.isUserTurnToShoot,
                        flightState = BallFlightState()
                    )
                }
            }
        }
    }

    /**
     * Practice shot in Training mode without match consequences.
     */
    fun practiceShot(shot: ShotSpec) {
        val dummyDive = DiveSpec(sector = GoalSector.values().random())
        val outcome = engine.calculateOutcome(shot, dummyDive, Difficulty.NORMAL)

        triggerAudio(GameAudioEvent.Kick)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    trainingAttempts = it.trainingAttempts + 1,
                    trainingScore = if (outcome.isGoal) it.trainingScore + 1 else it.trainingScore,
                    trainingOutcome = outcome
                )
            }
            if (outcome.isGoal) triggerAudio(GameAudioEvent.GoalCrowd)
            delay(1600)
            _uiState.update { it.copy(trainingOutcome = null) }
        }
    }

    fun resetTraining() {
        _uiState.update {
            it.copy(
                trainingScore = 0,
                trainingAttempts = 0,
                trainingOutcome = null
            )
        }
    }

    fun updateSettings(newSettings: GameSettings) {
        statsRepository.updateSettings(newSettings)
    }

    fun resetCareerStats() {
        statsRepository.resetStats()
    }

    private fun triggerAudio(event: GameAudioEvent) {
        if (gameSettings.value.soundEnabled) {
            viewModelScope.launch {
                _audioEvents.emit(event)
            }
        }
    }
}
