package com.hlodving.mytestgold.accounthelper

import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.database.DbManager

class AccountHelper(private val act: MainActivity) {

    // DbManager с коллбэком в MainActivity
    private val dbManager = DbManager(act)

    fun signUpWithEmail(email: String, password: String, alias: String) {
        if (email.isNotEmpty() && password.isNotEmpty() && alias.isNotEmpty()) {
            act.mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user!!
                        sendEmailVerification(user)

                        // ИНИЦИАЛИЗАЦИЯ ДАННЫХ НОВОГО АККА:
                        // 1) прогресс отдельно
                        dbManager.saveProgress(0, 0, 1)
                        // 2) алиас отдельно
                        dbManager.saveAlias(alias)

                        act.uiUpdate(user)
                    } else {
                        val errorMessage = task.exception?.message
                            ?: act.resources.getString(R.string.sign_up_error)
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
                        Toast.makeText(act, act.getString(R.string.sign_in_success), Toast.LENGTH_SHORT).show()
                        act.uiUpdate(task.result?.user)
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

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(act, act.resources.getString(R.string.send_verification_done), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(act, act.resources.getString(R.string.send_verification_error), Toast.LENGTH_LONG).show()
            }
        }
    }
}
