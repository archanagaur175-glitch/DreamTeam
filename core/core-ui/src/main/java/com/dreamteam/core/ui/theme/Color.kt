package com.dreamteam.core.ui.theme

import androidx.compose.ui.graphics.Color

// Base palette — "Hyper-Immersive Dark Studio"
val NightBase = Color(0xFF0A0B12)
val NightSurface = Color(0xFF12131D)
val NightSurfaceHigh = Color(0xFF1A1C2B)
val NightBorder = Color(0xFF262838)

val TextPrimary = Color(0xFFECEAF3)
val TextSecondary = Color(0xFF9A97AD)
val TextMuted = Color(0xFF5D5B6E)

// Signature bioluminescent gradient: night -> dawn
val AccentIndigo = Color(0xFF6366F1)
val AccentViolet = Color(0xFF8B5CF6)
val AccentAmber = Color(0xFFF59E0B)

val SuccessTeal = Color(0xFF34D399)
val WarningGold = Color(0xFFFBBF24)
val DangerRed = Color(0xFFF87171)

/** The signature night→dawn gradient used for the energy curve, debt ring and alarm arc. */
val NightToDawn = listOf(AccentIndigo, AccentViolet, AccentAmber)
