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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun TutorialScreen(
    onNavigateBack: () -> Unit,
    onStartTraining: () -> Unit
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
                text = "HOW TO PLAY",
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        TutorialStepCard(
            stepNumber = "1",
            icon = Icons.Default.SportsSoccer,
            title = "Aiming Your Penalty",
            description = "When shooting, touch and drag anywhere across the goal area to place your target reticle. Aiming into top or bottom corners gives goalkeepers the hardest time, but straying too far wide risks hitting the post or woodwork."
        )

        Spacer(modifier = Modifier.height(14.dp))

        TutorialStepCard(
            stepNumber = "2",
            icon = Icons.Default.Speed,
            title = "The Optimum Power Meter",
            description = "Adjust the power slider before striking. The green sweet spot is 65% – 85% power. Going above 90% risks skying the ball over the crossbar into the stands, while below 30% results in a tame shot easily caught."
        )

        Spacer(modifier = Modifier.height(14.dp))

        TutorialStepCard(
            stepNumber = "3",
            icon = Icons.Default.PanTool,
            title = "Goalkeeping & Diving",
            description = "When defending, select one of the 6 dive sectors (Top Left, Low Left, High Mid, Low Mid, Top Right, Low Right). Anticipating the shooter's corner and matching the height makes the crucial match-winning save."
        )

        Spacer(modifier = Modifier.height(14.dp))

        TutorialStepCard(
            stepNumber = "4",
            icon = Icons.Default.CheckCircle,
            title = "Match Rules & Early Clinch",
            description = "A standard shootout lasts 5 rounds. If one team creates an insurmountable lead that the opponent mathematically cannot catch (e.g. 3-0 after round 3), the match ends immediately. If tied after 5 rounds, sudden death begins!"
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStartTraining,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ArenaEmerald),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "PRACTICE IN TRAINING CAMP",
                color = StadiumNightDark,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun TutorialStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ArenaEmerald.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ArenaEmerald, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "STEP $stepNumber: $title",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
