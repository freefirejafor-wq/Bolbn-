package com.penaltyarena.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.penaltyarena.game.model.GoalSector
import com.penaltyarena.game.model.ShotOutcome
import com.penaltyarena.game.model.ShotSpec
import com.penaltyarena.game.ui.theme.ArenaEmerald
import com.penaltyarena.game.ui.theme.ArenaGold
import com.penaltyarena.game.ui.theme.ArenaRed
import com.penaltyarena.game.ui.theme.PitchGreenDark
import com.penaltyarena.game.ui.theme.PitchGreenLight
import com.penaltyarena.game.ui.theme.StadiumNightCard
import com.penaltyarena.game.ui.theme.StadiumNightDark
import com.penaltyarena.game.viewmodel.BallFlightState
import com.penaltyarena.game.viewmodel.MatchPhase
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2.5D / 3D Perspective Stadium Canvas component.
 * Features realistic night arena floodlights, grass perspective, articulated goalkeeper,
 * net mesh, physics ball trajectory with shadow, and interactive touch controls.
 */
@Composable
fun ArenaStadiumView(
    modifier: Modifier = Modifier,
    isUserShooting: Boolean,
    phase: MatchPhase,
    flightState: BallFlightState,
    onKickShot: (ShotSpec) -> Unit,
    onUserDive: (GoalSector) -> Unit
) {
    // Striker control state
    var aimX by remember { mutableFloatStateOf(0.0f) }
    var aimY by remember { mutableFloatStateOf(0.45f) }
    var power by remember { mutableFloatStateOf(0.75f) }
    var spin by remember { mutableFloatStateOf(0.0f) }

    // Goalkeeper subtle idle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "idle_bounce")
    val idleBounce by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gk_idle_bounce"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(StadiumNightDark)
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        // Main Rendering Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isUserShooting, phase) {
                    if (isUserShooting && phase == MatchPhase.AWAITING_INPUT) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // Convert drag to aim coordinates (-1.0 to 1.0)
                            aimX = (aimX + (dragAmount.x / (size.width * 0.45f))).coerceIn(-1.1f, 1.1f)
                            aimY = (aimY - (dragAmount.y / (size.height * 0.35f))).coerceIn(0.0f, 1.15f)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            // 1. Draw Night Stadium Atmosphere & Floodlights
            drawStadiumAtmosphere(w, h)

            // 2. Draw Perspective Pitch & Markings
            drawPerspectivePitch(w, h)

            // 3. Draw 3D Goal Frame & Net
            val goalLeft = w * 0.16f
            val goalRight = w * 0.84f
            val goalTop = h * 0.22f
            val goalBottom = h * 0.58f

            drawGoalAndNet(
                left = goalLeft,
                right = goalRight,
                top = goalTop,
                bottom = goalBottom,
                netBulge = if (flightState.outcome?.isGoal == true) 14f else 0f
            )

            // 4. Draw Goalkeeper
            val gkX = when (phase) {
                MatchPhase.AWAITING_INPUT -> (goalLeft + goalRight) / 2f
                MatchPhase.BALL_IN_FLIGHT, MatchPhase.OUTCOME_CELEBRATION -> {
                    val targetSectorX = (goalLeft + goalRight) / 2f + (flightState.goalkeeperDiveSector.targetX * (goalRight - goalLeft) * 0.45f)
                    val baseMid = (goalLeft + goalRight) / 2f
                    baseMid + (targetSectorX - baseMid) * flightState.goalkeeperProgress
                }
                else -> (goalLeft + goalRight) / 2f
            }

            val gkY = when (phase) {
                MatchPhase.AWAITING_INPUT -> goalBottom - 10f + idleBounce
                MatchPhase.BALL_IN_FLIGHT, MatchPhase.OUTCOME_CELEBRATION -> {
                    val diveSector = flightState.goalkeeperDiveSector
                    val targetY = if (diveSector == GoalSector.TOP_LEFT || diveSector == GoalSector.TOP_RIGHT || diveSector == GoalSector.CENTER_HIGH) {
                        goalTop + (goalBottom - goalTop) * 0.35f
                    } else {
                        goalBottom - 15f
                    }
                    val baseY = goalBottom - 10f
                    baseY + (targetY - baseY) * flightState.goalkeeperProgress
                }
                else -> goalBottom - 10f
            }

            drawGoalkeeper(
                scope = this,
                centerX = gkX,
                groundY = gkY,
                phase = phase,
                diveSector = flightState.goalkeeperDiveSector,
                progress = flightState.goalkeeperProgress,
                isSave = flightState.outcome == ShotOutcome.SAVED,
                isConceded = flightState.outcome?.isGoal == true
            )

            // 5. Draw Football & Shadow
            val ballSpotX = w * 0.50f
            val ballSpotY = h * 0.86f

            if (phase == MatchPhase.BALL_IN_FLIGHT || phase == MatchPhase.OUTCOME_CELEBRATION) {
                val p = flightState.progress
                // 3D perspective path
                val goalTargetPxX = (goalLeft + goalRight) / 2f + (flightState.targetX * (goalRight - goalLeft) * 0.45f)
                val goalTargetPxY = goalBottom - (flightState.targetY * (goalBottom - goalTop))

                // Ball position with parabolic curve
                val currentBallX = ballSpotX + (goalTargetPxX - ballSpotX) * p
                val linearY = ballSpotY + (goalTargetPxY - ballSpotY) * p
                val arcHeight = sin(p * Math.PI.toFloat()) * (h * 0.14f)
                val currentBallY = linearY - arcHeight

                // Ball scale in perspective (large at spot, smaller at goal)
                val ballRadius = (w * 0.045f) * (1.0f - (p * 0.55f))
                val shadowRadius = ballRadius * 0.85f
                val shadowAlpha = (1.0f - (arcHeight / (h * 0.2f))).coerceIn(0.15f, 0.6f)

                // Draw ball shadow on turf
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    topLeft = Offset(currentBallX - shadowRadius, linearY - (shadowRadius * 0.3f)),
                    size = Size(shadowRadius * 2f, shadowRadius * 0.6f)
                )

                // Draw football
                drawFootball(
                    center = Offset(currentBallX, currentBallY),
                    radius = ballRadius,
                    rotationAngle = p * 720f
                )
            } else {
                // Static ball at penalty spot
                val ballRadius = w * 0.042f
                // Ball shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.55f),
                    topLeft = Offset(ballSpotX - ballRadius * 0.9f, ballSpotY + ballRadius * 0.7f),
                    size = Size(ballRadius * 1.8f, ballRadius * 0.6f)
                )
                drawFootball(
                    center = Offset(ballSpotX, ballSpotY),
                    radius = ballRadius,
                    rotationAngle = 0f
                )

                // If user is shooting, draw aiming reticle
                if (isUserShooting && phase == MatchPhase.AWAITING_INPUT) {
                    val aimPxX = (goalLeft + goalRight) / 2f + (aimX * (goalRight - goalLeft) * 0.45f)
                    val aimPxY = goalBottom - (aimY * (goalBottom - goalTop))

                    // Reticle circle
                    drawCircle(
                        color = ArenaEmeraldLight.copy(alpha = 0.85f),
                        radius = 16f,
                        center = Offset(aimPxX, aimPxY),
                        style = Stroke(width = 3f)
                    )
                    // Reticle crosshairs
                    drawLine(
                        color = ArenaEmeraldLight,
                        start = Offset(aimPxX - 24f, aimPxY),
                        end = Offset(aimPxX + 24f, aimPxY),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = ArenaEmeraldLight,
                        start = Offset(aimPxX, aimPxY - 24f),
                        end = Offset(aimPxX, aimPxY + 24f),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // Overlay: Outcome Celebration Banner
        if (phase == MatchPhase.OUTCOME_CELEBRATION && flightState.outcome != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (flightState.outcome.isGoal) ArenaEmerald.copy(alpha = 0.95f)
                        else ArenaRed.copy(alpha = 0.95f)
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = flightState.outcome.message,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
        }

        // Bottom Controls Overlay
        if (phase == MatchPhase.AWAITING_INPUT) {
            if (isUserShooting) {
                // Striker Controls: Power bar & Shoot button
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(StadiumNightCard.copy(alpha = 0.85f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "AIM: Drag goal area to aim target",
                            color = ArenaEmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "POWER: ${(power * 100).toInt()}%",
                            color = if (power > 0.88f) ArenaRed else if (power in 0.65f..0.85f) ArenaEmerald else ArenaGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Power Slider
                    Slider(
                        value = power,
                        onValueChange = { power = it },
                        valueRange = 0.3f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (power in 0.65f..0.85f) ArenaEmerald else ArenaGold,
                            activeTrackColor = if (power in 0.65f..0.85f) ArenaEmerald else ArenaGold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onKickShot(
                                ShotSpec(
                                    aimX = aimX,
                                    aimY = aimY,
                                    power = power,
                                    spin = spin
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArenaEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "STRIKE PENALTY", fontWeight = FontWeight.Bold, color = StadiumNightDark)
                    }
                }
            } else {
                // Goalkeeper Controls: Sector dive buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(StadiumNightCard.copy(alpha = 0.88f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR TURN IN GOAL - SELECT DIVE SECTOR",
                        color = ArenaGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // 6 Sector Grid Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DiveSectorButton(Modifier.weight(1f), "TOP LEFT", GoalSector.TOP_LEFT, onUserDive)
                        DiveSectorButton(Modifier.weight(1f), "HIGH MID", GoalSector.CENTER_HIGH, onUserDive)
                        DiveSectorButton(Modifier.weight(1f), "TOP RIGHT", GoalSector.TOP_RIGHT, onUserDive)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DiveSectorButton(Modifier.weight(1f), "LOW LEFT", GoalSector.BOTTOM_LEFT, onUserDive)
                        DiveSectorButton(Modifier.weight(1f), "LOW MID", GoalSector.CENTER_LOW, onUserDive)
                        DiveSectorButton(Modifier.weight(1f), "LOW RIGHT", GoalSector.BOTTOM_RIGHT, onUserDive)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiveSectorButton(
    modifier: Modifier = Modifier,
    label: String,
    sector: GoalSector,
    onDive: (GoalSector) -> Unit
) {
    Button(
        onClick = { onDive(sector) },
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3252)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

private fun DrawScope.drawStadiumAtmosphere(w: Float, h: Float) {
    // Night sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF040812), Color(0xFF0B192E), Color(0xFF122846)),
            startY = 0f,
            endY = h * 0.45f
        ),
        size = Size(w, h * 0.45f)
    )

    // Floodlight glow cones (top corners)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x99DCEEFE), Color(0x3360A5FA), Color.Transparent),
            center = Offset(w * 0.10f, h * 0.08f),
            radius = w * 0.35f
        ),
        radius = w * 0.35f,
        center = Offset(w * 0.10f, h * 0.08f)
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x99DCEEFE), Color(0x3360A5FA), Color.Transparent),
            center = Offset(w * 0.90f, h * 0.08f),
            radius = w * 0.35f
        ),
        radius = w * 0.35f,
        center = Offset(w * 0.90f, h * 0.08f)
    )

    // Crowd stands silhouette band
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF091424), Color(0xFF060D17)),
            startY = h * 0.15f,
            endY = h * 0.28f
        ),
        topLeft = Offset(0f, h * 0.15f),
        size = Size(w, h * 0.13f)
    )
}

private fun DrawScope.drawPerspectivePitch(w: Float, h: Float) {
    val horizonY = h * 0.26f
    val pitchPath = Path().apply {
        moveTo(w * 0.05f, horizonY)
        lineTo(w * 0.95f, horizonY)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }

    // Pitch base gradient
    drawPath(
        path = pitchPath,
        brush = Brush.verticalGradient(
            colors = listOf(PitchGreenDark, PitchGreenLight, Color(0xFF1E8238)),
            startY = horizonY,
            endY = h
        )
    )

    // Alternating mowed stripes in perspective
    val stripes = 6
    for (i in 0 until stripes) {
        val y1 = horizonY + (h - horizonY) * (i.toFloat() / stripes)
        val y2 = horizonY + (h - horizonY) * ((i + 1).toFloat() / stripes)
        if (i % 2 == 0) {
            drawRect(
                color = Color.Black.copy(alpha = 0.06f),
                topLeft = Offset(0f, y1),
                size = Size(w, y2 - y1)
            )
        }
    }

    // Goal line
    drawLine(
        color = Color.White.copy(alpha = 0.85f),
        start = Offset(w * 0.10f, h * 0.58f),
        end = Offset(w * 0.90f, h * 0.58f),
        strokeWidth = 3f
    )

    // Penalty spot
    drawCircle(
        color = Color.White,
        radius = 5f,
        center = Offset(w * 0.50f, h * 0.86f)
    )

    // Penalty arc curve
    drawArc(
        color = Color.White.copy(alpha = 0.7f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(w * 0.35f, h * 0.82f),
        size = Size(w * 0.30f, h * 0.14f),
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawGoalAndNet(
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
    netBulge: Float
) {
    val goalDepth = 22f

    // Net background mesh
    val meshCols = 16
    val meshRows = 10
    val netWidth = right - left
    val netHeight = bottom - top

    // Vertical net strings
    for (c in 0..meshCols) {
        val x = left + (netWidth * (c.toFloat() / meshCols))
        val bulge = if (c in 6..10) netBulge else 0f
        drawLine(
            color = Color.White.copy(alpha = 0.22f),
            start = Offset(x, top - goalDepth * 0.6f),
            end = Offset(x + bulge * 0.2f, bottom + bulge),
            strokeWidth = 1f
        )
    }

    // Horizontal net strings
    for (r in 0..meshRows) {
        val y = top + (netHeight * (r.toFloat() / meshRows))
        val bulge = if (r in 3..7) netBulge else 0f
        drawLine(
            color = Color.White.copy(alpha = 0.22f),
            start = Offset(left, y + bulge),
            end = Offset(right, y + bulge),
            strokeWidth = 1f
        )
    }

    // 3D Goal Posts and Crossbar
    val postThickness = 7f

    // Crossbar
    drawLine(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFD1D5DB), Color(0xFF9CA3AF)),
            startY = top - postThickness / 2f,
            endY = top + postThickness / 2f
        ),
        start = Offset(left - postThickness / 2f, top),
        end = Offset(right + postThickness / 2f, top),
        strokeWidth = postThickness,
        cap = StrokeCap.Round
    )

    // Left Post
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF9CA3AF), Color.White, Color(0xFFD1D5DB)),
            startX = left - postThickness / 2f,
            endX = left + postThickness / 2f
        ),
        start = Offset(left, top),
        end = Offset(left, bottom),
        strokeWidth = postThickness
    )

    // Right Post
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFD1D5DB), Color.White, Color(0xFF9CA3AF)),
            startX = right - postThickness / 2f,
            endX = right + postThickness / 2f
        ),
        start = Offset(right, top),
        end = Offset(right, bottom),
        strokeWidth = postThickness
    )
}

private fun drawGoalkeeper(
    scope: DrawScope,
    centerX: Float,
    groundY: Float,
    phase: MatchPhase,
    diveSector: GoalSector,
    progress: Float,
    isSave: Boolean,
    isConceded: Boolean
) {
    scope.apply {
        // Goalkeeper sizing
        val bodyScale = 1.0f
        val headRadius = 9f * bodyScale
        val torsoWidth = 20f * bodyScale
        val torsoHeight = 28f * bodyScale

        val gkJerseyColor = Color(0xFFF59E0B) // Amber neon goalkeeper kit
        val gkShortsColor = Color(0xFF0F172A) // Dark navy shorts
        val gkGlovesColor = Color(0xFF22C55E) // Bright green gloves

        val isDiving = (phase == MatchPhase.BALL_IN_FLIGHT || phase == MatchPhase.OUTCOME_CELEBRATION) && !isSave && !isConceded
        val diveAngle = when (diveSector) {
            GoalSector.TOP_LEFT, GoalSector.BOTTOM_LEFT -> -35f * progress
            GoalSector.TOP_RIGHT, GoalSector.BOTTOM_RIGHT -> 35f * progress
            else -> 0f
        }

        // Draw Goalkeeper Shadow on pitch
        drawOval(
            color = Color.Black.copy(alpha = 0.45f),
            topLeft = Offset(centerX - 16f, groundY - 4f),
            size = Size(32f, 8f)
        )

        val torsoY = groundY - 45f
        val headY = torsoY - 14f

        // Head
        drawCircle(
            color = Color(0xFFE2B29F), // Skin tone
            radius = headRadius,
            center = Offset(centerX, headY)
        )
        // Hair
        drawArc(
            color = Color(0xFF332014),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(centerX - headRadius, headY - headRadius),
            size = Size(headRadius * 2f, headRadius * 1.3f)
        )

        // Torso / Jersey
        drawRoundRect(
            color = gkJerseyColor,
            topLeft = Offset(centerX - torsoWidth / 2f, torsoY),
            size = Size(torsoWidth, torsoHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Arms & Gloves
        val armSpread = if (isDiving) 34f else 24f
        val armY = torsoY + 6f
        // Left arm
        drawLine(
            color = gkJerseyColor,
            start = Offset(centerX - torsoWidth / 2f, armY),
            end = Offset(centerX - armSpread, armY + 12f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        // Left glove
        drawCircle(
            color = gkGlovesColor,
            radius = 5.5f,
            center = Offset(centerX - armSpread, armY + 12f)
        )

        // Right arm
        drawLine(
            color = gkJerseyColor,
            start = Offset(centerX + torsoWidth / 2f, armY),
            end = Offset(centerX + armSpread, armY + 12f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        // Right glove
        drawCircle(
            color = gkGlovesColor,
            radius = 5.5f,
            center = Offset(centerX + armSpread, armY + 12f)
        )

        // Shorts
        drawRoundRect(
            color = gkShortsColor,
            topLeft = Offset(centerX - torsoWidth * 0.45f, torsoY + torsoHeight),
            size = Size(torsoWidth * 0.9f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )

        // Legs
        val legY = torsoY + torsoHeight + 12f
        drawLine(
            color = Color(0xFFE2B29F),
            start = Offset(centerX - 5f, legY),
            end = Offset(centerX - 6f, groundY - 2f),
            strokeWidth = 4.5f
        )
        drawLine(
            color = Color(0xFFE2B29F),
            start = Offset(centerX + 5f, legY),
            end = Offset(centerX + 6f, groundY - 2f),
            strokeWidth = 4.5f
        )

        // Cleats
        drawOval(
            color = Color.Black,
            topLeft = Offset(centerX - 10f, groundY - 3f),
            size = Size(8f, 5f)
        )
        drawOval(
            color = Color.Black,
            topLeft = Offset(centerX + 2f, groundY - 3f),
            size = Size(8f, 5f)
        )
    }
}

private fun DrawScope.drawFootball(
    center: Offset,
    radius: Float,
    rotationAngle: Float
) {
    // White leather ball base
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center
    )

    // Ball 3D shading
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)),
            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Central pentagon patch
    val pRad = radius * 0.38f
    val radRot = Math.toRadians(rotationAngle.toDouble())
    val patchPath = Path().apply {
        for (i in 0 until 5) {
            val a = radRot + (i * 2.0 * Math.PI / 5.0)
            val px = center.x + (cos(a) * pRad).toFloat()
            val py = center.y + (sin(a) * pRad).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path = patchPath, color = Color(0xFF1E293B))

    // Outer border
    drawCircle(
        color = Color(0xFF0F172A).copy(alpha = 0.4f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )
}
