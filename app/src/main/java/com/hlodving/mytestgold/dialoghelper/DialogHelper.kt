package com.hlodving.mytestgold.dialoghelper

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.accounthelper.AccountHelper
import com.hlodving.mytestgold.databinding.SignDialogBinding

class DialogHelper(act: MainActivity) {
    private val act = act
    private val accHelper = AccountHelper(act)
    fun createSignDialog(index:Int){
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)
        builder.setView(rootDialogElement.root)

        setDialogState(index, rootDialogElement)

        val dialog = builder.create()
        rootDialogElement.btSignUpIn.setOnClickListener {
            setOnClickSignUpIn(index, rootDialogElement, dialog)
        }

        rootDialogElement.btForgetP.setOnClickListener {
            setOnClickResetPassword(rootDialogElement, dialog)
        }

        dialog.show()
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) {
        if (index == DialogConst.SING_UP_STATE) {
            rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.menu_sign_up)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
            rootDialogElement.edSignAlias.visibility = View.VISIBLE // Показываем поле псевдонима
        } else {
            rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.menu_sign_in)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
            rootDialogElement.btForgetP.visibility = View.VISIBLE
            rootDialogElement.edSignAlias.visibility = View.GONE // Скрываем поле псевдонима
        }
    }

    private fun setOnClickSignUpIn(index: Int,rootDialogElement: SignDialogBinding, dialog: AlertDialog){
        dialog.dismiss()
        if(index == DialogConst.SING_UP_STATE){
            val email = rootDialogElement.edSignEmail.text.toString()
            val password = rootDialogElement.edSignPassword.text.toString()
            val alias = rootDialogElement.edSignAlias.text.toString() // Получаем псевдоним

            if (alias.isNotEmpty()) {
                // Передаём псевдоним в AccountHelper
                accHelper.signUpWithEmail(email, password, alias)
            } else {
                Toast.makeText(act, "Пожалуйста, введите псевдоним", Toast.LENGTH_LONG).show()
            }
        } else {
            accHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString())
        }
    }

    private fun setOnClickResetPassword (rootDialogElement: SignDialogBinding, dialog: AlertDialog){
        if(rootDialogElement.edSignEmail.text.isNotEmpty()){
            act.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
                }
            }
            dialog?.dismiss()
        } else {
            rootDialogElement.tvDialogMessage.visibility = View.VISIBLE
        }
    }


}

