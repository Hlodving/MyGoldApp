package com.hlodving.mytestgold

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.hlodving.mytestgold.databinding.ActivityMainBinding

//UI Приложения

class MainViewUpdater(
    private val context: Context,
    private val binding: ActivityMainBinding,
    private val progressColors: List<Int>
) {

    fun updateGoldProgress(progress: Int, max: Int, stageNumber: Int) {
        binding.progressBar.max = max
        binding.progressBar.progress = progress.coerceAtMost(max)
        binding.progressText.text = "$progress / $max"
        val colorDrawableId = progressColors[(stageNumber - 1) % progressColors.size]
        binding.progressBar.progressDrawable = ContextCompat.getDrawable(context, colorDrawableId)
    }

    fun updateBonusProgress(progress: Int, max: Int, stageNumber: Int) {
        binding.progressBar2.max = max
        binding.progressBar2.progress = progress
        binding.progressText2.text = "$progress / $max"
        val colorDrawableId = progressColors[(stageNumber - 1) % progressColors.size]
        binding.progressBar2.progressDrawable = ContextCompat.getDrawable(context, colorDrawableId)
    }

    fun updateTotalTaps(taps: Int) {
        val formattedTaps = java.text.NumberFormat.getInstance().format(taps)
        binding.totalTapsText.text = "Ваш счет: $formattedTaps"
    }

    fun updateTimer(formattedTime: String) {
        binding.timerText.text = formattedTime
    }

    fun showAmuletForever() {
        binding.timerText.text = context.getString(R.string.active_forever_amulet)
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.GONE
        binding.progressBar2.visibility = View.GONE
        binding.progressText2.visibility = View.GONE
    }

    fun showAmuletNotActive() {
        binding.timerText.text = context.getString(R.string.not_active_amulet)
    }

    fun updateBonusQuote(quote: String) {
        binding.bonusQuoteText.text = quote
    }

    fun playShineAnimation() {
        HeartAnimation.playLottieAnimation(binding.lottieViewShineOne)
        HeartAnimation.playLottieAnimation(binding.lottieViewShineTwo)
    }

    fun updateTapAnimation(isAmuletActive: Boolean) {
        HeartAnimation.updateTapAnimation(binding.lottieTapGold, isAmuletActive)
    }
}