package com.penaltyarena.game.engine

import com.penaltyarena.game.model.Difficulty
import com.penaltyarena.game.model.DiveSpec
import com.penaltyarena.game.model.GoalSector
import com.penaltyarena.game.model.KickAttempt
import com.penaltyarena.game.model.ShotOutcome
import com.penaltyarena.game.model.ShotSpec
import com.penaltyarena.game.model.Team
import kotlin.math.abs
import kotlin.random.Random

class PenaltyMatchEngine(
    val difficulty: Difficulty = Difficulty.NORMAL,
    private val random: Random = Random.Default
) {
    var userScore: Int = 0
        private set
    var aiScore: Int = 0
        private set

    var currentRound: Int = 1
        private set

    var isUserTurnToShoot: Boolean = true
        private set

    var isMatchFinished: Boolean = false
        private set

    var winner: Team? = null
        private set

    var isSuddenDeath: Boolean = false
        private set

    private val _history = mutableListOf<KickAttempt>()
    val history: List<KickAttempt> get() = _history

    val userAttempts: List<KickAttempt> get() = _history.filter { it.shootingTeam == Team.USER }
    val aiAttempts: List<KickAttempt> get() = _history.filter { it.shootingTeam == Team.AI }

    fun executeKick(
        shot: ShotSpec,
        dive: DiveSpec
    ): KickAttempt {
        check(!isMatchFinished) { "Cannot execute kick: match is already finished" }

        val activeShooter = if (isUserTurnToShoot) Team.USER else Team.AI
        val outcome = calculateOutcome(shot, dive, difficulty)

        if (outcome.isGoal) {
            if (activeShooter == Team.USER) {
                userScore++
            } else {
                aiScore++
            }
        }

        val attempt = KickAttempt(
            round = currentRound,
            shootingTeam = activeShooter,
            shotSpec = shot,
            diveSpec = dive,
            outcome = outcome
        )
        _history.add(attempt)

        updateTurnAndEvaluateMatch()
        return attempt
    }

    fun calculateOutcome(
        shot: ShotSpec,
        dive: DiveSpec,
        diff: Difficulty
    ): ShotOutcome {
        val effectiveAimX = shot.aimX + (shot.spin * 0.12f)
        val effectiveAimY = shot.aimY

        if (shot.power > 0.93f || effectiveAimY > 0.94f) {
            return ShotOutcome.OVER_THE_BAR
        }

        if (abs(effectiveAimX) > 0.92f) {
            return ShotOutcome.WIDE
        }

        val nearLeftPost = abs(effectiveAimX - (-0.84f)) < 0.05f
        val nearRightPost = abs(effectiveAimX - 0.84f) < 0.05f
        if ((nearLeftPost || nearRightPost) && effectiveAimY in 0.05f..0.88f) {
            return ShotOutcome.POST_HIT
        }

        if (abs(effectiveAimY - 0.88f) < 0.045f && abs(effectiveAimX) < 0.84f) {
            return ShotOutcome.CROSSBAR_HIT
        }

        if (shot.power < 0.20f) {
            return ShotOutcome.SAVED
        }

        val ballSector = GoalSector.fromCoords(effectiveAimX, effectiveAimY)
        val isExactSectorMatch = (dive.sector == ballSector)
        val isSameSide = isSameHorizontalSide(dive.sector, ballSector)

        val saveChance = when {
            isExactSectorMatch -> {
                val optimalPowerBonus = if (shot.power in 0.68f..0.85f) 0.18f else 0.0f
                var baseChance = 0.82f - optimalPowerBonus
                baseChance *= diff.saveProbabilityMultiplier
                baseChance.coerceIn(0.20f, 0.92f)
            }
            isSameSide -> {
                val base = 0.28f * diff.saveProbabilityMultiplier
                base.coerceIn(0.05f, 0.45f)
            }
            else -> 0.02f
        }

        val roll = random.nextFloat()
        return if (roll < saveChance) {
            ShotOutcome.SAVED
        } else {
            ShotOutcome.GOAL
        }
    }

    private fun isSameHorizontalSide(sectorA: GoalSector, sectorB: GoalSector): Boolean {
        val aLeft = sectorA == GoalSector.TOP_LEFT || sectorA == GoalSector.BOTTOM_LEFT
        val bLeft = sectorB == GoalSector.TOP_LEFT || sectorB == GoalSector.BOTTOM_LEFT
        if (aLeft && bLeft) return true

        val aRight = sectorA == GoalSector.TOP_RIGHT || sectorA == GoalSector.BOTTOM_RIGHT
        val bRight = sectorB == GoalSector.TOP_RIGHT || sectorB == GoalSector.BOTTOM_RIGHT
        return aRight && bRight
    }

    fun generateAiShot(): ShotSpec {
        return when (difficulty) {
            Difficulty.EASY -> {
                val errantRoll = random.nextFloat()
                if (errantRoll < 0.20f) {
                    ShotSpec(
                        aimX = if (random.nextBoolean()) -0.96f else 0.96f,
                        aimY = random.nextFloat() * 0.7f,
                        power = 0.5f + random.nextFloat() * 0.3f
                    )
                } else {
                    ShotSpec(
                        aimX = (random.nextFloat() * 1.2f) - 0.6f,
                        aimY = random.nextFloat() * 0.65f,
                        power = 0.55f + random.nextFloat() * 0.25f,
                        spin = (random.nextFloat() - 0.5f) * 0.4f
                    )
                }
            }
            Difficulty.NORMAL -> {
                val targetSectors = listOf(
                    GoalSector.BOTTOM_LEFT,
                    GoalSector.BOTTOM_RIGHT,
                    GoalSector.TOP_LEFT,
                    GoalSector.TOP_RIGHT,
                    GoalSector.CENTER_LOW
                )
                val target = targetSectors.random(random)
                ShotSpec(
                    aimX = target.targetX + (random.nextFloat() - 0.5f) * 0.15f,
                    aimY = target.targetY + (random.nextFloat() - 0.5f) * 0.15f,
                    power = 0.68f + random.nextFloat() * 0.18f,
                    spin = (random.nextFloat() - 0.5f) * 0.6f
                )
            }
            Difficulty.HARD -> {
                val targetSectors = listOf(
                    GoalSector.TOP_LEFT,
                    GoalSector.TOP_RIGHT,
                    GoalSector.BOTTOM_LEFT,
                    GoalSector.BOTTOM_RIGHT
                )
                val target = targetSectors.random(random)
                ShotSpec(
                    aimX = (target.targetX * 0.95f) + (random.nextFloat() - 0.5f) * 0.08f,
                    aimY = (target.targetY * 0.95f) + (random.nextFloat() - 0.5f) * 0.08f,
                    power = 0.75f + random.nextFloat() * 0.14f,
                    spin = (random.nextFloat() - 0.5f) * 0.8f
                )
            }
        }
    }

    fun generateAiGoalkeeperDive(playerShot: ShotSpec): DiveSpec {
        val playerSector = GoalSector.fromCoords(
            playerShot.aimX + (playerShot.spin * 0.12f),
            playerShot.aimY
        )

        val predictCorrectSectorRoll = random.nextFloat()
        val anticipationThreshold = when (difficulty) {
            Difficulty.EASY -> 0.40f
            Difficulty.NORMAL -> 0.65f
            Difficulty.HARD -> 0.85f
        }

        val chosenSector = if (predictCorrectSectorRoll < anticipationThreshold) {
            playerSector
        } else {
            GoalSector.values().filter { it != playerSector }.random(random)
        }

        return DiveSpec(
            sector = chosenSector,
            timingOffsetMs = when (difficulty) {
                Difficulty.EASY -> 120L
                Difficulty.NORMAL -> 30L
                Difficulty.HARD -> -10L
            }
        )
    }

    private fun updateTurnAndEvaluateMatch() {
        if (isUserTurnToShoot) {
            isUserTurnToShoot = false
            checkEarlyClinch()
        } else {
            isUserTurnToShoot = true
            evaluateRoundEnd()
        }
    }

    private fun checkEarlyClinch() {
        if (currentRound <= 5 && !isSuddenDeath) {
            val userRemainingKicks = 5 - userAttempts.size
            val aiRemainingKicks = 5 - aiAttempts.size

            if (userScore > aiScore + aiRemainingKicks) {
                finishMatch(Team.USER)
                return
            }
            if (aiScore > userScore + userRemainingKicks) {
                finishMatch(Team.AI)
                return
            }
        }
    }

    private fun evaluateRoundEnd() {
        if (!isSuddenDeath && currentRound < 5) {
            val userRemaining = 5 - currentRound
            val aiRemaining = 5 - currentRound

            if (userScore > aiScore + aiRemaining) {
                finishMatch(Team.USER)
                return
            }
            if (aiScore > userScore + userRemaining) {
                finishMatch(Team.AI)
                return
            }

            currentRound++
            return
        }

        if (!isSuddenDeath && currentRound == 5) {
            if (userScore > aiScore) {
                finishMatch(Team.USER)
            } else if (aiScore > userScore) {
                finishMatch(Team.AI)
            } else {
                isSuddenDeath = true
                currentRound = 6
            }
            return
        }

        if (isSuddenDeath) {
            if (userScore > aiScore) {
                finishMatch(Team.USER)
            } else if (aiScore > userScore) {
                finishMatch(Team.AI)
            } else {
                currentRound++
            }
        }
    }

    private fun finishMatch(winnerTeam: Team) {
        isMatchFinished = true
        winner = winnerTeam
    }

    fun reset() {
        userScore = 0
        aiScore = 0
        currentRound = 1
        isUserTurnToShoot = true
        isMatchFinished = false
        winner = null
        isSuddenDeath = false
        _history.clear()
    }
}
