package com.hlodving.mytestgold
//Не дает приложению уйти в спячку


import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class AnimationCheckWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext

        // 1. Проверяем, должен ли оберег вообще быть активен.
        // Для этого читаем те же сохраненные данные, что и приложение.
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        val timerEndTime = prefs.getLong("timerEndTime", 0L)
        val isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true)

        // Оберег активен, если таймер еще не истек (или он вечный) и анимация включена в настройках.
        val isOberegActive = (timerEndTime > System.currentTimeMillis() || timerEndTime == Long.MAX_VALUE) && isAnimationEnabled

        // 2. Если оберег должен быть активен, но сервис анимации по какой-то причине не запущен...
        if (isOberegActive && !isServiceRunning(context, WidgetAnimationService::class.java)) {
            // ...то мы принудительно его запускаем.
            val serviceIntent = Intent(context, WidgetAnimationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        // 3. Сообщаем WorkManager, что задача успешно выполнена.
        return Result.success()
    }

    /**
     * Вспомогательная функция, которая проверяет, запущен ли сервис в данный момент.
     * Этот код взят из вашего файла GoldWidget.kt для переиспользования.
     */
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}