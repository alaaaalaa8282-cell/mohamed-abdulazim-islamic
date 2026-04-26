package com.alaa.mohamedabdulazim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alaa.mohamedabdulazim.data.repository.Ayah
import com.alaa.mohamedabdulazim.data.repository.Surah
import com.alaa.mohamedabdulazim.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = QuranRepository(app)

    private val _surahs    = MutableStateFlow<List<Surah>>(emptyList())
    val surahs = _surahs.asStateFlow()

    private val _ayahs     = MutableStateFlow<List<Ayah>>(emptyList())
    val ayahs = _ayahs.asStateFlow()

    private val _loading   = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error     = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah = _selectedSurah.asStateFlow()

    init { loadSurahList() }

    private fun loadSurahList() {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            try {
                _surahs.value = repo.getSurahList()
            } catch (e: Exception) {
                _error.value = "تعذّر تحميل القرآن، تحقق من الاتصال"
            } finally {
                _loading.value = false
            }
        }
    }

    fun openSurah(surah: Surah) {
        _selectedSurah.value = surah
        _ayahs.value = emptyList()
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            try {
                _ayahs.value = repo.getSurahAyahs(surah.number)
            } catch (e: Exception) {
                _error.value = "تعذّر تحميل السورة"
            } finally {
                _loading.value = false
            }
        }
    }

    fun closeSurah() {
        _selectedSurah.value = null
        _ayahs.value = emptyList()
    }

    fun isCached(surahNumber: Int) = repo.isSurahCached(surahNumber)
}
