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

        private var useAuraAnimation = false // Перенесено в companion object

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
                views.setImageViewResource(R.id.widgetImage, R.drawable.gold_cb)
            } else {
                val frameName = if (useAuraAnimation) {
                    val frameNum = String.format("%02d", (currentFrame % 60) + 1)


                    "aura_$frameNum"
                } else {
                    val frameNum = String.format("%02d", currentFrame % 23)
                    "shine_$frameNum"
                }

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

            // Переключаемся на другую анимацию
            useAuraAnimation = !useAuraAnimation

            val totalFrames = if (useAuraAnimation) 60 else 23



            frameRunnable = object : Runnable {
                override fun run() {
                    for (id in appWidgetIds) {
                        updateWidget(context, appWidgetManager, id)
                    }
                    currentFrame++

                    if (currentFrame < totalFrames) {
                        frameHandler?.postDelayed(this, 30)
                    } else {
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
            }, 15_000L)
        }

        fun startAnimationCycle(context: Context) {
            if (delayHandler == null && frameHandler == null) {
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
