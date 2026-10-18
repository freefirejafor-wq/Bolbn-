package com.penaltyarena.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.repository.GameSettings
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaRed
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.ui.theme.TextMuted
import com.penaltyarena.game.ui.theme.TextWhite

@Composable
fun SettingsScreen(
    settings: GameSettings,
    onUpdateSettings: (GameSettings) -> Unit,
    onResetStats: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }

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
                text = "SETTINGS",
                color = TextWhite,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio & Feedback Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound Effects",
                    subtitle = "Stadium crowd cheers, kicks, woodwork collisions, and whistles",
                    checked = settings.soundEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.Vibration,
                    title = "Haptic Feedback",
                    subtitle = "Device vibrations on shot strikes and goal post impacts",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { onUpdateSettings(settings.copy(hapticsEnabled = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.MotionPhotosOff,
                    title = "Reduced Motion",
                    subtitle = "Minimize camera shakes and rapid celebration animations",
                    checked = settings.reducedMotion,
                    onCheckedChange = { onUpdateSettings(settings.copy(reducedMotion = it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSwitchRow(
                    icon = Icons.Default.GraphicEq,
                    title = "High Fidelity Pitch Lighting",
                    subtitle = "Enhanced floodlight halos, grass shading, and dynamic ball shadows",
                    checked = settings.highFidelityGraphics,
                    onCheckedChange = { onUpdateSettings(settings.copy(highFidelityGraphics = it)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Career Statistics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StadiumNightCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Career Data",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clear local history of matches played, wins, goals, and save streaks.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ArenaRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RESET CAREER STATS", color = ArenaRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset All Career Stats?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text(text = "This will permanently reset your match wins, goals scored, and saves to zero.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    onResetStats()
                }) {
                    Text(text = "RESET", color = ArenaRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "CANCEL", color = ArenaEmerald)
                }
            },
            containerColor = StadiumNightCard
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ArenaEmerald,
                checkedTrackColor = Color(0xFF0F3D24)
            )
        )
    }
}
