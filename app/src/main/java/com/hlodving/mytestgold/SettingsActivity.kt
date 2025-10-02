package com.hlodving.mytestgold

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import com.hlodving.mytestgold.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var isAnimationEnabled = true

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()


        val prefs = getSharedPreferences("GoldPrefs", MODE_PRIVATE)
        isAnimationEnabled = prefs.getBoolean("widgetAnimationEnabled", true)
        updateToggleButtonText(isAnimationEnabled)


        binding.toggleAnimationButton.setOnClickListener {
            isAnimationEnabled = !isAnimationEnabled
            prefs.edit().putBoolean("widgetAnimationEnabled", isAnimationEnabled).apply()
            updateToggleButtonText(isAnimationEnabled)
            if (isAnimationEnabled) {
                startForegroundService(Intent(this, WidgetAnimationService::class.java))
            } else {
                stopService(Intent(this, WidgetAnimationService::class.java))
            }
        }

        // Обработчик для кнопки удаления аккаунта
        binding.deleteAccountButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun updateToggleButtonText(isEnabled: Boolean) {
        binding.toggleAnimationButton.text = if (isEnabled) {
            getString(R.string.animation_off)
        } else {
            getString(R.string.animation_on)
        }
    }

    // Показываем первый диалог подтверждения
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_account)
            .setMessage(R.string.warning_delete_account)
            .setPositiveButton(R.string.delete) { _, _ ->
                // Вместо немедленного удаления, запрашиваем пароль
                showPasswordPromptDialog()
            }
            .setNegativeButton(R.string.cancellation, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    // Показываем диалог для ввода пароля
    private fun showPasswordPromptDialog() {
        val user = auth.currentUser ?: return

        val view = layoutInflater.inflate(R.layout.dialog_reauth_password, null)
        val til = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilReauthPassword)
        val et = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edReauthPassword)

        val dialog = MaterialAlertDialogBuilder(this)
            .setMessage(R.string.enter_password_delete)
            .setView(view)
            .setPositiveButton(R.string.confirmation, null) // перехватим, чтобы не закрывать диалог при пустом вводе
            .setNegativeButton(R.string.cancellation, null)
            .create()

        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)

        dialog.setOnShowListener {
            val okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okBtn.setOnClickListener {
                val password = et.text?.toString()?.trim().orEmpty()
                if (password.isEmpty()) {
                    til.error = getString(R.string.empty_password)
                    return@setOnClickListener
                }
                til.error = null
                reauthenticateAndThenDelete(password)
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    //  Повторная аутентификация и удаление
    private fun reauthenticateAndThenDelete(password: String) {
        val user = auth.currentUser
        val userEmail = user?.email

        if (user == null || userEmail == null) {
            Toast.makeText(this, R.string.could_not_determine_user, Toast.LENGTH_SHORT).show()
            return
        }

        // Создаем "учетные данные" из email и пароля пользователя
        val credential = EmailAuthProvider.getCredential(userEmail, password)

        // Выполняем повторную аутентификацию
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                // Пароль верный! Теперь можно безопасно удалить аккаунт.
                performFinalDeletion()
            } else {
                // Пароль неверный или другая ошибка
                Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_LONG).show()
            }
        }
    }

    //  Непосредственно удаление (после всех проверок)
    private fun performFinalDeletion() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        val userDbRef = db.getReference("users").child(userId)

        // 1. СНАЧАЛА удаляем данные пользователя из Realtime Database
        userDbRef.removeValue().addOnCompleteListener { dbTask ->
            if (dbTask.isSuccessful) {
                // 2. И только ПОСЛЕ успешного удаления данных, удаляем аккаунт из Authentication
                user.delete().addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_LONG).show()

                        // 3. Перенаправляем пользователя на главный экран
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        // Эта ошибка маловероятна, но ее стоит обработать
                        // Аккаунт не удалился, но данные уже стерты. Нужно сообщить пользователю.
                        Toast.makeText(this, "Данные были удалены, но произошла ошибка при удалении аккаунта: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Ошибка на первом шаге - данные не удалились.
                Toast.makeText(this, "Не удалось удалить данные пользователя: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}