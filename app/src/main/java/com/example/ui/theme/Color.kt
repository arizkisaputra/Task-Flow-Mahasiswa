package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Obsidian (Dark Mode) & Alabaster (Light Mode) Calm Palette
val Obsidian950 = Color(0xFF090D12)
val Obsidian900 = Color(0xFF0E141B)
val Obsidian850 = Color(0xFF131B24)
val Obsidian800 = Color(0xFF19232F)
val Obsidian700 = Color(0xFF243242)

val Alabaster50 = Color(0xFFF7F8F6)
val Alabaster100 = Color(0xFFEFF1ED)
val Alabaster200 = Color(0xFFE2E6DF)
val PureWhite = Color(0xFFFFFFFF)

// Text & Hairline Borders
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)

// Calm Accents (Sky, Teal, Emerald/Sage, Coral/Rose, Amber)
val Sky600 = Color(0xFF0284C7)
val Sky500 = Color(0xFF0EA5E9)
val Sky400 = Color(0xFF38BDF8)
val Sky100 = Color(0xFFE0F2FE)
val Sky950 = Color(0xFF082F49)

val Teal500 = Color(0xFF14B8A6)
val Teal400 = Color(0xFF2DD4BF)

val Emerald700 = Color(0xFF047857)
val Emerald600 = Color(0xFF059669)
val Emerald500 = Color(0xFF10B981)
val Emerald400 = Color(0xFF34D399)
val Emerald300 = Color(0xFF6EE7B7)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald950 = Color(0xFF064E3B)

val Amber700 = Color(0xFFB45309)
val Amber600 = Color(0xFFD97706)
val Amber500 = Color(0xFFF59E0B)
val Amber400 = Color(0xFFFBBF24)
val Amber300 = Color(0xFFFCD34D)
val Amber100 = Color(0xFFFEF3C7)
val Amber950 = Color(0xFF451A03)

val Rose700 = Color(0xFFBE123C)
val Rose500 = Color(0xFFF43F5E)
val Rose400 = Color(0xFFFB7185)
val Rose300 = Color(0xFFFDA4AF)

// Mindful Reflection Card Gradient Stops
val ReflectionGradStartLight = Color(0xFFECFDF5)
val ReflectionGradMidLight = Color(0xFFF0FDFA)
val ReflectionGradEndLight = Color(0xFFF0F9FF)

val ReflectionGradStartDark = Obsidian850
val ReflectionGradMidDark = Color(0xFF122026)
val ReflectionGradEndDark = Color(0xFF111C26)

// Quadrant Semantic Tokens (Matching HTML Redesign)
// Q1: Penting & Mendesak (Rose)
val Q1Dot = Rose500
val Q1AccentLight = Rose700
val Q1ContainerLight = Color(0xFFFFF1F2)
val Q1BorderLight = Color(0xFFFDA4AF)
val Q1AccentDark = Rose300
val Q1ContainerDark = Color(0xFF1E1418)
val Q1BorderDark = Rose500.copy(alpha = 0.25f)

// Q2: Penting & Tidak Mendesak (Emerald)
val Q2Dot = Emerald500
val Q2AccentLight = Emerald700
val Q2ContainerLight = Color(0xFFECFDF5)
val Q2BorderLight = Color(0xFF6EE7B7)
val Q2AccentDark = Emerald300
val Q2ContainerDark = Color(0xFF111E1A)
val Q2BorderDark = Emerald500.copy(alpha = 0.25f)

// Q3: Tidak Penting & Mendesak (Amber)
val Q3Dot = Amber500
val Q3AccentLight = Amber700
val Q3ContainerLight = Color(0xFFFFFBEB)
val Q3BorderLight = Color(0xFFFCD34D)
val Q3AccentDark = Amber300
val Q3ContainerDark = Color(0xFF1E1A12)
val Q3BorderDark = Amber500.copy(alpha = 0.25f)

// Q4: Tidak Penting & Tidak Mendesak (Slate / Obsidian)
val Q4Dot = Slate400
val Q4AccentLight = Color(0xFF334155)
val Q4ContainerLight = Slate100
val Q4BorderLight = Slate300
val Q4AccentDark = Slate300
val Q4ContainerDark = Obsidian850
val Q4BorderDark = Color.White.copy(alpha = 0.08f)

// Schedule Sky Slot Tokens
val SkySlotContainerLight = Color(0xFFF0F9FF)
val SkySlotBorderLight = Color(0xFF7DD3FC)
val SkySlotContainerDark = Color(0xFF111B24)
val SkySlotBorderDark = Sky400.copy(alpha = 0.25f)
