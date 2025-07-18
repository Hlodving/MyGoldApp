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
    private var frameRunnable: Runnable? = null
    private var delayHandler: Handler? = null

    private var currentFrame = 0
    private var useAura = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        startForeground(1, createNotification()) // всегда первым!

        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        val isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true)
        val timerEndTime = prefs.getLong("timerEndTime", 0L)
        val now = System.currentTimeMillis()

        val isOberegActive = when {
            timerEndTime == Long.MAX_VALUE -> true
            timerEndTime > now -> true
            else -> false
        }

        if (!isAnimationEnabled || !isOberegActive) {
            stopSelf() // но уже после старта
            return
        }

        startAnimationCycle()
    }



    override fun onDestroy() {
        super.onDestroy()
        frameHandler?.removeCallbacksAndMessages(null)
        delayHandler?.removeCallbacksAndMessages(null)
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
            .setContentText("Анимация виджета активно работает")
            .setSmallIcon(R.drawable.gold_cb)
            .build()
    }

    private fun startAnimationCycle() {
        delayHandler = Handler(Looper.getMainLooper())
        delayHandler?.postDelayed({
            playOneAnimation()
        }, 0)
    }

    private fun playOneAnimation() {
        frameHandler = Handler(Looper.getMainLooper())
        currentFrame = 0
        useAura = !useAura
        val totalFrames = if (useAura) 60 else 23

        frameRunnable = object : Runnable {
            override fun run() {
                updateAllWidgets()
                currentFrame++
                if (currentFrame < totalFrames) {
                    frameHandler?.postDelayed(this, 30)
                } else {
                    delayHandler?.postDelayed({
                        playOneAnimation()
                    }, 15000)
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
