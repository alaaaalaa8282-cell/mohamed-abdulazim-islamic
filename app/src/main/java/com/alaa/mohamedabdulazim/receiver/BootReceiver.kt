package com.alaa.mohamedabdulazim.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.service.ZekrService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = PreferencesManager(context)
            if (prefs.getSettings().zekrEnabled)
                ZekrService.start(context)
        }
    }
}
