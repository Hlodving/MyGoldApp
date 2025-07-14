package com.hlodving.mytestgold

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

//Запускает фоновую задачу и обнуляет прогресс

object ResetScheduler {

    fun scheduleResetWorker(context: Context, delayMillis: Long) {
        val workRequest = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "resetGoldWorker",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
