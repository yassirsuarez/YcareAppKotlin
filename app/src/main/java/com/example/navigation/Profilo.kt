package com.example.navigation

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide

class Profilo:AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val viewModel: DatiUtenteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profilo_layout)

         var nomecognome: String? = null
         var sesso: String? = null
         var compleanno: String? = null
        var imageUri: String? = null

        val imageUtente= findViewById<ImageView>(R.id.user_image)


        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        // Ottieni l'utente corrente
        val user = auth.currentUser

        // Verifica se l'utente è autenticato
        user?.let {
            val uid = it.uid

            viewModel.fetchUserData(uid)
            viewModel.fetchDatiSalute(uid)

        } ?: run {
            Log.d(TAG, "No user is currently signed in")
        }
        val intent2=Intent(this,DatiSalute::class.java)

        viewModel.peso.observe(this, Observer { peso ->
            findViewById<TextView>(R.id.peso).text = peso
            intent2.putExtra("peso",peso)
        })

        viewModel.altezza.observe(this, Observer { altezza ->
            findViewById<TextView>(R.id.altezza).text = altezza
            intent2.putExtra("altezza",altezza)
        })

        viewModel.data.observe(this, Observer { data ->
            findViewById<TextView>(R.id.data).text= data
            intent2.putExtra("data",data)
        })
        viewModel.userData.observe(this, Observer { userData ->
            userData?.let {
                nomecognome=it["nome_cognome"]
                compleanno = it["data_nascita"]
                sesso = it["sesso"]
                findViewById<TextView>(R.id.nome_cognome).text = nomecognome
                findViewById<TextView>(R.id.genere).text = sesso
                findViewById<TextView>(R.id.data_nascita).text = compleanno
                imageUri = it["foto"]

                imageUri?.let { uriString ->
                    Glide.with(this)
                        .load(uriString)
                        .into(imageUtente)
                }

            }})
        findViewById<Button>(R.id.modifica_datiUtente).setOnClickListener {
            val intent = Intent(this, DatiUtente::class.java)
            intent.putExtra("nomecognome", nomecognome)
            intent.putExtra("sesso", sesso)
            intent.putExtra("compleanno", compleanno)
            intent.putExtra("foto",imageUri)
            startActivity(intent)
        }
        findViewById<Button>(R.id.modifica_datisalute).setOnClickListener{
            startActivity(intent2)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}
