package com.hlodving.mytestgold

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityConnectionBinding

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}