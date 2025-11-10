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
        fun onDataLoaded(totalTaps: Int, bonusProgress: Int, bonusStage: Int, alias: String, faith: String?)
    }


    // Сохраняет данные пользователя в Firebase
    fun saveProgress(totalTaps: Int, bonusProgress: Int, bonusStage: Int) {
        val userId = auth.currentUser?.uid ?: return
        val ref = db.getReference("users").child(userId)
        val map = mapOf(
            "globalTapCounter" to totalTaps,
            "bonusProgress" to bonusProgress,
            "bonusStage" to bonusStage
        )
        ref.updateChildren(map)
    }
    fun saveAlias(alias: String) {
        val userId = auth.currentUser?.uid ?: return
        db.getReference("users").child(userId).child("alias").setValue(alias)
    }

    fun saveFaith(faith: String) {
        val userId = auth.currentUser?.uid ?: return
        db.getReference("users").child(userId).child("faith").setValue(faith)
    }





    // Загружает данные пользователя из Firebase
    fun loadData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalTaps = snapshot.child("globalTapCounter").getValue(Int::class.java) ?: 0
                val bonusProgress = snapshot.child("bonusProgress").getValue(Int::class.java) ?: 0
                val bonusStage = snapshot.child("bonusStage").getValue(Int::class.java) ?: 1


                val aliasFromDb = snapshot.child("alias").getValue(String::class.java)
                val faithFromDb = snapshot.child("faith").getValue(String::class.java)


                val fallbackAlias =
                    auth.currentUser?.displayName?.takeIf { !it.isNullOrBlank() } ?:
                    auth.currentUser?.email?.substringBefore("@")?.takeIf { !it.isNullOrBlank() } ?:
                    "Гость"

                val aliasForUi = aliasFromDb?.takeIf { it.isNotBlank() } ?: fallbackAlias

                // если alias отсутствует или пуст – сохраним нормализованный
                if (aliasFromDb.isNullOrBlank()) {
                    userRef.child("alias").setValue(aliasForUi)
                }

                dbCallback.onDataLoaded(totalTaps, bonusProgress, bonusStage, aliasForUi, faithFromDb)
            }

            override fun onCancelled(error: DatabaseError) { /* TODO: лог */ }
        })
    }

}