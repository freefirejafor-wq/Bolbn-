package com.penaltyarena.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.model.GoalSector
import com.penaltyarena.game.model.ShotOutcome
import com.penaltyarena.game.model.ShotSpec
import com.penaltyarena.game.ui.components.ArenaStadiumView
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaGold
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.ui.theme.TextMuted
import com.penaltyarena.game.ui.theme.TextWhite
import com.penaltyarena.game.viewmodel.BallFlightState
import com.penaltyarena.game.viewmodel.MatchPhase

@Composable
fun TrainingScreen(
    score: Int,
    attempts: Int,
    outcome: ShotOutcome?,
    onPracticeShot: (ShotSpec) -> Unit,
    onResetTraining: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumNightDark)
            .padding(top = 12.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TRAINING CAMP",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            IconButton(onClick = onResetTraining) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Training stats bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goals: $score / $attempts",
                    color = ArenaEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                val conversionRate = if (attempts > 0) (score * 100) / attempts else 0
                Text(
                    text = "Accuracy: $conversionRate%",
                    color = ArenaGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stadium practice canvas
        ArenaStadiumView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            isUserShooting = true,
            phase = if (outcome != null) MatchPhase.OUTCOME_CELEBRATION else MatchPhase.AWAITING_INPUT,
            flightState = BallFlightState(outcome = outcome),
            onKickShot = onPracticeShot,
            onUserDive = {}
        )
    }
}
