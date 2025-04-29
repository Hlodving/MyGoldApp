package com.hlodving.mytestgold

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews

class GoldWidget : AppWidgetProvider() {

    companion object {
        var currentGold = 0

        private const val TOTAL_FRAMES = 13
        private var frameHandler: Handler? = null
        private var frameRunnable: Runnable? = null
        private var delayHandler: Handler? = null

        private var currentFrame = 0

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_gold)

            if (currentGold == 0) {
                val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
                currentGold = prefs.getInt("goldCount", 0)
            }

            val stage = Stage.fromGold(currentGold)

            if (stage.number == 1) {
                // Фаза 1 — статичная картинка
                views.setImageViewResource(R.id.widgetImage, R.drawable.gold_cb)
            } else {
                // Фаза больше 1 — показываем кадры shine_frame
                val frameName = "shine_frame_${currentFrame % TOTAL_FRAMES}"
                val resId = context.resources.getIdentifier(frameName, "drawable", context.packageName)
                if (resId != 0) {
                    views.setImageViewResource(R.id.widgetImage, resId)
                }
            }

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

        private fun playOneAnimation(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, GoldWidget::class.java)
            )

            frameHandler = Handler(Looper.getMainLooper())
            currentFrame = 0

            frameRunnable = object : Runnable {
                override fun run() {
                    for (id in appWidgetIds) {
                        updateWidget(context, appWidgetManager, id)
                    }
                    currentFrame++

                    if (currentFrame < TOTAL_FRAMES) {
                        frameHandler?.postDelayed(this, 30) // 30 мс между кадрами
                    } else {
                        // После завершения всех кадров — старт ожидания снова
                        startDelay(context)
                    }
                }
            }

            frameHandler?.post(frameRunnable!!)
        }

        private fun startDelay(context: Context) {
            delayHandler = Handler(Looper.getMainLooper())
            delayHandler?.postDelayed({
                playOneAnimation(context)
            }, 15_000L) // 15 секунд ожидания
        }

        fun startAnimationCycle(context: Context) {
            if (delayHandler == null && frameHandler == null) {
                // Первая активация
                startDelay(context)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
        startAnimationCycle(context)
    }
}
