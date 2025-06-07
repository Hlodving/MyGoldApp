package com.hlodving.mytestgold

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityWidgetInfoBinding

class WidgetInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWidgetInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.widgetInstructions.text = getString(R.string.widget_instructions)

    }
}
