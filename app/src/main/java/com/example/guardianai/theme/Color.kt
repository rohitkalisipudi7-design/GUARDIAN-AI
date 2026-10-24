package com.example.guardianai.theme

import androidx.compose.ui.graphics.Color

// Guardian Hand-Crafted Editorial & Technical Palette (From Vercel/Linear Senior Designer Reference Images)
// Pure warm cream/sand/sienna foundation, eliminating generic cyan-on-black AI glassmorphism

// Background & Structural Foundations
val AppBackground = Color(0xFFFBF8F3)          // Warm Ivory/Cream Foundation (#FBF8F3)
val DarkSurface = AppBackground                 // Alias mapping existing code to Cream Foundation
val CardSand = Color(0xFFF0ECE5)               // Warm Sand/Beige Card Surface (#F0ECE5)
val CardDark = CardSand                        // Alias mapping existing code to Sand Surface
val CardSandElevated = Color(0xFFE5E0D6)       // Header Bars inside Cards / Secondary Surface (#E5E0D6)
val CardSurfaceElevated = CardSandElevated     // Alias mapping existing code to Card Header
val BorderSand = Color(0xFFDCD6CC)             // Crisp 1px Editorial Division Line (#DCD6CC)
val GlassBorder = BorderSand                   // Alias mapping existing code to crisp sand border

// Multi-Level Editorial Text Hierarchy
val TextPrimary = Color(0xFF2A2725)            // Deep Warm Charcoal Header Text (#2A2725)
val TextSubtitle = Color(0xFF5D5954)           // Subtitle & Secondary Editorial Text (#5D5954)
val TextMuted = Color(0xFF7D7872)              // Monospace & Technical Metadata (#7D7872)

// Committed Brand Point-of-View Signature Tokens
val GuardianSienna = Color(0xFF8B3B26)         // Rich Terracotta / Sienna Brand Signature (#8B3B26)
val GuardianCrimson = Color(0xFFD63D30)        // Critical Threat Red / SOS Alert (#D63D30)
val GuardianCrimsonMuted = Color(0xFFF4D8D1)   // Soft Warm Sienna/Crimson Tint for Badges
val StatusEmerald = Color(0xFF2EB886)          // Editorial Emerald / Chain-Valid Green (#2EB886)
val StatusAmber = Color(0xFFE08535)            // Editorial Amber/Orange for Escalate States (#E08535)
val EscalateOrange = StatusAmber               // Alias for High-Risk Orange
val InterveneBrown = Color(0xFF8B4A2B)         // Warm Brown for Intervene/Action Required
val SignalIndigo = GuardianSienna              // Alias: SignalIndigo -> GuardianSienna (#8B3B26)
val SignalIndigoMuted = Color(0xFFEFEAE2)      // Subdued Sand background

// Footer & Dark Charcoal Accents
val FooterDark = Color(0xFF282625)             // Rich Charcoal Footer Base (#282625)

// Backward compatibility aliases
val ShieldBlue = GuardianSienna
val MonitorGreen = StatusEmerald
val CriticalRed = GuardianCrimson
val CryptoPurple = GuardianSienna
val NeonCyan = StatusEmerald

