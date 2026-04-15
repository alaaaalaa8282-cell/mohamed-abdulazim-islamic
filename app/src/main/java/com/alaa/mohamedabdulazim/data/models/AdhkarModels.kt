package com.alaa.mohamedabdulazim.data.models

enum class AdhkarCategory(val nameAr: String) {
    MORNING("أذكار الصباح"),
    EVENING("أذكار المساء"),
    AFTER_PRAYER("أذكار بعد الصلاة"),
    TASBIH("التسبيح")
}

data class Zekr(
    val id: Int,
    val text: String,
    val count: Int,
    val benefit: String = "",
    val category: AdhkarCategory,
    var currentCount: Int = 0
)
