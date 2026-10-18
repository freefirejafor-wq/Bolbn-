package com.penaltyarena.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.model.MatchCareerStats
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaEmeraldLight
import com.penaltyarena.game.ui.theme.ArenaGold
import com.penaltyarena.game.ui.theme.ArenaRed
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightCardBorder
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.ui.theme.TextMuted
import com.penaltyarena.game.ui.theme.TextWhite
import com.penaltyarena.game.viewmodel.Screen

@Composable
fun HomeScreen(
    careerStats: MatchCareerStats,
    onNavigate: (Screen) -> Unit
) {
    var showMultiplayerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumNightDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar with Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ArenaEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = StadiumNightDark,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PENALTY ARENA",
                        color = TextWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Native Football Shootout",
                        color = ArenaEmeraldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = { onNavigate(Screen.SETTINGS) }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Hero Match Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ArenaEmeraldLight.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                StadiumNightCard,
                                Color(0xFF0F2618)
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ArenaEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PHASE 1 • SINGLE PLAYER",
                        color = ArenaEmeraldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "5-ROUND PENALTY SHOOTOUT",
                    color = TextWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Compete as striker and goalkeeper against AI with realistic physics, ball arcs, and sudden death.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigate(Screen.MATCH_SETUP) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArenaEmerald)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = StadiumNightDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY MATCH",
                        color = StadiumNightDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Modes (Training & Tutorial)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.TRAINING) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = ArenaGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Training Camp",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Free practice",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.TUTORIAL) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = ArenaEmeraldLight,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How to Play",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Controls & Rules",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phase 2 Online Multiplayer (Coming Soon Badge)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMultiplayerDialog = true },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Online Multiplayer",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Matchmaking & PVP Rooms",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF334155))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMING SOON",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Local Career Statistics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ArenaGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAREER STATISTICS",
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "Win Rate: ${careerStats.winRatePercent}%",
                        color = ArenaEmeraldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox("MATCHES", "${careerStats.matchesPlayed}")
                    StatBox("WINS", "${careerStats.matchesWon}")
                    StatBox("GOALS", "${careerStats.goalsScored}")
                    StatBox("SAVES", "${careerStats.savesMade}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Roster notice
        Text(
            text = "Squad Notice: Original fictional players active (Leo Vance & Marc Sterling).",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }

    if (showMultiplayerDialog) {
        AlertDialog(
            onDismissRequest = { showMultiplayerDialog = false },
            title = {
                Text(
                    text = "Phase 2: Online Multiplayer",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Text(
                    text = "Penalty Arena is designed with a decoupled architecture. In Phase 1, the game engine runs 100% offline with zero external dependencies.\n\nPhase 2 will integrate a real-time WebSocket backend with player matchmaking, authenticated room lobbies, and server-verified ball trajectories.\n\nNo fake bots or mocked multiplayer matches are used.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showMultiplayerDialog = false }) {
                    Text(text = "GOT IT", color = ArenaEmerald)
                }
            },
            containerColor = StadiumNightCard
        )
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}
