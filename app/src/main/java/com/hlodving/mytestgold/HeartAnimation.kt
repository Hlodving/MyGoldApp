package com.hlodving.mytestgold

import android.content.Context
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class HeartAnimation {


    fun animationRestart (lottieFile:LottieAnimationView) {
        lottieFile.repeatCount = 0
        lottieFile.repeatMode = LottieDrawable.RESTART
    }


    fun playLottieAnimation(lottieView: LottieAnimationView, minProgress: Float = 0f, maxProgress: Float = 1f) {
        lottieView.setMinAndMaxProgress(minProgress, maxProgress)
        lottieView.playAnimation()
    }




}



