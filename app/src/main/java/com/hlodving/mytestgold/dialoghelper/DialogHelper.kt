package com.hlodving.mytestgold.dialoghelper

import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.accounthelper.AccountHelper
import com.hlodving.mytestgold.databinding.SignDialogBinding

class DialogHelper(private val act: MainActivity) {
    private val accHelper = AccountHelper(act)
    private var isResetPasswordState = false

    fun createSignDialog(index: Int) {
        val builder = AlertDialog.Builder(act)
        val root = SignDialogBinding.inflate(act.layoutInflater)
        builder.setView(root.root)

        isResetPasswordState = false
        setDialogState(index, root)

        val dialog = builder.create()

        if (index == DialogConst.SING_UP_STATE) {
            // 1. Делаем кнопку "Зарегистрироваться" неактивной по умолчанию.
            root.btSignUpIn.isEnabled = false

            // 2. Активируем ссылки в TextView, чтобы по ним можно было кликать.
            root.tvTerms.movementMethod = LinkMovementMethod.getInstance()

            // 3. Устанавливаем слушатель на чекбокс.
            root.cbTerms.setOnCheckedChangeListener { _, isChecked ->
                // Кнопка становится активной только тогда, когда поставлена галочка.
                root.btSignUpIn.isEnabled = isChecked
            }
        }


        root.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, root, dialog)
        }

        root.btForgetP.setOnClickListener {
            if (isResetPasswordState) {
                setOnClickResetPassword(root, dialog)
            } else {
                setResetPasswordState(root)
            }
        }

        dialog.show()
    }

    private fun setDialogState(index: Int, root: SignDialogBinding) {
        if (index == DialogConst.SING_UP_STATE) {
            // Регистрация
            root.tvSingTitle.text = act.getString(R.string.menu_sign_up)
            root.btSignUpIn.text = act.getString(R.string.sign_up_action)

            root.edSignAlias.visibility = View.VISIBLE
            root.tilSignPassword.visibility = View.VISIBLE
            root.tilSignPasswordConfirm.visibility = View.VISIBLE

            root.btForgetP.visibility = View.GONE
            root.btSignUpIn.visibility = View.VISIBLE
            root.tvDialogMessage.visibility = View.GONE

            root.cbTerms.visibility = View.VISIBLE
            root.tvTerms.visibility = View.VISIBLE
        } else {
            // Вход
            root.tvSingTitle.text = act.getString(R.string.menu_sign_in)
            root.btSignUpIn.text = act.getString(R.string.sign_in_action)

            root.edSignAlias.visibility = View.GONE
            root.tilSignPasswordConfirm.visibility = View.GONE
            root.tilSignPassword.visibility = View.VISIBLE

            root.btForgetP.visibility = View.VISIBLE
            root.btForgetP.text = act.getString(R.string.forget_password)
            root.btSignUpIn.visibility = View.VISIBLE
            root.tvDialogMessage.visibility = View.GONE

            root.cbTerms.visibility = View.GONE
            root.tvTerms.visibility = View.GONE
        }
    }

    private fun setOnClickSignUpIn(index: Int, root: SignDialogBinding, dialog: AlertDialog) {
        if (index == DialogConst.SING_UP_STATE) {
            val email = root.edSignEmail.text.toString()
            val password = root.edSignPassword.text.toString()
            val passwordConfirm = root.edSignPasswordConfirm.text.toString()
            val alias = root.edSignAlias.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty() && alias.isNotEmpty()) {
                if (password == passwordConfirm) {
                    accHelper.signUpWithEmail(email, password, alias, "christian")
                    dialog.dismiss()
                } else {
                    Toast.makeText(act, "Пароли не совпадают. Пожалуйста, проверьте введённые данные.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(act, "Пожалуйста, заполните все поля", Toast.LENGTH_LONG).show()
            }
        } else {
            val email = root.edSignEmail.text.toString()
            val password = root.edSignPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                accHelper.signInWithEmail(email, password)
                dialog.dismiss()
            } else {
                Toast.makeText(act, "Введите email и пароль", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setResetPasswordState(root: SignDialogBinding) {
        isResetPasswordState = true
        // Прячем поля пароля целиком
        root.tilSignPassword.visibility = View.GONE
        root.tilSignPasswordConfirm.visibility = View.GONE
        root.btSignUpIn.visibility = View.GONE

        root.btForgetP.text = act.getString(R.string.restore_password)
        root.tvSingTitle.text = act.getString(R.string.reset_password_title)
        root.tvDialogMessage.visibility = View.VISIBLE
    }

    private fun setOnClickResetPassword(root: SignDialogBinding, dialog: AlertDialog) {
        val email = root.edSignEmail.text.toString()
        if (email.isNotEmpty()) {
            act.mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
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
