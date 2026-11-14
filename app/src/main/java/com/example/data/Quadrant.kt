package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Q1AccentDark
import com.example.ui.theme.Q1AccentLight
import com.example.ui.theme.Q1BorderDark
import com.example.ui.theme.Q1BorderLight
import com.example.ui.theme.Q1ContainerDark
import com.example.ui.theme.Q1ContainerLight
import com.example.ui.theme.Q2AccentDark
import com.example.ui.theme.Q2AccentLight
import com.example.ui.theme.Q2BorderDark
import com.example.ui.theme.Q2BorderLight
import com.example.ui.theme.Q2ContainerDark
import com.example.ui.theme.Q2ContainerLight
import com.example.ui.theme.Q3AccentDark
import com.example.ui.theme.Q3AccentLight
import com.example.ui.theme.Q3BorderDark
import com.example.ui.theme.Q3BorderLight
import com.example.ui.theme.Q3ContainerDark
import com.example.ui.theme.Q3ContainerLight
import com.example.ui.theme.Q4AccentDark
import com.example.ui.theme.Q4AccentLight
import com.example.ui.theme.Q4BorderDark
import com.example.ui.theme.Q4BorderLight
import com.example.ui.theme.Q4ContainerDark
import com.example.ui.theme.Q4ContainerLight

enum class Quadrant(
    val code: String,
    val title: String,
    val criteriaLabel: String,
    val actionHint: String,
    val description: String
) {
    Q1_DO_FIRST(
        code = "Kuadran I",
        title = "Penting & Mendesak",
        criteriaLabel = "Lakukan Segera",
        actionHint = "Deadline dekat, ujian besok, atau tugas krusial",
        description = "Kerjakan terlebih dahulu dengan fokus penuh agar tidak menumpuk mendekati batas waktu."
    ),
    Q2_SCHEDULE(
        code = "Kuadran II",
        title = "Penting & Tidak Mendesak",
        criteriaLabel = "Jadwalkan",
        actionHint = "Tugas jangka panjang, belajar rutin, olahraga",
        description = "Kunci ketenangan mahasiswa: cicil bertahap lewat Time-Blocking sebelum menjadi mendesak."
    ),
    Q3_DELEGATE(
        code = "Kuadran III",
        title = "Tidak Penting & Mendesak",
        criteriaLabel = "Batasi / Delegasikan",
        actionHint = "Pesan masuk beruntun atau gangguan aktivitas mendadak",
        description = "Tangani singkat di sela jadwal agar tidak menggeser prioritas tugas kuliah utama."
    ),
    Q4_ELIMINATE(
        code = "Kuadran IV",
        title = "Tidak Penting & Tidak Mendesak",
        criteriaLabel = "Kurangi",
        actionHint = "Media sosial berlebih atau hiburan berlarut-larut",
        description = "Kendalikan durasinya agar waktu tetap bernilai dan terhindar dari penundaan."
    );

    fun colors(darkTheme: Boolean): QuadrantColors = when (this) {
        Q1_DO_FIRST -> if (darkTheme) {
            QuadrantColors(Q1AccentDark, Q1ContainerDark, Q1BorderDark)
        } else {
            QuadrantColors(Q1AccentLight, Q1ContainerLight, Q1BorderLight)
        }
        Q2_SCHEDULE -> if (darkTheme) {
            QuadrantColors(Q2AccentDark, Q2ContainerDark, Q2BorderDark)
        } else {
            QuadrantColors(Q2AccentLight, Q2ContainerLight, Q2BorderLight)
        }
        Q3_DELEGATE -> if (darkTheme) {
            QuadrantColors(Q3AccentDark, Q3ContainerDark, Q3BorderDark)
        } else {
            QuadrantColors(Q3AccentLight, Q3ContainerLight, Q3BorderLight)
        }
        Q4_ELIMINATE -> if (darkTheme) {
            QuadrantColors(Q4AccentDark, Q4ContainerDark, Q4BorderDark)
        } else {
            QuadrantColors(Q4AccentLight, Q4ContainerLight, Q4BorderLight)
        }
    }

    companion object {
        fun fromImportanceAndUrgency(isImportant: Boolean, isUrgent: Boolean): Quadrant = when {
            isImportant && isUrgent -> Q1_DO_FIRST
            isImportant && !isUrgent -> Q2_SCHEDULE
            !isImportant && isUrgent -> Q3_DELEGATE
            else -> Q4_ELIMINATE
        }
    }
}

data class QuadrantColors(
    val accent: Color,
    val container: Color,
    val border: Color
)
