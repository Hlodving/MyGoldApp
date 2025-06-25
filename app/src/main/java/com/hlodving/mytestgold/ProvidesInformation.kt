package com.hlodving.mytestgold

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityProvidesInformationBinding

class ProvidesInformation : AppCompatActivity() {

    private lateinit var binding: ActivityProvidesInformationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProvidesInformationBinding
            .inflate(layoutInflater)
        setContentView(binding.root)
    }
}