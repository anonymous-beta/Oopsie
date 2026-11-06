package com.anonymousbeta.oopsie.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size

@Composable
fun Mascot(modifier: Modifier = Modifier) {
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobValue"
    )

    Canvas(modifier = modifier.size(132.dp)) {
        val w = size.width
        val h = size.height
        val lift = bob * w * 0.022f
        val cx = w / 2f
        val cy = h / 2f + lift
        val r = w * 0.43f

        drawCircle(color = Bubblegum, radius = r, center = Offset(cx, cy))

        val eyeW = w * 0.115f
        val eyeH = w * 0.145f
        val eyeY = cy - r * 0.18f
        for (ex in listOf(cx - r * 0.40f, cx + r * 0.40f)) {
            drawOval(
                color = androidx.compose.ui.graphics.Color.White,
                topLeft = Offset(ex - eyeW / 2f, eyeY - eyeH / 2f),
                size = Size(eyeW, eyeH)
            )
            drawCircle(color = Ink, radius = eyeW * 0.26f, center = Offset(ex, eyeY))
        }

        val blushY = cy + r * 0.28f
        for (bx in listOf(cx - r * 0.66f, cx + r * 0.66f)) {
            drawOval(
                color = Butter.copy(alpha = 0.55f),
                topLeft = Offset(bx - w * 0.055f, blushY - w * 0.028f),
                size = Size(w * 0.11f, w * 0.056f)
            )
        }

        drawArc(
            color = Ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - r * 0.42f, cy + r * 0.02f),
            size = Size(r * 0.84f, r * 0.56f),
            style = Stroke(width = w * 0.022f)
        )
    }
}
