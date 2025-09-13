package com.hlodving.mytestgold.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DbManager(private val dbCallback: DatabaseCallback) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Интерфейс для обработки коллбэков данных
    interface DatabaseCallback {
        fun onDataLoaded(totalTaps: Int, bonusProgress: Int, bonusStage: Int, alias: String)
    }


    // Сохраняет данные пользователя в Firebase
    fun saveData(totalTaps: Int, bonusProgress: Int, bonusStage: Int, alias: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.getReference("users").child(userId)

        val userData = mapOf(
            "globalTapCounter" to totalTaps,
            "bonusProgress" to bonusProgress,
            "bonusStage" to bonusStage,
            "alias" to alias
        )
        userRef.setValue(userData)
    }

    // Загружает данные пользователя из Firebase
    fun loadData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val totalTaps = snapshot.child("globalTapCounter").getValue(Int::class.java) ?: 0
                    val bonusProgress = snapshot.child("bonusProgress").getValue(Int::class.java) ?: 0
                    val bonusStage = snapshot.child("bonusStage").getValue(Int::class.java) ?: 1
                    val alias = snapshot.child("alias").getValue(String::class.java) ?: auth.currentUser?.email ?: "Псевдоним" // Получаем псевдоним

                    dbCallback.onDataLoaded(totalTaps, bonusProgress, bonusStage, alias)
                } else {
                    // Если данных нет (пользователь только что зарегистрировался),
                    // передаем дефолтные значения и email как псевдоним по умолчанию
                    val alias = auth.currentUser?.email ?: "Псевдоним"
                    dbCallback.onDataLoaded(0, 0, 1, alias)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Здесь можно добавить обработку ошибок
            }
        })
    }
}