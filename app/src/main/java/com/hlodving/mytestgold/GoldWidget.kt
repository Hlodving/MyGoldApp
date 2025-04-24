package com.hlodving.mytestgold

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class GoldWidget : AppWidgetProvider() {

    companion object {
        var currentGold = 0

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_gold)

            // Загружаем goldCount из SharedPreferences, если currentGold == 0
            if (currentGold == 0) {
                val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
                currentGold = prefs.getInt("goldCount", 0)
            }

            val stage = Stage.fromGold(currentGold)
            val imageRes = if (stage.number > 1) R.drawable.gold else R.drawable.gold_cb
            views.setImageViewResource(R.id.widgetImage, imageRes)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widgetImage, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // 👇 Вынеси метод onUpdate из companion object — сюда!
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            updateWidget(context, manager, id)
        }
    }
}
