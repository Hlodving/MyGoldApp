package com.hlodving.mytestgold

import android.content.Context

class GlobalTapCounter(private val context: Context) {

    // Переменная общего количества тапов
    var totalTaps: Int = 0
        private set

    // Загружаем сохранённое значение из памяти
    fun load() {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        totalTaps = prefs.getInt("totalTaps", 0)
    }

    // Увеличиваем и сохраняем значение
    fun increment() {
        totalTaps++
        save()
    }

    // Сохраняем значение в память
    private fun save() {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("totalTaps", totalTaps).apply()
    }
}
