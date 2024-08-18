package com.example.navigation

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.navigation.databinding.StoricoBinding
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import androidx.fragment.app.viewModels
import java.time.format.DateTimeFormatter
import androidx.lifecycle.Observer
import androidx.core.content.ContextCompat

class Storico : Fragment() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding: StoricoBinding
    private val viewModel: DatiUtenteViewModel by viewModels()
    private var peso: Int? = null
    private var altezza: Int? = null
    private var dataValue: String? = null

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val user = auth.currentUser
            user?.let {
                val uid = it.uid
                viewModel.fetchDatiSalute(uid)
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= StoricoBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            viewModel.fetchDatiSalute(uid)
            binding.storico.setOnClickListener {
                val intent = Intent(requireContext(), Storico_generale::class.java)
                intent.putExtra("id", uid)
                startForResult.launch(intent)
            }
        }
        viewModel.peso.observe(viewLifecycleOwner, Observer { pesoString ->
            peso = pesoString?.toIntOrNull()
            checkAndUpdateIbm()
        })

        viewModel.altezza.observe(viewLifecycleOwner, Observer { altezzaString ->
            altezza = altezzaString?.toIntOrNull()
            checkAndUpdateIbm()
        })
        return binding.root  }

    private fun checkAndUpdateIbm() {
        val pesoInt = peso
        val altezzaInt = altezza
        if (pesoInt != null && altezzaInt != null) {
            ibm(pesoInt, altezzaInt,requireContext())
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.w(TAG, " valore data:$dataValue")
        viewModel.data.observe(viewLifecycleOwner, Observer { data ->
            data?.let {
                dataValue = it }  })

            val today: LocalDate = LocalDate.now()
                val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formattedDate: String = today.format(formatter)

                binding.aggiungi.setOnClickListener {
                    if (dataValue != formattedDate) {
                        startActivity(Intent(requireContext(), DatiSalute::class.java))
                    } else {
                        Toast.makeText(requireContext(), "Esiste già un dato odierno", Toast.LENGTH_SHORT).show()
                    }
                }
    }

    fun ibm(peso: Int, altezza: Int, context: Context?) {
        val altezzaInMetri = altezza / 100.0
        val risultato = peso / (altezzaInMetri * altezzaInMetri)
        val risultatoFormattato = String.format("%.2f", risultato)

        val testo = when {
            risultato < 18.5 -> "sottopeso"
            risultato >= 18.5 && risultato < 25 -> "normopeso"
            risultato >= 25 && risultato < 30 -> "pre-obeso"
            risultato >= 30 && risultato < 35 -> "obeso classe 1"
            risultato >= 35 && risultato < 40 -> "obeso classe 2"
            risultato >= 40 -> "obeso classe 3"
            else -> "errore"
        }

        binding.ibm.text = "$testo : $risultatoFormattato"

        context?.let {
            val color = when (testo) {
                "normopeso" -> ContextCompat.getColor(it, android.R.color.holo_green_dark)
                "sottopeso", "obeso classe 3" -> ContextCompat.getColor(it, android.R.color.holo_red_dark)
                "pre-obeso" -> ContextCompat.getColor(it, android.R.color.holo_orange_light)
                "obeso classe 1", "obeso classe 2" -> ContextCompat.getColor(it, android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(it, android.R.color.black)
            }

            binding.ibm.setTextColor(color)
        }
    }

}