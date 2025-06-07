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

        // Обработка кнопки "ВАЖНО"
        binding.widgetHelpButton.setOnClickListener {
            val intent = Intent(this, WidgetInfoActivity::class.java)
            startActivity(intent)
        }
    }
}
