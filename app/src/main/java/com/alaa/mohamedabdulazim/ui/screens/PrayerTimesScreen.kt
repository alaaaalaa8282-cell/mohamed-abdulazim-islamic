package com.alaa.mohamedabdulazim.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaa.mohamedabdulazim.data.models.PrayerTime
import com.alaa.mohamedabdulazim.ui.components.FatherHeader
import com.alaa.mohamedabdulazim.ui.theme.*
import com.alaa.mohamedabdulazim.viewmodel.PrayerViewModel

@Composable
fun PrayerTimesScreen(vm: PrayerViewModel = viewModel()) {
    val context = LocalContext.current
    val prayers by vm.prayers.collectAsState()
    val hijriDate by vm.hijriDate.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true)
            fetchLocation(context) { lat, lng -> vm.fetchPrayerTimes(lat, lng) }
    }

    LaunchedEffect(Unit) {
        if (prayers.isEmpty()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                fetchLocation(context) { lat, lng -> vm.fetchPrayerTimes(lat, lng) }
            else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Column(Modifier.fillMaxSize()) {
        FatherHeader(compact = true)

        // Header bar
        Row(
            Modifier.fillMaxWidth().background(IslamicGreen).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("مواقيت الصلاة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (hijriDate.isNotEmpty())
                    Text(hijriDate, color = IslamicGold, fontSize = 12.sp)
            }
            IconButton(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    fetchLocation(context) { lat, lng -> vm.fetchPrayerTimes(lat, lng) }
                else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }) {
                Icon(Icons.Filled.Refresh, null, tint = IslamicGold)
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IslamicGreen)
                    Spacer(Modifier.height(8.dp))
                    Text("جاري جلب المواقيت...", color = IslamicGreen)
                }
            }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), Alignment.Center) {
                Text(error ?: "", color = Color.Red, textAlign = TextAlign.Center)
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(prayers) { prayer -> PrayerCard(prayer) }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun PrayerCard(prayer: PrayerTime) {
    val bgColor = when {
        prayer.isNext   -> Brush.horizontalGradient(listOf(IslamicGreen, IslamicGreenLight))
        prayer.isPassed -> Brush.horizontalGradient(listOf(Color(0xFFE0E0E0), Color(0xFFEEEEEE)))
        else            -> Brush.horizontalGradient(listOf(Color.White, CardBackground))
    }
    val textColor = if (prayer.isNext) Color.White else if (prayer.isPassed) PrayerPassed else PrayerNormal

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(if (prayer.isNext) 6.dp else 2.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text(
                        prayer.nameAr,
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium
                    )
                    if (prayer.isNext)
                        Text("← الصلاة القادمة", color = IslamicGold, fontSize = 12.sp)
                    else if (prayer.isPassed)
                        Text("مضت", color = PrayerPassed, fontSize = 12.sp)
                }
                Text(
                    prayer.time,
                    color = if (prayer.isNext) IslamicGold else textColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
private fun fetchLocation(
    context: android.content.Context,
    onLocation: (Double, Double) -> Unit
) {
    val locationManager = context.getSystemService(
        android.content.Context.LOCATION_SERVICE
    ) as android.location.LocationManager

    try {
        val location = locationManager.getLastKnownLocation(
            android.location.LocationManager.GPS_PROVIDER
        ) ?: locationManager.getLastKnownLocation(
            android.location.LocationManager.NETWORK_PROVIDER
        )
        if (location != null) {
            onLocation(location.latitude, location.longitude)
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}
