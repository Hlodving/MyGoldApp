package com.hlodving.mytestgold
//Класс предназначенный для работы с lottie анимацией

import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class HeartAnimation {


    fun animationRestart (lottieFile:LottieAnimationView) {
        lottieFile.repeatCount = 0 //Проигрывает анимацию один раз
        lottieFile.repeatMode = LottieDrawable.RESTART //Устанавливает проигрывание анимации с начала
    }

    //Функция для проигрыша анимации в ней можно задать конкретный отрезок
    fun playLottieAnimation(lottieView: LottieAnimationView, minProgress: Float = 0f, maxProgress: Float = 1f) {
        lottieView.setMinAndMaxProgress(minProgress, maxProgress)
        lottieView.playAnimation()
    }




}



