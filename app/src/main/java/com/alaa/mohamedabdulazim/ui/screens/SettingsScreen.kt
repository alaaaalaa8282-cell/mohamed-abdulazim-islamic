package com.alaa.mohamedabdulazim.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaa.mohamedabdulazim.data.models.AppSettings
import com.alaa.mohamedabdulazim.service.ZekrService
import com.alaa.mohamedabdulazim.ui.components.FatherHeader
import com.alaa.mohamedabdulazim.ui.theme.*
import com.alaa.mohamedabdulazim.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val context  = LocalContext.current
    val settings by vm.settings.collectAsState()

    var latText  by remember { mutableStateOf(settings.latitude.toString()) }
    var lngText  by remember { mutableStateOf(settings.longitude.toString()) }
    var cityText by remember { mutableStateOf(settings.cityName) }

    LaunchedEffect(settings) {
        if (latText == "0.0") latText = settings.latitude.toString()
        if (lngText == "0.0") lngText = settings.longitude.toString()
        if (cityText.isEmpty()) cityText = settings.cityName
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FatherHeader(compact = true)

        Spacer(Modifier.height(8.dp))

        // === Prayer Calculation Method ===
        SettingsSection(title = "طريقة حساب المواقيت", icon = Icons.Filled.Calculate) {
            val methods = listOf(
                0 to "شمال أمريكا (ISNA)",
                1 to "مسلم الجاليات (MWL)",
                2 to "مصر (الأزهر)",
                3 to "مكة المكرمة (رابطة العالم)",
                4 to "كراتشي",
                5 to "الكويت",
                8 to "قطر",
                9 to "سنغافورة",
                11 to "تركيا",
                15 to "مصر (وزارة الأوقاف)"
            )
            methods.forEach { (id, name) ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        vm.save(settings.copy(calculationMethod = id))
                    }.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = settings.calculationMethod == id,
                        onClick  = { vm.save(settings.copy(calculationMethod = id)) },
                        colors   = RadioButtonDefaults.colors(selectedColor = IslamicGreen)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(name, color = IslamicGreenDark, fontSize = 14.sp)
                }
            }
        }

        // === Location Settings ===
        SettingsSection(title = "الموقع", icon = Icons.Filled.LocationOn) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("استخدام GPS تلقائياً", color = IslamicGreenDark)
                Switch(
                    checked = settings.useGps,
                    onCheckedChange = { vm.save(settings.copy(useGps = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = IslamicGold, checkedTrackColor = IslamicGreen)
                )
            }
            if (!settings.useGps) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value  = cityText,
                        onValueChange = { cityText = it },
                        label  = { Text("اسم المدينة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value  = latText,
                            onValueChange = { latText = it },
                            label  = { Text("خط العرض") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen)
                        )
                        OutlinedTextField(
                            value  = lngText,
                            onValueChange = { lngText = it },
                            label  = { Text("خط الطول") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            vm.save(settings.copy(
                                latitude  = latText.toDoubleOrNull() ?: 0.0,
                                longitude = lngText.toDoubleOrNull() ?: 0.0,
                                cityName  = cityText
                            ))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("حفظ الموقع", color = Color.White) }
                }
            }
        }

        // === Athan Settings ===
        SettingsSection(title = "إعدادات الأذان", icon = Icons.Filled.VolumeUp) {
            // Volume slider
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("مستوى الصوت: ${(settings.athanVolume * 100).toInt()}%",
                    color = IslamicGreenDark, fontSize = 14.sp)
                Slider(
                    value = settings.athanVolume,
                    onValueChange = { vm.save(settings.copy(athanVolume = it)) },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGold, activeTrackColor = IslamicGreen
                    )
                )
            }

            // Per-prayer athan selection
            val prayers = listOf(
                "الفجر"  to "fajr",
                "الظهر"  to "dhuhr",
                "العصر"  to "asr",
                "المغرب" to "maghrib",
                "العشاء" to "isha"
            )
            val athanOptions = listOf("default" to "الأذان الافتراضي")
            prayers.forEach { (name, key) ->
                val current = when (key) {
                    "fajr"    -> settings.fajrAthan
                    "dhuhr"   -> settings.dhuhrAthan
                    "asr"     -> settings.asrAthan
                    "maghrib" -> settings.maghribAthan
                    else      -> settings.ishaAthan
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("صلاة $name", color = IslamicGreenDark, fontSize = 14.sp)
                    Text(
                        athanOptions.find { it.first == current }?.second ?: "الافتراضي",
                        color = IslamicGreen, fontSize = 13.sp
                    )
                }
            }
            Text(
                "ملاحظة: لتغيير الأذان ضع ملف صوتي في مجلد التطبيق",
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = IslamicGreenDark.copy(0.5f),
                fontSize = 11.sp
            )
        }

        // === Zekr Service Settings ===
        SettingsSection(title = "الذكر التلقائي", icon = Icons.Filled.NotificationsActive) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("تفعيل الذكر التلقائي", color = IslamicGreenDark, fontWeight = FontWeight.Medium)
                    Text("يعمل في الخلفية مع إشعارات", color = IslamicGreenDark.copy(0.6f), fontSize = 12.sp)
                }
                Switch(
                    checked = settings.zekrEnabled,
                    onCheckedChange = { enabled ->
                        vm.save(settings.copy(zekrEnabled = enabled))
                        if (enabled) ZekrService.start(context)
                        else context.stopService(Intent(context, ZekrService::class.java))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = IslamicGold, checkedTrackColor = IslamicGreen)
                )
            }

            if (settings.zekrEnabled) {
                // Interval
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("الفاصل الزمني: ${settings.zekrIntervalMinutes} دقيقة",
                        color = IslamicGreenDark, fontSize = 14.sp)
                    val intervals = listOf(15, 30, 60, 120)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        intervals.forEach { mins ->
                            FilterChip(
                                selected = settings.zekrIntervalMinutes == mins,
                                onClick  = { vm.save(settings.copy(zekrIntervalMinutes = mins)) },
                                label    = { Text("${mins}د") },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IslamicGreen,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }
                }

                // Mode
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("نمط الأذكار", color = IslamicGreenDark, fontSize = 14.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.zekrMode == "sequential",
                            onClick  = { vm.save(settings.copy(zekrMode = "sequential")) },
                            label    = { Text("تسلسلي") },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreen, selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = settings.zekrMode == "repeat",
                            onClick  = { vm.save(settings.copy(zekrMode = "repeat")) },
                            label    = { Text("تكرار") },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreen, selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // === Battery optimization ===
        SettingsSection(title = "حماية الخلفية", icon = Icons.Filled.BatteryFull) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "لضمان عمل الأذان والأذكار في الخلفية، يُنصح بإلغاء تحسين البطارية للتطبيق",
                    color = IslamicGreenDark, fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("إلغاء تحسين البطارية", color = Color.White) }
            }
        }

        Spacer(Modifier.height(24.dp))

        // App version
        Text(
            "محمد عبد العظيم الطويل الإسلامي v1.0\nرحمه الله وأسكنه فسيح جناته",
            Modifier.fillMaxWidth().padding(16.dp),
            color = IslamicGreenDark.copy(0.5f),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().background(IslamicGreen.copy(0.08f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = IslamicGreen, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = IslamicGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Divider(color = IslamicGreen.copy(0.1f))
            content()
        }
    }
}
