package com.penaltyarena.game.repository

import android.content.Context
import android.content.SharedPreferences
import com.penaltyarena.game.model.MatchCareerStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GameSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val highFidelityGraphics: Boolean = true
)

class MatchStatsRepository(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("penalty_arena_prefs", Context.MODE_PRIVATE)

    private val _statsFlow = MutableStateFlow(loadStats())
    val statsFlow: StateFlow<MatchCareerStats> = _statsFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<GameSettings> = _settingsFlow.asStateFlow()

    private fun loadStats(): MatchCareerStats {
        val sp = prefs ?: return MatchCareerStats()
        return MatchCareerStats(
            matchesPlayed = sp.getInt("matches_played", 0),
            matchesWon = sp.getInt("matches_won", 0),
            goalsScored = sp.getInt("goals_scored", 0),
            savesMade = sp.getInt("saves_made", 0),
            cleanSheets = sp.getInt("clean_sheets", 0),
            winStreak = sp.getInt("win_streak", 0),
            bestStreak = sp.getInt("best_streak", 0)
        )
    }

    private fun loadSettings(): GameSettings {
        val sp = prefs ?: return GameSettings()
        return GameSettings(
            soundEnabled = sp.getBoolean("sound_enabled", true),
            hapticsEnabled = sp.getBoolean("haptics_enabled", true),
            reducedMotion = sp.getBoolean("reduced_motion", false),
            highFidelityGraphics = sp.getBoolean("high_fidelity", true)
        )
    }

    fun recordMatchResult(won: Boolean, goals: Int, saves: Int, opponentGoals: Int) {
        val current = _statsFlow.value
        val newPlayed = current.matchesPlayed + 1
        val newWon = if (won) current.matchesWon + 1 else current.matchesWon
        val newStreak = if (won) current.winStreak + 1 else 0
        val newBestStreak = maxOf(current.bestStreak, newStreak)
        val newCleanSheets = if (opponentGoals == 0) current.cleanSheets + 1 else current.cleanSheets

        val updated = current.copy(
            matchesPlayed = newPlayed,
            matchesWon = newWon,
            goalsScored = current.goalsScored + goals,
            savesMade = current.savesMade + saves,
            cleanSheets = newCleanSheets,
            winStreak = newStreak,
            bestStreak = newBestStreak
        )

        _statsFlow.value = updated

        prefs?.edit()?.apply {
            putInt("matches_played", updated.matchesPlayed)
            putInt("matches_won", updated.matchesWon)
            putInt("goals_scored", updated.goalsScored)
            putInt("saves_made", updated.savesMade)
            putInt("clean_sheets", updated.cleanSheets)
            putInt("win_streak", updated.winStreak)
            putInt("best_streak", updated.bestStreak)
            apply()
        }
    }

    fun updateSettings(newSettings: GameSettings) {
        _settingsFlow.value = newSettings
        prefs?.edit()?.apply {
            putBoolean("sound_enabled", newSettings.soundEnabled)
            putBoolean("haptics_enabled", newSettings.hapticsEnabled)
            putBoolean("reduced_motion", newSettings.reducedMotion)
            putBoolean("high_fidelity", newSettings.highFidelityGraphics)
            apply()
        }
    }

    fun resetStats() {
        _statsFlow.value = MatchCareerStats()
        prefs?.edit()?.apply {
            putInt("matches_played", 0)
            putInt("matches_won", 0)
            putInt("goals_scored", 0)
            putInt("saves_made", 0)
            putInt("clean_sheets", 0)
            putInt("win_streak", 0)
            putInt("best_streak", 0)
            apply()
        }
    }
}
