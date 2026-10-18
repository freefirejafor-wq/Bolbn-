package com.penaltyarena.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.model.GoalSector
import com.penaltyarena.game.model.KickAttempt
import com.penaltyarena.game.model.ShotSpec
import com.penaltyarena.game.model.Team
import com.penaltyarena.game.ui.components.ArenaStadiumView
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaEmeraldLight
import com.penaltyarena.game.ui.theme.ArenaGold
import com.penaltyarena.game.ui.theme.ArenaRed
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.ui.theme.TextMuted
import com.penaltyarena.game.ui.theme.TextWhite
import com.penaltyarena.game.viewmodel.MatchPhase
import com.penaltyarena.game.viewmodel.MatchUiState

@Composable
fun MatchScreen(
    state: MatchUiState,
    onUserShoot: (ShotSpec) -> Unit,
    onUserDive: (GoalSector) -> Unit,
    onAbandonMatch: () -> Unit
) {
    var showQuitDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumNightDark)
            .padding(top = 12.dp, bottom = 8.dp, start = 12.dp, end = 12.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { showQuitDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Quit Match",
                    tint = TextMuted
                )
            }

            // Round & Sudden Death Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (state.isSuddenDeath) ArenaRed.copy(alpha = 0.2f) else Color(0xFF1E293B))
                    .border(
                        1.dp,
                        if (state.isSuddenDeath) ArenaRed else ArenaEmerald.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (state.isSuddenDeath) "SUDDEN DEATH • ROUND ${state.currentRound}" else "ROUND ${state.currentRound} OF 5",
                    color = if (state.isSuddenDeath) ArenaRed else ArenaEmeraldLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
            }

            // Difficulty tag
            Text(
                text = state.difficulty.name,
                color = ArenaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Match Scoreboard HUD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Player side
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "PLAYER FC",
                            color = ArenaEmeraldLight,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShootoutTrackerRow(
                            attempts = state.history.filter { it.shootingTeam == Team.USER },
                            totalRounds = maxOf(5, state.currentRound)
                        )
                    }

                    // Score Display
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0B1424))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.userScore}  -  ${state.aiScore}",
                            color = TextWhite,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }

                    // AI side
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "RIVAL UNITED",
                            color = ArenaGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShootoutTrackerRow(
                            attempts = state.history.filter { it.shootingTeam == Team.AI },
                            totalRounds = maxOf(5, state.currentRound)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Active Role Banner
        val roleBannerColor = if (state.isUserShooting) ArenaEmerald else ArenaGold
        val roleText = if (state.isUserShooting) "YOU ARE SHOOTING (Striker)" else "OPPONENT SHOOTING (Defend in Goal)"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(roleBannerColor.copy(alpha = 0.15f))
                .border(1.dp, roleBannerColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = roleText,
                color = roleBannerColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stadium 2.5D View
        ArenaStadiumView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            isUserShooting = state.isUserShooting,
            phase = state.phase,
            flightState = state.flightState,
            onKickShot = onUserShoot,
            onUserDive = onUserDive
        )
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text(text = "Abandon Match?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Leaving will forfeit the current penalty shootout.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onAbandonMatch()
                }) {
                    Text(text = "FORFEIT", color = ArenaRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text(text = "RESUME", color = ArenaEmerald)
                }
            },
            containerColor = StadiumNightCard
        )
    }
}

@Composable
private fun ShootoutTrackerRow(
    attempts: List<KickAttempt>,
    totalRounds: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val displayCount = maxOf(5, totalRounds)
        for (i in 0 until displayCount) {
            val attempt = attempts.getOrNull(i)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            attempt == null -> Color(0xFF1E293B)
                            attempt.outcome.isGoal -> ArenaEmerald
                            else -> ArenaRed
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (attempt != null) {
                    Icon(
                        imageVector = if (attempt.outcome.isGoal) Icons.Default.Done else Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }
        }
    }
}
