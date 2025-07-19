package com.hlodving.mytestgold

import android.content.Context
import android.os.CountDownTimer

// Класс отвечает за работу таймера: запуск, сброс, сохранение, форматирование и определение состояния
class CountdownTimerManager(
    private val context: Context, // Для сохранения/загрузки таймера
    private val onTick: (String) -> Unit, // Обновляет UI времени
    private val onFinished: () -> Unit, // Выполняется при завершении таймера
    private val onStopped: () -> Unit
) {

    var baseHoursToAdd: Long = 2L // Начальная прибавка — 2 часа
    var resetHappened: Boolean = false // Флаг сброса
    private var countdownTimer: CountDownTimer? = null // Сам таймер
    var timerEndTime: Long = 0L // Время окончания в миллисекундах

    // Состояние таймера
    sealed class TimerState {
        object Forever : TimerState()
        data class Running(val remainingMillis: Long) : TimerState()
        object Expired : TimerState()
    }

    // Получить текущее состояние таймера
    fun getTimerState(): TimerState {
        val now = System.currentTimeMillis()
        return when {
            timerEndTime == Long.MAX_VALUE -> TimerState.Forever
            timerEndTime > now -> TimerState.Running(timerEndTime - now)
            else -> TimerState.Expired
        }
    }

    // Запустить таймер
    fun startTimer() {
        stopTimer()
        val remainingTime = getRemainingTimeMillis()
        if (remainingTime <= 0) {
            onFinished()
            return
        }

        countdownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTick(formatTime(millisUntilFinished))
            }

            override fun onFinish() {
                onFinished()
                onStopped() // ← Вызов остановки
            }
        }.start()
    }

    // Сброс таймера
    fun resetTimer() {
        timerEndTime = 0L
        saveTimerEndTime(timerEndTime)
    }

    // Остановить таймер
    fun stopTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    // Проверка вечного таймера
    fun isOberegForever(): Boolean {
        return loadTimerEndTime() == Long.MAX_VALUE
    }

    // Получить оставшееся время
    fun getRemainingTimeMillis(): Long {
        return (timerEndTime - System.currentTimeMillis()).coerceAtLeast(0)
    }

    // Сохранить время окончания
    fun saveTimerEndTime(time: Long) {
        timerEndTime = time
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("timerEndTime", time).apply()
    }

    // Загрузить время окончания
    fun loadTimerEndTime(): Long {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        timerEndTime = prefs.getLong("timerEndTime", 0L)
        return timerEndTime
    }

    // Форматировать время (часы:минуты:секунды)
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
