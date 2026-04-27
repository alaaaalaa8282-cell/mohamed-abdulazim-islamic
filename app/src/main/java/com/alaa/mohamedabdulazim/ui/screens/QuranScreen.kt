package com.alaa.mohamedabdulazim.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaa.mohamedabdulazim.data.repository.Ayah
import com.alaa.mohamedabdulazim.data.repository.Surah
import com.alaa.mohamedabdulazim.ui.theme.IslamicGold
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreen
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreenDark
import com.alaa.mohamedabdulazim.viewmodel.QuranViewModel

private fun toArabicNumber(n: Int): String {
    val arabic = mapOf('0' to '٠','1' to '١','2' to '٢','3' to '٣',
        '4' to '٤','5' to '٥','6' to '٦','7' to '٧','8' to '٨','9' to '٩')
    return n.toString().map { arabic[it] ?: it }.joinToString("")
}

// تقسيم الآيات لصفحات (15 آية في الصفحة)
private fun splitToPages(ayahs: List<Ayah>, pageSize: Int = 15): List<List<Ayah>> {
    return ayahs.chunked(pageSize)
}

@Composable
fun QuranScreen(vm: QuranViewModel = viewModel()) {
    val surahs        by vm.surahs.collectAsState()
    val ayahs         by vm.ayahs.collectAsState()
    val loading       by vm.loading.collectAsState()
    val error         by vm.error.collectAsState()
    val selectedSurah by vm.selectedSurah.collectAsState()

    BackHandler(enabled = selectedSurah != null) { vm.closeSurah() }

    if (selectedSurah != null) {
        SurahReaderScreen(
            surah   = selectedSurah!!,
            ayahs   = ayahs,
            loading = loading,
            error   = error,
            onBack  = { vm.closeSurah() }
        )
    } else {
        SurahListScreen(
            surahs   = surahs,
            loading  = loading,
            error    = error,
            isCached = { vm.isCached(it) },
            onSelect = { vm.openSurah(it) }
        )
    }
}

@Composable
private fun SurahListScreen(
    surahs: List<Surah>,
    loading: Boolean,
    error: String?,
    isCached: (Int) -> Boolean,
    onSelect: (Surah) -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF5F0E8))) {
        Box(
            Modifier.fillMaxWidth()
                .background(IslamicGreen)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "القرآن الكريم",
                color      = IslamicGold,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IslamicGreen)
                    Spacer(Modifier.height(8.dp))
                    Text("جاري تحميل القرآن...", color = IslamicGreenDark)
                }
            }
            error != null -> Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
                Text(error, color = Color.Red, textAlign = TextAlign.Center)
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(surahs) { index, surah ->
                    SurahItem(
                        surah   = surah,
                        cached  = isCached(surah.number),
                        onClick = { onSelect(surah) }
                    )
                    if (index < surahs.lastIndex)
                        Divider(
                            color    = IslamicGreen.copy(0.1f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                }
            }
        }
    }
}

@Composable
private fun SurahItem(surah: Surah, cached: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (cached) IslamicGreen else IslamicGreen.copy(0.15f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                surah.number.toString(),
                color      = if (cached) Color.White else IslamicGreen,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                surah.name,
                color      = IslamicGreenDark,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${surah.numberOfAyahs} آية · ${surah.type}",
                color    = IslamicGreenDark.copy(0.6f),
                fontSize = 12.sp
            )
        }

        if (cached) {
            Icon(
                Icons.Filled.DownloadDone,
                contentDescription = "محفوظ",
                tint     = IslamicGreen,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SurahReaderScreen(
    surah: Surah,
    ayahs: List<Ayah>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit
) {
    val pages = remember(ayahs) { splitToPages(ayahs) }
    val pagerState = rememberPagerState(pageCount = { if (pages.isEmpty()) 1 else pages.size })

    Column(Modifier.fillMaxSize().background(Color(0xFFFDF8F0))) {

        // Header
        Row(
            Modifier.fillMaxWidth()
                .background(IslamicGreen)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowForward, null, tint = Color.White)
            }
            Text(
                surah.name,
                color      = IslamicGold,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.weight(1f),
                textAlign  = TextAlign.Center
            )
            Text(
                "${surah.numberOfAyahs} آية",
                color    = Color.White.copy(0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IslamicGreen)
                    Spacer(Modifier.height(8.dp))
                    Text("جاري تحميل السورة...", color = IslamicGreenDark)
                }
            }
            error != null -> Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
                Text(error, color = Color.Red, textAlign = TextAlign.Center)
            }
            pages.isNotEmpty() -> {
                // مؤشر الصفحة
                Box(
                    Modifier.fillMaxWidth()
                        .background(IslamicGreen.copy(0.08f))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "صفحة ${toArabicNumber(pagerState.currentPage + 1)} من ${toArabicNumber(pages.size)}",
                        color    = IslamicGreenDark,
                        fontSize = 13.sp
                    )
                }

                // الصفحات
                HorizontalPager(
                    state    = pagerState,
                    modifier = Modifier.weight(1f),
                    reverseLayout = true // من اليمين لليسار زي المصحف
                ) { pageIndex ->
                    val pageAyahs = pages[pageIndex]
                    val showBismillah = pageIndex == 0
                            && surah.number != 1
                            && surah.number != 9

                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier  = Modifier.fillMaxSize(),
                            colors    = CardDefaults.cardColors(containerColor = Color.White),
                            shape     = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // البسملة في أول صفحة بس
                                if (showBismillah) {
                                    Text(
                                        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        color      = IslamicGold,
                                        fontSize   = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign  = TextAlign.Center,
                                        modifier   = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp)
                                    )
                                    Divider(
                                        color    = IslamicGold.copy(0.3f),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }

                                // نص الصفحة
                                Text(
                                    text = pageAyahs.joinToString(" ") { ayah ->
                                        "${ayah.text} ﴿${toArabicNumber(ayah.numberInSurah)}﴾"
                                    },
                                    color      = Color(0xFF1A1A1A),
                                    fontSize   = 22.sp,
                                    textAlign  = TextAlign.Right,
                                    lineHeight = 44.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier   = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // أسهم التنقل
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(IslamicGreen.copy(0.05f))
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // السابقة (يسار = صفحة أعلى رقماً)
                    IconButton(
                        onClick  = { },
                        enabled  = pagerState.currentPage < pages.size - 1
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "الصفحة التالية",
                            tint = if (pagerState.currentPage < pages.size - 1)
                                IslamicGreen else IslamicGreen.copy(0.3f)
                        )
                    }

                    // نقاط الصفحات (لو أقل من 10 صفحات)
                    if (pages.size <= 10) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(pages.size) { i ->
                                Box(
                                    Modifier
                                        .size(if (i == pagerState.currentPage) 10.dp else 6.dp)
                                        .background(
                                            if (i == pagerState.currentPage) IslamicGreen
                                            else IslamicGreen.copy(0.3f),
                                            RoundedCornerShape(50)
                                        )
                                )
                            }
                        }
                    } else {
                        Text(
                            "${pagerState.currentPage + 1} / ${pages.size}",
                            color    = IslamicGreenDark,
                            fontSize = 14.sp
                        )
                    }

                    // التالية (يمين = صفحة أقل رقماً)
                    IconButton(
                        onClick  = { },
                        enabled  = pagerState.currentPage > 0
                    ) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "الصفحة السابقة",
                            tint = if (pagerState.currentPage > 0)
                                IslamicGreen else IslamicGreen.copy(0.3f)
                        )
                    }
                }
            }
        }
    }
}
