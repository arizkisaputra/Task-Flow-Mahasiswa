package com.example.data

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Q1AccentDark
import com.example.ui.theme.Q1AccentLight
import com.example.ui.theme.Q1BorderDark
import com.example.ui.theme.Q1BorderLight
import com.example.ui.theme.Q1ContainerDark
import com.example.ui.theme.Q1ContainerLight
import com.example.ui.theme.Q1Dot
import com.example.ui.theme.Q2AccentDark
import com.example.ui.theme.Q2AccentLight
import com.example.ui.theme.Q2BorderDark
import com.example.ui.theme.Q2BorderLight
import com.example.ui.theme.Q2ContainerDark
import com.example.ui.theme.Q2ContainerLight
import com.example.ui.theme.Q2Dot
import com.example.ui.theme.Q3AccentDark
import com.example.ui.theme.Q3AccentLight
import com.example.ui.theme.Q3BorderDark
import com.example.ui.theme.Q3BorderLight
import com.example.ui.theme.Q3ContainerDark
import com.example.ui.theme.Q3ContainerLight
import com.example.ui.theme.Q3Dot
import com.example.ui.theme.Q4AccentDark
import com.example.ui.theme.Q4AccentLight
import com.example.ui.theme.Q4BorderDark
import com.example.ui.theme.Q4BorderLight
import com.example.ui.theme.Q4ContainerDark
import com.example.ui.theme.Q4ContainerLight
import com.example.ui.theme.Q4Dot

enum class Quadrant(
    val code: String,
    val shortCode: String,
    val title: String,
    val criteriaLabel: String,
    val actionHint: String,
    val description: String
) {
    Q1_DO_FIRST(
        code = "Kuadran I",
        shortCode = "KI",
        title = "Penting & Mendesak",
        criteriaLabel = "Lakukan Segera",
        actionHint = "Deadline dekat, ujian besok, atau tugas krusial",
        description = "Kerjakan terlebih dahulu dengan fokus penuh agar tidak menumpuk mendekati batas waktu."
    ),
    Q2_SCHEDULE(
        code = "Kuadran II",
        shortCode = "KII",
        title = "Penting & Tidak Mendesak",
        criteriaLabel = "Jadwalkan",
        actionHint = "Tugas jangka panjang, belajar rutin, olahraga",
        description = "Kunci ketenangan mahasiswa: cicil bertahap lewat Jadwal Harian sebelum menjadi mendesak."
    ),
    Q3_DELEGATE(
        code = "Kuadran III",
        shortCode = "KIII",
        title = "Tidak Penting & Mendesak",
        criteriaLabel = "Batasi",
        actionHint = "Pesan masuk beruntun atau gangguan mendadak",
        description = "Tangani singkat di sela jadwal agar tidak menggeser prioritas tugas kuliah utama."
    ),
    Q4_ELIMINATE(
        code = "Kuadran IV",
        shortCode = "KIV",
        title = "Tidak Penting & Tidak Mendesak",
        criteriaLabel = "Kurangi",
        actionHint = "Media sosial berlebihan atau hiburan berlarut-larut",
        description = "Kendalikan durasinya agar waktu tetap bernilai dan terhindar dari penundaan."
    );

    private val darkColors: QuadrantColors by lazy(LazyThreadSafetyMode.NONE) {
        when (this) {
            Q1_DO_FIRST -> QuadrantColors(Q1Dot, Q1AccentDark, Q1ContainerDark, Q1BorderDark)
            Q2_SCHEDULE -> QuadrantColors(Q2Dot, Q2AccentDark, Q2ContainerDark, Q2BorderDark)
            Q3_DELEGATE -> QuadrantColors(Q3Dot, Q3AccentDark, Q3ContainerDark, Q3BorderDark)
            Q4_ELIMINATE -> QuadrantColors(Q4Dot, Q4AccentDark, Q4ContainerDark, Q4BorderDark)
        }
    }

    private val lightColors: QuadrantColors by lazy(LazyThreadSafetyMode.NONE) {
        when (this) {
            Q1_DO_FIRST -> QuadrantColors(Q1Dot, Q1AccentLight, Q1ContainerLight, Q1BorderLight)
            Q2_SCHEDULE -> QuadrantColors(Q2Dot, Q2AccentLight, Q2ContainerLight, Q2BorderLight)
            Q3_DELEGATE -> QuadrantColors(Q3Dot, Q3AccentLight, Q3ContainerLight, Q3BorderLight)
            Q4_ELIMINATE -> QuadrantColors(Q4Dot, Q4AccentLight, Q4ContainerLight, Q4BorderLight)
        }
    }

    fun colors(darkTheme: Boolean): QuadrantColors = if (darkTheme) darkColors else lightColors

    companion object {
        fun fromImportanceAndUrgency(isImportant: Boolean, isUrgent: Boolean): Quadrant = when {
            isImportant && isUrgent -> Q1_DO_FIRST
            isImportant && !isUrgent -> Q2_SCHEDULE
            !isImportant && isUrgent -> Q3_DELEGATE
            else -> Q4_ELIMINATE
        }
    }
}

@Immutable
data class QuadrantColors(
    val dot: Color,
    val accent: Color,
    val container: Color,
    val border: Color
)
