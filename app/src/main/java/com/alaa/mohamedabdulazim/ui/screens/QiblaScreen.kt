package com.alaa.mohamedabdulazim.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaa.mohamedabdulazim.ui.components.FatherHeader
import com.alaa.mohamedabdulazim.ui.theme.*
import com.alaa.mohamedabdulazim.viewmodel.QiblaViewModel
import kotlin.math.*

@Composable
fun QiblaScreen(vm: QiblaViewModel = viewModel()) {
    val context    = LocalContext.current
    val azimuth    by vm.azimuth.collectAsState()
    val qiblaAngle by vm.qiblaAngle.collectAsState()
    val hasCompass by vm.hasCompass.collectAsState()

    var latitude  by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var hasLocation by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            fetchLocation(context) { lat, lng ->
                latitude = lat; longitude = lng; hasLocation = true
                vm.startListening(lat, lng)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fetchLocation(context) { lat, lng ->
                latitude = lat; longitude = lng; hasLocation = true
                vm.startListening(lat, lng)
            }
        } else {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    DisposableEffect(Unit) { onDispose { vm.stopListening() } }

    // Smooth rotation animation
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "azimuth"
    )

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FatherHeader(compact = true)

        Spacer(Modifier.height(16.dp))

        Text(
            "اتجاه القبلة",
            color = IslamicGreen,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        if (hasLocation) {
            Text(
                "زاوية القبلة: ${qiblaAngle.toInt()}°",
                color = IslamicGreenDark.copy(0.7f),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        if (!hasCompass) {
            Card(
                Modifier.fillMaxWidth().padding(24.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFF3E0))
            ) {
                Text(
                    "جهازك لا يدعم البوصلة\nيُرجى استخدام جهاز يحتوي على مستشعر المغناطيس",
                    Modifier.padding(20.dp),
                    color = Color(0xFFE65100),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Compass
            Box(
                Modifier.size(280.dp),
                Alignment.Center
            ) {
                // Compass rose background
                Canvas(Modifier.fillMaxSize().rotate(-animatedAzimuth)) {
                    drawCompassRose(this)
                }
                // Qibla needle
                val needleAngle = qiblaAngle - azimuth
                Box(
                    Modifier.fillMaxSize().rotate(needleAngle),
                    Alignment.TopCenter
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val len = size.height * 0.42f
                        // Green needle toward Kaaba
                        drawLine(
                            color = IslamicGreen,
                            start = Offset(cx, cy),
                            end   = Offset(cx, cy - len),
                            strokeWidth = 6f
                        )
                        // Gold dot at tip
                        drawCircle(color = IslamicGold, radius = 10f, center = Offset(cx, cy - len))
                        // Center dot
                        drawCircle(color = IslamicGreenDark, radius = 14f, center = Offset(cx, cy))
                        drawCircle(color = Color.White, radius = 8f, center = Offset(cx, cy))
                    }
                }
                // Kaaba label
                Box(Modifier.fillMaxSize().rotate(qiblaAngle - azimuth), Alignment.TopCenter) {
                    Text("🕋", Modifier.padding(top = 4.dp), fontSize = 22.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Direction info
            val relativeAngle = ((qiblaAngle - azimuth + 360) % 360).toInt()
            val isAligned = relativeAngle < 15 || relativeAngle > 345

            Card(
                Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(if (isAligned) Color(0xFFE8F5E9) else Color.White)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isAligned) "✅ أنت متجه نحو القبلة" else "استدر $relativeAngle° نحو القبلة",
                        color     = if (isAligned) IslamicGreen else IslamicGreenDark,
                        fontSize  = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (hasLocation && latitude != 0.0 && longitude != 0.0) {
                        Spacer(Modifier.height(8.dp))
                        val dist = calculateDistanceToKaaba(latitude, longitude)
                        Text(
                            "المسافة إلى الكعبة: ${dist.toInt()} كم",
                            color = IslamicGreenDark.copy(0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun drawCompassRose(scope: DrawScope) {
    val cx = scope.size.width / 2
    val cy = scope.size.height / 2
    val r  = scope.size.width / 2 - 10f

    // Outer circle
    scope.drawCircle(
        color  = IslamicGreen.copy(0.15f),
        radius = r,
        center = Offset(cx, cy)
    )
    scope.drawCircle(
        color       = IslamicGreen,
        radius      = r,
        center      = Offset(cx, cy),
        style       = androidx.compose.ui.graphics.drawscope.Stroke(4f)
    )

    // Cardinal directions as tick marks
    val cardinalLen = r * 0.12f
    for (i in 0 until 360 step 45) {
        val rad  = Math.toRadians(i.toDouble()).toFloat()
        val r2   = r - 8f
        val isCardinal = i % 90 == 0
        val tickLen = if (isCardinal) cardinalLen else cardinalLen * 0.5f
        val startX = cx + r2 * sin(rad)
        val startY = cy - r2 * cos(rad)
        val endX   = cx + (r2 - tickLen) * sin(rad)
        val endY   = cy - (r2 - tickLen) * cos(rad)
        scope.drawLine(
            color = if (isCardinal) IslamicGold else IslamicGreen.copy(0.5f),
            start = Offset(startX, startY),
            end   = Offset(endX, endY),
            strokeWidth = if (isCardinal) 4f else 2f
        )
    }
}

private fun calculateDistanceToKaaba(lat: Double, lng: Double): Double {
    val R    = 6371.0
    val lat1 = Math.toRadians(lat)
    val lat2 = Math.toRadians(21.4225)
    val dLat = Math.toRadians(21.4225 - lat)
    val dLng = Math.toRadians(39.8262 - lng)
    val a    = sin(dLat/2).pow(2) + cos(lat1)*cos(lat2)*sin(dLng/2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1-a))
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
