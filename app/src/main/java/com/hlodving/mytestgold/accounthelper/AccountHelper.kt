package com.hlodving.mytestgold.accounthelper

import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.database.DbManager

// Класс для работы с аккаунтом: регистрация, вход, отправка письма подтверждения
class AccountHelper(private val act: MainActivity) {

    // Обёртка над Realtime Database для сохранения/чтения пользовательских данных
    private val dbManager = DbManager(act)

        // Регистрация пользователя по email и паролю + первичная инициализация данных в БД
        fun signUpWithEmail(email: String, password: String, alias: String) {
            if (email.isNotEmpty() && password.isNotEmpty() && alias.isNotEmpty()) {
                act.mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user!!

                            // 1. Отправляем письмо для верификации
                            sendEmailVerification(user)

                            // 2. Обновляем displayName в профиле Firebase (хорошая практика)
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(alias)
                                .build()
                            user.updateProfile(profileUpdates)

                            // 3. Сохраняем начальный прогресс и псевдоним
                            dbManager.saveProgress(0, 0, 1)
                            dbManager.saveAlias(alias)

                            // 4. ДОБАВИТЬ: Сохраняем флаг, что почта НЕ подтверждена
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(user.uid)
                                .child("emailVerified")
                                .setValue(false)

                            // 5. ИЗМЕНИТЬ: Обновляем UI, чтобы пользователь сразу вошел в аккаунт
                            act.uiUpdate(user)

                            // 6. ИЗМЕНИТЬ: Показываем более дружелюбное сообщение
                            Toast.makeText(
                                act,
                                "Регистрация успешна! Для защиты аккаунта подтвердите почту.",
                                Toast.LENGTH_LONG
                            ).show()

                        } else {
                            val errorMessage = task.exception?.message
                                ?: act.resources.getString(R.string.sign_up_error)
                            Toast.makeText(act, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }


     //Вход пользователя по email и паролю.
     fun signInWithEmail(email: String, password: String) {
         if (email.isNotEmpty() && password.isNotEmpty()) {
             act.mAuth.signInWithEmailAndPassword(email, password)
                 .addOnCompleteListener { task ->
                     if (task.isSuccessful) {
                         val user = task.result?.user
                         if (user != null) {
                             // Пользователь успешно вошел, обновляем UI
                             Toast.makeText(act, act.getString(R.string.sign_in_success), Toast.LENGTH_SHORT).show()
                             act.uiUpdate(user)

                             // Если почта не подтверждена, просто покажем дополнительное напоминание
                             if (!user.isEmailVerified) {
                                 Toast.makeText(
                                     act,
                                     "Пожалуйста, подтвердите вашу почту, чтобы защитить аккаунт.",
                                     Toast.LENGTH_LONG
                                 ).show()
                             }
                         }
                     } else {
                         // Ошибка входа (неверный пароль и т.д.)
                         Toast.makeText(
                             act,
                             act.resources.getString(R.string.sign_in_error),
                             Toast.LENGTH_LONG
                         ).show()
                     }
                 }
         }
     }


     //Отправка письма подтверждения e-mail текущему пользователю.
    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.send_verification_done),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.send_verification_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
