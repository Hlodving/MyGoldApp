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
        fun onDataLoaded(totalTaps: Int, bonusProgress: Int, bonusStage: Int)
    }

    // Сохраняет данные пользователя в Firebase
    fun saveData(totalTaps: Int, bonusProgress: Int, bonusStage: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.getReference("users").child(userId)

        val userData = mapOf(
            "globalTapCounter" to totalTaps,
            "bonusProgress" to bonusProgress,
            "bonusStage" to bonusStage
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

                    dbCallback.onDataLoaded(totalTaps, bonusProgress, bonusStage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Здесь можно добавить обработку ошибок
            }
        })
    }
}