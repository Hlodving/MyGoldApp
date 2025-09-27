package com.hlodving.mytestgold.accounthelper

import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
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
                            sendEmailVerification(user)
                            dbManager.saveProgress(0, 0, 1)
                            dbManager.saveAlias(alias)

                            Toast.makeText(
                                act,
                                R.string.send_verification_done,
                                Toast.LENGTH_LONG
                            ).show()

                            act.mAuth.signOut()


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
                         // Пользователь успешно ввел логин/пароль, теперь проверим почту
                         val user = task.result?.user
                         if (user != null && user.isEmailVerified) {
                             // Почта подтверждена, всё в порядке
                             Toast.makeText(act, act.getString(R.string.sign_in_success), Toast.LENGTH_SHORT).show()
                             act.uiUpdate(user)
                         } else {
                             Toast.makeText(
                                 act,
                                 R.string.check_email,
                                 Toast.LENGTH_LONG
                             ).show()

                             act.mAuth.signOut()
                         }
                     } else {
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
