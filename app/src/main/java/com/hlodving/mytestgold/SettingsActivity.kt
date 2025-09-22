package com.hlodving.mytestgold

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    // 1. Создаем переменную для хранения текущего состояния
    private var isAnimationEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Загружаем сохраненное состояние ОДИН РАЗ при создании Activity
        val prefs = getSharedPreferences("GoldPrefs", MODE_PRIVATE)
        isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true) // true - значение по умолчанию

        // 3. Устанавливаем начальный текст кнопки на основе загруженного состояния
        updateToggleButtonText(isAnimationEnabled)

        // 4. Устанавливаем обработчик нажатия
        binding.toggleAnimationButton.setOnClickListener {
            // 5. Инвертируем (меняем) состояние в нашей переменной
            isAnimationEnabled = !isAnimationEnabled

            // 6. Сохраняем новое состояние в SharedPreferences
            prefs.edit().putBoolean("widgetAnimationEnabled", isAnimationEnabled).apply()

            // 7. Обновляем текст кнопки на основе НОВОГО состояния
            updateToggleButtonText(isAnimationEnabled)

            // 8. Запускаем или останавливаем сервис на основе НОВОГО состояния
            if (isAnimationEnabled) {
                // Включаем сервис
                startForegroundService(Intent(this, WidgetAnimationService::class.java))
            } else {
                // Останавливаем сервис
                stopService(Intent(this, WidgetAnimationService::class.java))
            }
        }
    }

    private fun updateToggleButtonText(isEnabled: Boolean) {
        binding.toggleAnimationButton.text = if (isEnabled) {
            getString(R.string.animation_off)
        } else {
            getString(R.string.animation_on)
        }
    }
}