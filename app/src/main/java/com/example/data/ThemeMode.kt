package com.example.data

enum class ThemeMode(val label: String, val description: String) {
    LIGHT("Light Mode", "Tampilan cerah, bersih & menenangkan"),
    DARK("Dark Mode", "Nyaman di mata saat malam atau minim cahaya"),
    SYSTEM("Ikuti Sistem", "Otomatis menyesuaikan tema perangkat");

    companion object {
        fun fromStorage(raw: String?): ThemeMode {
            return entries.firstOrNull { it.name == raw } ?: DARK
        }
    }
}
