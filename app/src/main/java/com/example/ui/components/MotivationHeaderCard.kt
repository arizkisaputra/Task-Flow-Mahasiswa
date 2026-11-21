package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald300
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald700
import com.example.ui.theme.FrauncesFamily
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Obsidian900
import com.example.ui.theme.Obsidian950
import com.example.ui.theme.PureWhite
import com.example.ui.theme.ReflectionGradEndDark
import com.example.ui.theme.ReflectionGradEndLight
import com.example.ui.theme.ReflectionGradMidDark
import com.example.ui.theme.ReflectionGradMidLight
import com.example.ui.theme.ReflectionGradStartDark
import com.example.ui.theme.ReflectionGradStartLight
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky400
import com.example.ui.theme.Sky500
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import kotlinx.coroutines.launch

private data class MindfulReflection(
    val badge1: String,
    val badge2: String,
    val quote: String,
    val tip: String
)

private val reflections = listOf(
    MindfulReflection(
        badge1 = "QS. Al-'Asr • Disiplin Waktu",
        badge2 = "Ritme Mahasiswa",
        quote = "\"Demi masa. Sungguh, manusia berada dalam kerugian, kecuali orang-orang yang beriman dan mengerjakan kebajikan serta saling menasihati untuk kebenaran dan kesabaran.\"",
        tip = "Selesaikan satu prioritas utama dalam satu waktu tanpa membebani pikiran dengan tumpukan tugas sekaligus."
    ),
    MindfulReflection(
        badge1 = "Keseimbangan Belajar",
        badge2 = "Fokus Mendalam",
        quote = "\"Ketekunan mengalahkan intensitas sesaat. Dua sesi fokus yang tenang hari ini jauh lebih menenangkan daripada begadang mengejar tenggat di menit akhir.\"",
        tip = "Gunakan Kuadran II untuk mencicil makalah dan riset sebelum berubah menjadi tekanan di Kuadran I."
    ),
    MindfulReflection(
        badge1 = "Kendali Diri & Jeda",
        badge2 = "Kesehatan Mental",
        quote = "\"Sesungguhnya badanmu memiliki hak atas dirimu, dan matamu memiliki hak atas dirimu.\" (HR. Bukhari)",
        tip = "Gunakan jeda 5 menit Pomodoro untuk bernapas lega, minum air, dan menjaga kejernihan pikiran."
    )
)

@Composable
fun MotivationHeaderCard(
    onOpenPrototypeInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val pagerState = rememberPagerState(pageCount = { reflections.size })
    val coroutineScope = rememberCoroutineScope()
    val current = reflections[pagerState.currentPage]

    val bgBrush = remember(isDark) {
        val gradientColors = if (isDark) {
            listOf(ReflectionGradStartDark, ReflectionGradMidDark, ReflectionGradEndDark)
        } else {
            listOf(ReflectionGradStartLight, ReflectionGradMidLight, ReflectionGradEndLight)
        }
        Brush.linearGradient(gradientColors)
    }

    val borderColor = if (isDark) {
        Emerald500.copy(alpha = 0.18f)
    } else {
        Emerald400.copy(alpha = 0.45f)
    }

    val glowColor = if (isDark) {
        Emerald400.copy(alpha = 0.12f)
    } else {
        Emerald400.copy(alpha = 0.16f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("motivation_header_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .background(bgBrush)
                .drawWithCache {
                    val center = Offset(size.width * 0.92f, size.height * 0.08f)
                    val radius = size.width * 0.45f
                    val glowBrush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        center = center,
                        radius = radius
                    )
                    onDrawBehind {
                        drawCircle(
                            brush = glowBrush,
                            radius = radius,
                            center = center
                        )
                    }
                }
                .padding(top = 18.dp, bottom = 10.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("motivation_pills_row"),
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                    item(key = "badge_pill") {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) Obsidian900.copy(alpha = 0.9f) else PureWhite.copy(alpha = 0.92f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.10f) else Sky100
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MenuBook,
                                    contentDescription = "Kutipan Nilai Disiplin",
                                    tint = if (isDark) Sky400 else Sky600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = current.badge1,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDark) Sky400 else Sky600,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    item(key = "ritme_pill") {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Emerald500.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.22f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isDark) Emerald300 else Emerald700,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = current.badge2,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isDark) Emerald300 else Emerald700,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    item(key = "kelompok_pill") {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) Obsidian900.copy(alpha = 0.75f) else PureWhite.copy(alpha = 0.85f),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.08f) else Slate200
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onOpenPrototypeInfo)
                                .testTag("open_prototype_info_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Alur & Kelompok 7",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(14.dp))

            // Editorial Fraunces Quote Pager
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 18.dp),
                pageSpacing = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("motivation_pager")
            ) { page ->
                val item = reflections[page]
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = item.quote,
                        fontFamily = FrauncesFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        lineHeight = 25.sp,
                        color = if (isDark) Slate200 else Slate800
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Mindful Callout Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Obsidian950.copy(alpha = 0.58f) else PureWhite.copy(alpha = 0.78f),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.06f) else Slate200.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Emerald500.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = "Prinsip ketenangan mental",
                                    tint = if (isDark) Emerald400 else Emerald700,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Slate200 else Slate800
                                        )
                                    ) {
                                        append("Prinsip Tenang: ")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            color = if (isDark) Slate300 else Slate600
                                        )
                                    ) {
                                        append(item.tip)
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Bottom-Centered Tappable Slide Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("cycle_quote_button")
            ) {
                repeat(reflections.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = if (isSelected) 22.dp else 8.dp, height = 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Sky500
                                    } else {
                                        Slate400.copy(alpha = 0.35f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}
