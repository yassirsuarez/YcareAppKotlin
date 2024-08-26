package com.example.navigation

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

open class DailyFirebaseUpdateWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val userId = getCurrentUserId()
        val appointmentsRef = db.collection("medicine")

        return try {
            val querySnapshot = appointmentsRef.whereEqualTo("user_id", userId).get().await()

            for (document in querySnapshot.documents) {
                val orari = document.get("orari") as? Map<String, Map<String, Any>>

                val updatedOrari = orari?.mapValues { entry ->
                    val orarioDetails = entry.value.toMutableMap()
                    orarioDetails["stato"] = false
                    orarioDetails
                }

                document.reference.update("orari", updatedOrari)
                    .addOnSuccessListener {
                        Log.d("DailyFirebaseUpdateWorker", "Document0 ${document.id} caricato con successo.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DailyFirebaseUpdateWorker", "Errore documento ${document.id}: ${e.message}")
                    }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyFirebaseUpdateWorker", "Errore caricamento: ", e)
            Result.retry()
        }
    }

    private fun getCurrentUserId(): String {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid ?: "unknown_user"
    }
}
