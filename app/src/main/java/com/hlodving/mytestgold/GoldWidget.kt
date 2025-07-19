package com.hlodving.mytestgold

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

// Класс GoldWidget — это AppWidgetProvider, управляющий отображением виджета на рабочем столе
class GoldWidget : AppWidgetProvider() {

    companion object {
        // Переменная для хранения текущего количества золота
        private var currentGold = 0

        // Обновляет все виджеты (например, при изменении количества золота)
        fun updateAllWidgets(context: Context, goldCount: Int) {
            currentGold = goldCount // сохраняем новое значение

            // Создаём broadсast-интент для обновления всех экземпляров виджета
            val intent = Intent(context, GoldWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, GoldWidget::class.java))
                )
            }

            // Отправляем broadcast, чтобы вызвать onUpdate()
            context.sendBroadcast(intent)
        }
    }

    // Метод вызывается системой при необходимости обновления виджета (например, раз в несколько часов)
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Обновляем каждый экземпляр виджета
        for (id in appWidgetIds) {
            updateStaticWidget(context, appWidgetManager, id)
        }

        // Получаем настройки пользователя (включена ли анимация виджета)
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        val isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true)

        // Если включена анимация и сервис ещё не запущен — запускаем его
        if (isAnimationEnabled && !isServiceRunning(context, WidgetAnimationService::class.java)) {
            val serviceIntent = Intent(context, WidgetAnimationService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    // Обновляет конкретный виджет: показывает статику (например, gold_cb), если анимация выключена
    private fun updateStaticWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Загружаем макет виджета
        val views = RemoteViews(context.packageName, R.layout.widget_gold)

        // Если текущий счётчик нулевой, пробуем загрузить его из памяти
        if (currentGold == 0) {
            val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
            currentGold = prefs.getInt("goldCount", 0)
        }

        // Определяем текущую стадию по количеству золота
        val stage = Stage.fromGold(currentGold)

        // Если стадия первая (0 золота) — показываем статичное изображение gold_cb
        if (stage.number == 1) {
            views.setImageViewResource(R.id.widgetImage, R.drawable.gold_cb)
        }

        // Настраиваем поведение при клике по виджету — откроется MainActivity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widgetImage, pendingIntent)

        // Обновляем виджет
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    // Вызывается системой, когда последний экземпляр виджета удалён с экрана
    override fun onDisabled(context: Context) {
        // Останавливаем фоновую анимацию, если она ещё работает
        context.stopService(Intent(context, WidgetAnimationService::class.java))
    }

    // Проверяет, запущен ли уже фоновый сервис анимации
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}
