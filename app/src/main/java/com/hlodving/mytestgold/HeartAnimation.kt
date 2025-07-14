import android.content.Context
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.hlodving.mytestgold.GoldWidget
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.Stage
import com.hlodving.mytestgold.databinding.ActivityMainBinding

//Анимация в приложении
object HeartAnimation {

    private var currentTapAnimation: String? = null // Текущий файл анимации для нажатий
    private var animationIndex = 0 // Чередует анимации

    // Меняет анимацию активную и не активную
    fun updateTapAnimationForStage(lottieView: LottieAnimationView, stage: Stage) {
        val newAnimation = if (stage.number == 1) "Gold_movement_cb.json" else "Gold_movement.json"
        if (currentTapAnimation != newAnimation) {
            currentTapAnimation = newAnimation
            lottieView.setAnimation(newAnimation)
        }
    }

    // обновляет UI после сброса
    fun applyResetUI(
        context: Context,
        binding: ActivityMainBinding,
        progressColors: List<Int>,
        countFirstProgress: Int,
        secondStageNumber: Int
    ) {
        binding.timerText.text = context.getString(R.string.not_active_amulet)

        val (stageProgress, stageMax, currentStage) = Stage.getStageData(countFirstProgress)
        updateTapAnimationForStage(binding.lottieTapGold, currentStage)

        binding.progressBar.max = stageMax
        binding.progressBar.progress = stageProgress
        binding.progressText.text = "$stageProgress / $stageMax"

        val colorDrawableId2 = progressColors[(secondStageNumber - 1) % progressColors.size]
        binding.progressBar2.progressDrawable = ContextCompat.getDrawable(context, colorDrawableId2)

        val safeStageNum = currentStage.number.coerceIn(1, 101)
        val colorDrawableId = progressColors[(safeStageNum - 1) % progressColors.size]
        binding.progressBar.progressDrawable = ContextCompat.getDrawable(context, colorDrawableId)

        GoldWidget.updateAllWidgets(context, countFirstProgress)
    }

    // Запуск анимации 1 раз с начала до конца
    fun animationRestart(lottieFile: LottieAnimationView) {
        lottieFile.repeatCount = 0
        lottieFile.repeatMode = LottieDrawable.RESTART
    }

    // Проигрывает анимацию в заданном диапазоне
    fun playLottieAnimation(
        lottieView: LottieAnimationView,
        minProgress: Float = 0f,
        maxProgress: Float = 1f
    ) {
        lottieView.setMinAndMaxProgress(minProgress, maxProgress)
        lottieView.playAnimation()
    }

    // Чередует 3 анимации
    fun playNextHeartAnimation(
        lottieView: LottieAnimationView,
        minProgressOne: Float,
        minProgressTwo: Float,
        minProgressThree: Float,
        maxProgressOne: Float,
        maxProgressTwo: Float,
        maxProgressThree: Float
    ) {
        when (animationIndex % 3) {
            0 -> playLottieAnimation(lottieView, minProgressOne, maxProgressOne)
            1 -> playLottieAnimation(lottieView, minProgressTwo, maxProgressTwo)
            2 -> playLottieAnimation(lottieView, minProgressThree, maxProgressThree)
        }
        animationIndex++
    }
}
