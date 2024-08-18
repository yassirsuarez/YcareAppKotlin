package com.example.navigation

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.net.Uri
import com.bumptech.glide.Glide
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class DatiUtente : AppCompatActivity() {
    lateinit var nome: EditText
    lateinit var cognome: EditText
    private lateinit var btnconferma: Button
    lateinit var data_nascita: EditText
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null
    private lateinit var imageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dati_personali)

        auth = Firebase.auth
        nome = findViewById(R.id.nome)
        cognome = findViewById(R.id.cognome)
        data_nascita = findViewById(R.id.DataNascita)

        data_nascita.setOnClickListener {
            showDatePickerDialog()
        }
        btnconferma = findViewById(R.id.conferma)
        imageView = findViewById(R.id.foto_utente)

        val nomecognome = intent.getStringExtra("nomecognome")
        val sesso = intent.getStringExtra("sesso")
        val compleanno = intent.getStringExtra("compleanno")
        val foto = intent.getStringExtra("foto")

        if (nomecognome != null && sesso != null && compleanno != null) {
            val parts = nomecognome.split(" ")
            if (parts.size >= 2) {
                nome.setText(parts[0])
                cognome.setText(parts[1])
            } else {
                nome.setText(parts[0])
                cognome.setText("")
            }
            data_nascita.setText(compleanno)
            foto?.let { uriString ->
                Glide.with(this)
                    .load(uriString)
                    .into(imageView)
            }
            val radioGroupSesso = findViewById<RadioGroup>(R.id.radioGroupSesso)
            if (sesso == "Maschio") {
                radioGroupSesso.check(R.id.SessoM)
            } else {
                radioGroupSesso.check(R.id.sessoF)
            }
        }
        btnconferma.setOnClickListener {
            val user = auth.currentUser
            user?.let {
                val uid = it.uid
                if (nomecognome != null && sesso != null && compleanno != null) {
                    UpdateData(uid)
                } else setupUserDatabase(uid)
            }

        }
        imageView.setOnClickListener {
            selectImage()
        }


    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                Glide.with(this).load(it).into(imageView)
            }
        }
    }

    private fun uploadImageAndGetUrl(uid: String, callback: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("user_photo/${uid}.jpg")

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

    private fun setupUserDatabase(uid: String) {
        val db = Firebase.firestore
        val radioGroupSesso = findViewById<RadioGroup>(R.id.radioGroupSesso)
        var sesso = true

        when (radioGroupSesso.checkedRadioButtonId) {
            R.id.SessoM -> sesso = true
            R.id.sessoF -> sesso = false
            else -> {
            }
        }

        uploadImageAndGetUrl(uid) { imageUrl ->
            val user = hashMapOf(
                "id" to uid,
                "nome" to nome.text.toString(),
                "cognome" to cognome.text.toString(),
                "sesso" to sesso,
                "data_nascita" to data_nascita.text.toString(),
                "foto" to imageUrl
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.w(TAG, "Documento aggiunto con ID: ${documentReference.id}")
                    // Avvia MainActivity e chiudi l'attività corrente
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Errore nell'aggiungere il documento", e)
                }
        }
    }

    private fun UpdateData(uid: String) {
        val db = Firebase.firestore
        val radioGroupSesso = findViewById<RadioGroup>(R.id.radioGroupSesso)
        var sesso = true

        when (radioGroupSesso.checkedRadioButtonId) {
            R.id.SessoM -> sesso = true
            R.id.sessoF -> sesso = false
            else -> {
            }
        }

        uploadImageAndGetUrl(uid) { imageUrl ->
            val updates = hashMapOf(
                "nome" to nome.text.toString(),
                "cognome" to cognome.text.toString(),
                "sesso" to sesso,
                "data_nascita" to data_nascita.text.toString(),
                "foto" to imageUrl
            )

            db.collection("users")
                .whereEqualTo("id", uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.documents.isEmpty()) {
                        Log.d(TAG, "Nessun documento trovato per l'ID: $uid")
                        return@addOnSuccessListener
                    }

                    for (document in querySnapshot.documents) {
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
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Errore durante la query: ", exception)
                }
        }
    }
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate.time)

                data_nascita.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
}