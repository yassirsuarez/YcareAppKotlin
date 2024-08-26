package com.example.navigation

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Toast

class DatiSalute : AppCompatActivity() {
    lateinit var peso: EditText
    lateinit var altezza: EditText
    private lateinit var btnconferma: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dati_salute)

        val pesoIn = intent.getStringExtra("peso")
        val altezzaIn = intent.getStringExtra("altezza")
        val dataIn = intent.getStringExtra("data")

        auth = Firebase.auth
        peso=findViewById(R.id.peso)
        altezza=findViewById(R.id.altezza)
        btnconferma=findViewById(R.id.conferma)
        peso.setText(pesoIn)
        altezza.setText(altezzaIn)
        btnconferma.setOnClickListener {
            val user = auth.currentUser
            user?.let {
                val uid = it.uid
                if(pesoIn!=null && altezzaIn!=null){
                    updateDatiSalute(uid, dataIn)
                }
                else setupUserDatabase(uid)  }
        }
    }
    private fun updateDatiSalute(uid: String, dataIn: String?) {
        val db = Firebase.firestore
        if (peso.text.toString().isNullOrEmpty() || altezza.text.toString().isNullOrEmpty() ) {
            Toast.makeText(this, "Completa tutti i campi obbligatori", Toast.LENGTH_SHORT).show()
            return
        }
        val updates = hashMapOf(
            "peso" to peso.text.toString(),
            "altezza" to altezza.text.toString()
        )

        if (dataIn != null) {
            db.collection("dati_salute")
                .whereEqualTo("id", uid)
                .whereEqualTo("data", dataIn)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        document.reference.update(updates as Map<String, Any>)
                            .addOnSuccessListener {
                                Log.d(TAG, "Documento aggiornato con successo")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Errore durante l'aggiornamento del documento: ", exception)
                            }
                    } else {
                        Log.d(TAG, "Nessun documento trovato per l'aggiornamento")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Errore durante la query: ", exception)
                }
        } else {
            Log.w(TAG, "Data non può essere null")
        }
    }
    private fun setupUserDatabase(uid: String) {
        val db = Firebase.firestore
        if (peso.text.toString().isNullOrEmpty() || altezza.text.toString().isNullOrEmpty() ) {
            Toast.makeText(this, "Completa tutti i campi obbligatori", Toast.LENGTH_SHORT).show()
            return
        }
        val salute = hashMapOf(
            "id" to uid,
            "peso" to peso.text.toString(),
            "altezza" to altezza.text.toString(),
            "data" to getCurrentDate())

        db.collection("dati_salute")
            .add(salute)
            .addOnSuccessListener { documentReference->
                Log.w(TAG,"Documento aggiunto con ID: $(documentReference.id)")
            }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }


}