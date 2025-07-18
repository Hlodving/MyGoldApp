package com.hlodving.mytestgold

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hlodving.mytestgold.databinding.ActivityWidgetInfoBinding

class WidgetInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetInfoBinding

    private fun updateToggleButtonText(isEnabled: Boolean) {
        binding.toggleAnimationButton.text = if (isEnabled) {
            "Отключить анимацию"
        } else {
            "Включить анимацию"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val prefs = getSharedPreferences("GoldPrefs", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("widgetAnimationEnabled", true)

        updateToggleButtonText(isEnabled)

        binding.toggleAnimationButton.setOnClickListener {
            val newState = !prefs.getBoolean("widgetAnimationEnabled", true)
            prefs.edit().putBoolean("widgetAnimationEnabled", newState).apply()
            updateToggleButtonText(newState)

            if (newState) {
                // Включаем сервис
                startForegroundService(Intent(this, WidgetAnimationService::class.java))
            } else {
                // Останавливаем сервис
                stopService(Intent(this, WidgetAnimationService::class.java))
            }
        }



        // Загрузка и отображение GIF
        Glide.with(this)
            .asGif()
            .load(R.drawable.my_animation)   // ваш GIF в res/drawable
            .into(binding.gifImageView)
    }
}
