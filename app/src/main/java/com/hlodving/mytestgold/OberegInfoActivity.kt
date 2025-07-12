package com.hlodving.mytestgold

import android.content.Intent
import android.os.Bundle
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
            val intent = Intent(this, WidgetInfoActivity::class.java)
            startActivity(intent)
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

        // Обработка кнопки "yourProgressButton"
        binding.yourProgressButton.setOnClickListener {
            val intent = Intent(this, ProgressActivity::class.java)
            startActivity(intent)
        }
    }
}
