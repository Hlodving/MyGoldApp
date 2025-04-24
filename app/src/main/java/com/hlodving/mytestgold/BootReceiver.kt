package com.hlodving.mytestgold

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
            val endTime = prefs.getLong("timerEndTime", 0L)
            val goldCount = prefs.getInt("goldCount", 0)
            val now = System.currentTimeMillis()

            if (endTime > now) {
                val delay = endTime - now

                // Планируем WorkManager
                val workRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "resetGoldWorker",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

                // Обновляем виджет вручную после перезапуска устройства
                GoldWidget.currentGold = goldCount
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids =
                    appWidgetManager.getAppWidgetIds(ComponentName(context, GoldWidget::class.java))
                for (id in ids) {
                    GoldWidget.updateWidget(context, appWidgetManager, id)
                }
            }
        }

    }
}