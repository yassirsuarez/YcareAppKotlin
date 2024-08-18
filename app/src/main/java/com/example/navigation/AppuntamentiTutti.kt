package com.example.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navigation.databinding.AppuntamentiTuttiBinding
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class AppuntamentiTutti : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: AppuntamentiTuttiBinding
    private lateinit var myAdapter: AdapterAppuntamenti
    private lateinit var auth: FirebaseAuth
    private val viewModel: AppuntamentiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppuntamentiTuttiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val toolbar: Toolbar = binding.toolbar2
        setSupportActionBar(toolbar)
        recyclerView = binding.storicoAppuntamenti
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        myAdapter=AdapterAppuntamenti(arrayListOf(),
            onDeleteClick = { appuntamenti ->
                viewModel.deleteAppuntamento(appuntamenti.id)
            },
            onUpdateClick = { appuntamenti ->
                val intent= Intent(this,DatiAppuntamenti::class.java)
                intent.putExtra("titolo",appuntamenti.titolo)
                intent.putExtra("luogo",appuntamenti.luogo)
                intent.putExtra("data",appuntamenti.data)
                intent.putExtra("ora",appuntamenti.ora)
                intent.putExtra("id_appuntamento",appuntamenti.id)
                startActivity(intent)
            },
            onLuogoClick = {appuntamento->
                openAddressInGoogleMaps(appuntamento.luogo)},
            2)
        recyclerView.adapter = myAdapter
        viewModel.appuntamentiList.observe(this, { appuntamentiList ->
            myAdapter.updateData(appuntamentiList)
        })
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            viewModel.datiAppuntamentiLista(uid)}
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    fun openAddressInGoogleMaps(address: String?) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
                startActivity(mapIntent)

        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Google Maps non è installato.", Toast.LENGTH_SHORT).show()
        }
    }

}