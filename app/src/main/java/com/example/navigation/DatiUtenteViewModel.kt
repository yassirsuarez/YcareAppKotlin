package com.example.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import android.util.Log
import android.content.ContentValues.TAG
import com.google.firebase.firestore.DocumentChange

class DatiUtenteViewModel : ViewModel(){
    private val db = FirebaseFirestore.getInstance()
    private val _userData = MutableLiveData<Map<String, String?>>()
    val userData: LiveData<Map<String, String?>> get() = _userData

    private val _peso = MutableLiveData<String?>()
    val peso: LiveData<String?> get() = _peso

    private val _altezza = MutableLiveData<String?>()
    val altezza: LiveData<String?> get() = _altezza

    private val _data = MutableLiveData<String?>()
    val data: LiveData<String?> get() = _data

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> get() = _userList

    fun fetchDatiSalute(uid: String) {
        db.collection("dati_salute")
            .whereEqualTo("id", uid)
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    _peso.value = document.getString("peso")
                    _altezza.value = document.getString("altezza")
                    _data.value = document.getString("data")
                } else {
                    Log.d(TAG, "Nessun documento trovato")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Errore durante la query: ", exception)
            }
    }

    fun fetchUserData(uid: String) {
        db.collection("users")
            .whereEqualTo("id", uid)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val document = result.documents[0]
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                    val nomecognome = (document.getString("nome") ?: "") + " " + (document.getString("cognome") ?: "")
                    val compleanno = document.getString("data_nascita")
                    val foto=document.getString("foto")
                    val sesso = if (document.getBoolean("sesso") == true) {
                        "Maschio"
                    } else {
                        "Femmina"
                    }

                    val userDataMap = mapOf(
                        "nome_cognome" to nomecognome,
                        "data_nascita" to compleanno,
                        "sesso" to sesso,
                        "foto" to foto

                    )

                    _userData.value = userDataMap
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
    fun datiSaluteLista(id: String?) {
        id?.let { userId ->
            db.collection("dati_salute")
                .whereEqualTo("id", userId)
                .orderBy("data", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return@addSnapshotListener
                    }

                    val userArrayList = mutableListOf<User>()
                    snapshot?.documentChanges?.forEach { dc ->
                        if (dc.type == DocumentChange.Type.ADDED) {
                            userArrayList.add(dc.document.toObject(User::class.java))
                        }
                    }

                    _userList.value = userArrayList
                }
        }
    }
    fun deleteDatoSalute(user: User, userId: String?) {
        if (user.data != null && userId != null) {
            db.collection("dati_salute")
                .whereEqualTo("data", user.data)
                .whereEqualTo("id", userId)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val currentList = _userList.value?.toMutableList() ?: mutableListOf()
                        db.collection("dati_salute").document(document.id)
                            .delete()
                            .addOnSuccessListener {
                                currentList.remove(user)
                                _userList.value = currentList
                       Log.d("ViewModel", "User deleted and list updated")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore error", "Error deleting user", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore error", "Error finding user", e)
                }
        }
    }

}

