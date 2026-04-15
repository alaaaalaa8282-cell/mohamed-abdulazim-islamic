package com.alaa.mohamedabdulazim.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.android.gms.location.LocationServices
import com.alaa.mohamedabdulazim.ui.components.FatherHeader
import com.alaa.mohamedabdulazim.ui.theme.*
import com.alaa.mohamedabdulazim.viewmodel.PrayerViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(vm: PrayerViewModel = viewModel()) {
    val context = LocalContext.current
    val prayers by vm.prayers.collectAsState()
    val nextPrayer by vm.nextPrayer.collectAsState()
    val hijriDate by vm.hijriDate.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf("") }

    // Update clock
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            currentTime = SimpleDateFormat("hh:mm:ss a", Locale("ar")).format(now.time)
            currentDate = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar")).format(now.time)
            // Calculate countdown to next prayer
            nextPrayer?.let { next ->
                val prayerIdx = prayers.indexOf(next)
                // Simple countdown display
                countdown = "⏳ الصلاة القادمة: ${next.nameAr} (${next.time})"
            }
            delay(1000)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fetchLocation(context) { lat, lng -> vm.fetchPrayerTimes(lat, lng) }
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted) {
            fetchLocation(context) { lat, lng -> vm.fetchPrayerTimes(lat, lng) }
        } else {
            locationPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        FatherHeader(compact = false)

        Spacer(Modifier.height(16.dp))

        // Date & Time Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = IslamicGreen)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentTime, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text(currentDate, color = Color.White.copy(0.9f), fontSize = 14.sp, textAlign = TextAlign.Center)
                if (hijriDate.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(hijriDate, color = IslamicGold, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Next Prayer Card
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                CircularProgressIndicator(color = IslamicGreen)
            }
        } else if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(error ?: "", modifier = Modifier.padding(16.dp),
                    color = Color.Red, textAlign = TextAlign.Center)
            }
        } else {
            nextPrayer?.let { next ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الصلاة القادمة", color = IslamicGreen.copy(0.7f), fontSize = 13.sp)
                            Text(next.nameAr, color = IslamicGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(next.time, color = IslamicGreenDark, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Today's prayers summary
            if (prayers.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("مواقيت اليوم", color = IslamicGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        prayers.forEach { prayer ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    prayer.nameAr,
                                    color = when {
                                        prayer.isNext   -> IslamicGreen
                                        prayer.isPassed -> PrayerPassed
                                        else            -> PrayerNormal
                                    },
                                    fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    prayer.time,
                                    color = when {
                                        prayer.isNext   -> IslamicGreen
                                        prayer.isPassed -> PrayerPassed
                                        else            -> PrayerNormal
                                    },
                                    fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (prayer != prayers.last()) Divider(color = Color.LightGray.copy(0.5f))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun fetchLocation(context: android.content.Context, onResult: (Double, Double) -> Unit) {
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            client.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) onResult(loc.latitude, loc.longitude)
                else onResult(30.0444, 31.2357) // Cairo fallback
            }.addOnFailureListener {
                onResult(30.0444, 31.2357)
            }
        }
    } catch (e: Exception) {
        onResult(30.0444, 31.2357)
    }
}
