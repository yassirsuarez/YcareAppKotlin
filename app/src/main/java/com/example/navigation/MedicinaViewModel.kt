package com.example.navigation

import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import android.widget.Toast
import com.google.firebase.firestore.DocumentChange
import android.content.Context

class MedicinaViewModel : ViewModel() {
    val pulsantiOra = MutableLiveData<List<String>>()
    val voltePerGiorno = MutableLiveData<String>()
    val dataInizio = MutableLiveData<String>()
    val nomeMedicina = MutableLiveData<String>()
    val numeroMedicine = MutableLiveData<String>()
    private val _orari = MutableLiveData<Map<String, Orario>>()
    val orari: LiveData<Map<String, Orario>> get() = _orari
    private val _medicineList = MutableLiveData<List<MedicinaData>>()
    val medicineList: LiveData<List<MedicinaData>> get() = _medicineList
    private val _expandedMedicineList = MutableLiveData<List<ExpandedMedicineItem>>()
    val expandedMedicineList: LiveData<List<ExpandedMedicineItem>> get() = _expandedMedicineList

    private val _medicineX = MutableLiveData<MedicinaData?>()
    val medicineX: LiveData<MedicinaData?> get() = _medicineX
    private val _orariX = MutableLiveData<Map<String, MedicinaData.Orario>>()
    val orariX: LiveData<Map<String, MedicinaData.Orario>> get() = _orariX

    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    init {
        voltePerGiorno.observeForever { volte ->
            updateTimesPerDay(volte)
        }
    }
    fun datiMedicineList() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let { userid ->
            db.collection("medicine")
                .whereEqualTo("user_id", userid.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return@addSnapshotListener
                    }

                    val medicineArrayList = mutableListOf<MedicinaData>()
                    snapshot?.documentChanges?.forEach { dc ->
                        if (dc.type == DocumentChange.Type.ADDED) {
                            try {
                                val medicine = dc.document.toObject(MedicinaData::class.java)
                                if (medicine.numero_medicine.toInt() > 0) {
                                    medicineArrayList.add(medicine)
                                }
                            } catch (e: Exception) {
                                Log.e("Firestore error", "Could not deserialize medicine data", e)
                            }
                        }
                    }
                    _medicineList.value = medicineArrayList
                    val expandedList=prepareExpandedData(medicineArrayList)
                    _expandedMedicineList.value=expandedList
               }
        }
    }

    fun updatePresa(id_medicina: String, orario: String, nuovoStato: Boolean = true) {
        val docRef = db.collection("medicine").document(id_medicina)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val orariMap = snapshot.get("orari") as? MutableMap<String, MutableMap<String, Any>>
            if (orariMap != null) {
                val orarioEntry = orariMap.entries.find { it.value["orario"] == orario }
                if (orarioEntry != null) {
                    orarioEntry.value["stato"] = nuovoStato
                }
                transaction.update(docRef, "orari", orariMap)
            } else {
                val newOrariMap = mutableMapOf<String, MutableMap<String, Any>>()
                newOrariMap["1"] = mutableMapOf("orario" to orario, "stato" to nuovoStato)
                transaction.update(docRef, "orari", newOrariMap)
            }
            val numeroMedicineStr = snapshot.getString("numero_medicine")
            val numeroMedicine = numeroMedicineStr?.toIntOrNull() ?: 0
            if (numeroMedicine > 0) {
                transaction.update(docRef, "numero_medicine", (numeroMedicine - 1).toString())
            }

            null
        }.addOnSuccessListener {
            Log.d("TAG", "DocumentSnapshot successfully updated!")
        }.addOnFailureListener { e ->
            Log.w("TAG", "Error updating document", e)
        }
    }

    fun inserireMedicina(foto: Uri?) {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = user?.uid
        if (uid == null) {
            Log.w("MedicinaViewModel", "Utente non autenticato")
            return
        }
        imageUri = foto
        uploadImageAndGetUrl { imageUrl ->
            val sanitizedOrari = orari.value?.filterKeys { it != "initialized" }?.mapValues { (_, orarioData) ->
                Orario(
                    orario = orarioData.orario,
                    stato = orarioData.stato
                )
            } ?: emptyMap()

            val medicina = hashMapOf(
                "user_id" to uid,
                "nome" to (nomeMedicina.value ?: "0"),
                "numero_medicine" to (numeroMedicine.value ?: "0"),
                "Numero_Per_Giorno" to (voltePerGiorno.value ?: "0"),
                "Data_inizio" to (dataInizio.value ?: medicineX.value?.Data_inizio),
                "foto" to imageUrl,
                "orari" to sanitizedOrari
            )

            db.collection("medicine")
                .add(medicina)
                .addOnSuccessListener { documentReference ->
                    val medicinaId = documentReference.id
                        db.collection("medicine")
                            .document(documentReference.id)
                            .update("id", medicinaId)
                        .addOnSuccessListener {
                            Log.w(TAG, "Documento aggiunto e aggiornato con ID: $medicinaId")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Errore durante l'aggiornamento del documento", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Errore durante l'aggiunta del documento", e)
                }
        }
    }

    fun aggiornaMedicina(foto: Uri?,id_medicina: String?){

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = user?.uid
        if (uid == null) {
            Log.w("MedicinaViewModel", "Utente non autenticato")
            return
        }
        imageUri = foto
        uploadImageAndGetUrl { imageUrl ->
            // Sanitizzazione e preparazione degli orari
            val sanitizedOrari = orari.value?.filterKeys { it != "initialized" }?.mapValues { (_, orarioData) ->
                Orario(
                    orario = orarioData.orario,
                    stato = orarioData.stato
                )
            } ?: emptyMap()
        val updates = hashMapOf(
            "user_id" to uid,
            "nome" to (nomeMedicina.value ?: "0"),
            "numero_medicine" to (numeroMedicine.value ?: "0"),
            "Numero_Per_Giorno" to (voltePerGiorno.value ?: "0"),
            "Data_inizio" to (dataInizio.value ?: "0"),
            "foto" to imageUrl,
            "orari" to sanitizedOrari
        )

            if (id_medicina != null) {
                db.collection("medicine").document(id_medicina)
                    .update(updates as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d("ViewModel", "Documento aggiornato con successo")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ViewModel", "Errore durante l'aggiornamento del documento", e)
                    }
            }
    }
    }

    fun datiMedicinaX(id_medicina: String?) {
        if (id_medicina != null) {
            db.collection("medicine")
                .document(id_medicina)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        _medicineX.postValue(null)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _medicineX.postValue(snapshot.toObject(MedicinaData::class.java))
                    } else {
                        _medicineX.postValue(null)
                    }
                }
        } else {
            _medicineX.postValue(null)
        }
    }
    private fun updateTimesPerDay(times: String?) {
        val timesList = mutableListOf<String>()
        times?.toIntOrNull()?.let {
            for (i in 1..it) {
                timesList.add("Seleziona Ora $i")
            }
        }
        pulsantiOra.value = timesList
    }

    private fun uploadImageAndGetUrl(callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("medicine_photo/${(nomeMedicina.value ?: "0")}.jpg")

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
        } ?: callback(null)
    }

    fun setOrari(id: Int, value: String?) {
        val currentOrari = _orari.value ?: mutableMapOf()

        val updatedOrari = currentOrari.toMutableMap()

        value?.let {
            updatedOrari[id.toString()] = Orario(orario = it, stato = false)
        } ?: run {
            updatedOrari.remove(id.toString())
        }

        _orari.value = updatedOrari
    }


    private fun prepareExpandedData(medicinaDataList: List<MedicinaData>): List<ExpandedMedicineItem> {
        val expandedList = mutableListOf<ExpandedMedicineItem>()

        for (medicina in medicinaDataList) {
            medicina.orari.forEach { (id, orario) ->
               if(orario.stato==false){ expandedList.add(
                    ExpandedMedicineItem(
                        id =medicina.id,
                        user_id = medicina.user_id,
                        nome = medicina.nome,
                        numero_medicine = medicina.numero_medicine,
                        Numero_Per_Giorno = medicina.Numero_Per_Giorno,
                        Data_inizio = medicina.Data_inizio,
                        foto = medicina.foto,
                        orario = orario.orario,
                        stato=orario.stato
                    )
                )
            }}
        }

        return expandedList
    }

    fun deleteMedicina(id_medicina: String){
        if (id_medicina == null) {
            Log.e("ViewModel", "Invalid appointment ID")
            return
        }
        val currentList = _medicineList.value?.toMutableList() ?: mutableListOf()
        db.collection("medicine").document(id_medicina)
            .delete()
            .addOnSuccessListener {
                val iterator = currentList.iterator()
                while (iterator.hasNext()) {
                    val medicina = iterator.next()
                    if (medicina.id == id_medicina) {
                        iterator.remove()
                        break
                    }
                }
                _medicineList.value = currentList
                Log.d("ViewModel", "Appointment deleted and list updated")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore error", "Error deleting appointment", e)
            }
    }
    fun shareMedicineInfo(context: Context, medicina: MedicinaData) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.setPackage("com.whatsapp")

        val shareContent = """
            Nome: ${medicina.nome}
            Data Inizio: ${medicina.Data_inizio}
            Numero Medicine: ${medicina.numero_medicine}
            Volte per Giorno: ${medicina.Numero_Per_Giorno}
            Orari: ${medicina.orari.values.joinToString { it.orario }}
        """.trimIndent()

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent)

        try {
            context.startActivity(shareIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp non è installato.", Toast.LENGTH_SHORT).show()
        }
    }
    fun shareMedicineInfoSpecifico(context: Context, medicina: ExpandedMedicineItem) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.setPackage("com.whatsapp")

        val shareContent = """
            Nome: ${medicina.nome}
            Data Inizio: ${medicina.Data_inizio}
            Numero Medicine: ${medicina.numero_medicine}
            Volte per Giorno: ${medicina.Numero_Per_Giorno}
            Orari: ${medicina.orario}
        """.trimIndent()

        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent)

        try {
            context.startActivity(shareIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp non è installato.", Toast.LENGTH_SHORT).show()
        }
    }


    data class Orario(
        val orario:String="",
        val stato:Boolean=false
    )
}