package com.hlodving.mytestgold
//Этот класс сбрасывает виджет при откате таймера даже если приложение закрыто


import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class TimerWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val context = applicationContext

        // Сброс сохранённых значений
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("goldCount", 0)
            putLong("timerEndTime", 0L)
            apply()
        }

        // Обновление виджета
        GoldWidget.updateAllWidgets(context, 0)


        return Result.success()
    }
}
