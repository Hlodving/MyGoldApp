package com.hlodving.mytestgold

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityOberegInfoBinding

class OberegInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOberegInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOberegInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Пульсация заголовка
        val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.oberegTitle.startAnimation(pulseAnimation)

        // Обработка кнопки "widgetHelpButton"
        binding.widgetHelpButton.setOnClickListener {
            val appWidgetManager = getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(this, GoldWidget::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                // Создаем интент, который просто подтверждает запрос (можно добавить PendingIntent, если нужно отследить успех)
                val successCallback: PendingIntent? = null

                // Вызываем системное диалоговое окно добавления виджета
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            } else {
                // Если лаунчер не поддерживает быстрое добавление (редкий случай для совр. Android)
                Toast.makeText(this, R.string.error_obereg_widget, Toast.LENGTH_SHORT).show()
            }
        }

        // Обработка кнопки "providesInformationButton"
        binding.providesInformationButton.setOnClickListener {
            val intent = Intent(this, ProvidesInformation::class.java)
            startActivity(intent)
        }

        // Обработка кнопки "connectionButton"
        binding.connectionButton.setOnClickListener {
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)
        }


    }
}
