package com.hlodving.mytestgold

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.RemoteViews

class WidgetAnimationService : Service() {

    private var frameHandler: Handler? = null
    private var delayHandler: Handler? = null
    private var frameRunnable: Runnable? = null

    private var currentFrame = 0
    private var useAura = false

    private var isAnimationRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification()) // Без этого Android 13+ убьёт сервис

        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        val isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true)
        val timerEndTime = prefs.getLong("timerEndTime", 0L)
        val now = System.currentTimeMillis()

        val isOberegActive = timerEndTime == Long.MAX_VALUE || timerEndTime > now

        if (!isAnimationEnabled || !isOberegActive) {
            stopSelf()
            return
        }

        startAnimationCycle()
    }

    override fun onDestroy() {
        super.onDestroy()
        frameHandler?.removeCallbacksAndMessages(null)
        delayHandler?.removeCallbacksAndMessages(null)
        isAnimationRunning = false
    }

    private fun createNotification(): Notification {
        val channelId = "widget_anim_channel"
        val channelName = "Widget Animation"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Анимация оберега")
            .setContentText("Виджет-анимация активна")
            .setSmallIcon(R.drawable.gold_cb)
            .build()
    }

    private fun startAnimationCycle() {
        if (isAnimationRunning) return
        isAnimationRunning = true
        currentFrame = 0
        useAura = !useAura

        val totalFrames = if (useAura) 60 else 23
        val frameDuration = 30L // миллисекунд на один кадр

        frameHandler = Handler(Looper.getMainLooper())
        delayHandler = Handler(Looper.getMainLooper())

        frameRunnable = object : Runnable {
            override fun run() {
                updateAllWidgets()

                currentFrame++
                if (currentFrame < totalFrames) {
                    frameHandler?.postDelayed(this, frameDuration)
                } else {
                    // Завершение анимации → пауза 15 сек → снова цикл
                    isAnimationRunning = false
                    delayHandler?.postDelayed({
                        startAnimationCycle()
                    }, 15000L)
                }
            }
        }

        frameHandler?.post(frameRunnable!!)
    }

    private fun updateAllWidgets() {
        val context = applicationContext
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, GoldWidget::class.java))

        val frameName = if (useAura) {
            "aura_%02d".format(currentFrame % 60 + 1)
        } else {
            "shine_%02d".format(currentFrame % 23)
        }

        val resId = context.resources.getIdentifier(frameName, "drawable", context.packageName)

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_gold)
            views.setImageViewResource(R.id.widgetImage, resId)
            manager.updateAppWidget(id, views)
        }
    }
}
