
package com.hlodving.mytestgold.accounthelper

import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.database.DbManager

class AccountHelper(private val act: MainActivity) {

    private val dbManager = DbManager(act)

    fun signUpWithEmail(email: String, password: String, alias: String, faithType: String) {
        if (email.isNotEmpty() && password.isNotEmpty() && alias.isNotEmpty()) {
            act.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user!!

                        sendEmailVerification(user)

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(alias)
                            .build()
                        user.updateProfile(profileUpdates)

                        dbManager.saveProgress(0, 0, 1)
                        dbManager.saveAlias(alias)
                        dbManager.saveFaith(faithType)

                        FirebaseDatabase.getInstance().getReference("users")
                            .child(user.uid)
                            .child("emailVerified")
                            .setValue(false)


                        Toast.makeText(
                            act,
                            "Регистрация успешна! Для защиты аккаунта подтвердите почту.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        val errorMessage = when {
                            task.exception?.message?.contains("password is invalid") == true ||
                                    task.exception?.message?.contains("at least 6") == true ->
                                "Пароль слишком короткий. Минимум 6 символов."

                            task.exception?.message?.contains("email address is badly formatted") == true ->
                                "Неверный формат email. Проверьте правильность написания."

                            task.exception?.message?.contains("email address is already in use") == true ->
                                "Этот email уже зарегистрирован. Попробуйте войти."

                            task.exception?.message?.contains("network error") == true ->
                                "Нет подключения к интернету. Проверьте соединение."

                            task.exception?.message?.contains("too many requests") == true ->
                                "Слишком много попыток. Подождите немного и попробуйте снова."

                            else -> task.exception?.message ?: "Ошибка регистрации. Попробуйте ещё раз."
                        }
                        Toast.makeText(act, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            Toast.makeText(act, act.getString(R.string.sign_in_success), Toast.LENGTH_SHORT).show()



                            if (!user.isEmailVerified) {
                                Toast.makeText(
                                    act,
                                    "Пожалуйста, подтвердите вашу почту, чтобы защитить аккаунт.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        val errorMessage = when {
                            task.exception?.message?.contains("password is invalid") == true ||
                                    task.exception?.message?.contains("wrong-password") == true ->
                                "Неверный пароль. Попробуйте ещё раз."

                            task.exception?.message?.contains("no user record") == true ||
                                    task.exception?.message?.contains("user-not-found") == true ->
                                "Пользователь с таким email не найден."

                            task.exception?.message?.contains("email address is badly formatted") == true ->
                                "Неверный формат email."

                            task.exception?.message?.contains("network error") == true ->
                                "Нет подключения к интернету."

                            task.exception?.message?.contains("too many requests") == true ->
                                "Аккаунт временно заблокирован из-за множества неудачных попыток. Попробуйте позже."

                            task.exception?.message?.contains("user disabled") == true ->
                                "Этот аккаунт отключён. Обратитесь в поддержку."

                            else -> "Ошибка входа. Проверьте данные и попробуйте снова."
                        }
                        Toast.makeText(act, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

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