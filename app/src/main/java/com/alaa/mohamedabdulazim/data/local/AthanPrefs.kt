package com.alaa.mohamedabdulazim.data.local

import android.content.Context

object AthanPrefs {
    private const val PREFS_NAME   = "athan_prefs"
    private const val KEY_ATHAN    = "selected_athan_key"
    private const val KEY_CUSTOM   = "custom_athan_uri"

    fun getSelectedKey(ctx: Context): String =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ATHAN, "default") ?: "default"

    fun saveSelectedKey(ctx: Context, key: String) =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ATHAN, key).apply()

    fun getCustomUri(ctx: Context): String? =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM, null)

    fun saveCustomUri(ctx: Context, uri: String) =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM, uri)
            .putString(KEY_ATHAN, "custom")
            .apply()
}
