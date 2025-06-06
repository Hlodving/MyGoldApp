import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class HeartAnimation {

    private var animationIndex = 0

    fun animationRestart(lottieFile: LottieAnimationView) {
        lottieFile.repeatCount = 0
        lottieFile.repeatMode = LottieDrawable.RESTART
    }

    fun playLottieAnimation(
        lottieView: LottieAnimationView,
        minProgress: Float = 0f,
        maxProgress: Float = 1f
    ) {
        lottieView.setMinAndMaxProgress(minProgress, maxProgress)
        lottieView.playAnimation()
    }

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


