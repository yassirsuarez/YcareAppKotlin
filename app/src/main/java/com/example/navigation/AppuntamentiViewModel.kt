package com.example.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.content.ContentValues.TAG
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class AppuntamentiViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _appuntamentiData = MutableLiveData<Map<String, String?>>()
    val appuntamentiData: LiveData<Map<String, String?>> get() = _appuntamentiData
    private lateinit var auth: FirebaseAuth
    private val _appuntamentiList = MutableLiveData<List<AppuntamentiData>>()
    val appuntamentiList: LiveData<List<AppuntamentiData>> get() = _appuntamentiList
    private val _numeroAppuntamentiOggi = MutableLiveData<Int>()
    val numeroAppuntamentiOggi: LiveData<Int> get() = _numeroAppuntamentiOggi
    fun inserisciDatiAppuntamenti(uid:String,titolo:String,luogo:String,data:String,ora:String) {
        val db = Firebase.firestore
        val appuntamenti = hashMapOf(
            "id_user" to uid,
            "titolo" to titolo,
            "luogo" to luogo,
            "data" to data,
            "ora" to ora)

        db.collection("appuntamenti")
            .add(appuntamenti)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id
                // Ora aggiorna il documento con il nuovo ID
                documentReference.update("id", documentId)
                    .addOnSuccessListener {
                        Log.w(TAG, "Documento aggiunto e aggiornato con ID: $documentId")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Errore durante l'aggiornamento del documento", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Errore durante l'aggiunta del documento", e)
            }
    }

    fun datiAppuntamentiLista(id: String?) {
        id?.let { userId ->
            db.collection("appuntamenti")
                .whereEqualTo("id_user", userId)
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return@addSnapshotListener
                    }

                    val userArrayList = mutableListOf<AppuntamentiData>()
                    snapshot?.documentChanges?.forEach { dc ->
                        if (dc.type == DocumentChange.Type.ADDED) {
                            userArrayList.add(dc.document.toObject(AppuntamentiData::class.java))
                        }
                    }

                    _appuntamentiList.value = userArrayList
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun numeroAppuntamentiOggi(id: String?) {
        Log.d("User ID", "User ID: $id")
        if (id == null) {
            _numeroAppuntamentiOggi.value = 0
            return
        }

        val oggi = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dataOggiFormattata = oggi.format(formatter)

        db.collection("appuntamenti")
            .whereEqualTo("id_user", id)
            .whereEqualTo("data", dataOggiFormattata)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore error", error.message.toString())
                    _numeroAppuntamentiOggi.value = 0
                    return@addSnapshotListener
                }

                var count = 0
                snapshot?.documents?.forEach { document ->
                    val appuntamento = document.toObject(AppuntamentiData::class.java)
                    try {
                        if (appuntamento?.data == dataOggiFormattata) {
                            count++
                        }
                    } catch (e: DateTimeParseException) {
                        Log.e("DateParseError", "Error parsing date: ${appuntamento?.data}", e)
                    }
                }
                _numeroAppuntamentiOggi.value = count
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun datiAppuntamentiListaFragment(id: String?) {
        id?.let { userId ->
            db.collection("appuntamenti")
                .whereEqualTo("id_user", userId)
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return@addSnapshotListener
                    }
                    val userArrayList = mutableListOf<AppuntamentiData>()
                    val oggi = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    snapshot?.documentChanges?.forEach { dc ->
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val appuntamento = dc.document.toObject(AppuntamentiData::class.java)
                            try {
                                val dataAppuntamento = LocalDate.parse(appuntamento.data, formatter)
                                if (dataAppuntamento.isAfter(oggi) || dataAppuntamento == oggi) {
                                    userArrayList.add(appuntamento)
                                }
                            } catch (e: DateTimeParseException) {
                                Log.e("DateParseError", "Error parsing date: ${appuntamento.data}", e)
                            }
                        }
                    }
                    _appuntamentiList.value = userArrayList
                }
        }
    }


    fun deleteAppuntamento(id_appuntamento: String?) {
        if (id_appuntamento == null) {
            Log.e("ViewModel", "Invalid appointment ID")
            return
        }
        val currentList = _appuntamentiList.value?.toMutableList() ?: mutableListOf()
        db.collection("appuntamenti").document(id_appuntamento)
            .delete()
            .addOnSuccessListener {
                val iterator = currentList.iterator()
                while (iterator.hasNext()) {
                    val appuntamento = iterator.next()
                    if (appuntamento.id == id_appuntamento) {
                        iterator.remove()
                        break
                    }
                }
                _appuntamentiList.value = currentList
                Log.d("ViewModel", "Appointment deleted and list updated")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore error", "Error deleting appointment", e)
            }
    }

    fun UpdateAppuntamento(uid:String?,titolo:String?,luogo:String?,data:String?,ora:String?, id:String?){
        if(id!=null){
        val appuntamenti = hashMapOf(
            "id_user" to uid,
            "titolo" to titolo,
            "luogo" to luogo,
            "data" to data,
            "ora" to ora)
            val appuntamentiMap = appuntamenti as Map<String, Any>
        db.collection("appuntamenti").document(id)
            .update(appuntamentiMap)
            .addOnSuccessListener {
                Log.d("ViewModel", "Documento aggiornato con successo")
            }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Errore durante l'aggiornamento del documento", e)
            }
    }}

}