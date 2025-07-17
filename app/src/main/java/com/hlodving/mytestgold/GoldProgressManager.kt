package com.hlodving.mytestgold

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.hlodving.mytestgold.Stage.Companion.getStageData
import com.hlodving.mytestgold.databinding.ActivityMainBinding

// Класс, управляющий логикой первого прогресс-бара (золота)
class GoldProgressManager(
    private val context: Context, // Контекст приложения
    private val binding: ActivityMainBinding, // ViewBinding для доступа к элементам UI
    private val progressColors: List<Int> // Список ресурсов стилей прогресс-бара по стадиям
) {
    // SharedPreferences для сохранения количества золота
    private val prefs: SharedPreferences =
        context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)

    // Общее количество кликов (золота)
    var count: Int = 0
        private set

    // Последняя достигнутая стадия (для отслеживания переходов)
    var lastStage: Int = 1
        private set

    // Загружает сохранённое значение золота и вычисляет текущую стадию
    fun load() {
        count = prefs.getInt("goldCount", 0)
        lastStage = getStageData(count).third.number
    }

    // Сохраняет текущее значение золота
    fun save() {
        prefs.edit().putInt("goldCount", count).apply()
    }

    // Увеличивает счётчик золота и возвращает данные текущей стадии
    fun increment(): Triple<Int, Int, Stage> {
        count++
        save()
        return getStageData(count)
    }

    // Полный сброс прогресса и возврат к первой стадии
    fun reset() {
        count = 0
        lastStage = 1
        save()
    }

    // Обновляет UI первого прогресс-бара (текст, прогресс, цвет)
    fun updateUI() {
        val (stageProgress, stageMax, currentStage) = getStageData(count)

        binding.progressBar.max = stageMax
        binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
        binding.progressText.text = "$stageProgress / $stageMax"

        val colorDrawableId = progressColors[(currentStage.number - 1) % progressColors.size]
        binding.progressBar.progressDrawable =
            ContextCompat.getDrawable(context, colorDrawableId)
    }

    // Проверка: перешли ли мы на новую стадию?
    fun isNextStage(stage: Stage): Boolean {
        return stage.number > lastStage
    }

    // Зафиксировать новую достигнутую стадию
    fun markStageReached(stage: Stage) {
        lastStage = stage.number
    }

    // Получить текущую стадию по количеству золота
    fun getStage(): Stage {
        return getStageData(count).third
    }

    // Получить данные о прогрессе: текущее значение, максимум и стадия
    fun getProgressInfo(): Triple<Int, Int, Stage> {
        return getStageData(count)
    }
}
