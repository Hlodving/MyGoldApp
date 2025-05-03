package com.hlodving.mytestgold
// Активити в котором отображается информация об обереге
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityOberegInfoBinding

class OberegInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOberegInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOberegInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Пульсация текста "Сила оберега"
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        binding.oberegTitle.startAnimation(pulseAnimation)
    }
}
