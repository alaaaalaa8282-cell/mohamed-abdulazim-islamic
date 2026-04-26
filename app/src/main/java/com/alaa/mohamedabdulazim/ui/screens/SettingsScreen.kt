package com.alaa.mohamedabdulazim.ui.screens

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.data.models.AppSettings
import com.alaa.mohamedabdulazim.service.AthanService
import com.alaa.mohamedabdulazim.service.ZekrService
import com.alaa.mohamedabdulazim.ui.components.FatherHeader
import com.alaa.mohamedabdulazim.ui.theme.*
import com.alaa.mohamedabdulazim.viewmodel.SettingsViewModel

// قائمة الأصوات المدمجة
private val ATHAN_OPTIONS = listOf(
    "default"       to "أذان افتراضي",
    "elharm"        to "أذان الحرم المكي",
    "elhosary"      to "أذان الحصري",
    "mohamed_refat" to "أذان محمد رفعت",
    "abd_elbasit"   to "عبد الباسط عبد الصمد",
    "silent"        to "🔇 صامت (بدون أذان)",
    "custom"        to "📂 صوت من هاتفك"
)

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val context  = LocalContext.current
    val settings by vm.settings.collectAsState()
    val prefsManager = remember { PreferencesManager(context) }

    var latText  by remember { mutableStateOf(settings.latitude.toString()) }
    var lngText  by remember { mutableStateOf(settings.longitude.toString()) }
    var cityText by remember { mutableStateOf(settings.cityName) }

    // الصلاة اللي بيتم اختيار صوتها حالياً
    var dialogPrayerKey  by remember { mutableStateOf("") }
    var dialogPrayerName by remember { mutableStateOf("") }
    var showDialog       by remember { mutableStateOf(false) }

    // MediaPlayer للمعاينة
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) { onDispose { previewPlayer?.release() } }

    // file picker للصوت المخصص
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            // احفظ الـ URI للصلاة المحددة
            prefsManager.saveCustomAthanUri(dialogPrayerKey, it.toString())
            // احفظ "custom" كـ key في الإعدادات
            val updated = when (dialogPrayerKey) {
                "fajr"    -> settings.copy(fajrAthan    = "custom")
                "dhuhr"   -> settings.copy(dhuhrAthan   = "custom")
                "asr"     -> settings.copy(asrAthan     = "custom")
                "maghrib" -> settings.copy(maghribAthan = "custom")
                else      -> settings.copy(ishaAthan    = "custom")
            }
            vm.save(updated)
            showDialog = false
        }
    }

    LaunchedEffect(settings) {
        if (latText == "0.0") latText = settings.latitude.toString()
        if (lngText == "0.0") lngText = settings.longitude.toString()
        if (cityText.isEmpty()) cityText = settings.cityName
    }

    // Dialog اختيار الصوت
    if (showDialog) {
        AthanSoundDialog(
            prayerName    = dialogPrayerName,
            currentKey    = when (dialogPrayerKey) {
                "fajr"    -> settings.fajrAthan
                "dhuhr"   -> settings.dhuhrAthan
                "asr"     -> settings.asrAthan
                "maghrib" -> settings.maghribAthan
                else      -> settings.ishaAthan
            },
            hasCustomUri  = prefsManager.getCustomAthanUri(dialogPrayerKey) != null,
            onSelect      = { key ->
                if (key == "custom") {
                    filePicker.launch("audio/*")
                } else {
                    previewPlayer?.release()
                    val updated = when (dialogPrayerKey) {
                        "fajr"    -> settings.copy(fajrAthan    = key)
                        "dhuhr"   -> settings.copy(dhuhrAthan   = key)
                        "asr"     -> settings.copy(asrAthan     = key)
                        "maghrib" -> settings.copy(maghribAthan = key)
                        else      -> settings.copy(ishaAthan    = key)
                    }
                    vm.save(updated)
                    showDialog = false
                }
            },
            onPreview     = { key ->
                previewPlayer?.release()
                previewPlayer = MediaPlayer.create(context, AthanService.getResId(key))
                previewPlayer?.start()
            },
            onDismiss     = { showDialog = false; previewPlayer?.release() }
        )
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        FatherHeader(compact = true)
        Spacer(Modifier.height(8.dp))

        // === طريقة حساب المواقيت ===
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
                    Modifier.fillMaxWidth()
                        .clickable { vm.save(settings.copy(calculationMethod = id)) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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

        // === الموقع ===
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
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = IslamicGold,
                        checkedTrackColor = IslamicGreen
                    )
                )
            }
            if (!settings.useGps) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = cityText,
                        onValueChange = { cityText = it },
                        label = { Text("اسم المدينة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen)
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latText,
                            onValueChange = { latText = it },
                            label = { Text("خط العرض") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen)
                        )
                        OutlinedTextField(
                            value = lngText,
                            onValueChange = { lngText = it },
                            label = { Text("خط الطول") },
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

        // === إعدادات الأذان ===
        SettingsSection(title = "إعدادات الأذان", icon = Icons.Filled.VolumeUp) {
            // مستوى الصوت
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "مستوى الصوت: ${(settings.athanVolume * 100).toInt()}%",
                    color = IslamicGreenDark, fontSize = 14.sp
                )
                Slider(
                    value = settings.athanVolume,
                    onValueChange = { vm.save(settings.copy(athanVolume = it)) },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = IslamicGold,
                        activeTrackColor = IslamicGreen
                    )
                )
            }

            Divider(color = IslamicGreen.copy(0.1f))

            // اختيار صوت لكل صلاة
            val prayers = listOf(
                Triple("fajr",    "الفجر",  settings.fajrAthan),
                Triple("dhuhr",   "الظهر",  settings.dhuhrAthan),
                Triple("asr",     "العصر",  settings.asrAthan),
                Triple("maghrib", "المغرب", settings.maghribAthan),
                Triple("isha",    "العشاء", settings.ishaAthan)
            )

            prayers.forEach { (key, name, currentKey) ->
                val label = ATHAN_OPTIONS.find { it.first == currentKey }?.second
                    ?: "أذان افتراضي"
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            dialogPrayerKey  = key
                            dialogPrayerName = name
                            showDialog       = true
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // اسم الصوت الحالي + أيقونة تغيير
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = IslamicGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(label, color = IslamicGreen, fontSize = 13.sp)
                    }
                    Text("صلاة $name", color = IslamicGreenDark, fontSize = 14.sp)
                }
                if (key != "isha") Divider(
                    color = IslamicGreen.copy(0.07f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // === الذكر التلقائي ===
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
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = IslamicGold,
                        checkedTrackColor = IslamicGreen
                    )
                )
            }
            if (settings.zekrEnabled) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        "الفاصل الزمني: ${settings.zekrIntervalMinutes} دقيقة",
                        color = IslamicGreenDark, fontSize = 14.sp
                    )
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
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text("نمط الأذكار", color = IslamicGreenDark, fontSize = 14.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = settings.zekrMode == "sequential",
                            onClick  = { vm.save(settings.copy(zekrMode = "sequential")) },
                            label    = { Text("تسلسلي") },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreen,
                                selectedLabelColor     = Color.White
                            )
                        )
                        FilterChip(
                            selected = settings.zekrMode == "repeat",
                            onClick  = { vm.save(settings.copy(zekrMode = "repeat")) },
                            label    = { Text("تكرار") },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IslamicGreen,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }
            }
        }

        // === حماية الخلفية ===
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
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("إلغاء تحسين البطارية", color = Color.White) }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "محمد عبد العظيم الطويل الإسلامي v1.0\nرحمه الله وأسكنه فسيح جناته",
            Modifier.fillMaxWidth().padding(16.dp),
            color = IslamicGreenDark.copy(0.5f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
    }
}

// === Dialog اختيار الصوت ===
@Composable
private fun AthanSoundDialog(
    prayerName:   String,
    currentKey:   String,
    hasCustomUri: Boolean,
    onSelect:     (String) -> Unit,
    onPreview:    (String) -> Unit,
    onDismiss:    () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "اختر أذان صلاة $prayerName",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    color      = IslamicGreenDark,
                    modifier   = Modifier.fillMaxWidth(),
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))

                ATHAN_OPTIONS.forEach { (key, name) ->
                    val isCustom   = key == "custom"
                    val customLabel = if (isCustom && hasCustomUri) "✅ صوت مخصص (تغيير)"
                                      else name
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(key) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentKey == key,
                            onClick  = { onSelect(key) },
                            colors   = RadioButtonDefaults.colors(selectedColor = IslamicGreen)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            customLabel,
                            color    = if (isCustom) IslamicGreen else IslamicGreenDark,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        // زر المعاينة للأصوات المدمجة فقط
                        if (!isCustom) {
                            IconButton(
                                onClick = { onPreview(key) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "معاينة",
                                    tint = IslamicGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    if (key != "custom") Divider(color = IslamicGreen.copy(0.07f))
                }

                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) { Text("إغلاق", color = IslamicGreenDark) }
            }
        }
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
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .background(IslamicGreen.copy(0.08f))
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
