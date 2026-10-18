package com.penaltyarena.game.repository

import com.penaltyarena.game.model.DiveSpec
import com.penaltyarena.game.model.KickAttempt
import com.penaltyarena.game.model.ShotSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Architectural interface boundary for Phase 2 real online multiplayer.
 * Keeps gameplay rules strictly decoupled from networking so WebSocket / WebRTC
 * matchmaking, synchronized room lobbies, and server-verified physics can be plugged in
 * without rewriting the game engine.
 */
interface MultiplayerRepository {

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        data class Connected(val roomId: String, val opponentName: String) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    sealed class MultiplayerEvent {
        data class MatchFound(val roomId: String, val opponentName: String, val playerShootsFirst: Boolean) : MultiplayerEvent()
        data class OpponentShot(val shot: ShotSpec) : MultiplayerEvent()
        data class OpponentDive(val dive: DiveSpec) : MultiplayerEvent()
        data class RoundVerified(val round: Int, val attempt: KickAttempt) : MultiplayerEvent()
        data class OpponentDisconnected(val reason: String) : MultiplayerEvent()
    }

    val connectionState: Flow<ConnectionState>
    val events: Flow<MultiplayerEvent>

    suspend fun findMatch(region: String = "auto"): Result<String>
    suspend fun sendShot(shot: ShotSpec): Result<Unit>
    suspend fun sendDive(dive: DiveSpec): Result<Unit>
    suspend fun leaveMatch(): Result<Unit>
}

/**
 * Phase 1 Offline stub. Explicitly labels multiplayer as unavailable until
 * the Phase 2 backend service is connected and deployed.
 */
class OfflineMultiplayerStub : MultiplayerRepository {
    override val connectionState: Flow<MultiplayerRepository.ConnectionState> = emptyFlow()
    override val events: Flow<MultiplayerRepository.MultiplayerEvent> = emptyFlow()

    override suspend fun findMatch(region: String): Result<String> {
        return Result.failure(
            IllegalStateException("Phase 2 Backend Required: Online matchmaking service is not currently configured.")
        )
    }

    override suspend fun sendShot(shot: ShotSpec): Result<Unit> {
        return Result.failure(IllegalStateException("No active multiplayer session."))
    }

    override suspend fun sendDive(dive: DiveSpec): Result<Unit> {
        return Result.failure(IllegalStateException("No active multiplayer session."))
    }

    override suspend fun leaveMatch(): Result<Unit> {
        return Result.success(Unit)
    }
}
