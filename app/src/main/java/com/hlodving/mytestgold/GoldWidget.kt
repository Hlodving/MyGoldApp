package com.hlodving.mytestgold
//Этот класс это виджет приложения


import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

class GoldWidget : AppWidgetProvider() {

    companion object {
        private var currentGold = 0 //Текущее колличество кликов

        private var useAuraAnimation = false //Переключает анимацию между aura и shine

        private var frameHandler: Handler? = null //используются для проигрывания анимации по кадрам
        private var frameRunnable: Runnable? = null //используются для проигрывания анимации по кадрам
        private var delayHandler: Handler? = null //делает паузу между циклами анимации

        private var currentFrame = 0 //номер текущего кадра анимации (в пределах 0–22 или 0–59)


        fun updateAllWidgets(context: Context, goldCount: Int) {
            currentGold = goldCount

            val intent = Intent(context, GoldWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, GoldWidget::class.java))
                )
            }

            context.sendBroadcast(intent)
        }


        //Эта функция обнавляет внешний вид виджета
        fun updateWidgetAnimation(
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
        //Анимация виджета
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
                        updateWidgetAnimation(context, appWidgetManager, id)
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
        // время между анимациями
        private fun startDelay(context: Context) {
            delayHandler = Handler(Looper.getMainLooper())
            delayHandler?.postDelayed({
                playOneAnimation(context)
            }, 15_000L)
        }


        //Запуск анимации
        fun startAnimationCycle(context: Context) {
            if (delayHandler == null && frameHandler == null) {
                startDelay(context)
            }
        }
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }


    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidgetAnimation(context, appWidgetManager, id)
        }

        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        val isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true)

        if (isAnimationEnabled && !isServiceRunning(context, WidgetAnimationService::class.java)) {
            val serviceIntent = Intent(context, WidgetAnimationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        startAnimationCycle(context)
    }

    override fun onDisabled(context: Context) {
        // Этот метод вызывается, когда удалён последний экземпляр виджета с экрана
        context.stopService(Intent(context, WidgetAnimationService::class.java))
    }

}
