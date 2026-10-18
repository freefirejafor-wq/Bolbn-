package com.penaltyarena.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.model.ShotOutcome
import com.penaltyarena.game.model.Team
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaEmeraldLight
import com.penaltyarena.game.ui.theme.ArenaGold
import com.penaltyarena.game.ui.theme.ArenaRed
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.ui.theme.TextMuted
import com.penaltyarena.game.ui.theme.TextWhite
import com.penaltyarena.game.viewmodel.MatchUiState
import com.penaltyarena.game.viewmodel.Screen

@Composable
fun ResultsScreen(
    state: MatchUiState,
    onPlayAgain: () -> Unit,
    onNavigateHome: () -> Unit
) {
    val isWinner = (state.matchWinner == Team.USER)
    val accentColor = if (isWinner) ArenaEmerald else ArenaRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumNightDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Trophy / Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f))
                .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isWinner) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isWinner) "VICTORY!" else "DEFEAT",
            color = TextWhite,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            letterSpacing = 1.sp
        )

        Text(
            text = if (isWinner) "You won the penalty shootout!" else "Rival United took the victory.",
            color = TextMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.isSuddenDeath) "FINAL SCORE (SUDDEN DEATH)" else "FINAL SCORE (5 ROUNDS)",
                    color = ArenaGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "PLAYER FC", color = ArenaEmeraldLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${state.userScore}", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 42.sp)
                    }

                    Text(
                        text = "—",
                        color = TextMuted,
                        fontWeight = FontWeight.Light,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "RIVAL UNITED", color = ArenaGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${state.aiScore}", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 42.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val userGoals = state.history.count { it.shootingTeam == Team.USER && it.outcome.isGoal }
                val userMisses = state.history.count { it.shootingTeam == Team.USER && !it.outcome.isGoal }
                val userSaves = state.history.count { it.shootingTeam == Team.AI && it.outcome == ShotOutcome.SAVED }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResultStatItem("Goals Scored", "$userGoals")
                    ResultStatItem("Saves Made", "$userSaves")
                    ResultStatItem("Shots Missed", "$userMisses")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Play Again CTA
        Button(
            onClick = onPlayAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ArenaEmerald)
        ) {
            Icon(Icons.Default.Replay, contentDescription = null, tint = StadiumNightDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PLAY AGAIN",
                color = StadiumNightDark,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Return Home
        OutlinedButton(
            onClick = onNavigateHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = TextWhite)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RETURN TO ARENA",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ResultStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}
