package com.alaa.mohamedabdulazim.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val numberOfAyahs: Int,
    val revelationType: String
) {
    val nameAr: String get() = name
    val type: String get() = if (revelationType == "Meccan") "مكية" else "مدنية"
}

data class Ayah(
    val number: Int,
    val numberInSurah: Int,
    val text: String
)

class QuranRepository(private val context: Context) {
    private val gson = Gson()
    private val cacheDir = File(context.filesDir, "quran_cache").also { it.mkdirs() }

    // جيب قائمة السور
    suspend fun getSurahList(): List<Surah> = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, "surah_list.json")
        if (cacheFile.exists()) {
            try {
                val type = object : TypeToken<List<Surah>>() {}.type
                return@withContext gson.fromJson<List<Surah>>(cacheFile.readText(), type)
            } catch (_: Exception) {}
        }
        // جيب من الـ API
        val json = URL("https://api.alquran.cloud/v1/surah").readText()
        val root = gson.fromJson(json, Map::class.java)
        val data = (root["data"] as List<*>)
        val surahs = data.map { item ->
            val map = item as Map<*, *>
            Surah(
                number        = (map["number"] as Double).toInt(),
                name          = map["name"] as String,
                englishName   = map["englishName"] as String,
                numberOfAyahs = (map["numberOfAyahs"] as Double).toInt(),
                revelationType = map["revelationType"] as String
            )
        }
        cacheFile.writeText(gson.toJson(surahs))
        surahs
    }

    // جيب آيات سورة
    suspend fun getSurahAyahs(surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val cacheFile = File(cacheDir, "surah_$surahNumber.json")
        if (cacheFile.exists()) {
            try {
                val type = object : TypeToken<List<Ayah>>() {}.type
                return@withContext gson.fromJson<List<Ayah>>(cacheFile.readText(), type)
            } catch (_: Exception) {}
        }
        val json = URL("https://api.alquran.cloud/v1/surah/$surahNumber").readText()
        val root = gson.fromJson(json, Map::class.java)
        val data = (root["data"] as Map<*, *>)
        val ayahs = (data["ayahs"] as List<*>).map { item ->
            val map = item as Map<*, *>
            Ayah(
                number         = (map["number"] as Double).toInt(),
                numberInSurah  = (map["numberInSurah"] as Double).toInt(),
                text           = map["text"] as String
            )
        }
        cacheFile.writeText(gson.toJson(ayahs))
        ayahs
    }

    fun isSurahCached(surahNumber: Int): Boolean =
        File(cacheDir, "surah_$surahNumber.json").exists()
}
