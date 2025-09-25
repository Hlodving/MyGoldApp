package com.hlodving.mytestgold

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityConnectionBinding

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // навешиваем копирование по тапу
        binding.oberegEmail.copyOnTap()
        binding.oberegDonat.copyOnTap()
    }

    private fun TextView.copyOnTap() {
        // гарантируем, что режим выделения не перехватывает клик
        setTextIsSelectable(false)
        isLongClickable = false
        isClickable = true
        isFocusable = true

        setOnClickListener {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("text", text))
            Toast.makeText(context, R.string.copie, Toast.LENGTH_SHORT).show()
        }
    }
}
