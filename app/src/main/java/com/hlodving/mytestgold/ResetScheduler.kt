import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hlodving.mytestgold.TimerWorker
import com.hlodving.mytestgold.WidgetAnimationService
import java.util.concurrent.TimeUnit

object ResetScheduler {

    fun stopWidgetAnimationService(context: Context) {
        val intent = Intent(context, WidgetAnimationService::class.java)
        context.stopService(intent)
    }

    fun startWidgetAnimationService(context: Context) {
        val intent = Intent(context, WidgetAnimationService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Запуск запрещён системой — пропускаем
        }
    }

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