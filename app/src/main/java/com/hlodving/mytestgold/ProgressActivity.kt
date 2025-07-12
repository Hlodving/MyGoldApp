package com.hlodving.mytestgold


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityProgressBinding

class ProgressActivity : AppCompatActivity() {


    private lateinit var binding: ActivityProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Загружаем прогресс из SharedPreferences
        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        val yourProgress = prefs.getInt("bonusProgress", 0)

        binding.myProgress.text = "Прогресс: $yourProgress"

    }
}