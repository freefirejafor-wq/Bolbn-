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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.model.Difficulty
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaEmeraldLight
import com.penaltyarena.game.ui.theme.ArenaGold
import com.penaltyarena.game.ui.theme.ArenaRed
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.ui.theme.TextMuted
import com.penaltyarena.game.ui.theme.TextWhite
import com.penaltyarena.game.viewmodel.Screen

@Composable
fun MatchSetupScreen(
    currentDifficulty: Difficulty,
    onSelectDifficulty: (Difficulty) -> Unit,
    onStartMatch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StadiumNightDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MATCH SETUP",
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SELECT OPPONENT AI DIFFICULTY",
            color = ArenaEmeraldLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Difficulty.values().forEach { diff ->
            val isSelected = (diff == currentDifficulty)
            val borderColor = if (isSelected) ArenaEmerald else Color(0xFF1E293B)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable { onSelectDifficulty(diff) },
                colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectDifficulty(diff) },
                        colors = RadioButtonDefaults.colors(selectedColor = ArenaEmerald)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = diff.title,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = diff.description,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selected Squad Information
        Text(
            text = "ACTIVE SQUAD (ORIGINAL FICTIONAL ROSTER)",
            color = ArenaGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PlayerRow(role = "Striker", name = "Leo Vance #9 (Fictional)", note = "Controlled curve & placement")
                Spacer(modifier = Modifier.height(10.dp))
                PlayerRow(role = "Goalkeeper", name = "Marc Sterling #1 (Fictional)", note = "Agile reach & reflex saves")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Kick Off CTA
        Button(
            onClick = onStartMatch,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ArenaEmerald),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = StadiumNightDark)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KICK OFF SHOOTOUT",
                color = StadiumNightDark,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun PlayerRow(role: String, name: String, note: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = ArenaEmeraldLight, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = "$role: $name", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = note, color = TextMuted, fontSize = 11.sp)
        }
    }
}
