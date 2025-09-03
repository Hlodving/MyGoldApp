// DbManager.kt
package com.hlodving.mytestgold.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database // убедись, что есть зависимость firebase-database-ktx

class DbManager {
    private val db = Firebase.database.reference

    fun publishAd() {
        // пишем НЕ в корень, а, например, в "/test"
        db.child("test").push().setValue("Hola")
            .addOnSuccessListener { Log.d("DB", "Write OK") }
            .addOnFailureListener { e ->
                Log.e("DB", "Write FAIL: ${e.message}", e)
            }
    }
}
