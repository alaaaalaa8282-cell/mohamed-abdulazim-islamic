package com.alaa.mohamedabdulazim.ui.settings

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.mohamedabdulazim.data.local.AthanPrefs
import com.alaa.mohamedabdulazim.service.AthanService

@Composable
fun AthanSoundPickerSection(context: Context = LocalContext.current) {

    val builtInSounds = listOf(
        "default"       to "أذان افتراضي",
        "elharm"        to "أذان الحرم المكي",
        "elhosary"      to "أذان الحصري",
        "mohamed_refat" to "أذان محمد رفعت",
        "abd_elbasit"   to "عبد الباسط عبد الصمد",
    )

    var selectedKey  by remember { mutableStateOf(AthanPrefs.getSelectedKey(context)) }
    var customUri    by remember { mutableStateOf(AthanPrefs.getCustomUri(context)) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose { previewPlayer?.release() }
    }

    // فتح الملفات من الهاتف
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // احتفظ بصلاحية الوصول للملف
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            AthanPrefs.saveCustomUri(context, it.toString())
            customUri   = it.toString()
            selectedKey = "custom"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text       = "🔊 اختر صوت الأذان",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.fillMaxWidth(),
            textAlign  = TextAlign.Right,
            color      = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // الأصوات المدمجة
        builtInSounds.forEach { (key, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedKey = key
                        AthanPrefs.saveSelectedKey(context, key)
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // زر المعاينة
                IconButton(onClick = {
                    previewPlayer?.release()
                    previewPlayer = MediaPlayer.create(context, AthanService.getAthanResId(key))
                    previewPlayer?.start()
                }) {
                    Icon(
                        imageVector        = Icons.Default.PlayArrow,
                        contentDescription = "معاينة",
                        tint               = MaterialTheme.colorScheme.secondary
                    )
                }

                Text(
                    text      = name,
                    fontSize  = 16.sp,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Right
                )

                RadioButton(
                    selected = selectedKey == key,
                    onClick  = {
                        selectedKey = key
                        AthanPrefs.saveSelectedKey(context, key)
                    }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // صوت مخصص من الهاتف
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { filePicker.launch("audio/*") }
                .padding(vertical = 6.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { filePicker.launch("audio/*") }) {
                Text(
                    text = if (customUri != null) "تغيير الملف" else "اختر ملف",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = if (customUri != null)
                    "✅ صوت مخصص من هاتفك"
                else
                    "📂 رفع صوت من الهاتف",
                fontSize  = 16.sp,
                modifier  = Modifier.weight(1f),
                textAlign = TextAlign.Right,
                color     = if (customUri != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
            )

            RadioButton(
                selected = selectedKey == "custom",
                onClick  = { filePicker.launch("audio/*") }
            )
        }
    }
}
