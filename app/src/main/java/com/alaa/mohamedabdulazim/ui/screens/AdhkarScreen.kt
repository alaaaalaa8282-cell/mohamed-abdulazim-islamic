package com.alaa.mohamedabdulazim.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaa.mohamedabdulazim.data.models.AdhkarCategory
import com.alaa.mohamedabdulazim.data.models.Zekr
import com.alaa.mohamedabdulazim.ui.components.FatherHeader
import com.alaa.mohamedabdulazim.ui.theme.*
import com.alaa.mohamedabdulazim.viewmodel.AdhkarViewModel

@Composable
fun AdhkarScreen(vm: AdhkarViewModel = viewModel()) {
    val category by vm.selectedCategory.collectAsState()
    val list     by vm.adhkarList.collectAsState()

    Column(Modifier.fillMaxSize()) {
        FatherHeader(compact = true)

        // Category tabs
        LazyRow(
            Modifier.fillMaxWidth().background(IslamicGreen).padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(AdhkarCategory.values()) { cat ->
                val selected = cat == category
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (selected) IslamicGold else Color.White.copy(0.2f))
                        .clickable { vm.selectCategory(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    Alignment.Center
                ) {
                    Text(
                        cat.nameAr,
                        color = if (selected) IslamicGreenDark else Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Reset all button
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { vm.resetAll() }) {
                Icon(Icons.Filled.Refresh, null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("إعادة ضبط الكل", color = IslamicGreen, fontSize = 13.sp)
            }
        }

        // Adhkar list
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(list, key = { it.id }) { zekr ->
                ZekrCard(
                    zekr      = zekr,
                    onTap     = { vm.incrementZekr(zekr) },
                    onReset   = { vm.resetZekr(zekr) }
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ZekrCard(zekr: Zekr, onTap: () -> Unit, onReset: () -> Unit) {
    val isDone = zekr.count > 0 && zekr.currentCount >= zekr.count
    val progress = if (zekr.count > 0) zekr.currentCount.toFloat() / zekr.count.toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth().clickable { if (!isDone) onTap() },
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Zekr text
            Text(
                text      = zekr.text,
                fontSize  = 18.sp,
                color     = if (isDone) IslamicGreen else IslamicGreenDark,
                textAlign = TextAlign.Right,
                lineHeight = 30.sp,
                style     = MaterialTheme.typography.bodyLarge.copy(
                    textDirection = TextDirection.Rtl
                ),
                modifier  = Modifier.fillMaxWidth()
            )

            if (zekr.benefit.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(zekr.benefit, color = IslamicGold, fontSize = 12.sp,
                    textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                // Reset button
                IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Refresh, null,
                        tint = IslamicGreen.copy(0.6f), modifier = Modifier.size(18.dp))
                }

                // Counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (zekr.count > 0) {
                        // Progress bar + counter
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (progress > 0f) {
                                LinearProgressIndicator(
                                    progress = progress.coerceIn(0f, 1f),
                                    modifier  = Modifier.width(80.dp).height(4.dp).clip(CircleShape),
                                    color     = if (isDone) IslamicGold else IslamicGreen,
                                    trackColor = Color.LightGray.copy(0.3f)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                            Text(
                                "${zekr.currentCount} / ${zekr.count}",
                                color     = if (isDone) IslamicGold else IslamicGreen,
                                fontSize  = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Tasbih - unlimited
                        Text(
                            "${zekr.currentCount}",
                            color     = IslamicGreen,
                            fontSize  = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    // Tap button
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isDone) IslamicGold else IslamicGreen)
                            .clickable { onTap() },
                        Alignment.Center
                    ) {
                        Text(
                            if (isDone) "✓" else "اضغط",
                            color     = Color.White,
                            fontSize  = if (isDone) 20.sp else 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
