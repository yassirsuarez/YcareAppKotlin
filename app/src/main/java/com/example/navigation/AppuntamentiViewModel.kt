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
import android.content.Context
import android.widget.Toast

class AppuntamentiViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _appuntamentiData = MutableLiveData<Map<String, String?>>()
    val appuntamentiData: LiveData<Map<String, String?>> get() = _appuntamentiData
    private lateinit var auth: FirebaseAuth
    private val _appuntamentiList = MutableLiveData<List<AppuntamentiData>>()
    val appuntamentiList: LiveData<List<AppuntamentiData>> get() = _appuntamentiList
    private val _numeroAppuntamentiOggi = MutableLiveData<Int>()
    val numeroAppuntamentiOggi: LiveData<Int> get() = _numeroAppuntamentiOggi
    private val _risultato = MutableLiveData<Boolean>()
    val risultato: LiveData<Boolean> get() = _risultato

    fun inserisciDatiAppuntamenti(uid:String,titolo:String,luogo:String,data:String,ora:String,context:Context) {
        val db = Firebase.firestore
        if (titolo.isNullOrEmpty() ||
            luogo.isNullOrEmpty() ||
            data.isNullOrEmpty() ||
            ora.isNullOrEmpty()) {
            Toast.makeText(context, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
            _risultato.postValue(false)
            return
        }
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
                documentReference.update("id", documentId)
                    .addOnSuccessListener {
                        Log.w(TAG, "Documento aggiunto e aggiornato con ID: $documentId")
                        _risultato.postValue(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Errore durante l'aggiornamento del documento", e)

                        _risultato.postValue(false)  }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Errore durante l'aggiunta del documento", e)
                _risultato.postValue(false)  }
    }

    fun datiAppuntamentiLista(id: String?) {
        id?.let { userId ->
            db.collection("appuntamenti")
                .whereEqualTo("id_user", userId)
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore errore", error.message.toString())
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
                        Log.e("DateParseError", "Errore dato: ${appuntamento?.data}", e)
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

    fun UpdateAppuntamento(uid:String?,titolo:String?,luogo:String?,data:String?,ora:String?, id:String?,context:Context){
        if (titolo.isNullOrEmpty() ||
            luogo.isNullOrEmpty() ||
            data.isNullOrEmpty() ||
            ora.isNullOrEmpty()) {
            Toast.makeText(context, "Completa tutti i campi", Toast.LENGTH_SHORT).show()
            _risultato.postValue(false)
            return
        }
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
                _risultato.postValue(true)  }
            .addOnFailureListener { e ->
                Log.e("ViewModel", "Errore durante l'aggiornamento del documento", e)
                _risultato.postValue(false)  }
    }}

}