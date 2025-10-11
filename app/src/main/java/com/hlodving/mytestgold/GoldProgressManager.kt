// Файл: com/hlodving/mytestgold/GoldProgressManager.kt

package com.hlodving.mytestgold

import android.content.Context
import android.content.SharedPreferences
import com.hlodving.mytestgold.Stage.Companion.getStageData

// Класс, управляющий ТОЛЬКО ЛОГИКОЙ первого прогресс-бара.
// Он больше не знает о UI (binding).
class GoldProgressManager(
    private val context: Context,
    // progressColors больше не нужен, так как он используется только для UI
    // и передается напрямую в MainViewUpdater
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)

    var count: Int = 0
        private set

    var lastStage: Int = 1
        private set

    fun load() {
        count = prefs.getInt("goldCount", 0)
        lastStage = getStageData(count).third.number
    }

    fun save() {
        prefs.edit().putInt("goldCount", count).apply()
    }

    fun increment(): Triple<Int, Int, Stage> {
        count++
        save()
        return getStageData(count)
    }

    fun reset() {
        count = 0
        lastStage = 1
        save()
    }

    // МЕТОД updateUI() УДАЛЕН.
    // Вся логика обновления UI теперь находится в MainViewUpdater.

    fun isNextStage(stage: Stage): Boolean {
        return stage.number > lastStage
    }

    fun markStageReached(stage: Stage) {
        lastStage = stage.number
    }



    fun getStage(): Stage {
        return getStageData(count).third
    }

    fun getProgressInfo(): Triple<Int, Int, Stage> {
        return getStageData(count)
    }
}