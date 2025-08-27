package com.hlodving.mytestgold.dialoghelper

import androidx.appcompat.app.AlertDialog
import com.hlodving.mytestgold.MainActivity
import com.hlodving.mytestgold.R
import com.hlodving.mytestgold.databinding.SignDialogBinding

class DialogHelper(act: MainActivity) {
    private val act = act
    fun createSignDialog(index:Int){
        val builder = AlertDialog.Builder(act)
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater)
        if (index == DialogConst.SING_UP_STATE){
            rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.menu_sign_up)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_up_action)
        } else {
            rootDialogElement.tvSingTitle.text = act.resources.getString(R.string.menu_sign_in)
            rootDialogElement.btSignUpIn.text = act.resources.getString(R.string.sign_in_action)
        }
        builder.setView(rootDialogElement.root)
        builder.show()
    }
}