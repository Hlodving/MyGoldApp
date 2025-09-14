package com.hlodving.mytestgold.dialoghelper

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.accounthelper.AccountHelper
import com.hlodving.mytestgold.databinding.SignDialogBinding

class DialogHelper(private val act: MainActivity) {
    private val accHelper = AccountHelper(act)

    // Переменная для отслеживания состояния диалога
    private var isResetPasswordState = false

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)
        builder.setView(rootDialogElement.root)

        isResetPasswordState = false
        setDialogState(index, rootDialogElement)

        val dialog = builder.create()
        rootDialogElement.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, rootDialogElement, dialog)
        }

        rootDialogElement.btForgetP.setOnClickListener {
            if (isResetPasswordState) {
                // Если уже в режиме восстановления, выполняем сброс пароля
                setOnClickResetPassword(rootDialogElement, dialog)
            } else {
                // Если нет, переключаем UI в режим восстановления
                setResetPasswordState(rootDialogElement)
            }
        }

        dialog.show()
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if (index == DialogConst.SING_UP_STATE) {
            rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.menu_sign_up)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
            rootDialogElement.edSignAlias.visibility = View.VISIBLE // Показываем поле псевдонима
            rootDialogElement.btForgetP.visibility = View.GONE // Скрываем для регистрации
        } else {
            rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.menu_sign_in)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            rootDialogElement.btForgetP.visibility = View.VISIBLE
            rootDialogElement.edSignAlias.visibility = View.GONE // Скрываем поле псевдонима
            rootDialogElement.edSignPassword.visibility = View.VISIBLE
            rootDialogElement.btSignUpIn.visibility = View.VISIBLE
            rootDialogElement.btForgetP.text = act.resources.getString(R.string.forget_password)
            rootDialogElement.tvDialogMessage.visibility = View.GONE
        }
    }

    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog) {
        if (index == DialogConst.SING_UP_STATE) {
            val email = rootDialogElement.edSignEmail.text.toString()
            val password = rootDialogElement.edSignPassword.text.toString()
            val alias = rootDialogElement.edSignAlias.text.toString()

            // Добавлена проверка на пустые поля email и password
            if (email.isNotEmpty() && password.isNotEmpty() && alias.isNotEmpty()) {
                accHelper.signUpWithEmail(email, password, alias)
                dialog.dismiss()
            } else {
                Toast.makeText(act, "Пожалуйста, заполните все поля", Toast.LENGTH_LONG).show()
            }
        } else {
            accHelper.signInWithEmail(
                rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString()
            )
            dialog.dismiss()
        }
    }

    // Переключаем UI в режим восстановления пароля
    private fun setResetPasswordState(rootDialogElement: SignDialogBinding) {
        isResetPasswordState = true
        rootDialogElement.edSignPassword.visibility = View.GONE // Скрываем поле пароля
        rootDialogElement.btSignUpIn.visibility = View.GONE // Скрываем кнопку "Войти"
        // Меняем текст на кнопке "Забыли пароль?"
        rootDialogElement.btForgetP.text = act.resources.getString(R.string.restore_password)
        // Использование строкового ресурса вместо хардкода
        rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.reset_password_title)
        rootDialogElement.tvDialogMessage.visibility = View.VISIBLE
    }

    // Обновлённый метод для сброса пароля
    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding, dialog: AlertDialog) {
        if (rootDialogElement.edSignEmail.text.isNotEmpty()) {
            act.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(act, "Ошибка: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                    dialog.dismiss()
                }
        } else {
            Toast.makeText(act, "Пожалуйста, введите ваш email", Toast.LENGTH_LONG).show()
        }
    }
}