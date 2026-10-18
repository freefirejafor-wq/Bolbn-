package com.penaltyarena.game.model

/**
 * Core domain models for the Penalty Arena football penalty shootout engine.
 */

enum class TeamRole {
    STRIKER,
    GOALKEEPER
}

enum class Team {
    USER,
    AI;

    val displayName: String
        get() = when (this) {
            USER -> "Player FC"
            AI -> "Rival United"
        }
}

enum class Difficulty(
    val title: String,
    val description: String,
    val saveProbabilityMultiplier: Float,
    val reactionDelayMs: Long,
    val shotAccuracyMultiplier: Float
) {
    EASY(
        title = "Amateur (Easy)",
        description = "Goalkeeper has slower reflexes and dives predictably. Striker shoots with moderate pace.",
        saveProbabilityMultiplier = 0.55f,
        reactionDelayMs = 280L,
        shotAccuracyMultiplier = 0.65f
    ),
    NORMAL(
        title = "Semi-Pro (Normal)",
        description = "Goalkeeper anticipates well and covers realistic radius. Striker aims toward corners with good power.",
        saveProbabilityMultiplier = 0.85f,
        reactionDelayMs = 150L,
        shotAccuracyMultiplier = 0.85f
    ),
    HARD(
        title = "World Class (Hard)",
        description = "Elite reflexes and anticipation. Goalkeeper covers upper corners. Striker places explosive curling shots.",
        saveProbabilityMultiplier = 1.15f,
        reactionDelayMs = 60L,
        shotAccuracyMultiplier = 1.0f
    )
}

enum class GoalSector(val label: String, val targetX: Float, val targetY: Float) {
    TOP_LEFT("Top Left", -0.7f, 0.75f),
    BOTTOM_LEFT("Bottom Left", -0.7f, 0.2f),
    CENTER_HIGH("Center High", 0.0f, 0.75f),
    CENTER_LOW("Center Low", 0.0f, 0.2f),
    TOP_RIGHT("Top Right", 0.7f, 0.75f),
    BOTTOM_RIGHT("Bottom Right", 0.7f, 0.2f);

    companion object {
        fun fromCoords(x: Float, y: Float): GoalSector {
            val isLeft = x < -0.25f
            val isRight = x > 0.25f
            val isHigh = y > 0.45f

            return when {
                isLeft && isHigh -> TOP_LEFT
                isLeft && !isHigh -> BOTTOM_LEFT
                isRight && isHigh -> TOP_RIGHT
                isRight && !isHigh -> BOTTOM_RIGHT
                isHigh -> CENTER_HIGH
                else -> CENTER_LOW
            }
        }
    }
}

enum class ShotOutcome(val message: String, val isGoal: Boolean) {
    GOAL("GOAL! Top class finish!", true),
    SAVED("SAVED! Sensational stop!", false),
    POST_HIT("WOODWORK! Off the post!", false),
    CROSSBAR_HIT("CROSSBAR! Inches away!", false),
    WIDE("WIDE! Missed the target!", false),
    OVER_THE_BAR("OVER! Skied into the stands!", false)
}

data class ShotSpec(
    val aimX: Float, // -1.0 (far left) to 1.0 (far right)
    val aimY: Float, // 0.0 (ground) to 1.0 (crossbar height and above)
    val power: Float, // 0.0 to 1.0 (0.6 - 0.85 is optimum green zone)
    val spin: Float = 0.0f // -1.0 (curve left) to 1.0 (curve right)
)

data class DiveSpec(
    val sector: GoalSector,
    val timingOffsetMs: Long = 0L // negative = anticipated early, positive = late
)

data class KickAttempt(
    val round: Int,
    val shootingTeam: Team,
    val shotSpec: ShotSpec,
    val diveSpec: DiveSpec,
    val outcome: ShotOutcome
)

data class MatchCareerStats(
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val goalsScored: Int = 0,
    val savesMade: Int = 0,
    val cleanSheets: Int = 0,
    val winStreak: Int = 0,
    val bestStreak: Int = 0
) {
    val winRatePercent: Int
        get() = if (matchesPlayed > 0) (matchesWon * 100) / matchesPlayed else 0
}

data class PlayerRosterItem(
    val id: String,
    val name: String,
    val number: Int,
    val isFictional: Boolean = true,
    val role: TeamRole
)
