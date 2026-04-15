package com.alaa.mohamedabdulazim

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.service.ZekrService
import com.alaa.mohamedabdulazim.ui.navigation.AppNavGraph
import com.alaa.mohamedabdulazim.ui.theme.IslamicTheme

class MainActivity : ComponentActivity() {

    private val notifLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not - continue */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Start Zekr service if enabled
        val prefs = PreferencesManager(applicationContext)
        if (prefs.getSettings().zekrEnabled) {
            ZekrService.start(applicationContext)
        }

        setContent {
            IslamicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
