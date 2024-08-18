package com.example.navigation

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.map
import com.google.firebase.storage.FirebaseStorage


class DottoreViewModel : ViewModel() {

    private val _nome = MutableLiveData<String>()
    val nome: LiveData<String> get() = _nome

    private val _luogo = MutableLiveData<String>()
    val luogo: LiveData<String> get() = _luogo

    private val _numero = MutableLiveData<String>()
    val numero: LiveData<String> get() = _numero

    private val _foto = MutableLiveData<String>()
    val foto: LiveData<String> get() = _foto

    private val _orari = MutableLiveData<Map<String, Map<String, String>>>()
    val orari: LiveData<Map<String, Map<String, String>>> get() = _orari

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var imageUri: Uri? = null
    fun datiDottore() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("dottore").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data ?: return@addOnSuccessListener

                    _nome.value = data["nome"] as? String
                    _luogo.value = data["luogo"] as? String
                    _numero.value = data["numero"] as? String
                    _foto.value = data["foto"] as? String
                    _orari.value = data["orari"] as? Map<String, Map<String, String>>
                }
            }
            .addOnFailureListener { e ->
            }
    }

    fun updateDottore(uid:String,nome:String,luogo:String,numero:String,imageUrl:String?, orari: Map<String, MutableMap<String, String>>){
            val updates = hashMapOf(
                "id_user" to uid,
                "nome" to nome,
                "luogo" to luogo,
                "numero" to numero,
                "orari" to orari,
                "foto" to imageUrl
            )
            db.collection("dottore").document(uid)
                .update(updates as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("ViewModel", "Documento aggiornato con successo")
                }
                .addOnFailureListener { e ->
                    Log.e("ViewModel", "Errore durante l'aggiornamento del documento", e)
                }

    }

    fun getOrarioPerGiorno(giorno: String, chiave: String): LiveData<String> {
        return orari.map { map ->
            map[giorno]?.get(chiave) ?: ""
        }
    }

    fun verificaOra(ora: String): String {
        return if (ora == "Fine" || ora == "Inizio") {
            "--:--"
        } else {
            ora
        }
    }

    private fun uploadImageAndGetUrl(uid: String, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("doctor_photo/${uid}.jpg")

        imageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        callback(downloadUri.toString())
                    }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Errore nel recuperare l'URL dell'immagine", e)
                            callback(null)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Errore nel caricare l'immagine", e)
                    callback(null)
                }
        } ?: callback(null) // Chiamata del callback anche se non c'è un URI
    }

    val lunediMattinaInizio = getOrarioPerGiorno("Lunedì", "MattinaInizio")
    val lunediMattinaFine = getOrarioPerGiorno("Lunedì", "MattinaFine")
    val lunediPomeriggioInizio = getOrarioPerGiorno("Lunedì", "PomeriggioInizio")
    val lunediPomeriggioFine = getOrarioPerGiorno("Lunedì", "PomeriggioFine")

    // Ripeti per gli altri giorni della settimana...
    val martediMattinaInizio = getOrarioPerGiorno("Martedì", "MattinaInizio")
    val martediMattinaFine = getOrarioPerGiorno("Martedì", "MattinaFine")
    val martediPomeriggioInizio = getOrarioPerGiorno("Martedì", "PomeriggioInizio")
    val martediPomeriggioFine = getOrarioPerGiorno("Martedì", "PomeriggioFine")

    val mercolediMattinaInizio = getOrarioPerGiorno("Mercoledì", "MattinaInizio")
    val mercolediMattinaFine = getOrarioPerGiorno("Mercoledì", "MattinaFine")
    val mercolediPomeriggioInizio = getOrarioPerGiorno("Mercoledì", "PomeriggioInizio")
    val mercolediPomeriggioFine = getOrarioPerGiorno("Mercoledì", "PomeriggioFine")

    val giovediMattinaInizio = getOrarioPerGiorno("Giovedì", "MattinaInizio")
    val giovediMattinaFine = getOrarioPerGiorno("Giovedì", "MattinaFine")
    val giovediPomeriggioInizio = getOrarioPerGiorno("Giovedì", "PomeriggioInizio")
    val giovediPomeriggioFine = getOrarioPerGiorno("Giovedì", "PomeriggioFine")

    val venerdiMattinaInizio = getOrarioPerGiorno("Venerdì", "MattinaInizio")
    val venerdiMattinaFine = getOrarioPerGiorno("Venerdì", "MattinaFine")
    val venerdiPomeriggioInizio = getOrarioPerGiorno("Venerdì", "PomeriggioInizio")
    val venerdiPomeriggioFine = getOrarioPerGiorno("Venerdì", "PomeriggioFine")

    val sabatoMattinaInizio = getOrarioPerGiorno("Sabato", "MattinaInizio")
    val sabatoMattinaFine = getOrarioPerGiorno("Sabato", "MattinaFine")
    val sabatoPomeriggioInizio = getOrarioPerGiorno("Sabato", "PomeriggioInizio")
    val sabatoPomeriggioFine = getOrarioPerGiorno("Sabato", "PomeriggioFine")

    val domenicaMattinaInizio = getOrarioPerGiorno("Domenica", "MattinaInizio")
    val domenicaMattinaFine = getOrarioPerGiorno("Domenica", "MattinaFine")
    val domenicaPomeriggioInizio = getOrarioPerGiorno("Domenica", "PomeriggioInizio")
    val domenicaPomeriggioFine = getOrarioPerGiorno("Domenica", "PomeriggioFine")

}
