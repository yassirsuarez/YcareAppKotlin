package com.example.navigation

import android.app.Activity
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import android.net.Uri
import androidx.activity.viewModels
import androidx.lifecycle.Observer

class DatiDottore : AppCompatActivity() {
        private lateinit var db: FirebaseFirestore
        private var viewIdCounter = 1
        private lateinit var auth: FirebaseAuth
        private val buttonMap = SparseArray<Button>()
        private lateinit var uid: String
        private lateinit var fotoDoctor: ImageView
        private var imageUri: Uri? = null
        private val viewModel: DottoreViewModel by viewModels()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.dati_dottore)

            if(intent.getIntExtra("tipo",0)==1){
               viewModel.datiDottore()
                viewModel.nome.observe(this, Observer { nome ->
                    findViewById<EditText>(R.id.nome_dottore).setText(nome)
                })
                viewModel.luogo.observe(this, Observer { luogo ->
                    findViewById<EditText>(R.id.luogo_dottore).setText(luogo)
                })
                viewModel.numero.observe(this, Observer { numero ->
                    findViewById<EditText>(R.id.numero_dott).setText(numero)
                })
                viewModel.foto.observe(this, { foto ->
                    foto?.let { uriString ->
                        Glide.with(this)
                            .load(uriString)
                            .into(findViewById<ImageView>(R.id.foto_dottore))
                    }
                })
            }

            var imageUri: String? = null
            // Inizializzazione di FirebaseAuth e Firestore
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            fotoDoctor = findViewById(R.id.foto_dottore)
            fotoDoctor.setOnClickListener {
                selectImage()
            }

            val user = auth.currentUser
            uid = user?.uid ?: run {
                Toast.makeText(this, "Utente non autenticato", Toast.LENGTH_SHORT).show()
                return
            }

        val mainLayout = findViewById<LinearLayout>(R.id.main_layout)
        val daysOfWeek = arrayOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")
        for (day in daysOfWeek) {
            val dayContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, 0, 16)
            }

            val dayTextView = TextView(this).apply {
                text = day
                textSize = 18f
                setPadding(0, 0, 0, 8)
            }
            dayContainer.addView(dayTextView)
            if(intent.getIntExtra("tipo",0)==1){
                viewModel.orari.observe(this, Observer { orari ->
                    val morningLayout = createTimePickerLayout("Mattina", day,orari)
                    val afternoonLayout = createTimePickerLayout("Pomeriggio", day,orari)
                    dayContainer.addView(morningLayout)
                    dayContainer.addView(afternoonLayout)
                })
            }
            else{
            val morningLayout = createTimePickerLayout("Mattina", day,null)

            val afternoonLayout = createTimePickerLayout("Pomeriggio",day, null)

            dayContainer.addView(morningLayout)
            dayContainer.addView(afternoonLayout)}

            mainLayout.addView(dayContainer)
        }

       findViewById<Button>(R.id.conferma).setOnClickListener(){
           if(intent.getIntExtra("tipo",0)==1){
               val orari = mutableMapOf<String, MutableMap<String, String>>()
               val daysOfWeek = arrayOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")

               for (day in daysOfWeek) {
                   val daySchedule = mutableMapOf<String, String>()

                   val morningStartButton = buttonMap[generateIdForDayAndSession(day, "MattinaInizio")]
                   val morningEndButton = buttonMap[generateIdForDayAndSession(day, "MattinaFine")]
                   val afternoonStartButton = buttonMap[generateIdForDayAndSession(day, "PomeriggioInizio")]
                   val afternoonEndButton = buttonMap[generateIdForDayAndSession(day, "PomeriggioFine")]

                   daySchedule["MattinaInizio"] = viewModel.verificaOra(morningStartButton?.text.toString())
                   daySchedule["MattinaFine"] = viewModel.verificaOra(morningEndButton?.text.toString())
                   daySchedule["PomeriggioInizio"] = viewModel.verificaOra(afternoonStartButton?.text.toString())
                   daySchedule["PomeriggioFine"] = viewModel.verificaOra(afternoonEndButton?.text.toString())

                   orari[day] = daySchedule
               }
               uploadImageAndGetUrl(uid) { imageUrl ->
               viewModel.updateDottore(uid,findViewById<EditText>(R.id.nome_dottore).text.toString(),
                   findViewById<EditText>(R.id.luogo_dottore).text.toString(),
                   findViewById<EditText>(R.id.numero_dott).text.toString(),imageUrl,orari)
               }
           }
           else saveDataToFirestore(uid)
          finish()
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
                Glide.with(this).load(it).into(fotoDoctor)
            }
        }
    }
    private fun createTimePickerLayout(label: String, day: String, orari: Map<String, Map<String, String>>?): LinearLayout {
        val timePickerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 8)
        }

        val timeLabel = TextView(this).apply {
            text = label
            textSize = 16f
            setPadding(0, 0, 8, 0)
        }
        val startKey = "${label}Inizio"
        val endKey = "${label}Fine"
        val startButton = Button(this).apply {
            text = orari?.get(day)?.get(startKey)?.takeIf { it != "--:--" } ?: "Inizio"
            id = generateUniqueId()
        }

        // Usa un ID unico per "Inizio" e "Fine"
        val endButton = Button(this).apply {
            text = orari?.get(day)?.get(endKey)?.takeIf { it != "--:--" } ?: "Fine"
            id = generateUniqueId()
            setPadding(8, 0, 0, 0)
        }

        buttonMap.put(startButton.id, startButton)
        buttonMap.put(endButton.id, endButton)

        setupTimePicker(startButton)
        setupTimePicker(endButton)

        timePickerLayout.addView(timeLabel)
        timePickerLayout.addView(startButton)
        timePickerLayout.addView(endButton)

        return timePickerLayout
    }
    private fun generateUniqueId(): Int {
        return viewIdCounter++
    }
    private fun setupTimePicker(button: Button) {
        button.setOnClickListener {
            val timePickerDialog = TimePickerDialog(this,
                { _, hourOfDay, minute ->
                    button.text = String.format("%02d:%02d", hourOfDay, minute)
                }, 8, 0, true)
            timePickerDialog.show()
        }
    }

    private fun saveDataToFirestore(id_user:String) {
        val nome = findViewById<EditText>(R.id.nome_dottore).text.toString()
        val luogo =  findViewById<EditText>(R.id.luogo_dottore).text.toString()
        val numero= findViewById<EditText>(R.id.numero_dott).text.toString()
        val orari = mutableMapOf<String, MutableMap<String, String>>()
        val daysOfWeek = arrayOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")

        for (day in daysOfWeek) {
            val daySchedule = mutableMapOf<String, String>()

            val morningStartButton = buttonMap[generateIdForDayAndSession(day, "MattinaInizio")]
            val morningEndButton = buttonMap[generateIdForDayAndSession(day, "MattinaFine")]
            val afternoonStartButton = buttonMap[generateIdForDayAndSession(day, "PomeriggioInizio")]
            val afternoonEndButton = buttonMap[generateIdForDayAndSession(day, "PomeriggioFine")]

            daySchedule["MattinaInizio"] = viewModel.verificaOra(morningStartButton?.text.toString())
            daySchedule["MattinaFine"] = viewModel.verificaOra(morningEndButton?.text.toString())
            daySchedule["PomeriggioInizio"] = viewModel.verificaOra(afternoonStartButton?.text.toString())
            daySchedule["PomeriggioFine"] = viewModel.verificaOra(afternoonEndButton?.text.toString())

            orari[day] = daySchedule
        }
        uploadImageAndGetUrl(id_user) { imageUrl ->
            val userData = mapOf(
                "id_user" to id_user,
                "nome" to nome,
                "luogo" to luogo,
                "numero" to numero,
                "orari" to orari,
                "foto" to imageUrl
            )

        db.collection("dottore")
            .document(id_user) // Utilizza l'UID dell'utente come nome del documento
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Dati salvati con successo", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Errore durante il salvataggio: ${e.message}", Toast.LENGTH_SHORT).show()
            }}
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
    private fun generateIdForDayAndSession(day: String, session: String): Int {
        val id = when(day) {
            "Lunedì" -> when(session) {
                "MattinaInizio" -> 1
                "MattinaFine" -> 2
                "PomeriggioInizio" -> 3
                "PomeriggioFine" -> 4
                else -> -1
            }
            "Martedì" -> when(session) {
                "MattinaInizio" -> 5
                "MattinaFine" -> 6
                "PomeriggioInizio" -> 7
                "PomeriggioFine" -> 8
                else -> -1
            }
            "Mercoledì" -> when(session) {
                "MattinaInizio" -> 9
                "MattinaFine" -> 10
                "PomeriggioInizio" -> 11
                "PomeriggioFine" -> 12
                else -> -1
            }
            "Giovedì" -> when(session) {
                "MattinaInizio" -> 13
                "MattinaFine" -> 14
                "PomeriggioInizio" -> 15
                "PomeriggioFine" -> 16
                else -> -1
            }
            "Venerdì" -> when(session) {
                "MattinaInizio" -> 17
                "MattinaFine" -> 18
                "PomeriggioInizio" -> 19
                "PomeriggioFine" -> 20
                else -> -1
            }
            "Sabato" -> when(session) {
                "MattinaInizio" -> 21
                "MattinaFine" -> 22
                "PomeriggioInizio" -> 23
                "PomeriggioFine" -> 24
                else -> -1
            }
            "Domenica" -> when(session) {
                "MattinaInizio" -> 25
                "MattinaFine" -> 26
                "PomeriggioInizio" -> 27
                "PomeriggioFine" -> 28
                else -> -1
            }
            else -> -1
        }
        return id
    }
}
