    import com.airbnb.lottie.LottieAnimationView
    import com.airbnb.lottie.LottieDrawable

    //Анимация в приложении
    class HeartAnimation {

        //Чередует анимации
        private var animationIndex = 0

        //Запуск анимации 1 раз с сначал до конца
        fun animationRestart(lottieFile: LottieAnimationView) {
            lottieFile.repeatCount = 0
            lottieFile.repeatMode = LottieDrawable.RESTART
        }

        //Проигрывает анимацию в заданном диапазоне
        fun playLottieAnimation(
            lottieView: LottieAnimationView,
            minProgress: Float = 0f,
            maxProgress: Float = 1f
        ) {
            lottieView.setMinAndMaxProgress(minProgress, maxProgress)
            lottieView.playAnimation()
        }

        //Чередует 3 анимации
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


