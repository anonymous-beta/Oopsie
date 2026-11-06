package com.anonymousbeta.oopsie.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFFF6F0)
val Bubblegum = Color(0xFFFF8FA3)
val Mint = Color(0xFF7FD1C1)
val Butter = Color(0xFFFFD166)
val Ink = Color(0xFF3A2E39)
val Soft = Color(0xFFF3E6DE)

@Composable
fun OopsieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Bubblegum,
            onPrimary = Color.White,
            secondary = Mint,
            onSecondary = Ink,
            tertiary = Butter,
            onTertiary = Ink,
            background = Cream,
            onBackground = Ink,
            surface = Color.White,
            onSurface = Ink
        ),
        content = content
    )
}
