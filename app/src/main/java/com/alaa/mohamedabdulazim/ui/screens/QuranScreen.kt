package com.alaa.mohamedabdulazim.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.alaa.mohamedabdulazim.data.repository.Surah
import com.alaa.mohamedabdulazim.ui.theme.IslamicGold
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreen
import com.alaa.mohamedabdulazim.ui.theme.IslamicGreenDark
import com.alaa.mohamedabdulazim.viewmodel.QuranViewModel

@Composable
fun QuranScreen(vm: QuranViewModel = viewModel()) {
    val surahs        by vm.surahs.collectAsState()
    val ayahs         by vm.ayahs.collectAsState()
    val loading       by vm.loading.collectAsState()
    val error         by vm.error.collectAsState()
    val selectedSurah by vm.selectedSurah.collectAsState()

    // رجوع للقائمة لو مفتوح سورة
    BackHandler(enabled = selectedSurah != null) { vm.closeSurah() }

    if (selectedSurah != null) {
        // شاشة قراءة السورة
        SurahReaderScreen(
            surah   = selectedSurah!!,
            ayahs   = ayahs,
            loading = loading,
            error   = error,
            onBack  = { vm.closeSurah() }
        )
    } else {
        // قائمة السور
        SurahListScreen(
            surahs  = surahs,
            loading = loading,
            error   = error,
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
        // Header
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
                        surah    = surah,
                        cached   = isCached(surah.number),
                        onClick  = { onSelect(surah) }
                    )
                    if (index < surahs.lastIndex)
                        Divider(color = IslamicGreen.copy(0.1f),
                            modifier = Modifier.padding(horizontal = 16.dp))
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
        // رقم السورة
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

        // اسم السورة
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

        // أيقونة محفوظ
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

@Composable
private fun SurahReaderScreen(
    surah: com.alaa.mohamedabdulazim.data.repository.Surah,
    ayahs: List<com.alaa.mohamedabdulazim.data.repository.Ayah>,
    loading: Boolean,
    error: String?,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color(0xFFF5F0E8))) {
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
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // البسملة (مش للفاتحة والتوبة)
                if (surah.number != 1 && surah.number != 9) {
                    item {
                        Text(
                            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            color      = IslamicGold,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center,
                            modifier   = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                    }
                }

                items(ayahs) { ayah ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors   = CardDefaults.cardColors(containerColor = Color.White),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // رقم الآية
                            Box(
                                Modifier.align(Alignment.End)
                                    .size(28.dp)
                                    .background(IslamicGreen.copy(0.1f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    ayah.numberInSurah.toString(),
                                    color    = IslamicGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            // نص الآية
                            Text(
                                ayah.text,
                                color     = Color(0xFF1A1A1A),
                                fontSize  = 22.sp,
                                textAlign = TextAlign.Right,
                                lineHeight = 38.sp,
                                modifier  = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}
