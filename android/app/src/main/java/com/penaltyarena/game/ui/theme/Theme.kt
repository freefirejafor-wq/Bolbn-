package com.penaltyarena.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ArenaEmerald,
    secondary = ArenaGold,
    tertiary = ArenaEmeraldLight,
    background = StadiumNightDark,
    surface = StadiumNightCard,
    onPrimary = StadiumNightDark,
    onSecondary = StadiumNightDark,
    onTertiary = StadiumNightDark,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun PenaltyArenaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
