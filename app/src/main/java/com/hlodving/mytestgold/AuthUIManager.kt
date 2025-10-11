package com.hlodving.mytestgold

import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.hlodving.mytestgold.database.DbManager

//Отвечает за кнопки и меню

class AuthUIManager(
    private val activity: MainActivity,
    private val navView: NavigationView,
    private val dbManager: DbManager
) {
    private val tvAccount: TextView = navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)

    fun updateUiForUser(user: FirebaseUser?) {
        val menu = navView.menu
        if (user == null) {
            activity.userDataReady = false
            tvAccount.text = activity.resources.getString(R.string.not_reg)
            menu.findItem(R.id.menu_sign_in).isVisible = true
            menu.findItem(R.id.menu_sign_up).isVisible = true
            menu.findItem(R.id.menu_sign_out).isVisible = false

            // Делегируем сброс MainActivity
            activity.performLocalDataClear()
        } else {
            menu.findItem(R.id.menu_sign_in).isVisible = false
            menu.findItem(R.id.menu_sign_up).isVisible = false
            menu.findItem(R.id.menu_sign_out).isVisible = true

            // Загружаем данные из Firebase
            dbManager.loadData()
        }
    }
}