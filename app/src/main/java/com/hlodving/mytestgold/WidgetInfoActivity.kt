package com.hlodving.mytestgold

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.hlodving.mytestgold.databinding.ActivityWidgetInfoBinding

class WidgetInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetInfoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Загрузка и отображение GIF
        Glide.with(this)
            .asGif()
            .load(R.drawable.my_animation)   // ваш GIF в res/drawable
            .into(binding.gifImageView)
    }
}
