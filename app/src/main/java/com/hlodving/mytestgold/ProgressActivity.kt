package com.hlodving.mytestgold


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityProgressBinding

class ProgressActivity : AppCompatActivity() {

    //Переменная с основным счетчиком
    private lateinit var globalTapCounter: GlobalTapCounter


    private lateinit var binding: ActivityProgressBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создаём объект и загружаем данные
        globalTapCounter = GlobalTapCounter(this)
        globalTapCounter.load()

        // Показываем общее количество тапов
        val allTaps = globalTapCounter.totalTaps
        binding.myProgress.text = "Прогресс: $allTaps"



    }
}