package com.hlodving.mytestgold

import android.content.Context
import android.os.CountDownTimer
import android.preference.PreferenceManager

class CountdownTimerManager(
    private val context: Context,
    private val onTick: (String) -> Unit,
    private val onFinished: () -> Unit
) {
    private var countdownTimer: CountDownTimer? = null
    var timerEndTime: Long = 0L

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

    fun stopTimer() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    fun getRemainingTimeMillis(): Long {
        return (timerEndTime - System.currentTimeMillis()).coerceAtLeast(0)
    }

    fun saveTimerEndTime(time: Long) {
        timerEndTime = time
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("timerEndTime", time).apply()
    }

    fun loadTimerEndTime(): Long {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        timerEndTime = prefs.getLong("timerEndTime", 0L)
        return timerEndTime
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}


