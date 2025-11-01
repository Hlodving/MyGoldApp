package com.hlodving.mytestgold

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hlodving.mytestgold.databinding.ActivityConnectionBinding

class ConnectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ИСПРАВЛЕННАЯ СТРОКА
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Навешиваем копирование по тапу для email и доната
        binding.oberegEmail.copyOnTap()
        binding.oberegDonat.copyOnTap()

        // Навешиваем открытие ссылок для правовых документов
        setupLegalLinks()
    }

    private fun TextView.copyOnTap() {
        // Гарантируем, что режим выделения не перехватывает клик
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

    private fun setupLegalLinks() {
        binding.privacyPolicyLink.setOnClickListener {
            openUrl("https://docs.google.com/document/d/e/2PACX-1vT9wPvDVPMprRoKxomQOu7l-mYYi40l0WvpCyQLy9nmTqf4KfXj3h2EJCT5N3QXL1NmLotm2J50ATxO/pub")
        }

        binding.termsOfUseLink.setOnClickListener {
            openUrl("https://docs.google.com/document/d/e/2PACX-1vQEQWLfTFtkXB6UF-NOpJZZVMDJmN9B9vYOvS_AWbOF-MI6F3RPr2lQ3iTH9aCcqIllA59ejFHLmtiu/pub")
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }
}