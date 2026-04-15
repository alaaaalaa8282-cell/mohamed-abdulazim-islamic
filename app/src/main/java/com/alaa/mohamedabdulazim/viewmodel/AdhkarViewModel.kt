package com.alaa.mohamedabdulazim.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alaa.mohamedabdulazim.data.local.AdhkarData
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.data.models.AdhkarCategory
import com.alaa.mohamedabdulazim.data.models.Zekr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdhkarViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferencesManager(app)

    private val _selectedCategory = MutableStateFlow(AdhkarCategory.MORNING)
    val selectedCategory: StateFlow<AdhkarCategory> = _selectedCategory

    private val _adhkarList = MutableStateFlow<List<Zekr>>(AdhkarData.morningAdhkar.map { it.copy() })
    val adhkarList: StateFlow<List<Zekr>> = _adhkarList

    private val counters = mutableMapOf<Int, Int>()

    fun selectCategory(cat: AdhkarCategory) {
        _selectedCategory.value = cat
        _adhkarList.value = when (cat) {
            AdhkarCategory.MORNING     -> AdhkarData.morningAdhkar.map { it.copy(currentCount = counters[it.id] ?: 0) }
            AdhkarCategory.EVENING     -> AdhkarData.eveningAdhkar.map { it.copy(currentCount = counters[it.id] ?: 0) }
            AdhkarCategory.AFTER_PRAYER -> AdhkarData.afterPrayerAdhkar.map { it.copy(currentCount = counters[it.id] ?: 0) }
            AdhkarCategory.TASBIH      -> AdhkarData.tasbihList.map { it.copy(currentCount = counters[it.id] ?: 0) }
        }
    }

    fun incrementZekr(zekr: Zekr) {
        val current = counters[zekr.id] ?: 0
        counters[zekr.id] = current + 1
        _adhkarList.value = _adhkarList.value.map {
            if (it.id == zekr.id) it.copy(currentCount = current + 1) else it
        }
    }

    fun resetZekr(zekr: Zekr) {
        counters[zekr.id] = 0
        _adhkarList.value = _adhkarList.value.map {
            if (it.id == zekr.id) it.copy(currentCount = 0) else it
        }
    }

    fun resetAll() {
        counters.clear()
        selectCategory(_selectedCategory.value)
    }
}
