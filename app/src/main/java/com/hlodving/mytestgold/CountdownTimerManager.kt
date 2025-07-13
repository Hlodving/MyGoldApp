package com.hlodving.mytestgold

import android.content.Context
import android.os.CountDownTimer
import android.preference.PreferenceManager
//Класс отвечает за таймер

class CountdownTimerManager(
    private val context: Context, //Сохраняет или загружает время окончания таймера
    private val onTick: (String) -> Unit, //Обновляет таймер
    private val onFinished: () -> Unit // Сброс состояния при завершении таймера
) {
    private var countdownTimer: CountDownTimer? = null //Сам таймер
    var timerEndTime: Long = 0L //Время окончания таймера в миллисекундах

    //Запускает таймер
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
            }
        }.start()
    }

    //stopTimer
    fun stopTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    //Возвращает количество миллисекунд до конца таймера
    fun getRemainingTimeMillis(): Long {
        return (timerEndTime - System.currentTimeMillis()).coerceAtLeast(0)
    }

    //Сохраняет таймер
    fun saveTimerEndTime(time: Long) {
        timerEndTime = time
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("timerEndTime", time).apply()
    }

    //Загружает таймер
    fun loadTimerEndTime(): Long {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        timerEndTime = prefs.getLong("timerEndTime", 0L)
        return timerEndTime
    }

    //Преобразует миллисекунды в строки
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}


