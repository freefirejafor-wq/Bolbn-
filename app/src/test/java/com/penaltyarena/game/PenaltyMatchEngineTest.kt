package com.penaltyarena.game

import com.penaltyarena.game.engine.PenaltyMatchEngine
import com.penaltyarena.game.model.Difficulty
import com.penaltyarena.game.model.DiveSpec
import com.penaltyarena.game.model.GoalSector
import com.penaltyarena.game.model.ShotOutcome
import com.penaltyarena.game.model.ShotSpec
import com.penaltyarena.game.model.Team
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PenaltyMatchEngineTest {

    @Test
    fun testInitialMatchState() {
        val engine = PenaltyMatchEngine(Difficulty.NORMAL)
        assertEquals(0, engine.userScore)
        assertEquals(0, engine.aiScore)
        assertEquals(1, engine.currentRound)
        assertTrue(engine.isUserTurnToShoot)
        assertFalse(engine.isMatchFinished)
        assertFalse(engine.isSuddenDeath)
    }

    @Test
    fun testShotOutcome_SkiedOverTheBar() {
        val engine = PenaltyMatchEngine(Difficulty.NORMAL)
        // High power shot (> 0.93f)
        val overpoweredShot = ShotSpec(aimX = 0.0f, aimY = 0.6f, power = 0.98f)
        val dummyDive = DiveSpec(GoalSector.CENTER_HIGH)

        val outcome = engine.calculateOutcome(overpoweredShot, dummyDive, Difficulty.NORMAL)
        assertEquals(ShotOutcome.OVER_THE_BAR, outcome)
    }

    @Test
    fun testShotOutcome_WideMiss() {
        val engine = PenaltyMatchEngine(Difficulty.NORMAL)
        // Far right (> 0.92f)
        val wideShot = ShotSpec(aimX = 0.98f, aimY = 0.4f, power = 0.75f)
        val dummyDive = DiveSpec(GoalSector.TOP_RIGHT)

        val outcome = engine.calculateOutcome(wideShot, dummyDive, Difficulty.NORMAL)
        assertEquals(ShotOutcome.WIDE, outcome)
    }

    @Test
    fun testShotOutcome_PostHit() {
        val engine = PenaltyMatchEngine(Difficulty.NORMAL)
        // Aimed right on left post (-0.84f)
        val postShot = ShotSpec(aimX = -0.84f, aimY = 0.5f, power = 0.75f)
        val dummyDive = DiveSpec(GoalSector.BOTTOM_RIGHT)

        val outcome = engine.calculateOutcome(postShot, dummyDive, Difficulty.NORMAL)
        assertEquals(ShotOutcome.POST_HIT, outcome)
    }

    @Test
    fun testShotOutcome_CrossbarHit() {
        val engine = PenaltyMatchEngine(Difficulty.NORMAL)
        // Aimed right on crossbar (y = 0.88f)
        val barShot = ShotSpec(aimX = 0.1f, aimY = 0.88f, power = 0.75f)
        val dummyDive = DiveSpec(GoalSector.BOTTOM_RIGHT)

        val outcome = engine.calculateOutcome(barShot, dummyDive, Difficulty.NORMAL)
        assertEquals(ShotOutcome.CROSSBAR_HIT, outcome)
    }

    @Test
    fun testEarlyMatchClinch_UserWins3_0AfterRound3() {
        // Deterministic random seed where User shots score and AI shots miss/save
        val engine = PenaltyMatchEngine(Difficulty.NORMAL, Random(42))

        // Perfect corner shot with optimal power
        val goalShot = ShotSpec(aimX = -0.7f, aimY = 0.75f, power = 0.75f)
        val wrongSideDive = DiveSpec(GoalSector.BOTTOM_RIGHT)

        // Errant AI shot
        val errantShot = ShotSpec(aimX = 0.99f, aimY = 0.5f, power = 0.75f)

        // Round 1
        engine.executeKick(goalShot, wrongSideDive) // User scores (1-0)
        engine.executeKick(errantShot, wrongSideDive) // AI misses (1-0)
        assertFalse(engine.isMatchFinished)
        assertEquals(2, engine.currentRound)

        // Round 2
        engine.executeKick(goalShot, wrongSideDive) // User scores (2-0)
        engine.executeKick(errantShot, wrongSideDive) // AI misses (2-0)
        assertFalse(engine.isMatchFinished)
        assertEquals(3, engine.currentRound)

        // Round 3
        engine.executeKick(goalShot, wrongSideDive) // User scores (3-0)
        engine.executeKick(errantShot, wrongSideDive) // AI misses (3-0)

        // After round 3: User leads 3-0. AI only has 2 remaining kicks, max possible AI score is 2.
        // Match must end mathematically early!
        assertTrue(engine.isMatchFinished)
        assertEquals(Team.USER, engine.winner)
    }

    @Test
    fun testSuddenDeathProgression() {
        val engine = PenaltyMatchEngine(Difficulty.NORMAL, Random(100))

        val errantShot = ShotSpec(aimX = 0.99f, aimY = 0.5f, power = 0.75f)
        val dummyDive = DiveSpec(GoalSector.CENTER_LOW)

        // Simulate 5 tied rounds (0-0)
        for (r in 1..5) {
            engine.executeKick(errantShot, dummyDive) // User misses
            engine.executeKick(errantShot, dummyDive) // AI misses
        }

        // Must enter sudden death at round 6!
        assertTrue(engine.isSuddenDeath)
        assertEquals(6, engine.currentRound)
        assertFalse(engine.isMatchFinished)

        // Sudden death Round 6: User scores, AI misses
        val goalShot = ShotSpec(aimX = -0.7f, aimY = 0.75f, power = 0.75f)
        val wrongDive = DiveSpec(GoalSector.BOTTOM_RIGHT)

        engine.executeKick(goalShot, wrongDive) // User scores
        engine.executeKick(errantShot, dummyDive) // AI misses

        // Match should finish with User victory in sudden death!
        assertTrue(engine.isMatchFinished)
        assertEquals(Team.USER, engine.winner)
        assertEquals(1, engine.userScore)
        assertEquals(0, engine.aiScore)
    }

    @Test
    fun testAiDecisionGeneration() {
        val engine = PenaltyMatchEngine(Difficulty.HARD)

        val aiShot = engine.generateAiShot()
        assertNotNull(aiShot)
        assertTrue(aiShot.power in 0.0f..1.0f)

        val playerShot = ShotSpec(aimX = 0.6f, aimY = 0.7f, power = 0.75f)
        val aiDive = engine.generateAiGoalkeeperDive(playerShot)
        assertNotNull(aiDive)
        assertNotNull(aiDive.sector)
    }
}
